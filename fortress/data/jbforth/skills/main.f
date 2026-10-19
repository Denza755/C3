0 user-value calc-stat-modif

: get-min-skill-level
  skill "Id" p@
  self "ClassId" p@
  skill x.level

  "net.sf.l2j.gameserver.SkillTreeTable" class 
  "getMinSkillLevel"

  { int.class  "net.sf.l2j.gameserver.model.ClassId" class  int.class } jget
;

new-hashmap
  1.0   =>  92    \ Shield Stun
  0.8   =>  100   \ Stun Attack
  1.0   =>  260   \ Hammer Crush
  0.4   =>  1072  \ Sleeping Cloud
  0.6   =>  1069  \ Sleep
  0.03  =>  1170  \ Anchor
  0.4   =>  1201  \ Dryad Root
  0.6   =>  4047  \ Hold
  0.7   =>  4073  \ NPC Stun Attack
  0.7   =>  4075  \ Shock
  0.4   =>  107   \ Holy Aura
value skill-rate

: pair-chance  ( -- rate )
  skill-rate skill "Id" p@ string m@ 
  dup null? if
    drop 0.5
\   "Undefined skill [ skill ]" parse .out
  then
  0.5 f*
  skill x.level 1 + 2 f/ sqrt f*
;

: calc-success  ( -- flag )
  self x.level   
  target x.level   
  f/ 
  
  pair-chance   f*
  calc-stat-modif f*

  atan pi f/ 2. f*
  
  self L2PcInstance.class instance-of? if
    "Chance =" . dup 100. f* "%2.1f" .f "%" .
  then
  
\ "Use [ skill ] from [ self ][[ self x.level ]] to [ target ][[ target x.level ]]; chance=[ dup 100 f* 0.001 fround ]%" parse .out
  rnd f>
;

: calc-matk-modif  ( modify "type" -- flag )
  target swap p@  f/ 

  target skill self "getMAtkSps" { L2Character.class L2Skill.class } jget 
  target self skill mdef
  f/  sqrt              \ matk/mdef

  f*

  to calc-stat-modif
  calc-success
; 

: calc-patk-modif  ( modify "ModifType" -- flag )
  target swap p@  f/          \ modify/targetModifType
  to calc-stat-modif
  calc-success
; 

: calc-PARALYZE-success ( -- flag )  40 "WIT" calc-matk-modif ;
: calc-SLEEP-success  ( -- flag )  40 "WIT" calc-matk-modif ;
: calc-ROOT-success   ( -- flag )  30 "CON" calc-matk-modif ;

: calc-STUN-success ( -- flag )   30 "CON" calc-patk-modif ;

: calc-PDAM-success
  skill "Id" p@ { 48 81 92 100 101 120 4063 4072 4073 4075 260 281 4120 } in-list? if
    30 "CON" calc-patk-modif
    exit
  then

  null
;

: calc-savevs
  skill "Savevs" p@
  dup 0 = if
    drop
    1
    exit
  then
  dup 1 = if
    drop
    23
    target "INT" p@ f/
    exit
  then
  dup 2 = if
    drop
    11
    target "WIT" p@ f/
    exit
  then
  dup 3 = if
    drop
    27
    target "MEN" p@ f/
    exit
  then
  dup 4 = if
    drop
    30
    target "CON" p@ f/
    exit
  then
  dup 5 = if
    drop
    23
    target "DEX" p@ f/
    exit
  then
  6 = if
    drop
    30
    target "STR" p@ f/
    exit
  then
;

: calc-NEW-success
  skill "ActivateRate" p@
  dup -1 = if
    exit
  then
  skill "MagicLevel" p@
  target x.level -
  \ skill "LevelModifier" p@ *
  +
  calc-savevs sqrt f*
  \ 100. f/ dup . rnd dup " " . . " " . f> dup .
  100. f/ rnd f>
;

: calc-NEWmagic-success
  skill "ActivateRate" p@
  dup -1 = if
    exit
  then
  skill "MagicLevel" p@
  target x.level -
  \ skill "LevelModifier" p@ *
  +
  calc-savevs sqrt f*

  target skill self "getMAtkSps" { L2Character.class L2Skill.class } jget
  target self skill mdef
  f/  sqrt  f*

  \ 100. f/ dup . rnd dup " " . . " " . f> dup .
  100. f/ rnd f>
;

skills/skill-learn-list
skills/skill-learn-map

\ : unStunCheck   rnd 0.1 f< ;
\ ['] unStunCheck L2Character.class "unStunCheck" field!

0 user-value attacker

: on-hp-reduce@  ( -- map<object_id,array<code>> )
  L2Character.class "reduceHpTargetHandlers" field@
;

: on-hp-reduce!  ( map<object_id,array<code>> -- )
  L2Character.class "reduceHpTargetHandlers" field!
;

: on-obj-hp-reduce@  ( obj-id -- array<code> )
  on-hp-reduce@ swap m@
;

: on-obj-hp-reduce!  ( array<code> obj-id -- )
  on-hp-reduce@ swap m!
;

: on-hp-reduce+  ( 'code object -- )
    "ObjectId" p@
    dup on-obj-hp-reduce@  ( 'code object_id array )
    new-list?
    rot over ( object_id array 'code array ) list+
  swap on-obj-hp-reduce!
;

: on-hp-reduce-  ( 'code object -- )
    "ObjectId" p@
    nip
    new-list  ( oid list )
  swap on-obj-hp-reduce!
;

:noname  ( damage1 -- damage2 )
  self "Pet" p@ ?dup if
    over 0.1 f* swap ( damage pet-damage pet )
    attacker swap
    "reduceCurrentHp" { doub.class L2Character.class } jexec
    0.9 f*
  then
; value pain-to-pet-transfer
