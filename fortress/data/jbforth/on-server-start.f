\ This file loaded after full server initialization

matherial-randdrop-append

item-handlers   server@ "ItemTable" p@  "ItemHandlers" p!

doors-to-open-at-start ['] door-open do-list

goe-doors-open \  "[Garden of Eva doors engine started] " .
coliseum-doors-close

on-server-start-words ['] execute do-list

item-handler-map item-table@ "setItemHandlers" 1 jexec


"RESTART_TIME" config@ hours restart

"UPDATE `characters` SET `accesslevel` = '0' WHERE `accesslevel` = '-1'" update drop drop

"Server startup process ended" .out
