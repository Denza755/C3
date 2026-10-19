\ Remove polymorph from character
: unpoly  ( char -- )  0 null rot 0 (polymorph) ;

\ Polymorph player char to npc with npc_id code
: poly  ( npc_id char -- )  "npc" swap -1 (polymorph) ;

\ Polymorph char to item with npc_id code
: ipoly  ( item_id char -- )  "item" swap -1 (polymorph) ;

