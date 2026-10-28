-- Transaction-safety schema introduced after the original project schema.
-- MySQL DDL implicitly commits. Back up production data and run migrations in
-- numeric order. Every ALTER below checks information_schema first so it may be
-- re-run after a Hibernate `ddl-auto=update` deployment.

CREATE TABLE IF NOT EXISTS after_sale_requests (
    request_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    buyer_id INT NOT NULL,
    reason VARCHAR(500) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    processed_at DATETIME(6) NULL,
    UNIQUE KEY uq_after_sale_order (order_id),
    KEY idx_after_sale_status_created (status, created_at),
    KEY idx_after_sale_buyer_created (buyer_id, created_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS wallet_transactions (
    transaction_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    order_id INT NULL,
    amount DECIMAL(10,2) NOT NULL,
    type VARCHAR(32) NOT NULL,
    balance_after DECIMAL(10,2) NOT NULL,
    note VARCHAR(255) NULL,
    request_key VARCHAR(80) NULL,
    created_at DATETIME(6) NOT NULL,
    UNIQUE KEY uq_wallet_transactions_request_key (request_key),
    KEY idx_wallet_transactions_user_created (user_id, created_at),
    KEY idx_wallet_transactions_order (order_id)
) ENGINE=InnoDB;

SET @db_name = DATABASE();

-- Version columns are initialized for existing rows so JPA optimistic locking
-- has a deterministic starting point.
SET @column_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @db_name AND lower(table_name) = 'artworks' AND column_name = 'version');
SET @sql = IF(@column_exists = 0, 'ALTER TABLE Artworks ADD COLUMN version BIGINT NOT NULL DEFAULT 0', 'SELECT ''Artworks.version already exists''');
PREPARE migration_statement FROM @sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;

SET @column_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @db_name AND lower(table_name) = 'auctions' AND column_name = 'version');
SET @sql = IF(@column_exists = 0, 'ALTER TABLE Auctions ADD COLUMN version BIGINT NOT NULL DEFAULT 0', 'SELECT ''Auctions.version already exists''');
PREPARE migration_statement FROM @sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;

SET @column_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @db_name AND lower(table_name) = 'orders' AND column_name = 'version');
SET @sql = IF(@column_exists = 0, 'ALTER TABLE Orders ADD COLUMN version BIGINT NOT NULL DEFAULT 0', 'SELECT ''Orders.version already exists''');
PREPARE migration_statement FROM @sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;

SET @column_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @db_name AND lower(table_name) = 'shipping' AND column_name = 'version');
SET @sql = IF(@column_exists = 0, 'ALTER TABLE Shipping ADD COLUMN version BIGINT NOT NULL DEFAULT 0', 'SELECT ''Shipping.version already exists''');
PREPARE migration_statement FROM @sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;

SET @column_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @db_name AND lower(table_name) = 'orders' AND column_name = 'payment_request_key');
SET @sql = IF(@column_exists = 0, 'ALTER TABLE Orders ADD COLUMN payment_request_key VARCHAR(80) NULL', 'SELECT ''Orders.payment_request_key already exists''');
PREPARE migration_statement FROM @sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;

SET @column_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @db_name AND lower(table_name) = 'shipping' AND column_name = 'request_key');
SET @sql = IF(@column_exists = 0, 'ALTER TABLE Shipping ADD COLUMN request_key VARCHAR(80) NULL', 'SELECT ''Shipping.request_key already exists''');
PREPARE migration_statement FROM @sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;

SET @column_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @db_name AND lower(table_name) = 'wallet_transactions' AND column_name = 'request_key');
SET @sql = IF(@column_exists = 0, 'ALTER TABLE wallet_transactions ADD COLUMN request_key VARCHAR(80) NULL', 'SELECT ''wallet_transactions.request_key already exists''');
PREPARE migration_statement FROM @sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;

-- These unique indexes make a retry safe even across application restarts.
SET @index_exists = (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = @db_name AND lower(table_name) = 'orders' AND column_name = 'payment_request_key' AND non_unique = 0);
SET @sql = IF(@index_exists = 0, 'ALTER TABLE Orders ADD CONSTRAINT uq_orders_payment_request_key UNIQUE (payment_request_key)', 'SELECT ''uq_orders_payment_request_key already exists''');
PREPARE migration_statement FROM @sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;

SET @index_exists = (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = @db_name AND lower(table_name) = 'shipping' AND column_name = 'request_key' AND non_unique = 0);
SET @sql = IF(@index_exists = 0, 'ALTER TABLE Shipping ADD CONSTRAINT uq_shipping_request_key UNIQUE (request_key)', 'SELECT ''uq_shipping_request_key already exists''');
PREPARE migration_statement FROM @sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;

SET @index_exists = (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = @db_name AND lower(table_name) = 'after_sale_requests' AND column_name = 'order_id' AND non_unique = 0);
SET @sql = IF(@index_exists = 0, 'ALTER TABLE after_sale_requests ADD CONSTRAINT uq_after_sale_order UNIQUE (order_id)', 'SELECT ''uq_after_sale_order already exists''');
PREPARE migration_statement FROM @sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;

SET @index_exists = (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = @db_name AND lower(table_name) = 'wallet_transactions' AND column_name = 'request_key' AND non_unique = 0);
SET @sql = IF(@index_exists = 0, 'ALTER TABLE wallet_transactions ADD CONSTRAINT uq_wallet_transactions_request_key UNIQUE (request_key)', 'SELECT ''uq_wallet_transactions_request_key already exists''');
PREPARE migration_statement FROM @sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;
