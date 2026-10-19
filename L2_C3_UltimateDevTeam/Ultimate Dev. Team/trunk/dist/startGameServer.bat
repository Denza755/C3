@echo off
title UltimateGameInstance
:start
echo Starting Ultimate Game Server.
echo.
rem ======== Optimize memory settings =======
rem 
rem   1GB: -Xmn128m -Xms512m -Xmx512m
rem 1.5GB: -Xmn192m -Xms768m -Xmx768m
rem   2GB: -Xmn256m -Xms1024m -Xmx1024m
rem  
rem Make sure -Xmn value is always 1/4 the size of -Xms and -Xmx.
rem -Xms and -Xmx should always be equal.
rem ==========================================
java -Xmx512m -cp bsf.jar;bsh-2.0.jar;javolution.jar;c3p0-0.9.0.jar;mysql-connector-java-3.1.10-bin.jar;sqljdbc.jar;l2jserver.jar;jython.jar net.sf.l2j.gameserver.GameServer
if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end
:restart
echo.
echo Admin Restart ...
echo.
goto start
:error
echo.
echo Server terminated abnormaly
echo.
:end
echo.
echo server terminated
echo.
pause
