@echo off

REM ############################################
REM ## You can change here your own DB params ##
REM ############################################
REM MYSQL BIN PATH
set mysqlBinPath=C:\server\diskw\usr\local\mysql\bin

REM LOGINSERVER
set lsuser=root
set lspass=root
set lsdb=l2jdb
set lshost=localhost

REM GAMESERVER
set gsuser=root
set gspass=root
set gsdb=l2jdb
set gshost=localhost
REM ############################################

set mysqldumpPath="%mysqlBinPath%\mysqldump"
set mysqlPath="%mysqlBinPath%\mysql"

echo.
echo Making a backup of the original loginserver database.
%mysqldumpPath% --add-drop-table -h %lshost% -u %lsuser% --password=%lspass% %lsdb% > loginserver_backup.sql
echo.
echo WARNING: A full install (f) will destroy data in your
echo          `accounts` and `gameserver` tables.
echo          Choose upgrade (u) if you already have an `accounts` table but no
echo          `gameserver` table (ie. your server is a pre LS/GS split version.)
echo          Choose skip (s) to skip loginserver DB installation and go to
echo          gameserver DB installation/upgrade.
echo.
echo.
:asklogin
set loginprompt=x
set /p loginprompt=LOGINSERVER DB install type: (f) full or (u) upgrade or (s) skip or (q) quit? 
if /i %loginprompt%==f goto logininstall
if /i %loginprompt%==u goto loginupgrade
if /i %loginprompt%==s goto gsbackup
if /i %loginprompt%==q goto end
goto asklogin

:logininstall
echo Deleting loginserver tables for new content.
%mysqlPath% -h %lshost% -u %lsuser% --password=%lspass% -D %lsdb% < login_install.sql

:loginupgrade
echo Installling new loginserver content.
%mysqlPath% -h %lshost% -u %lsuser% --password=%lspass% -D %lsdb% < ../sql/accounts.sql
%mysqlPath% -h %lshost% -u %lsuser% --password=%lspass% -D %lsdb% < ../sql/gameservers.sql

:gsbackup
echo.
echo Making a backup of the original gameserver database.
%mysqldumpPath% --add-drop-table -h %gshost% -u %gsuser% --password=%gspass% %gsdb% > gameserver_backup.sql

echo.
echo.
echo WARNING: A full install (f) will destroy all existing character data.
:asktype
set installtype=x
set /p installtype=GAMESERVER DB install type: (f) full install or (u) upgrade or (s) skip or (q) quit? 
if /i %installtype%==f goto fullinstall
if /i %installtype%==u goto upgradeinstall
if /i %installtype%==s goto experimental
if /i %installtype%==q goto end
goto asktype

:fullinstall
echo Deleting all gameserver tables for new content.
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < full_install.sql

:upgradeinstall
echo Installling new gameserver content.
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/armor.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/boxaccess.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/boxes.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle_door.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle_doorupgrade.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle_siege_guards.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/char_templates.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_friends.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_hennas.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_macroses.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_quests.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_recipebook.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_shortcuts.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_skills.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_skills_save.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_subclasses.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/characters.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_data.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_wars.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/class_list.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/droplist.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/etcitem.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/global_tasks.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/henna.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/henna_trees.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/items.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/locations.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/lvlupgain.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/mapregion.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/merchant_areas_list.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/merchant_buylists.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/merchant_lease.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/merchant_shopids.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/merchants.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/minions.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/npc.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/npcskills.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/pets.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/random_spawn.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/random_spawn_loc.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/record.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/seven_signs.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/seven_signs_festival.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/siege_clans.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/skill_learn.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/skill_spellbooks.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/skill_trees.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/spawnlist.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/teleport.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/weapon.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/zone.sql

:experimental
echo.
echo.
echo WARNING: Installing data_fix (y) may change game balance.
echo          Some fix will make game better but I'm not tested all things.
:askexp
set expprompt=x
set /p expprompt=Install data_fix: (y) yes or (n) no or (q) quit? 
if /i %expprompt%==y goto expinstall
if /i %expprompt%==n goto lost_data_menu
if /i %expprompt%==q goto end
goto askexp

:expinstall
echo Making a backup of the default gameserver tables.
%mysqldumpPath% --add-drop-table -h %gshost% -u %gsuser% --password=%gspass% %gsdb% > experimental_backup.sql
echo Installing new content.

ECHO FIX: Will lower chance to drop high grade stuff on lower level mobs (L2J Fortress).
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < fix\fix_auto_drop.sql

ECHO FIX: Automatic respawn time base on HP stat (L2J Fortress).
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < fix\fix_auto_respawn.sql

ECHO FIX: Automatic set price and crystals of Enhanced weapons (Advi)
ECHO *** Only enable this fix if you don't use built in GMShop ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < fix\fix_auto_enhance_weapon.sql > nul

ECHO FIX: Summoned Pets Stats (HellFire/Drake)
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < fix\fix_npc_summon.sql

REM ECHO FIX: More Robust Droplist (l2j datapack team, edinho_sp, cfx69)
REM %mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < fix\c_droplist.sql

ECHo FIX: Raidboss (l2wh.com)
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < fix\fix_npc_raidboss.sql

ECHO FIX: Necropolis of Sacrifice Lilim and Nephilim droplist (titan86vn)
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < fix\fix_sevensign_droplist.sql

REM Echo Fix: Fix catacombs/necropoli - cfx69 (based on mastermind007 and Pivi Ct's work all in one package)
REM %mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < fix\c_allcata.sql

REM ECHO FIX: Some Missing Mob Weapons -cfx69 (based on contribs from l2j datapack forums
REM %mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < fix\c_mobweap.sql

REM ECHO FIX: Add soulshots & spiritshots to town merchants (mastermind007)
REM ** This is custom so if you want it, remove REM before the line ***
REM %mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < fix\fix_shop_soulshots.sql

ECHO FIX: Get 1 crystal when enchant fail S items (titan86vn)
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < fix\fix_crystal.sql

ECHO FIX: Some C4 NPC Stats/Skills/Droplist (edinho_sp)
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < fix\fix_c4stats.sql

ECHO FIX: Custom C4 Spawnlist (vitacite)
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < fix\fix_c4allinone.sql

REM ## Lost data controller, the data is not keep by any availble data will make trash into your server, so if it can be del, your server may be not crash any more - L2jFortress ##
REM ## Not completed - only use with one game server and one login server in one PC ##
:lost_data_menu  
cls  
echo ### Lost Data Menu ###  
echo.
echo (s) Show lost data  
echo (d) Delete lost data  
echo (q) Quit  
set button=x  
set /p button=Select backup type ?:  
if /i %button%==s goto show_lost_data  
if /i %button%==d goto delete_lost_data  
if /i %button%==q goto end  
goto lost_data_menu  
  
:show_lost_data  
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < fix/show_lost_data.sql 
goto end

:delete_lost_data  
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < fix/fix_cleanupdata.sql  
echo.
echo All lost data deleted !!!  
echo.
goto end

:end
echo.       
echo Script complete.
pause
