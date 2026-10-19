\ Show Admin main menu
\ called by "//admin"

: admin_admin
	"admin-menu" check-access
	"jbforth/admin/admin.htm" show
;

: admin_misc
	"admin-menu" check-access
	"jbforth/admin/admin-misc.htm" show
;

: admin_time
	"admin-menu" check-access
	"jbforth/admin/time.htm" show
;
