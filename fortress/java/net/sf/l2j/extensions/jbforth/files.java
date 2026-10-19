package net.sf.l2j.extensions.jbforth;

import java.io.File;
import java.util.logging.Logger;

import ru.balancer.JBForth;
import ru.balancer.jbforth.*;
import net.sf.l2j.gameserver.lib.Files;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;

public class files
{
	static Logger _log = Logger.getLogger(files.class.getName());

	public files(Dict d)
	{
		d.reg("show", new show());
		d.reg("p.show", new pshow());
	}

	public class show extends Word
	{
// 		show  ( "text" -- ) or
// 		show  ( "path/file/name" -- )
// 		Show text in html-dialog or load html file from data/jbforth
		public void exec(JBForth jbf) 
		{ 

//			String parent = ((File)jbf.current_file.get()).getParent();
//			String current_path = parent.replaceAll("^.*data/jbforth/", "");

			Object player = jbf.getObject();
			if(player != null && player instanceof L2PcInstance)
				_show((L2PcInstance)player, jbf.st.pop_s(), jbf.ws != null ? jbf.ws.word.getFile() : null);

		}
	}

	public class pshow extends Word
	{
// 		p.show ( "text" player -- ) or
// 		p.show ( "path/file/name" -- )
// 		Show html-dialog to player or load html file from data/jbforth
		public void exec(JBForth jbf) 
		{ 
			_show((L2PcInstance)jbf.st.pop(), jbf.st.pop_s(), jbf.ws.word.getFile());
		}
	}

	protected void _show(L2PcInstance player, String text, File parent)
	{
		NpcHtmlMessage msg = new NpcHtmlMessage(5);
		
		if(text.matches(".*\\.htm"))
		{
			String content = null;
			if(parent != null)
				content = Files.read(parent.getParent()+"/"+text, player);
			if(content == null)
				content = Files.read("data/"+text, player);
			if(content != null)
				text = content;
		}
		
		msg.setHtml(text);

		player.sendPacket(msg);
	}
}
