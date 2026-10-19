DROP TABLE IF EXISTS `boxes`;

CREATE TABLE boxes (
  id INT NOT NULL auto_increment,
  spawn INT DEFAULT NULL,
  npcid INT DEFAULT NULL,
  drawer varchar(32) DEFAULT NULL,
  itemid INT DEFAULT NULL,
  name varchar(32) DEFAULT '',
  `count` INT DEFAULT NULL,
  enchant INT DEFAULT NULL,
  PRIMARY KEY  (id)
);