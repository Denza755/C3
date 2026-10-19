: don  ( Debug on  )  -1 boolean "DEBUG" config! ;
: admin_don  ( Debug on  )  "debug" check-access   -1 boolean "DEBUG" config! ;
: admin_doff ( Debug off )  "debug" check-access    0 boolean "DEBUG" config! ;
: admin_ton \ Devel trace on
	"debug" check-access  
	-1 boolean "net.sf.l2j.Config" class "DEVELOPER" field!
;

: admin_toff \ Devel trace off
	"debug" check-access  
	0 boolean "net.sf.l2j.Config" class "DEVELOPER" field!
;

: reg@  "CurrentWorldRegion" p@ ;

: admin_gap  "debug" check-access  1250 target-or-me@ "getAllPlayers" 1 jget . ;
: admin_gvp  "debug" check-access  1250 target-or-me@ "getVisiblePlayers" 1 jget . ;
: admin_gko  "debug" check-access  500 target-or-me@ "getKnownObjects" { int.class } jget . ;
: admin_ikp  "debug" check-access  target-or-me@ "KnownPlayers" p? . ;
: admin_gvo  "debug" check-access  player@ 6144 target-or-me@ reg@ "getVisibleObjects" { L2Object.class int.class } jget . ;
: admin_dist "debug" check-access  player@ target-or-me@ "getDistance" { L2Object.class } jget . ;
: admin_knm  "debug" check-access  player@ "ObjectId" p@ target-or-me@ "knowsObjectCheck" { Integer.class } jget . ;
: admin_mkn  "debug" check-access  target-or-me@ "ObjectId" p@ player@ "knowsObjectCheck" { Integer.class } jget . ;

: admin_raw   "debug" check-access  target-or-me@ reg@ "RawVisibleObjects" p@ . ;
: admin_watching   "debug" check-access  target-or-me@ reg@ "RawWatchingChars" p@ . ;
