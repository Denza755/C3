\ Server-wide variables autosavable in database

: sv@ ( var-name -- value )
	"SELECT `value` FROM `server_variables` WHERE `type` = 'svalue' AND `name` LIKE '[ >slashes ]';" parse
	query ?dup if type exit then
	dup list# 0= if drop null exit then
	0 list@ "value" m@
;

: sv-del ( var-name -- )
	"DELETE FROM `server_variables` WHERE `type` = 'svalue'	AND `name` LIKE '[ >slashes ]';" parse
	update ?dup if type else drop then
;

: sv! ( value var-name -- )
	over null? if  nip sv-del exit	then
		
	"REPLACE `server_variables` SET `type`='svalue', `name` = '[ >slashes ]', `value` = '[ >slashes ]';" parse
	update ?dup if type else drop then
;

new-list svalue sv-list

: svalue  ( val -- \ name )
    also forth definitions
	value
	latest sv-list list+
	latest sv@ 
	?dup null? not if
		"to " latest s+ eval
	then
	previous definitions
;

: sv-save ( name -- )
    forth
	dup eval swap sv!
;

: sv-save-all  ( -- )
	sv-list ['] sv-save do-list
;

on-jbf-stop: sv-save-all
