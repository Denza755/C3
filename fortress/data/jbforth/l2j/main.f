l2j/var-load
l2j/constants
l2j/variables
l2j/server

l2j/access

false suvalue back-coordinates

l2j/map
l2j/teleports
l2j/doors

: distance ( xt yt player -- distance )
	coords@ drop ( xt yt xp yp )
	rot - sqr
	rot rot - sqr
	+ sqrt
	int
;

: cr+   "
" s+ ;

l2j/telnet

l2j/player
l2j/spawns
l2j/sounds
l2j/items
l2j/logs
l2j/zones
l2j/broadcast
