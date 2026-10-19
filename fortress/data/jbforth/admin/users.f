: trim
	"^ +" "" replace
	" +$" "" replace
;

: (att.)  ( hash -- )
	"char_name" m@
	. "
" .
;

: admin_account_chars  ( "name" -- )
	\ See all chars from account, where placed targeted or named char.
	
	trim ?dup if
	else
		\ not given name, get target
		target-or-me@ p.name@
	then
	
	"SELECT account_name FROM characters WHERE char_name LIKE '[ >slashes ]' LIMIT 1" parse 
	query ?dup if
		"Error while getting account chars: [ ]" parse .out
		exit
	then
		
	0 list@ "account_name" m@
	
	"Chars from account [ dup ]
" parse .
	
	"SELECT char_name FROM characters WHERE account_name LIKE '[  >slashes ]'" parse 
	query ?dup if
		"Error while getting account chars: [ ]" parse .out
		exit
	then

	['] (att.) do-list
;

		