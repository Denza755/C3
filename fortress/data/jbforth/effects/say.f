: (shout)  ( args -- )
	list> drop
	
	"shout" swap
	0
	self
	( "text" "type" "sender" sender reciever -- ) say2
;

: .shout  ( "text" "from" -- )
	2 >list "(shout)" p-do-players
;

: .tell ( "text" "sender" reciever -- )
	>r
	
	"tell" swap 0 r> say2
;

: .trade  ( "text" "sender" -- )
	2 >list "list> drop 'trade' swap 0 self say2" p-do-players
;

: .to-caller ( "text" -- )
	self "Name" p@ caller .tell
;
