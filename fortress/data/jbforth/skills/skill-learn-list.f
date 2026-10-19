: get-availeble-skills  ( -- skills-list )
	player@
	dup "ClassId" p@

	skill-tree-table@ "getAvailableSkills" 2 jget

	[]>list
;

: new-AquireSkillList  ( -- server-packet )  "AquireSkillList" 0 new-server-packet ;

: player&class-id@  ( -- player classId )
	player@ dup "ClassId" p@
;

: id&level>skill  ( int:skill int:level -- )
	skill-table@  "getInfo" { int.class int.class } jget
;

: calc-cost  ( skill -- cost|null )
	dup "Id" p@ 
	swap "Level" p@
	id&level>skill
	
	dup null? if exit then

	player@ "ClassId" p@  over  "getCanLearn" 1 jget 0= if drop null exit then

	player@ swap
	skill-tree-table@ "getSkillCost" { L2PcInstance.class L2Skill.class } jget
;

: asl.addSkill  ( asl flag skill -- asl flag )
	>r \ r: skill

	r@ "Id" p@
	r@ "Level" p@
	dup
	r> 
						\ ( asl flag skill-id skill-level skill-level skill )
	calc-cost

	dup null? if
		4 ndrop
		exit
	then

	0

	6 pick

	"addSkill" { int.class int.class int.class int.class int.class } jexec
	1+
;

: >aquire-skill-list  ( skill-list -- asl flag )
	new-AquireSkillList 0
	rot ['] asl.addSkill do-list
;

: sm607  ( min-level -- )
	607 new-SystemMessage
	swap sm.addNumber
	send-packet
;


: cannot-teache-notify  ( -- )
	player&class-id@
	skill-tree-table@  "getMinLevelForNewSkill" 2 jget

	dup 0> if
		sm607
		exit
	then

	drop
	"I cannot teach you any skills for your class." show
;

: skill-learn-list  ( classes-to-teach -- )
	player@ "ClassId" p@ "Id" p@
	swap in-list? not if
		"I cannot teach you any skills for your class." show
		exit
	then

	get-availeble-skills >aquire-skill-list
	0= if
		cannot-teache-notify
		exit
	then
	
	send-packet
	action-failed
;
