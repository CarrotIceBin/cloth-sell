CREATE TABLE IF NOT EXISTS mall_refresh_token (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  user_type varchar(16) NOT NULL,
  user_id bigint(20) NOT NULL,
  account varchar(64) NOT NULL,
  token_hash char(64) NOT NULL,
  expire_time datetime NOT NULL,
  creator varchar(64) DEFAULT NULL,
  create_time datetime DEFAULT NULL,
  updater varchar(64) DEFAULT NULL,
  update_time datetime DEFAULT NULL,
  deleted tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (id),
  UNIQUE KEY uk_refresh_hash (token_hash),
  KEY idx_refresh_user (user_type, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
