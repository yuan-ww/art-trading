-- Cleanup for databases where Hibernate created an unnamed unique index before
-- migration 006 added its named equivalent.  Keep the named index unless a
-- different unique index already protects the same column.
SET @db_name = DATABASE();

SET @drop_index = (SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = @db_name AND lower(table_name) = 'orders'
      AND index_name = 'uq_orders_payment_request_key'
      AND (SELECT COUNT(DISTINCT index_name) FROM information_schema.statistics
           WHERE table_schema = @db_name AND lower(table_name) = 'orders'
             AND column_name = 'payment_request_key' AND non_unique = 0) > 1);
SET @sql = IF(@drop_index > 0, 'ALTER TABLE Orders DROP INDEX uq_orders_payment_request_key',
    'SELECT ''orders request-key index needs no cleanup''');
PREPARE migration_statement FROM @sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;

SET @drop_index = (SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = @db_name AND lower(table_name) = 'shipping'
      AND index_name = 'uq_shipping_request_key'
      AND (SELECT COUNT(DISTINCT index_name) FROM information_schema.statistics
           WHERE table_schema = @db_name AND lower(table_name) = 'shipping'
             AND column_name = 'request_key' AND non_unique = 0) > 1);
SET @sql = IF(@drop_index > 0, 'ALTER TABLE Shipping DROP INDEX uq_shipping_request_key',
    'SELECT ''shipping request-key index needs no cleanup''');
PREPARE migration_statement FROM @sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;
