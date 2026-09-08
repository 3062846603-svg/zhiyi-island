#!/usr/bin/env bash
set -euo pipefail
umask 077
backup_root=/var/backups/zhiyi-island
install -d -m 700 "$backup_root"
stamp=$(date +%Y%m%d-%H%M%S)
destination="$backup_root/$stamp"
mkdir -m 700 "$destination"
runuser -u postgres -- /usr/pgsql-16/bin/pg_dump -Fc zhiyi_island > "$destination/database.dump"
/usr/pgsql-16/bin/pg_restore --list "$destination/database.dump" >/dev/null
# Copy object files after stopping writes when a strictly consistent snapshot is required.
tar -czf "$destination/minio.tar.gz" -C /var/lib/zhiyi-minio .
tar -czf "$destination/config.tar.gz" -C /etc zhiyi-island
cd "$destination"
sha256sum database.dump minio.tar.gz config.tar.gz > SHA256SUMS
echo "Backup completed: $destination"
# Keep the newest seven completed backups; remove only timestamp directories under backup_root.
mapfile -t old_backups < <(find "$backup_root" -mindepth 1 -maxdepth 1 -type d -name '20??????-??????' | sort -r | tail -n +8)
for old in "${old_backups[@]}"; do
    [ -f "$old/SHA256SUMS" ] || continue
    case "$old" in "$backup_root"/20??????-??????) rm -rf -- "$old";; esac
done