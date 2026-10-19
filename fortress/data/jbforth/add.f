\ { } value ttitems
\ : t1 1 max 57 swap  player@ drop-avoid to ttitems ;
\ : t2 ttitems decay-by-list ;

: (tx1)
	self 'Clan' p@ ?dup if '[' swap s+ '] ' s+ else '' then step@ s+ .to-caller
;

: tx "(tx1)" do-players ;

: jtest 55261 163983 -2642 jump ;
: x5  -14929 122899 -2462 jump ;
