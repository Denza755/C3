package net.sf.l2j.extensions.jbforth;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.NpcTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2DropData;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import ru.balancer.JBForth;
import ru.balancer.jbforth.Dict;
import ru.balancer.jbforth.Word;

public class l2j
{
    static Logger _log = Logger.getLogger(l2j.class.getName());

    public l2j(Dict d)
    {
        d.reg("COORDS@", new coords_at());
        d.reg("TELEPORT_PLAYER_TO", new teleport_player_to());
        d.reg("TELEPORT-PLAYER-TO", new teleport_player_to2());
        d.reg("TELEPORT-CHAR-TO", new teleport_player_to2());
        d.reg("NPC_NAME>ID", new npc_name_to_id());
        d.reg("NPC_ID>NAME", new npc_id_to_name());
        d.reg("NPC_ID>SPAWN", new npc_id_to_spawn());
        d.reg("set(d)", new set_d());
        d.reg("get(ch)", new prop_get_ch());
        d.reg("TARGET@", new target_at());
        d.reg("REMOVE-FROM-ALL-PLAYERS", new remove_from_all_players());
        d.reg("CLASS?", new getclass());
        d.reg("CLASS!", new setclass());
        d.reg("jbf@", new get_jbf());
        d.reg("sprintchance", new sprintchance());
        d.reg("strreplace", new strreplace());
        d.reg("addrop-level-beyond", new addrop_level_beyond());
        d.reg("addrop", new addrop());
    }

    public class coords_at extends Word
    {
        public void exec(JBForth jbf)
        {
            // ( character -- x y z )
            Object x = jbf.st.pop();

            if(x instanceof L2Character)
            {
                L2Character ch = (L2Character)x;
                jbf.st.push(ch.getX());
                jbf.st.push(ch.getY());
                jbf.st.push(ch.getZ());
                return;
            }

            if(x instanceof L2Spawn)
            {
                L2Spawn sp = (L2Spawn)x;
                jbf.st.push(sp.getLocx());
                jbf.st.push(sp.getLocy());
                jbf.st.push(sp.getLocz());
                return;
            }

            jbf.st.push(null);
            jbf.st.push(null);
            jbf.st.push(null);
        }
    }

    public class teleport_player_to extends Word
    {
        public void exec(JBForth jbf)
        {
            // ( character x y z -- )
            int z = jbf.st.pop_i();
            int y = jbf.st.pop_i();
            int x = jbf.st.pop_i();
            L2Character cha = (L2Character)jbf.st.pop();

            cha.teleToLocation(x, y, z);
        }
    }

    public class teleport_player_to2 extends Word
    {
        public void exec(JBForth jbf)
        {
            // ( x y z player -- )
            L2Character cha = (L2Character)jbf.st.pop();
            int z = jbf.st.pop_i();
            int y = jbf.st.pop_i();
            int x = jbf.st.pop_i();

            cha.teleToLocation(x, y, z);
        }
    }

    public class npc_name_to_id extends Word
    {
        public void exec(JBForth jbf)
        {
/*            // ( "name" -- npc_id )
            String name = (String)jbf.st.pop();
            for(L2NpcTemplate npc_tpl : NpcTable.getInstance().getNpcTemplatesList())
            {
//              System.out.println("Test NPC '"+npc_tpl.getName()+"' to '"+name+"'");
                if(name.equals(npc_tpl.name))
                {
                    jbf.st.push(npc_tpl.npcId);
                    return;
                }
            }*/

            jbf.st.push(null);
        }
    }

    public class npc_id_to_name extends Word
    {
        public void exec(JBForth jbf)
        {
            // ( "name" -- npc_id )
            int npc_id = jbf.st.pop_i();
            L2NpcTemplate npc_template = NpcTable.getInstance().getTemplate(npc_id);
            jbf.st.push(npc_template.name);
        }
    }

    public class npc_id_to_spawn extends Word
    {
        public void exec(JBForth jbf)
        {
            // ( npc_id -- spawn )
/*            int npc_id = (Integer)jbf.st.pop();
            for(L2WorldRegion region : L2World.getAllRegions())
            {
	            for(L2Spawn spawn : region.getSpawnTable().getSpawnList())
    	        {
//	              System.out.println("Test NPC spawn'"+spawn.getNpcid()+"' to '"+npc_id+"'");
    	            if(spawn.getNpcid() == npc_id)
        	        {
            	        jbf.st.push(spawn);
                	    return;
	                }
    	        }
	        }
*/       	    jbf.st.push(null);
		}
    }

    public class set_d extends Word
    {
        // ( double. oid field -- )
        public void exec(JBForth jbf)
        {
            String method = "set"+jbf.st.pop();
            Object obj    = jbf.st.pop();

            try
            {
                Method m = obj.getClass().getMethod(method, Double.class);
                m.invoke(obj, 0.0+jbf.st.pop_f());
            } catch(Exception e){_log.warning(""+e); e.printStackTrace();}
        }
    }

    public class prop_get_ch extends Word
    {
        // ( char-obj oid "Field" -- x )
        // call getField(char-obj) function, return object x
        public void exec(JBForth jbf)
        {
            String method = "get"+jbf.st.pop();
            Object obj    = jbf.st.pop();

            try
            {
                Method m = obj.getClass().getMethod(method, L2Character.class);
                jbf.st.push(m.invoke(obj, (L2Character)jbf.st.pop()));
            } catch(Exception e){_log.warning(""+e); e.printStackTrace();}
        }
    }

    public class target_at extends Word
    {
    	// ( character_oid -- target_oid )
        public void exec(JBForth jbf)
        {
            Object ch = jbf.st.pop();
            jbf.st.push((ch != null && ch instanceof L2Character) ? ((L2Character)ch).getTarget() : new Integer(0));
        }
    }

    public class remove_from_all_players extends Word
    {
    	// ( character_oid -- )
        public void exec(JBForth jbf)
        {
            L2World.removeFromAllPlayers((L2PcInstance)jbf.st.pop());
        }
    }

	public class getclass extends Word
    {
//    	( player -- classId )
        public void exec(JBForth jbf)
        {
        	L2PcInstance player = (L2PcInstance)jbf.st.pop();
			jbf.st.push(player.getClassId().getId());
        }
 	}

	public class setclass extends Word
    {
//    	( classId player -- )
        public void exec(JBForth jbf)
        {
			L2PcInstance player = (L2PcInstance)jbf.st.pop();
			int classId = jbf.st.pop_i();
			player.setClassId(classId);
			player.broadcastUserInfo();
//			jbf.st.push(player.getClassId().getId());
			//SystemMessage sm = new SystemMessage(614);
			//sm.addString("You accupation changed to "+player.getTemplate().className);
			jbf.stdout +="You accupation changed to "+player.getClassId();
		}
 	}

	public class get_jbf   extends Word { public void exec(JBForth jbf) { jbf.st.push(jbf);} }

	/* ( x format -- string )
	 * Formatted rounded print of number.
	 * Returns string, rounded to integer if >10, rounded like x.xx if >1, rounded like 0.xxxx if <1
	 * P.S. by Abaddon: ---------------------------------------
	 * P.P.S by Balancer: pishite ili v UTF-8 ili translitom!
	 */
	public class sprintchance extends Word
	{
		public void exec(JBForth jbf)
		{
			double tmp = jbf.st.pop_f();
			if (tmp >= 10. || tmp == (int) tmp)tmp = (int) tmp;
			if (tmp < 10 && tmp >= 1)
			{
				tmp *= 100.;
				tmp = Math.round(tmp) / 100.;
			}
			if (tmp < 1)
			{
				String tmp1 = Double.toString(tmp);
				while ( (Integer.parseInt(tmp1.substring(tmp1.length() - 1)) == 1 || Integer.parseInt(tmp1.substring(tmp1.length() - 1)) == 9) && tmp1.length()>3 && Integer.parseInt(tmp1.substring(2, tmp1.length()-1)) > 0)
				{
					if(Integer.parseInt(tmp1.substring(tmp1.length() - 1)) == 1)
					{
						tmp1 = tmp1.substring(0, tmp1.length() - 1);
					}
					if(Integer.parseInt(tmp1.substring(tmp1.length() - 1)) == 9)
					{
						if(Integer.parseInt(tmp1.substring(tmp1.length() - 2,tmp1.length() - 1)) != 9)
						{
							tmp1 = tmp1.substring(0, tmp1.length() - 2) + (Integer.parseInt(tmp1.substring(tmp1.length()-2, tmp1.length()-1))+1);
						}
						else
						{
							tmp1 = tmp1.substring(0, tmp1.length() - 1);
						}
					}
				}
				tmp = Double.parseDouble(tmp1);
			}
			if ( tmp == (int) tmp ){
				jbf.st.push( (int) tmp);
			}
			else
				jbf.st.push(tmp);
		}

	}

	public class strreplace extends Word
	{
		public void exec(JBForth jbf)
		{

			String rep2 = jbf.st.pop_s();
			String rep = jbf.st.pop_s();
			String str = jbf.st.pop_s();
			str = str.replaceAll(rep, rep2);
			jbf.st.push(str);

		}
 	}
	
	public class addrop_level_beyond extends Word
	{
		public void exec(JBForth jbf)
		{

			int level = jbf.st.pop_i();
			int itemID = jbf.st.pop_i();
			int min = jbf.st.pop_i();
			int max = jbf.st.pop_i() * level / 30;
			int chance = jbf.st.pop_i();
			
			//System.out.println("Forth send: "+level+" "+itemID+" "+min+" "+max+" "+chance);
			
			while (level <= 95)
			{
				L2NpcTemplate[] npcs = NpcTable.getInstance().getAllOfLevel(level);
				for (L2NpcTemplate n : npcs)
				{
					if (n != null){ //TODO:     
					L2DropData drop = new L2DropData();
					drop.setItemId(itemID);
					drop.setMinDrop(min);
					drop.setMaxDrop(max);
					drop.setSweep(false);
					drop.setChance(chance);
					n.addDropData(drop);
					//System.out.println("Added drop: "+n.npcId+" "+n.name+" - "+itemID+" "+min+" "+max+" "+chance);
					}
				}
				level=level+1;
			}
		}
 	}

	public class addrop extends Word
	{
		public void exec(JBForth jbf)
		{

			int mob_id = jbf.st.pop_i();
			int itemID = jbf.st.pop_i();
			int min = jbf.st.pop_i();
			int max = jbf.st.pop_i();
			boolean sweep = jbf.st.pop_i()==1;
			int chance = jbf.st.pop_i();
			
			L2NpcTemplate npc = NpcTable.getInstance().getTemplate(mob_id);
	        if (npc == null)
	        {
	            throw new NullPointerException();
	        }
	        L2DropData drop = new L2DropData();
	        drop.setItemId(itemID);
	        drop.setMinDrop(min);
	        drop.setMaxDrop(max);
	        drop.setSweep(sweep);
	        drop.setChance(chance);
	        npc.addDropData(drop);
		}
 	}
}
