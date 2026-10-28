-- ArtTrading 数据完整性迁移（MySQL 8.0+）。
-- 先在测试/备份后的数据库执行。DDL 会隐式提交，不能依赖事务回滚。
-- 下方四个查询必须均返回 0 行；若有重复数据，先人工确认保留记录再执行约束部分。

SELECT user_id, artwork_id, COUNT(*) AS duplicate_count
FROM Favorites
GROUP BY user_id, artwork_id
HAVING COUNT(*) > 1;

SELECT auction_id, user_id, COUNT(*) AS duplicate_count
FROM auctionregistrations
GROUP BY auction_id, user_id
HAVING COUNT(*) > 1;

SELECT order_id, COUNT(*) AS duplicate_count
FROM Shipping
GROUP BY order_id
HAVING COUNT(*) > 1;

SELECT order_id, user_id, COUNT(*) AS duplicate_count
FROM Reviews
GROUP BY order_id, user_id
HAVING COUNT(*) > 1;

-- 确认没有重复记录后执行。本段可重复运行：已存在同名索引时会跳过。
SET @db_name = DATABASE();

SET @constraint_exists = (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = @db_name AND table_name = 'Favorites'
      AND index_name = 'uq_favorites_user_artwork'
);
SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE Favorites ADD CONSTRAINT uq_favorites_user_artwork UNIQUE (user_id, artwork_id)',
    'SELECT ''uq_favorites_user_artwork already exists''');
PREPARE migration_statement FROM @sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;

SET @constraint_exists = (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = @db_name AND table_name = 'auctionregistrations'
      AND index_name = 'uq_auction_registrations_auction_user'
);
SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE auctionregistrations ADD CONSTRAINT uq_auction_registrations_auction_user UNIQUE (auction_id, user_id)',
    'SELECT ''uq_auction_registrations_auction_user already exists''');
PREPARE migration_statement FROM @sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;

SET @constraint_exists = (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = @db_name AND table_name = 'Shipping'
      AND index_name = 'uq_shipping_order'
);
SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE Shipping ADD CONSTRAINT uq_shipping_order UNIQUE (order_id)',
    'SELECT ''uq_shipping_order already exists''');
PREPARE migration_statement FROM @sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;

SET @constraint_exists = (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = @db_name AND table_name = 'Reviews'
      AND index_name = 'uq_reviews_order_user'
);
SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE Reviews ADD CONSTRAINT uq_reviews_order_user UNIQUE (order_id, user_id)',
    'SELECT ''uq_reviews_order_user already exists''');
PREPARE migration_statement FROM @sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;
