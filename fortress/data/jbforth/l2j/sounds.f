: play-music  ( "MusicName" player@ -- )
	1 rot 0 0 0 0 0 "PlaySound" { int.class string.class int.class int.class int.class int.class int.class } jnew
	swap broadcast
;
