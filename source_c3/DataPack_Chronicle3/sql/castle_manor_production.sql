DROP TABLE IF EXISTS `castle_manor_production`;
CREATE TABLE `castle_manor_production` (
  `castle_id` int(11) NOT NULL default '0',
  `can_produce` int(11) NOT NULL default '0',
  `seed_id` int(11) NOT NULL default '0',
  `seed_price` int(11) NOT NULL default '0'
) DEFAULT CHARSET=utf8;

INSERT INTO `castle_manor_production` VALUES (1, 1000, 5022, 20);
INSERT INTO `castle_manor_production` VALUES (1, 1000, 5017, 5);
INSERT INTO `castle_manor_production` VALUES (1, 1000, 5021, 30);
