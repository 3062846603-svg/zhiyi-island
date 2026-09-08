# PostgreSQL数据库优化指南

## 概述

PostgreSQL是一款功能强大的开源对象关系型数据库系统，拥有超过35年的活跃开发历史，以其可靠性、功能健壮性和性能而闻名。本文档将介绍PostgreSQL的核心优化策略。

## 查询优化

### 1. 索引策略

索引是提升查询性能的关键。PostgreSQL支持多种索引类型：

#### B-Tree索引
最常用的索引类型，适用于等值查询和范围查询。

```sql
CREATE INDEX idx_user_username ON users(username);
CREATE INDEX idx_note_create_time ON notes(create_time);
```

#### GIN索引
适用于数组、JSONB、全文搜索等场景。

```sql
CREATE INDEX idx_note_tags ON notes USING GIN(tags);
CREATE INDEX idx_document_content ON documents USING GIN(to_tsvector('english', content));
```

#### 向量索引（pgvector）
用于向量相似度搜索，支持AI应用场景。

```sql
CREATE EXTENSION IF NOT EXISTS vector;
CREATE INDEX idx_note_embedding ON notes USING ivfflat (embedding vector_cosine_ops);
```

### 2. 查询计划分析

使用EXPLAIN分析查询执行计划：

```sql
EXPLAIN ANALYZE SELECT * FROM notes WHERE user_id = 1;
```

关注以下指标：
- 执行时间
- 扫描方式（Seq Scan vs Index Scan）
- 连接方式
- 内存使用

### 3. 查询重写技巧

#### 避免SELECT *
```sql
-- 不推荐
SELECT * FROM notes;

-- 推荐
SELECT id, title, content FROM notes;
```

#### 使用LIMIT优化分页
```sql
-- 深度分页优化
SELECT * FROM notes 
WHERE id > last_id 
ORDER BY id 
LIMIT 20;
```

## 连接池配置

### PgBouncer

PgBouncer是PostgreSQL的轻量级连接池，可以有效管理数据库连接。

配置示例：
```ini
[databases]
zhiyi_island = host=localhost port=5432 dbname=zhiyi_island

[pgbouncer]
pool_mode = transaction
max_client_conn = 1000
default_pool_size = 25
```

### HikariCP

在Java应用中使用HikariCP连接池：

```yaml
spring:
  datasource:
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      idle-timeout: 30000
      max-lifetime: 1800000
      connection-timeout: 30000
```

## 内存优化

### 共享缓冲区

shared_buffers是PostgreSQL最重要的内存参数：

```postgresql
# postgresql.conf
shared_buffers = 4GB  # 通常设置为系统内存的25%
```

### 工作内存

work_mem控制排序和哈希操作的内存使用：

```postgresql
work_mem = 64MB
maintenance_work_mem = 512MB
```

### 有效缓存大小

effective_cache_size帮助优化器估计系统缓存：

```postgresql
effective_cache_size = 12GB  # 系统可用缓存的估计值
```

## 并发控制

### MVCC机制

PostgreSQL使用多版本并发控制（MVCC）处理并发：
- 读操作不阻塞写操作
- 写操作不阻塞读操作
- 需要定期VACUUM清理死元组

### 事务隔离级别

PostgreSQL支持四种隔离级别：
1. READ UNCOMMITTED（实际等同于READ COMMITTED）
2. READ COMMITTED（默认）
3. REPEATABLE READ
4. SERIALIZABLE

### 锁优化

```sql
-- 使用行级锁避免表锁
SELECT * FROM notes WHERE id = 1 FOR UPDATE;

-- 使用ADVISORY锁实现应用层锁
SELECT pg_advisory_lock(12345);
```

## 监控与维护

### 1. 统计信息收集

```sql
-- 手动收集统计信息
ANALYZE notes;

-- 查看表统计信息
SELECT * FROM pg_stat_user_tables WHERE relname = 'notes';
```

### 2. 自动清理配置

```postgresql
autovacuum = on
autovacuum_max_workers = 3
autovacuum_naptime = 1min
```

### 3. 慢查询日志

```postgresql
log_min_duration_statement = 1000  # 记录超过1秒的查询
```

## 备份与恢复

### pg_dump逻辑备份

```bash
# 备份单个数据库
pg_dump -U postgres zhiyi_island > backup.sql

# 备份所有数据库
pg_dumpall -U postgres > all_backup.sql
```

### 物理备份（WAL归档）

```postgresql
archive_mode = on
archive_command = 'cp %p /backup/wal/%f'
wal_level = replica
```

## 总结

PostgreSQL的性能优化是一个系统工程，需要从查询、索引、内存、并发等多个维度综合考虑。定期监控和维护是保持数据库高性能运行的关键。建议建立完善的监控体系，及时发现和解决性能瓶颈。
