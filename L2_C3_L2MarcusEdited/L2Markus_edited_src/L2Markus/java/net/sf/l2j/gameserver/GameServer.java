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
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.Server;
import net.sf.l2j.gameserver.cache.CrestCache;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.communitybbs.Manager.ForumsBBSManager;
import net.sf.l2j.gameserver.datatables.*;
import net.sf.l2j.gameserver.handler.*;
import net.sf.l2j.gameserver.handler.admincommandhandlers.*;
import net.sf.l2j.gameserver.handler.itemhandlers.AdvQuestItems;
import net.sf.l2j.gameserver.handler.itemhandlers.BeastSoulShot;
import net.sf.l2j.gameserver.handler.itemhandlers.BeastSpiritShot;
import net.sf.l2j.gameserver.handler.itemhandlers.BlessedSpiritShot;
import net.sf.l2j.gameserver.handler.itemhandlers.CharChangePotions;
import net.sf.l2j.gameserver.handler.itemhandlers.ChristmasTree;
import net.sf.l2j.gameserver.handler.itemhandlers.CompBlessedSpiritShotPacks;
import net.sf.l2j.gameserver.handler.itemhandlers.CompShotPacks;
import net.sf.l2j.gameserver.handler.itemhandlers.CompSpiritShotPacks;
import net.sf.l2j.gameserver.handler.itemhandlers.CrystalCarol;
import net.sf.l2j.gameserver.handler.itemhandlers.EnchantScrolls;
import net.sf.l2j.gameserver.handler.itemhandlers.Firework;
import net.sf.l2j.gameserver.handler.itemhandlers.Guide;
import net.sf.l2j.gameserver.handler.itemhandlers.Harvester;
import net.sf.l2j.gameserver.handler.itemhandlers.MercTicket;
import net.sf.l2j.gameserver.handler.itemhandlers.MysteryPotion;
import net.sf.l2j.gameserver.handler.itemhandlers.PetSummon;
import net.sf.l2j.gameserver.handler.itemhandlers.Potions;
import net.sf.l2j.gameserver.handler.itemhandlers.Recipes;
import net.sf.l2j.gameserver.handler.itemhandlers.Remedy;
import net.sf.l2j.gameserver.handler.itemhandlers.RollingDice;
import net.sf.l2j.gameserver.handler.itemhandlers.ScrollOfEscape;
import net.sf.l2j.gameserver.handler.itemhandlers.ScrollOfResurrection;
import net.sf.l2j.gameserver.handler.itemhandlers.Scrolls;
import net.sf.l2j.gameserver.handler.itemhandlers.Seed;
import net.sf.l2j.gameserver.handler.itemhandlers.SevenSignsRecord;
import net.sf.l2j.gameserver.handler.itemhandlers.SoulCrystals;
import net.sf.l2j.gameserver.handler.itemhandlers.SoulShots;
import net.sf.l2j.gameserver.handler.itemhandlers.SpiritShot;
import net.sf.l2j.gameserver.handler.itemhandlers.WorldMap;
import net.sf.l2j.gameserver.handler.skillhandlers.Charge;
import net.sf.l2j.gameserver.handler.skillhandlers.CombatPointHeal;
import net.sf.l2j.gameserver.handler.skillhandlers.Continuous;
import net.sf.l2j.gameserver.handler.skillhandlers.Craft;
import net.sf.l2j.gameserver.handler.skillhandlers.Disablers;
import net.sf.l2j.gameserver.handler.skillhandlers.DrainSoul;
import net.sf.l2j.gameserver.handler.skillhandlers.Heal;
import net.sf.l2j.gameserver.handler.skillhandlers.ManaHeal;
import net.sf.l2j.gameserver.handler.skillhandlers.Mdam;
import net.sf.l2j.gameserver.handler.skillhandlers.Pdam;
import net.sf.l2j.gameserver.handler.skillhandlers.Recall;
import net.sf.l2j.gameserver.handler.skillhandlers.Resurrect;
import net.sf.l2j.gameserver.handler.skillhandlers.SiegeFlag;
import net.sf.l2j.gameserver.handler.skillhandlers.Spoil;
import net.sf.l2j.gameserver.handler.skillhandlers.Sweep;
import net.sf.l2j.gameserver.handler.skillhandlers.TakeCastle;
import net.sf.l2j.gameserver.handler.skillhandlers.Unlock;
import net.sf.l2j.gameserver.handler.usercommandhandlers.ClanPenalty;
import net.sf.l2j.gameserver.handler.usercommandhandlers.ClanWarsList;
import net.sf.l2j.gameserver.handler.usercommandhandlers.DisMount;
import net.sf.l2j.gameserver.handler.usercommandhandlers.Escape;
import net.sf.l2j.gameserver.handler.usercommandhandlers.Loc;
import net.sf.l2j.gameserver.handler.usercommandhandlers.Mount;
import net.sf.l2j.gameserver.handler.usercommandhandlers.PartyInfo;
import net.sf.l2j.gameserver.handler.usercommandhandlers.Time;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.stats;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.instancemanager.*;
import net.sf.l2j.gameserver.model.AutoChatHandler;
import net.sf.l2j.gameserver.model.AutoSpawnHandler;
import net.sf.l2j.gameserver.model.L2PetDataTable;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.quest.engine.CompiledScriptCache;
import net.sf.l2j.gameserver.model.quest.engine.L2ScriptEngineManager;
import net.sf.l2j.gameserver.model.entity.DevastatedCastle;
import net.sf.l2j.gameserver.model.entity.BanditStrongholdSiege;
import net.sf.l2j.gameserver.taskmanager.TaskManager;
import net.sf.l2j.gameserver.util.DynamicExtension;
import net.sf.l2j.gameserver.util.FloodProtector;
import net.sf.l2j.gameserver.pathfinding.geonodes.GeoPathFinding;
import net.sf.l2j.gameserver.geoeditorcon.GeoEditorListener;
import net.sf.l2j.status.Status;

/**
 * This class ...
 *
 * @version $Revision: 1.29.2.15.2.19 $ $Date: 2005/04/05 19:41:23 $
 */
public class GameServer
{
	private static final Logger _log = Logger.getLogger(GameServer.class.getName());
	private final SelectorThread _selectorThread;
	private final SkillTable _skillTable;
	private final ItemTable _itemTable;
	private final NpcTable _npcTable;
	private final HennaTable _hennaTable;
	private final IdFactory _idFactory;
	public static GameServer gameServer;

	private final ItemHandler _itemHandler;
	private final SkillHandler _skillHandler;
	private final AdminCommandHandler _adminCommandHandler;
	private final Shutdown _shutdownHandler;
	private final UserCommandHandler _userCommandHandler;
    private final VoicedCommandHandler _voicedCommandHandler;
    private final DoorTable _doorTable;
    private final SevenSigns _sevenSignsEngine;
    private final AutoChatHandler _autoChatHandler;
	private final AutoSpawnHandler _autoSpawnHandler;
	private LoginServerThread _loginThread;

    private static ClanHallManager _cHManager;


    public static Status statusServer;
	@SuppressWarnings("unused")
	private final ThreadPoolManager _threadpools;

    public static final Calendar DateTimeServerStarted = Calendar.getInstance();

    public long getUsedMemoryMB()
	{
		return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1024/1024;
	}

    public SelectorThread getSelectorThread()
    {
    	return _selectorThread;
    }

	public ClanHallManager getCHManager(){
		return _cHManager;
	}    

    public GameServer() throws Exception
	{
        gameServer = this;
		_log.finest("used mem:" + getUsedMemoryMB()+"MB" );
        // ��� ���� ��� ����� ��������� ��� ��������� ���� L2MarkuS � �������� ������� :)
        _idFactory = IdFactory.getInstance();
        if (!_idFactory.isInitialized())
        {
            _log.severe("Could not read object IDs from DB. Please Check Your Data.");
            throw new Exception("Could not initialize the ID factory");
        }

        _threadpools = ThreadPoolManager.getInstance();

		new File(Config.DATAPACK_ROOT, "data/clans").mkdirs();
		new File(Config.DATAPACK_ROOT, "data/crests").mkdirs();

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        printSection("World");
		GameTimeController.getInstance();
        L2World.getInstance();
        Announcements.getInstance();
        AutoAnnouncementHandler.getInstance();
        MapRegionTable.getInstance();
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        printSection("Skills");
        SkillTreeTable.getInstance();
		_skillTable = SkillTable.getInstance();
		if (!_skillTable.isInitialized())
		{
		    _log.severe("Could not find the extraced files. Please Check Your Data.");
		    throw new Exception("Could not initialize the skill table");
		}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        printSection("Items");
        _itemTable = ItemTable.getInstance();
		if (!_itemTable.isInitialized())
		{
		    _log.severe("Could not find the extraced files. Please Check Your Data.");
		    throw new Exception("Could not initialize the item table");
		}
		TradeController.getInstance();
        SkillSpellbookTable.getInstance();
		RecipeController.getInstance();
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        printSection("Characters");
		CharNameTable.getInstance();
        CharTemplateTable.getInstance();
        GmListTable.getInstance();
        LevelUpData.getInstance();
        FloodProtector.getInstance();
 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        printSection("Clans");
        ClanTable.getInstance();
        AuctionManager.getInstance();
        ClanHallManager.getInstance();
        DevastatedCastle.getInstance();
        BanditStrongholdSiege.getInstance();
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        printSection("Geodata");
        GeoData.getInstance();
        if (Config.GEODATA == 2)
        	GeoPathFinding.getInstance();
		if (Config.ACCEPT_GEOEDITOR_CONN)
			GeoEditorListener.getInstance();
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        printSection("NPCs");
        _npcTable = NpcTable.getInstance();

		if (!_npcTable.isInitialized())
		{
		    _log.severe("Could not find the extraced files. Please Check Your Data.");
		    throw new Exception("Could not initialize the npc table");
		}
        ZoneData.getInstance();
        SpawnTable.getInstance();
        RaidBossSpawnManager.getInstance();
        DayNightSpawnManager.getInstance().notifyChangeMode();
        EventDroplist.getInstance();
        StaticObjects.getInstance();
        TownManager.getInstance();
        BoatManager.getInstance();
        ArenaManager.getInstance();
        // Load clan hall data before zone data
        _cHManager = ClanHallManager.getInstance();
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        printSection("Siege");
        SiegeManager.getInstance();
        CastleManager.getInstance();
        MercTicketManager.getInstance();
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        printSection("Cache");
        //Call to load caches
        HtmCache.getInstance();
        CrestCache.getInstance();
        TeleportLocationTable.getInstance();
        HennaTreeTable.getInstance();
		_hennaTable = HennaTable.getInstance();

		if (!_hennaTable.isInitialized())
		{
		   throw new Exception("Could not initialize the Henna Table");
		}
 		if (!_hennaTable.isInitialized())
		{
		   throw new Exception("Could not initialize the Henna Tree Table");
		}
        PetitionManager.getInstance();
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		L2ScriptEngineManager.getInstance();
		nProtect.getInstance();
		if(nProtect.isEnabled())
			_log.info("nProtect System Enabled");
        try
				{
					_log.info("Loading Server Scripts");
					File scripts = new File(Config.DATAPACK_ROOT + "/data/scripts.cfg");
					L2ScriptEngineManager.getInstance().executeScriptList(scripts);
				}
				catch (IOException ioe)
				{
					_log.severe("Failed loading scripts.cfg, no script going to be loaded");
				}
				try
				{
					CompiledScriptCache compiledScriptCache = L2ScriptEngineManager.getInstance().getCompiledScriptCache();
					if (compiledScriptCache == null)
					{
						_log.info("Compiled Scripts Cache is disabled.");
					}
					else
					{
						compiledScriptCache.purge();

						if (compiledScriptCache.isModified())
						{
							compiledScriptCache.save();
							_log.info("Compiled Scripts Cache was saved.");
						}
						else
						{
							_log.info("Compiled Scripts Cache is up-to-date.");
						}
					}

				}
				catch (IOException e)
				{
					_log.log(Level.SEVERE, "Failed to store Compiled Scripts Cache.", e);
				}
        MonsterRace.getInstance();

        _doorTable = DoorTable.getInstance();


		_sevenSignsEngine = SevenSigns.getInstance();
        SevenSignsFestival.getInstance();
		_autoSpawnHandler = AutoSpawnHandler.getInstance();
		_autoChatHandler = AutoChatHandler.getInstance();

        // Spawn the Orators/Preachers if in the Seal Validation period.
		
        // DISABLED, NO WORK!
		//_sevenSignsEngine.spawnSevenSignsNPC();
		

		_log.config("AutoChatHandler: Loaded " + _autoChatHandler.size() + " handlers in total.");
		_log.config("AutoSpawnHandler: Loaded " + _autoSpawnHandler.size() + " handlers in total.");

		_itemHandler = ItemHandler.getInstance();
		_itemHandler.registerItemHandler(new ChristmasTree());
		_itemHandler.registerItemHandler(new PetSummon());
		_itemHandler.registerItemHandler(new ScrollOfEscape());
		_itemHandler.registerItemHandler(new ScrollOfResurrection());
		_itemHandler.registerItemHandler(new SoulShots());
		_itemHandler.registerItemHandler(new SpiritShot());
		_itemHandler.registerItemHandler(new BlessedSpiritShot());
        _itemHandler.registerItemHandler(new BeastSoulShot());
        _itemHandler.registerItemHandler(new BeastSpiritShot());
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
        _itemHandler.registerItemHandler(new Firework());
        _itemHandler.registerItemHandler(new AdvQuestItems());
        _itemHandler.registerItemHandler(new Seed());
        _itemHandler.registerItemHandler(new Harvester());
        _itemHandler.registerItemHandler(new MercTicket());
        _log.config("ItemHandler: Loaded " + _itemHandler.size() + " handlers.");

		_skillHandler = SkillHandler.getInstance();
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

		_adminCommandHandler = AdminCommandHandler.getInstance();
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
        _adminCommandHandler.registerAdminCommandHandler(new AdminMenu());
        _adminCommandHandler.registerAdminCommandHandler(new AdminSiege());
        _adminCommandHandler.registerAdminCommandHandler(new AdminPetition());
        _adminCommandHandler.registerAdminCommandHandler(new AdminPForge());
        _adminCommandHandler.registerAdminCommandHandler(new AdminBBS());
        _adminCommandHandler.registerAdminCommandHandler(new AdminEffects());
        _adminCommandHandler.registerAdminCommandHandler(new AdminDoorControl());
        _adminCommandHandler.registerAdminCommandHandler(new AdminTest());
        _adminCommandHandler.registerAdminCommandHandler(new AdminEnchant());
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
        _adminCommandHandler.registerAdminCommandHandler(new AdminPremium());
        _adminCommandHandler.registerAdminCommandHandler(new AdminGeodata());
        _adminCommandHandler.registerAdminCommandHandler(new AdminPathNode());
        _adminCommandHandler.registerAdminCommandHandler(new AdminGeoEditor());

        //_adminCommandHandler.registerAdminCommandHandler(new AdminRadar());
        _log.config("AdminCommandHandler: Loaded " + _adminCommandHandler.size() + " handlers.");

        _userCommandHandler = UserCommandHandler.getInstance();
        _userCommandHandler.registerUserCommandHandler(new ClanPenalty());
        _userCommandHandler.registerUserCommandHandler(new ClanWarsList());
        _userCommandHandler.registerUserCommandHandler(new DisMount());
        _userCommandHandler.registerUserCommandHandler(new Escape());
        _userCommandHandler.registerUserCommandHandler(new Loc());
        _userCommandHandler.registerUserCommandHandler(new Mount());
        _userCommandHandler.registerUserCommandHandler(new PartyInfo());
		_userCommandHandler.registerUserCommandHandler(new Time());

        _log.config("UserCommandHandler: Loaded " + _userCommandHandler.size() + " handlers.");

		_voicedCommandHandler = VoicedCommandHandler.getInstance();
		_voicedCommandHandler.registerVoicedCommandHandler(new stats());

        _log.config("VoicedCommandHandler: Loaded " + _voicedCommandHandler.size() + " handlers.");

        TaskManager.getInstance();



        // ������ � ��������� ����� ����� �� ���� ������.
        L2PetDataTable.getInstance().loadPetsData();

        Universe.getInstance();

        _shutdownHandler = Shutdown.getInstance();
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
            _log.warning("There is errors in your Door.csv file. Update door.csv");
            if (Config.DEBUG)
            	e.printStackTrace();
        }

        ForumsBBSManager.getInstance();
        _log.config("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());

        // initialize the dynamic extension loader
        try {
            DynamicExtension.getInstance();
        } catch (Exception ex) {
            _log.log(Level.WARNING, "DynamicExtension could not be loaded and initialized", ex);
        }

		System.gc();
		// maxMemory is the upper limit the jvm can use, totalMemory the size of the current allocation pool, freeMemory the unused memory in the allocation pool
		long freeMem = (Runtime.getRuntime().maxMemory()-Runtime.getRuntime().totalMemory()+Runtime.getRuntime().freeMemory()) / 1024 / 1024;
		long totalMem = Runtime.getRuntime().maxMemory() / 1024 / 1024;
		_log.info("GameServer Started, free memory "+freeMem+" Mb of "+totalMem+" Mb");

		_loginThread = LoginServerThread.getInstance();
		_loginThread.start();

		_selectorThread = SelectorThread.getInstance();
		_selectorThread.start();
		_log.config("Maximum Numbers of Connected Players: " + Config.MAXIMUM_ONLINE_USERS);
	    System.out.println(".:============================================================================:.");
	    System.out.println(".: All rights reserved OnLineage2.COM and NCSOFT.                             :.");
        System.out.println(".: This core server Rise of Darknes is compile.                               :.");
        System.out.println(".: The key for the server was purchased and implemented correctly.            :.");
        System.out.println(".: The key for the server was purchased for $ 10,000, for a period of 1 year. :.");
        System.out.println(".: Server developers are willing to play nice on the server.                  :.");
        System.out.println(".: Version core server v57.23.                                                :.");
        System.out.println(".:                        L2Markus and L2DontStrong.                          :.");
        System.out.println(".:============================================================================:.");
    }

	public static void main(String[] args) throws Exception
    {
	    System.out.println(".:===========================================:.");
	    System.out.println(".:       Thank you for using our software!   :.");
	    System.out.println(".:              Compile sources              :.");
	    System.out.println(".:      OnLineage2.COM Game Server v57.23    :.");
	    System.out.println(".:===========================================:.");

        Server.SERVER_MODE = Server.MODE_GAMESERVER;
//      Local Constants
		final String LOG_FOLDER = "log"; // Name of folder for log file
		final String LOG_NAME   = "./log.cfg"; // Name of log file

		/*** Main ***/
		// ������� ����� ��� �����
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

	public static void printSection(String s)
	{
		s = "=[ " + s + " ]";
		while (s.length() < 78)
			s = "-" + s;
		_log.info(s);
	}
}
