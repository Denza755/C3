: MYTARGET@  ( -- my_target_oid )  PLAYER@ TARGET@ ;

: args  ( "arg1 arg2 arg3 .. argn .. argx" n -- arg1 arg2 .. argn )
	depth 1 = if
		tail swap
	then
	swap explode swap 0 ?do
		list-shift
  	loop
  	drop
;

\ Return n-th element of list, placed in return stack in caller+caller word
: rl@  ( n -- i ) r> r> rot r@ swap list@ swap >r swap >r ;
