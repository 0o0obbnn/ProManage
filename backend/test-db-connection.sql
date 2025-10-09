-- Test database connection and check tables
-- This script is meant to be executed via JDBC connection

-- Check if database exists
SELECT current_database();

-- List all tables in the current database
SELECT tablename FROM pg_tables WHERE schemaname = 'public';
