: lspawn  ( {npc_id x y z h respawn save-db} -- obj )
	list-rev> 7 <> if
		"Error: to low elements in spawn point" .
		exit
	then
	>r >r 
	4 pick
	r> r> spawn
	nip
;

: lspawn-list+   lspawn over list+ ;

: npc-list-spawn  ( {list-of-spawn-data-lists} -- {spawned-npc-objects-list})
	new-list swap  ['] lspawn-list+ do-list
;

: npc-list-unspawn
	list> 0 ?do
		true unspawn
	loop
;

: item-spawn-from-list  ( { item_id count x y z } -- obj )
	list-rev> 5 <> if
		"Error: to low elements in item spawn list" .
		exit
	then
	>r >r >r
	null
	r> r> r> item_spawn
;

: item-spawn-list+  item-spawn-from-list over list+ ;

: items-list-spawn  ( {list-of-item-spawn} -- {spawned-items-objects-list})
	new-list swap ['] item-spawn-list+ do-list
;

: items-list-unspawn
	list> 0 ?do
		item_unspawn
	loop
;
