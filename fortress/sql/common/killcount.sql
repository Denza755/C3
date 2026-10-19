DROP TABLE IF EXISTS `killcount`;

CREATE TABLE `killcount` (
  `char_id` int(11) NOT NULL,
  `npc_id` int(11) NOT NULL,
  `count` int(5) UNSIGNED NOT NULL,
  KEY `char_id` (`char_id`),
  KEY `npc_id` (`npc_id`)
) ENGINE=MyISAM;