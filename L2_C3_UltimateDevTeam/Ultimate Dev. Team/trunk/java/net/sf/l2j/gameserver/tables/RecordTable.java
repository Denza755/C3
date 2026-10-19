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
package net.sf.l2j.gameserver.tables;

import net.sf.l2j.L2DatabaseFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

/**
 * This class ...
 * 
 * @version $Revision$ $Date$
 */
public class RecordTable
{
	private static Logger _log = Logger.getLogger(RecordTable.class.getName());

	private static RecordTable _instance;

	private int _maxPlayer=0;
	private String _strDateMaxPlayer=null;
	
	private boolean _initialized = true;
	
	/**
	* Not really useful to make an instance of recordtable because data is reloaded each time. 
	* But it's quite easy to use like this.
	*/
	public static RecordTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new RecordTable();
		}
		_instance.RestoreRecordData();
		return _instance;
	}

	private RecordTable()
	{
	}

	/**
	 * 
	 */
	public void RestoreRecordData()
	{
		java.sql.Connection con = null;
		try
		{
			try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT maxplayer, date FROM record");
			ResultSet recorddata = statement.executeQuery();

			fillRecordTable(recorddata);
			recorddata.close();
			statement.close();
			} catch (Exception e) {
				_log.severe("error while creating record table " + e);
				e.printStackTrace();
			}
			 
		} finally {
			try { con.close(); } catch (Exception e) {}
		}
	}

	private void fillRecordTable(ResultSet Recorddata)
			throws Exception
	{
		// In fact, there is just one record
		while (Recorddata.next())
		{
			_maxPlayer = Recorddata.getInt("maxplayer");
			_strDateMaxPlayer = Recorddata.getString("date");
		}
	}
    

	public int getMaxPlayer()
	{
		return _maxPlayer;
	}

	public String getDateMaxPlayer()
	{
		return _strDateMaxPlayer;
	}    
	
}
