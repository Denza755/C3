DELETE FROM clan_data WHERE leader_id NOT IN ( SELECT obj_Id FROM characters );
UPDATE characters SET clanid='0' WHERE clanid NOT IN ( SELECT clan_id FROM clan_data );
