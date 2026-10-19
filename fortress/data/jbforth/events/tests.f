{
{ 115285 -182680 -1467 }
{ 115575 -182670 -1474 }
{ 116325 -182367 -1527 }
{ 117752 -182441 -1557 }
{ 118042 -182420 -1574 }
{ 118945 -182143 -1947 }
{ 119496 -182033 -1903 }
{ 119982 -181973 -1901 }
{ 120453 -181903 -1996 }
{ 121514 -181130 -1926 }
{ 123790 -181784 -1909 }
{ 124641 -181653 -1907 }
{ 125168 -181224 -1807 }
{ 125963 -180182 -1803 }
{ 127185 -179693 -1870 }
{ 127859 -178802 -1988 }
{ 129152 -177623 -2175 }
{ 129601 -177259 -2254 }
{ 129943 -176993 -2371 }
{ 130447 -177017 -2568 }
{ 130984 -177434 -2723 }
{ 132783 -178715 -2670 }
{ 134187 -179026 -2498 }
{ 136718 -179297 -1888 }
{ 137526 -179311 -1668 }
{ 138136 -178963 -1583 }
{ 139071 -178065 -1567 }
{ 139305 -177833 -1558 }
{ 139888 -177486 -1569 }
} value path_to_abandoned_mine

0 value c
: gm_zz
	7527 find-by-npc_id dup to c
	path_to_abandoned_mine
	walk
;

: max  ( a b -- max_a,b )
	2dup < if swap then
	drop
;

: drop-avoid  ( item count player -- {items-list} )
	new-list rot ( item player {list} count )
	1 max 0 ?do
		( item player {list} )
		>r >r
		dup 1 r@ r@ coords@ 100 + 100 coords-randomize item_spawn
		( item_id new_item_obj; R: player list )
		r> swap r@ list+ r>
	loop nip nip
;

: admin_idrop  ( -- \ item_id [count] )
	\ Drop <count> items with <item_id> around GM 
	"drop" check-access
	2 args int 1 max 
	swap int 
	swap
	player@
	drop-avoid
	drop
;

: decay-by-list  ( {items-list} -- )
	list> 0 max 0 ?do
		item_unspawn
	loop
;

\ { } user-value ttitems
: tt1  
	5010 5 player@ drop-avoid 
	4412 5 player@ drop-avoid 
	4413 5 player@ drop-avoid 
	4414 5 player@ drop-avoid 
	4415 5 player@ drop-avoid 
	4416 5 player@ drop-avoid 
	4417 5 player@ drop-avoid 
;
\ : t2 ttitems decay-by-list ;


\ {
\ { 656 149518 46630 -3438 0 0 false } \ Lesser Giant Scout
\ { 209 149788 46911 -3438 0 0 false } \ Ol Mahum Marksman
\ { 5060 149514 46670 -3438 0 0 false } \ Kracha
\ } value npc-for-spawn-test
