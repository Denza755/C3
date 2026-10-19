new-hashmap value access-map

: access  ( access_level -- / access_name )
	\ Get name of access from input stream and set it to word from stack
	\ Example: 		20 access ride
	access-map next-word m$!
;

: access?  ( "access_name" -- flag )
	\ Check to access level by name from stack
	\ Example:		"ride" access?		\ return true or false

	\ If called from telnet - always full access
	telnet? if
		drop
		true
		exit
	then

	access-map swap m$@ ?dup null? if
		access-map "default" m$@
	then
	player@ "AccessLevel" p@
	<=
;

: check-access  ( "access-name" -- )
	\ check access. If yes, nothing. If no - exit from called word with warning.
	access? not if
		"You have not access level" .
		rdrop
		exit
  	then
;

"config/access.f" INCLUDED
