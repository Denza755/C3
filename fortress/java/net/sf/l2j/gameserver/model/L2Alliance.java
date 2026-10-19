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
package net.sf.l2j.gameserver.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ClanTable;
import net.sf.l2j.gameserver.serverpackets.ServerBasePacket;
//import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.7.2.4.2.7 $ $Date: 2005/04/06 16:13:41 $
 */
public class L2Alliance
{
	private static final Logger _log = Logger.getLogger(L2Alliance.class.getName());

	private String _allyName;
	private int _allyId;
	private L2Clan _leader;
	private Map<String, L2Clan> _members = new FastMap<String, L2Clan>();
	
	private boolean _hasAllyCrest;
    private int _allyCrestId;

	private List<Integer> _atAllyWarWith = new FastList<Integer>();
    
    /**
     * called if a ally is referenced only by id.
     * in this case all other data needs to be fetched from db
     * @param allyId
     */
    public L2Alliance(int allyId)
    {
        _allyId = allyId;
        restore();
    }
    
    /**
     * this is only called if a new ally is created
     * @param allyId
     * @param allyName
     * @param leader
     */
    public L2Alliance(int allyId, String allyName, L2Clan leader)
    {
        _allyId = allyId;
        _allyName = allyName;
        setLeader(leader);
    }
    
	/**
	 * @return Returns the leaderId.
	 */
	public int getLeaderId()
	{
		return _leader != null ? _leader.getClanId() : 0;
	}
	/**
     * @return L2ClanMember of ally leader.
     */
    public L2Clan getLeader()
    {
        return _leader;
    }
	/**
	 * @param leaderId The leaderId to set.
	 */
	public void setLeader(L2Clan leader)
	{
		_leader = leader;
		_members.put(leader.getName(), leader);
	}
	
	/**
	 * @return Returns the leaderName.
	 */
	public String getAllyLeaderName()
	{
		return this.getLeader().getLeaderName();
	}
	
	public void addAllyMember(L2Clan member)
	{
        // store in db
        storeNewMemberInDatabase(member);
        // store in memory
		_members.put(member.getName(), member);
	}
	
	public L2Clan getAllyMember(String name)
	{
		return _members.get(name);
	}

	public void removeAlly()
	{
	    //broadcastToOnlineMembers(new SystemMessage(SystemMessage.CLAN_MEMBERSHIP_TERMINATED));
	    for (L2Clan member : getMembers())
	    {
	        removeAllyMember(member.getName());
	    }
	    
	    java.sql.Connection con = null;
	    try
	    {
	        con = L2DatabaseFactory.getInstance().getConnection();
	        PreparedStatement statement = con.prepareStatement("DELETE FROM ally_data WHERE ally_id=?");
	        statement.setInt(1, getAllyId());
	        statement.execute();
	        statement.close();                  
	        if (Config.DEBUG) _log.fine("clan removed in db: "+getAllyId());
	    }
	    catch (Exception e)
	    {
	        _log.warning("error while removing clan in db "+e);
	    }
	    finally
	    {
	        try { con.close(); } catch (Exception e) {}
	    }
	}

	public void removeAllyMember(String name)
	{
		L2Clan exMember = _members.remove(name);
		if(exMember == null)
		{
			_log.warning("Clan "+name+" not found in alliance while trying to remove");
			return;
		}
		if (exMember.getName().equals(getAllyLeaderName()))
        {
            removeAlly();
        }
        removeMemberInDatabase(exMember);
        if (_members.isEmpty())
        {
            removeAlly();
        }
	}

	public L2Clan[] getMembers()
	{
		return _members.values().toArray(new L2Clan[_members.size()]);
	}
	
	/**
	 * @return
	 */
	public int getAllyId()
	{
		return _allyId;
	}
	/**
	 * @return
	 */
	public String getAllyName()
	{
		return _allyName;
	}
    
    public void setAllyCrestId(int allyCrestId)
    {
        _allyCrestId = allyCrestId;
    }
    
	/**
	 * @return
	 */
	public int getAllyCrestId()
	{
		return _allyCrestId;
	}
	/**
	 * @param allyId The allyId to set.
	 */
	public void setAllyId(int allyId)
	{
		_allyId = allyId;
	}
	/**
	 * @param allyName The allyName to set.
	 */
	public void setAllyName(String allyName)
	{
		_allyName = allyName;
	}
	
	/**
	 * @param player name
	 * @return
	 */
	public boolean isMember(String name)
	{
		return _members.containsKey(name);
	}
	
	/*public void updateClanInDB()
	{
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET leader_id=?,ally_id=?,ally_name=? WHERE clan_id=?");
			statement.setInt(1, getLeaderId());
			statement.setInt(2, getAllyId());
			statement.setString(3, getAllyName());
			statement.setInt(4, getAllyId());
			statement.execute();
			statement.close();
			if (Config.DEBUG) _log.fine("New ally leader saved in db: "+getAllyId());
		}
		catch (Exception e)
		{
			_log.warning("error while saving new ally leader to db "+e);
		}
		finally
		{
			try { con.close(); } catch (Exception e) {}
		}
	}	*/

	public void store()
	{
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO ally_data (ally_id,ally_name,leader_id,ally_crestid) values (?,?,?,?)");
			statement.setInt(1, getAllyId());
			statement.setString(2, getAllyName());
			statement.setInt(3, getLeaderId());
            statement.setInt(4,getAllyCrestId());
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("UPDATE clan_data SET ally_id=? WHERE clan_id=?");
			statement.setInt(1, getAllyId());
			statement.setInt(2, getLeaderId());
			statement.execute();
			statement.close();					

			if (Config.DEBUG) _log.fine("New ally saved in db: "+getAllyId());
		}
		catch (Exception e)
		{
			_log.warning("error while saving new ally to db "+e);
		}
		finally
		{
			try { con.close(); } catch (Exception e) {}
		}
	}	

    private void storeNewMemberInDatabase(L2Clan member)
    {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET ally_id=? WHERE clan_id=?");
            statement.setInt(1, getAllyId());
            statement.setInt(2, member.getClanId());
            statement.execute();
            statement.close();                  
            if (Config.DEBUG) _log.fine("New alliance member saved in db: "+getAllyId());
        }
        catch (Exception e)
        {
            _log.warning("error while saving new alliance member to db "+e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }

    private void removeMemberInDatabase(L2Clan member)
    {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET ally_id=0 WHERE clan_id=?");
            statement.setInt(1, member.getClanId());
            statement.execute();
            statement.close();                  
            if (Config.DEBUG) _log.fine("ally member removed in db: "+getAllyId());
        }
        catch (Exception e)
        {
            _log.warning("error while removing ally member in db "+e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }

    private void restorewars(){
     	java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;
            statement = con.prepareStatement("SELECT clan1, clan2, wantspeace1, wantspeace2, type FROM clan_wars");
            ResultSet rset = statement.executeQuery();
            while(rset.next())
            {
               if(rset.getInt("clan1") == this.getAllyId())
			   {
				   if(rset.getInt("type")==2)
					   this.setEnemyAlly(rset.getInt("clan2"));
			   }
               if(rset.getInt("clan2") == this.getAllyId())
			   {
				   if(rset.getInt("type")==2)
					   this.setEnemyAlly(rset.getInt("clan1"));
			   }
           	}
            statement.close();
        }
        catch (Exception e)
        {
            _log.warning("could not restore ally wars data:"+e);
        } 
        finally 
        {
            try { con.close(); } catch (Exception e) {}
        }
            	
    	
    }
    private void restore()
    {
        //restorewars();
    	java.sql.Connection con = null;
        try
        {
            L2Clan member;
            
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT ally_name,leader_id,ally_crestid FROM ally_data where ally_id=?");
            statement.setInt(1, getAllyId());
            ResultSet allyData = statement.executeQuery();
            
            if (allyData.next())
            {
                setAllyName(allyData.getString("ally_name"));
				int leaderId = (allyData.getInt("leader_id"));
                setAllyCrestId(allyData.getInt("ally_crestid"));
				
				if (getAllyCrestId() != 0)
                {
                    setHasAllyCrest(true);
                }
                
                PreparedStatement statement2 = con.prepareStatement("SELECT clan_id,clan_name FROM clan_data WHERE ally_id=?");
                statement2.setInt(1, getAllyId());
                ResultSet allyMembers = statement2.executeQuery();
                
                while (allyMembers.next())
                {
                    member = ClanTable.getInstance().getClan(allyMembers.getInt("clan_id"));
                    if (member.getClanId() == leaderId)
                    {
                        setLeader(member);
                    }
                    else
                    {
                        addAllyMember(member);
                    }                   
                }               
                allyMembers.close();
                statement2.close();             
            }
            
			allyData.close();
            statement.close();
            
            if (getAllyName() != null)
            	_log.config("Restored ally data for \"" + getAllyName() + "\" from database.");
            restorewars();
        }
        catch (Exception e)
        {
            _log.warning("error while restoring ally "+e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
	public void broadcastToOnlineMembers(ServerBasePacket packet)
	{
		for (L2Clan member : _members.values())
		{
			if (member!=null)
			{
				member.broadcastToOnlineMembers(packet);
			}
		}
	}
	
	public void broadcastToOtherOnlineMembers(ServerBasePacket packet, L2PcInstance player)
	{
		for (L2Clan member : _members.values())
		{
			if (member!=null)
			{
				member.broadcastToOtherOnlineMembers(packet,player);
			}
		}
	}
	
	public String toString()
	{
		return getAllyName();
	}

    /**
     * @return
     */
    
	public boolean hasAllyCrest()
	{
		return _hasAllyCrest;
	}
	
	public void setHasAllyCrest(boolean flag)
	{
		_hasAllyCrest = flag;
	}
	 
	public void setEnemyAlly(L2Clan clan)
    {
    	Integer id = clan.getClanId();
    	_atAllyWarWith.add(id);
    }
    public void setEnemyAlly(Integer clan)
    {
    	_atAllyWarWith.add(clan);
    }
    public void deleteEnemyAlly(L2Clan clan)
    {
    	Integer id = clan.getClanId();
    	_atAllyWarWith.remove(id);
    }
	
    public int isAtWar()
    {
		int temp=0;
	   if ((_atAllyWarWith != null)&&(_atAllyWarWith.size() > 0))
           temp=+2;
       return temp;       
    }
	
	public L2PcInstance[] getOnlineMembers(String exclude)
	{
		List<L2PcInstance> result = new FastList<L2PcInstance>();
		for (L2Clan temp : _members.values())
		{

		for (L2ClanMember temp2 : temp.getMembers())
		{
			//L2ClanMember temp = (L2ClanMember) iter.next();
			if (temp2.isOnline() && temp2.getPlayerInstance() != null && (exclude == null || !temp2.getName().equals(exclude)))
			{
				result.add(temp2.getPlayerInstance());
			}
		}
		}
		return result.toArray(new L2PcInstance[result.size()]);
	}
	
	public List<Integer> getWarList()
	{
		return _atAllyWarWith;
	}
}
