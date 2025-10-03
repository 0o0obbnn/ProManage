# ProManage Database Setup Guide

## Prerequisites

### System Requirements
- PostgreSQL 15+ (recommended: PostgreSQL 15.5 or later)
- Minimum 8GB RAM (16GB recommended for production)
- SSD storage for optimal performance
- Docker and Docker Compose (for containerized setup)

### Required PostgreSQL Extensions
```sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";     -- UUID generation
CREATE EXTENSION IF NOT EXISTS "pg_trgm";       -- Trigram similarity
CREATE EXTENSION IF NOT EXISTS "unaccent";      -- Remove accents for search
CREATE EXTENSION IF NOT EXISTS "btree_gin";     -- Composite indexes
```

## Installation Options

### Option 1: Docker Setup (Recommended for Development)

#### 1. Create Docker Compose File
```yaml
# docker-compose.yml
version: '3.8'

services:
  postgres:
    image: postgres:15
    container_name: promanage-postgres
    environment:
      POSTGRES_DB: promanage_dev
      POSTGRES_USER: promanage_user
      POSTGRES_PASSWORD: promanage_password_2024
      POSTGRES_INITDB_ARGS: "--encoding=UTF8 --locale=en_US.UTF-8"
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init-scripts:/docker-entrypoint-initdb.d
      - ./postgresql.conf:/etc/postgresql/postgresql.conf
    command: postgres -c config_file=/etc/postgresql/postgresql.conf
    restart: unless-stopped
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U promanage_user -d promanage_dev"]
      interval: 30s
      timeout: 10s
      retries: 3

  redis:
    image: redis:7-alpine
    container_name: promanage-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    command: redis-server --appendonly yes --requirepass redis_password_2024
    restart: unless-stopped

  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.9.0
    container_name: promanage-elasticsearch
    environment:
      - discovery.type=single-node
      - "ES_JAVA_OPTS=-Xms1g -Xmx1g"
      - xpack.security.enabled=false
      - xpack.security.enrollment.enabled=false
    ports:
      - "9200:9200"
      - "9300:9300"
    volumes:
      - elasticsearch_data:/usr/share/elasticsearch/data
    restart: unless-stopped

volumes:
  postgres_data:
  redis_data:
  elasticsearch_data:
```

#### 2. PostgreSQL Configuration File
Create `postgresql.conf`:
```ini
# PostgreSQL Configuration for ProManage Development

# Memory Configuration
shared_buffers = 256MB
effective_cache_size = 1GB
work_mem = 4MB
maintenance_work_mem = 64MB

# WAL Configuration
wal_buffers = 16MB
checkpoint_completion_target = 0.9
checkpoint_timeout = 10min
max_wal_size = 1GB
min_wal_size = 80MB

# Connection Configuration
max_connections = 100
shared_preload_libraries = 'pg_stat_statements'

# Logging Configuration
logging_collector = on
log_directory = 'log'
log_filename = 'postgresql-%Y-%m-%d_%H%M%S.log'
log_statement = 'all'
log_min_duration_statement = 1000
log_checkpoints = on
log_connections = on
log_disconnections = on

# Query Planner Configuration
random_page_cost = 1.1
effective_io_concurrency = 200

# Locale and Formatting
lc_messages = 'en_US.UTF-8'
lc_monetary = 'en_US.UTF-8'
lc_numeric = 'en_US.UTF-8'
lc_time = 'en_US.UTF-8'
default_text_search_config = 'pg_catalog.english'
```

#### 3. Initialization Script
Create `init-scripts/01-init.sql`:
```sql
-- Create application user
CREATE USER promanage_app_user WITH ENCRYPTED PASSWORD 'app_user_password_2024';
CREATE USER promanage_readonly_user WITH ENCRYPTED PASSWORD 'readonly_password_2024';

-- Grant database access
GRANT CONNECT ON DATABASE promanage_dev TO promanage_app_user;
GRANT CONNECT ON DATABASE promanage_dev TO promanage_readonly_user;

-- Create schema and grant permissions
GRANT USAGE ON SCHEMA public TO promanage_app_user;
GRANT USAGE ON SCHEMA public TO promanage_readonly_user;

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
CREATE EXTENSION IF NOT EXISTS "unaccent";
CREATE EXTENSION IF NOT EXISTS "btree_gin";
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";
```

#### 4. Start the Environment
```bash
# Clone the repository and navigate to project directory
cd ProManage

# Start the services
docker-compose up -d

# Verify services are running
docker-compose ps

# Check PostgreSQL logs
docker-compose logs postgres

# Connect to database
docker-compose exec postgres psql -U promanage_user -d promanage_dev
```

### Option 2: Native Installation

#### Ubuntu/Debian Installation
```bash
# Update package list
sudo apt update

# Install PostgreSQL
sudo apt install postgresql-15 postgresql-contrib-15

# Start and enable PostgreSQL
sudo systemctl start postgresql
sudo systemctl enable postgresql

# Create database and user
sudo -u postgres createuser --interactive promanage_user
sudo -u postgres createdb promanage_dev
sudo -u postgres psql -c "ALTER USER promanage_user WITH PASSWORD 'your_secure_password';"
```

#### macOS Installation (Homebrew)
```bash
# Install PostgreSQL
brew install postgresql@15

# Start PostgreSQL service
brew services start postgresql@15

# Create database and user
createuser -s promanage_user
createdb promanage_dev
psql -d promanage_dev -c "ALTER USER promanage_user WITH PASSWORD 'your_secure_password';"
```

#### Windows Installation
1. Download PostgreSQL installer from https://www.postgresql.org/download/windows/
2. Run installer and follow setup wizard
3. Use pgAdmin or command line to create database and user

## Database Schema Setup

### 1. Apply Schema
```bash
# Using psql command line
psql -h localhost -U promanage_user -d promanage_dev -f database_schema.sql

# Using Docker
docker-compose exec postgres psql -U promanage_user -d promanage_dev -f /docker-entrypoint-initdb.d/database_schema.sql
```

### 2. Verify Installation
```sql
-- Connect to database
\c promanage_dev

-- Check tables created
\dt

-- Check indexes
\di

-- Check views
\dv

-- Verify sample data
SELECT COUNT(*) FROM organizations;
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM roles;
SELECT COUNT(*) FROM permissions;
```

### 3. Create Additional Users
```sql
-- Create application connection user
CREATE USER promanage_app WITH ENCRYPTED PASSWORD 'app_secure_password_2024';
GRANT application_user TO promanage_app;

-- Create read-only user for reporting
CREATE USER promanage_reports WITH ENCRYPTED PASSWORD 'reports_password_2024';
GRANT readonly_user TO promanage_reports;

-- Create backup user
CREATE USER promanage_backup WITH ENCRYPTED PASSWORD 'backup_password_2024';
GRANT CONNECT ON DATABASE promanage_dev TO promanage_backup;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO promanage_backup;
```

## Application Configuration

### Connection Strings

#### Spring Boot Configuration
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/promanage_dev
    username: promanage_app
    password: app_secure_password_2024
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 20000
      validation-timeout: 5000

  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        jdbc:
          time_zone: UTC
        default_schema: public
```

#### Node.js Configuration
```javascript
// database.js
const { Pool } = require('pg');

const pool = new Pool({
  host: 'localhost',
  port: 5432,
  database: 'promanage_dev',
  user: 'promanage_app',
  password: 'app_secure_password_2024',
  max: 20,
  idleTimeoutMillis: 30000,
  connectionTimeoutMillis: 2000,
});

module.exports = pool;
```

### Environment Variables
```bash
# .env file
DATABASE_URL=postgresql://promanage_app:app_secure_password_2024@localhost:5432/promanage_dev
DATABASE_HOST=localhost
DATABASE_PORT=5432
DATABASE_NAME=promanage_dev
DATABASE_USER=promanage_app
DATABASE_PASSWORD=app_secure_password_2024
DATABASE_POOL_SIZE=20
DATABASE_POOL_MIN=5
```

## Development Workflow

### Schema Migrations

#### 1. Migration Script Template
```sql
-- migrations/V001__initial_schema.sql
-- ProManage Migration V001
-- Description: Initial schema creation
-- Date: 2024-12-30

BEGIN;

-- Your migration SQL here
-- (Copy from database_schema.sql)

-- Update schema version
INSERT INTO schema_migrations (version, description, applied_at)
VALUES ('001', 'Initial schema creation', CURRENT_TIMESTAMP);

COMMIT;
```

#### 2. Create Migration Table
```sql
CREATE TABLE IF NOT EXISTS schema_migrations (
    version VARCHAR(50) PRIMARY KEY,
    description TEXT NOT NULL,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    applied_by VARCHAR(100) DEFAULT CURRENT_USER
);
```

#### 3. Apply Migration
```bash
# Using Flyway (recommended)
flyway -url=jdbc:postgresql://localhost:5432/promanage_dev -user=promanage_app -password=app_secure_password_2024 migrate

# Using custom script
psql -h localhost -U promanage_app -d promanage_dev -f migrations/V001__initial_schema.sql
```

### Data Seeding

#### 1. Development Seed Data
```sql
-- seeds/development.sql
SET session_replication_role = replica; -- Disable triggers for seeding

-- Insert sample organizations
INSERT INTO organizations (name, slug, description) VALUES
('Acme Corporation', 'acme-corp', 'Sample organization for development'),
('Tech Startup Inc', 'tech-startup', 'Another sample organization');

-- Insert sample users
INSERT INTO users (organization_id, username, email, password_hash, first_name, last_name, status, email_verified_at)
SELECT
    o.id,
    'john.doe',
    'john.doe@acme.com',
    '$2a$10$dummy.hash.for.development.purposes.only',
    'John',
    'Doe',
    'ACTIVE',
    CURRENT_TIMESTAMP
FROM organizations o WHERE o.slug = 'acme-corp';

-- Insert sample projects
INSERT INTO projects (organization_id, name, slug, description, project_manager_id)
SELECT
    o.id,
    'Sample Project',
    'sample-project',
    'A sample project for development and testing',
    u.id
FROM organizations o, users u
WHERE o.slug = 'acme-corp' AND u.username = 'john.doe' AND u.organization_id = o.id;

SET session_replication_role = DEFAULT; -- Re-enable triggers
```

#### 2. Apply Seed Data
```bash
psql -h localhost -U promanage_app -d promanage_dev -f seeds/development.sql
```

### Testing Setup

#### 1. Test Database Setup
```bash
# Create test database
createdb promanage_test -O promanage_user

# Apply schema to test database
psql -h localhost -U promanage_user -d promanage_test -f database_schema.sql

# Apply test data
psql -h localhost -U promanage_user -d promanage_test -f seeds/test.sql
```

#### 2. Test Configuration
```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/promanage_test
    username: promanage_user
    password: promanage_password_2024
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
```

## Performance Tuning

### 1. Monitor Performance
```sql
-- Enable query statistics
CREATE EXTENSION IF NOT EXISTS pg_stat_statements;

-- Check slow queries
SELECT
    query,
    calls,
    total_time,
    mean_time,
    stddev_time,
    rows
FROM pg_stat_statements
ORDER BY mean_time DESC
LIMIT 10;
```

### 2. Index Optimization
```sql
-- Check unused indexes
SELECT
    schemaname,
    tablename,
    indexname,
    idx_scan,
    idx_tup_read,
    idx_tup_fetch
FROM pg_stat_user_indexes
WHERE idx_scan = 0
ORDER BY pg_relation_size(indexrelid) DESC;

-- Check missing indexes (requires pg_stat_statements)
SELECT
    schemaname,
    tablename,
    attname,
    n_distinct,
    correlation
FROM pg_stats
WHERE schemaname = 'public'
AND n_distinct > 100
ORDER BY n_distinct DESC;
```

### 3. Connection Pooling (PgBouncer)
```ini
# pgbouncer.ini
[databases]
promanage_dev = host=localhost port=5432 dbname=promanage_dev

[pgbouncer]
listen_port = 6432
listen_addr = 127.0.0.1
auth_type = md5
auth_file = /etc/pgbouncer/userlist.txt
pool_mode = transaction
server_reset_query = DISCARD ALL
max_client_conn = 1000
default_pool_size = 25
min_pool_size = 5
reserve_pool_size = 5
```

## Backup and Recovery

### 1. Backup Scripts
```bash
#!/bin/bash
# backup.sh

DB_NAME="promanage_dev"
DB_USER="promanage_backup"
BACKUP_DIR="/var/backups/postgresql"
DATE=$(date +%Y%m%d_%H%M%S)

# Create backup directory
mkdir -p $BACKUP_DIR

# Full database backup
pg_dump -h localhost -U $DB_USER -Fc $DB_NAME > $BACKUP_DIR/promanage_backup_$DATE.dump

# Compressed SQL backup
pg_dump -h localhost -U $DB_USER $DB_NAME | gzip > $BACKUP_DIR/promanage_backup_$DATE.sql.gz

# Schema-only backup
pg_dump -h localhost -U $DB_USER -s $DB_NAME > $BACKUP_DIR/promanage_schema_$DATE.sql

# Clean old backups (keep 30 days)
find $BACKUP_DIR -name "promanage_backup_*.dump" -mtime +30 -delete
```

### 2. Restore Procedures
```bash
# Restore from dump file
pg_restore -h localhost -U promanage_user -d promanage_dev -c -v promanage_backup_20241230.dump

# Restore from SQL file
gunzip -c promanage_backup_20241230.sql.gz | psql -h localhost -U promanage_user -d promanage_dev
```

## Monitoring and Maintenance

### 1. Health Check Script
```bash
#!/bin/bash
# health_check.sh

# Check PostgreSQL is running
pg_isready -h localhost -p 5432

# Check database connectivity
psql -h localhost -U promanage_app -d promanage_dev -c "SELECT 1;" > /dev/null

# Check table sizes
psql -h localhost -U promanage_app -d promanage_dev -c "
SELECT
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
"
```

### 2. Maintenance Tasks
```sql
-- Weekly maintenance
SELECT maintain_database();

-- Manual vacuum analyze
VACUUM ANALYZE;

-- Reindex if needed
REINDEX DATABASE promanage_dev;

-- Update statistics
ANALYZE;
```

This setup guide provides everything needed to get the ProManage database running in development, testing, and production environments with proper configuration, monitoring, and maintenance procedures.