new-hashmap 
	"admin_reload" => back  	
	"Сообщение" => title 
	"JBForth перезагружен!" => message
value admin_message

: admin_reload_npc  ( -- )
	"reload" check-access
	"NpcTable reload begin" type
	NpcTable "reloadAllNpc" 0 jexec
	"NpcTable reload end" type
	
	"Перезагрузка даных NPC и дропа завершена" admin_message "message" m!
	"jbforth/admin/_msg_back.htm" show
;


: admin_reload_skills  ( -- )
	"reload" check-access
	skills-reload
	"Skill table reloaded" . 
;

: admin_reload  ( -- )
	"reload" check-access
	"jbforth/admin/reload.htm" show
;

: admin_reload_spawns  ( -- )
	"reload" check-access
	SpawnTable "reloadAll" 0 jexec
;

: admin_reload_abuse  ( -- )
	"reload" check-access
	config-instance@ "abuseLoad" 0 jexec
;

: admin_reload_jbf  ( -- )
	"reload" check-access
	"reload_jbf" eval \ For last version of word
	"jbforth/admin/_msg_back.htm" show
;

: admin_reload_strings  ( -- )
	"reload" check-access
	"net.sf.l2j.gameserver.lib.Strings" class "reload" 0 jget drop

	"Перезагрузка строковой библиотеки (в т.ч. транслита) завершена" admin_message "message" m!
	"jbforth/admin/_msg_back.htm" show
;

: admin_file_cache_clean  ( -- )
	"reload" check-access
	"net.sf.l2j.gameserver.lib.Files" class "cacheClean" 0 jget drop

	"Очистка файлового кеша завершена" admin_message "message" m!
	"jbforth/admin/_msg_back.htm" show
;
