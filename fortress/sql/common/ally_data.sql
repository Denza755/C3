DROP TABLE IF EXISTS `ally_data`;

CREATE TABLE IF NOT EXISTS ally_data (
  ally_id INT NOT NULL default 0,
  ally_name varchar(45) ,
  leader_id INT,
  ally_crestid INT,
  PRIMARY KEY  (ally_id),
  KEY `leader_id` (`leader_id`)
);
