DROP TABLE IF EXISTS total;
DROP TABLE IF EXISTS prices;
CREATE TABLE prices SELECT item_id, name, price, weight, crystal_type, item_type, consume_type, icon  FROM etcitem;
ALTER TABLE `prices` ADD PRIMARY KEY (`item_id`), ADD KEY (`name`), ADD KEY(`price`), ADD KEY(`crystal_type`), ADD KEY(`item_type`);
INSERT INTO prices SELECT item_id, name, price, weight, crystal_type, 'weapon', 'normal', icon FROM weapon;
INSERT INTO prices SELECT item_id, name, price, weight, crystal_type, 'armor', 'normal', icon FROM armor;

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
