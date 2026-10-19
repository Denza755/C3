: admin_rename
	"rename" check-access
	?dup "" = if
		"jbforth/admin/players/rename.htm" show
		exit
	then
	target-or-me@ player_rename
;

: smart-find  ( name/npc_id/empty -- object_id | null )
	depth 0= if target-or-me@ exit then
	?dup "" = if target-or-me@ exit then

	dup find_player ?dup if nip exit then
	dup find-by-npc_id ?dup if nip exit then
	"Not found player or NPC [  ]" parse type
	null
;

: admin_to_char  ( name -- )
	"to-char" check-access
	smart-find ?dup null? if exit then
	"Teleport to [ dup ]" parse type
	coords@ 50 coords-randomize jump
;

: admin_character_list  ( name -- )
	"players-data-view" check-access
	smart-find ?dup null? if exit then
	to object
    object player@ c.target!
	"jbforth/admin/players/character_list.htm" show
;

: admin_banchat
	"banchat" check-access
	2 args ( name time | time-for-target | name-for-infinity-banchat )
	?dup null? if \ only time or name
		dup int if \ time
			target-or-me@
		else \ name
        	smart-find ?dup null? if exit then
        	-1 swap
		then
	else
       	swap smart-find ?dup null? if exit then
	then
	swap int 60000 * over "updateNoChannel" { int.class } jexec
	"Banchat for [  ] by [ name@ ]" parse
	dup "bans" >log .
;

: admin_unbanchat
	"banchat" check-access
	smart-find ?dup null? if exit then
	0 over "updateNoChannel" { int.class } jexec
	"Unbanchat for [  ] by [ name@ ]" parse
	dup "bans" >log .
;

: p.no-channel@  ( player -- )
	"NoChannel" p@
	dup int 0 < if drop -1 exit then
	1000L d/ int
	time -
	dup 0 > if 
		60 / 
	else
		drop 0
	then
;
