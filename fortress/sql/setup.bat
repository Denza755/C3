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

REM ########################################
set mysqldumpPath="%mysqlBinPath%\mysqldump"
set mysqlPath="%mysqlBinPath%\mysql"
%mysqlPath% -h %DBHost% -u %user% --password=%pass% --execute="CREATE DATABASE IF NOT EXISTS %DBname%"
if not exist backup (
mkdir backup
)

REM ######################################## :main_menu
:main_menu
cls
echo.L2J Fortress Setup
echo.
echo.### Main Menu ###
echo.
echo.(1) Full install DB
echo.(2) Upgrade DB
echo.(3) Backup DB
echo.(4) Restore DB
echo.(5) Lost data in DB
echo.(q) Quit
echo.
set button=x
set /p button=What do you want ?:
if /i %button%==1 goto full_install_menu
if /i %button%==2 goto upgrade_menu
if /i %button%==3 goto backup_menu
if /i %button%==4 goto restore_menu
if /i %button%==5 goto lost_data_menu
if /i %button%==q goto end
goto main_menu

REM ######################################## :full_install_menu
:full_install_menu
cls
echo.L2J Fortress Setup
echo.
echo.### Full Install Menu ###
echo.
echo.(i) Install LA2 All DB (Warning !!! All DB will be deleted !!!)
echo.(m) Main menu
echo.(q) Quit
echo.
set button=x
set /p button=Select Chronicles ?:
if /i %button%==i goto full_install
if /i %button%==m goto main_menu
if /i %button%==q goto end
goto full_install_menu

REM ######################################## :upgrade_menu
:upgrade_menu
cls
echo.L2J Fortress Setup
echo.
echo.### Upgrade Menu ###
echo.
echo.(3) Upgrade DB for Chronicles 3
echo.(m) Main menu
echo.(q) Quit
echo.
set button=x
set /p button=Select Chronicles ?:
if /i %button%==3 goto upgrade_c3
if /i %button%==m goto main_menu
if /i %button%==q goto end
goto upgrade_menu

REM ######################################## :backup_menu
:backup_menu
cls
echo.L2J Fortress Setup
echo.
echo.### Backup Menu ###
echo.
echo.(1) Full backup
echo.(2) General tables backup only
echo.(m) Main menu
echo.(q) Quit
echo.
set button=x
set /p button=Select backup type ?:
if /i %button%==1 goto full_backup
if /i %button%==2 goto not_working_now
if /i %button%==m goto setup
if /i %button%==q goto end
goto backup_menu

REM ######################################## :restore_menu
:restore_menu
cls
echo.List all files in dir "/backup" !
echo.
dir backup /B /P
echo.
echo.L2J Fortress Setup
echo.
echo.### Restore Menu ###
echo.
echo.Enter a full filename do you want to restore to the database !
echo.(m) Main menu
echo.(q) Quit
echo.
set filename=x
set /p filename=Enter filename ?:
if /i %filename%==m goto main_menu
if /i %filename%==q goto end
if /i %filename%==%filename% goto restore_DB
goto restore_menu

REM ######################################## :lost_data_menu
:lost_data_menu
cls
echo.L2J Fortress Setup
echo.
echo.### Lost Data Menu ###
echo.
echo.(1) Show lost data
echo.(2) Delete lost data
echo.(m) Main menu
echo.(q) Quit
echo.
set button=x
set /p button=Select backup type ?:
if /i %button%==1 goto show_lost_data
if /i %button%==2 goto delete_lost_data
if /i %button%==m goto setup
if /i %button%==q goto end
goto lost_data_menu

:show_lost_data
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < fixes/lost_data_show.sql
pause
goto lost_data_menu

:delete_lost_data
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < fixes/lost_data_del.sql
echo.
echo.All lost data deleted !!!
echo.
pause
goto lost_data_menu

REM ######################################## :full_install
:full_install
set ctime=%TIME:~0,2%
if "%ctime:~0,1%" == " " (
set ctime=0%ctime:~1,1%
)
set ctime=%ctime%'%TIME:~3,2%'%TIME:~6,2%
echo.
echo Making a full backup into %DATE%-%ctime%_backup_full.sql
echo.
%mysqldumpPath% --add-drop-table -h %DBHost% -u %user% --password=%pass% %DBname% > backup/%DATE%-%ctime%_backup_full.sql
echo.
echo.Installing general tables !!!
echo.
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/accounts.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/character_friends.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/character_hennas.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/character_macroses.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/character_quests.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/character_recipebook.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/character_shortcuts.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/character_skills.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/character_skills_save.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/characters.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/character_variables.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/clan_data.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/ally_data.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/clan_wars.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/items.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/pets.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/server_variables.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/seven_signs.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/seven_signs_festival.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/siege_clans.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/killcount.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/dropcount.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/craftcount.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/banned_ips.sql
goto upgrade_c3

REM ######################################## :upgrade_c3
:upgrade_c3
echo.
echo.Upgrading tables for Chronicles 3 !!!
echo.
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/armor.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/boxaccess.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/boxes.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/castle.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/castle_door.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/castle_doorupgrade.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/castle_guards.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/castle_guards_skills.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/castle_siege_guards.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/char_templates.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/class_list.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < fortress/droplist.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/etcitem.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/henna.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/henna_trees.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < fortress/locations.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/lvlupgain.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/mapregion.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/merchant_areas_list.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/merchant_buylists.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/merchant_lease.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/merchant_shopids.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/merchants.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/minions.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < fortress/npc.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < fortress/npcskills.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/skill_spellbooks.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/skill_trees.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < fortress/spawnlist.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/teleport.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/weapon.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/zone.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/prices.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/random_spawn.sql
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < common/random_spawn_loc.sql
goto end

REM ######################################## :full_backup
:full_backup
set ctime=%TIME:~0,2%
if "%ctime:~0,1%" == " " (
set ctime=0%ctime:~1,1%
)
set ctime=%ctime%'%TIME:~3,2%'%TIME:~6,2%
echo.
echo Making a full backup into %DATE%-%ctime%_backup_full.sql
echo.
%mysqldumpPath% --add-drop-table -h %DBHost% -u %user% --password=%pass% %DBname% > backup/%DATE%-%ctime%_backup_full.sql
goto end

REM ######################################## :general_backup
:general_backup

REM ######################################## :restore_DB
:restore_DB
if not exist backup/%filename% (
echo.
echo.File not found !
echo.
pause
goto restore_menu
)
cls
echo.
echo.Restore from file %filename% !
echo.
pause
%mysqlPath% -h %DBHost% -u %user% --password=%pass% -D %DBname% < backup/%filename%
goto end

REM ######################################## :not_working_now
:not_working_now
echo.
echo Not working NOW !!!
echo.
pause
goto main_menu

REM ######################################## :end
:end
echo.
echo Setup complete.
pause
