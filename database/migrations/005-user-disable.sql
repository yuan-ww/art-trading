-- Hibernate may already have added this column.  MySQL DDL implicitly commits.
SET @db_name = DATABASE();
SET @column_exists = (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = @db_name AND lower(table_name) = 'users' AND column_name = 'disabled');
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE users ADD COLUMN disabled BOOLEAN NOT NULL DEFAULT FALSE',
    'SELECT ''users.disabled already exists''');
PREPARE migration_statement FROM @sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;
