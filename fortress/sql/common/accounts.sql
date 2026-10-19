DROP TABLE IF EXISTS `accounts`;

CREATE TABLE `accounts` (
  `login`		VARCHAR(45)			 NOT NULL DEFAULT '',
  `password`		VARCHAR(45)			 NOT NULL DEFAULT '',
  `lastactive`		BIGINT(13)		UNSIGNED NOT NULL DEFAULT 0,	    -- Максимум 9999999999999
  `access_level`	SMALLINT(5)			 NOT NULL DEFAULT 0,	    -- Минимум -32768, Максимум 32767
  `lastIP`		VARCHAR(15)			 NOT NULL DEFAULT '',	    -- xxx.xxx.xxx.xxx
  `comments`		VARCHAR(45)			 NOT NULL DEFAULT '',	    -- Комментарий для бана и др.
  PRIMARY KEY (`login`)
);