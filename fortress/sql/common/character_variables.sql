DROP TABLE IF EXISTS `character_variables`;

CREATE TABLE IF NOT EXISTS `character_variables` (
  `obj_id` INT NOT NULL DEFAULT 0,
  `type` VARCHAR(86) NOT NULL DEFAULT 0,
  `name` VARCHAR(86) NOT NULL DEFAULT 0,
  `index` INT NOT NULL DEFAULT 0,
  `value` VARCHAR(255) NOT NULL DEFAULT 0,
  `expire_time` INT NOT NULL DEFAULT 0,
  UNIQUE KEY `prim` (`obj_id`, `type`, `name`, `index`),
  KEY (`obj_id`),
  KEY (`type`),
  KEY (`name`),
  KEY (`index`),
  KEY (`value`),
  KEY (`expire_time`)
);