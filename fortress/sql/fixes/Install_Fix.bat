@echo off
REM ######################################## DB path

REM ### MYSQL 4.1 ###
REM set mysqlBinPath=C:\Program Files\MySQL\MySQL Server 4.1\bin

REM ### MYSQL 5.0 ###
set mysqlBinPath=C:\Program Files\MySQL\MySQL Server 5.0\bin

REM ######################################## DB

set user=root
set pass=
set DBname=l2jdb
set DBHost=localhost

REM ############################################

set mysqldumpPath="%mysqlBinPath%\mysqldump"
set mysqlPath="%mysqlBinPath%\mysql"

set ctime=%TIME:~0,2%
if "%ctime:~0,1%" == " " (
set ctime=0%ctime:~1,1%
)
set ctime=%ctime%'%TIME:~3,2%'%TIME:~6,2%

echo.
echo Making a full backup before installing fixes.
mkdir ..\backup
%mysqldumpPath% --add-drop-table -h %DBHost% -u %user% --password=%pass% %DBname% > ..\backup/%DATE%-%ctime%_upgrade.sql

echo.
echo Installing fixes.

%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < FixDrop.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < FixNPC.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < FixPirTun.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < l2wh.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < respawn.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < drop.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < exp_sp.sql

echo.
echo Fixes installed.


set /A usedhours=%TIME:~0,2%-%ctime:~0,2%
set /A usedmins=%TIME:~3,2%-%ctime:~3,2%
set /A usedsecs=%TIME:~6,2%-%ctime:~6,2%
if "%usedhours:~0,1%" EQU " " (
set usedhours=%usedhours:~1,1%
)
If "%usedmins:~0,1%" EQU " " (
set usedmins=%usedmins:~1,1%
)
If "%usedsecs:~0,1%" EQU " " (
set usedsecs=%usedsecs:~1,1%
)
If %usedsecs% LSS 0 (
set /A usedsecs=%usedsecs%+60
set /A usedmins=%usedmins%-1
)
if %usedmins% LSS 0 (
set /A usedmins=%usedmins%+60
set /A usedhours=%usedhours%-1
)
if %usedhours% GTR 0 (
Set usedtime=%usedhours%h
)
if %usedmins% GTR 0 (
Set usedtime=%usedtime% %usedmins%m
)

echo.
echo Time used to perform operation: %usedtime%%usedsecs%s
echo.
echo Done.
echo.

pause