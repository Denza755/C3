: >log  ( "string" "file" -- )
	>r parse

	player@ if 
		" (player [ name@ ]; oid=[ player@ 'ObjectId' p@ ];" parse
		player@ target@ if
			" target=[ player@ target@ ])" parse s+
		else
			" target=null)" parse s+
		then
	else
		" (player==null)"
	then

	parse s+ cr+
	
	"log/game/" r> s+ ".log" s+
	file-append
;
