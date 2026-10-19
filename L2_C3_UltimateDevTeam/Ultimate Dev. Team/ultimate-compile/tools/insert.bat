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

echo Installling new content.
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < %1
pause
