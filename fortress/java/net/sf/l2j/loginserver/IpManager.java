/*
 * $Header: LoginManager.java, 14-Jul-2005 14:33:14 luisantonioa Exp $
 *
 * $Author: Beast $
 * $Date: 27-Jan-2006 17:00:00 $
 * $Revision: 1 $
 * $Log: IpManager.java,v $
 * Revision 1  27-Jan-2006 17:00:00  Beast
 * Added copyright notice
 *
 * 
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

package net.sf.l2j.loginserver;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.logging.Logger;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2PcInstance;

public class IpManager
{
    private static Logger _log = Logger.getLogger(IpManager.class.getName());
    private static IpManager _instance;
    
    public class BannedIp
    {
        public String ip;
        public String admin;
    }
    
    public static IpManager getInstance()
    {
        if(_instance==null)
            _instance = new IpManager();
        return _instance;
    }
    
    public IpManager()
    {
    }
    
    public void BanIp(String ip, String admin)
    {
        if(CheckIp(ip))
            return;
        java.sql.Connection con = null;
        try
        { 
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("INSERT INTO banned_ips (ip,admin) values(?,?)");
            statement.setString(1, ip);
            statement.setString(2, admin);
            statement.execute();
            statement.close();
        }
        catch (Exception e)
        {
            _log.info("error while reading banned_ips");
        } 
        finally 
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
    public void UnbanIp(String ip)
    {
        if(!CheckIp(ip))
            return;
        java.sql.Connection con = null;
        try
        { 
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("DELETE FROM banned_ips WHERE ip=?");
            statement.setString(1, ip);
            statement.execute();
            statement.close();
        }
        catch (Exception e)
        {
            _log.info("error while reading banned_ips");
        } 
        finally 
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
    public boolean CheckIp(String ip)
    {
        boolean result = false;
        java.sql.Connection con = null;
        try
        {   
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT * FROM banned_ips WHERE ip=?");
            statement.setString(1, ip);
            ResultSet rset = statement.executeQuery();
            if(rset.next())
                result=true;
            rset.close();
            statement.close();
        }
        catch (Exception e)
        {
            _log.info("error while reading banned_ips");
        } 
        finally 
        {
            try { con.close(); } catch (Exception e) {}
        }
        return result;
    }
    
    public int getBannedCount()
    {
        int result = 0;
        java.sql.Connection con = null;
        try
        {   
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT * FROM banned_ips");
            ResultSet rset = statement.executeQuery();
            while(rset.next())
                result++;
            rset.close();
            statement.close();
        }
        catch (Exception e)
        {
            _log.info("error while reading banned_ips");
        } 
        finally 
        {
            try { con.close(); } catch (Exception e) {}
        }
        return result;
    }
    
    public ArrayList<BannedIp> getBanList()
    {
        ArrayList<BannedIp> result = new ArrayList<BannedIp>();
        java.sql.Connection con = null;
        try
        {   
            con = L2DatabaseFactory.getInstance().getConnection();
            BannedIp temp;
            PreparedStatement statement = con.prepareStatement("SELECT ip,admin FROM banned_ips");
            ResultSet rset = statement.executeQuery();
            while(rset.next())
            {
                temp = new BannedIp();
                temp.ip = rset.getString("ip");
                temp.admin = rset.getString("admin");
                result.add(temp);
            }
            rset.close();
            statement.close();
        }
        catch (Exception e)
        {
            _log.info("error while reading banned_ips");
        } 
        finally 
        {
            try { con.close(); } catch (Exception e) {}
        }
        return result;
    }
    
    public String getIpByPlayer(L2PcInstance player)
    {
        String ip = "";
        if(player!=null)
        {
            ip = player.getNetConnection().getSocket().getInetAddress().getHostAddress();
        }
        return ip;
    }
}