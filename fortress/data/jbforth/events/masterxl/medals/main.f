vocabulary medals
medals also definitions


\ ======= MasterXl ======= 
\ Medal shop
\ 6392 Event Medal
\ 6393 Event Glittering Medal
\
new-hashmap
\ items_to_buy | event_medals event_glittering_medal
	{	1374	300		} => 1	\ Greater Haster Potion
	{	1375	600		} => 2	\ Swift Attack Potion
	{	6036	600		} => 3	\ Magic Haste Potin
	{	1540	1000	} => 4	\ Quick Healing Potion
	{	1538	1500	} => 5	\ Blessed Scroll of Escape
	{	3936	1000	} => 6	\ Blessed Scroll of Ressurection
	{	958		600		} => 7	\ Scroll Enchant armor D-grade
	{	957		600		} => 8	\ Scroll Enchant weapon D-grade
	{	954		1500	} => 9	\ Scroll Enchant armor C-grade
	{	953		1500	} => 10	\ Scroll Enchant weapon C-grade
	{	6394	3500	} => 11	\ Red Party Mask
value medalshop-items-list  

: itb@	( -- items-to-buy )		0 rl@ ;
: em@	( -- event-medals )		1 rl@ ;
  
new-hashmap
\ items_to_buy | event_glittering_medal
	{	6326	2000	} => 1	\ Sealed Majestic Necklace
	{	6327	1950	} => 2	\ Sealed Majestic Earring
	{	6328	1300	} => 3	\ Sealed Majestic Ring
	{	1540	10	 	} => 4	\ Quick Healing Potion
	{	1538	15	 	} => 5	\ Blessed Scroll of Escape
	{	3936	10	 	} => 6	\ Blessed Scroll of Ressurection
	{	950		600		} => 7	\ Scroll Enchant armor B-grade
	{	949		600		} => 8	\ Scroll Enchant weapon B-grade
	{	730		900		} => 9	\ Scroll Enchant armor A-grade
	{	731		900		} => 10	\ Scroll Enchant weapon A-grade
	{	961		1500	} => 11	\ Scroll Enchant armor S-grade
	{	962		1500	} => 12	\ Scroll Enchant weapon S-grade
value medalshophi-items-list  

: ithb@	( -- itemsh-to-buy )			0 rl@ ;
: egm@	( -- event-glittering-medal )	1 rl@ ;  

0 user-value npcid

: .rexit ( "text" -- )
	rdrop
	parse .
	rdrop
;

forth also definitions
: bypass_do-medals
	player@ target@ "NpcId" p@ to npcid
;

: bypass_medals
    ._s
	string medalshop-items-list swap m@
	>r
	em@ 6392 items# > if "Not enough " egm@ 6392 items# - s+ " medals" s+ parse .rexit then
	em@ 6392 items_remove
	1 itb@ items_add
	rdrop
;

: bypass_medalshi
	string medalshophi-items-list swap m@
	>r
	egm@ 6393 items# > if "Not enough [ egm@ 6393 items# - ] medals" parse .rexit then
	egm@ 6393 items_remove
	1 ithb@ items_add
	rdrop
;
previous definitions

\ ======statement
{ spawnlist.f } value medals-spawnlist
false value medals-started
{ } value medals-spawned-list

\ ======awards
: award   1 6393 killer p.items_add ;
: awards  1 6392 killer p.items_add ;

\ ======main code

: start
	medals-spawnlist npc-list-spawn  to medals-spawned-list
	true to medals-started
	"Medal collect begin" announce do-players
;

: stop
	medals-spawned-list npc-list-unspawn
	false to medals-started
	"Medal collect stopped" announce do-players
;

: run? 
	medals-started not if rdrop exit then
;

:noname ( on npc die )
	run?
	rnd 0.1 f< if award then
	rnd 0.5 f< if awards then
; { 1061 815 812 810 813 1077 1076 1075 815 250 190 191 168 68 } ['] npc-die+! p-do-list

\ ==== dialogs

forth also definitions

: bypass_medals-1
	"1.htm" show
;	

: bypass_medals-2
	"2.htm" show
;

: bypass_medals-change
	6392 player@ inventory? 1000 <= if
	"Not enough medals!" show
	exit
	then
	1000 6392 items_remove
	10 6393 items_add
;

: reload_jbf
	medals-started if stop then
	reload_jbf
;

: admin_events_masterxl_medals_menu
	"events-admin" check-access
	'<html><body><font color="LEVEL">Мероприятие "Medals Event"</font><br>'
	'<a action="bypass -h forth events masterxl medals start">Запуск эвента</a><br>' s+
	'<a action="bypass -h forth events masterxl medals stop">Остановка эвента</a><br>' s+
	'</body></html>' s+
	show
;
previous definitions

"Medals Event" "admin_events_masterxl_medals_menu" admin-event+

previous definitions
