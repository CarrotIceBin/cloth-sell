CREATE TABLE IF NOT EXISTS mall_login_log (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  user_type varchar(16) NOT NULL,
  user_id bigint(20) DEFAULT NULL,
  account varchar(64) NOT NULL,
  success tinyint(4) NOT NULL,
  user_ip varchar(64) DEFAULT NULL,
  user_agent varchar(255) DEFAULT NULL,
  creator varchar(64) DEFAULT NULL,
  create_time datetime DEFAULT NULL,
  updater varchar(64) DEFAULT NULL,
  update_time datetime DEFAULT NULL,
  deleted tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (id),
  KEY idx_login_account (user_type, account),
  KEY idx_login_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
