\ Change name of current player

: player_rename ( "new_name" player -- )
	over unless 2drop
		"Can not rename to empty name" show
		exit
	then

	2dup
	"Player '[ 'Name' p@ ]' try rename to '[  ]'" "renames" >log

	>r

	"SELECT `char_name` FROM `characters` WHERE `char_name` LIKE '[ dup >slashes ]'" parse query drop
	list# if
		"This name already exists!" dup "renames" >log
		.
		drop rdrop exit
	then
	
	"UPDATE `characters` SET `char_name` = '[ dup >slashes ]' WHERE `obj_Id` = [ r@ 'ObjectId' p@ ]" parse

	warning @ >r warning off
	update
	r> warning !

	?dup if \ error
		dup .
		"Can't change name to [ swap ]: [  ]" parse show
		rdrop true exit
	then

	drop \ changed lines
	r@ remove-from-all-players
	dup r@ "Name" p! \ Set new name
	r@ dup coords@ teleport_player_to
	"Successfull changed name to [  ] " parse show
	false \ ok

	rdrop
;

: rename_for_price ( price "new_name" -- )
	\ Check for enough adena
	over player@ adena@ > if 
		drop 
		"Not enough adena" show 
		exit 
	then
	
	\ rename
	player@ player_rename if ( error )
		drop 
		exit
	then

	\ remove adena
	player@ adena-!
;
