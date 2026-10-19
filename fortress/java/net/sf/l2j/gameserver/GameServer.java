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
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.Server;
import net.sf.l2j.extensions.jbforth.L2PrintHandler;
import net.sf.l2j.gameserver.cache.CrestCache;
import net.sf.l2j.gameserver.handler.AdminCommandHandler;
import net.sf.l2j.gameserver.handler.ItemHandler;
import net.sf.l2j.gameserver.handler.SkillHandler;
import net.sf.l2j.gameserver.handler.UserCommandHandler;
import net.sf.l2j.gameserver.handler.VoicedCommandHandler;
import net.sf.l2j.gameserver.handler.admincommandhandlers.*;
import net.sf.l2j.gameserver.handler.itemhandlers.*;
import net.sf.l2j.gameserver.handler.skillhandlers.*;
import net.sf.l2j.gameserver.handler.usercommandhandlers.*;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.kill;
import net.sf.l2j.gameserver.ItemsAutoDestroy;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.lib.Events;
import net.sf.l2j.gameserver.lib.Strings;
import net.sf.l2j.gameserver.model.*;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.script.faenor.FaenorScriptEngine;
import net.sf.l2j.loginserver.LoginController;
import ru.balancer.JBForth;
import ru.balancer.jbforth.Word;

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

	private final LoginController _loginController;
	private final ItemHandler _itemHandler;
	private final SkillHandler _skillHandler;
	private final AdminCommandHandler _adminCommandHandler;
	private final Shutdown _shutdownHandler;
    private final UserCommandHandler _userCommandHandler;
    private final VoicedCommandHandler _voicedCommandHandler;
    private static DoorTable _doorTable;
	private final SevenSigns _sevenSignsEngine;
	private final AutoChatHandler _autoChatHandler;
	private final AutoSpawnHandler _autoSpawnHandler;


	//NEWAGE >>
	public static boolean _playerExpPenalty = true;
	public static boolean _playerDrop = true;
	//NEWAGE <<
    public static JBForth jbf;
	public static Events events;

	private static ConcurrentHashMap<Integer,ArrayList<Word>> npc_on_die_map = new ConcurrentHashMap<Integer,ArrayList<Word>>();

    public static final ConcurrentHashMap<Integer,ArrayList<Word>> getNpcOnDieMap()
	{
		return npc_on_die_map;
	}

    public static final void setNpcOnDieMap(ConcurrentHashMap<Integer,ArrayList<Word>> map)
	{
		npc_on_die_map = map;
	}

	public long getUsedMemoryMB()
	{
		return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1024/1024;
	}

    public LoginController getLoginController()
	{
		return _loginController;
	}

    public SelectorThread getSelectorThread()
	{
		return _selectorThread;
	}

    public static final DoorTable getDoorsTable()
	{
		return _doorTable;
	}

	public GameServer() throws Exception
	{
	    Server.gameServer = this;
		_log.finest("used mem:" + getUsedMemoryMB()+"MB" );

		Strings.reload();

	    _idFactory = IdFactory.getInstance();
	    if (!_idFactory.isInitialized())
		{
		    _log.severe("Could not read object IDs from DB. Please Check Your Data.");
		    throw new Exception("Could not initialize the ID factory");
		}

		ClientScheduler.getInstance();
		_selectorThread = new SelectorThread(Config.GAMESERVER_HOSTNAME, Config.PORT_GAME);
		_selectorThread.start();

		_log.config("Maximum Numbers of Connected Players: " + Config.MAXIMUM_ONLINE_USERS);

		new File("./", "data/clans").mkdirs();
		new File("./", "data/crests").mkdirs();
	    new File("pathnode").mkdirs();

		// start game time control early
		GameTimeController.getInstance();

		// keep the references of Singletons to prevent garbage collection
		_loginController = LoginController.getInstance();
		_loginController.setMaxAllowedOnlinePlayers(Config.MAXIMUM_ONLINE_USERS);
		CharNameTable.getInstance();

		L2World.init();

		_itemTable = ItemTable.getInstance();
		if (!_itemTable.isInitialized())
		{
		    _log.severe("Could not find the extraced files. Please Check Your Data.");
		    throw new Exception("Could not initialize the item table");
		}

		jbf = new JBForth(null);
		JBForth.printHandler = new L2PrintHandler();

		events = new Events();

		TradeController.getInstance();
		_skillTable = SkillTable.getInstance();
		if (!_skillTable.isInitialized())
		{
		    _log.severe("Could not find the extraced files. Please Check Your Data.");
		    throw new Exception("Could not initialize the skill table");
		}

		RecipeController.getInstance();

		SkillTreeTable.getInstance();
		SkillSpellbookTable.getInstance();
		CharTemplateTable.getInstance();
		CastleGuardsSpawner.getInstance();
        CrestCache.getInstance();

		_npcTable = NpcTable.getInstance();
		if (!_npcTable.isInitialized())
		{
		    _log.severe("Could not find the extraced files. Please Check Your Data.");
		    throw new Exception("Could not initialize the npc table");
		}
		_hennaTable = HennaTable.getInstance();
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
		
//		L2World.getInstance();

		SpawnTable.getInstance();

		Announcements.getInstance();
		MapRegionTable.getInstance();
		EventDroplist.getInstance();
        
	    if (Config.AUTODESTROY_ITEM_AFTER > 0)
		    ItemsAutoDestroy.getInstance();
        
	    MonsterRace.getInstance();
        
		_doorTable 		   = DoorTable.getInstance();
	    StaticObjects.getInstance();
        
		_sevenSignsEngine 	= SevenSigns.getInstance();
	    SevenSignsFestival.getInstance();		
		_autoSpawnHandler = AutoSpawnHandler.getInstance();
		_autoChatHandler 	 = AutoChatHandler.getInstance();

		// Spawn the Orators/Preachers if in the Seal Validation period.
	    if (_sevenSignsEngine.isSealValidationPeriod())
		    _sevenSignsEngine.spawnSevenSignsNPCs();

		_log.config("AutoChatHandler: Loaded " + _autoChatHandler.size() + " handlers in total.");
		_log.config("AutoSpawnHandler: Loaded " + _autoSpawnHandler.size() + " handlers in total.");

		_itemHandler = ItemHandler.getInstance();
		_itemHandler.registerItemHandler(new PetSummon());
		_itemHandler.registerItemHandler(new ScrollOfEscape());
		_itemHandler.registerItemHandler(new ScrollOfResurrection());
		_itemHandler.registerItemHandler(new SoulShots());
		_itemHandler.registerItemHandler(new SpiritShot());
		_itemHandler.registerItemHandler(new BlessedSpiritShot());
		_itemHandler.registerItemHandler(new WorldMap());
		_itemHandler.registerItemHandler(new Recipes());
	    _itemHandler.registerItemHandler(new CompSoulShotPacks());
	    _itemHandler.registerItemHandler(new CompSpiritShotPacks());
	    _itemHandler.registerItemHandler(new CompBlessedSpiritShotPacks());
	    _itemHandler.registerItemHandler(new MysteryPotion());
		_itemHandler.registerItemHandler(new EnchantScrolls());
		_itemHandler.registerItemHandler(new Remedy());
		_itemHandler.registerItemHandler(new Scrolls());
		_itemHandler.registerItemHandler(new CrystalCarol());
		_itemHandler.registerItemHandler(new SoulCrystals());
		_itemHandler.registerItemHandler(new SevenSignsRecord());
	    _itemHandler.registerItemHandler(new CharChangePotions());
	    _itemHandler.registerItemHandler(new Firework());
	    _itemHandler.registerItemHandler(new MercTicket());
	    _log.config("ItemHandler: Loaded " + _itemHandler.size() + " handlers.");

		_skillHandler = SkillHandler.getInstance();
		_skillHandler.registerSkillHandler(new Pdam());
		_skillHandler.registerSkillHandler(new FatalBlow());
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
	    _adminCommandHandler.registerAdminCommandHandler(new AdminEffects());
	    _adminCommandHandler.registerAdminCommandHandler(new AdminDoorControl());
	    _adminCommandHandler.registerAdminCommandHandler(new AdminTest());
	    _adminCommandHandler.registerAdminCommandHandler(new AdminMobGroup());
	    _adminCommandHandler.registerAdminCommandHandler(new AdminRes());
	    _adminCommandHandler.registerAdminCommandHandler(new AdminMammon());
	    _adminCommandHandler.registerAdminCommandHandler(new AdminUnblockIp());
	    _adminCommandHandler.registerAdminCommandHandler(new AdminPledge());
	    _adminCommandHandler.registerAdminCommandHandler(new AdminSiege());
        _adminCommandHandler.registerAdminCommandHandler(new AdminIP());

		_adminCommandHandler.registerAdminCommandHandler(new AdminCancel()); // admin debuff by Sab
		_adminCommandHandler.registerAdminCommandHandler(new AdminNochannel()); // admin nochannel by Sab

//          _adminCommandHandler.registerAdminCommandHandler(new AdminRadar());
	    _log.config("AdminCommandHandler: Loaded " + _adminCommandHandler.size() + " handlers.");

	    _userCommandHandler = UserCommandHandler.getInstance();
	    _userCommandHandler.registerUserCommandHandler(new Loc());
	    _userCommandHandler.registerUserCommandHandler(new Escape());
		_userCommandHandler.registerUserCommandHandler(new Time());
	    _userCommandHandler.registerUserCommandHandler(new PartyInfo());		
	    _log.config("UserCommandHandler: Loaded " + _userCommandHandler.size() + " handlers.");

		_voicedCommandHandler = VoicedCommandHandler.getInstance();
		_voicedCommandHandler.registerVoicedCommandHandler(new kill());
	    _log.config("VoicedCommandHandler: Loaded " + _voicedCommandHandler.size() + " handlers.");

	    FaenorScriptEngine.getInstance();

		Quest.init();

        CastleManager.getInstance();
		
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
		} catch (NullPointerException e)
		{
		    _log.warning("Door.csv does not contain the right door info. Update door.csv");
		    e.printStackTrace();
		}
		
	    _log.config("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());

	    jbf.execp("on-server-start");

		System.gc();
		// maxMemory is the upper limit the jvm can use, totalMemory the size of the current allocation pool, freeMemory the unused memory in the allocation pool
		long freeMem = (Runtime.getRuntime().maxMemory()-Runtime.getRuntime().totalMemory()+Runtime.getRuntime().freeMemory()) / 1024 / 1024;
		long totalMem = Runtime.getRuntime().maxMemory() / 1024 / 1024;
		_log.info("GameServer Started, free memory "+freeMem+" Mb of "+totalMem+" Mb");
	}

	public final ItemTable getItemTable()
	{
		return _itemTable;
	}
}
