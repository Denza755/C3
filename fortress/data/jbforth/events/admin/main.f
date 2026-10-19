new-list value event-admin-menus

: show_admin_event_link  ( str1 {text word_name} -- str2 )
	list> drop
	'<a action="bypass -h [  ]">[  ]</a><br>' parse s+
;

: admin-event+ ( text word-name -- )
	2 >list
	event-admin-menus list+
;

also forth definitions

: admin_events
	"events-admin" check-access
	event-admin-menus list# 0= if
		"Not any events to manage" show exit
	then

	"" event-admin-menus ['] show_admin_event_link do-list
	"!Events management
	
" swap s+ show
;

previous definitions
