834 constant S1_ROLLED_S2

: use-rolling-dice
    6 choose 1+ >r
    player@ 'ObjectId' p@
    item-id
    r@
    player@ coords@ 50 coords-randomize
	"net.sf.l2j.gameserver.serverpackets.Dice" { int.class int.class int.class int.class int.class int.class } jnew
	player@ broadcast

	S1_ROLLED_S2 new-SystemMessage
	name@ sm.addString
	r>    sm.addNumber

	player@ in-town? if
		player@ broadcast-party
	else
		player@ broadcast
	then
;

: make-rolling-loop  ( item_id -- )  ": use-item-[  ]   use-rolling-dice ;" parse eval ;

{ 4625 4626 4627 4628 } ['] make-rolling-loop do-list
