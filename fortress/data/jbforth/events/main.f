"Events loading..." .out

vocabulary events
events also definitions

tools/main.f
\ walk/main.f

4442 CONSTANT Lottery_Ticket

: sell-ticket  ( price -- )
    Lottery_Ticket player@ inventory? 0 > if
		"You already have ticket" .
		drop
		exit
	then

    adena? not if
		drop
		"Not enough adena" .
		exit
    then
    
    player@ adena-!
	Lottery_Ticket item_add
;

: jump-for-item ( x y z item_id -- )
    dup player@ inventory? 1 < if
		"You have not needed item!" .
		2drop 2drop
		exit
	then
	
	1 player@ inventory-! drop
	jump
;

games/CurrentEvent
events/admin

masterxl/main.f

\ events/tests

\ balancer/main.f

previous definitions
