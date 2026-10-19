-- add comments column for accounts
ALTER TABLE `accounts` ADD COLUMN `comments` VARCHAR(45) NOT NULL DEFAULT '';
