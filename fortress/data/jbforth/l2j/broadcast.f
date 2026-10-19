: p.action-failed  ( player -- )
\ Send ActionFailed packet to player
	"ActionFailed" 0 new-server-packet
	swap p.send-packet
;

: action-failed   player@ p.action-failed ;

: p.broadcast  ( packet player -- )
	"broadcastPacket" { ServerBasePacket.class } jexec
;

: broadcast-other  ( packet player -- )
	"broadcastPacketToOthers" { ServerBasePacket.class } jexec
;

: broadcast-party  ( packet player -- )
    >r
	r@ party@ ?dup if
		rdrop
		"broadcastToPartyMembers" { ServerBasePacket.class } jexec
	else
		r> p.send-packet
	then
;

: new-SystemMessage  ( msg_id -- SystemMessage )
	"SystemMessage" { int.class } new-server-packet
;

: sm.addString   ( SystemMessage "string" -- SystemMessage )  string over "addString" 1 jexec ;
: sm.addNumber   ( server-packet n        -- server-packet )  int over "addNumber"   { int.class } jexec ;
: sm.addItemName ( server-packet item_id  -- server-packet )  int over "addItemName" { int.class } jexec ;

: new-MagicSkillUser  ( L2Character:char L2Character:target int:skillId int:skillLevel int:hitTime int:reuseDelay -- MagicSkillUser )
	"MagicSkillUser" { L2Character.class L2Character.class int.class int.class int.class int.class } new-server-packet
;

: type ( "text" -- )
	>r
	614 new-SystemMessage
	r@ sm.addString
	player@ ?dup if 
		p.send-packet 
	else
		r@ .
	then
	rdrop
;

: p.type  ( "text" player -- )
	614 new-SystemMessage
	rot sm.addString
	swap p.send-packet
;
