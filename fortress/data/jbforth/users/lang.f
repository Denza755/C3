\ Langs and translation switcher by Balancer
\ 2005-11-05

: .lang_switch
	lang@ "ru" = if
		"en"
	else
		"ru"
	then
;

: .translit_switch
	translit@not if
		"on"
	else
		"off"
	then
;

: .status   if "Выключить" else "Включить" then ;

: user_lang_show
	"jbforth/users/lang.htm" show
;

: user_lang
	1 args
	>r r@ "ru" = r@ "en" = or if
		r> dup "en" = if drop null then 
		to lang@
	else
		rdrop 
	then
	user_lang_show
;

: user_translit
	1 args
	dup "off" = if
		drop
		true to translit@not
	else
		null to translit@not
	then
	user_lang_show
;
