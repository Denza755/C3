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

import java.util.ArrayList;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.serverpackets.ServerBasePacket;
import net.sf.l2j.gameserver.serverpackets.SunRise;
import net.sf.l2j.gameserver.serverpackets.SunSet;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.4.8 $ $Date: 2005/04/06 16:13:24 $
 */
public class GameTimeController extends Thread
{
	static final Logger _log = Logger.getLogger(GameTimeController.class.getName());
	
	public static final int TICKS_PER_SECOND = 10;
	public static final int MILLIS_IN_TICK = 1000 / TICKS_PER_SECOND;
	
	private static GameTimeController _instance = new GameTimeController();
	
	private static int  _gameTicks;
	private static long _gameStartTime;
	protected static boolean _isNight;
	
	private static ArrayList<L2Character> movingObjects = new ArrayList<L2Character>(); 
	
	/**
	 * one ingame day is 240 real minutes
	 */
	public static GameTimeController getInstance()
	{
		return _instance;
	}
	
	private GameTimeController()
	{
		super("GameTimeController");
		setDaemon(true);
		setPriority(MAX_PRIORITY);
		_gameStartTime = System.currentTimeMillis()- 3600000; // offset so that the server starts a day begin
		_gameTicks = 3600000/MILLIS_IN_TICK; // offset so that the server starts a day begin
		start();
		ClientScheduler.getInstance().scheduleLowAtFixedRate(
				new BroadcastSunState(), 0, 60*60*1000
				);
	}
	
	public boolean isNowNight() {
		return _isNight;
	}
	
	public int getGameTime()
	{
		return (_gameTicks / (TICKS_PER_SECOND*10));
	}
	
	public static int getGameTicks()
	{
		return _gameTicks;
	}
	
	public void run()
	{
		try {
			for (;;)
			{
				long runtime = System.currentTimeMillis() - _gameStartTime;
				_gameTicks = (int)(runtime / MILLIS_IN_TICK);
				moveObjects();
				runtime = System.currentTimeMillis() - _gameStartTime;
				int sleepTime = 1 + MILLIS_IN_TICK - ((int)runtime) % MILLIS_IN_TICK; 
				sleep( sleepTime ); 
			}
		} catch (InterruptedException e) {}
	}

	public synchronized void registerMovingObject(L2Character cha) {
		if (!movingObjects.contains(cha))
			movingObjects.add(cha);
	}
	
	private synchronized void moveObjects() {
		L2Character[] chars = movingObjects.toArray(new L2Character[movingObjects.size()]);
		ArrayList<L2Character> ended = null;
		for (int i=0; i < chars.length; i++) {
			L2Character cha = chars[i];
			boolean end = cha.updatePosition(_gameTicks);
			if (end) {
				movingObjects.remove(cha);
				if (ended == null)
					ended = new ArrayList<L2Character>();
				ended.add(cha);
			}
		}
		if (ended != null)
			ClientScheduler.getInstance().scheduleLow(new MovingObjectArrived(ended), 10);
	}
	
	class MovingObjectArrived implements Runnable
	{
		final ArrayList<L2Character> ended;
		MovingObjectArrived(ArrayList<L2Character> ended) {
			this.ended = ended;
		}
		public void run() {
			for (L2Character cha : ended) {
				cha.updateKnownObjects();
				cha.getAI().notifyEvent(CtrlEvent.EVT_ARRIVED);
			}
		}
	}
	
	/**
	 * @param rise
	 */
	class BroadcastSunState implements Runnable
	{
		public void run()
		{
			ServerBasePacket packet;
			int time = getGameTime()+100 / 360; // time in hours
			time %= 6;
			_isNight = (time >= 5);
			if ( time >= 5) {
				packet = new SunSet();
				if (Config.DEBUG) _log.fine("SunSet");
			}
			else if (time == 0) {
				_isNight = false;
				packet = new SunRise();
				if (Config.DEBUG) _log.fine("SunRise");
			}
			else
				return;
			
			for (L2PcInstance player : L2World.getInstance().getAllPlayers())
			{
				player.sendPacket(packet);
			}
		}
	}
}
