-- MySQL 8: indexes for the auction settlement and order expiry scans.
-- DDL implicitly commits.  This version is safe to re-run on a database that
-- already has the named index (for example after Hibernate schema update).
SET @db_name = DATABASE();

SET @index_exists = (SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = @db_name AND lower(table_name) = 'auctions'
      AND index_name = 'idx_auctions_status_end');
SET @sql = IF(@index_exists = 0, 'CREATE INDEX idx_auctions_status_end ON auctions (status, end_time)',
    'SELECT ''idx_auctions_status_end already exists''');
PREPARE migration_statement FROM @sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;

SET @index_exists = (SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = @db_name AND lower(table_name) = 'orders'
      AND index_name = 'idx_orders_payment_created');
SET @sql = IF(@index_exists = 0, 'CREATE INDEX idx_orders_payment_created ON orders (payment_status, created_at)',
    'SELECT ''idx_orders_payment_created already exists''');
PREPARE migration_statement FROM @sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;

SET @index_exists = (SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = @db_name AND lower(table_name) = 'artworks'
      AND index_name = 'idx_artworks_status_sale');
SET @sql = IF(@index_exists = 0, 'CREATE INDEX idx_artworks_status_sale ON artworks (status, sale_type)',
    'SELECT ''idx_artworks_status_sale already exists''');
PREPARE migration_statement FROM @sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;

SET @index_exists = (SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = @db_name AND lower(table_name) = 'bids'
      AND index_name = 'idx_bids_auction_time');
SET @sql = IF(@index_exists = 0, 'CREATE INDEX idx_bids_auction_time ON bids (auction_id, bid_time)',
    'SELECT ''idx_bids_auction_time already exists''');
PREPARE migration_statement FROM @sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;
