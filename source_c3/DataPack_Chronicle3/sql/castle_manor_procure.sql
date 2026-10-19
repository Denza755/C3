DROP TABLE IF EXISTS `castle_manor_procure`;
CREATE TABLE `castle_manor_procure` (
  `castle_id` int(11) NOT NULL default '0',
  `crop_id` int(11) NOT NULL default '0',
  `can_buy` int(11) NOT NULL default '0',
  `reward_type` int(11) NOT NULL default '0',
  PRIMARY KEY  (`castle_id`,`crop_id`,`reward_type`)
) DEFAULT CHARSET=utf8;


INSERT INTO `castle_manor_procure` VALUES (1, 5122, 1000, 2);
INSERT INTO `castle_manor_procure` VALUES (1, 5108, 1000, 2);
INSERT INTO `castle_manor_procure` VALUES (1, 5124, 1000, 2);
