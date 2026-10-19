: door>id  ( door -- door_id )
	"DoorId" p@
;

: id>door  ( door_id -- door_object )
	doors@ "Door" get(i)
;

: door-open  ( door_id -- )
	id>door "openMe" 0 jexec
;

: door-close  ( door_id -- )
	id>door "closeMe" 0 jexec
;
