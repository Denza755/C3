new-hashmap
\ { skill-id skill-level hit-time reuse-delay } => item-id
        { 2001 1 1 0 } => 65    \ Red Potion
        { 2002 1 1 0 } => 725 \ Healing Drug
        { 2003 1 1 0 } => 726 \ Mana Drug
        { 2005 1 1 0 } => 728 \ Mana Potion
        { 2032 1 1 0 } => 727 \ Healing Potion
        { 2011 1 1 0 } => 734 \ Quick Step Potion
        { 2012 1 1 0 } => 735 \ Swift Attack Potion
        { 2031 1 1 0 } => 1060 \ Lesser Healing Potion
        { 2031 1 1 0 } => 1073 \ Beginner's Potion
        { 2032 1 1 0 } => 1061 \ Healing Potion
        { 2033 1 1 0 } => 1062 \ Haste Potion
        { 2034 1 1 0 } => 1374 \ Advanced Quick Step Potion
        { 2035 1 1 0 } => 1375 \ Advanced Swift Attack Potion
        { 2037 1 1 0 } => 1539 \ Greater Healing Potion
        { 2038 1 1 0 } => 1540 \ Quick Healing Potion
        { 2169 1 1 0 } => 6035 \ Magic Haste Potion
        { 2169 2 1 0 } => 6036 \ Greater Magic Haste Potion
\		{ 4342 2 1 0 } => 5956 \ Test 15 year old wine to use Clanhall: Wind walk
value potions

: all-skills-disabled?  ( -- ) 
    \ Exit from called word if all skills disabled.
	player@ "AllSkillsDisabled" p? if
		"Casting another skill now, abort new skill" .
		rdrop 
	then 
;

: use-potion ( L2PcInstance:activeChar int:magicId int:level -- )
        id&level>skill
        ?dup null? if drop exit then
        ( player skill )
        swap >r
        false boolean false boolean r>
        "useMagic" { L2Skill.class bool.class bool.class } jexec
;

: on-use-potion  ( item_id -- )
\        "Use potion [ dup ]" parse .out
        all-skills-disabled?
        1 over items_remove
        string potions swap m@
        self player@ rot list-rev> drop 
        3 pick 3 pick >r >r
        new-MagicSkillUser
        player@ broadcast
        player@ r> r> use-potion
;

: make-potion-loop  ( {list} item-id -- )
    nip
        ":noname   [ dup ] on-use-potion ;" parse eval 
        swap item-handler+
;

potions ['] make-potion-loop do-hashmap

items/potions/face-hair-changes
