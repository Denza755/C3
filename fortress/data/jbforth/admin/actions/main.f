: c.target!  ( target char -- )
	"setTarget" { L2Object.class } jexec
;

: obj-p@  ( "Property" -- value )
	object swap p@
	?dup unless "none" then
;

: npc.spawn.location@  ( L2NpcInstance -- location )
	"Spawn" p@ ?dup if
		"Location" p@
	else
		0
	then
;

: npc>spawn_id  ( L2NpcInstance -- spawn_id )
	"Spawn" p@ ?dup if
		"Id" p@
	else
		0
	then
;

: npc.aggro@  ( L2NpcInstance -- location )
	dup L2Attackable.class instance-of? if
		"AggroRange" p@
	else
		0
	then
;

: npc.id@   ( L2NpcInstance -- npc_id )
	"Template" p@
	"npcId" field@
;

: L2NpcInstance.onActionShift
	object player@ c.target!

	"npc-data-view" access?
	if
		"jbforth/admin/actions/L2NpcInstance.onActionShift.htm" parse show
	else
		"ALT_GAME_SHOW_DROPLIST" config@ if
			"jbforth/admin/actions/player.L2NpcInstance.onActionShift.htm" parse show
		else
			"Showing droplist to players forbidden by administrator" show
		then
	then
	action-failed
	true boolean
;

: L2MonsterInstance.onActionShift		L2NpcInstance.onActionShift ;
: L2MinionInstance.onActionShift		L2NpcInstance.onActionShift ;
: L2TeleporterInstance.onActionShift	L2NpcInstance.onActionShift ;
: L2RaidBossInstance.onActionShift		L2NpcInstance.onActionShift ;

: L2DoorInstance.onActionShift
    "npc-data-view" check-access
    object player@ c.target!
	"jbforth/admin/actions/L2DoorInstance.onActionShift.htm" parse show
	action-failed
	true boolean
;

: L2PcInstance.onActionShift
	"players-data-view" access? not
	if
		false boolean exit
	then
    object player@ c.target!
	"jbforth/admin/players/character_list.htm" show
	action-failed
	true boolean
;

" " value accum

: npc-drop-list ( hashmap -- )
   >r
   dup r@ 'Iid' m@ 57 - if
      dup r@ 'sweep' m@ if
         "<tr><td>[ r@ 'id' m@ 'Recipe:' 'R:' strreplace 'Scroll: Enchant Weapon ' 'Enchant Weapon ' strreplace 'Scroll: Enchant Armor ' 'Enchant Armor ' strreplace 'Compressed Package' 'CP' strreplace ]</td><td>[ r@ 'qty' m@ ]</td><td>+</td><td>[ r@ 'chance' m@ 'RATE_DROP_SPOIL' config@ f* int 1000000 min 10000 f/ sprintchance ]%</td></tr>" parse accum swap s+ to accum
      else
         "<tr><td>[ r@ 'id' m@ 'Recipe:' 'R:' strreplace 'Scroll: Enchant Weapon ' 'Enchant Weapon ' strreplace 'Scroll: Enchant Armor ' 'Enchant Armor ' strreplace 'Compressed Package' 'CP' strreplace ]</td><td>[ r@ 'qty' m@ ]</td><td> </td><td>[ r@ 'chance' m@ 'RATE_DROP_ITEMS' config@ f* int 1000000 min 10000 f/ sprintchance ]%</td></tr>" parse accum swap s+ to accum
      then
   else
   "<tr><td>Adena</td><td>[ r@ 'qty' m@ 'RATE_DROP_ADENA' config@ f* int 999999 min ]</td><td> </td><td>70%</td></tr>" parse accum swap s+ to accum
   then
   rdrop
;

: npc-drop-html
	"" to accum
	"SELECT `d`.`itemId` AS `Iid`, `p`.`name` AS `id`, FLOOR( ( `d`.`min` + `d`.`max` ) / 2 ) AS `qty`, `d`.`sweep` AS `sweep`, `d`.`chance` AS `chance` FROM `droplist` `d` LEFT JOIN `prices` `p` ON ( `p`.`item_id` = `d`.`itemId` ) WHERE `d`.`mobId`="
	object npc.id@ s+
	" ORDER BY `d`.`chance` DESC;" s+ query drop
	['] npc-drop-list do-list
	accum
;

: show-user-droplist
	'jbforth/admin/actions/L2NpcInstance.showNpcDropList.htm' parse show
;