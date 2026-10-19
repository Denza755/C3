: server-stat-instance  ( class-name -- instance )
	"net.sf.l2j.gameserver." swap s+ class
	"getInstance" 0 jget
;

: skill-table@  	( -- SkillTable 	)  	"SkillTable" 		server-stat-instance ;
: skill-tree-table@ ( -- SkillTreeTable )  	"SkillTreeTable"	server-stat-instance ;
: item-table@  		( -- ItemTable 		)  	"ItemTable" 		server-stat-instance ;
: teleport-location-table  ( -- TeleportLocationTable )	"TeleportLocationTable" server-stat-instance ;
: doors@	  		( -- DoorTable  	)  	"DoorTable" 		server-stat-instance ;
: world@	  		( -- L2World	  	)  	"model.L2World" 	server-stat-instance ;
: NpcTable			( -- NpcTable		)	"NpcTable"			server-stat-instance ;
: SpawnTable		( -- SpawnTable		)	"SpawnTable"		server-stat-instance ;
: ClanTable			( -- ClanTable		)	"ClanTable"			server-stat-instance ;
: Shutdown@			( -- Shutdown		)	"Shutdown"			server-stat-instance ;

"net.sf.l2j.Config" class value Config.class

: config@  ( field_name -- value )  Config.class swap field@ ;
: config!  ( value field_name -- )  Config.class swap field! ;

: config-instance@  ( -- Config.getInstance )  Config.class "getInstance" 0 jget ;

: skills-reload  ( -- ) 	skill-table@ "reload" 0 jexec ;

false value server-file-include
false value player-online-notify
false value player-offline-notify
false value jbf-first-start

"config/server.f" INCLUDED

: server-addintional-load  ( -- )
	server-file-include if
		server-file-include INCLUDED
	then
;

: restart  ( seconds -- )
	"JBForth" swap 
	int true boolean
	Shutdown@ "startTelnetShutdown" { String.class int.class bool.class }
	jexec
;

: shutdown  ( seconds -- )
	"JBForth" swap 
	int false boolean
	Shutdown@ "startTelnetShutdown" { String.class int.class bool.class }
	jexec
;

new-hashmap server@ "setNpcOnDieMap"  { "java.util.concurrent.ConcurrentHashMap" class } jexec
