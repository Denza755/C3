DROP TABLE IF EXISTS `clan_wars_new` ;
CREATE TABLE IF NOT EXISTS `clan_wars_new` (
  `clan1` INT NOT NULL default 0,
  `clan2` INT NOT NULL default 0,
  `wantspeace1` decimal(2,0) NOT NULL default '0',
  `wantspeace2` decimal(2,0) NOT NULL default '0',
  `type` INT NOT NULL default '1'
) TYPE=MyISAM;


INSERT INTO `clan_wars_new` (`clan1`,`clan2`,`wantspeace1`,`wantspeace2`,`type`) 
	SELECT (select `clan_id` from `clan_data` where `clan_data`.`clan_name`=`clan_wars`.`clan1`),
	(select `clan_id` from `clan_data` where `clan_data`.`clan_name`=`clan_wars`.`clan2`),
	`clan_wars`.`wantspeace1`,`clan_wars`.`wantspeace2`,`clan_wars`.`type`
       FROM `clan_wars` ;
DROP TABLE `clan_wars`;
RENAME TABLE `clan_wars_new` TO `clan_wars`;
#ALTER TABLE `clan_wars` MODIFY COLUMN `clan1` INT NOT NULL DEFAULT 0;
#ALTER TABLE `clan_wars` MODIFY COLUMN `clan1` INT NOT NULL DEFAULT 0;
