\ Ride/unride

: admin_ride  ( -- )  
\ Show ride menu
	"ride" check-access
	"jbforth/admin/ride.htm" show
;

: admin_ride_wyvern    ( -- ) ( Ride gm on wyvern )	"ride" check-access	wyvern player@ ride ;
: admin_ride_strider   ( -- ) ( Ride gm on strider)	"ride" check-access	strider player@ ride ;
: admin_unride         ( -- ) ( unride GM ) "ride" check-access   player@ unride ;
: admin_unride_wyvern  ( -- ) ( unride GM ) admin_unride ;
: admin_unride_strider ( -- ) ( unride GM ) admin_unride ;

\ unride selected player
: admin_ur   "ride" check-access   target-or-me@ unride ;

\ ride strider selected player
: admin_sr   "ride" check-access   admin_ur strider target-or-me@ ride ;

\ ride wyvern selected player
: admin_wr   "ride" check-access   admin_ur wyvern target-or-me@ ride ;
