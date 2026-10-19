DROP TABLE IF EXISTS `tmp_top_drop`;

UPDATE prices SET price=90000 WHERE price=0 AND crystal_type='none';
UPDATE prices SET price=600000 WHERE price=0 AND crystal_type='d';
UPDATE prices SET price=2800000 WHERE price=0 AND crystal_type='c';
UPDATE prices SET price=4200000 WHERE price=0 AND crystal_type='b';
UPDATE prices SET price=9400000 WHERE price=0 AND crystal_type='a';

CREATE TABLE tmp_top_drop 
	SELECT  DISTINCT
		d.chance*p.price*(d.max+d.min)/(sqrt(n.hp)*(n.matk+n.patk+n.mdef+n.pdef)) as rate,
		d.itemId as item_id,
		p.price, 
		p.name AS item_name, 
		n.id AS npc_id, 
		n.name AS npc_name, 
		n.level,
		d.chance/10000 AS chance,
		p.crystal_type
	FROM droplist d LEFT JOIN prices p ON (p.item_id = d.itemId) 
		LEFT JOIN spawnlist s ON (s.npc_templateid = d.mobId) 
		LEFT JOIN npc n ON (n.id = d.mobId)
	WHERE n.id IS NOT NULL 
		AND s.id IS NOT NULL 
		AND p.price IS NOT NULL 
		AND d.chance*p.price*(d.max+d.min)/(sqrt(n.hp)*(n.matk+n.patk+n.mdef+n.pdef)) > 100000
	ORDER BY n.level DESC;

#UPDATE tmp_top_drop SET rate=rate*8 WHERE crystal_type='b';
#UPDATE tmp_top_drop SET rate=rate*16 WHERE crystal_type='a';

UPDATE droplist d 
	LEFT JOIN tmp_top_drop t ON (d.mobId = t.npc_id) 
SET d.max = d.max*100000/rate + 1,
	d.min = d.min*100000/rate + 1
WHERE d.mobId IS NOT NULL 
	AND t.npc_id IS NOT NULL
	AND d.itemId = 57;

UPDATE droplist d 
	LEFT JOIN tmp_top_drop t ON (d.mobId = t.npc_id) 
SET d.chance = d.chance*100000/rate
WHERE d.mobId IS NOT NULL 
	AND t.npc_id IS NOT NULL
	AND d.itemId <> 57;

DELETE FROM d USING droplist d LEFT JOIN prices p ON (d.itemId = p.item_id)	WHERE p.price = 0 AND p.crystal_type <> 'none';

-- DROP TABLE IF EXISTS `tmp_top_drop`;

