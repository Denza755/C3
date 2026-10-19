: move-to  ( npc x y z h -- )
	coords>pos swap  ( pos npc )
	"AI" p@ ( pos ai )
	"move-to" ( pos ai "move-to" ) rot null
	intention!
;

new-hashmap value walk:full-path
new-hashmap value walk:curr-path

: walk:curr-path!  ( path npc-obj -- )
	walk:curr-path swap m$!
;

: walk:full-path!  ( path npc_obj -- )
	"ObjectId" p@ "" s+ 2dup 
	walk:full-path swap m$!
	swap list> >list swap walk:curr-path!
;

: walk:full-path@  ( npc-obj -- path )
	"ObjectId" p@
	walk:full-path swap "" s+ m$@
;

: walk:curr-path@  ( npc-obj -- path )
	"ObjectId" p@
	walk:curr-path swap "" s+ m$@
;

: walk?  ( npc_obj -- flag ) 
	dup  walk:full-path@ null? if drop false exit then
	walk:curr-path@ dup null? if drop false exit then
	list# 0= not 
;

: walk:next  ( npc-obj -- npc-obj x y z h )
    >r
	r@ walk:curr-path@ list-shift r@ walk:curr-path!
	rdrop
	list-rev> drop
\	"Next point: " . >r 2dup swap . . r>
	0
;

new-hashmap value on-arrived

\		r@ dup full-path walk-by-path

: walk:do  ( npc_obj -- )
	>r
	r@ walk:curr-path@ list# 0 <= if
		null r> walk:full-path!
		exit
   	then

	r@ walk:curr-path@ list# 0 > if
		r@ dup walk:next move-to
		rdrop
		exit
	then
	rdrop
;

: on-char-EVT_ARRIVED
	self "CurrentHp" p@ int
	self "MaxHp" p@ int <> if
		null exit
	then

	self walk? if
		self walk:do
		500 sleep
		true
	else
		on-arrived self "ObjectId" p@ m@ ?dup if
			100 do-timer
			true 
		else 
			null 
		then
   	then
;

: walk  ( npc-obj path -- )
	over walk:full-path!
\	>r
\	r@ walk:next drop r@ teleport-char-to 
\	sleep 500
\	r> 
	walk:do
;	
