@echo off
title L2MarkuS Login Console
color 4f
:start
echo Starting L2MarkuS Login Server v2.1.
echo.
java -Xmx128m -Xmx128m -cp ./../libs/*;l2markus.jar net.sf.l2j.loginserver.LoginServer
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
