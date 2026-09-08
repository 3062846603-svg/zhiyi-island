#!/usr/bin/env bash
set -euo pipefail
cd /root/zhiyi-install
test "$(id -u)" = 0
id zhiyi >/dev/null 2>&1 || useradd --system --home-dir /opt/zhiyi-island --shell /sbin/nologin zhiyi
id zhiyi-minio >/dev/null 2>&1 || useradd --system --home-dir /var/lib/zhiyi-minio --shell /sbin/nologin zhiyi-minio
install -d -m 755 /etc/zhiyi-island /opt/zhiyi-island/backend /opt/zhiyi-island/frontend
install -d -m 750 -o zhiyi -g zhiyi /opt/zhiyi-island/state
install -d -m 750 -o redis -g redis /var/lib/zhiyi-redis
install -d -m 750 -o zhiyi-minio -g zhiyi-minio /var/lib/zhiyi-minio
if [ ! -f /etc/zhiyi-island/infra.env ]; then
    umask 077
    cat > /etc/zhiyi-island/infra.env <<EOF
DB_URL=jdbc:postgresql://127.0.0.1:5432/zhiyi_island
DB_USERNAME=zhiyi
DB_PASSWORD=$(openssl rand -hex 24)
REDIS_HOST=127.0.0.1
REDIS_PORT=6379
REDIS_PASSWORD=$(openssl rand -hex 24)
MINIO_ENDPOINT=http://127.0.0.1:9005
MINIO_ACCESS_KEY=zhiyi-storage
MINIO_SECRET_KEY=$(openssl rand -hex 24)
JWT_SECRET=$(openssl rand -hex 32)
EOF
fi
set -a
. /etc/zhiyi-island/infra.env
set +a
umask 077
cat > /etc/zhiyi-island/minio.env <<EOF
MINIO_ROOT_USER=$MINIO_ACCESS_KEY
MINIO_ROOT_PASSWORD=$MINIO_SECRET_KEY
EOF
cat > /etc/zhiyi-island/redis.conf <<EOF
bind 127.0.0.1
protected-mode yes
port 6379
daemonize no
dir /var/lib/zhiyi-redis
requirepass $REDIS_PASSWORD
maxmemory 64mb
maxmemory-policy noeviction
appendonly yes
appendfsync everysec
save 900 1
EOF
chown root:redis /etc/zhiyi-island/redis.conf
chmod 640 /etc/zhiyi-island/redis.conf
if [ ! -f /var/lib/pgsql/16/data/PG_VERSION ]; then
    /usr/pgsql-16/bin/postgresql-16-setup initdb
fi
cat > /var/lib/pgsql/16/data/zhiyi.conf <<'EOF'
listen_addresses = '127.0.0.1'
max_connections = 30
shared_buffers = '128MB'
work_mem = '4MB'
maintenance_work_mem = '64MB'
effective_cache_size = '512MB'
password_encryption = 'scram-sha-256'
EOF
chown postgres:postgres /var/lib/pgsql/16/data/zhiyi.conf
grep -q "include_if_exists = 'zhiyi.conf'" /var/lib/pgsql/16/data/postgresql.conf || echo "include_if_exists = 'zhiyi.conf'" >> /var/lib/pgsql/16/data/postgresql.conf
if [ ! -f /var/lib/pgsql/16/data/pg_hba.conf.zhiyi-backup ]; then
    cp /var/lib/pgsql/16/data/pg_hba.conf /var/lib/pgsql/16/data/pg_hba.conf.zhiyi-backup
fi
cat > /var/lib/pgsql/16/data/pg_hba.conf <<'EOF'
local all all peer
host all all 127.0.0.1/32 scram-sha-256
EOF
chown postgres:postgres /var/lib/pgsql/16/data/pg_hba.conf
systemctl enable --now postgresql-16
systemctl reload postgresql-16
if ! runuser -u postgres -- /usr/pgsql-16/bin/psql -tAc "SELECT 1 FROM pg_roles WHERE rolname='zhiyi'" | grep -q 1; then
    runuser -u postgres -- /usr/pgsql-16/bin/psql -v ON_ERROR_STOP=1 -c "CREATE ROLE zhiyi LOGIN PASSWORD '$DB_PASSWORD';"
fi
if ! runuser -u postgres -- /usr/pgsql-16/bin/psql -tAc "SELECT 1 FROM pg_database WHERE datname='zhiyi_island'" | grep -q 1; then
    runuser -u postgres -- /usr/pgsql-16/bin/createdb -O zhiyi zhiyi_island
fi
runuser -u postgres -- /usr/pgsql-16/bin/psql -v ON_ERROR_STOP=1 -d zhiyi_island -c 'CREATE EXTENSION IF NOT EXISTS vector;'
if ! runuser -u postgres -- /usr/pgsql-16/bin/psql -d zhiyi_island -tAc "SELECT to_regclass('public.note') IS NOT NULL" | grep -q t; then
    export PGPASSWORD="$DB_PASSWORD"
    /usr/pgsql-16/bin/psql -h 127.0.0.1 -U zhiyi -d zhiyi_island -v ON_ERROR_STOP=1 --single-transaction -f /root/zhiyi-install/init.sql
    unset PGPASSWORD
fi
if [ ! -f /swapfile-zhiyi ]; then
    fallocate -l 2G /swapfile-zhiyi
    chmod 600 /swapfile-zhiyi
    mkswap /swapfile-zhiyi
fi
swapon --show=NAME --noheadings | grep -qx /swapfile-zhiyi || swapon /swapfile-zhiyi
grep -q '^/swapfile-zhiyi ' /etc/fstab || echo '/swapfile-zhiyi none swap sw 0 0' >> /etc/fstab
echo 'vm.swappiness=10' > /etc/sysctl.d/90-zhiyi-swap.conf
sysctl -p /etc/sysctl.d/90-zhiyi-swap.conf
install -m 644 application-cloud.yml logback-cloud.xml /etc/zhiyi-island/
install -m 644 zhiyi-backend.service zhiyi-redis.service zhiyi-minio.service /etc/systemd/system/
if [ ! -f /etc/nginx/nginx.conf.zhiyi-backup ]; then cp /etc/nginx/nginx.conf /etc/nginx/nginx.conf.zhiyi-backup; fi
install -m 644 nginx.conf /etc/nginx/nginx.conf
nginx -t
systemctl daemon-reload
systemctl enable --now zhiyi-redis
echo 'Database, Redis, swap and service configurations prepared.'