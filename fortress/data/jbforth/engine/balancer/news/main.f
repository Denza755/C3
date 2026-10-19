4362 constant reciever	\ итем, наличие которого в инвентаре позволит игроку слушать объявления

true suvalue news-engine-reciever-state

\ { { 1000 57 } { 1000 57 } }  { { 1 4362 } } items_exchange

": use-item-[ reciever ]  'jbforth/engine/balancer/news/reciever.htm' show ;" parse eval

: ?:  ( "true" "false" flag -- )  if drop else nip then ;
