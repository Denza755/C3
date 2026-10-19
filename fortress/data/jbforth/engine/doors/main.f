new-hashmap value doors-pairs-map

: .doormen-dialog  ( -- str )
	'<br1><a action="bypass -h jbf_doors_open">Open doors</a><br1>
	<a action="bypass -h jbf_doors_close">Close doors</a><br1>
'
;

: doormen-dialog+  ( {doorman-npc_id door_id} -- )
	list> drop
	doors-pairs-map over m@ 		\ ( door_id doorman_id doors-list )
	?dup null? if
		['] .doormen-dialog over
		npc-dialog+
		new-list
	then
	rot over list+
	doors-pairs-map rot m!
;

: bypass_doors_open  ( -- )
	player@ target@ 
	?dup null? if exit then
	"NpcId" p@ ?dup if
		doors-pairs-map swap m@ ?dup null? not if
			['] door-open do-list
		then
	then
;

: bypass_doors_close  ( -- )
	player@ target@ 
	?dup null? if exit then
	"NpcId" p@ ?dup if
		doors-pairs-map swap m@ ?dup null? not if
			['] door-close do-list
		then
	then
;

{ engine/doors/pairs } ['] doormen-dialog+ do-list

coliseum
garden-of-eva
