DROP TABLE IF EXISTS total;
DROP TABLE IF EXISTS prices;
CREATE TABLE prices SELECT item_id, name, price, weight, crystal_type FROM armor;
ALTER TABLE `prices` ADD PRIMARY KEY (`item_id`), ADD KEY (`name`), ADD KEY(`price`), ADD KEY(`crystal_type`);
REPLACE INTO prices SELECT item_id, name, price, weight, crystal_type FROM weapon;
REPLACE INTO prices SELECT item_id, name, price, weight, crystal_type FROM etcitem;

CREATE TABLE total 
	SELECT 
		sum(p.price*i.count) as `sum`, 
		i.item_id, 
		i.owner_id,  
		c.char_name, 
		c.account_name, 
		c.accesslevel 
	FROM items i 
		LEFT JOIN characters c ON (i.owner_id = c.obj_Id)
		LEFT JOIN prices p ON i.item_id = p.item_id
	GROUP BY owner_id ORDER BY `sum` DESC;

ALTER TABLE `total` ADD PRIMARY KEY ( `owner_id` );

DROP TABLE IF EXISTS too_many;
CREATE TABLE too_many 
	SELECT i.item_id, i.count, p.price, i.count*p.price AS `sum`, p.name, i.owner_id,  c.char_name, c.level, c.account_name, c.accesslevel
	FROM items i 
		LEFT JOIN characters c ON (i.owner_id = c.obj_Id)
		LEFT JOIN prices p ON (p.item_id = i.item_id)
	WHERE ((i.count > 100000 AND i.item_id <> 57)
			OR (i.count*p.price > 10000000 and i.item_id <> 57)
			OR (i.count*p.price > 30000000 and i.item_id = 57)
			OR i.count < 0 
			OR i.count*p.price < 0
		)
		AND c.accesslevel < 100 
	ORDER BY `sum` DESC;

DROP TABLE IF EXISTS `tmp_top_drop`;

CREATE TABLE tmp_top_drop 
	SELECT  DISTINCT
		d.chance*p.price*(d.max+d.min)/(sqrt(n.hp)*(n.matk+n.patk+n.mdef+n.pdef)) as rate,
		d.itemId as item_id,
		p.price, 
		p.name AS item_name, 
		n.id AS npc_id, 
		n.name AS npc_name, 
		n.level,
		d.chance/10000 AS chance 
	FROM droplist d LEFT JOIN prices p ON (p.item_id = d.itemId) 
		LEFT JOIN spawnlist s ON (s.npc_templateid = d.mobId) 
		LEFT JOIN npc n ON (n.id = d.mobId)
	WHERE n.id IS NOT NULL 
		AND s.id IS NOT NULL 
		AND p.price IS NOT NULL 
		AND d.chance*p.price*(d.max+d.min)/(sqrt(n.hp)*(n.matk+n.patk+n.mdef+n.pdef)) > 100000
	ORDER BY n.level DESC;
	

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

