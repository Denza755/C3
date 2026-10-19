: admin_config
	"admin-menu" check-access
	depth if
		?dup if show exit then
	then
	false to dialog-back
	"jbforth/admin/config/main.htm"
	show
;

: admin_altconfig
	"admin-menu" check-access
	"jbforth/admin/config/altconfig.htm" show
;

: htm-config-boolean-parse   ( config_name text -- html )
	'<tr><td>[  ] = <font color="' parse
	over config@ tuck if '00FF00">ON' else 'FF0000">OFF' then '</font></td>' s+ s+ swap
	if
		'<td></td><td><button width=40 height=15 back="sek.cbui94" fore="sek.cbui92" action="bypass -h forth 0 boolean ' s+
		" '[ over ]' " parse s+
		'config! dialog-back admin_config " value="Off"></td>' s+
	else
		'<td></td><td><button width=40 height=15 back="sek.cbui94" fore="sek.cbui92" action="bypass -h forth -1 boolean ' s+
		" '[ swap ]' " parse s+
		'config! dialog-back admin_config " value="On"></td>' s+
	then
	'</tr>' s+
;

: admin_config-int-set:  ( "field-name value" -- )
    "config" check-access
	explode list-rev> drop
	int swap config!
	dialog-back admin_config
;

: admin_config-float-set:  ( "field-name value" -- )
    "config" check-access
	explode list-rev> drop
	int swap config!
	dialog-back admin_config
;

: htm-config-int-parse   ( config_name text -- html )
	'<tr>
<td>[  ] = <font color="FF8F00">[ dup >r config@ ]</font></td>
<td><edit var="[ r@ ]" width=40 height=10></td>
<td><button value="Set" width=40 height=15 back="sek.cbui94" fore="sek.cbui92" action="bypass -h admin_config-int-set: [ r@ ] $[ r@ ]"></td>
	</tr>'parse
	rdrop
;

: htm-config-float-parse   ( config_name text -- html )
	'<tr>
<td>[  ] = <font color="FF8F00">[ dup >r config@ ]</font></td>
<td><edit var="[ r@ ]" width=40 height=10></td>
<td><button value="Set" width=40 height=15 back="sek.cbui94" fore="sek.cbui92" action="bypass -h admin_config-float-set: [ r@ ] $[ r@ ]"></td>
	</tr>'parse
	rdrop
;
