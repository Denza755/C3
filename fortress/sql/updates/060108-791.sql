INSERT INTO `server_variables` SELECT 'killcount', name, id, '0', '-1' FROM `npc`;
UPDATE `server_variables` `s`
LEFT JOIN `killcount` `k` ON (`s`.`index` = `k`.`npc_id`)
SET `value` = (SELECT SUM(`k`.`count`) FROM `killcount` `k` WHERE `s`.`index` = `k`.`npc_id` ) WHERE `type` = 'killcount';
UPDATE `server_variables` SET `value` = '0' WHERE `value` = '';