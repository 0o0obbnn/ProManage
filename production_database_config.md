# ProManage Production Database Configuration

## Production Environment Setup

This document outlines the production database configuration for ProManage, including high availability, security, and performance optimization settings.

## Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Application   │    │  Connection     │    │   PostgreSQL    │
│   Servers       │───▶│  Pool           │───▶│   Primary       │
│   (Multiple)    │    │  (PgBouncer)    │    │   Server        │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                       │
                                                       ▼
                                               ┌─────────────────┐
                                               │   PostgreSQL    │
                                               │   Read Replica  │
                                               │   (Multiple)    │
                                               └─────────────────┘
```

## PostgreSQL Configuration

### postgresql.conf (Production)
```ini
# PostgreSQL 15 Production Configuration for ProManage
# File: /etc/postgresql/15/main/postgresql.conf

#------------------------------------------------------------------------------
# CONNECTIONS AND AUTHENTICATION
#------------------------------------------------------------------------------
listen_addresses = '*'
port = 5432
max_connections = 200
superuser_reserved_connections = 3

# SSL Configuration
ssl = on
ssl_cert_file = '/etc/ssl/certs/postgresql.crt'
ssl_key_file = '/etc/ssl/private/postgresql.key'
ssl_ca_file = '/etc/ssl/certs/ca.crt'
ssl_crl_file = ''

#------------------------------------------------------------------------------
# RESOURCE USAGE (except WAL)
#------------------------------------------------------------------------------
# Memory Configuration (for 16GB RAM server)
shared_buffers = 4GB                    # 25% of RAM
effective_cache_size = 12GB             # 75% of RAM
work_mem = 16MB                         # Per connection working memory
maintenance_work_mem = 512MB            # For maintenance operations
max_worker_processes = 8
max_parallel_workers_per_gather = 4
max_parallel_workers = 8
max_parallel_maintenance_workers = 4

# Disk I/O
random_page_cost = 1.1                  # For SSD storage
effective_io_concurrency = 200          # For SSD storage
seq_page_cost = 1.0

#------------------------------------------------------------------------------
# WRITE AHEAD LOG
#------------------------------------------------------------------------------
wal_level = replica
wal_buffers = 32MB
max_wal_size = 4GB
min_wal_size = 512MB
checkpoint_completion_target = 0.9
checkpoint_timeout = 15min
checkpoint_warning = 30s

# Archive settings (for backup)
archive_mode = on
archive_command = 'test ! -f /var/lib/postgresql/wal_archive/%f && cp %p /var/lib/postgresql/wal_archive/%f'
archive_timeout = 300

#------------------------------------------------------------------------------
# REPLICATION
#------------------------------------------------------------------------------
max_wal_senders = 5
wal_keep_size = 512MB
hot_standby = on
hot_standby_feedback = on

#------------------------------------------------------------------------------
# LOGGING
#------------------------------------------------------------------------------
logging_collector = on
log_directory = '/var/log/postgresql'
log_filename = 'postgresql-%Y-%m-%d_%H%M%S.log'
log_file_mode = 0640
log_rotation_age = 1d
log_rotation_size = 100MB

# What to log
log_min_messages = warning
log_min_error_statement = error
log_min_duration_statement = 5000      # Log queries taking > 5 seconds
log_checkpoints = on
log_connections = on
log_disconnections = on
log_lock_waits = on
log_statement = 'ddl'
log_temp_files = 10MB

# CSV logging for analysis
log_destination = 'csvlog'
log_statement_stats = off
log_parser_stats = off
log_planner_stats = off
log_executor_stats = off

#------------------------------------------------------------------------------
# RUNTIME STATISTICS
#------------------------------------------------------------------------------
shared_preload_libraries = 'pg_stat_statements,pg_cron'
track_activities = on
track_counts = on
track_io_timing = on
track_functions = all
stats_temp_directory = '/var/run/postgresql/15-main.pg_stat_tmp'

# pg_stat_statements
pg_stat_statements.max = 10000
pg_stat_statements.track = all
pg_stat_statements.track_utility = off
pg_stat_statements.save = on

#------------------------------------------------------------------------------
# AUTOVACUUM PARAMETERS
#------------------------------------------------------------------------------
autovacuum = on
autovacuum_naptime = 30s
autovacuum_vacuum_threshold = 50
autovacuum_analyze_threshold = 50
autovacuum_vacuum_scale_factor = 0.1
autovacuum_analyze_scale_factor = 0.05
autovacuum_vacuum_cost_delay = 10ms
autovacuum_vacuum_cost_limit = 1000
autovacuum_max_workers = 4

#------------------------------------------------------------------------------
# CLIENT CONNECTION DEFAULTS
#------------------------------------------------------------------------------
search_path = 'public'
default_tablespace = ''
temp_tablespaces = ''
check_function_bodies = on
default_transaction_isolation = 'read committed'
default_transaction_read_only = off
default_transaction_deferrable = off
session_replication_role = 'origin'
statement_timeout = 0
lock_timeout = 0
idle_in_transaction_session_timeout = 600000  # 10 minutes
vacuum_cost_delay = 0
vacuum_cost_page_hit = 1
vacuum_cost_page_miss = 10
vacuum_cost_page_dirty = 20
vacuum_cost_limit = 200

# Locale and Formatting
datestyle = 'iso, mdy'
timezone = 'UTC'
lc_messages = 'en_US.UTF-8'
lc_monetary = 'en_US.UTF-8'
lc_numeric = 'en_US.UTF-8'
lc_time = 'en_US.UTF-8'
default_text_search_config = 'pg_catalog.english'
```

### pg_hba.conf (Production)
```
# PostgreSQL Client Authentication Configuration
# File: /etc/postgresql/15/main/pg_hba.conf

# TYPE  DATABASE        USER            ADDRESS                 METHOD

# Local connections
local   all             postgres                                peer
local   all             all                                     md5

# IPv4 local connections
host    all             all             127.0.0.1/32            md5

# Application connections (with SSL)
hostssl promanage_prod  promanage_app   10.0.0.0/8              md5
hostssl promanage_prod  promanage_readonly  10.0.0.0/8          md5

# Replication connections
hostssl replication     replicator      10.0.0.0/8              md5

# Monitoring connections
hostssl all             promanage_monitor   10.0.0.0/8          md5

# Backup connections
hostssl promanage_prod  promanage_backup    10.0.0.0/8          md5

# Deny all other connections
host    all             all             0.0.0.0/0               reject
```

## Connection Pooling Configuration

### PgBouncer Configuration
```ini
# /etc/pgbouncer/pgbouncer.ini

[databases]
promanage_prod = host=localhost port=5432 dbname=promanage_prod user=promanage_app
promanage_readonly = host=localhost port=5432 dbname=promanage_prod user=promanage_readonly

[pgbouncer]
# Network settings
listen_addr = 0.0.0.0
listen_port = 6432
unix_socket_dir = /var/run/postgresql

# Authentication
auth_type = md5
auth_file = /etc/pgbouncer/userlist.txt
auth_hba_file = /etc/pgbouncer/pg_hba.conf

# Pool settings
pool_mode = transaction
server_reset_query = DISCARD ALL
server_reset_query_always = 0

# Connection limits
max_client_conn = 1000
default_pool_size = 25
min_pool_size = 10
reserve_pool_size = 10
max_db_connections = 50
max_user_connections = 50

# Timeouts
server_connect_timeout = 15
server_login_retry = 15
server_lifetime = 3600
server_idle_timeout = 600
query_timeout = 0
query_wait_timeout = 120
client_idle_timeout = 0
client_login_timeout = 60

# Logging
admin_users = pgbouncer_admin
stats_users = pgbouncer_stats
log_connections = 1
log_disconnections = 1
log_pooler_errors = 1
verbose = 0

# Console access
ignore_startup_parameters = extra_float_digits

# DNS settings
dns_max_ttl = 15
dns_zone_check_period = 0
dns_nxdomain_ttl = 15

# TLS settings
server_tls_sslmode = require
server_tls_ca_file = /etc/ssl/certs/ca.crt
server_tls_cert_file = /etc/ssl/certs/pgbouncer.crt
server_tls_key_file = /etc/ssl/private/pgbouncer.key
```

### PgBouncer User List
```
# /etc/pgbouncer/userlist.txt
"promanage_app" "md5hashed_password_here"
"promanage_readonly" "md5hashed_password_here"
"pgbouncer_admin" "md5hashed_admin_password"
"pgbouncer_stats" "md5hashed_stats_password"
```

## Docker Compose for Production

### Production Docker Compose
```yaml
# docker-compose.prod.yml
version: '3.8'

services:
  postgres-primary:
    image: postgres:15
    container_name: promanage-postgres-primary
    environment:
      POSTGRES_DB: promanage_prod
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: ${POSTGRES_MASTER_PASSWORD}
      POSTGRES_INITDB_ARGS: "--encoding=UTF8 --locale=en_US.UTF-8"
      POSTGRES_REPLICATION_USER: replicator
      POSTGRES_REPLICATION_PASSWORD: ${POSTGRES_REPLICATION_PASSWORD}
    volumes:
      - postgres_primary_data:/var/lib/postgresql/data
      - postgres_wal_archive:/var/lib/postgresql/wal_archive
      - ./postgresql.conf:/etc/postgresql/postgresql.conf
      - ./pg_hba.conf:/etc/postgresql/pg_hba.conf
      - ./ssl:/etc/ssl/postgres
    ports:
      - "5432:5432"
    command: |
      postgres
      -c config_file=/etc/postgresql/postgresql.conf
      -c hba_file=/etc/postgresql/pg_hba.conf
    restart: unless-stopped
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 30s
      timeout: 10s
      retries: 3
    networks:
      - promanage_network

  postgres-replica:
    image: postgres:15
    container_name: promanage-postgres-replica
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: ${POSTGRES_MASTER_PASSWORD}
      PGUSER: postgres
      POSTGRES_MASTER_SERVICE: postgres-primary
      POSTGRES_REPLICA_USER: replicator
      POSTGRES_REPLICA_PASSWORD: ${POSTGRES_REPLICATION_PASSWORD}
    volumes:
      - postgres_replica_data:/var/lib/postgresql/data
      - ./ssl:/etc/ssl/postgres
    ports:
      - "5433:5432"
    depends_on:
      - postgres-primary
    restart: unless-stopped
    networks:
      - promanage_network

  pgbouncer:
    image: pgbouncer/pgbouncer:latest
    container_name: promanage-pgbouncer
    environment:
      DATABASES_HOST: postgres-primary
      DATABASES_PORT: 5432
      DATABASES_USER: promanage_app
      DATABASES_PASSWORD: ${PROMANAGE_APP_PASSWORD}
      DATABASES_DBNAME: promanage_prod
      POOL_MODE: transaction
      MAX_CLIENT_CONN: 1000
      DEFAULT_POOL_SIZE: 25
      MIN_POOL_SIZE: 10
      RESERVE_POOL_SIZE: 10
      AUTH_TYPE: md5
    volumes:
      - ./pgbouncer.ini:/etc/pgbouncer/pgbouncer.ini
      - ./userlist.txt:/etc/pgbouncer/userlist.txt
    ports:
      - "6432:6432"
    depends_on:
      - postgres-primary
    restart: unless-stopped
    networks:
      - promanage_network

  redis:
    image: redis:7-alpine
    container_name: promanage-redis
    command: redis-server --requirepass ${REDIS_PASSWORD} --appendonly yes --maxmemory 2gb --maxmemory-policy allkeys-lru
    volumes:
      - redis_data:/data
    ports:
      - "6379:6379"
    restart: unless-stopped
    networks:
      - promanage_network

  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.9.0
    container_name: promanage-elasticsearch
    environment:
      - node.name=es-node1
      - cluster.name=promanage-cluster
      - discovery.type=single-node
      - "ES_JAVA_OPTS=-Xms2g -Xmx2g"
      - xpack.security.enabled=true
      - ELASTIC_PASSWORD=${ELASTICSEARCH_PASSWORD}
    volumes:
      - elasticsearch_data:/usr/share/elasticsearch/data
    ports:
      - "9200:9200"
    restart: unless-stopped
    networks:
      - promanage_network

  prometheus:
    image: prom/prometheus:latest
    container_name: promanage-prometheus
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus
    ports:
      - "9090:9090"
    restart: unless-stopped
    networks:
      - promanage_network

  grafana:
    image: grafana/grafana:latest
    container_name: promanage-grafana
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=${GRAFANA_PASSWORD}
    volumes:
      - grafana_data:/var/lib/grafana
    ports:
      - "3000:3000"
    restart: unless-stopped
    networks:
      - promanage_network

volumes:
  postgres_primary_data:
    driver: local
    driver_opts:
      type: none
      o: bind
      device: /data/postgres/primary

  postgres_replica_data:
    driver: local
    driver_opts:
      type: none
      o: bind
      device: /data/postgres/replica

  postgres_wal_archive:
    driver: local
    driver_opts:
      type: none
      o: bind
      device: /data/postgres/wal_archive

  redis_data:
  elasticsearch_data:
  prometheus_data:
  grafana_data:

networks:
  promanage_network:
    driver: bridge
```

## Monitoring Configuration

### Prometheus Configuration
```yaml
# prometheus.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  - "/etc/prometheus/rules/*.yml"

scrape_configs:
  - job_name: 'postgresql'
    static_configs:
      - targets: ['postgres_exporter:9187']
    scrape_interval: 30s

  - job_name: 'pgbouncer'
    static_configs:
      - targets: ['pgbouncer_exporter:9127']
    scrape_interval: 30s

  - job_name: 'redis'
    static_configs:
      - targets: ['redis_exporter:9121']
    scrape_interval: 30s

  - job_name: 'promanage-app'
    static_configs:
      - targets: ['app:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s

alerting:
  alertmanagers:
    - static_configs:
        - targets:
          - alertmanager:9093
```

### Database Monitoring Queries
```sql
-- Create monitoring views
CREATE VIEW pg_stat_database_summary AS
SELECT
    datname,
    numbackends,
    xact_commit,
    xact_rollback,
    blks_read,
    blks_hit,
    tup_returned,
    tup_fetched,
    tup_inserted,
    tup_updated,
    tup_deleted,
    conflicts,
    temp_files,
    temp_bytes,
    deadlocks,
    checksum_failures,
    checksum_last_failure,
    blk_read_time,
    blk_write_time,
    stats_reset
FROM pg_stat_database
WHERE datname = 'promanage_prod';

-- Connection monitoring
CREATE VIEW pg_connections_summary AS
SELECT
    state,
    COUNT(*) as connection_count,
    AVG(EXTRACT(EPOCH FROM (now() - state_change))) as avg_duration_seconds
FROM pg_stat_activity
WHERE datname = 'promanage_prod'
GROUP BY state;

-- Lock monitoring
CREATE VIEW pg_locks_summary AS
SELECT
    mode,
    locktype,
    COUNT(*) as lock_count
FROM pg_locks l
JOIN pg_stat_activity a ON l.pid = a.pid
WHERE a.datname = 'promanage_prod'
GROUP BY mode, locktype;
```

## Backup Strategy

### Automated Backup Script
```bash
#!/bin/bash
# /opt/promanage/scripts/backup.sh

set -e

# Configuration
DB_NAME="promanage_prod"
DB_HOST="localhost"
DB_PORT="5432"
DB_USER="promanage_backup"
BACKUP_DIR="/opt/promanage/backups"
RETENTION_DAYS=30
S3_BUCKET="promanage-backups"

# Create timestamp
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="promanage_backup_${TIMESTAMP}"

# Create backup directory
mkdir -p $BACKUP_DIR

# Full database backup
echo "Starting full database backup..."
pg_dump -h $DB_HOST -p $DB_PORT -U $DB_USER -Fc $DB_NAME > $BACKUP_DIR/${BACKUP_FILE}.dump

# Compress backup
echo "Compressing backup..."
gzip $BACKUP_DIR/${BACKUP_FILE}.dump

# Upload to S3 (if configured)
if [ ! -z "$S3_BUCKET" ]; then
    echo "Uploading to S3..."
    aws s3 cp $BACKUP_DIR/${BACKUP_FILE}.dump.gz s3://$S3_BUCKET/database/
fi

# Schema-only backup for quick reference
pg_dump -h $DB_HOST -p $DB_PORT -U $DB_USER -s $DB_NAME > $BACKUP_DIR/schema_${TIMESTAMP}.sql

# Clean up old backups
echo "Cleaning up old backups..."
find $BACKUP_DIR -name "promanage_backup_*.dump.gz" -mtime +$RETENTION_DAYS -delete
find $BACKUP_DIR -name "schema_*.sql" -mtime +$RETENTION_DAYS -delete

# Verify backup integrity
echo "Verifying backup integrity..."
pg_restore --list $BACKUP_DIR/${BACKUP_FILE}.dump.gz > /dev/null

echo "Backup completed successfully: ${BACKUP_FILE}.dump.gz"
```

### Continuous WAL Archiving
```bash
#!/bin/bash
# /opt/promanage/scripts/wal_archive.sh

# Configuration
WAL_ARCHIVE_DIR="/opt/promanage/wal_archive"
S3_BUCKET="promanage-backups"
RETENTION_DAYS=7

# Archive WAL file
WAL_FILE=$1
WAL_PATH=$2

# Copy to local archive
cp $WAL_PATH $WAL_ARCHIVE_DIR/$WAL_FILE

# Upload to S3
aws s3 cp $WAL_ARCHIVE_DIR/$WAL_FILE s3://$S3_BUCKET/wal_archive/

# Clean up old WAL files
find $WAL_ARCHIVE_DIR -name "*.wal" -mtime +$RETENTION_DAYS -delete

echo "WAL file $WAL_FILE archived successfully"
```

## Security Configuration

### SSL Certificate Setup
```bash
# Generate self-signed certificates for development
openssl req -new -x509 -days 365 -nodes -text -out postgresql.crt -keyout postgresql.key -subj "/CN=postgres"
chmod og-rwx postgresql.key

# For production, use certificates from a trusted CA
# Place certificates in /etc/ssl/postgres/
# - postgresql.crt (server certificate)
# - postgresql.key (private key)
# - ca.crt (certificate authority)
```

### Database User Security
```sql
-- Create application users with limited privileges
CREATE USER promanage_app WITH ENCRYPTED PASSWORD 'secure_app_password_2024';
CREATE USER promanage_readonly WITH ENCRYPTED PASSWORD 'secure_readonly_password_2024';
CREATE USER promanage_backup WITH ENCRYPTED PASSWORD 'secure_backup_password_2024';
CREATE USER promanage_monitor WITH ENCRYPTED PASSWORD 'secure_monitor_password_2024';

-- Grant appropriate permissions
GRANT application_user TO promanage_app;
GRANT readonly_user TO promanage_readonly;

-- Grant backup permissions
GRANT CONNECT ON DATABASE promanage_prod TO promanage_backup;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO promanage_backup;

-- Grant monitoring permissions
GRANT CONNECT ON DATABASE promanage_prod TO promanage_monitor;
GRANT SELECT ON pg_stat_database, pg_stat_activity, pg_locks TO promanage_monitor;

-- Revoke public permissions
REVOKE ALL ON SCHEMA public FROM public;
REVOKE ALL ON ALL TABLES IN SCHEMA public FROM public;
```

## Performance Tuning

### Vacuum and Analyze Schedule
```sql
-- Create custom autovacuum settings for large tables
ALTER TABLE activity_logs SET (
    autovacuum_vacuum_scale_factor = 0.05,
    autovacuum_analyze_scale_factor = 0.02,
    autovacuum_vacuum_cost_delay = 5
);

ALTER TABLE notifications SET (
    autovacuum_vacuum_scale_factor = 0.1,
    autovacuum_analyze_scale_factor = 0.05
);

-- Schedule manual maintenance
SELECT cron.schedule('weekly-vacuum', '0 2 * * 0', 'VACUUM ANALYZE;');
SELECT cron.schedule('monthly-reindex', '0 3 1 * *', 'REINDEX DATABASE promanage_prod;');
SELECT cron.schedule('daily-stats-update', '0 1 * * *', 'SELECT maintain_database();');
```

This production configuration provides a robust, secure, and scalable database setup for the ProManage system, capable of handling 500+ concurrent users with high availability and comprehensive monitoring.