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
package net.sf.l2j;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

/**
 * This class containce global server configuration.
 * It has static final fields initialized from configuration files.
 * It's initialized at the very begin of startup, and later JIT will optimize
 * away debug/unused code.
 * 
 * @author mkizub
 */
public final class Config {

	/** Debug/release mode */
	public static final boolean DEBUG;
	/** Enable/disable assertions */
	public static final boolean ASSERT;
	/** Enable/disable code 'in progress' */
	public static final boolean DEVELOPER;
	/** Global chat state */
	public static final String DEFAULT_GLOBAL_CHAT;
	/** For test servers - evrybody has admin rights */
	public static final boolean EVERYBODY_HAS_ADMIN_RIGHTS;
	/** Alternative game crafting */
	public static final boolean ALT_GAME_CREATION;
	/** Alternative game skill learning */
	public static final boolean ALT_GAME_SKILL_LEARN;
	/** Cancel attack/cast by hit */
	public static final boolean ALT_GAME_CANCEL_ALL;
	public static final boolean ALT_GAME_CANCEL_BOW;
	public static final boolean ALT_GAME_CANCEL_CAST;
	
	/** Alternative game - use tiredness, instead of CP */
	public static final boolean ALT_GAME_TIREDNESS;
	
	/** Alternative shield defence */
	public static final boolean ALT_GAME_SHIELD_BLOCKS;
	
	/** Alternative gameing - loss of XP on death */
	public static final boolean ALT_GAME_DELEVEL;
	
	/** Spell Book needed to learn skill */
	public static final boolean SP_BOOK_NEEDED;
	/** Logging Chat Window */
	public static final boolean LOG_CHAT;
	/**      GM CONFIG         *
	 ** General GM AccessLevel */
	public static final int GM_ACCESSLEVEL;
	/** General GM Minimal AccessLevel */
	public static final int GM_MIN;
	/** General GM AccessLevel to change announcements */
	public static final int GM_ANNOUNCE;
	/** General GM AccessLevel can /ban /unban */
	public static final int GM_BAN;
	/** General GM AccessLevel can /create_item and /gmshop */
	public static final int GM_CREATE_ITEM;
	/** General GM AccessLevel can /delete */
	public static final int GM_DELETE;
	/** General GM AccessLevel can /kick /disconnect*/
	public static final int GM_KICK;
	/** General GM AccessLevel for access to GMMenu*/
	public static final int GM_MENU;
	/** General GM AccessLevel tu use god mode command */
	public static final int GM_GODMODE;
	/** General GM AccessLevel with character edit rights */
	public static final int GM_CHAR_EDIT;
	/** General GM AccessLevel with NPC edit rights */
	public static final int GM_NPC_EDIT;
	/** General GM AccessLevel to teleport to any location */
	public static final int GM_TELEPORT;
	/** General GM AccessLevel to restart server*/
	public static final int GM_RESTART;
	/** General GM AccessLevel for MonsterRace */
	public static final int GM_MONSTERRACE;
	/** General GM AccessLevel to ride Wyvern */
	public static final int GM_RIDER;
	/** General GM AccessLevel to unstuck without 5min delay */
	public static final int GM_ESCAPE;
	/** General GM AccessLevel to resurect fixed after death */
	public static final int GM_FIXED;
	
	/** Rate control */
	public static final float RATE_XP;
	public static final float RATE_SP;
	public static final float RATE_DROP_ADENA;
	public static final float RATE_DROP_ITEMS;
	public static final float RATE_DROP_SPOIL;
	public static final float ALT_GAME_EXPONENT_XP;
	public static final float ALT_GAME_EXPONENT_SP;

	// Pets
	public static final int WYVERN_SPEED;
	public static final int STRIDER_SPEED;
	
	/** Datapack root directory */
	public static final File DATAPACK_ROOT;
    public static final Boolean ALLOW_WAREHOUSE;

    // protocol revision
    public static final int MIN_PROTOCOL_REVISION;
    public static final int MAX_PROTOCOL_REVISION;
    
	static {
		try {
			Properties serverSettings = new Properties();
			InputStream is = Config.class.getResourceAsStream("/server.cfg");  
			serverSettings.load(is);
			is.close();
	
			String debugo = serverSettings.getProperty("Debug", "false");
			DEBUG = Boolean.parseBoolean(debugo);
			
			String devel = serverSettings.getProperty("Developer", "false");
			DEVELOPER = Boolean.parseBoolean(devel);
			
			String asserts = serverSettings.getProperty("Assert", "false");
			ASSERT = Boolean.parseBoolean(asserts);
			
			DEFAULT_GLOBAL_CHAT = serverSettings.getProperty("GlobalChat", "ON");

			String altgame;
			
			altgame = serverSettings.getProperty("AltGameTiredness", "false");
			ALT_GAME_TIREDNESS = Boolean.parseBoolean(altgame);
			
			altgame = serverSettings.getProperty("AltGameCreation", "false");
			ALT_GAME_CREATION = Boolean.parseBoolean(altgame);
			
			altgame = serverSettings.getProperty("AltGameSkillLearn", "false");
			ALT_GAME_SKILL_LEARN = Boolean.parseBoolean(altgame);
			
			altgame = serverSettings.getProperty("AltGameCancelByHit", "");
			ALT_GAME_CANCEL_ALL  = altgame.indexOf("all")  >= 0;
			ALT_GAME_CANCEL_BOW  = altgame.indexOf("bow")  >= 0;
			ALT_GAME_CANCEL_CAST = altgame.indexOf("cast") >= 0;
			
			altgame = serverSettings.getProperty("AltShieldBlocks", "true");
			ALT_GAME_SHIELD_BLOCKS = Boolean.parseBoolean(altgame);
			
			altgame = serverSettings.getProperty("Delevel", "true");
			ALT_GAME_DELEVEL = Boolean.parseBoolean(altgame);
			
			String spellbook = serverSettings.getProperty("SpBookNeeded", "true");
			SP_BOOK_NEEDED = Boolean.parseBoolean(spellbook);
			
			String rights = serverSettings.getProperty("EverybodyHasAdminRights", "false");
			EVERYBODY_HAS_ADMIN_RIGHTS = Boolean.parseBoolean(rights);
			
			
			
			String logchat = serverSettings.getProperty("LogChat", "false");
			LOG_CHAT = Boolean.valueOf(logchat);
			
			GM_ACCESSLEVEL = Integer.parseInt(serverSettings.getProperty("GMAccessLevel", "100"));
			GM_MIN = Integer.parseInt(serverSettings.getProperty("GMMinLevel", "100"));
			GM_ANNOUNCE = Integer.parseInt(serverSettings.getProperty("GMCanAnnounce", "100"));
			GM_BAN = Integer.parseInt(serverSettings.getProperty("GMCanBan", "100"));
			GM_CREATE_ITEM = Integer.parseInt(serverSettings.getProperty("GMCanShop", "100"));
			GM_DELETE = Integer.parseInt(serverSettings.getProperty("GMCanDelete", "100"));
			GM_KICK = Integer.parseInt(serverSettings.getProperty("GMCanKick", "100"));
			GM_MENU = Integer.parseInt(serverSettings.getProperty("GMMenu", "100"));
			GM_GODMODE = Integer.parseInt(serverSettings.getProperty("GMGodMode", "100"));
			GM_CHAR_EDIT = Integer.parseInt(serverSettings.getProperty("GMCanEditChar", "100"));
			GM_NPC_EDIT = Integer.parseInt(serverSettings.getProperty("GMCanEditNPC", "100"));
			GM_TELEPORT = Integer.parseInt(serverSettings.getProperty("GMCanTeleport", "100"));
			GM_RESTART = Integer.parseInt(serverSettings.getProperty("GMCanRestart", "100"));
			GM_MONSTERRACE = Integer.parseInt(serverSettings.getProperty("GMMonsterRace", "100"));
			GM_RIDER = Integer.parseInt(serverSettings.getProperty("GMRider", "100"));
			GM_ESCAPE = Integer.parseInt(serverSettings.getProperty("GMFastUnstuck", "100"));
			GM_FIXED = Integer.parseInt(serverSettings.getProperty("GMResurectFixed", "100"));

			
			
			RATE_XP = Float.parseFloat(serverSettings.getProperty("RateXp", "1."));
			RATE_SP = Float.parseFloat(serverSettings.getProperty("RateSp", "1."));
			RATE_DROP_ADENA = Float.parseFloat(serverSettings.getProperty("RateDropAdena", "1."));
			RATE_DROP_ITEMS = Float.parseFloat(serverSettings.getProperty("RateDropItems", "1."));
			RATE_DROP_SPOIL = Float.parseFloat(serverSettings.getProperty("RateDropSpoil", "1."));
			ALT_GAME_EXPONENT_XP = Float.parseFloat(serverSettings.getProperty("AltGameExponentXp", "0."));
			ALT_GAME_EXPONENT_SP = Float.parseFloat(serverSettings.getProperty("AltGameExponentSp", "0."));
			
			String datapackRoot = serverSettings.getProperty("DatapackRoot", ".");
			DATAPACK_ROOT = new File(datapackRoot).getCanonicalFile();
            
            ALLOW_WAREHOUSE   = Boolean.valueOf(serverSettings.getProperty("AllowWarehouse", "False"));

            WYVERN_SPEED =    Integer.parseInt(serverSettings.getProperty("WyvernSpeed", "400"));         
            STRIDER_SPEED =    Integer.parseInt(serverSettings.getProperty("StriderSpeed", "250")); 
			
            MIN_PROTOCOL_REVISION = Integer.parseInt(serverSettings.getProperty("MinProtocolRevision", "551"));
            MAX_PROTOCOL_REVISION = Integer.parseInt(serverSettings.getProperty("MaxProtocolRevision", "553"));
            if (MIN_PROTOCOL_REVISION > MAX_PROTOCOL_REVISION)
            	throw new Error("MinProtocolRevision is bigger than MaxProtocolRevision in server configuration file.");

		} catch (Exception e) {
			e.printStackTrace();
			throw new Error("Failed to load configuration");
		}
		 
	}
	
	
	// it has no instancies
	private Config() {}
	
}
