: npc-die+!  ( 'word npc_id# -- )
\ Append word to execute on kill npc with npc_id
	>r
	npc-die-map@ r@ m@		( 'w list R: id )

	?dup null? if
		new-list
	then
	tuck
	list+
	npc-die-map@ r> m!
;
