\ Jail system by Balancer, (c) 2005-2006

vocabulary jail
jail also definitions

\ ======================================
\ You may change it properties
{ 83230 -15935 -1892 } value jail-coords
~BlueGemstone	constant jail-item
\ ======================================

false 	suvalue jailed?
0 		suvalue jail-to-collect
0 		suvalue jail-total-collected
0		suvalue	jail-coords-back

{
	{ 653 83894 -16865 -1860 0 120 false } \ Lesser Giant
	{ 653 82302 -16271 -1891 0 120 false } \ Lesser Giant
	{ 653 82284 -16383 -1891 0 100 false } \ Lesser Giant
	{ 653 81902 -16920 -1835 0 90 false } \ Lesser Giant
	{ 653 82809 -17258 -1865 0 60 false } \ Lesser Giant
	{ 172 81991 -16715 -1894 0 60 false } \ Sylph
	{ 172 82007 -16140 -1892 0 60 false } \ Sylph
	{ 172 81786 -15728 -1873 0 65 false } \ Sylph
	{ 172 82166 -15545 -1869 0 30 false } \ Sylph
	{ 172 82274 -15318 -1861 0 25 false } \ Sylph
} value jail-mobs

null	value	jail-mobs-spawn

: items:remove-all ( item-id -- )
	dup items# ?dup if
		swap items_remove
	else
		drop
	then
;

: jail-me ( items_count -- )
	int to jail-to-collect

	jail-to-collect jail-total-collected + to jail-total-collected
	
	true 	to jailed?
	false	to can-teleport?

	{ 736 1538 1829 1830 3958 4677 5858 5859 } ['] items:remove-all do-list

	jail-coords-back 0= if
		loc@ coords>s to jail-coords-back
	then
	
	10000 karma+!

	"You are jailed! To free you must collect [ jail-to-collect ] item(s) from killed mobs" parse 
	"Jail system" player@ .tell

	"jbforth/service/jail/help.htm" show

	jail-coords list-rev> drop jump
	suv-save-all
;

: jail-offline  ( "char_name" items -- )
	int swap >r

	"SELECT obj_Id AS object_id FROM characters WHERE char_name like '[ r@ >slashes ]'" parse
	query ?dup if . rdrop exit then

	dup list# ?dup 0= if "Not found player " r> s+ . exit then
	1 > if "More then 1 character with name " r> s+ . exit then
	
	list-shift drop
	'object_id' m@ int >r
	"REPLACE character_quests SET char_id=[ r@ ], name='user-var', var='jail-to-collect', value=[ dup ]" parse 
	update ?dup if . else drop then
	"REPLACE character_quests SET char_id=[ r@ ], name='user-var', var='jailed?', value=-1" parse 
	update ?dup if . else drop then
	rdrop

	"Character [ r> ] was offlie jailed by [ '' name@ s+ ] for [  ] items" parse dup . "jail" >log
;

: jail-stop ( -- )
	0 		to jail-to-collect
	false 	to jailed?
	true	to can-teleport?
	
	jail-item dup items# ?dup if swap items_remove else drop then
	-10000 karma+!
	
	"You are freed!" "Jail system" player@ .tell
	1000 sleep
	jail-coords-back s>coords drop jump
	0 to jail-coords-back
	suv-save-all
;

: p.jail-check ( killer -- )
    to object

	jailed? int 0= if rdrop rdrop exit then
	
	self   "Level" p@
	object "Level" p@ /
	1 max choose 0 > if exit then

	jail-to-collect 1- to jail-to-collect
	1 jail-item items_add
	"Need to collect [ jail-to-collect ] more items" parse 
	"Jail system" player@ .tell
	"Itemsound.quest_itemget" so
	suv-save-all	

	jail-to-collect 0 > if exit then

	"Player [ name@ ] collect all jail items and freed"	parse
	"jail" >log

	jail-stop
;

: on-kill  ( -- )  self  ['] p.jail-check killer p-do-player ;

: dist-to-jail  ( -- distance )  jail-coords list-rev> 2drop player@ distance ;

: enter-jail-check
	jailed? int 0= if exit then
	jail-to-collect 0= if exit then
	jail-coords-back s>coords if 
		"jbforth/service/jail/help.htm" show
		jail-coords list-rev> drop jump
		exit 
	then
	3 ndrop
	jail-to-collect jail-me
;

{ 653 172 866 867 } "['] on-kill swap npc-die+!" do-list

on-server-start:: jail-mobs npc-list-spawn drop ;
   
forth definitions
: admin_jail  ( items -- )
	"jail" check-access

	player@ target@ null? if "No target selected" . exit then

	int ?dup 0= if 100 then

	"Player [ player@ target@ p.name@ ] jailed by [ name@ ] for [ dup ] items" parse
	"jail" >log

	['] jail-me player@ target@ p-do-player 
;

: admin_jail:  ( name items -- )
	"jail" check-access

	2 args

	?dup null? if 100 then

	int	>r

	dup find_player ?dup if
		"Player [ swap ] jailed by [ name@ ] for [ r@ ] items" parse dup . 
		"jail" >log
		r> ['] jail-me rot p-do-player
		drop
	else
		r> jail-offline
	then
;

"jail: name [num]  - jail player <name> (incl. offline) for <num> (100 default) items" telnet-help+

: admin_unjail
	drop 

	"jail" check-access

	player@ target@ null? if "No target selected" . exit then

	"Player [ player@ target@ p.name@ ] unjailed by [ name@ ]" parse dup .
	"jail" >log
	
	['] jail-stop player@ target@ do-player 
;


: on-player-escape
	jailed? int if 
		"You are jailed yet." "Jail system" player@ .tell
		jail-coords list-rev> drop
	else
		0 0 0
	then
;

previous definitions
