INSERT INTO `server_variables` SELECT 'craftcount', name, item_id, '0', '-1' FROM `etcitem`;
INSERT INTO `server_variables` SELECT 'craftcount', name, item_id, '0', '-1' FROM `weapon`;
INSERT INTO `server_variables` SELECT 'craftcount', name, item_id, '0', '-1' FROM `armor`;

CREATE TABLE `craftcount` (
  `char_id` int(11) NOT NULL,
  `item_id` int(11) NOT NULL,
  `count` bigint(20) unsigned default '0',
  KEY `char_id` (`char_id`),
  KEY `item_id` (`item_id`)
) ENGINE=MyISAM;