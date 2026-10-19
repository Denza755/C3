\ Teleport current player to x y z coords for adena

: gatekeeper-jump ( x y z adena -- )
	adena? not if
		"You have not adena!" .
		drop 2drop
		exit
	then
	
	player@ adena-!
	
	jump
;

\ Bypass packets
: bypass_gkj_7878_giran   Town_of_Giran  6300 gatekeeper-jump ;
: bypass_gkj_7878_dion    Town_of_Dion   6500 gatekeeper-jump ;
