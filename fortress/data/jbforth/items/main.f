new-hashmap value item-handler-map

: item-handler+  ( 'code item-id -- )
        string item-handler-map swap m!
;

items/potions
items/christmas-tree
items/tutorial
items/rolling-dice
items/scrolls

\ : on-skill-2001-target        "!!!" . .s ;