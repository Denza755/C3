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

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.Server;
import net.sf.l2j.gameserver.cache.CrestCache;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.handler.*;
import net.sf.l2j.gameserver.handler.admincommandhandlers.*;
import net.sf.l2j.gameserver.handler.itemhandlers.*;
import net.sf.l2j.gameserver.handler.skillhandlers.*;
import net.sf.l2j.gameserver.handler.usercommandhandlers.Escape;
import net.sf.l2j.gameserver.handler.usercommandhandlers.Loc;
import net.sf.l2j.gameserver.handler.usercommandhandlers.PartyInfo;
import net.sf.l2j.gameserver.handler.usercommandhandlers.Time;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.stats;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.instancemanager.*;
import net.sf.l2j.gameserver.model.AutoChatHandler;
import net.sf.l2j.gameserver.model.AutoSpawnHandler;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.entity.MonsterRace;
import net.sf.l2j.gameserver.model.entity.SevenSigns;
import net.sf.l2j.gameserver.model.entity.SevenSignsFestival;
import net.sf.l2j.gameserver.script.faenor.FaenorScriptEngine;
import net.sf.l2j.gameserver.tables.*;
import net.sf.l2j.gameserver.taskmanager.TaskManager;
import net.sf.l2j.status.Status;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * This class ...
 *
 * @version $Revision: 1.29.2.15.2.19 $ $Date: 2005/04/05 19:41:23 $
 */
public class GameServer
{
	private static final Logger _log = Logger.getLogger(GameServer.class.getName());
	private final SelectorThread _selectorThread;
    public static GameServer gameServer;

    private static DoorTable _doorTable;
    public static Status statusServer;

    public static final Calendar DateTimeServerStarted = Calendar.getInstance();
    
    public long getUsedMemoryMB()
	{
		return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1024/1024;
	}

    public SelectorThread getSelectorThread()
    {
    	return _selectorThread;
    }


	public static DoorTable getDoorsTable() { return _doorTable; }	
	public GameServer() throws Exception
	{
        gameServer = this;
		_log.finest("used mem:" + getUsedMemoryMB()+"MB" );

        if (Config.SERVER_VERSION != null)
        {
            _log.info("Ultimate Devs Server Version:    "+Config.SERVER_VERSION);
            _log.info("Ultimate Devs Server Build Date: "+Config.SERVER_BUILD_DATE);
        }

		//Rnd.getInstance();
        IdFactory _idFactory = IdFactory.getInstance();
        if (!_idFactory.isInitialized())
        {
            _log.severe("Could not read object IDs from DB. Please Check Your Data.");
            throw new Exception("Could not initialize the ID factory");
        }

        ClientScheduler.getInstance();

		new File(Config.DATAPACK_ROOT, "data/clans").mkdirs();
		new File(Config.DATAPACK_ROOT, "data/crests").mkdirs();
        new File("pathnode").mkdirs();

		// start game time control early
		GameTimeController.getInstance();

		// keep the references of Singletons to prevent garbage collection
		CharNameTable.getInstance();

        ItemTable _itemTable = ItemTable.getInstance();
		if (!_itemTable.isInitialized())
		{
		    _log.severe("Could not find the extraced files. Please Check Your Data.");
		    throw new Exception("Could not initialize the item table");
		}

		TradeController.getInstance();
        SkillTable _skillTable = SkillTable.getInstance();
		if (!_skillTable.isInitialized())
		{
		    _log.severe("Could not find the extraced files. Please Check Your Data.");
		    throw new Exception("Could not initialize the skill table");
		}

		RecipeController.getInstance();

		SkillTreeTable.getInstance();
		SkillSpellbookTable.getInstance();
		CharTemplateTable.getInstance();
        
        //Call to load caches
        HtmCache.getInstance();
        CrestCache.getInstance();

        NpcTable _npcTable = NpcTable.getInstance();
		if (!_npcTable.isInitialized())
		{
		    _log.severe("Could not find the extraced files. Please Check Your Data.");
		    throw new Exception("Could not initialize the npc table");
		}
        HennaTable _hennaTable = HennaTable.getInstance();
		if (!_hennaTable.isInitialized())
		{
		   throw new Exception("Could not initialize the Henna Table");
		}
		HennaTreeTable.getInstance();
		if (!_hennaTable.isInitialized())
		{
		   throw new Exception("Could not initialize the Henna Tree Table");
		}

		TeleportLocationTable.getInstance();
		LevelUpData.getInstance();
		L2World.getInstance();
        ZoneManager.getInstance();
		SpawnTable.getInstance();
		Announcements.getInstance();
		MapRegionTable.getInstance();
		EventDroplist.getInstance();
        
        if (Config.AUTODESTROY_ITEM_AFTER > 0)
	{
    	    ItemsAutoSetPriority.getInstance();
    	    ItemsAutoDestroy.getInstance();
	}
        
        MonsterRace.getInstance();
        
		_doorTable = DoorTable.getInstance();
        StaticObjects.getInstance();

        SevenSigns _sevenSignsEngine = SevenSigns.getInstance();
        SevenSignsFestival.getInstance();
        AutoSpawnHandler _autoSpawnHandler = AutoSpawnHandler.getInstance();
        AutoChatHandler _autoChatHandler = AutoChatHandler.getInstance();

        // Spawn the Orators/Preachers if in the Seal Validation period.
        if (_sevenSignsEngine.isSealValidationPeriod())
            _sevenSignsEngine.spawnSevenSignsNPCs();

		_log.config("AutoChatHandler: Loaded " + _autoChatHandler.size() + " handlers in total.");
		_log.config("AutoSpawnHandler: Loaded " + _autoSpawnHandler.size() + " handlers in total.");

        ItemHandler _itemHandler = ItemHandler.getInstance();
		_itemHandler.registerItemHandler(new ChristmasTree());
		_itemHandler.registerItemHandler(new PetSummon());
		_itemHandler.registerItemHandler(new ScrollOfEscape());
		_itemHandler.registerItemHandler(new ScrollOfResurrection());
		_itemHandler.registerItemHandler(new SoulShots());
		_itemHandler.registerItemHandler(new SpiritShot());
		_itemHandler.registerItemHandler(new BlessedSpiritShot());
		_itemHandler.registerItemHandler(new WorldMap());
		_itemHandler.registerItemHandler(new Potions());
		_itemHandler.registerItemHandler(new Recipes());
        _itemHandler.registerItemHandler(new CompShotPacks());
        _itemHandler.registerItemHandler(new CompSpiritShotPacks());
        _itemHandler.registerItemHandler(new CompBlessedSpiritShotPacks());
        _itemHandler.registerItemHandler(new RollingDice());
        _itemHandler.registerItemHandler(new MysteryPotion());
		_itemHandler.registerItemHandler(new EnchantScrolls());
		_itemHandler.registerItemHandler(new Remedy());
		_itemHandler.registerItemHandler(new Guide());
		_itemHandler.registerItemHandler(new Scrolls());
		_itemHandler.registerItemHandler(new CrystalCarol());
		_itemHandler.registerItemHandler(new SoulCrystals());
		_itemHandler.registerItemHandler(new SevenSignsRecord());
        _itemHandler.registerItemHandler(new CharChangePotions());
	_itemHandler.registerItemHandler(new EnergyStone());
        _itemHandler.registerItemHandler(new Firework());
        _itemHandler.registerItemHandler(new Funny());
        _itemHandler.registerItemHandler(new AdvQuestItems());
        _itemHandler.registerItemHandler(new Seed());
        _itemHandler.registerItemHandler(new Harvester());
        _itemHandler.registerItemHandler(new MercTicket());
        _log.config("ItemHandler: Loaded " + _itemHandler.size() + " handlers.");

        SkillHandler _skillHandler = SkillHandler.getInstance();
		_skillHandler.registerSkillHandler(new Pdam());
		_skillHandler.registerSkillHandler(new Mdam());
		_skillHandler.registerSkillHandler(new Heal());
        _skillHandler.registerSkillHandler(new CombatPointHeal());
		_skillHandler.registerSkillHandler(new ManaHeal());
		_skillHandler.registerSkillHandler(new Charge());
		_skillHandler.registerSkillHandler(new Continuous());
		_skillHandler.registerSkillHandler(new Resurrect());
        _skillHandler.registerSkillHandler(new Spoil());
        _skillHandler.registerSkillHandler(new Sweep());
        _skillHandler.registerSkillHandler(new Disablers());
		_skillHandler.registerSkillHandler(new Recall());
        _skillHandler.registerSkillHandler(new SiegeFlag());
        _skillHandler.registerSkillHandler(new TakeCastle());
        _skillHandler.registerSkillHandler(new Unlock());
        _skillHandler.registerSkillHandler(new DrainSoul());
        _skillHandler.registerSkillHandler(new Craft()); 
        _log.config("SkillHandler: Loaded " + _skillHandler.size() + " handlers.");

        AdminCommandHandler _adminCommandHandler = AdminCommandHandler.getInstance();
		_adminCommandHandler.registerAdminCommandHandler(new AdminAdmin());
		_adminCommandHandler.registerAdminCommandHandler(new AdminInvul());
		_adminCommandHandler.registerAdminCommandHandler(new AdminDelete());
		_adminCommandHandler.registerAdminCommandHandler(new AdminKill());
		_adminCommandHandler.registerAdminCommandHandler(new AdminTarget());
		_adminCommandHandler.registerAdminCommandHandler(new AdminShop());
		_adminCommandHandler.registerAdminCommandHandler(new AdminAnnouncements());
		_adminCommandHandler.registerAdminCommandHandler(new AdminCreateItem());
        _adminCommandHandler.registerAdminCommandHandler(new AdminHeal());
		_adminCommandHandler.registerAdminCommandHandler(new AdminHelpPage());
		_adminCommandHandler.registerAdminCommandHandler(new AdminShutdown());
		_adminCommandHandler.registerAdminCommandHandler(new AdminSpawn());
		_adminCommandHandler.registerAdminCommandHandler(new AdminSkill());
		_adminCommandHandler.registerAdminCommandHandler(new AdminExpSp());
        _adminCommandHandler.registerAdminCommandHandler(new AdminEventEngine());
		_adminCommandHandler.registerAdminCommandHandler(new AdminEncArmor());
		_adminCommandHandler.registerAdminCommandHandler(new AdminGmChat());
		_adminCommandHandler.registerAdminCommandHandler(new AdminEditChar());
		_adminCommandHandler.registerAdminCommandHandler(new AdminGm());
		_adminCommandHandler.registerAdminCommandHandler(new AdminTeleport());
		_adminCommandHandler.registerAdminCommandHandler(new AdminRepairChar());
        _adminCommandHandler.registerAdminCommandHandler(new AdminChangeAccessLevel());
        _adminCommandHandler.registerAdminCommandHandler(new AdminBan());
        _adminCommandHandler.registerAdminCommandHandler(new AdminPolymorph());
		_adminCommandHandler.registerAdminCommandHandler(new AdminBanChat());
        _adminCommandHandler.registerAdminCommandHandler(new AdminKick());
        _adminCommandHandler.registerAdminCommandHandler(new AdminMonsterRace());
        _adminCommandHandler.registerAdminCommandHandler(new AdminEditNpc());
        _adminCommandHandler.registerAdminCommandHandler(new AdminFightCalculator());
	_adminCommandHandler.registerAdminCommandHandler(new AdminForth());
        _adminCommandHandler.registerAdminCommandHandler(new AdminMenu());
	_adminCommandHandler.registerAdminCommandHandler(new AdminSendHome());
        _adminCommandHandler.registerAdminCommandHandler(new AdminSiege());
        _adminCommandHandler.registerAdminCommandHandler(new AdminPathNode());
        _adminCommandHandler.registerAdminCommandHandler(new AdminPForge());
        _adminCommandHandler.registerAdminCommandHandler(new AdminEffects());
        _adminCommandHandler.registerAdminCommandHandler(new AdminDoorControl());
        _adminCommandHandler.registerAdminCommandHandler(new AdminTalk());
        _adminCommandHandler.registerAdminCommandHandler(new AdminTest());
        _adminCommandHandler.registerAdminCommandHandler(new AdminMobGroup());
        _adminCommandHandler.registerAdminCommandHandler(new AdminRes());
        _adminCommandHandler.registerAdminCommandHandler(new AdminMammon());
        _adminCommandHandler.registerAdminCommandHandler(new AdminUnblockIp());
        _adminCommandHandler.registerAdminCommandHandler(new AdminPledge());
        _adminCommandHandler.registerAdminCommandHandler(new AdminRideWyvern());
        _adminCommandHandler.registerAdminCommandHandler(new AdminLogin());
        _adminCommandHandler.registerAdminCommandHandler(new AdminCache());
        _adminCommandHandler.registerAdminCommandHandler(new AdminLevel());
        _adminCommandHandler.registerAdminCommandHandler(new AdminQuest());
        _adminCommandHandler.registerAdminCommandHandler(new AdminZone());

        //_adminCommandHandler.registerAdminCommandHandler(new AdminRadar());
        _log.config("AdminCommandHandler: Loaded " + _adminCommandHandler.size() + " handlers.");

        UserCommandHandler _userCommandHandler = UserCommandHandler.getInstance();
        _userCommandHandler.registerUserCommandHandler(new Loc());
        _userCommandHandler.registerUserCommandHandler(new Escape());
		_userCommandHandler.registerUserCommandHandler(new Time());
        _userCommandHandler.registerUserCommandHandler(new PartyInfo());        
        _log.config("UserCommandHandler: Loaded " + _userCommandHandler.size() + " handlers.");

        VoicedCommandHandler _voicedCommandHandler = VoicedCommandHandler.getInstance();
		_voicedCommandHandler.registerVoicedCommandHandler(new stats());
		
        _log.config("VoicedCommandHandler: Loaded " + _voicedCommandHandler.size() + " handlers.");

        TaskManager.getInstance();
        
		GmListTable.getInstance();

        FaenorScriptEngine.getInstance();

        Universe.getInstance();

		QuestManager.getInstance();

        CastleManager.getInstance();
        
        MercTicketManager.getInstance();
        
        BoatManager.getInstance();

        Shutdown _shutdownHandler = Shutdown.getInstance();
		Runtime.getRuntime().addShutdownHook(_shutdownHandler);

		try
        {
            _doorTable.getDoor(24190001).openMe();
            _doorTable.getDoor(24190002).openMe();
            _doorTable.getDoor(24190003).openMe();
            _doorTable.getDoor(24190004).openMe();
            _doorTable.getDoor(23180001).openMe();
            _doorTable.getDoor(23180002).openMe();
            _doorTable.getDoor(23180003).openMe();
            _doorTable.getDoor(23180004).openMe();
            _doorTable.getDoor(23180005).openMe();
            _doorTable.getDoor(23180006).openMe();
            
            _doorTable.checkAutoOpen();
        } 
        catch (NullPointerException e)
        {
            _log.warning("Door.csv does not contain the right door info. Update door.csv");
            e.printStackTrace();
        }
        
        _log.config("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());

		System.gc();
		// maxMemory is the upper limit the jvm can use, totalMemory the size of the current allocation pool, freeMemory the unused memory in the allocation pool
		long freeMem = (Runtime.getRuntime().maxMemory()-Runtime.getRuntime().totalMemory()+Runtime.getRuntime().freeMemory()) / 1024 / 1024;
		long totalMem = Runtime.getRuntime().maxMemory() / 1024 / 1024;
		_log.info("GameServer Started, free memory "+freeMem+" Mb of "+totalMem+" Mb");

        LoginServerThread _loginThread = LoginServerThread.getInstance();
		_loginThread.setMaxPlayer(Config.MAXIMUM_ONLINE_USERS);
		_loginThread.start();
		
		_selectorThread = new SelectorThread(Config.GAMESERVER_HOSTNAME, Config.PORT_GAME);
		_selectorThread.start();
		_log.config("Maximum Numbers of Connected Players: " + Config.MAXIMUM_ONLINE_USERS);
	}
	
	public static void main(String[] args) throws Exception
    {
	    System.out.println(".:===========================================:.");
	    System.out.println(".:       Thank you for using our software!   :.");
	    System.out.println(".:              ReMade bY Cr3a70r            :.");
	    System.out.println(".:            Ultimate Game Server           :.");
	    System.out.println(".:===========================================:.");
		Server.SERVER_MODE = Server.MODE_GAMESERVER;
//      Local Constants
		final String LOG_FOLDER = "log"; // Name of folder for log file
		final String LOG_NAME   = "./log.cfg"; // Name of log file
		
		/*** Main ***/
		// Create log folder
		File logFolder = new File(Config.DATAPACK_ROOT, LOG_FOLDER); 
		logFolder.mkdir();
		
		// Create input stream for log file -- or store file data into memory
		InputStream is =  new FileInputStream(new File(LOG_NAME));  
		LogManager.getLogManager().readConfiguration(is);
		is.close();
		
		// Initialize config 
		Config.load();
		L2DatabaseFactory.getInstance();
		gameServer = new GameServer();
		
		if ( Config.IS_TELNET_ENABLED ) {
		    statusServer = new Status(Server.SERVER_MODE);
		    statusServer.start();
		}
		else {
		    System.out.println("Telnet server is currently disabled.");
		}
    }
}
