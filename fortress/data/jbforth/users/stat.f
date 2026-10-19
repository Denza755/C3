" " value ret

: kills-stat-list ( hashmap -- )
   >r
   "<tr><td>[ r@ 'name' m@ ]</td><td>[ r@ 'count' m@ ]</td><td>[ r@ 'level' m@ ]</td></tr>" parse ret swap s+ to ret
   rdrop
;

: kills-stat-html
	"" to ret
	"SELECT `n`.`name` AS `name`, `k`.`count` AS `count`, `n`.`level` AS `level` FROM `killcount` `k` LEFT JOIN `npc` `n` ON ( `n`.`id` = `k`.`npc_id` ) WHERE `k`.`char_id`="
	player@ "ObjectId" p@ s+
	" ORDER BY `k`.`count` DESC, `n`.`level` DESC;" s+ query drop
	['] kills-stat-list do-list
	ret
;

: drops-stat-list ( hashmap -- )
   >r
   "<tr><td>[ r@ 'name' m@ 'Recipe:' 'R:' strreplace 'Scroll: Enchant Weapon ' 'Enchant Weapon ' strreplace 'Scroll: Enchant Armor ' 'Enchant Armor ' strreplace 'Compressed Package' 'CP' strreplace ]</td><td>[ r@ 'count' m@ ]</td></tr>" parse ret swap s+ to ret
   rdrop
;

: drops-stat-html
	"" to ret
	"SELECT `p`.`name` AS `name`, `d`.`counter` AS `count` FROM `dropcount` `d` LEFT JOIN `prices` `p` ON ( `p`.`item_id` = `d`.`item_id` ) WHERE `d`.`char_id`="
	player@ "ObjectId" p@ s+
	" ORDER BY `d`.`counter` DESC, `p`.`price` * `d`.`counter` DESC;" s+ query drop
	['] drops-stat-list do-list
	ret
;

: craft-stat-list ( hashmap -- )
   >r
   "<tr><td>[ r@ 'name' m@ 'Compressed Package' 'CP' strreplace ]</td><td>[ r@ 'count' m@ ]</td><td>[ r@ 'price' m@ ]</td></tr>" parse ret swap s+ to ret
   rdrop
;

: craft-stat-html
	"" to ret
	"SELECT `p`.`name` AS `name`, `c`.`count` AS `count`, `p`.`price` AS `price` FROM `craftcount` `c` LEFT JOIN `prices` `p` ON ( `p`.`item_id` = `c`.`item_id` ) WHERE `c`.`char_id`="
	player@ "ObjectId" p@ s+
	" ORDER BY `c`.`count` DESC, `p`.`price` * `c`.`count` DESC;" s+ query drop
	['] craft-stat-list do-list
	ret
;

: user_stat
	"jbforth/users/stat.htm" parse show
;

: user_statcraft
 "CRAFT_COUNTER" config@ if
		"jbforth/users/stat-craft.htm" parse show
	else
		"Craft counter disabled by administrator" show
	then
;

: user_statdrop
	"DROP_COUNTER" config@ if
		"jbforth/users/stat-drop.htm" parse show
	else
		"Drop counter disabled by administrator" show
	then
;

: user_statkills
	"KILL_COUNTER" config@ if
		"jbforth/users/stat-kills.htm" parse show
	else
		"Kill counter disabled by administrator" show
	then
;