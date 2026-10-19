\ : gm-log  ( "text" -- )

\ Door and custodian pair write to file
: admin_door/custodian>file  ( door_id -- )
	"admin" check-access
	'{ [ player@ target@ "NpcId" p@ ] [  ] }
' parse "data/jbforth/doors-pairs.f" file-append
	"Door/custodian pair saved" .
;
	