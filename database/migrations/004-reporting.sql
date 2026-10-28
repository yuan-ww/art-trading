CREATE TABLE IF NOT EXISTS reports (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 reporter_id INT NOT NULL,
 artwork_id INT NOT NULL,
 reason VARCHAR(500) NOT NULL,
 status VARCHAR(20) NOT NULL,
 created_at DATETIME(6) NOT NULL,
 resolved_at DATETIME(6),
 INDEX idx_reports_status_created (status, created_at)
) ENGINE=InnoDB;
