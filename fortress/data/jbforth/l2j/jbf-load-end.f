: JBForth.new  ( -- JBForth.instance )
	self "ru.balancer.JBForth" { Object.class } jnew
;

: player_jbf_refresh
    player@ unless exit then
	JBForth.new
	player@ "jbf" field!
;

: reload_jbf  ( -- )
	on-jbf-stop-words ['] execute do-list
	reload_jbf
	['] player_jbf_refresh do-players
	JBForth.new jbf@ "Object" p@ "jbf" field!
;

['] suv-load-all do-players

item-handler-map item-table@ "setItemHandlers" 1 jexec

server-addintional-load
on-jbf-start-words ['] execute do-list
