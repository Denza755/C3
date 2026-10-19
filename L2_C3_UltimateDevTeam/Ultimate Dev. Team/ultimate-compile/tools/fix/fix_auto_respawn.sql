DROP TABLE IF EXISTS `tmp_sum_drop`;

CREATE TABLE tmp_sum_drop
	SELECT d.mobId as mob_id,
		sum(d.chance*p.price*(d.max+d.min))/1000.0 as total,
		n.name,
		n.level,
		n.hp
	FROM droplist d
		LEFT JOIN prices p ON (d.itemId = p.item_id)
		LEFT JOIN npc n ON (d.mobId = n.id)
	WHERE n.id IS NOT NULL
		AND p.price <> 0
		AND d.itemId IS NOT NULL
		AND p.item_id IS NOT NULL
	GROUP BY d.mobId
;

ALTER TABLE tmp_sum_drop ADD KEY(mob_id), ADD KEY(total);

DROP TABLE IF EXISTS `tmp_top_spawn_drop`;
CREATE TABLE tmp_top_spawn_drop
	SELECT DISTINCT
		n.total/(s.respawn_delay*sqrt(n.hp)) as rate,
		n.total,
		s.respawn_delay,
		n.mob_id, 
		n.name as mob_name, 
		n.level,
		n.hp
	FROM spawnlist s
		LEFT JOIN tmp_sum_drop n ON (n.mob_id = s.npc_templateid)
	WHERE n.total IS NOT NULL
		AND s.id IS NOT NULL
;

ALTER TABLE tmp_top_spawn_drop ADD KEY(rate), ADD KEY(mob_id);

UPDATE spawnlist s 
	LEFT JOIN tmp_top_spawn_drop t ON (s.npc_templateid = t.mob_id)
SET s.respawn_delay = s.respawn_delay*t.rate/1000.0
WHERE t.rate > 1000.0;

UPDATE npc n LEFT JOIN spawnlist s ON n.id = s.npc_templateid SET n.hp=n.hp*s.respawn_delay/86400 WHERE s.respawn_delay > 86400;
UPDATE spawnlist SET respawn_delay = 86400 WHERE respawn_delay > 86400;

DROP TABLE IF EXISTS `tmp_sum_drop`;
DROP TABLE IF EXISTS `tmp_top_spawn_drop`;
