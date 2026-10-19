\ To accept changes in this file type "//reload" into game or telnet.



: map>loc-c3  ( map_x map_y -- loc_x loc_y )
	\ map-coordinates to server-coordinates.
	dup 39 < if
		12198.6   f* 539062.8  f- \ Elmor
	else
		12178.364 f* 505265.57 f- \ Aden
	then

	swap

	12191.92  f* 559420.12 f-

	0 +
	swap 0 +
;

: loc>map-c3  ( loc_x loc_y -- map_x map_y )
	\ server-coordinates to map-coordinates
    dup -30500 < if
    	539062.8  f+ 12198.6   f/
   	else
		505265.57 f+ 12178.364 f/
	then
	swap
	559420.12 f+ 12191.92  f/
	swap
;


: map>loc-c4  ( map_x map_y -- loc_x loc_y )
    \ map-coordinates to server-coordinates.
	
    12189.921 f* 505882.184 f-  0 + \ y
    swap
    12188.148 f* 362490.963 f-  0 + \ x
	swap
;
					
: loc>map-c4  ( loc_x loc_y -- map_x map_y )
    \ server-coordinates to map-coordinates
    505882.184  f+ 12189.921 f/     \ y
    swap
    362490.963 f+ 12188.148 f/      \ x
    swap
;

: revision@  ( -- revision-version )
	player@ "NetConnection" p@ "Client" p@ "Revision" p@
;

: map>loc  ( map_x map_y -- loc_x loc_y )
	revision@ 560 > if map>loc-c4 else map>loc-c3 then
;

: loc>map  ( loc_x loc_y -- map_x map_y )
	revision@ 560 > if loc>map-c4 else loc>map-c3 then
;
