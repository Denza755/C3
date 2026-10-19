package net.sf.l2j.extensions.jbforth;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.logging.Logger;

import ru.balancer.JBForth;
import ru.balancer.jbforth.Dict;
import ru.balancer.jbforth.Word;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.serverpackets.ServerBasePacket;

public class characters
{
	static Logger _log = Logger.getLogger(characters.class.getName());

	public characters(Dict d)
	{
//		d.reg("PLAYER!", new player_sto());
		d.reg("PLAYER@", new player_at());

		d.reg("mdef", new get_mdef());
		d.reg("matk", new get_matk());
		d.reg("pdef", new get_pdef());
		d.reg("patk", new get_patk());

		d.reg("around-players", new get_around_players());
		d.reg("do-player", new do_player());
		d.reg("p-do-player", new p_do_player());
		d.reg("do-players", new do_players());
		d.reg("p-do-players", new p_do_players());

		d.reg("broadcast", new broadcast());
	}

/*	public class player_sto extends Word
	{
		public void exec(JBForth jbf)
		{ 
			jbf.setPlayer((L2PcInstance)jbf.st.pop());
		}
	}*/

	public class player_at extends Word
	{
		public void exec(JBForth jbf) 
		{ 
			jbf.st.push(jbf.getObject());
		}
	}

	public class get_matk extends Word
	{
		// ( char target skill -- matk ) 
		public void exec(JBForth jbf) 
		{
			L2Skill skill = (L2Skill)jbf.st.pop();
			L2Character target = (L2Character)jbf.st.pop();
			L2Character ch = (L2Character)jbf.st.pop();
			jbf.st.push(ch.getMAtk(target, skill));
		}
 	}

	public class get_mdef extends Word
	{
		// ( char target skill -- mdef ) 
		public void exec(JBForth jbf) 
		{
			L2Skill skill = (L2Skill)jbf.st.pop();
			L2Character target = (L2Character)jbf.st.pop();
			L2Character ch = (L2Character)jbf.st.pop();
			jbf.st.push(ch.getMDef(target, skill));
		}
 	}

	public class get_patk extends Word
	{
		// ( char target "type" -- patk ) 
		public void exec(JBForth jbf) 
		{
			String type = jbf.st.pop_s();
			L2Character target = (L2Character)jbf.st.pop();
			L2Character ch = (L2Character)jbf.st.pop();
			if(type==null || type=="")
			{
				jbf.st.push(ch.getPAtk(target));
		   	}
		   	else
		   	{
				try
				{
					Method m = ch.getClass().getMethod("getPatk"+type, L2Character.class);
					jbf.st.push(m.invoke(target));
				} catch(Exception e){_log.warning(""+e); e.printStackTrace();}
		   	}
		}
 	}

	public class get_pdef extends Word
	{
		// ( char target -- pdef ) 
		public void exec(JBForth jbf) 
		{
			L2Character target = (L2Character)jbf.st.pop();
			L2Character ch = (L2Character)jbf.st.pop();
			jbf.st.push(ch.getPDef(target));
		}
 	}


	public class get_around_players extends Word
	{
		// ( char distance -- {players} ) 
		public void exec(JBForth jbf) 
		{
			int distance = jbf.st.pop_i();
			L2Character ch = (L2Character)jbf.st.pop();

			ArrayList<L2PcInstance> players = new ArrayList<L2PcInstance>();
			
			for(L2PcInstance player : L2World.getAllPlayers())
				if(ch.getDistance(player) < distance)
					players.add(player);

			jbf.st.push(players);
		}
 	}

	public class do_player extends Word
	{	
		// ( "code" player -- )
		public void exec(JBForth jbf) 
		{ 
			L2PcInstance player = (L2PcInstance)jbf.st.pop();
			Object code = jbf.st.pop();

			JBForth jbf_tmp = new JBForth(player);
			jbf_tmp.uvset("caller", jbf.getObject());

			if(code instanceof String)
				jbf_tmp.execp((String)code);
			else
			{
				try	{ ((Word)code).exec(jbf_tmp); }
				catch(Exception e) { printStackTrace(jbf_tmp, e); }
			}
		}
	}

	public class p_do_player extends Word
	{	
		// ( args "code" player -- )
		public void exec(JBForth jbf) 
		{ 
			L2PcInstance player = (L2PcInstance)jbf.st.pop();
			Object code = jbf.st.pop();
			Object args = jbf.st.pop();

			JBForth jbf_tmp = new JBForth(player);
			jbf_tmp.st.push(args);
			jbf_tmp.uvset("caller", jbf.getObject());
			if(code instanceof String)
				jbf_tmp.execp((String)code);
			else
			{
				try	{ ((Word)code).exec(jbf_tmp); }
				catch(Exception e) { printStackTrace(jbf_tmp, e); }
			}
		}
	}

	public class do_players extends Word
	{	
		// ( "code" | 'code -- )
		public void exec(JBForth jbf) 
		{ 
			Object code = jbf.st.pop();

			if(code instanceof String)
			{
				for(L2PcInstance player : L2World.getAllPlayers())
				{
					JBForth jbf_tmp = new JBForth(player);
					jbf_tmp.uvset("caller", jbf.getObject());
					jbf_tmp.exec((String)code);
				}
			}
			else
			{
				for(L2PcInstance player : L2World.getAllPlayers())
				{
					JBForth jbf_tmp = new JBForth(player);
					jbf_tmp.uvset("caller", jbf.getObject());
					try	{ ((Word)code).exec(jbf_tmp); }
					catch(Exception e) { printStackTrace(jbf_tmp, e); }
				}
			}
		}
	}
	
	public class p_do_players extends Word
	{	
		// ( args "code"|'code -- )
		public void exec(JBForth jbf) 
		{ 
			Object code = jbf.st.pop();
			Object args = jbf.st.pop();

			if(code instanceof String)
			{
				for(L2PcInstance player : L2World.getAllPlayers())
				{
					JBForth jbf_tmp = new JBForth(player);
					jbf_tmp.uvset("caller", jbf.getObject());
					jbf_tmp.st.push(args);
					jbf_tmp.exec((String)code);
				}
			}
			else
			{
				for(L2PcInstance player : L2World.getAllPlayers())
				{
					JBForth jbf_tmp = new JBForth(player);
					jbf_tmp.uvset("caller", jbf.getObject());
					jbf_tmp.st.push(args);
					try	{ ((Word)code).exec(jbf_tmp); }
					catch(Exception e) { printStackTrace(jbf_tmp, e); }
				}
			}
		}
	}

	public class broadcast extends Word
	{	
// 		broadcast  ( serverpacket player -- )
		public void exec(JBForth jbf) 
		{ 
			L2PcInstance player = (L2PcInstance)jbf.st.pop();
			player.broadcastPacket((ServerBasePacket)jbf.st.pop());
		}
	}
}
