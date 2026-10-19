0 value name

\ Word to loads user-defined vars from character_quests table

: uv@ ( var-name -- value )
	player@ null? if
		drop
		null exit
	then

	"SELECT value FROM character_quests WHERE char_id = " player@ "ObjectId" p@ s+
	" AND name='user-var' AND var='" s+
	swap >slashes s+
	"';" s+
	
	query ?dup if . exit then
	dup list# 0= if drop null exit then
	0 list@ "value" m@
;

: uv-del ( var-name -- )
	"delete from character_quests where char_id = "	player@ "ObjectId" p@ s+
	" and name='user-var' and var='" s+
	swap >slashes s+
	"';" s+
	update ?dup if . else drop then
;

: uv! ( value var-name -- )
\	2dup "Save " swap s+ "=" s+ swap s+ .
	over null? if  nip uv-del exit	then
		
	"replace character_quests set name='user-var', char_id = "	player@ "ObjectId" p@ s+
	", var='" s+
	swap >slashes s+
	"', value='" s+
	swap s+
	"';" s+
	update ?dup if . else drop then
;

new-list value suv-list

: suvalue  ( val -- \ name )
    also forth definitions
	user-value
	latest suv-list list+
	player@ null? if exit then
	latest uv@  dup null? if
		drop
	else
		"to " latest s+ eval
	then
	previous definitions
;

: suv-load  ( name -- )
    also forth
	dup uv@ "to " rot s+ eval
	previous
;

: suv-load-all  ( -- )
	suv-list ['] suv-load do-list
;

: suv-save ( name -- )
    also forth
	dup eval swap uv!
	previous
;

: suv-save-all  ( -- )
	suv-list ['] suv-save do-list
;

: suv-save-all-players  ( -- )
	['] suv-save-all do-players
;

['] suv-load-all do-players

on-jbf-stop: suv-save-all-players
