\ Show player commands help

\ called by ".help"

: user_help
	>r

	r@ "jail" = if
		"jbforth/users/help/jail.htm" show
		exit
	then

	rdrop

	"jbforth/users/help/help.htm" show
;
