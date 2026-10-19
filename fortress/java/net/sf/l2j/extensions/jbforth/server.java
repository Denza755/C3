package net.sf.l2j.extensions.jbforth;

import java.util.HashMap;
import java.util.logging.Logger;

import net.sf.l2j.Server;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.GameServer;
import net.sf.l2j.gameserver.SpawnTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.SetupGauge;
import ru.balancer.JBForth;
import ru.balancer.jbforth.Dict;
import ru.balancer.jbforth.Word;

public class server
{
	static Logger _log = Logger.getLogger(server.class.getName());

	protected final static HashMap<String,Integer> say2_type = new HashMap<String,Integer>();

	public server(Dict d)
	{
		say2_type.put("all", 0);
		say2_type.put("shout", 1); //!
		say2_type.put("tell", 2);
		say2_type.put("party", 3); //#
		say2_type.put("clan", 4);  //@
		say2_type.put("gm", 5);	
		say2_type.put("petition_player", 6); // used for petition
		say2_type.put("petition_gm", 7); //* used for petition
		say2_type.put("trade", 8); //+
		say2_type.put("alliance", 9); //$
		say2_type.put("announcement", 10);
		
		d.reg("announce", new announce());
		d.reg("(polymorph)", new _polymorph_());
		d.reg("log-info", new log_info());
		d.reg("coords>pos", new coords_to_pos());
		d.reg("pos@", new get_pos());
		d.reg("find-by-npc_id", new find_by_npc_id());

		d.reg("say2", new say2());

		d.reg("npc-die-map@", new npc_on_die_map());

		d.reg("new-object-id", new new_object_id());
		d.reg("server@", new server_get());
	}

	public class announce extends Word
	{
//		( "text" -- ) 
		public void exec(JBForth jbf) 
		{ 
			(new Announcements()).announceToAll(jbf.st.pop_s());
		}
	}


	public class _polymorph_ extends Word
	{
//		( "id" "type" object show -- ) 
		public void exec(JBForth jbf) 
		{ 
			Integer show = jbf.st.pop_i(); // not 0 for show skill casting animation
			L2Object obj = (L2Object)jbf.st.pop();
			String type = jbf.st.pop_s();
			String id = jbf.st.pop_s();

			obj.setPolyInfo(type, id);

			if(show != 0 && obj instanceof L2Character)
			{
				L2Character Char = (L2Character) obj;
				Char.broadcastPacket(new MagicSkillUser(Char, 1008, 1, 4000, 0));
				Char.sendPacket(new SetupGauge(0, 4000));
			}

			if(obj instanceof L2Character)
				((L2Character)obj).teleToLocation(obj.getX(),obj.getY(),obj.getZ());
		}
	}

	public class log_info extends Word
	{
//		( "text" -- ) 
//		Log text with 'info' log level.
		public void exec(JBForth jbf) 
		{ 
			_log.info(jbf.st.pop_s());
		}
	}

	public class coords_to_pos extends Word
	{
//		( x y z heading -- L2CharPosition ) 
		public void exec(JBForth jbf) 
		{ 
			int h = jbf.st.pop_i();
			int z = jbf.st.pop_i();
			int y = jbf.st.pop_i();
			int x = jbf.st.pop_i();
			jbf.st.push(new L2CharPosition(x,y,z,h));
	 	}
	}

	public class get_pos extends Word
	{
//		( char -- L2CharPosition ) 
		public void exec(JBForth jbf) 
		{ 
			L2Character ch = (L2Character)jbf.st.pop();
			jbf.st.push(new L2CharPosition(ch.getX(),ch.getY(), ch.getZ(), ch.getHeading()));
	 	}
	}

	public class find_by_npc_id extends Word
	{
//		( npc_id -- npc_object ) 
		public void exec(JBForth jbf) 
		{ 
			int npc_id = jbf.st.pop_i();
			for(L2Spawn s : SpawnTable.getInstance().getSpawnTable().values())
			{
				if(npc_id == s.getNpcid())
				{
					jbf.st.push(s.getLastSpawn());
					return;
				}
			}
			jbf.st.push(null);
	 	}
	}

	public class say2 extends Word
	{
//		( "text" "type" "sender" sender reciever -- ) 
//		Print text with type to player
		public void exec(JBForth jbf) 
		{ 
			L2PcInstance reciever = (L2PcInstance)jbf.st.pop();
			int sender   = jbf.st.pop_i();
			String from = jbf.st.pop_s();
			String type = jbf.st.pop_s().toLowerCase();
			String text = jbf.st.pop_s();

			int itype = say2_type.get(type)!=null ? say2_type.get(type) : 1;

			reciever.sendPacket(new CreatureSay(sender, itype, from, text));
	 	}
	}

	public class npc_on_die_map extends Word
	{
//		( -- HashMap ) 
//		return hashmap with on die JBForth handlers map
		public void exec(JBForth jbf) 
		{ 
			jbf.st.push(GameServer.getNpcOnDieMap());
	 	}
	}

	public class new_object_id extends Word
	{
//		( -- int ) 
//		return next free object id from IDFactory
		public void exec(JBForth jbf) 
		{ 
			jbf.st.push(IdFactory.getInstance().getNextId());
	 	}
	}

	public class server_get extends Word
	{
//		server@ ( -- GameServer ) 
//		return GameServer id
		public void exec(JBForth jbf) 
		{ 
			jbf.st.push(Server.gameServer);
	 	}
	}
}
