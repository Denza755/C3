@echo off
:start
echo Starting L2J server.
echo.
java -Xmx512m -cp bsf.jar;bsh-2.0.jar;c3p0-0.8.4.5.jar;mysql-connector-java-3.0.16-ga-bin.jar;l2jserver.jar;jython.jar net.sf.l2j.Server
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