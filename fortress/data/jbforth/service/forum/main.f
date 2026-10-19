: make-bypass  ( action text -- link )  '<a action="bypass -h [ swap ]">[  ]</a>' parse ;

0 uvalue tmp_forum_data

: post>topic  ( post_id -- topic_id )
	"SELECT topic_id FROM punbb.posts WHERE id=[  ]" parse 
	query if rdrop exit then 
	list> drop 'topic_id' m@
;

: topic.title  ( topic_id -- "title" )
	"SELECT subject FROM punbb.topics WHERE id=[  ]" parse
	query if rdrop exit then 
	list> drop 'subject' m@
;

: topic>forum  ( topic_id -- forum_id )
	"SELECT forum_id FROM punbb.topics WHERE id=[  ]" parse 
	query if rdrop exit then 
	list> drop 'forum_id' m@
;

: forum.name  ( forum_id -- "forum-name" )
	"SELECT forum_name FROM punbb.forums WHERE id=[  ]" parse 
	query if rdrop exit then 
	list> drop 'forum_name' m@
;

: add-forum  ( string query-hash -- string2 )	
	>r
	"jbf_show-forum [ r@ 'id' m@ ]" parse 
	r@ 'forum_name' m@ 20 str-left-cut
	make-bypass
	
'<tr><td>[  ]</td>
<td>[ r@ "last_poster" m@ ]</td></tr>' parse s+
	rdrop
;

: bypass_forums-list ( -- )
	"SELECT id, forum_name, num_topics, num_posts, last_post, last_post_id, last_poster FROM punbb.forums WHERE cat_id=6 AND id<>12 ORDER BY disp_position;"
	query ?dup if show exit then
	'<table width=100%><tr><td><font color="LEVEL">Форум:</font></td><td><font color="LEVEL">Последний пост</font></td></tr>'
	swap ['] add-forum do-list
	'</table>' s+
	show
;	

: add-topic  ( string query-hash -- string2 )	
	>r
	"jbf_show-topic [ r@ 'id' m@ ]" parse 
	r@ 'subject' m@ 30 str-left-cut
	make-bypass
'<tr><td>[  ]</td>
<td>[ r@ "last_poster" m@ ]</td></tr>' parse s+ rdrop
;

: bypass_show-forum  ( "forum-id" -- )
	"SELECT id, subject, last_poster FROM punbb.topics WHERE forum_id=[  ] ORDER BY last_post DESC LIMIT 20;" parse
	query ?dup if show exit then
	'jbf_forums-list' 'Форумы' make-bypass
	'[  ]<table width=100%><tr><td><font color="LEVEL">Топик</font></td><td><font color="LEVEL">Последний пост</font></td></tr>' parse
	swap ['] add-topic do-list
	'</table>' s+
	show
;

: add-post  ( string query-hash -- string2 )	
	>r
	"jbf_show-post [ r@ 'id' m@ ]" parse 
	r@ 'message' m@ 30 str-left-cut
	make-bypass
'<tr><td>[ r@ "poster" m@ ]</td>
<td>[  ]</td></tr>' parse s+ rdrop
;

: bypass_show-topic  ( "topic-id" -- )
	>r
	"SELECT id, poster, message FROM punbb.posts WHERE topic_id=[ r@ ] ORDER BY posted DESC LIMIT 20;" parse
	query ?dup if show exit then
	'jbf_forums-list' 'Форумы' make-bypass
	
	"SELECT forum_id FROM punbb.topics WHERE id=[ r> ]" parse 
	query if exit then 
	list> drop 'forum_id' m@

	"SELECT forum_name FROM punbb.forums WHERE id=[ dup ]" parse 
	query if exit then 
	list> drop 'forum_name' m@

	'jbf_show-forum [ swap ]' parse swap make-bypass
	
	'[ swap ] ->
[  ]<table width=100%><tr><td><font color="LEVEL">Автор</font></td><td><font color="LEVEL">Пост</font></td></tr>' parse
	swap ['] add-post do-list
	'</table>' s+
	show
;

: user_forum
	bypass_forums-list
;

: bypass_show-post  ( post_id -- )
	to tmp_forum_data
	"jbforth/service/forum/post.htm" show
;

