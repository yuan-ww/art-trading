CREATE TABLE IF NOT EXISTS notifications (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 user_id INT NOT NULL,
 message VARCHAR(500) NOT NULL,
 is_read BOOLEAN NOT NULL DEFAULT FALSE,
 created_at DATETIME(6) NOT NULL,
 INDEX idx_notifications_user_read_created (user_id, is_read, created_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS shipping_events (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 shipping_id INT NOT NULL,
 description VARCHAR(500) NOT NULL,
 occurred_at DATETIME(6) NOT NULL,
 INDEX idx_shipping_events_shipping_time (shipping_id, occurred_at)
) ENGINE=InnoDB;
