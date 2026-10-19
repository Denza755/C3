53 constant EARNED_S2_S1_s

: id>item  ( item_id -- L2Item )
	item-table@
	"getTemplate" { int.class } jget
;

: i.name@  ( item_id -- item_name )  id>item "Name" p@ ;

\ Get adena for curent player
: adena#  ( -- count )  player@ adena@ ;

\ Have current player n adena or not?
: adena?  ( n -- n f )  adena# over >= ;

\ Return count of items for curent player
: items#  ( item_id -- count )  player@ inventory? ;

: p.items_add  ( items_count item_id player -- )  >r swap r> inventory+! ;

\ add count items to current player
: items_add  ( count item_id -- )  swap player@ inventory+! ;

\ add 1 item to current player
: item_add  ( item_id -- )  1 player@ inventory+! ;

: inventory_show  ( -- ) 
	player@ true boolean 
	"ItemList" { L2PcInstance.class bool.class } new-server-packet
	send-packet
;

: adena_pay  ( adena -- ) \ remove adena from player if exists. Exit from called word if not have.
	adena? not if
		drop
    	"You have not enough money to pay" show 
    	rdrop \ drop one call level, return to up level function
    	exit
	then

	~Adena swap player@ inventory-! drop
;

: items_remove  ( count_to_remove item_id -- ) 
	\ remove count items from inventory. Exit from calling word if not have.
	2dup items# (  count_to_remove  item_id  count_to_remove have_count  )
	> if \ not enough items
		"Not enough items. Need " .
		over .						\ print count
		i.name@	.						\ print item name
		1 > if "s" . then			\ print "s" if multiple items
		rdrop exit					\ exit from calling word
	then
	swap player@ inventory-! drop
;

: item_remove  ( item_id -- )
	\ remove one item from inventory of current player
	1 swap items_remove
;



\ === Items exchange engine ===
\ Use like:
\ { 
\    { 1000 57 } { ... } { ... }
\    { 1 4362 } { ... } { ... }
\ } items_exchange

false uvalue have-items?

: (change-check-loop)  ( { count item } -- )
	list-rev> drop
	2dup items# > if
		"You have not [ swap ] [ i.name@ ] for exchange!" parse .
		false to have-items?
		exit
	then

	2drop
;

: (change-remove-loop)  ( { count item } -- )
	list-rev> drop
	items_remove
;

: (change-add-loop)  ( { count item } -- )
	list-rev> drop
	items_add
;

: items_exchange  ( { { count_from1 item_from1 } { count_from2 item_from2 } ... } { { count_to1 item_to1 } ... } -- )
	swap
	true to have-items?
	dup ['] (change-check-loop) do-list
	have-items? not if 2drop exit then
	['] (change-remove-loop) do-list
	['] (change-add-loop) do-list
	inventory_show
;
