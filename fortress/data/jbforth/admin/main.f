\ GM commands handlers

admin/admin
admin/help
admin/teleports
admin/polymorph
admin/ride
admin/users
admin/debug
admin/actions
admin/server
admin/config
admin/reload
admin/players
admin/tools

: bypass_admin_menu
	"admin-menu" check-access
	'<button value="Play Sounds" action="bypass -h admin_play_sounds" width=90 height=15 back="sek.cbui94" fore="sek.cbui92">'
	'<button value="Paralyze" action="bypass -h forth player@ target@ c.paralyze" width=90 height=15 back="sek.cbui94" fore="sek.cbui92">' S+
	'<button value="Unparalyze" action="bypass -h forth player@ target@ c.unparalyze" width=90 height=15 back="sek.cbui94" fore="sek.cbui92">' S+
	show
;

: gm_set-hp
	\ Set target HP

	"char-modify" check-access
	
	float
	target-or-me@ 
	"setCurrentHp" { doub.class } jexec
;

: gm_set-mp
	\ Set target HP

	"char-modify" check-access
	
	float
	target-or-me@
	"CurrentMp" set(d)
;
