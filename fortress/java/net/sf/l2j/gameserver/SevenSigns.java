package net.sf.l2j.gameserver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.AutoChatHandler;
import net.sf.l2j.gameserver.model.AutoSpawnHandler;
import net.sf.l2j.gameserver.model.L2MinionData;
import net.sf.l2j.gameserver.model.L2MinionInstance;
import net.sf.l2j.gameserver.model.L2MonsterInstance;
import net.sf.l2j.gameserver.model.L2NpcInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.AutoChatHandler.AutoChatInstance;
import net.sf.l2j.gameserver.model.AutoSpawnHandler.AutoSpawnInstance;
import net.sf.l2j.gameserver.serverpackets.SignsSky;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.StatsSet;

/**
 *  Seven Signs Engine
 *    
 *  TODO: 
 *	- Currently all those in the winning cabal can enter the catacombs and necropolises.
 *		The official site says this is seal-based rather than winner-based. If so, then
 *		this needs to be factored in also.
 *  - Implementation of the Seal of Strife for sieges.
 *  
 *  @author Tempy
 */
public class SevenSigns {
    protected static Logger _log = Logger.getLogger(SevenSigns.class.getName());
	private static SevenSigns _instance;

	// Basic Seven Signs Constants \\
	public static final String SEVEN_SIGNS_DATA_FILE = "config/signs.properties";
	public static final String SEVEN_SIGNS_HTML_PATH = "data/html/seven_signs/";
	
	public static final int CABAL_NULL = 0;
	public static final int CABAL_DUSK = 1;
	public static final int CABAL_DAWN = 2;	
	
	public static final int SEAL_NULL = 0;
	public static final int SEAL_AVARICE = 1;
	public static final int SEAL_GNOSIS = 2;
	public static final int SEAL_STRIFE = 3;

	public static final int PERIOD_COMP_RECRUITING = 0;
	public static final int PERIOD_COMPETITION = 1;
	public static final int PERIOD_COMP_RESULTS = 2;
	public static final int PERIOD_SEAL_VALIDATION = 3;
    
	public static final int PERIOD_START_HOUR = 1;
	public static final int PERIOD_START_MINS = 20;
    public static final int PERIOD_START_DAY = Calendar.MONDAY;

    // The quest event and seal validation periods last for approximately one week
    // with a 1 hour "interval" period sandwiched between them.
    public static final int PERIOD_MINOR_LENGTH = 3600000;
    public static final int PERIOD_MAJOR_LENGTH = 604800000 - PERIOD_MINOR_LENGTH;
	
	public static final int ANCIENT_ADENA_ID = 5575;
	public static final int RECORD_SEVEN_SIGNS_ID = 5707;
    public static final int CERTIFICATE_OF_APPROVAL_ID = 6388;
	public static final int RECORD_SEVEN_SIGNS_COST = 500;
    public static final int ADENA_JOIN_DAWN_COST = 50000;

    // NPC Related Constants \\
	public static final int ORATOR_NPC_ID = 8094;
	public static final int PREACHER_NPC_ID = 8093;
    public static final int MAMMON_MERCHANT_ID = 8113;
    public static final int MAMMON_BLACKSMITH_ID = 8126;
    public static final int LILITH_NPC_ID = 10283;
    public static final int ANAKIM_NPC_ID = 10286;

	// Seal Stone Related Constants \\
	public static final int SEAL_STONE_BLUE_ID = 6360;
	public static final int SEAL_STONE_GREEN_ID = 6361;
	public static final int SEAL_STONE_RED_ID = 6362;
	
	public static final int SEAL_STONE_BLUE_VALUE = 3;
	public static final int SEAL_STONE_GREEN_VALUE = 5;
	public static final int SEAL_STONE_RED_VALUE = 10; 

	private static final int BLUE_CONTRIB_POINTS = 1;
	private static final int GREEN_CONTRIB_POINTS = 5;
	private static final int RED_CONTRIB_POINTS = 10;

	// There is a max on official, but not sure what!
	private static final int MAXIMUM_PLAYER_CONTRIB = 300000;
	
	// NPC Related Constants \\
	private static final int [][] merchantSpawns = {
		{172373, -17833, -4901}, // Disciples
		{83175, 208998, -5439}, // Saints
		{118343, 132578, -4831}, // Martyrs
		{-52172, 78884, -4741}, // Ascetics
		{-21657, 77164, -5173}, // Patriots
		{111337, 173804, -5439}, // Worshippers
		{45029, 123802, -5413}, // Pilgrims
		{-41350, 209876, -5087}, // Sacrifice
		};

	private static final int [][] blacksmithSpawns = {
		{114013, 84326, -6541}, // Forbidden Path
		{-19360, 13278, -4901}, // Dark Omens
		{140519, 79464, -5429}, // Witch
		{77787, 78176, -5125}, // Apostate
		{46303, 170091, -4981}, // Branded
		{43093, 143706, -5381}, // Heretics
		};
			
	private static final int[][][] oratorSpawns = {
        {
            {16901, 144748, -3027}, 
            {81002, 53495, -1586},
            {143926, 26468, -2338},
            {85328, 147352, -3431},
            {114907, 77916, -2662},
            {-12344, 123605, -3132},
            {-83157, 149846, -3155},
            {115482, 219266, -3689},
            {77830, 148627, -3623},
            {-79123, 155098, -3204}
        },
        {
            {17557, 147084, -3150},
            {80039, 54291, -1586},
            {150417, 25182, -2141},
            {83872, 143882, -3431},
            {115509, 74857, -2625},
            {-14199, 126128, -3171},
            {-84356, 152742, -3204},
            {107251, 218166, -3701},
            {81556, 152183, -3559}
        },
        {
            {20826, 145769, -3171},
            {81527, 56009, -1551},
            {147460, 20537, -2101},
            {81540, 144204, -3559},
            {118273, 74592, -2529},
            {-16168, 124129, -3143},
            {-79563, 152726, -3204},
            {19038, 142923, -3078},
            {-14563, 121461, -3015}
        }
		};

	private static final int[][][] preacherSpawns = {
        {
            {83319, 148054, -3431},
            {15722, 142877, -2732},
            {-80690, 149996, -3070},
            {-12591, 122673, -3142},
            {111389, 219257, -3572},
            {117098, 77066, -2720},
            {82863, 53290, -1522},
            {147440, 30047, -2487},
            {-80752, 152738, -3204},
            {145015, 25269, -2167},
            {109660, 217339, -3775}
        },
        {
            {83894, 147495, -3431},
            {18172, 145740, -3140},
            {-83107, 150270, -3155},
            {-13252, 123689, -3143},
            {113479, 217795, -3731},
            {115893, 77551, -2729},
            {80617, 54116, -1586},
            {145394, 27629, -2295},
            {81535, 146866, -3559},
            {147459, 21148, -2167}
        },
        {
            {81547, 150347, -3559},
            {19096, 143980, -3096},
            {-83070, 152740, -3204},
            {-14268, 124941, -3156},
            {114086, 220214, -3568},
            {115936, 75382, -2625},
            {80416, 55959, -1586},
            {149515, 27641, -2295},
            {-14480, 122330, -3126},
            {146616, 25816, -2039}
        },
        {
            {79825, 148619, -3559},
            {19823, 145863, -3142},
            {-80037, 154344, -3204},
            {-15549, 124170, -3143},
            {110189, 221393, -3569},
            {118049, 75783, -2715},
            {82048, 55417, -1551},
            {149899, 24719, -2167},
            {107256, 218615, -3701},
            {148013, 27029, -2231}
        }
		};

	public static final String[] chatDoomPreacher =	{
        "The prophecy of doom has awoken!", 
		"All is lost! Prepare to meet the goddess of death!",
		"All is lost! The prophecy of destruction has been fulfilled!",
		"This world will soon be annihilated!",
		"%player_cabal_loser%! You might as well give up!",
		"%player_cabal_loser%! You bring an ill wind!",
		"A curse upon you!",
		"%player_cabal_loser%! All is lost! Prepare to meet the goddess of death!"
		};
	
	public static final String[] chatOratorRevelations = {
		"%player_cabal_winner%, the time for glory is at hand!", 
		"The prophecy of darkness has been fulfilled!",
        "The prophecy of darkness has come to pass!",
        "As foretold in the prophecy of darkness, the era of chaos has begun!",
        "The day of judgment is near!",
        "%player_cabal_winner%, Darkness shall be banished forever!",
		"All hail the eternal twilight!",
		"%player_cabal_winner%! I bestow on you the authority of the abyss!"
		};
	
	// Current max of 30 items sellable right now.
    // Updated to include official prices as well as "modern" adena price.
	public static final String[][] priestItems = {
		// Greater Healing Potion
		{"1539","1080","90","icon.etc_reagent_white_i00"},
        // Blessed Rez Scroll for Pet
        {"6387","6000","500","etc_scroll_of_resurrection_pet_i01"},
        // Greater Haste Potion
        {"1374","3600","360","etc_lesser_potion_green_i00"},
        // Greater Swift Attack Potion
        {"1375","7200","600","etc_lesser_potion_purple_i00"},
        // Greater Magic Haste Potion
        {"6036","7200","600","etc_potion_of_acumen3_i00"},
        // CP Potion
        {"5591","240","20","etc_cp_potion_i00"},
        // Greater CP Potion
        {"5592","600","50","etc_cp_potion_i01"},        
        // SP Scroll - Low Grade
        {"5593","2400","200","etc_sp_scroll1_i00"},
        // SP Scroll - Medium Grade
        {"5594","24000","2000","etc_sp_scroll2_i00"},
        // SP Scroll - High Grade
        {"5595","480000","40000","etc_sp_scroll3_i00"},
		// Mystery Potion 
		{"5234","12000","1000","etc_lesser_potion_clear_i00"},
        // Facelifting Potions A-C
		{"5235","24000","2000","etc_lesser_potion_purple_i00"}, {"5236","24000","2000","etc_lesser_potion_purple_i00"}, 
        {"5237","24000","2000","etc_lesser_potion_purple_i00"}, 
        // Hair Dye Potions A-D
		{"5238","24000","2000","etc_lesser_potion_scarlet_i00"}, {"5239","24000","2000","etc_lesser_potion_scarlet_i00"}, 
        {"5240","24000","2000","etc_lesser_potion_scarlet_i00"}, {"5241","24000","2000","etc_lesser_potion_scarlet_i00"}, 
        // Hair Style Change Potion A-G
		{"5242","24000","2000","etc_lesser_potion_green_i00"}, {"5243","24000","2000","etc_lesser_potion_green_i00"}, 
        {"5244","24000","2000","etc_lesser_potion_green_i00"}, {"5245","24000","2000","etc_lesser_potion_green_i00"}, 
        {"5246","24000","2000","etc_lesser_potion_green_i00"}, {"5247","24000","2000","etc_lesser_potion_green_i00"}, 
		// Party Masks
		{"5808","600000","50000","etc_party_mask_i00"}, {"6394","600000","50000","etc_party_mask_i01"},
        // Squeaking Shoes
        {"5590","420000","35000","armor_cloth_shoes_i00"},
		// Record of the Seven Signs
		{"5707","600","50","etc_ssq_scroll_i00"}
        // Luck Charms 
        //{"5703","5000","etc_charm_of_luck_i00"},{"5803","7500","etc_charm_of_luck_i00"},
        //{"5804","11000","etc_charm_of_luck_i00"},{"5805","15000","etc_charm_of_luck_i00"},{"5806","21000","etc_charm_of_luck_i00"},
        //{"5807","28500","etc_charm_of_luck_i00"},
		}; // Total: 28 Items

	private final Calendar _calendar = Calendar.getInstance();
	
    protected int _activePeriod;
    protected int _currentCycle;
    protected int _dawnStoneScore;
    protected int _duskStoneScore;
    protected int _dawnFestivalScore;
    protected int _duskFestivalScore;
    protected int _compWinner;
    protected int _previousWinner;
    
	private boolean _initializedSpawns;

	private Map<Integer, StatsSet> _signsPlayerData;
	private List<L2NpcInstance> _signsNPCInsts;
	
	private Map<Integer, Integer> _signsSealOwners;
	private Map<Integer, Integer> _signsDuskSealTotals;
	private Map<Integer, Integer> _signsDawnSealTotals;
    
    private static AutoSpawnInstance _merchantSpawn;
    private static AutoSpawnInstance _blacksmithSpawn;
    private ArrayList<AutoSpawnInstance> _preacherSpawns = new ArrayList<AutoSpawnInstance>();
    private ArrayList<AutoSpawnInstance> _oratorSpawns = new ArrayList<AutoSpawnInstance>();
    private static AutoChatInstance _preacherChat;
    private static AutoChatInstance _oratorChat;
	
	public SevenSigns() 
	{
		_signsPlayerData = new FastMap<Integer, StatsSet>();
		_signsNPCInsts = new FastList<L2NpcInstance>();
		_signsSealOwners = new FastMap<Integer, Integer>();
		_signsDuskSealTotals = new FastMap<Integer, Integer>();
		_signsDawnSealTotals = new FastMap<Integer, Integer>();		
		
		try {
			restoreSevenSignsData();
		}
		catch (Exception e) {
			_log.severe("SevenSigns: Failed to load configuration: " + e);
		}
		
		_log.info("SevenSigns: Currently in the " + getCurrentPeriodName() + " period!");
		initializeSeals();
		
		if (isSealValidationPeriod())
            _log.info("SevenSigns: The " + getCabalName(getCabalHighestScore()) + " were victorious last week.");
		else 
			_log.info("SevenSigns: The " + getCabalName(getCabalHighestScore()) + " are in the lead this week.");
		
		synchronized (this) 
		{
            setCalendarForNextPeriodChange();
            long milliToChange = getMilliToPeriodChange();		
			
			// Schedule a time for the next period change.
			SevenSignsPeriodChange sspc = new SevenSignsPeriodChange();
			ClientScheduler.getInstance().scheduleMed(sspc, milliToChange);
		
			// Thanks to http://rainbow.arch.scriptmania.com/scripts/timezone_countdown.html for help with this.
			double numSecs = (milliToChange / 1000) % 60;
			double countDown = ((milliToChange / 1000) - numSecs) / 60;
			int numMins = (int)Math.floor(countDown % 60);
			countDown = (countDown - numMins) / 60; 
			int numHours = (int)Math.floor(countDown % 24);
			int numDays = (int)Math.floor((countDown - numHours) / 24);

			_log.info("SevenSigns: Next period begins in " + numDays + " days, " + numHours + " hours and " + numMins + " mins.");
		}
	}
	
	public static SevenSigns getInstance() {
		if (_instance == null) {
			_instance = new SevenSigns();
		}
		return _instance;
	}
	
	public static int calcContributionScore(int blueCount, int greenCount, int redCount)
	{
		int contrib = blueCount * BLUE_CONTRIB_POINTS;
		contrib += greenCount * GREEN_CONTRIB_POINTS; 
		contrib += redCount * RED_CONTRIB_POINTS;
		
		return contrib;
	}
	
	public static int calcAncientAdenaReward(int blueCount, int greenCount, int redCount)
	{
		int reward = blueCount * SEAL_STONE_BLUE_VALUE;
		reward += greenCount * SEAL_STONE_GREEN_VALUE;
		reward += redCount * SEAL_STONE_RED_VALUE;
		
		return reward;
	}
	
	public static final String getCabalShortName(int cabal) {
		return (cabal == CABAL_DUSK) ? "dusk" : "dawn";
	}
	
	public static final String getCabalName(int cabal) {
		return (cabal == CABAL_DUSK) ? "Revolutionaries of Dusk" : "Lords of Dawn";
	}
	
	public static final String getSealName(int seal, boolean shortName) 
	{
		String sealName = (!shortName) ? "Seal of " : "";
		
		switch (seal) {
		case SEAL_AVARICE:
			sealName += "Avarice"; 
			break;
		case SEAL_GNOSIS:
			sealName += "Gnosis";
			break;
		case SEAL_STRIFE:
			sealName += "Strife";
			break;
		}
 
		return sealName;
	}
	
	 /**
	 * Used to capitalize the first letter of every "word" in a string.<br>
	 * (Ported from the idea in Perl and PHP)
	 * 
	 * @param String str
	 * @return String containing the modified string.
	 */
	public static String capitalizeWords(String str) 
	{
		char[] charArray = str.toCharArray();
		String result = "";
	   
		// Capitalize the first letter in the given string!
		charArray[0] = Character.toUpperCase(charArray[0]);
	   
		for (int i = 0; i < charArray.length; i++) {
			if (Character.isWhitespace(charArray[i])) 
				charArray[i + 1] = Character.toUpperCase(charArray[i + 1]);

			result += Character.toString(charArray[i]);
		}
	   
		return result;
	}
	
	public final int getCurrentCycle() {
		return _currentCycle;
	}
	
	public final int getCurrentPeriod() {
		return _activePeriod;
	}
	
	private final int getDaysToPeriodChange() {
		int numDays = _calendar.get(Calendar.DAY_OF_WEEK) - PERIOD_START_DAY;

		if (numDays < 0) 
			return 0 - numDays;
        
		return 7 - numDays;
	}
	
    public final long getMilliToPeriodChange() 
    {
        long currTimeMillis = System.currentTimeMillis();
        long changeTimeMillis = _calendar.getTimeInMillis();
        
        return (changeTimeMillis - currTimeMillis);    
    }
    
    private void setCalendarForNextPeriodChange() 
    {
        // Calculate the number of days until the next period
        // A period starts at 1:20 am (local time), like on official servers.
        switch (getCurrentPeriod())
        {
            case PERIOD_SEAL_VALIDATION:
            case PERIOD_COMPETITION:
                int daysToChange = getDaysToPeriodChange();
                
                if (daysToChange == 7) 
                    if (_calendar.get(Calendar.MINUTE) < PERIOD_START_MINS)
                        if (_calendar.get(Calendar.HOUR_OF_DAY) <= PERIOD_START_HOUR)
                            // If next period is due to start today, don't add any days to the calendar.
                            daysToChange = 0; 
                
                // Otherwise...
                if (daysToChange > 0)
                    _calendar.add(Calendar.DATE, daysToChange);
                
                _calendar.set(Calendar.HOUR_OF_DAY, PERIOD_START_HOUR);
                _calendar.set(Calendar.MINUTE, PERIOD_START_MINS);
                break;
            case PERIOD_COMP_RECRUITING:
            case PERIOD_COMP_RESULTS:
                _calendar.add(Calendar.MILLISECOND, PERIOD_MINOR_LENGTH);
                break;
        }
    }
    
	public final String getCurrentPeriodName() {
		String periodName = null;
		
		switch (_activePeriod) {
		case PERIOD_COMP_RECRUITING:
			periodName = "Quest Event Initialization";
			break;
		case PERIOD_COMPETITION:
			periodName = "Competition (Quest Event)";
			break;
		case PERIOD_COMP_RESULTS:
			periodName = "Quest Event Results";
			break;
		case PERIOD_SEAL_VALIDATION:
			periodName = "Seal Validation";
			break;
		}
		
		return periodName;
	}
	
	public final boolean isSealValidationPeriod() {
		return (_activePeriod == PERIOD_SEAL_VALIDATION);
	}
		
	public final int getCurrentScore(int cabal) {
		if (cabal == CABAL_DUSK)
			return _duskStoneScore + _duskFestivalScore;
		else if (cabal == CABAL_DAWN)
			return _dawnStoneScore + _dawnFestivalScore;
		else
			return 1; // to prevent any divide by zero errors
	}
	
	public final int getCurrentStoneScore(int cabal) {
		if (cabal == CABAL_DUSK)
			return _duskStoneScore;
		else if (cabal == CABAL_DAWN)
			return _dawnStoneScore;
		else
			return 1; // to prevent any divide by zero errors
	}
	
	public final int getCurrentFestivalScore(int cabal) {
        if (cabal == CABAL_DUSK)
            return _duskFestivalScore;
        else if (cabal == CABAL_DAWN)
            return _dawnFestivalScore;
        else
            return 1; // to prevent any divide by zero errors
	}
	
	public final int getCabalHighestScore() {
		if (getCurrentScore(CABAL_DUSK) > getCurrentScore(CABAL_DAWN))
			return CABAL_DUSK;	
        
		return CABAL_DAWN;
	}
	
	public final int getSealOwner(int seal) {
		return _signsSealOwners.get(seal);
	}

	public final int getSealProportion(int seal, int cabal) {
		if (cabal == CABAL_NULL)
			return -1;
		else if (cabal == CABAL_DUSK)
			return _signsDuskSealTotals.get(seal);
		else
			return _signsDawnSealTotals.get(seal);
	}
	
	public int getTotalMembers(int cabal)
	{
		int cabalMembers = 0;
		String cabalName = getCabalShortName(cabal);
		
		for (StatsSet sevenDat : _signsPlayerData.values())
			if (sevenDat.getString("cabal").equals(cabalName))
				cabalMembers++;
		
		return cabalMembers;
	}
	
    public final StatsSet getPlayerStatsSet(L2PcInstance player)
    {
        if (!hasRegisteredBefore(player))
            return null;
        
        return _signsPlayerData.get(player.getObjectId());
    }
	
	public int getPlayerStoneContrib(L2PcInstance player) {
		if (!hasRegisteredBefore(player))
			return 0;
		
		int stoneCount = 0;
		
		StatsSet currPlayer = _signsPlayerData.get(player.getObjectId());
		
		stoneCount += currPlayer.getInteger("red_stones");
		stoneCount += currPlayer.getInteger("green_stones");
		stoneCount += currPlayer.getInteger("blue_stones");
		
		return stoneCount;
	}
	
	public int getPlayerAdenaCollect(L2PcInstance player) {
		if (!hasRegisteredBefore(player))
			return 0;
		
		return _signsPlayerData.get(player.getObjectId()).getInteger("ancient_adena_amount");
	}
	
	public int getPlayerSeal(L2PcInstance player) {
		if (!hasRegisteredBefore(player))
			return SEAL_NULL;
			
		return _signsPlayerData.get(player.getObjectId()).getInteger("seal");
	}

	public int getPlayerCabal(L2PcInstance player) {
		if (!hasRegisteredBefore(player)) 
			return CABAL_NULL;
		
		String playerCabal = getPlayerCabalStr(player);

		if (playerCabal.equals(""))
			return CABAL_NULL;
		else if (playerCabal.equals("dawn")) 
			return CABAL_DAWN;
		else
			return CABAL_DUSK;
	}
	
	public String getPlayerCabalStr(L2PcInstance player) {
		return _signsPlayerData.get(player.getObjectId()).getString("cabal");
	}
	
    /**
     * Registers all random spawns and auto-chats for Seven Signs NPCs, 
     * along with spawns for the Preachers of Doom and Orators of Revelations 
     * at the beginning of the Seal Validation period. 
     */
    public void spawnSevenSignsNPCs()
    {
        L2NpcTemplate npcTemplate = null;
        
        // Register the random auto spawns of the Mammon NPCs
        if(getSealOwner(SEAL_AVARICE)!=CABAL_NULL)
        {
            _merchantSpawn = AutoSpawnHandler.getInstance().registerSpawn(8113, merchantSpawns, -1, -1, -1);
            _merchantSpawn.setRandomSpawn(true);
            if (Config.ANNOUNCE_MAMMON_SPAWN)
                _merchantSpawn.setBroadcast(true);
            
        }
        
        if(getSealOwner(SEAL_GNOSIS)!=CABAL_NULL)
        {
            _blacksmithSpawn = AutoSpawnHandler.getInstance().registerSpawn(8126, blacksmithSpawns, -1, -1, -1);
            _blacksmithSpawn.setRandomSpawn(true);
            if (Config.ANNOUNCE_MAMMON_SPAWN)
                _blacksmithSpawn.setBroadcast(true);
        }
        
        /*if (Config.ANNOUNCE_MAMMON_SPAWN) {
            _merchantSpawn.setBroadcast(true);
            _blacksmithSpawn.setBroadcast(true);
        }*/
        
        /* Set up the spawning and required auto chats. */
        // First, do this for the Preachers of Doom
        _preacherChat = AutoChatHandler.getInstance().registerGlobalChat(PREACHER_NPC_ID, SevenSigns.chatDoomPreacher, -1);
        _preacherChat.setDefaultRandom(true);
        
        _oratorChat = AutoChatHandler.getInstance().registerGlobalChat(ORATOR_NPC_ID, SevenSigns.chatOratorRevelations, -1);
        _oratorChat.setDefaultRandom(true);
        
        /*
         * If the spawns have been initialized before don't try to do it again.
         * Otherwise, create the spawns for the required monsters and minions.
         */
        if (_initializedSpawns)
            return;
        
        try {
            // First, do this for the Preachers of Doom \\
            if(getSealOwner(SEAL_GNOSIS)!=CABAL_NULL)
            {
                for(int i=0;i<4;i++)
                {
                    _preacherSpawns.add(AutoSpawnHandler.getInstance().registerSpawn(PREACHER_NPC_ID, preacherSpawns[i], -1, -1, -1));
                    _preacherSpawns.get(i).setRandomSpawn(true);
                }
                for(int i=0;i<3;i++)
                {
                    _oratorSpawns.add(AutoSpawnHandler.getInstance().registerSpawn(ORATOR_NPC_ID, oratorSpawns[i], -1, -1, -1));
                    _oratorSpawns.get(i).setRandomSpawn(true);
                }
            }
            /*npcTemplate = NpcTable.getInstance().getTemplate(PREACHER_NPC_ID);
            
            for (int i = 0; i < preacherSpawns.length; i++) 
            {   
                    L2Spawn npcSpawn = new L2Spawn(npcTemplate);
                    
                    npcSpawn.setLocx(preacherSpawns[i][0]);
                    npcSpawn.setLocy(preacherSpawns[i][1]);
                    npcSpawn.setLocz(preacherSpawns[i][2]);
                    npcSpawn.setAmount(1);
                    SpawnTable.getInstance().addNewSpawn(npcSpawn, false);
                    _signsNPCInsts.add(npcSpawn.doSpawn(true));
                    
                    if (Config.DEBUG)
                        _log.info("SevenSigns: Spawned a Preacher at "
                            + preacherSpawns[i][0] + " " + preacherSpawns[i][1] + " " + preacherSpawns[i][2]);
                }
            */
            // Now do the same again for the Orators of Revelations \\
            /*
            for (int i = 0; i < oratorSpawns.length; i++) 
            {   
                    L2Spawn npcSpawn = new L2Spawn(npcTemplate);
                    
                    npcSpawn.setLocx(oratorSpawns[i][0]);
                    npcSpawn.setLocy(oratorSpawns[i][1]);
                    npcSpawn.setLocz(oratorSpawns[i][2]);
                    npcSpawn.setAmount(1);
                    SpawnTable.getInstance().addNewSpawn(npcSpawn, false);
                    _signsNPCInsts.add(npcSpawn.doSpawn(true));
                    
                    if (Config.DEBUG)
                        _log.info("SevenSigns: Spawned an Orator at "
                            + oratorSpawns[i][0] + " " + oratorSpawns[i][1] + " " + oratorSpawns[i][2]);
                }*/
            if(getSealOwner(SEAL_AVARICE)!=CABAL_NULL)
            {
            /* Similarly, spawn either of the two major boss monsters and their minions.
             * 
             * If Dawn wins, spawn Lilith with 2 Lilith's Agent and 2 Lilith's Escort.
             * If Dusk wins, spawn Anakim with 2 Anakim's Guardian and 2 Anakim's Royal Guard.
             * 
             */ 
                final String bossName;
                L2MinionData minionDat;
            
                if (getCabalHighestScore() == CABAL_DAWN) {
                    bossName = "Lilith";
                    npcTemplate = NpcTable.getInstance().getTemplate(LILITH_NPC_ID);
                
                    minionDat = new L2MinionData();                 
                    minionDat.setMinionId(10284);
                    minionDat.setAmount(2);
                    npcTemplate.addRaidData(minionDat);
                
                    minionDat = new L2MinionData();                 
                    minionDat.setMinionId(10285);
                    minionDat.setAmount(2);
                    npcTemplate.addRaidData(minionDat);
                }
                else {
                    bossName = "Anakim";
                    npcTemplate = NpcTable.getInstance().getTemplate(ANAKIM_NPC_ID);
                
                    minionDat = new L2MinionData();                 
                    minionDat.setMinionId(10287);
                    minionDat.setAmount(2);
                    npcTemplate.addRaidData(minionDat);     
                
                    minionDat = new L2MinionData();                 
                    minionDat.setMinionId(10288);
                    minionDat.setAmount(2);
                    npcTemplate.addRaidData(minionDat);       
                }
            
                L2Spawn npcSpawn = new L2Spawn(npcTemplate);
                npcSpawn.setLocx(184460);
                npcSpawn.setLocy(-12015);
                npcSpawn.setLocz(-5495);
                npcSpawn.setRespawnDelay(86400); // Respawns every 24 hours.
                npcSpawn.setAmount(1);
            
                SpawnTable.getInstance().addNewSpawn(npcSpawn, false);
                _signsNPCInsts.add(npcSpawn.doSpawn(true));
            
                if (Config.DEBUG)
                    _log.info("SevenSigns: Spawned " + bossName + " with her minions.");
            }
        }
        catch (Exception e) {
            _log.warning("SevenSigns: Error while spawning period NPCs: " + e);
            e.printStackTrace();
        }
        
        _initializedSpawns = true;
    } 
	
	/**
	 * Used to remove random spawns and auto-chats at the beginning of a new Quest Event period.
     * <BR>
     * Also hides all Preachers of Doom and Orators of Revelations.
	 */
	protected void removeSevenSignsNPCs()
	{
		// Remove the random auto spawns of Mammon NPCs
		AutoSpawnHandler.getInstance().removeSpawn(_merchantSpawn);
		AutoSpawnHandler.getInstance().removeSpawn(_blacksmithSpawn);
		
		// Remove the auto chats of Seven Signs NPCs and hide them
		AutoChatHandler.getInstance().removeChat(_preacherChat);
		AutoChatHandler.getInstance().removeChat(_oratorChat);
        for(AutoSpawnInstance temp:_preacherSpawns)
            AutoSpawnHandler.getInstance().removeSpawn(temp);
        for(AutoSpawnInstance temp:_oratorSpawns)
            AutoSpawnHandler.getInstance().removeSpawn(temp);
        
        // Remove all spawned NPCs and clean up the boss' minions.
		for (L2NpcInstance npcInst : _signsNPCInsts)
        {
            if (npcInst.getNpcId() == LILITH_NPC_ID || npcInst.getNpcId() == ANAKIM_NPC_ID) 
            {
                L2MonsterInstance monsterInst = (L2MonsterInstance)npcInst;
                
                for (L2MinionInstance minion : monsterInst.getSpawnedMinions())
                    minion.deleteMe();
            }
            
			npcInst.deleteMe();
        }
	}
	
    /**
     * Restores all Seven Signs data and settings, usually called at server startup.
     * 
     * @throws Exception
     */
    protected void restoreSevenSignsData() throws Exception
	{
		java.sql.Connection con = L2DatabaseFactory.getInstance().getConnection();
        
	    PreparedStatement statement = con.prepareStatement("SELECT char_obj_id, cabal, seal, red_stones, green_stones, blue_stones, " +
	    	"ancient_adena_amount, contribution_score FROM seven_signs");
	    ResultSet rset = statement.executeQuery();
		    
	    while (rset.next())
	    {
	    	int charObjId = rset.getInt("char_obj_id");
	    	
	    	StatsSet sevenDat = new StatsSet(); 
			sevenDat.set("char_obj_id", charObjId);
			sevenDat.set("cabal", rset.getString("cabal"));
			sevenDat.set("seal", rset.getInt("seal"));
			sevenDat.set("red_stones", rset.getInt("red_stones"));
			sevenDat.set("green_stones", rset.getInt("green_stones"));
			sevenDat.set("blue_stones", rset.getInt("blue_stones"));
			sevenDat.set("ancient_adena_amount", rset.getInt("ancient_adena_amount"));
			sevenDat.set("contribution_score", rset.getInt("contribution_score"));

			if (Config.DEBUG)
				_log.info("SevenSigns: Loaded data from DB for char ID " + charObjId + " (" + sevenDat.getString("cabal") + ")");
			
			_signsPlayerData.put(charObjId, sevenDat);
	    }
		    
	    rset.close();
		statement.close();	
		con.close();
		
		Properties SevenSignsProp = new Properties();
		InputStream is = SevenSigns.class.getResourceAsStream("/" + SEVEN_SIGNS_DATA_FILE);  
		SevenSignsProp.load(is);
		is.close();

		_currentCycle = Integer.parseInt(SevenSignsProp.getProperty("CurrentCycle", "1"));
		_activePeriod = Integer.parseInt(SevenSignsProp.getProperty("Period", String.valueOf(PERIOD_COMPETITION)));
		_previousWinner = Integer.parseInt(SevenSignsProp.getProperty("PreviousWinner", String.valueOf(CABAL_NULL)));		
		
		_dawnStoneScore = Integer.parseInt(SevenSignsProp.getProperty("DawnStoneScore", "0"));
        _dawnFestivalScore = Integer.parseInt(SevenSignsProp.getProperty("DawnFestivalScore", "0"));
        _duskStoneScore = Integer.parseInt(SevenSignsProp.getProperty("DuskStoneScore", "0"));
        _duskFestivalScore = Integer.parseInt(SevenSignsProp.getProperty("DuskFestivalScore", "0"));
 
		_signsSealOwners.put(SEAL_AVARICE, Integer.parseInt(SevenSignsProp.getProperty("AvariceOwner", String.valueOf(CABAL_NULL))));
		_signsSealOwners.put(SEAL_GNOSIS, Integer.parseInt(SevenSignsProp.getProperty("GnosisOwner", String.valueOf(CABAL_NULL))));
		_signsSealOwners.put(SEAL_STRIFE, Integer.parseInt(SevenSignsProp.getProperty("StrifeOwner", String.valueOf(CABAL_NULL))));
		
		_signsDawnSealTotals.put(SEAL_AVARICE, Integer.parseInt(SevenSignsProp.getProperty("AvariceDawnScore", "0")));
		_signsDawnSealTotals.put(SEAL_GNOSIS, Integer.parseInt(SevenSignsProp.getProperty("GnosisDawnScore", "0")));
		_signsDawnSealTotals.put(SEAL_STRIFE, Integer.parseInt(SevenSignsProp.getProperty("StrifeDawnScore", "0")));
		_signsDuskSealTotals.put(SEAL_AVARICE, Integer.parseInt(SevenSignsProp.getProperty("AvariceDuskScore", "0")));
		_signsDuskSealTotals.put(SEAL_GNOSIS, Integer.parseInt(SevenSignsProp.getProperty("GnosisDuskScore", "0")));
		_signsDuskSealTotals.put(SEAL_STRIFE, Integer.parseInt(SevenSignsProp.getProperty("StrifeDuskScore", "0")));
		
		// Festival data is loaded now after the Seven Signs engine data.
	}
	
	/**
     * Saves all Seven Signs data, both to the database and properties file (if updateSettings = True).
     * Often called to preserve data integrity and synchronization with DB, in case of errors.
     * <BR>
     * If player != null, just that player's data is updated in the database, otherwise all player's data is 
     * sequentially updated.
     * 
	 * @param player
	 * @param updateSettings
	 * @throws Exception
	 */
	public synchronized void saveSevenSignsData(L2PcInstance player, boolean updateSettings) throws Exception
	{
        if (Config.DEBUG)
            System.out.println("SevenSigns: Saving data to disk.");
        
		java.sql.Connection con = L2DatabaseFactory.getInstance().getConnection();
			
	    for (StatsSet sevenDat : _signsPlayerData.values())
	    {
	    	if (player != null)
	    		if (sevenDat.getInteger("char_obj_id") != player.getObjectId())
	    			continue;
	    	
	    	PreparedStatement statement = con.prepareStatement(
	    		"UPDATE seven_signs SET cabal=?, seal=?, red_stones=?, " +
	    		"green_stones=?, blue_stones=?, ancient_adena_amount=?, contribution_score=? WHERE char_obj_id=?");
	    	statement.setString(1, sevenDat.getString("cabal"));
	    	statement.setInt(2, sevenDat.getInteger("seal"));
	    	statement.setInt(3, sevenDat.getInteger("red_stones"));
	    	statement.setInt(4, sevenDat.getInteger("green_stones"));
	    	statement.setInt(5, sevenDat.getInteger("blue_stones"));
	    	statement.setInt(6, sevenDat.getInteger("ancient_adena_amount"));
	    	statement.setInt(7, sevenDat.getInteger("contribution_score"));
	    	statement.setInt(8, sevenDat.getInteger("char_obj_id"));
	    	statement.execute();
			statement.close();	
			
			if (Config.DEBUG)
				_log.info("SevenSigns: Updated data in DB for char ID " + sevenDat.getInteger("char_obj_id") + " (" + sevenDat.getString("cabal") + ")");
	    }
	    
	    con.close();
			
	    if (updateSettings) {
	    	Properties SevenSignsProp = new Properties();
	    	FileOutputStream fos = new FileOutputStream(new File(Config.DATAPACK_ROOT, SEVEN_SIGNS_DATA_FILE));
	    	
	    	SevenSignsProp.setProperty("CurrentCycle", String.valueOf(_currentCycle));
	    	SevenSignsProp.setProperty("Period", String.valueOf(_activePeriod));
	    	SevenSignsProp.setProperty("PreviousWinner", String.valueOf(_previousWinner));
			
	    	SevenSignsProp.setProperty("DawnStoneScore", String.valueOf(_dawnStoneScore));
			SevenSignsProp.setProperty("DuskStoneScore", String.valueOf(_duskStoneScore));
			SevenSignsProp.setProperty("DawnFestivalScore", String.valueOf(_dawnFestivalScore));
			SevenSignsProp.setProperty("DuskFestivalScore", String.valueOf(_duskFestivalScore));
						
			SevenSignsProp.setProperty("AvariceOwner", String.valueOf(_signsSealOwners.get(SEAL_AVARICE)));
			SevenSignsProp.setProperty("GnosisOwner", String.valueOf(_signsSealOwners.get(SEAL_GNOSIS)));
			SevenSignsProp.setProperty("StrifeOwner", String.valueOf(_signsSealOwners.get(SEAL_STRIFE)));
			
			SevenSignsProp.setProperty("AvariceDawnScore", String.valueOf(_signsDawnSealTotals.get(SEAL_AVARICE)));
			SevenSignsProp.setProperty("GnosisDawnScore", String.valueOf(_signsDawnSealTotals.get(SEAL_GNOSIS)));
			SevenSignsProp.setProperty("StrifeDawnScore", String.valueOf(_signsDawnSealTotals.get(SEAL_STRIFE)));
			SevenSignsProp.setProperty("AvariceDuskScore", String.valueOf(_signsDuskSealTotals.get(SEAL_AVARICE)));
			SevenSignsProp.setProperty("GnosisDuskScore", String.valueOf(_signsDuskSealTotals.get(SEAL_GNOSIS)));
			SevenSignsProp.setProperty("StrifeDuskScore", String.valueOf(_signsDuskSealTotals.get(SEAL_STRIFE)));
			
            // Festival data is now saved along with the rest of the Seven Signs settings.
			SevenSignsProp.setProperty("FestivalCycle", String.valueOf(SevenSignsFestival.getInstance().getCurrentFestivalCycle()));
            
            for (int i = 0; i < SevenSignsFestival.FESTIVAL_COUNT; i++) 
                SevenSignsProp.setProperty("AccumulatedBonus_" + i, String.valueOf(SevenSignsFestival.getInstance().getAccumulatedBonus(i)));
            
			SevenSignsProp.store(fos, "Seven Signs Properties");
			fos.close();
            
            if (Config.DEBUG)
                _log.info("SevenSigns: Updated data in signs.properties file.");
	    }
	}
	
    /**
     * Used to reset the cabal details of all players, and update the database.<BR>
     * Primarily used when beginning a new cycle, and should otherwise never be called.
     */
    protected void resetPlayerData()
	{
		String compWinner = "dawn";
		
		if (getCabalHighestScore() == CABAL_DUSK) 
			compWinner = "dusk";	
		
		if (Config.DEBUG)
			_log.info("SevenSigns: Resetting player data for new event period.");
		
		for (StatsSet sevenDat : _signsPlayerData.values()) 
		{
			int charObjId = sevenDat.getInteger("char_obj_id");
			
			// Reset seal stones for the winning cabal
			if (sevenDat.getString("cabal").equals(compWinner))
			{ 
				sevenDat.set("red_stones", 0);
				sevenDat.set("green_stones", 0);
				sevenDat.set("blue_stones", 0);
				sevenDat.set("contribution_score", 0);
			}
			
			// Reset the player's cabal and seal information
			sevenDat.set("cabal", "");
			sevenDat.set("seal", SEAL_NULL);
			_signsPlayerData.put(charObjId, sevenDat);
		}
		
        // A database update should soon follow this!
	}
	
	/**
     * Tests whether the specified player has joined a cabal in the past.
     * 
	 * @param player
	 * @return boolean hasRegistered
	 */
	private boolean hasRegisteredBefore(L2PcInstance player) {
		return _signsPlayerData.containsKey(player.getObjectId());
	}
	
		
	/**
     * Used to specify cabal-related details for the specified player. This method
     * checks to see if the player has registered before and will update the database
     * if necessary.
     * <BR>
     * Returns the cabal ID the player has joined.
     * 
	 * @param player
	 * @param chosenCabal
	 * @param chosenSeal
	 * @return int cabal
	 */
	public int setPlayerInfo(L2PcInstance player, int chosenCabal, int chosenSeal)
	{
		final int charObjId = player.getObjectId();
				
		if (hasRegisteredBefore(player)) {
			final int oldCabal = getPlayerCabal(player);
			
			if (oldCabal == chosenCabal)
				return oldCabal;

			// If the seal validation period has passed, 
			// cabal information was removed and so "re-register" player 
			StatsSet currPlayer = _signsPlayerData.get(charObjId);
			currPlayer.set("cabal", getCabalShortName(chosenCabal));
			currPlayer.set("seal", chosenSeal);
			
			_signsPlayerData.put(charObjId, currPlayer);
            
            try {
                saveSevenSignsData(player, false);
            }
            catch (Exception e) {
                _log.severe("SevenSigns: Failed to save configuration: " + e);
            }
		}
		else {
			StatsSet sevenDat = new StatsSet(); 
			sevenDat.set("char_obj_id", charObjId);
			sevenDat.set("cabal", getCabalShortName(chosenCabal));
			sevenDat.set("seal", chosenSeal);
			sevenDat.set("red_stones", 0);
			sevenDat.set("green_stones", 0);
			sevenDat.set("blue_stones", 0);
			sevenDat.set("ancient_adena_amount", 0);
			sevenDat.set("contribution_score", 0);

			_signsPlayerData.put(charObjId, sevenDat);
			
			// Update data in database, as we have a new player signing up.
			java.sql.Connection con = null;
			
			try {
				con = L2DatabaseFactory.getInstance().getConnection();
				
				PreparedStatement statement = con.prepareStatement(
		    		"INSERT INTO seven_signs (char_obj_id, cabal, seal, red_stones, green_stones, blue_stones, " +
					"ancient_adena_amount, contribution_score) VALUES (?,?,?,0,0,0,0,0)");
				statement.setInt(1, charObjId);
				statement.setString(2, getCabalShortName(chosenCabal));
				statement.setInt(3, chosenSeal);
				statement.execute();
				statement.close();	
				
				con.close();
			}
			catch (Exception e) {
				_log.severe("SevenSigns: Failed to save data: " + e);
			}
			
			if (Config.DEBUG)
				_log.info("SevenSigns: Inserted data in DB for char ID " + sevenDat.getInteger("char_obj_id") + " (" + sevenDat.getString("cabal") + ")");
		}
		
		if (Config.DEBUG) 
			_log.info("SevenSigns: " + player.getName() + " has joined the " + getCabalName(chosenCabal) + " for the " + getSealName(chosenSeal, false) + "!");
		
		return chosenCabal;
	}
	
	/**
     * Returns the amount of ancient adena the specified player can claim, if any.<BR>
     * If removeReward = True, all the ancient adena owed to them is removed, then 
     * DB is updated.
     * 
	 * @param player
	 * @param removeReward
	 * @return int rewardAmount
	 */
	public int getAncientAdenaReward(L2PcInstance player, boolean removeReward) 
    {
		int charObjId = player.getObjectId();
		StatsSet currPlayer = _signsPlayerData.get(charObjId);
		
		int rewardAmount = currPlayer.getInteger("ancient_adena_amount");
		
		if (removeReward) {
			currPlayer.set("ancient_adena_amount", 0);
			_signsPlayerData.put(charObjId, currPlayer);
            
            try {
                saveSevenSignsData(player, false);
            }
            catch (Exception e) {
                _log.severe("Failed to save Seven Signs configuration: " + e);
            }
		}
		
		return rewardAmount;
	}
	
	/**
     * Used to add the specified player's seal stone contribution points 
     * to the current total for their cabal. Returns the point score the 
     * contribution was worth.
     * 
     * Each stone count <B>must be</B> broken down and specified by the stone's color.
     * 
	 * @param player
	 * @param blueCount
	 * @param greenCount
	 * @param redCount
	 * @return int contribScore
	 */
	public int addPlayerStoneContrib(L2PcInstance player, int blueCount, int greenCount, int redCount)
	{
		int charObjId = player.getObjectId();
		StatsSet currPlayer = _signsPlayerData.get(charObjId);
		
		int contribScore = calcContributionScore(blueCount, greenCount, redCount);
		int totalAncientAdena = currPlayer.getInteger("ancient_adena_amount") + calcAncientAdenaReward(blueCount, greenCount, redCount);
		int totalContribScore = currPlayer.getInteger("contribution_score") + contribScore; 
		
		if (totalContribScore > MAXIMUM_PLAYER_CONTRIB)
			return -1;
		
		currPlayer.set("red_stones", currPlayer.getInteger("red_stones") + redCount);
		currPlayer.set("green_stones", currPlayer.getInteger("green_stones") + greenCount);
		currPlayer.set("blue_stones", currPlayer.getInteger("blue_stones") + blueCount);
		currPlayer.set("ancient_adena_amount", totalAncientAdena);
		currPlayer.set("contribution_score", totalContribScore);
		_signsPlayerData.put(charObjId, currPlayer);
		
		if (currPlayer.getString("cabal").equals("dawn")) 
			_dawnStoneScore += contribScore;
		else
			_duskStoneScore += contribScore;
		
		try {
			saveSevenSignsData(player, true);
		}
		catch (Exception e) {
			_log.severe("Failed to save Seven Signs configuration: " + e);
		}
		
		if (Config.DEBUG)
			_log.info("SevenSigns: " + player.getName() + " contributed " + contribScore + " seal stone points to their cabal.");
		
		return contribScore;
	}
	
	/**
     * Adds the specified number of festival points to the specified cabal. 
     * Remember, the same number of points are <B>deducted from the rival cabal</B> 
     * to maintain proportionality.
     * 
	 * @param cabal
	 * @param amount
	 */
	public void addFestivalScore(int cabal, int amount) 
    {
		if (cabal == CABAL_DUSK) {
			_duskFestivalScore += amount;
            
            // To prevent negative scores!
            if (_dawnFestivalScore >= amount)
                _dawnFestivalScore -= amount;
        }
		else {
			_dawnFestivalScore += amount;
            
            if (_duskFestivalScore >= amount)
                _duskFestivalScore -= amount;        
        }
	}
	
	/**
     * Send info on the current Seven Signs period to the specified player.
     * 
	 * @param player
	 */
	public void sendCurrentPeriodMsg(L2PcInstance player)
	{
        SystemMessage sm = null;
		
    	switch (_activePeriod) {
        case PERIOD_COMP_RECRUITING:
        	sm = new SystemMessage(SystemMessage.SEVEN_SIGNS_PREPARATIONS_HAVE_BEGUN_FOR_THE_NEXT_QUEST_EVENT);
        	break;
        case PERIOD_COMPETITION:
        	sm = new SystemMessage(SystemMessage.SEVEN_SIGNS_THE_QUEST_EVENT_PERIOD_HAS_BEGUN_SPEAK_WITH_A_PRIEST_OF_DAWN_OR_DUSK_PRIESTESS_IF_YOU_WISH_TO_PARTICIPATE_IN_THE_EVENT);
        	break;
        case PERIOD_COMP_RESULTS:
        	sm = new SystemMessage(SystemMessage.SEVEN_SIGNS_QUEST_EVENT_HAS_ENDED_RESULTS_ARE_BEING_TALLIED);
        	break;
        case PERIOD_SEAL_VALIDATION:
        	sm = new SystemMessage(SystemMessage.SEVEN_SIGNS_THIS_IS_THE_SEAL_VALIDATION_PERIOD_A_NEW_QUEST_EVENT_PERIOD_BEGINS_NEXT_MONDAY);
        	break;
        }	
    	
   		player.sendPacket(sm);
	}
	
	/**
     * Sends the built-in system message specified by sysMsgId to all online players.
     * 
	 * @param sysMsgId
	 */
	public void sendMessageToAll(int sysMsgId)
	{
        SystemMessage sm = new SystemMessage(sysMsgId);
        
		for (L2PcInstance player : L2World.getAllPlayers())
			player.sendPacket(sm);
	}
	
    /**
     * Used to initialize the seals for each cabal. (Used at startup or at beginning of a new cycle).
     * This method should  be called after <B>resetSeals()</B> and <B>calcNewSealOwners()</B> on a new cycle.
     */
    protected void initializeSeals()
	{
		for (Integer currSeal : _signsSealOwners.keySet())
		{
			int sealOwner = _signsSealOwners.get(currSeal);
			
			if (sealOwner != CABAL_NULL)
				if (isSealValidationPeriod())
					_log.info("SevenSigns: The " + getCabalName(sealOwner) + " have won the " + getSealName(currSeal, false) + ".");
				else
					_log.info("SevenSigns: The " + getSealName(currSeal, false) + " is currently owned by " + getCabalName(sealOwner) + ".");
			else
				_log.info("SevenSigns: The " + getSealName(currSeal, false) + " remains unclaimed.");
		}
	}
	
    /**
     * Only really used at the beginning of a new cycle, this method resets all seal-related data.
     */
    protected void resetSeals()
	{
		_signsDawnSealTotals.put(SEAL_AVARICE, CABAL_NULL);
		_signsDawnSealTotals.put(SEAL_GNOSIS, CABAL_NULL);
		_signsDawnSealTotals.put(SEAL_STRIFE, CABAL_NULL);
		_signsDuskSealTotals.put(SEAL_AVARICE, CABAL_NULL);
		_signsDuskSealTotals.put(SEAL_GNOSIS, CABAL_NULL);
		_signsDuskSealTotals.put(SEAL_STRIFE, CABAL_NULL);
	}
	
    /**
     * Calculates the ownership of the three Seals of the Seven Signs, 
     * based on various criterion.
     * <BR><BR>
     * Should only ever called at the beginning of a new cycle.
     */
    protected void calcNewSealOwners()
	{
		for (StatsSet sevenDat : _signsPlayerData.values())
		{
			int playerSeal = sevenDat.getInteger("seal");
			
			if (playerSeal == SEAL_NULL)
				continue;
			
			if (sevenDat.getString("cabal").equals("dawn")) 
				_signsDawnSealTotals.put(playerSeal, _signsDawnSealTotals.get(playerSeal) + 1);
			else if (sevenDat.getString("cabal").equals("dusk"))
				_signsDuskSealTotals.put(playerSeal, _signsDuskSealTotals.get(playerSeal) + 1);
			else
				continue;
		}
		
		if (Config.DEBUG) {
			_log.info("SevenSigns: (Avarice) Dawn = " + _signsDawnSealTotals.get(SEAL_AVARICE) + ", Dusk = " + _signsDuskSealTotals.get(SEAL_AVARICE));
			_log.info("SevenSigns: (Gnosis) Dawn = " + _signsDawnSealTotals.get(SEAL_GNOSIS) + ", Dusk = " + _signsDuskSealTotals.get(SEAL_GNOSIS));
			_log.info("SevenSigns: (Strife) Dawn = " + _signsDawnSealTotals.get(SEAL_STRIFE) + ", Dusk = " + _signsDuskSealTotals.get(SEAL_STRIFE));
		}
		
		for (Integer currSeal : _signsDawnSealTotals.keySet()) 
		{
			int prevSealOwner = _signsSealOwners.get(currSeal);
			int newSealOwner;
			
			int winningCabalSealScore = 0;
			int totalWinningCabalMembers = 0;
			
			/*
			 * - If a Seal was already closed or owned by the opponent and the new winner wants
			 *   to assume ownership of the Seal, 35% or more of the members of the Cabal must 
			 *   have chosen the Seal. If they chose less than 35%, they cannot own the Seal. 
			 * 
			 * - If the Seal was owned by the winner in the previous Seven Signs, they can retain 
			 *   that seal if 10% or more members have chosen it. If they want to possess a new Seal, 
			 *   at least 35% of the members of the Cabal must have chosen the new Seal. 
			 */
			if (prevSealOwner != getCabalHighestScore())
			{
				totalWinningCabalMembers = getTotalMembers(getCabalHighestScore());
				
				if (getCabalHighestScore() == CABAL_DAWN)
					winningCabalSealScore = _signsDawnSealTotals.get(currSeal);
				else
					winningCabalSealScore = _signsDuskSealTotals.get(currSeal);
					
				if (winningCabalSealScore > Math.round((float)0.35 * totalWinningCabalMembers)) 
					newSealOwner = getCabalHighestScore();
				else 
					newSealOwner = prevSealOwner;
			}
			else
			{
				totalWinningCabalMembers = getTotalMembers(prevSealOwner);
				
				if (prevSealOwner == CABAL_DAWN)
					winningCabalSealScore = _signsDawnSealTotals.get(currSeal);
				else
					winningCabalSealScore = _signsDuskSealTotals.get(currSeal);

				if (winningCabalSealScore > Math.round((float)0.10 * totalWinningCabalMembers))
					newSealOwner = prevSealOwner;
				else
					newSealOwner = CABAL_NULL;
			}
			
			_signsSealOwners.put(currSeal, newSealOwner);
			
			// Alert all online players to new seal status.
			switch (currSeal) {
			case SEAL_AVARICE:
				if(newSealOwner == CABAL_DAWN)
					sendMessageToAll(SystemMessage.SEVEN_SIGNS_THE_LORDS_OF_DAWN_HAVE_OBTAINED_THE_SEAL_OF_AVARICE);
				else if(newSealOwner == CABAL_DUSK)
					sendMessageToAll(SystemMessage.SEVEN_SIGNS_THE_REVOLUTIONARIES_OF_DUSK_HAVE_OBTAINED_THE_SEAL_OF_AVARICE);
				break;
			case SEAL_GNOSIS:
				if(newSealOwner == CABAL_DAWN)
					sendMessageToAll(SystemMessage.SEVEN_SIGNS_THE_LORDS_OF_DAWN_HAVE_OBTAINED_THE_SEAL_OF_GNOSIS);
				else if(newSealOwner == CABAL_DUSK)
					sendMessageToAll(SystemMessage.SEVEN_SIGNS_THE_REVOLUTIONARIES_OF_DUSK_HAVE_OBTAINED_THE_SEAL_OF_GNOSIS);
				break;
			case SEAL_STRIFE:
				if(newSealOwner == CABAL_DAWN)
					sendMessageToAll(SystemMessage.SEVEN_SIGNS_THE_LORDS_OF_DAWN_HAVE_OBTAINED_THE_SEAL_OF_STRIFE);
				else if(newSealOwner == CABAL_DUSK)
					sendMessageToAll(SystemMessage.SEVEN_SIGNS_THE_REVOLUTIONARIES_OF_DUSK_HAVE_OBTAINED_THE_SEAL_OF_STRIFE);
				break;
			}
		}
	}
	
	/**
     * This method is called to remove all players from catacombs and 
     * necropolises, who belong to the losing cabal.
     * <BR><BR>
     * Should only ever called at the beginning of Seal Validation.
     */
    @SuppressWarnings("static-access")
	private void teleLosingCabalFromDungeons()
    {
        for (L2PcInstance onlinePlayer : L2World.getInstance().getAllPlayers())
            if (onlinePlayer.getZ() < -4700) 
            {
                Location nearestTown = MapRegionTable.getInstance().getTeleToLocation(onlinePlayer, MapRegionTable.TeleportWhereType.ClosestTown);

                MapRegionTable.getInstance().teleToLocation(onlinePlayer, nearestTown);
                onlinePlayer.sendMessage("You have been teleported to the nearest town due to the beginning of the Seal Validation period.");
            }
    }
    
	/**
     * The primary controller of period change of the Seven Signs system.
     * This runs all related tasks depending on the period that is about to begin.
     * 
	 * @author Tempy	 
	 */
	private class SevenSignsPeriodChange implements Runnable
	{
        public void run() 
        {
            /* 
             * Remember the period check here refers to the period just ENDED!
			*/
            final int periodEnded = _activePeriod;
            _activePeriod++;
            
	        switch (periodEnded) { 
	            case PERIOD_COMP_RECRUITING: // Initialization
                    SevenSignsFestival.getInstance().startFestivalManager();                
                    sendMessageToAll(SystemMessage.SEVEN_SIGNS_THE_QUEST_EVENT_PERIOD_HAS_BEGUN_VISIT_A_PRIEST_OF_DAWN_OR_DUSK_TO_PARTICIPATE_IN_THE_EVENT);
				break;
	            case PERIOD_COMPETITION: // Results Calculation
				
				sendMessageToAll(SystemMessage.SEVEN_SIGNS_THE_QUEST_EVENT_PERIOD_HAS_ENDED_THE_NEXT_QUEST_EVENT_WILL_START_IN_ONE_WEEK);
				
				int compWinner = getCabalHighestScore();				
				
                // Schedule a stop of the festival engine.
                SevenSignsFestival.getInstance().getFestivalManagerSchedule().cancel(false);
                
				resetSeals();
				calcNewSealOwners();
								
				if (compWinner == CABAL_DUSK)
                        sendMessageToAll(SystemMessage.SEVEN_SIGNS_THE_REVOLUTIONARIES_OF_DUSK_HAVE_WON);
				else 
                        sendMessageToAll(SystemMessage.SEVEN_SIGNS_THE_LORDS_OF_DAWN_HAVE_WON);

                    _previousWinner = compWinner;
	                break;
	            case PERIOD_COMP_RESULTS: // Seal Validation
				
                    // Perform initial Seal Validation set up.
                    initializeSeals();
				spawnSevenSignsNPCs();
                    teleLosingCabalFromDungeons();

				// Send message that Seal Validation has begun.
				sendMessageToAll(1218);
                    
                    _log.info("SevenSigns: The " + getCabalName(_previousWinner) + " have won the competition with " + getCurrentScore(_previousWinner) + " points!");
				break;
	            case PERIOD_SEAL_VALIDATION: // Reset for New Cycle
				
	                // Ensure a cycle restart when this period ends.
                    _activePeriod = PERIOD_COMP_RECRUITING;
				
				// Send message that Seal Validation has ended.
				sendMessageToAll(SystemMessage.SEVEN_SIGNS_THE_SEAL_VALIDATION_PERIOD_HAS_ENDED);
				
				// Reset all data
				resetPlayerData();

                    // Reset all Festival-related data and remove any unused blood offerings.
                    // NOTE: A full update of Festival data in the database is also performed.
                    SevenSignsFestival.getInstance().resetFestivalData(false);
	                
				_dawnStoneScore = 0;
				_duskStoneScore = 0;

				_dawnFestivalScore = 0;
				_duskFestivalScore = 0;

                removeSevenSignsNPCs();				
				_currentCycle++;
				break;
			}
			
            // Make sure all Seven Signs data is saved for future use.
            try {
                saveSevenSignsData(null, true);
            }
            catch (Exception e) {
                _log.severe("SevenSigns: Failed to save configuration: " + e);
            }
            
            SignsSky ss = new SignsSky();
            
			for (L2PcInstance player : L2World.getAllPlayers())
				player.sendPacket(ss);
				
			_log.info("SevenSigns: The " + getCurrentPeriodName() + " period has begun!");
	        
            setCalendarForNextPeriodChange();
            
	        ClientScheduler.getInstance().scheduleMed(new SevenSignsPeriodChange(), getMilliToPeriodChange());
		}
	}
}
