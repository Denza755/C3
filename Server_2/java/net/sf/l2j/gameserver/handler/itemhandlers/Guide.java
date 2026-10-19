package net.sf.l2j.gameserver.handler.itemhandlers;

import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2NpcInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2PlayableInstance;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;

public class Guide implements IItemHandler {
	private static int[] _itemIds = { 5588 };
	private static Logger _log = Logger.getLogger(L2NpcInstance.class.getName());
	
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		L2PcInstance activeChar = (L2PcInstance) playable;
		String filename = "data/html/tutorial/"+item.getItemId()+".html";
		File file = new File(Config.DATAPACK_ROOT, filename);
		if (!file.exists())
		{
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			html.setHtml("<html><head><body>My Text is missing:<br>"+filename+"</body></html>");
			activeChar.sendPacket(html);
			
			activeChar.sendPacket( new ActionFailed() );
		}
		else
		{
			FileInputStream fis = null;		
			try
			{
				fis = new FileInputStream(file);
				byte[] raw = new byte[fis.available()];
				fis.read(raw);
				
				String content = new String(raw, "UTF-8");
				NpcHtmlMessage itemReply = new NpcHtmlMessage(5);
				itemReply.setHtml(content);
				activeChar.sendPacket(itemReply);
			}
			catch (Exception e)
			{
				_log.warning("problem with tutorial guide text "+e);
			}
			finally
			{
				try	{ fis.close(); } catch (Exception e1) {/* ignore this */}
			}
		}
		
		activeChar.sendPacket( new ActionFailed() );
	}

	public int[] getItemIds()
	{
		return _itemIds;
	}
}
