package net.sf.l2j.gameserver.lib.jbf.words;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.NpcTable;
import net.sf.l2j.gameserver.SpawnTable;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.lib.jbf.JBData;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.serverpackets.Dice;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2j
{
    static Logger _log = Logger.getLogger(L2j.class.getName());

	public static void init(Dict d)
	{
		d.reg("PLAYER!", new player_sto());
		d.reg("PLAYER@", new player_at());
		d.reg("ADENA@", new adena_at());
		d.reg("ADENA+!", new adena_add());
		d.reg("SHOW", new show());
        d.reg("COORDS@", new coords_at());
        d.reg("TELEPORT_PLAYER_TO", new teleport_player_to());
        d.reg("NPC_NAME>ID", new npc_name_to_id());
        d.reg("NPC_ID>NAME", new npc_id_to_name());
        d.reg("NPC_ID>SPAWN", new npc_id_to_spawn());
        d.reg("JBF_RELOAD", new jbf_reload());
        d.reg("DICE", new dice());
        d.reg(".SET", new dset());
        d.reg(".GET", new dget());
	}

	public static class player_sto extends Word
	{
        public void exec(JBData data)
		{ 
            data.player = (L2PcInstance)data.st.pop();
		}
	}

	public static class player_at extends Word
	{
        public void exec(JBData data) 
		{ 
            data.st.push(data.player);
		}
	}

	public static class adena_at extends Word
	{
        public void exec(JBData data) 
		{ 
            data.st.push(data.player.getAdena());
		}
	}

	public static class adena_add extends Word
	{
        public void exec(JBData data) 
		{ 
            data.player.addAdena((Integer)data.st.pop());
		}
	}

	public static class show extends Word
	{
        public void exec(JBData data) 
		{ 
			NpcHtmlMessage msg = new NpcHtmlMessage(5);
		
            String s = "<html><title>Message</title><br><br>\n";
            s += data.st.pop();
			s += "</body></html>";
			msg.setHtml(s);

            data.player.sendPacket(msg);
        }
    }

    public static class coords_at extends Word
    {
        public void exec(JBData data) 
        { 
            // ( character -- x y z )
            Object x = data.st.pop();

            if(x instanceof L2Character)
            {
                L2Character ch = (L2Character)x;
                data.st.push(ch.getX());
                data.st.push(ch.getY());
                data.st.push(ch.getZ());
                return;
            }

            if(x instanceof L2Spawn)
            {
                L2Spawn sp = (L2Spawn)x;
                data.st.push(sp.getLocx());
                data.st.push(sp.getLocy());
                data.st.push(sp.getLocz());
                return;
            }

            data.st.push(null);
            data.st.push(null);
            data.st.push(null);
        }
    }

    public static class teleport_player_to extends Word
    {
        public void exec(JBData data) 
        { 
            // ( character x y z -- )
            int z = (Integer)data.st.pop();
            int y = (Integer)data.st.pop();
            int x = (Integer)data.st.pop();
            L2PcInstance player = (L2PcInstance)data.st.pop();
        
            player.teleToLocation(x, y, z);
            if(player.getPet() != null)
            {
                player.getPet().teleToLocation(x+100, y, z);
            }
        }
    }

    public static class npc_name_to_id extends Word
    {
        public void exec(JBData data) 
        { 
            // ( "name" -- npc_id )
            String name = (String)data.st.pop();
			L2NpcTemplate npc = NpcTable.getInstance().getTemplateByName(name);
			if (npc != null)
				data.st.push(npc.npcId);
			else
				data.st.push(null);
        }
    }

    public static class npc_id_to_name extends Word
    {
        public void exec(JBData data) 
        { 
            // ( "name" -- npc_id )
            int npc_id = (Integer)data.st.pop();
            L2NpcTemplate npc = NpcTable.getInstance().getTemplate(npc_id);
			if (npc != null)
				data.st.push(npc.name);
			else
				data.st.push(null);
        }
    }

    public static class npc_id_to_spawn extends Word
    {
        public void exec(JBData data) 
        { 
            // ( npc_id -- spawn )
            int npc_id = (Integer)data.st.pop();
            for(L2Spawn spawn : SpawnTable.getInstance().getSpawnTable().values())
            {
//              System.out.println("Test NPC spawn'"+spawn.getNpcid()+"' to '"+npc_id+"'");
                if(spawn.getNpcid() == npc_id)
                {
                    data.st.push(spawn);
                    return;
                }
            }
            data.st.push(null);
        }
    }

    public static class jbf_reload extends Word
    {
        public void exec(JBData data) 
        { 
            // ( -- )
            jbc.reload();
        }
    }

    public static class dice extends Word
    {
        public void exec(JBData data) 
        { 
            // ( player item_id -- number )

            int number = Rnd.get(1,6);
            int item_id = (Integer)data.st.pop();
            L2PcInstance activeChar = (L2PcInstance)data.st.pop();

            Dice d = new Dice (activeChar.getObjectId(), item_id, number, activeChar.getX()-30+Rnd.get(60), activeChar.getY()-30+Rnd.get(60), activeChar.getZ());

            activeChar.sendPacket(d);
            activeChar.broadcastPacket(d);
            SystemMessage sm = new SystemMessage(0x0342);
            sm.addString(activeChar.getName());
            sm.addNumber(number);

            activeChar.sendPacket(sm);
            activeChar.broadcastPacket(sm);

            data.st.push(number);
        }
    }


    public static class dset extends Word
    {
        // ( "value" oid field -- )
        public void exec(JBData data) 
        { 
            String method = "set"+data.st.pop();
            Object obj    = data.st.pop();

            try
            {
                Method m = obj.getClass().getMethod(method, String.class);
                m.invoke(obj, data.st.pop());
            } catch(Exception e){_log.warning(""+e); e.printStackTrace();}
        }
    }

    public static class dget extends Word
    {
        // ( oid field -- "value" )
        public void exec(JBData data) 
        { 
            String method = "get"+data.st.pop();
            Object obj    = data.st.pop();

            try
            {
                Method m = obj.getClass().getMethod(method);
                data.st.push(m.invoke(obj));
            } catch(Exception e){_log.warning(""+e); e.printStackTrace();}
		}
	}
}
