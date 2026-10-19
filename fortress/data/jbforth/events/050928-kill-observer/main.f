\ 2005-09-28, (c)Balancer, (c)Tais
\ Event "Kill observer"
\ Description:
\ 	Observer (big spider, npc_id 10202, hp=>200k, 66lvl) once spawn near Iris Lake.
\	Kill them, get quest item (Musfel fang, 3863)
\	Who kill them - give award (id given from start word) from King Amadeo (12254).

3863	constant ~MusfelFang
3427	constant ~BloodyTarantulNail
170		constant ~HeavyFlail
2393	constant ~TallumLeatherArmor

"51340 81742 -3342" constant monster-coords

~BloodyTarantulNail	constant event-item-id
~TallumLeatherArmor	constant award-id

\ ========= Begin of main code ============

null value e050928ko-monster-obj	\ null-started values saved while jbf_reload
null value e050928ko-started


: 2050928ko-unspawn
    e050928ko-monster-obj null? not if
		e050928ko-monster-obj false unspawn
	then
	null to e050928ko-monster-obj
;

: e050928ko-start
	2050928ko-unspawn

	\ spawn mob
	monster-coords s>coords drop 0 ( x y z heading )
	10202 ( mob-id ) 0 ( respawn-time) false ( update-db ) spawn to e050928ko-monster-obj

	true to e050928ko-started

	'Event "Kill Observer" started!' announce
	'Details to you are informed by King Amadeus in the Castle of Aden' announce
	"'ItemSound.quest_fanfare_2' so" do-players	\ play sound to all players
;

: e050928ko-stop 
	2050928ko-unspawn

	false to e050928ko-started

	'Event "Kill Observer" ended!' announce
;

: run?  \ exit from calling word if not spawned event mob.
	e050928ko-started true <> if rdrop exit then
;

: on-npc-10202-die  
	run?  
	1 event-item-id items_add
	e050928ko-monster-obj false unspawn
	null to e050928ko-monster-obj
;

: npc-12254-dialog-append
	run?
	'<br><a action="bypass -h jbf_e050928ko_12254">about Observer kill</a><br>' .
;

: bypass_e050928ko_12254
	run?

	event-item-id items# 0 > if
		\ Have event item, observer was killed
	 	1 event-item-id items_remove
		"jbforth/events/050928-kill-observer/12254-win.htm" show
		1 award-id items_add
		e050928ko-stop
		"Winner is " player@ "Name" p@ s+ announce
		"'ItemSound.quest_fanfare_2' so" do-players	\ play sound to all players
		exit
	then
	
	"jbforth/events/050928-kill-observer/12254.htm" show
;

: start   e050928ko-start ;
: stop    e050928ko-stop  ;
