/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.Server;
import net.sf.l2j.gameserver.handler.AdminCommandHandler;
import net.sf.l2j.gameserver.handler.ItemHandler;
import net.sf.l2j.gameserver.handler.SkillHandler;
import net.sf.l2j.gameserver.handler.UserCommandHandler;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminAdmin;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminAnnouncements;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminBan;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminChangeAccessLevel;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminCreateItem;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminDelete;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminEditChar;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminEditNpc;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminExpSp;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminFightCalculator;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminForth;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminGm;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminGmChat;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminHelpPage;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminInvul;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminKick;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminKill;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminMenu;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminMonsterRace;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminRepairChar;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminRideWyvern;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminShop;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminShutdown;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminSkill;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminSpawn;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminTarget;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminTeleport;
import net.sf.l2j.gameserver.handler.itemhandlers.BlessedSpiritShot;
import net.sf.l2j.gameserver.handler.itemhandlers.CompBlessedSpiritShotPacks;
import net.sf.l2j.gameserver.handler.itemhandlers.CompSoulShotPacks;
import net.sf.l2j.gameserver.handler.itemhandlers.CompSpiritShotPacks;
import net.sf.l2j.gameserver.handler.itemhandlers.EnchantScrolls;
import net.sf.l2j.gameserver.handler.itemhandlers.Guide;
import net.sf.l2j.gameserver.handler.itemhandlers.MysteryPotion;
import net.sf.l2j.gameserver.handler.itemhandlers.PetSummon;
import net.sf.l2j.gameserver.handler.itemhandlers.Potions;
import net.sf.l2j.gameserver.handler.itemhandlers.Recipes;
import net.sf.l2j.gameserver.handler.itemhandlers.Remedy;
import net.sf.l2j.gameserver.handler.itemhandlers.RollingDice;
import net.sf.l2j.gameserver.handler.itemhandlers.ScrollOfEscape;
import net.sf.l2j.gameserver.handler.itemhandlers.ScrollOfResurrection;
import net.sf.l2j.gameserver.handler.itemhandlers.SoulShots;
import net.sf.l2j.gameserver.handler.itemhandlers.SpiritShot;
import net.sf.l2j.gameserver.handler.itemhandlers.WorldMap;
import net.sf.l2j.gameserver.handler.skillhandlers.Charge;
import net.sf.l2j.gameserver.handler.skillhandlers.Continuous;
import net.sf.l2j.gameserver.handler.skillhandlers.Disablers;
import net.sf.l2j.gameserver.handler.skillhandlers.Drain;
import net.sf.l2j.gameserver.handler.skillhandlers.Heal;
import net.sf.l2j.gameserver.handler.skillhandlers.ManaHeal;
import net.sf.l2j.gameserver.handler.skillhandlers.Mdam;
import net.sf.l2j.gameserver.handler.skillhandlers.Pdam;
import net.sf.l2j.gameserver.handler.skillhandlers.Resurrect;
import net.sf.l2j.gameserver.handler.skillhandlers.Spoil;
import net.sf.l2j.gameserver.handler.skillhandlers.Sweep;
import net.sf.l2j.gameserver.handler.usercommandhandlers.Escape;
import net.sf.l2j.gameserver.handler.usercommandhandlers.Loc;
import net.sf.l2j.gameserver.handler.usercommandhandlers.Time;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.script.ScriptEngine;
import net.sf.l2j.gameserver.script.faenor.FaenorScriptEngine;
import net.sf.l2j.loginserver.LoginController;

/**
 * This class ...
 * 
 * @version $Revision: 1.29.2.15.2.19 $ $Date: 2005/04/05 19:41:23 $
 */
public class GameServer
{
	static Logger _log = Logger.getLogger(GameServer.class.getName());
	private final SelectorThread _selectorThread;
	private final TradeController _tradeController;
	private final SkillTable _skillTable;
	private final SkillTreeTable _skillTreeTable;
	private final SkillSpellbookTable _skillSpellbookTable;
	private final ClanTable _clanTable;
	private final ItemTable _itemTable;
	private final NpcTable _npcTable;
	private final HennaTable _hennaTable;
	private final HennaTreeTable _hennaTreeTable;
	private final TeleportLocationTable _teleTable;
	private final LevelUpData _levelUpData;
	
	private final L2World _world;
	private final CharTemplateTable _charTemplates;
	private final IdFactory _idFactory;
	
	private final LoginController _loginController;
	private final SpawnTable _spawnTable;
	private final CharNameTable _charNametable;
	private final GameTimeController _gameTimeController;
	private final Announcements _announcements;
	private final MapRegionTable _mapRegions;
	private final ItemHandler _itemHandler;
	private final SkillHandler _skillHandler;
	private final GmListTable _gmList;
	private final AdminCommandHandler _adminCommandHandler;
	private final Shutdown _shutdownHandler;
	private final RecipeController _recipeController;
    private final UserCommandHandler _userCommandHandler;
    private final EventDroplist _eventDroplist;
	private final DoorTable _doorTable;
    
    private final ScriptEngine _scriptEngine;
	
    private static boolean autoloot = false;
    private MonsterRace _monsterRace;
	
	public long getUsedMemoryMB()
	{
		return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1024/1024;		
	}

    public static boolean autoLootEnabled()
    {
        return autoloot;
    }
    
    public LoginController getLoginController()
    {
    	return _loginController;
    }

    public SelectorThread getSelectorThread()
    {
    	return _selectorThread;
    }

	public GameServer() throws Exception
	{
		Server.gameServer = this;
		_log.finest("used mem:" + getUsedMemoryMB()+"MB" );
		
		Properties serverSettings = new Properties();
		InputStream is = getClass().getResourceAsStream("/server.cfg");  
		serverSettings.load(is);
		is.close();

        autoloot = "true".equalsIgnoreCase(serverSettings.getProperty("AutoLoot"));
		String hostname = serverSettings.getProperty("GameserverHostname");
		int port = Integer.parseInt(serverSettings.getProperty("GameserverPort", "7777"));
		
		ClientScheduler.getInstance();
		_selectorThread = new SelectorThread(hostname, port);
		_selectorThread.start();

		String maxPlayerStr = serverSettings.getProperty("MaximumOnlineUsers");
		int maxPlayers = Integer.parseInt(maxPlayerStr);
		_log.config("Maximum Numbers of Connected Players: "+ maxPlayers);

		
		new File(Config.DATAPACK_ROOT, "data/clans").mkdirs();
		new File(Config.DATAPACK_ROOT, "data/crests").mkdirs();

		// start game time control early
		_gameTimeController = GameTimeController.getInstance();
		
		// keep the references of Singletons to prevent garbage collection
		_loginController = LoginController.getInstance();
		_loginController.setMaxAllowedOnlinePlayers(maxPlayers);
		_charNametable = CharNameTable.getInstance();

		_idFactory = IdFactory.getInstance();
		if (!_idFactory.isInitialized())
		{
		    _log.severe("Could not read object IDs from DB. Please run convertData.");
		    throw new Exception("Could not initialize the ID factory");
		}
		
		_itemTable = ItemTable.getInstance();
		if (!_itemTable.isInitialized())
		{
		    _log.severe("Could not find the extraced files. Please run convertData.");
		    throw new Exception("Could not initialize the item table");
		}
		
		_tradeController = TradeController.getInstance();
		_skillTable = SkillTable.getInstance();
		if (!_skillTable.isInitialized())
		{
		    _log.severe("Could not find the extraced files. Please run convertData.");
		    throw new Exception("Could not initialize the skill table");
		}
		
		_recipeController = RecipeController.getInstance();
		
		_skillTreeTable = SkillTreeTable.getInstance();
		_skillSpellbookTable = SkillSpellbookTable.getInstance();
		_charTemplates = CharTemplateTable.getInstance();
		_clanTable = ClanTable.getInstance();

		_npcTable = NpcTable.getInstance();
		if (!_npcTable.isInitialized())
		{
		    _log.severe("Could not find the extraced files. Please run convertData.");
		    throw new Exception("Could not initialize the npc table");
		}
		_hennaTable = HennaTable.getInstance();
		if (!_hennaTable.isInitialized())
		{
		   throw new Exception("Could not initialize the henna table");
		}
		_hennaTreeTable = HennaTreeTable.getInstance();
		if (!_hennaTable.isInitialized())
		{
		   throw new Exception("Could not initialize the henna tree table");
		}
		
		_teleTable = TeleportLocationTable.getInstance();
		_levelUpData = LevelUpData.getInstance();
		_world = L2World.getInstance();	
	//	_world.initWorldContent();
		_spawnTable = SpawnTable.getInstance();
		_announcements = Announcements.getInstance();
		_mapRegions = MapRegionTable.getInstance();
		_eventDroplist = EventDroplist.getInstance();
        _monsterRace = MonsterRace.getInstance();
        //_monsterRace.newRace();
		_doorTable = DoorTable.getInstance();
		
		_itemHandler = ItemHandler.getInstance();
		_itemHandler.registerItemHandler(new PetSummon());
		_itemHandler.registerItemHandler(new ScrollOfEscape());
		_itemHandler.registerItemHandler(new ScrollOfResurrection());
		_itemHandler.registerItemHandler(new SoulShots());
		_itemHandler.registerItemHandler(new SpiritShot());
		_itemHandler.registerItemHandler(new BlessedSpiritShot());
		_itemHandler.registerItemHandler(new WorldMap());
		_itemHandler.registerItemHandler(new Potions());
		_itemHandler.registerItemHandler(new Recipes());
        _itemHandler.registerItemHandler(new CompSoulShotPacks());
        _itemHandler.registerItemHandler(new CompSpiritShotPacks());
        _itemHandler.registerItemHandler(new CompBlessedSpiritShotPacks());
        _itemHandler.registerItemHandler(new RollingDice());
        _itemHandler.registerItemHandler(new MysteryPotion());
		_itemHandler.registerItemHandler(new EnchantScrolls());
		_itemHandler.registerItemHandler(new Remedy());
		_itemHandler.registerItemHandler(new Guide());
		
		_skillHandler = SkillHandler.getInstance();
		_skillHandler.registerSkillHandler(new Pdam());
		_skillHandler.registerSkillHandler(new Mdam());
		_skillHandler.registerSkillHandler(new Heal());
		_skillHandler.registerSkillHandler(new ManaHeal());
		_skillHandler.registerSkillHandler(new Charge());
		_skillHandler.registerSkillHandler(new Continuous());
		_skillHandler.registerSkillHandler(new Drain());
		_skillHandler.registerSkillHandler(new Resurrect());
        _skillHandler.registerSkillHandler(new Spoil());
        _skillHandler.registerSkillHandler(new Sweep());
        _skillHandler.registerSkillHandler(new Disablers());
				
		
		_adminCommandHandler = AdminCommandHandler.getInstance();
		_adminCommandHandler.registerAdminCommandHandler(new AdminAdmin());
		_adminCommandHandler.registerAdminCommandHandler(new AdminInvul());
		_adminCommandHandler.registerAdminCommandHandler(new AdminDelete());
		_adminCommandHandler.registerAdminCommandHandler(new AdminKill());
		_adminCommandHandler.registerAdminCommandHandler(new AdminTarget());
		_adminCommandHandler.registerAdminCommandHandler(new AdminShop());
		_adminCommandHandler.registerAdminCommandHandler(new AdminAnnouncements());
		_adminCommandHandler.registerAdminCommandHandler(new AdminCreateItem());
		_adminCommandHandler.registerAdminCommandHandler(new AdminHelpPage());
		_adminCommandHandler.registerAdminCommandHandler(new AdminShutdown());
		_adminCommandHandler.registerAdminCommandHandler(new AdminSpawn());
		_adminCommandHandler.registerAdminCommandHandler(new AdminSkill());
		_adminCommandHandler.registerAdminCommandHandler(new AdminExpSp());
		_adminCommandHandler.registerAdminCommandHandler(new AdminGmChat());
		_adminCommandHandler.registerAdminCommandHandler(new AdminEditChar());
		_adminCommandHandler.registerAdminCommandHandler(new AdminGm());
		_adminCommandHandler.registerAdminCommandHandler(new AdminTeleport());
		_adminCommandHandler.registerAdminCommandHandler(new AdminRepairChar());
        _adminCommandHandler.registerAdminCommandHandler(new AdminChangeAccessLevel());
        _adminCommandHandler.registerAdminCommandHandler(new AdminRideWyvern());
        _adminCommandHandler.registerAdminCommandHandler(new AdminBan());
        _adminCommandHandler.registerAdminCommandHandler(new AdminKick());
        _adminCommandHandler.registerAdminCommandHandler(new AdminMonsterRace());
        _adminCommandHandler.registerAdminCommandHandler(new AdminEditNpc());
        _adminCommandHandler.registerAdminCommandHandler(new AdminFightCalculator());
        _adminCommandHandler.registerAdminCommandHandler(new AdminForth());
        _adminCommandHandler.registerAdminCommandHandler(new AdminMenu());
        
        _userCommandHandler = UserCommandHandler.getInstance();
        _userCommandHandler.registerUserCommandHandler(new Loc());
        _userCommandHandler.registerUserCommandHandler(new Escape());
		_userCommandHandler.registerUserCommandHandler(new Time());
        
		_gmList = GmListTable.getInstance();
		
		_scriptEngine = new FaenorScriptEngine();
		
		Quest.init();
		
		_shutdownHandler = Shutdown.getInstance();
		Runtime.getRuntime().addShutdownHook(_shutdownHandler);

		System.gc();
		long freeMem = Runtime.getRuntime().freeMemory() / 1024 / 1024;
		long totalMem = Runtime.getRuntime().maxMemory() / 1024 / 1024;
		_log.info("Started, free memory "+freeMem+" Mb of "+totalMem+" Mb");
	}
}
