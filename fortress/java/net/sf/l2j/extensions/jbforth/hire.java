package net.sf.l2j.extensions.jbforth;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.NpcTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import ru.balancer.JBForth;
import net.sf.l2j.gameserver.lib.Rnd;
import ru.balancer.jbforth.Dict;
import ru.balancer.jbforth.Word;
import net.sf.l2j.gameserver.model.L2CastleGuardInstance;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class hire
{
    static Logger _log = Logger.getLogger(hire.class.getName());

    public hire(Dict d)
    {
        d.reg("HIRE-GUARD", new hire_guard());
        d.reg("toogle", new toogle());
    }
    public class toogle extends Word
    {
    	// how can i extract the subcommand?
    }
    public class hire_guard extends Word
    {
    	public void exec(JBForth jbf)
    	{
			L2PcInstance player = (L2PcInstance)jbf.getObject();
			L2Clan clan = player.getClan();

			if(clan.getHiredGuards()<Config.CG_MAXHIREDGUARDS&&clan.getHasCastle()!=0&&player.getInventory().getItemByItemId(5575).getCount()>Config.CG_HIREPRICE&&(player.getClanPrivileges()>=Config.CG_HIRE||player.isClanLeader()))
    		{
    			java.sql.Connection con = null;
    			 
    	        try
    	        {
    	        	clan.incrementHiredGuards();
    	            player.getInventory().destroyItemByItemId(5575,Config.CG_HIREPRICE);
    	        	con = L2DatabaseFactory.getInstance().getConnection();
    	            PreparedStatement statement;
    	            
    	            statement = con.prepareStatement("SELECT id from npc WHERE type=L2Guard");
    	            ResultSet rset = statement.executeQuery();

    	            int n=0;
    	            ArrayList<Integer> list = new ArrayList<Integer>();
    	    		while (rset.next())
   	                {
    	    			Integer num = rset.getInt("id");
    	    			list.add(num);
    	    			n++;
   	                }
    	    		n = Rnd.get(n);
    	    		L2NpcTemplate template1 = NpcTable.getInstance().getTemplate((Integer) list.get(n));
    	    		L2CastleGuardInstance castleguard = new L2CastleGuardInstance(IdFactory.getInstance().getNextId(), template1);
    	    		castleguard.setCurrentHpMp(castleguard.getMaxHp(), castleguard.getMaxMp());
    	    		castleguard.setClanId(clan);
    	    		castleguard.spawnMe(player.getX(), player.getY(), player.getZ());
    				castleguard.OnSpawn();
    				castleguard.say(L2CastleGuardInstance.JUST_HIRED);
    	        }
    	        catch (Exception e1)
    			{
    				// problem with initializing spawn, go to next one
    				_log.warning("spawn couldnt be initialized:"+e1);
    			} 
    			finally 
    			{
    				try { con.close(); } catch (Exception e) {}
    			}
    		}
    		else 
    		{
    			 NpcHtmlMessage msg = new NpcHtmlMessage(5);
    		        
    	         String s = "<html><title>Requirements for Castle Guards Hiring:</title><body><br><br><br>";
    	         s += clan.getHiredGuards() + "/" + Config.CG_MAXHIREDGUARDS +" Castle Guards Hired<br>";
    	         s += Config.CG_HIRE + " privilege level";
    	         s += "</body></html>";
    	         msg.setHtml(s);

    	         player.sendPacket(msg);
			}
    	}
    }
}
