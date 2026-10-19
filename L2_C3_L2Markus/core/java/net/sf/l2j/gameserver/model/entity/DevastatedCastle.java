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

/*
 *  Author: Qwerty, Scoria dev.
 *  v 2.2
 */

package net.sf.l2j.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import javolution.util.FastMap;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class DevastatedCastle
{
	private static final Logger _log = Logger.getLogger(DevastatedCastle.class.getName());
	private static DevastatedCastle _instance;
	private FastMap<Integer, DamageInfo> _clansDamageInfo;

	private static int START_DAY = 1;
	private static int HOUR = Config.DEVASTATED_HOUR;
	private static int MINUTES = Config.DEVASTATED_MINUTES;

	private static final int BOSS_ID = 12534; //Gustav    @Boss@
	private static final int BOSS1_ID = 12532; //Dietrich  @Minion@
	private static final int BOSS2_ID = 12533; //Mikhail   @Minion@
	private static final int MESSENGER_ID = 8057;

	private ScheduledFuture<?> _gustav;
	private ScheduledFuture<?> _dietrich;
	private ScheduledFuture<?> _mikhail;
	private ScheduledFuture<?> _monsterdespawn;

	private L2NpcInstance _minion1 = null;
	private L2NpcInstance _minion2 = null;

	private ArrayList<MonsterLocation> _monsters = new ArrayList<MonsterLocation>();
	private ArrayList<L2Spawn> _spawns = new ArrayList<L2Spawn>();

	private Calendar _siegetime = Calendar.getInstance();

	public boolean _progress = false;

	public static DevastatedCastle getInstance()
	{
		if(_instance == null)
		{
			_instance = new DevastatedCastle();
		}
		return _instance;
	}

	private class DamageInfo
	{
		public L2Clan _clan;
		public long _damage;
	}

	private DevastatedCastle()
	{
		if(Config.DEVASTATED_DAY == 1)
		{
			START_DAY = Calendar.MONDAY;
		}
		else if(Config.DEVASTATED_DAY == 2)
		{
			START_DAY = Calendar.TUESDAY;
		}
		else if(Config.DEVASTATED_DAY == 3)
		{
			START_DAY = Calendar.WEDNESDAY;
		}
		else if(Config.DEVASTATED_DAY == 4)
		{
			START_DAY = Calendar.THURSDAY;
		}
		else if(Config.DEVASTATED_DAY == 5)
		{
			START_DAY = Calendar.FRIDAY;
		}
		else if(Config.DEVASTATED_DAY == 6)
		{
			START_DAY = Calendar.SATURDAY;
		}
		else if(Config.DEVASTATED_DAY == 7)
		{
			START_DAY = Calendar.SUNDAY;
		}
		else
		{
			START_DAY = Calendar.MONDAY;
		}

		if(HOUR < 0 || HOUR > 23)
		{
			HOUR = 18;
		}
		if(MINUTES < 0 || MINUTES > 59)
		{
			MINUTES = 0;
		}

		_clansDamageInfo = new FastMap<Integer, DamageInfo>();

		synchronized (this)
		{
			setCalendarForNextSiege();
			long milliToSiege = getMilliToSiege();

			RunMessengerSpawn rms = new RunMessengerSpawn();
			ThreadPoolManager.getInstance().scheduleGeneral(rms, milliToSiege);

			long total_millis = System.currentTimeMillis() + milliToSiege;

			GregorianCalendar cal = (GregorianCalendar) Calendar.getInstance();
			cal.setTimeInMillis(total_millis);
			String next_ch_siege_date = DateFormat.getInstance().format(cal.getTime());

			_log.info("Devastated Castle: siege will start the "+next_ch_siege_date);
			rms = null;
		}
	}

	private void setCalendarForNextSiege()
	{
		int daysToChange = getDaysToSiege();

		if(daysToChange == 7)
			if(_siegetime.get(Calendar.HOUR_OF_DAY) < HOUR)
			{
				daysToChange = 0;
			}
			else if(_siegetime.get(Calendar.HOUR_OF_DAY) == HOUR && _siegetime.get(Calendar.MINUTE) < MINUTES)
			{
				daysToChange = 0;
			}

		if(daysToChange > 0)
		{
			_siegetime.add(Calendar.DATE, daysToChange);
		}

		_siegetime.set(Calendar.HOUR_OF_DAY, HOUR);
		_siegetime.set(Calendar.MINUTE, MINUTES);
	}

	private int getDaysToSiege()
	{
		int numDays = _siegetime.get(Calendar.DAY_OF_WEEK) - START_DAY;

		if(numDays < 0)
			return 0 - numDays;

		return 7 - numDays;
	}

	private long getMilliToSiege()
	{
		long currTimeMillis = System.currentTimeMillis();
		long siegeTimeMillis = _siegetime.getTimeInMillis();

		return siegeTimeMillis - currTimeMillis;
	}

	protected class RunMessengerSpawn implements Runnable
	{
		@Override
		public void run()
		{
			MessengerSpawn();
		}
	}

	public void MessengerSpawn()
	{
		if(!ClanHallManager.getInstance().isFree(34))
		{
			ClanHallManager.getInstance().setFree(34);
		}

		Announce("Siege registration of Devastated castle has begun!");
		Announce("Now its open for 2 hours!");

		L2NpcInstance result = null;
		try
		{
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(MESSENGER_ID);

			L2Spawn spawn = new L2Spawn(template);
			spawn.setLocx(179040);
			spawn.setLocy(-13717);
			spawn.setLocz(-2263);
			spawn.stopRespawn();
			result = spawn.spawnOne();
			template = null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		RunSiege rs = new RunSiege();
		ThreadPoolManager.getInstance().scheduleGeneral(rs, 14400000); //4 * 60 * 60 * 1000

		ThreadPoolManager.getInstance().scheduleGeneral(new DeSpawnTimer(result), 7200000); //2 * 60 * 60 * 1000
		ThreadPoolManager.getInstance().scheduleGeneral(new AnnounceInfo("Siege registration of Devastated castle is over!"), 7200000);
		ThreadPoolManager.getInstance().scheduleGeneral(new AnnounceInfo("2 hours until siege begin."), 7200000);

		result = null;
		rs = null;
	}

	protected class AnnounceInfo implements Runnable
	{
		String _message;

		public AnnounceInfo(String message)
		{
			_message = message;
		}

		@Override
		public void run()
		{
			Announce(_message);
		}
	}

	public void Announce(String message)
	{
		Announcements.getInstance().announceToAll(message);
	}

	protected class RunSiege implements Runnable
	{
		@Override
		public void run()
		{
			Siege();
		}
	}

	public void Siege()
	{
		L2NpcInstance result = null;
		L2NpcTemplate template = null;
		L2Spawn spawn = null;

		ClanHall CH = ClanHallManager.getInstance().getClanHallById(34);
		CH.banishForeigners();
		CH.spawnDoor();

		setIsInProgress(true);

		try
		{
			fillMonsters();

			template = NpcTable.getInstance().getTemplate(BOSS_ID);
			spawn = new L2Spawn(template);
			spawn.setLocx(178298);
			spawn.setLocy(-17624);
			spawn.setLocz(-2194);
			spawn.stopRespawn();
			result = spawn.spawnOne();
			_gustav = ThreadPoolManager.getInstance().scheduleGeneral(new DeSpawnTimer(result), 3600000); //60 * 60 * 1000

			template = NpcTable.getInstance().getTemplate(BOSS1_ID);
			spawn = new L2Spawn(template);
			spawn.setLocx(178306);
			spawn.setLocy(-17535);
			spawn.setLocz(-2195);
			spawn.stopRespawn();
			_minion1 = spawn.spawnOne();
			_dietrich = ThreadPoolManager.getInstance().scheduleGeneral(new DeSpawnTimer(_minion1), 3600000); //60 * 60 * 1000

			template = NpcTable.getInstance().getTemplate(BOSS2_ID);
			spawn = new L2Spawn(template);
			spawn.setLocx(178304);
			spawn.setLocy(-17712);
			spawn.setLocz(-2194);
			spawn.stopRespawn();
			_minion2 = spawn.spawnOne();
			_mikhail = ThreadPoolManager.getInstance().scheduleGeneral(new DeSpawnTimer(_minion2), 3600000); //60 * 60 * 1000

			spawnMonsters();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		result = null;
		template = null;
		spawn = null;

		Announce("Siege of Devastated castle has begun!");
	}

	private static class MonsterLocation
	{
		private int _id;
		private int _x;
		private int _y;
		private int _z;
		private int _heading;

		private MonsterLocation(int id, int x, int y, int z, int heading)
		{
			_id = id;
			_x = x;
			_y = y;
			_z = z;
			_heading = heading;
		}

		private int getId()
		{
			return _id;
		}

		private int getX()
		{
			return _x;
		}

		private int getY()
		{
			return _y;
		}

		private int getZ()
		{
			return _z;
		}

		private int getHeading()
		{
			return _heading;
		}

	}

	private void addMonster(int id, int x, int y, int z, int heading)
	{
		_monsters.add(new MonsterLocation(id, x, y, z, heading));
	}

	private void fillMonsters()
	{
		addMonster(1008, 178288, -14924, -2200, 6320);
		addMonster(1007, 178255, -14884, -2200, 6320);
		addMonster(1008, 178222, -14924, -2200, 6320);
		addMonster(1007, 178222, -14884, -2200, 6320);
		addMonster(1007, 178420, -14904, -2200, 6320);
		addMonster(1007, 178387, -14904, -2200, 6320);
		addMonster(1007, 178288, -14884, -2200, 6320);
		addMonster(1007, 178387, -14884, -2200, 6320);
		addMonster(1008, 178354, -14944, -2200, 6320);
		addMonster(1007, 178321, -14884, -2200, 6320);
		addMonster(1008, 178222, -14944, -2200, 6320);
		addMonster(1007, 178354, -14904, -2200, 6320);
		addMonster(1008, 178255, -14924, -2200, 6320);
		addMonster(1008, 178387, -14924, -2200, 6320);
		addMonster(1008, 178354, -14924, -2200, 6320);
		addMonster(1008, 178420, -14924, -2200, 6320);
		addMonster(1007, 178354, -14884, -2200, 6320);
		addMonster(1007, 178420, -14884, -2200, 6320);
		addMonster(1008, 178454, -14944, -2200, 6320);
		addMonster(1008, 178454, -14924, -2200, 6320);
		addMonster(1008, 178420, -14944, -2200, 6320);
		addMonster(1007, 178222, -14904, -2200, 6320);
		addMonster(1008, 178321, -14944, -2200, 6320);
		addMonster(1008, 178321, -14924, -2200, 6320);
		addMonster(1007, 178288, -14904, -2200, 6320);
		addMonster(1007, 178321, -14904, -2200, 6320);
		addMonster(1008, 178255, -14944, -2200, 6320);
		addMonster(1007, 178255, -14904, -2200, 6320);
		addMonster(1008, 178288, -14944, -2200, 6320);
		addMonster(1007, 178454, -14884, -2200, 6320);
		addMonster(1008, 178387, -14944, -2200, 6320);
		addMonster(1007, 178454, -14904, -2200, 6320);
		addMonster(1008, 179052, -15226, -2221, 6320);
		addMonster(1008, 179260, -15341, -2221, 6320);
		addMonster(1008, 179101, -15253, -2221, 6320);
		addMonster(1008, 179073, -15203, -2221, 6320);
		addMonster(1008, 179144, -15271, -2221, 6320);
		addMonster(1008, 179246, -15285, -2221, 6320);
		addMonster(1008, 179164, -15247, -2221, 6320);
		addMonster(1008, 179226, -15309, -2221, 6320);
		addMonster(1008, 179322, -15349, -2221, 6320);
		addMonster(1008, 179302, -15372, -2221, 6320);
		addMonster(1008, 179189, -15286, -2221, 6320);
		addMonster(1008, 179391, -15439, -2221, 6320);
		addMonster(1008, 179341, -15406, -2221, 6320);
		addMonster(1010, 179503, -15925, -2256, 6320);
		addMonster(1010, 179562, -15984, -2256, 6320);
		addMonster(1010, 179491, -15981, -2256, 6320);
		addMonster(1010, 179370, -16196, -2256, 6320);
		addMonster(1006, 179426, -16009, -2253, 6320);
		addMonster(1010, 179544, -15882, -2256, 6320);
		addMonster(1010, 179599, -15943, -2256, 6320);
		addMonster(1010, 179570, -15901, -2256, 6320);
		addMonster(1010, 179408, -16158, -2256, 6320);
		addMonster(1010, 179279, -16219, -2256, 6320);
		addMonster(1006, 179327, -16101, -2253, 6320);
		addMonster(1006, 179540, -16876, -2246, 6320);
		addMonster(1008, 179009, -15201, -2221, 6320);
		addMonster(1008, 178951, -14699, -2080, 6320);
		addMonster(1008, 178801, -14975, -2080, 6320);
		addMonster(1008, 178865, -14857, -2080, 6320);
		addMonster(1008, 178822, -14936, -2080, 6320);
		addMonster(1008, 178843, -14897, -2080, 6320);
		addMonster(1008, 178929, -14739, -2080, 6320);
		addMonster(1008, 178908, -14778, -2080, 6320);
		addMonster(1008, 178886, -14818, -2080, 6320);
		addMonster(1006, 177719, -15951, -2253, 6320);
		addMonster(1008, 177838, -15664, -2226, 6320);
		addMonster(1006, 177627, -15953, -2250, 6320);
		addMonster(1006, 177387, -15955, -2250, 6320);
		addMonster(1006, 177667, -15921, -2253, 6320);
		addMonster(1008, 177859, -15812, -2226, 6320);
		addMonster(1006, 177661, -16014, -2253, 6320);
		addMonster(1006, 177608, -15985, -2250, 6320);
		addMonster(1010, 177663, -16154, -2250, 6320);
		addMonster(1010, 177530, -16079, -2250, 6320);
		addMonster(1006, 177709, -16043, -2253, 6320);
		addMonster(1006, 177703, -15999, -2250, 6320);
		addMonster(1006, 177810, -16145, -2253, 6320);
		addMonster(1010, 177571, -16105, -2250, 6320);
		addMonster(1010, 177473, -16011, -2250, 6320);
		addMonster(1010, 177612, -16090, -2250, 6320);
		addMonster(1010, 177657, -16113, -2250, 6320);
		addMonster(1010, 177387, -15996, -2250, 6320);
		addMonster(1006, 177564, -15963, -2250, 6320);
		addMonster(1006, 177606, -16035, -2253, 6320);
		addMonster(1006, 177470, -15856, -2250, 6320);
		addMonster(1010, 177428, -15981, -2250, 6320);
		addMonster(1006, 177506, -15887, -2250, 6320);
		addMonster(1006, 177517, -15930, -2250, 6320);
		addMonster(1006, 177308, -15861, -2253, 6320);
		addMonster(1006, 177861, -16164, -2253, 6320);
		addMonster(1008, 177906, -15791, -2226, 6320);
		addMonster(1008, 177765, -15643, -2226, 6320);
		addMonster(1008, 177880, -15744, -2226, 6320);
		addMonster(1008, 177788, -15578, -2226, 6320);
		addMonster(1008, 177811, -15622, -2226, 6320);
		addMonster(1008, 177859, -15704, -2226, 6320);
		addMonster(1008, 177769, -15540, -2226, 6320);
		addMonster(1008, 177813, -15726, -2226, 6320);
		addMonster(1008, 177707, -15427, -2226, 6320);
		addMonster(1008, 177680, -15485, -2226, 6320);
		addMonster(1008, 177722, -15561, -2226, 6320);
		addMonster(1008, 177745, -15498, -2226, 6320);
		addMonster(1008, 177803, -14971, -2210, 6320);
		addMonster(1008, 177727, -15464, -2226, 6320);
		addMonster(1010, 177433, -16026, -2250, 6320);
		addMonster(1008, 177649, -14750, -2210, 6320);
		addMonster(1008, 177619, -14705, -2210, 6320);
		addMonster(1008, 177711, -14838, -2210, 6320);
		addMonster(1008, 177834, -15015, -2210, 6320);
		addMonster(1008, 177741, -14883, -2210, 6320);
		addMonster(1008, 177772, -14927, -2210, 6320);
		addMonster(1008, 177680, -14794, -2210, 6320);
		addMonster(1006, 177400, -15854, -2250, 6320);
		addMonster(1010, 179697, -17781, -2256, 6320);
		addMonster(1006, 179479, -17133, -2256, 6320);
		addMonster(1006, 179485, -17213, -2246, 6320);
		addMonster(1006, 179593, -16876, -2246, 6320);
		addMonster(1006, 179468, -17280, -2256, 6320);
		addMonster(1006, 179433, -16991, -2246, 6320);
		addMonster(1006, 179514, -17281, -2256, 6320);
		addMonster(1006, 179525, -17135, -2256, 6320);
		addMonster(1006, 179444, -16937, -2256, 6320);
		addMonster(1006, 179438, -16875, -2246, 6320);
		addMonster(1010, 179633, -17137, -2256, 6320);
		addMonster(1006, 179537, -17214, -2246, 6320);
		addMonster(1006, 179594, -17453, -2246, 6320);
		addMonster(1010, 179576, -17137, -2256, 6320);
		addMonster(1010, 179508, -17341, -2252, 6320);
		addMonster(1010, 179446, -17391, -2252, 6320);
		addMonster(1010, 179437, -17522, -2252, 6320);
		addMonster(1010, 179536, -17842, -2252, 6320);
		addMonster(1010, 179432, -17719, -2252, 6320);
		addMonster(1010, 179436, -17841, -2252, 6320);
		addMonster(1006, 179542, -17453, -2246, 6320);
		addMonster(1010, 179436, -17776, -2256, 6320);
		addMonster(1010, 179534, -17892, -2252, 6320);
		addMonster(1010, 179482, -17841, -2252, 6320);
		addMonster(1010, 179696, -17844, -2252, 6320);
		addMonster(1010, 179604, -17525, -2252, 6320);
		addMonster(1010, 179707, -17722, -2252, 6320);
		addMonster(1006, 179715, -17454, -2246, 6320);
		addMonster(1006, 179641, -17215, -2246, 6320);
		addMonster(1010, 179665, -17527, -2252, 6320);
		addMonster(1010, 179557, -17524, -2252, 6320);
		addMonster(1010, 179636, -17780, -2256, 6320);
		addMonster(1010, 179694, -17897, -2252, 6320);
		addMonster(1009, 178682, -18200, -2200, 6320);
		addMonster(1008, 178577, -18422, -2250, 6320);
		addMonster(1007, 178745, -18186, -2200, 6320);
		addMonster(1008, 178528, -18499, -2250, 6320);
		addMonster(1009, 178640, -18196, -2200, 6320);
		addMonster(1008, 178766, -18228, -2200, 6320);
		addMonster(1007, 178724, -18184, -2200, 6320);
		addMonster(1007, 178703, -18182, -2200, 6320);
		addMonster(1008, 178575, -18500, -2250, 6320);
		addMonster(1008, 178703, -18222, -2200, 6320);
		addMonster(1008, 178530, -18421, -2250, 6320);
		addMonster(1008, 178523, -18696, -2250, 6320);
		addMonster(1007, 178661, -18178, -2200, 6320);
		addMonster(1009, 178661, -18198, -2200, 6320);
		addMonster(1008, 178788, -18229, -2200, 6320);
		addMonster(1009, 178724, -18204, -2200, 6320);
		addMonster(1008, 178574, -18539, -2250, 6320);
		addMonster(1008, 178578, -18383, -2250, 6320);
		addMonster(1008, 178573, -18577, -2250, 6320);
		addMonster(1008, 178530, -18382, -2250, 6320);
		addMonster(1008, 178528, -18461, -2250, 6320);
		addMonster(1008, 178526, -18538, -2250, 6320);
		addMonster(1008, 178526, -18576, -2250, 6320);
		addMonster(1008, 178570, -18736, -2250, 6320);
		addMonster(1008, 178524, -18618, -2250, 6320);
		addMonster(1008, 178571, -18657, -2250, 6320);
		addMonster(1008, 178523, -18656, -2250, 6320);
		addMonster(1008, 178523, -18735, -2250, 6320);
		addMonster(1008, 178571, -18618, -2250, 6320);
		addMonster(1008, 178571, -18697, -2250, 6320);
		addMonster(1008, 178576, -18461, -2250, 6320);
		addMonster(1008, 178682, -18220, -2200, 6320);
		addMonster(1008, 178661, -18218, -2200, 6320);
		addMonster(1009, 178745, -18206, -2200, 6320);
		addMonster(1007, 178682, -18180, -2200, 6320);
		addMonster(1009, 178703, -18202, -2200, 6320);
		addMonster(1007, 178640, -18176, -2200, 6320);
		addMonster(1009, 178788, -18209, -2200, 6320);
		addMonster(1008, 178640, -18216, -2200, 6320);
		addMonster(1007, 178788, -18189, -2200, 6320);
		addMonster(1008, 178745, -18226, -2200, 6320);
		addMonster(1009, 178766, -18208, -2200, 6320);
		addMonster(1007, 178766, -18188, -2200, 6320);
		addMonster(1008, 178724, -18224, -2200, 6320);
		addMonster(1008, 178430, -16901, -2217, 6320);
		addMonster(1010, 178285, -16832, -2217, 6320);
		addMonster(1008, 178153, -16914, -2217, 6320);
		addMonster(1006, 178398, -16781, -2218, 6320);
		addMonster(1010, 178363, -16768, -2217, 6320);
		addMonster(1008, 178437, -16587, -2217, 6320);
		addMonster(1008, 178431, -16784, -2217, 6320);
		addMonster(1006, 178120, -16714, -2218, 6320);
		addMonster(1010, 178366, -16721, -2217, 6320);
		addMonster(1008, 178433, -16742, -2217, 6320);
		addMonster(1008, 178430, -16862, -2217, 6320);
		addMonster(1006, 178395, -16865, -2218, 6320);
		addMonster(1010, 178288, -16783, -2217, 6320);
		addMonster(1006, 178397, -16824, -2218, 6320);
		addMonster(1010, 178090, -16878, -2217, 6320);
		addMonster(1008, 178154, -16797, -2217, 6320);
		addMonster(1010, 178027, -16773, -2217, 6320);
		addMonster(1008, 178435, -16665, -2217, 6320);
		addMonster(1008, 178433, -16704, -2217, 6320);
		addMonster(1006, 178060, -16868, -2218, 6320);
		addMonster(1008, 178156, -16718, -2217, 6320);
		addMonster(1006, 178324, -16762, -2218, 6320);
		addMonster(1008, 177869, -16832, -2217, 6320);
		addMonster(1006, 178117, -16885, -2218, 6320);
		addMonster(1010, 178366, -16674, -2217, 6320);
		addMonster(1008, 178435, -16627, -2217, 6320);
		addMonster(1010, 178366, -16582, -2217, 6320);
		addMonster(1006, 178403, -16566, -2218, 6320);
		addMonster(1008, 178437, -16548, -2217, 6320);
		addMonster(1010, 178290, -16550, -2217, 6320);
		addMonster(1008, 178160, -16600, -2217, 6320);
		addMonster(1006, 178122, -16759, -2218, 6320);
		addMonster(1006, 178122, -16672, -2218, 6320);
		addMonster(1010, 178095, -16736, -2217, 6320);
		addMonster(1006, 178324, -16804, -2218, 6320);
		addMonster(1010, 178029, -16587, -2217, 6320);
		addMonster(1010, 178290, -16690, -2217, 6320);
		addMonster(1010, 178027, -16820, -2217, 6320);
		addMonster(1008, 178431, -16822, -2217, 6320);
		addMonster(1006, 178326, -16720, -2218, 6320);
		addMonster(1008, 178158, -16679, -2217, 6320);
		addMonster(1008, 178161, -16562, -2217, 6320);
		addMonster(1008, 178158, -16640, -2217, 6320);
		addMonster(1008, 177872, -16714, -2217, 6320);
		addMonster(1006, 178061, -16738, -2218, 6320);
		addMonster(1008, 177871, -16752, -2217, 6320);
		addMonster(1008, 177876, -16597, -2217, 6320);
		addMonster(1008, 177873, -16675, -2217, 6320);
		addMonster(1008, 177869, -16873, -2217, 6320);
		addMonster(1008, 177874, -16637, -2217, 6320);
		addMonster(1006, 178060, -16826, -2218, 6320);
		addMonster(1006, 178063, -16784, -2218, 6320);
		addMonster(1010, 178029, -16727, -2217, 6320);
		addMonster(1008, 177876, -16558, -2217, 6320);
		addMonster(1008, 177870, -16794, -2217, 6320);
		addMonster(1008, 177868, -16911, -2217, 6320);
		addMonster(1008, 178156, -16756, -2217, 6320);
		addMonster(1010, 178092, -16782, -2217, 6320);
		addMonster(1008, 178153, -16876, -2217, 6320);
		addMonster(1008, 178154, -16836, -2217, 6320);
		addMonster(1007, 177217, -17168, -2200, 6320);
		addMonster(1008, 177187, -17128, -2200, 6320);
		addMonster(1008, 177276, -17128, -2200, 6320);
		addMonster(1008, 177335, -17128, -2200, 6320);
		addMonster(1009, 177246, -17148, -2200, 6320);
		addMonster(1007, 177246, -17168, -2200, 6320);
		addMonster(1007, 177129, -17168, -2200, 6320);
		addMonster(1007, 177158, -17168, -2200, 6320);
		addMonster(1008, 177217, -17128, -2200, 6320);
		addMonster(1008, 177158, -17128, -2200, 6320);
		addMonster(1008, 177129, -17128, -2200, 6320);
		addMonster(1009, 177129, -17148, -2200, 6320);
		addMonster(1007, 177276, -17168, -2200, 6320);
		addMonster(1009, 177158, -17148, -2200, 6320);
		addMonster(1008, 177305, -17128, -2200, 6320);
		addMonster(1007, 177187, -17168, -2200, 6320);
		addMonster(1009, 177305, -17148, -2200, 6320);
		addMonster(1008, 177396, -17452, -2207, 6320);
		addMonster(1008, 177397, -17634, -2207, 6320);
		addMonster(674, 177664, -17599, -2219, 6320);
		addMonster(674, 177721, -17813, -2219, 6320);
		addMonster(674, 177501, -17424, -2219, 6320);
		addMonster(1010, 177632, -17788, -2215, 6320);
		addMonster(1008, 177466, -17445, -2207, 6320);
		addMonster(1009, 177217, -17148, -2200, 6320);
		addMonster(1008, 177396, -17545, -2207, 6320);
		addMonster(1009, 177187, -17148, -2200, 6320);
		addMonster(1009, 177335, -17148, -2200, 6320);
		addMonster(1007, 177335, -17168, -2200, 6320);
		addMonster(1007, 177305, -17168, -2200, 6320);
		addMonster(1010, 177439, -17426, -2215, 6320);
		addMonster(1010, 177438, -17472, -2215, 6320);
		addMonster(674, 177662, -17685, -2219, 6320);
		addMonster(1010, 177527, -17578, -2215, 6320);
		addMonster(1010, 177531, -17399, -2215, 6320);
		addMonster(1010, 177561, -17563, -2215, 6320);
		addMonster(1008, 177604, -17718, -2207, 6320);
		addMonster(1008, 177466, -17537, -2207, 6320);
		addMonster(674, 177500, -17469, -2219, 6320);
		addMonster(1010, 177527, -17537, -2215, 6320);
		addMonster(1008, 177467, -17809, -2207, 6320);
		addMonster(1008, 177466, -17716, -2207, 6320);
		addMonster(674, 177496, -17645, -2219, 6320);
		addMonster(1008, 177398, -17817, -2207, 6320);
		addMonster(674, 177560, -17849, -2219, 6320);
		addMonster(1010, 177524, -17763, -2215, 6320);
		addMonster(1010, 177432, -17791, -2215, 6320);
		addMonster(1008, 177397, -17723, -2207, 6320);
		addMonster(1010, 177523, -17810, -2215, 6320);
		addMonster(674, 177501, -17384, -2219, 6320);
		addMonster(1008, 177603, -17539, -2207, 6320);
		addMonster(674, 177725, -17503, -2219, 6320);
		addMonster(674, 177666, -17508, -2219, 6320);
		addMonster(1010, 177564, -17425, -2215, 6320);
		addMonster(1008, 177749, -17538, -2207, 6320);
		addMonster(1008, 177603, -17446, -2207, 6320);
		addMonster(1008, 177749, -17446, -2207, 6320);
		addMonster(674, 177728, -17377, -2219, 6320);
		addMonster(1010, 177440, -17380, -2215, 6320);
		addMonster(674, 177567, -17376, -2219, 6320);
		addMonster(1009, 177276, -17148, -2200, 6320);
		addMonster(674, 177724, -17594, -2219, 6320);
		addMonster(1010, 177436, -17605, -2215, 6320);
		addMonster(1010, 177636, -17516, -2215, 6320);
		addMonster(1008, 177601, -17811, -2207, 6320);
		addMonster(1008, 177693, -17718, -2207, 6320);
		addMonster(674, 177722, -17728, -2219, 6320);
		addMonster(1010, 177559, -17708, -2215, 6320);
		addMonster(674, 177662, -17644, -2219, 6320);
		addMonster(1010, 177635, -17603, -2215, 6320);
		addMonster(1008, 177694, -17811, -2207, 6320);
		addMonster(1010, 177558, -17755, -2215, 6320);
		addMonster(1008, 177693, -17447, -2207, 6320);
		addMonster(1008, 177693, -17629, -2207, 6320);
		addMonster(674, 177664, -17559, -2219, 6320);
		addMonster(1008, 177466, -17397, -2207, 6320);
		addMonster(1008, 177466, -17627, -2207, 6320);
		addMonster(674, 177724, -17553, -2219, 6320);
		addMonster(1008, 177750, -17628, -2207, 6320);
		addMonster(1008, 177751, -17810, -2207, 6320);
		addMonster(674, 177661, -17818, -2219, 6320);
		addMonster(674, 177496, -17686, -2219, 6320);
		addMonster(1008, 177750, -17717, -2207, 6320);
		addMonster(1010, 177633, -17740, -2215, 6320);
		addMonster(1008, 177693, -17539, -2207, 6320);
		addMonster(1008, 177604, -17628, -2207, 6320);
		addMonster(1010, 177431, -17836, -2215, 6320);
		addMonster(1010, 177631, -17835, -2215, 6320);
		addMonster(674, 177722, -17768, -2219, 6320);
		addMonster(1008, 177246, -17128, -2200, 6320);
	}

	public void spawnMonsters()
	{
		for(MonsterLocation ml : _monsters)
		{
			try
			{
				L2NpcTemplate template = NpcTable.getInstance().getTemplate(ml.getId());
				L2Spawn sp = new L2Spawn(template);
				sp.setAmount(1);
				sp.setLocx(ml.getX());
				sp.setLocy(ml.getY());
				sp.setLocz(ml.getZ());
				sp.setHeading(ml.getHeading());
				sp.setRespawnDelay(300); //3 * 60
				sp.setLocation(0);
				sp.init();
				_spawns.add(sp);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		_monsterdespawn = ThreadPoolManager.getInstance().scheduleGeneral(new DeSpawnMonsters(), 3600000); //60 * 60 * 1000
	}

	protected class DeSpawnMonsters implements Runnable
	{
		@Override
		public void run()
		{
			DeSpawn();
		}
	}

	public void DeSpawn()
	{
		for(L2Spawn sp : _spawns)
		{
			sp.stopRespawn();
			sp.getLastSpawn().doDie(sp.getLastSpawn());
		}
		_spawns.clear();
		setIsInProgress(false);
		_spawns = null;
	}

	protected class DeSpawnTimer implements Runnable
	{
		L2NpcInstance _npc = null;

		public DeSpawnTimer(L2NpcInstance npc)
		{
			_npc = npc;
		}

		@Override
		public void run()
		{
			if(_npc.getNpcId() == 12534)
			{
				Announce("Siege of Devastated castle is over.");
				Announce("Nobody won! ClanHall belong to NPC until next siege.");

				ClanHall CH = ClanHallManager.getInstance().getClanHallById(34);
				CH.banishForeigners();
				CH.spawnDoor();
			}
			_npc.onDecay();
		}
	}

	public final boolean Conditions(L2PcInstance player)
	{
		if(player != null && player.getClan() != null && player.isClanLeader() && player.getClan().getAuctionBiddedAt() <= 0 && ClanHallManager.getInstance().getClanHallByOwner(player.getClan()) == null && player.getClan().getLevel() > 3)
			return true;
		else
			return false;
	}

	public boolean getIsInProgress()
	{
		return _progress;
	}

	public void setIsInProgress(boolean is)
	{
		_progress = is;
	}

	public void SiegeFinish()
	{
		L2Clan clanIdMaxDamage = null;
		long tempMaxDamage = 0;
		for(DamageInfo damageInfo : _clansDamageInfo.values())
		{
			if(damageInfo != null)
			{
				if(damageInfo._damage > tempMaxDamage)
				{
					tempMaxDamage = damageInfo._damage;
					clanIdMaxDamage = damageInfo._clan;
				}
			}
		}
		if(clanIdMaxDamage != null)
		{
			ClanHallManager.getInstance().setOwner(34, clanIdMaxDamage);
			update();

			Announce("Siege of Devastated castle is over.");
			Announce("Now its belong to: '" + clanIdMaxDamage.getName() + "' until next siege.");
		}
		else
		{
			Announce("Siege of Devastated castle is over..");
			Announce("Nobody won! ClanHall belong to NPC until next siege.");
		}

		DeSpawn();
		_minion1.onDecay();
		_minion2.onDecay();
		_gustav.cancel(true);
		_dietrich.cancel(true);
		_mikhail.cancel(true);
		_monsterdespawn.cancel(true);

		ClanHall CH = ClanHallManager.getInstance().getClanHallById(34);
		CH.banishForeigners();
		CH.spawnDoor();
		CH = null;
	}

	public void addSiegeDamage(L2Clan clan, long damage)
	{
		DamageInfo clanDamage = _clansDamageInfo.get(clan.getClanId());
		if(clanDamage != null)
		{
			clanDamage._damage += damage;
		}
		else
		{
			clanDamage = new DamageInfo();
			clanDamage._clan = clan;
			clanDamage._damage += damage;
			_clansDamageInfo.put(clan.getClanId(), clanDamage);
		}
	}

	private void update()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;

			statement = con.prepareStatement("UPDATE clanhall SET paidUntil=?, paid=? WHERE id=?");
			statement.setLong(1, System.currentTimeMillis() + 59760000);
			statement.setInt(2, 1);
			statement.setInt(3, 34);
			statement.execute();
			statement.close();
			statement = null;
		}
		catch(Exception e)
		{
			System.out.println("Exception: updateOwnerInDB(L2Clan clan): " + e.getMessage());
			e.printStackTrace();
		}
	    finally
	    {
	        try { con.close(); } catch (Exception e) {}
	    }
	}
}
