-- add nochannel column - timing for banchat
ALTER TABLE `characters` ADD COLUMN `nochannel` INT NOT NULL DEFAULT 0;

-- fix price for Recipe: Heavy Sword(100%)
UPDATE `prices` set prices.price=85000 where item_id=5437;
UPDATE `etcitem` set etcitem.price=85000 where item_id=5437;
