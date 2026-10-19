: npc-7767-dialog-append
	'<br><a action="bypass -h jbf_items_exchange">Обмен эвентовых предметов</a><br>' .
;
	
{
	{ 3875 5 5586 }
	{ 3877 5 5564 }
	{ 3887 5 5563 }
	{ 3880 5 5583 }
	{ 3876 5 5566 }
	{ 4396 5 5587 }
	{ 4395 5 5565 }

	{ 5862 3 6403 }
	{ 6363 3 6406 }
	{  812 1 6407 }
	{ 1111 1 6407 }
} value exch-rules

0 uvalue changed

: exchange  ( {item_from item_to rate} -- )
	list-rev> drop
	>r swap
	dup items# ?dup if			\ ( rate item_id item_count )
		dup changed + to changed
		tuck swap items_remove	\ ( rate count )
		f* 1 fround
		r> items_add
	else
		rdrop
		2drop
	then
;

: bypass_items_exchange
	0 to changed
	exch-rules ['] exchange do-list
	changed if
		"Вы обменяли предметов:" . changed .
	else
		"У вас нет предметов для обмена" .
	then
;
