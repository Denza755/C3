// Java Forth Implementaton by Balancer
// Events handler

package net.sf.l2j.gameserver.lib;

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2NpcInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.Location;
import ru.balancer.JBForth;
import ru.balancer.jbforth.Word;

public final class Events
{
	static Logger _log = Logger.getLogger(Events.class.getName());

	public final static void onEvent(L2NpcInstance npc, L2PcInstance player)
	{
		int npc_id = npc.getTemplate().npcId;
		if(player.jbf.word_exists("on-npc-"+npc_id+"-event"))
		{
			JBForth jbf = new JBForth(npc);
			jbf.st.push(npc);
			jbf.exec("to self");
			jbf.st.push(player);
			jbf.exec("to player");
			jbf.execp("on-npc-"+npc_id+"-event");
	  	}
	}

	public final static Location onEscape(L2PcInstance player)
	{
		if(player.jbf.word_exists("on-player-escape"))
		{
			JBForth jbf = new JBForth(player);
//			jbf.setPlayer(player);
			jbf.st.push(player);	jbf.exec("to self");
			jbf.execp("on-player-escape");
			int z = jbf.st.pop_i();
			int y = jbf.st.pop_i();
			int x = jbf.st.pop_i();
			
			if(x!=0 || y!=0 || z!=0)
				return new Location(x,y,z);

	  	}
		return null;
	}

	@SuppressWarnings("static-access")
	public final static boolean onAction(L2PcInstance player, L2Object obj)
	{
		String name = obj.getL2ClassShortName()+".onAction";

		if(Config.DEBUG)
			System.out.println("Handler for "+player+".onAction("+obj+") = "+name);

		Word handler = player.jbf.find_word(name);
		if(handler == null)
			return false;

		JBForth jbf = new JBForth(player);
		jbf.uvset("object", obj);

		return Strings.parseBoolean(handler.execpr(jbf));
	}

	@SuppressWarnings("static-access")
	public final static boolean onActionShift(L2PcInstance player, L2Object obj)
	{
		String name = obj.getL2ClassShortName()+".onActionShift";
		if(Config.DEBUG)
			System.out.println("Handler for "+player+".onActionShift("+obj+") = "+name);

		Word handler = player.jbf.find_word(name);
		if(handler == null)
			return false;

		JBForth jbf = new JBForth(player);
		jbf.uvset("object", obj);

		return Strings.parseBoolean(handler.execpr(jbf));
	}
}
