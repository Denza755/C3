@echo off
title L2MarkuS Server Console
color 4f
:start
echo Starting L2MarkuS Game Server v2.1.
echo.
java -Xms1024m -Xmx1024m -cp ./../libs/*;l2markus.jar net.sf.l2j.gameserver.GameServer
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
