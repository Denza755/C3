: p.name@  ( character -- "name" )  "Name" p@ ;
: oid@  ( object -- object_id )  "ObjectId" p@ ;

: name@  ( -- "current-player-name" )  player@ p.name@ ;
: p.oid@  ( -- current-player-object_id )  player@ oid@ ;

: find_player  ( "name" -- player )  world@ "Player" get(s) ; \ Find player by his name

: target-or-me@  ( -- target )  \ Return target object or player, if target not selected
	player@ target@	?dup if exit then
	player@
;

: hcoords@  ( character -- x y z heading )
	>r
	r@ "X" p@
	r@ "Y" p@
	r@ "Z" p@
	r> "Heading" p@
;

: party@  ( player -- party | null )
	dup "InParty" p? if
		"Party" p@
	else
		drop null
	then
;

: db!  ( value "table field where_field" where-value -- false | errno )
	swap
	explode list> drop
	( value where-value where-field field table )
	"UPDATE `[ >slashes  ]` SET `[ >slashes ]`='[ rot >slashes  ]' WHERE `[ >slashes  ]`='[ >slashes ]';" parse dup .
	update ?dup if exit then
	drop false
;

: db@  ( "table field where_field" where-value -- value false | errno )
	swap
	explode list> drop
	( where-value where-field field table )
	"SELECT `[ swap dup >r >slashes ]` FROM `[ >slashes ]` WHERE `[ >slashes ]`='[ >slashes ]';" parse dup .
	query ?dup if rdrop exit then
	0 list@ r> m@
;	

\ : x1  "characters char_name obj_Id" p.oid@ db@ .s ;
\ : x2  ""  "characters char_name obj_Id" p.oid@ db! ;
\ : x2  "characters char_name level" 40 db@ .s ;

: broadcast-status  ( player -- )
	"broadcastStatusUpdate" 0 jexec
;

: p.send-packet  ( packet player -- )
	"sendPacket" { ServerBasePacket.class } jexec
;

: send-packet  ( packet -- )
	player@ "sendPacket" { ServerBasePacket.class } jexec
;

: new-server-packet  ( ... name params -- )
	"net.sf.l2j.gameserver.serverpackets." rot s+ swap
	jnew
;

: f!  ( face -- )
	player@ "setFace" { int.class } jexec
	player@ broadcast-status
	player@ "HennaInfo" { L2PcInstance.class } new-server-packet
	send-packet
	player@ "getFace" 0 jget .
;

: player-db!  ( value db-fields where-value set-method set.class -- errno )
	>r >r
	rdrop rdrop
	2drop
;

: p.karma@  ( player -- karma )			\ Get karma for player
	"Karma" p@ ;

: p.karma!  ( karma player -- )			\ Set karma for player
	swap 0 max swap "setKarma" { int.class } jexec ;

: p.karma+!  ( karma-change player -- )	\ Add karma for player
	dup >r p.karma@ + r> p.karma! ;

\ For current player
: karma@   player@ p.karma@ ;
: karma!   player@ p.karma! ;
: karma+!  player@ p.karma+! ;

: p.access-level  ( player -- access-level )  "AccessLevel" p@ ;
: p.access-level! ( access-level player -- )  "setAccessLevel" { int.class } jexec ;
: access-level  ( -- access-level )  player@ p.access-level ;
: access-level! ( access-level -- )  player@ p.access-level! ;
: p.clan-leader?  ( player -- flag )  "ClanLeader" p? ;
: clan-leader?  ( -- flag )  player@ p.clan-leader? ;

: id>clan  ( clan_id -- L2Clan )  ClanTable "getClan" { int.class } jget ;
: name>clan_id  ( "clan_name" -- clan_id )  "clan_data clan_id clan_name" swap db@ ?dup if . 0 then ;
: p.clan!  ( clan_id player -- )  swap id>clan swap "setClan" { L2Clan.class } jexec ;

: x.level  ( player -- level )  "Level" p@ ;
