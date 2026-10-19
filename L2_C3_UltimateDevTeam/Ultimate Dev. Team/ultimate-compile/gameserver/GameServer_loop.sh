#!/bin/bash

err=1
until [ $err == 0 ]; 
do
	mv log/java0.log.0 "log/`date +%Y-%m-%d_%H-%M-%S`_java.log"
	mv log/stdout.log "log/`date +%Y-%m-%d_%H-%M-%S`_stdout.log"
	nice -n -2 java -Xms512m -Xmx512m -cp bsf.jar:javolution.jar:bsh-2.0.jar:jython.jar:c3p0-0.9.0.jar:mysql-connector-java-3.1.10-bin.jar:sqljdbc.jar:l2jserver.jar net.sf.l2j.gameserver.GameServer > log/stdout.log 2>&1
	err=$?
#	/etc/init.d/mysql restart
	sleep 10;
done
