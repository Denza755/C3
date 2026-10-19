package net.sf.l2j.extensions.jbforth;

//import java.util.logging.Logger;
import net.sf.l2j.gameserver.NpcTable;
import net.sf.l2j.gameserver.SpawnTable;
import ru.balancer.JBForth;
import ru.balancer.jbforth.Dict;
import ru.balancer.jbforth.Word;
import net.sf.l2j.gameserver.model.L2NpcInstance;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class spawn
{
//    static Logger _log = Logger.getLogger(L2j_spawn.class.getName());

    public spawn(Dict d)
    {
        d.reg("spawn", new _spawn());
        d.reg("unspawn", new unspawn());
        d.reg("npc>spawn", new npc_to_spawn());
        d.reg("spawn>npc", new spawn_to_npc());
    }

    public class _spawn extends Word
    {
//    	( x y z heading npc_id respawn_time save_db -- npc-object )
        public void exec(JBForth jbf) 
        {
			boolean save_db = (jbf.st.pop_i() == 0) ? false : true;
			int respawn_time = jbf.st.pop_i();
			int npc_id = jbf.st.pop_i();
			int heading = jbf.st.pop_i();
			int z = jbf.st.pop_i();
			int y = jbf.st.pop_i();
			int x = jbf.st.pop_i();

			L2NpcTemplate template = NpcTable.getInstance().getTemplate(npc_id);

/*        Pattern pattern =Pattern.compile("[0-9]*");
        Matcher regexp = pattern.matcher(monsterId);
        L2NpcTemplate template1;
        if (regexp.matches())
        {
         //First parameter was an ID number
            int monsterTemplate = Integer.parseInt(monsterId);
            template1 = NpcTable.getInstance().getTemplate(monsterTemplate);
        }
        else {
//       //First parameter wasn't just numbers so go by name not ID
         monsterId = monsterId.replace('_', ' ');
         template1 = NpcTable.getInstance().getTemplateByName(monsterId); 
        }
*/
	        
			if (template == null)
			{
				jbf.st.push(null);
        	    return;
	        }

	        try
    	    {
    		    L2Spawn spawn = new L2Spawn(template);
        		spawn.setLocx(x);
		        spawn.setLocy(y);
    		    spawn.setLocz(z);
	    	    spawn.setAmount(1);
    	    	spawn.setHeading(heading);
	        	spawn.setRespawnDelay(respawn_time > 0 ? respawn_time : 86400*3653); // 10 years
		        SpawnTable.getInstance().addNewSpawn(spawn, save_db);
    		    spawn.init();
        		if(respawn_time == 0)
					spawn.stopRespawn();

				jbf.st.push(spawn.getLastSpawn());
    	    }
        	catch (Exception e) { jbf.st.push(null); }
	 	}
	}

    public class unspawn extends Word
    {
//    	( npc-object save_db -- )
        public void exec(JBForth jbf) 
        {
			boolean save_db = (jbf.st.pop_i() == 0) ? false : true;
            L2NpcInstance ch = (L2NpcInstance)jbf.st.pop();
            ch.deleteMe();
            L2Spawn spawn = ch.getSpawn();

            spawn.stopRespawn();
            SpawnTable.getInstance().deleteSpawn(spawn, save_db);
        }
	}

    public class npc_to_spawn extends Word
    {
//    	( npc-object -- spawn )
        public void exec(JBForth jbf) 
        {
			jbf.st.set(((L2NpcInstance)jbf.st.top()).getSpawn());
        }
	}

    public class spawn_to_npc extends Word
    {
//    	( npc-object -- spawn )
        public void exec(JBForth jbf) 
        {
			jbf.st.set(((L2Spawn)jbf.st.top()).getLastSpawn());
        }
	}
}
