service/teleport
\ service/forum

: online-notify
	player-online-notify unless exit then
	"[ caller 'Name' p@ ] is online parse ." do-players
;

: offline-notify
	player-offline-notify unless exit then
	'caller "Name" p@    " is offline " s+ . ' do-players
;
