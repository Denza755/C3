\ Map of appends lists
new-hashmap value npc-dialog-appends-map

: new-list?  ( null|list -- list )
\ Make new empty list if input null, else nothing.
	?dup null? if new-list then
;

: npc-dialog+  ( 'code npc-id -- )
	string >r npc-dialog-appends-map over m@
	new-list?							\ ( 'code list )
	tuck list+							\ ( list )
	npc-dialog-appends-map r> m!
;

: dialog@-loop  ( "sum-string" 'code -- "sum-string-next" )
	execute s+
;

: npc-dialogs@  ( npc -- "string" )
	npc-dialog-appends-map swap string m@ ?dup null? if "" exit then
	"" swap ['] dialog@-loop do-list
;
