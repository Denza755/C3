DROP TABLE IF EXISTS `dropcount`;

CREATE TABLE `dropcount` (
  `char_id` int(11) NOT NULL,
  `item_id` int(11) NOT NULL,
  `counter` bigint(20) unsigned default '0',
  KEY `char_id` (`char_id`),
  KEY `item_id` (`item_id`)
) ENGINE=MyISAM;


