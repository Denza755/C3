#!/bin/bash

err=1
until [ $err == 0 ]; 
do
	mv log/java0.log "log/`date +%Y-%m-%d_%H:%M:%S`-java.log"
	mv log/stdout.log "log/`date +%Y-%m-%d_%H:%M:%S`-stdout.log"
	cp -f /usr/local/games/l2j-svn/L2_Gameserver/build/dist/l2jserver.jar l2jserver_.jar
	nice -n -2 java -Xms512m -Xmx512m -cp bsf.jar:bsh-2.0.jar:jython.jar:c3p0-0.9.0.4.jar:mysql-connector-java-3.1.12-bin.jar:jbforth.jar:l2jserver_.jar net.sf.l2j.Server > log/stdout.log 2>&1
	err=$?
#	/etc/init.d/mysql restart
	sleep 10;
done
