\ 2005-10-02, (c)Balancer, (c)Tais
\ Event "Mirror of Duke Favorite"
\ Description:
\ 	Observer (big spider, npc_id 10202, hp=>200k, 66lvl) once spawn near Iris Lake.
\	Kill them, get quest item (Musfel fang, 3863)
\	Who kill them - give award (id given from start word) from King Amadeo (12254).

{ events/051002-Mirror/spawnlist } value 051002m-spawnlist

\ ========= Begin of main code ============

0 suvalue tmp-051002m-step

: step@	tmp-051002m-step int ;
: step? step@ > if rdrop exit then ;
: step! to tmp-051002m-step ;

{ } value 051002m-spawned-list
null value 051002m-item-spawn

false value 051002m-runned

: 051002m-start   
	ride-hair-disable
	"player@ unride" do-players
	051002m-spawnlist npc-list-spawn to 051002m-spawned-list 
	true to 051002m-runned
	1854 1 null -15508 124498 -2557 item_spawn to 051002m-item-spawn
	'Event "Mirror of Duke favorite" started!' announce
	'Details to you are informed by Mysterios Dark Elf in the Castle of Gludio' announce
	"'ItemSound.quest_fanfare_2' so" do-players
;

: 051002m-stop   
	051002m-item-spawn item_unspawn
	false to 051002m-runned
	051002m-spawned-list npc-list-unspawn 
	ride-hair-enable

	player@ "AccessLevel" p@ 100 < if 
		'Event "Mirror of Duke favorite" ended!' announce
		'Winner is ' player@ "Name" p@ s+ "!!!" s+ announce
		"'ItemSound.quest_fanfare_2' so" do-players
	then
;

: run?   
	051002m-runned not if rdrop exit then 
\	player@ "AccessLevel" p@ 100 < if 0 0 0 jump rdrop exit then
;

: npc-7661-dialog-append
	run?
	'<br><a action="bypass -h jbf_051002m-7661">Event "Mirror of Duke Favorite"</a><br>' .
;

: bypass_051002m-7661
	run?
	step@ 0= step@ 2 = or if
		"jbforth/events/051002-Mirror/7661.htm" show
		exit
	then

	step@ 1 = if
		'You must kill <font color="LEVEL">Preacher of Doom</font>' show
		exit
	then

	step@ 20 =  step@ 22 = or if
		"jbforth/events/051002-Mirror/7661-2.htm" show
		exit
	then

	step@ 21 = if
		'You must kill <font color="LEVEL">Immortal sevior Mardril</font>' show
		exit
	then

	step@ 30 =  step@ 32 = or if
		"jbforth/events/051002-Mirror/7661-3.htm" show
		exit
	then

	step@ 31 = if
		'You must kill <font color="LEVEL">Great Deamon King</font>' show
		exit
	then

	step@ 40 >= if
		5970 items# 0= if 1 5970 items_add then
		"jbforth/events/051002-Mirror/7661-4.htm" show
		exit
	then
	
	"I'm nothing say to you. Say to admin about " step@ s+ " error :) " s+ show
;

: on-npc-8196-die   run?  1 step@ <> if exit then   'Ok! You have killed Preacher of Doom and may try to solve the riddle again!' killer p.show  2 step! ;

: bypass_051002m-7661-2
	run?
	2 args = if
		20 step!
		"First riddle was solved by " player@ "Name" p@ s+ announce
		"jbforth/events/051002-Mirror/7661-2.htm" show
		exit
	then

	1 step!
	'You are miss and must kill <font color="LEVEL">Preacher of Doom</font> for try again' show
	300 sleep
	-18579 109295 -2683 50 coords-randomize jump
;

: bypass_051002m-7661-3
	run?
	2 args = if
		30 step!
		"Second riddle was solved by " player@ "Name" p@ s+ announce
		"jbforth/events/051002-Mirror/7661-3.htm" show
		exit
	then

	21 step!
	'You are miss and must kill <font color="LEVEL">Immortal sevior Mardril</font> for try again' show
	300 sleep
	-18260 109803 -2683 50 coords-randomize jump
;

: on-npc-876-die   run?  21 step@ <> if exit then   'Ok! You have killed Immortal sevior Mardril and may try to solve the riddle again!' killer p.show  22 step! ;

: bypass_051002m-7661-4
	run?
	2 args = if
		40 step!
		"3rd riddle was solved by " player@ "Name" p@ s+ announce
		1 5970 items_add
		"jbforth/events/051002-Mirror/7661-4.htm" show
		exit
	then

	31 step!
	'You are miss and must kill <font color="LEVEL">Great Deamon King</font> for try again' show
	300 sleep
	-17721 109466 -2683 50 coords-randomize jump
;

: on-npc-5138-die   run?  31  step@ <> if exit then   'Ok! You have killed Great Deamon King and may try to solve the riddle again!' killer p.show  32 step! ;

: npc-7827-dialog-append
	run? 40 step?
	'<br><a action="bypass -h jbf_051002m-7827">Event "Mirror of Duke Favorite"</a><br>' .
;

: bypass_051002m-7827
	run? 40 step?
	5970 items# if
		1 5970 items_remove
		50 step!
		wyvern player@ ride
		"Good fly!" show
		exit
	then
	
	50 step@ >= if
		wyvern player@ ride
		"Good fly!" show
		exit
	then
;

: npc-12093-dialog-append
	run? 50 step?
	'<br><a action="bypass -h jbf_051002m-12093">Event "Mirror of Duke Favorite"</a><br>' .
;

: bypass_051002m-12093
	run? 50 step?
	1854 items# if
		1 1806 items_add
		1 370 items_add
		1 1854 items_remove
		"jbforth/events/051002-Mirror/12093.htm" show
		051002m-stop
		exit
	then
;

: start   051002m-start ;
: stop    051002m-stop ;

: x1 -18235 109254 -2523 jump ;
: x2 -18579 109295 -2683 jump ;
: x3 -15508 124498 -2557 jump ;
: x4 -13974 123368 -3143 jump ;

: reload_jbf    stop reload_jbf ;
