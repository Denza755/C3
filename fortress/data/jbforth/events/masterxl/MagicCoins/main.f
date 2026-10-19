\ (c)MasterXL  MagicCoins quest
\  Warehouse Keeper Sorint id 7232 3 range 
\  Warehouse Keeper Randolf  id 7095 Giran
\  Warehouse Keeper Julia  id 7152 Elven willage
\  Warehouse Keeper Cliff  id 7182 Oren
\  Teleporter Mozella id 7483 Cruma entrance 3 range
\  Guard Stan id 7200 Oren 2 range
\  Ferris id 7847 Aden 1 range
\  Warehouse Keeper Collob id 7092 Giran 1 range
\  Merchant Pano id 7078 Floran willage 1 range
\  Coin Collect Album id 5900
\ Guild 3 rate
\ Guild 2 rate needed - 1 Blood Werewolf, 1 Gold Drake, 1 Silver Fairy, 1 Blood Drevanul, 1 Gold Knight, 1 Silver Golem
\ Guil 1 rate needed - 1 Silver Dryad, 1 Blood Basilisk, 1 Gold Giant, 1 Silver Undine, 1 Blood Succubus, 1 Gold Wyrm. 
\ coins - 	Blood Medusa	3472 drop ( 604, 583, 584, 585, 678 )
\	Gold Wyvern		3482 drop ( 578, 663, 240, 276, 235, 279, 245, 284, 146, 280 )
\	Silver Unicorn		3490 drop ( 568, 569, 685, 161, 575, 572 )

\ === statement
new-hashmap
\	coin-to-del coin-to-give need-coin-change coin-to-del2 need-coin-change-2
    { 3472	3483	10	0	0	0	0 } => 1
    { 3472	3491	10	0	0	0	0 } => 2
    { 3472	3473	10	0	0	0	0 } => 3
    { 3472	3480	20	0	0	0	0 } => 4
    { 3482	3483	10	0	0	0	0 } => 5
    { 3482	3494	10	0	0	0	0 } => 6
    { 3482	3475	10	0	0	0	0 } => 7
    { 3482	3480	20	0	0	0	0 } => 8
    { 3490	3485	10	0	0	0	0 } => 9
    { 3490	3473	10	0	0	0	0 } => 10
    { 3490	3498	20	0	0	0	0 } => 11
    { 3483	3484	5	3472	10	0	0 } => 12
    { 3483	3474	5	3472	10	0	0 } => 13
    { 3483	3492	5	3472	10	0	0 } => 14
    { 3483	3497	10	3472	20	0	0 } => 15
    { 3494	3486	5	3490	10	0	0 } => 16
    { 3494	3492	5	3490	10	0	0 } => 17
    { 3494	3495	5	3490	10	0	0 } => 18
    { 3494	3479	10	3490	20	0	0 } => 19
    { 3475	3484	5	3491	5	3482	10 } => 20
    { 3475	3476	5	3491	5	3482	10 } => 21
    { 3475	3495	5	3491	5	3482	10 } => 22
    { 3475	3488	10	3491	10	3482	20 } => 23
    { 3474	3477	5	3492	5	3483	10 } => 24
    { 3474	3493	5	3492	5	3483	10 } => 25
    { 3474	3481	5	3492	5	3483	10 } => 26
    { 3474	3478	10	3492	10	3483	20 } => 27
    { 3476	3477	5	3484	5	3485	10 } => 28
    { 3476	3493	5	3484	5	3485	10 } => 29
    { 3476	3481	5	3484	5	3485	10 } => 30
    { 3476	3487	10	3484	10	3485	20 } => 31
    { 3486	3477	5	3495	5	3491	10 } => 32
    { 3486	3493	5	3495	5	3491	10 } => 33
    { 3486	3481	5	3495	5	3491	10 } => 34
    { 3486	3496	10	3495	10	3491	20 } => 35
value coins-list
new-hashmap
\ item-buy item-sell-1 need-item-1 item-sell-2 need-item-2 item-sell-3 need-item-3
    { 874	3480	1	3473	6	3494	6 } => 1
    { 905	3489	1	3475	8	3491	7 } => 2
    { 936	3498	1	3483	5	3485	4 } => 3
    { 916	3480	3	3489	4	3498	3 } => 4
    { 2433	3474	1	3495	1	0	0 } => 5
    { 1332	3486	1	3476	1	3498	1 } => 6
    { 2456	3484	1	3492	1	3489	1 } => 7
    { 1149	3479	1	3488	1	3497	1 } => 8
    { 198	3478	1	3481	2	3495	2 } => 9
    { 230	3496	1	3479	2	3497	1 } => 10
    { 95	3496	1	3477	2	3493	2 } => 11
    { 132	3478	1	3496	1	3487	1 } => 12
    { 2381	5961	20	5962	20	5963	20 } => 13
    { 2392	5961	20	5962	20	5963	20 } => 14
    { 2399	5961	10	5962	10	5963	10 } => 15
    { 2404	5961	10	5962	10	5963	10 } => 16
    { 2417	5961	7	5962	7	5963	7 } => 17
    { 2475	5961	7	5962	7	5963	7 } => 18
    { 601	5961	7	5962	7	5963	7 } => 19
    { 74	5961	4	5962	4	5963	4 } => 20
    { 285	5961	4	5962	4	5963	4 } => 21
    { 266	5961	4	5962	4	5963	4 } => 22
value shop-list
0 value npc-id
null suvalue guild	\ value of range in Guild saved in DB
0 suvalue dialog

\ ====== death section ;)
 { 583 584 585 604 678 } value npc-medusa
 { 568 569 685 161 572 575 } value npc-unicorn
 { 587 663 240 276 235 279 279 245 284 146 280 } value npc-wyvern
: on-npc-targ
    player@ target@ "NpcId" p@ to npc-id
;
: guild? 
    guild 0= if exit then
; 
: wyv-target
    guild?
    on-npc-targ
    npc-id npc-wyvern in-list? 0= if exit then "ok" .
    rnd 0.3 f< if 1 3482 killer p.items_add then
;
: uni-target
    guild?
    on-npc-targ
    npc-id npc-unicorn in-list? 0= if exit then
     rnd 0.3 f< if 1 3490 killer p.items_add then
;

: med-target
    guild?
    on-npc-targ
    npc-id npc-medusa in-list? 0= if exit then
     rnd 0.3 f< if 1 3472 killer p.items_add then
;

' wyv-target 587 npc-die+!
' wyv-target 663 npc-die+!
' wyv-target 240 npc-die+!
' wyv-target 276 npc-die+!
' wyv-target 235 npc-die+!
' wyv-target 279 npc-die+!
' wyv-target 245 npc-die+!
' wyv-target 281 npc-die+!
' wyv-target 146 npc-die+!
' wyv-target 280 npc-die+!
' med-target 583 npc-die+!
' med-target 584 npc-die+!
' med-target 585 npc-die+!
' med-target 604 npc-die+!
' med-target 678 npc-die+!
' uni-target 568 npc-die+!
' uni-target 569 npc-die+!
' uni-target 685 npc-die+!
' uni-target 161 npc-die+!
' uni-target 572 npc-die+!
' uni-target 575 npc-die+!
' med-target 583 npc-die+!
' med-target 584 npc-die+!
' med-target 585 npc-die+!
' med-target 604 npc-die+!
' med-target 678 npc-die+!

\ ===== Dialogs

: npc-7232-dialog-append
    dialog 1 = if '<br><font color="ff0000">Go to Julia in the Elven Willadge!</font><br>'. exit then
    '<br><a action="bypass -h jbf_MagicCoins">What about coins?</a></br>' .
;
: npc-7483-dialog-append
    '<br><a action="bypass -h jbf_Guild-member31">I want change coins</a></br>'. 
    
    '<br><a action="bypass -h jbf_buy-items">I want buy items</a></br>'. 
;
: npc-7182-dialog-append
    '<br><a action="bypass -h jbf_Guild-member32">I want change coins</a></br>'. 
    '<br><a action="bypass -h jbf_buy-items">I want buy items</a></br>'. 
;
: npc-7200-dialog-append
    '<br><a action="bypass -h jbf_Guild-member2">I want change coins</a></br>'. 
    '<br><a action="bypass -h jbf_buy-items">I want buy items</a></br>'. 
;
: npc-7165-dialog-append
    '<br><a action="bypass -h jbf_Guild-member21">I want change coins</a></br>'. 
    '<br><a action="bypass -h jbf_buy-items">I want buy items</a></br>'. 
;
: npc-7688-dialog-append
    '<br><a action="bypass -h jbf_Guild-member22">I want change coins</a></br>'. 
    '<br><a action="bypass -h jbf_buy-items">I want buy items</a></br>'. 
;
: npc-7847-dialog-append
    '<br><a action="bypass -h jbf_Guild-member1">I want change coins</a></br>'. 
    '<br><a action="bypass -h jbf_buy-items">I want buy items</a></br>'. 
;
: npc-7092-dialog-append
    '<br><a action="bypass -h jbf_Guild-member11">I want change coins</a></br>'. 
    '<br><a action="bypass -h jbf_buy-items">I want buy items</a></br>'. 
;
: npc-7078-dialog-append
    '<br><a action="bypass -h jbf_Guild-member12">I want change coins</a></br>'. 
    '<br><a action="bypass -h jbf_buy-items">I want buy items</a></br>'. 
;
: npc-7152-dialog-append
    dialog 0= if exit then
    '<br><a action="bypass -h jbf_Julia">I bring to you a present from Sorint</a></br>' .
;    
: Julia-reward
    5900 player@ inventory? 1 < if 0 to dialog exit then
    "Oh shit! Welcome to guild! " show
    4 to guild
    3 to dialog
    1 5900 items_remove
;
: bypass_Julia
    5963 player@ inventory? 1 < if  exit then
    1 5963 items_remove
    "Oh, thank you! Can do you samething for me? Please, take this Album and bring it to Sorint with my please. And this my gift to you." show
    1 5900 items_add
    1 736 items_add
    1 737 items_add
    2 to dialog
;
: bypass_agree
    guild 1 < if
    " Go to Julia in the Elven Willadge and give she my small present. Take this coin" show 
    1 5963 items_add 
    1 to dialog
    exit
    then "You can't do it, because you are member of Guild!" . 
;

: bypass_level2     
    guild 4 = if "jbforth/events/masterxl/MagicCoins/level2.htm" show exit then
    guild 3 = if "jbforth/events/masterxl/MagicCoins/level1.htm" show exit then
    guild 2 = if "jbforth/events/masterxl/MagicCoins/leveltop.htm" show exit then
    guild 1 = if "You are have a top range of guild" show exit then
    "You are not in guild" show    
;

: bypass_2guild
  guild 4 = if   
  3473 player@ inventory? 1 < if "You have not " 3473 i.name@ s+ . exit then
  3485 player@ inventory? 1 < if "You have not " 3485 i.name@ s+ . exit then
  3491 player@ inventory? 1 < if "You have not " 3491 i.name@ s+ . exit then
  3475 player@ inventory? 1 < if "You have not " 3475 i.name@ s+ . exit then
  3483 player@ inventory? 1 < if "You have not " 3483 i.name@ s+ . exit then
  3494 player@ inventory? 1 < if "You have not " 3494 i.name@ s+ . exit then
  1 3473 items_remove 
  1 3485 items_remove
  1 3491 items_remove
  1 3475 items_remove
  1 3483 items_remove
  1 3494 items_remove
  3 to guild
  "Now you have 2 range in the Guild!" show
  then exit
;
: bypass_1guild
    
  3492 player@ inventory? 1 < if "You have not " 3492 i.name@ s+ . exit then
  3474 player@ inventory? 1 < if "You have not " 3474 i.name@ s+ . exit then
  3484 player@ inventory? 1 < if "You have not " 3484 i.name@ s+ . exit then
  3495 player@ inventory? 1 < if "You have not " 3495 i.name@ s+ . exit then
  3476 player@ inventory? 1 < if "You have not " 3476 i.name@ s+ . exit then
  3486 player@ inventory? 1 < if "You have not " 3486 i.name@ s+ . exit then
  1 3492 items_remove 
  1 3474 items_remove
  1 3484 items_remove
  1 3495 items_remove
  1 3476 items_remove
  1 3486 items_remove
  2 to guild
  "Now you have 1 range in the Guild!" show
;

: bypass_topguild
    
  3487 player@ inventory? 1 < if "You have not " 3487 i.name@ s+ . exit then
  3478 player@ inventory? 1 < if "You have not " 3478 i.name@ s+ . exit then
  3496 player@ inventory? 1 < if "You have not " 3496 i.name@ s+ . exit then
  5961 player@ inventory? 1 < if "You have not " 5961 i.name@ s+ . exit then
  5962 player@ inventory? 1 < if "You have not " 5962 i.name@ s+ . exit then
  5963 player@ inventory? 1 < if "You have not " 5963 i.name@ s+ . exit then
  1 3487 items_remove 
  1 3478 items_remove
  1 3496 items_remove
  1 5961 items_remove
  1 5962 items_remove
  1 5963 items_remove
  1 to guild
  "Now you have top range in the Guild!" show
;


: ctd@	 ( -- coin-to-del )		0 rl@ ;
: ctg@	 ( -- coin-to-give )		1 rl@ ;
: ncc@	 ( -- need-coin-change )	2 rl@ ;
: ctd2@	 ( -- coin-to-del-2 )		3 rl@ ;
: ncc2@	 ( -- need-coin-change-2 )	4 rl@ ;
: ctd3@	 ( -- coin-to-del-3 )		5 rl@ ;
: ncc3@	 ( -- need-coin-change-3 )	6 rl@ ;

: bypass_change3
    string coins-list swap m@
    >r
    ctd@ items# 0=  if "You have not item " ctd@ i.name@ s+  .rexit then
    ncc@ ctd@ items# > if "Not enough [ ctd@ i.name@ ]" .rexit then
    ncc2@ ctd2@ items# > if "Not enough [ ctd2@ i.name@ ]" .rexit then
    ncc3@ ctd3@ items# > if "Not enough [ ctd3@ i.name@ ]" .rexit then
    ncc@ ctd@ items_remove
    ncc2@ ctd2@ items_remove
    ncc3@ ctd3@ items_remove
    1 ctg@ items_add
    rdrop
;
: ib@	( -- items-buy )		0 rl@ ;
: is1@	( -- items-sell-1 )		1 rl@ ;
: ni1@	( -- need-items-1 )		2 rl@ ;
: is2@	( -- items-sell-2 )		3 rl@ ;
: ni2@	( -- need-items-2 )		4 rl@ ;
: is3@	( -- items-sell-3 )		5 rl@ ;
: ni3@	( -- need-items-3 )		6 rl@ ;

: bypass_shop
    string shop-list swap m@
    >r
    ni1@ is1@ items# > if "Not enough  [ is1@ i.name@ ]"  .rexit then
    ni2@ is2@ items# > if "Not enough  [ is2@ i.name@ ]"  .rexit then
    ni3@ is3@ items# > if "Not enough  [ is3@ i.name@ ]"  .rexit then
    ni1@ is1@ items_remove
    ni2@ is2@ items_remove
    ni3@ is3@ items_remove
    1 ib@ items_add
;

: bypass_MagicCoins
    guild 0 > if "jbforth/events/masterxl/MagicCoins/2.htm" show exit then
    dialog 2 = if Julia-reward exit then
    dialog 3 < if "jbforth/events/masterxl/MagicCoins/1.htm" show exit then
;

: bypass_disagree
    guild 0= if "You are not in guild" . exit then
    0 to guild
    0 to dialog
    "Now you're not member. Good bye!" show
;
: bypass_Guild-member3
    guild 0= if exit
    then "jbforth/events/masterxl/MagicCoins/member3.htm" show
;
: bypass_Guild-member31
    guild 0= if exit
    then "jbforth/events/masterxl/MagicCoins/member31.htm" show
;
: bypass_Guild-member32
    guild 0= if exit
    then "jbforth/events/masterxl/MagicCoins/member32.htm" show
;

: bypass_Guild-member2
    "jbforth/events/masterxl/MagicCoins/member2.htm" show
;
: bypass_Guild-member21
    "jbforth/events/masterxl/MagicCoins/member21.htm" show
;
: bypass_Guild-member22
    "jbforth/events/masterxl/MagicCoins/member22.htm" show
;

: bypass_Guild-member1
    "jbforth/events/masterxl/MagicCoins/member1.htm" show
;
: bypass_Guild-member11
    "jbforth/events/masterxl/MagicCoins/member11.htm" show
;
: bypass_Guild-member12
    "jbforth/events/masterxl/MagicCoins/member12.htm" show
;


: bypass_Guild-member3
    "jbforth/events/masterxl/MagicCoins/member3.htm" show
;

: bypass_buy-items
    guild 0 > if "jbforth/events/masterxl/MagicCoins/price.htm" show exit then
    "Your are not in guild" show
;

: bypass_buy-items1
    guild 0= if exit then
    guild 3 <= if "jbforth/events/masterxl/MagicCoins/price1.htm" show exit then
    "You must have a 2 range of Guild"  show
;

: bypass_buy-items2
    guild 0= if exit then
    guild 2 <= if "jbforth/events/masterxl/MagicCoins/price2.htm" show exit then
    "You must have a 1 range of Guild" show
;

: bypass_buy-items3
    guild 0= if exit then
    guild 1 = if "jbforth/events/masterxl/MagicCoins/pricetop.htm" show exit then
    "You must have a top tange of Guild" show
;

