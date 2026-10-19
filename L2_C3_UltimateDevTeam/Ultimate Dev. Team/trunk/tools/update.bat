@echo off

REM ############################################
REM ## You can change here your own DB params ##
REM ############################################

set mysqlBinPath=C:\server\diskw\usr\local\mysql\bin
set user=root
set pass=root
set DBname=l2jdb
set DBHost=localhost

REM ############################################

set mysqldumpPath="%mysqlBinPath%\mysqldump"
set mysqlPath="%mysqlBinPath%\mysql"

echo.
echo Upgrade database table. Just ingorne all errors (i.e 1060)...

REM No update needed atm :)

pause
