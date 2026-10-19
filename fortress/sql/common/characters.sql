DROP TABLE IF EXISTS `characters`;

CREATE TABLE characters (
  `account_name`	VARCHAR(45)			 NOT NULL DEFAULT '',
  `obj_Id`		INT(11)			UNSIGNED NOT NULL DEFAULT 0,
  `char_name`		VARCHAR(35)			 NOT NULL DEFAULT '',
  `level`		TINYINT(2)		UNSIGNED NOT NULL DEFAULT 0,	    -- Максимум = 99
  `maxHp`		MEDIUMINT(8)		UNSIGNED NOT NULL DEFAULT 0,	    -- Максимум = 16777215
  `curHp`		DECIMAL(8,4)		UNSIGNED NOT NULL DEFAULT 0,	    -- Максимум = 99999999.9999
  `maxCp`		MEDIUMINT(8)		UNSIGNED NOT NULL DEFAULT 0,	    -- Максимум = 16777215
  `curCp`		DECIMAL(8,4)		UNSIGNED NOT NULL DEFAULT 0,	    -- Максимум = 99999999.9999
  `maxMp`		MEDIUMINT(8)		UNSIGNED NOT NULL DEFAULT 0,	    -- Максимум = 16777215
  `curMp`		DECIMAL(8,4)		UNSIGNED NOT NULL DEFAULT 0,	    -- Максимум = 99999999.9999
  acc INT default NULL,
  crit INT default NULL,
  evasion INT default NULL,
  mAtk INT default NULL,
  mDef INT default NULL,
  mSpd INT default NULL,
  pAtk INT default NULL,
  pDef INT default NULL,
  pSpd INT default NULL,
  runSpd INT default NULL,
  walkSpd INT default NULL,
  str INT default NULL,
  con INT default NULL,
  dex INT default NULL,
  _int INT default NULL,
  men INT default NULL,
  wit INT default NULL,
  face INT default NULL,
  hairStyle INT default NULL,
  hairColor INT default NULL,
  sex INT default NULL,
  heading INT default NULL,
  x INT default NULL,
  y INT default NULL,
  z INT default NULL,
  movement_multiplier decimal(11,4) default NULL,
  attack_speed_multiplier decimal(11,4) default NULL,
  colRad decimal(11,4) default NULL,
  colHeight decimal(11,4) default NULL,
  exp INT default NULL,
  sp INT default NULL,
  karma INT default NULL,
  pvpkills INT default NULL,
  pkkills INT default NULL,
  clanid INT default NULL,
  maxload INT default NULL,
  race INT default NULL,
  classid INT default NULL,
  `deletetime`		BIGINT(13)		UNSIGNED NOT NULL DEFAULT 0,	    -- Максимум 9999999999999
  cancraft INT default NULL,
  title varchar(15) default NULL,
  allyId INT default NULL,
  rec_have int NOT NULL default '0',
  rec_left int NOT NULL default '0',
  accesslevel INT default NULL,
  online INT DEFAULT NULL,
  char_slot INT default NULL,
  newbie INT default '1',
  `lastAccess`		BIGINT(13)		UNSIGNED NOT NULL DEFAULT 0,	    -- Максимум 9999999999999
  clan_privs INT DEFAULT 0,
  wantspeace INT DEFAULT 0,
  `deleteclan`		BIGINT(13)		UNSIGNED NOT NULL DEFAULT 0,	    -- Максимум 9999999999999
  `nochannel` INT NOT NULL DEFAULT 0,
  PRIMARY KEY  (obj_Id),
  UNIQUE KEY `char_name` (`char_name`),
  KEY `clanid` (`clanid`)
);