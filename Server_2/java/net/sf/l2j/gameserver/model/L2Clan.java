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

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.serverpackets.ServerBasePacket;

/**
 * This class ...
 * 
 * @version $Revision: 1.7.2.4.2.7 $ $Date: 2005/04/06 16:13:41 $
 */
public class L2Clan
{
	private static final Logger _log = Logger.getLogger(L2Clan.class.getName());

	private String _name;
	private int _clanId;
	private L2ClanMember _leader;
	private Map<String, L2ClanMember> _members = new TreeMap<String, L2ClanMember>();
	
	private String _allyName;
	private int _allyId;
	private int _level;
	private int _hasCastle;
	private int _hasHideout;
    private boolean _hasCrest;

    private Warehouse _warehouse = new ClanWarehouse(this);

    /**
     * called if a clan is referenced only by id.
     * in this case all other data needs to be fetched from db
     * @param clanId
     */
    public L2Clan(int clanId)
    {
        _clanId = clanId;
        restore();
        File crestFile = new File(Config.DATAPACK_ROOT, "data/crests/Pledge_"+_clanId+".bmp");
        _hasCrest = crestFile.exists();
    }
    
    /**
     * this is only called if a new clan is created
     * @param clanId
     * @param clanName
     * @param leader
     */
    public L2Clan(int clanId, String clanName, L2ClanMember leader)
    {
        _clanId = clanId;
        _name = clanName;
        setLeader(leader);
    }
    
	/**
	 * @return Returns the clanId.
	 */
	public int getClanId()
	{
		return _clanId;
	}
	/**
	 * @param clanId The clanId to set.
	 */
	public void setClanId(int clanId)
	{
		_clanId = clanId;
	}
	/**
	 * @return Returns the leaderId.
	 */
	public int getLeaderId()
	{
		return _leader.getObjectId();
	}
	/**
	 * @param leaderId The leaderId to set.
	 */
	public void setLeader(L2ClanMember leader)
	{
		_leader = leader;
		_members.put(leader.getName(), leader);
	}
	/**
	 * @return Returns the leaderName.
	 */
	public String getLeaderName()
	{
		return _leader.getName();
	}

	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return _name;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(String name)
	{
		_name = name;
	}
	
	private void addClanMember(L2ClanMember member)
	{
		_members.put(member.getName(), member);
	}
	
	public void addClanMember(L2PcInstance player)
	{
		L2ClanMember member = new L2ClanMember(player);
        // store in db
        storeNewMemberInDatabase(member);
        // store in memory
		addClanMember(member);
	}
	
	public L2ClanMember getClanMember(String name)
	{
		return _members.get(name);
	}
	
	public void removeClanMember(String name)
	{
		L2ClanMember exMember = _members.remove(name);
        removeMemberInDatabase(exMember);
	}

	public L2ClanMember[] getMembers()
	{
		return _members.values().toArray(new L2ClanMember[_members.size()]);
	}
	
	public L2PcInstance[] getOnlineMembers(String exclude)
	{
		ArrayList<L2PcInstance> result = new ArrayList<L2PcInstance>();
		for (L2ClanMember temp : _members.values())
		{
			//L2ClanMember temp = (L2ClanMember) iter.next();
			if (temp.isOnline() && temp.getPlayerInstance()!=null && !temp.getName().equals(exclude))
			{
				result.add(temp.getPlayerInstance());
			}
		}
		
		return result.toArray(new L2PcInstance[result.size()]);
		
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
	/**
	 * @return
	 */
	public int getAllyCrestId()
	{
		return _allyId;
	}
	/**
	 * @return
	 */
	public int getLevel()
	{
		return _level;
	}
	/**
	 * @return
	 */
	public int getHasCastle()
	{
		return _hasCastle;
	}
	/**
	 * @return
	 */
	public int getHasHideout()
	{
		return _hasHideout;
	}
	/**
	 * @return Returns the clanCrestId.
	 */
	public int getCrestId()
	{
		return _clanId;
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
	 * @param hasCastle The hasCastle to set.
	 */
	public void setHasCastle(int hasCastle)
	{
		_hasCastle = hasCastle;
	}
	/**
	 * @param hasHideout The hasHideout to set.
	 */
	public void setHasHideout(int hasHideout)
	{
		_hasHideout = hasHideout;
	}
	/**
	 * @param level The level to set.
	 */
	public void setLevel(int level)
	{
		_level = level;
	}
	
	/**
	 * @param player name
	 * @return
	 */
	public boolean isMember(String name)
	{
		return _members.containsKey(name);
	}
	
	public void store()
	{
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO clan_data (clan_id,clan_name,clan_level,hasCastle,hasHideout,ally_id,ally_name,leader_id) values (?,?,?,?,?,?,?,?)");
			statement.setInt(1, getClanId());
			statement.setString(2, getName());
			statement.setInt(3, getLevel());
			statement.setInt(4, getHasCastle());
			statement.setInt(5, getHasHideout());
			statement.setInt(6, getAllyId());
			statement.setString(7, getAllyName());
			statement.setInt(8, getLeaderId());
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("UPDATE characters SET clanid=? WHERE obj_Id=?");
			statement.setInt(1, getClanId());
			statement.setInt(2, getLeaderId());
			statement.execute();
			statement.close();					

			if (Config.DEBUG) _log.fine("New clan saved in db: "+getClanId());
		}
		catch (Exception e)
		{
			_log.warning("error while saving new clan to db "+e);
		}
		finally
		{
			try { con.close(); } catch (Exception e) {}
		}
	}	

    private void storeNewMemberInDatabase(L2ClanMember member)
    {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("UPDATE characters SET clanid=? WHERE obj_Id=?");
            statement.setInt(1, getClanId());
            statement.setInt(2, member.getObjectId());
            statement.execute();
            statement.close();                  
            if (Config.DEBUG) _log.fine("New clan member saved in db: "+getClanId());
        }
        catch (Exception e)
        {
            _log.warning("error while saving new clan member to db "+e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }

    private void removeMemberInDatabase(L2ClanMember member)
    {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("UPDATE characters SET clanid=0 WHERE obj_Id=?");
            statement.setInt(1, member.getObjectId());
            statement.execute();
            statement.close();                  
            if (Config.DEBUG) _log.fine("clan member removed in db: "+getClanId());
        }
        catch (Exception e)
        {
            _log.warning("error while removing clan member in db "+e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }

    private void restore()
    {
        java.sql.Connection con = null;
        try
        {
            L2ClanMember member;
            
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT clan_name,clan_level,hasCastle,hasHideout,ally_id,ally_name,leader_id FROM clan_data where clan_id=?");
            statement.setInt(1, getClanId());
            ResultSet clanData = statement.executeQuery();
            
            if (clanData.next())
            {
                setName(clanData.getString("clan_name"));
                setLevel(clanData.getInt("clan_level"));
                setHasCastle(clanData.getInt("hasCastle"));
                setHasHideout(clanData.getInt("hasHideout"));
                setAllyId(clanData.getInt("ally_id"));
                setAllyName(clanData.getString("ally_name"));
                int leaderId = (clanData.getInt("leader_id"));          
                
                PreparedStatement statement2 = con.prepareStatement("SELECT char_name,level,classid,obj_Id FROM characters WHERE clanid=?");
                statement2.setInt(1, getClanId());
                ResultSet clanMembers = statement2.executeQuery();
                
                while (clanMembers.next())
                {
                    member = new L2ClanMember(clanMembers.getString("char_name"), clanMembers.getInt("level"), clanMembers.getInt("classid"), clanMembers.getInt("obj_id"));
                    if (member.getObjectId() == leaderId)
                    {
                        setLeader(member);
                    }
                    else
                    {
                        addClanMember(member);
                    }                   
                }               
                clanMembers.close();
                statement2.close();             
            }
            
            clanData.close();
            statement.close();
            
            _log.config("restored clan " + getName() + " from db");
        }
        catch (Exception e)
        {
            _log.warning("error while restoring clan "+e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
	public void broadcastToOnlineMembers(ServerBasePacket packet)
	{
		for (L2ClanMember member : _members.values())
		{
			if (member.isOnline() && member.getPlayerInstance() != null)
			{
				member.getPlayerInstance().sendPacket(packet);
			}
		}
	}
	
	public String toString()
	{
		return getName();
	}

    /**
     * @return
     */
    public boolean hasCrest()
    {
        return _hasCrest;
    }
    
    public void setHasCrest(boolean flag)
    {
        _hasCrest = flag;
    }
    
    public Warehouse getWarehouse()
    {
        return _warehouse;
    }
}
