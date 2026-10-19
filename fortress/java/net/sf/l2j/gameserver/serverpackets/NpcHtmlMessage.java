/*
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
package net.sf.l2j.gameserver.serverpackets;

import java.util.ArrayList;

import net.sf.l2j.gameserver.lib.Files;
import net.sf.l2j.gameserver.lib.Strings;
import net.sf.l2j.gameserver.model.BypassEngine;
import net.sf.l2j.gameserver.model.L2PcInstance;


/**
 *
 * the HTML parser in the client knowns these standard and non-standard tags and attributes 
 * VOLUMN
 * UNKNOWN
 * UL
 * U
 * TT
 * TR
 * TITLE
 * TEXTCODE
 * TEXTAREA
 * TD
 * TABLE
 * SUP
 * SUB
 * STRIKE
 * SPIN
 * SELECT
 * RIGHT
 * PRE
 * P
 * OPTION
 * OL
 * MULTIEDIT
 * LI
 * LEFT
 * INPUT
 * IMG
 * I
 * HTML
 * H7
 * H6
 * H5
 * H4
 * H3
 * H2
 * H1
 * FONT
 * EXTEND
 * EDIT
 * COMMENT
 * COMBOBOX
 * CENTER
 * BUTTON
 * BR
 * BODY
 * BAR
 * ADDRESS
 * A
 * SEL
 * LIST
 * VAR
 * FORE
 * READONL
 * ROWS
 * VALIGN
 * FIXWIDTH
 * BORDERCOLORLI
 * BORDERCOLORDA
 * BORDERCOLOR
 * BORDER
 * BGCOLOR
 * BACKGROUND
 * ALIGN
 * VALU
 * READONLY
 * MULTIPLE
 * SELECTED
 * TYP
 * TYPE
 * MAXLENGTH
 * CHECKED
 * SRC
 * Y
 * X
 * QUERYDELAY
 * NOSCROLLBAR
 * IMGSRC
 * B
 * FG
 * SIZE
 * FACE
 * COLOR
 * DEFFON
 * DEFFIXEDFONT
 * WIDTH
 * VALUE
 * TOOLTIP
 * NAME
 * MIN
 * MAX
 * HEIGHT
 * DISABLED
 * ALIGN
 * MSG
 * LINK
 * HREF
 * ACTION
 *	
 *
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class NpcHtmlMessage extends ServerBasePacket
{
	// d S
	// d is usually 0, S is the html text starting with <html> and ending with </html>
	//
	private static final String _S__1B_NPCHTMLMESSAGE = "[S] 0f NpcHtmlMessage";
	private int _messageId;
	private String _html;
	private String _file = null;
	private ArrayList<String> replaces = new ArrayList<String>();

	/**
	 * @param _characters
	 */
	public NpcHtmlMessage(int messageId)
	{
		_messageId = messageId;
	}
	
	public final NpcHtmlMessage setHtml(String text)
	{
		if(!text.contains("<html>"))
			text = "<html><body>" + text + "</body></html>"; //<title>Message:</title> <br><br><br>

		_html = text;

		return this;
	}

	public final NpcHtmlMessage setFile(String file)
	{
		_file = file;
		return this;
	}

	private final void _setFile()
	{
//		_log.info("Set file to "+_file);
		String content = html_load(_file, getClient().getActiveChar());

		if(content==null)
			content = _file;
		
		setHtml(content);
	}

	protected String html_load(String name, L2PcInstance player)
	{
		return Files.read(name, player);
	}

	protected String html_load(String name, String lang)
	{
		String content = Files.read(name, lang);
		if(content == null)
			content = "Can't find file'"+name+"'";
		return content;
	}

	public void replace(String pattern, String value)
	{
		replaces.add(pattern);
		replaces.add(value);
	}

	final void runImpl()
	{
		// no long-running tasks
	}
	
	final void writeImpl()
	{
		if(_file != null)
			_setFile();

		L2PcInstance player = getClient().getActiveChar();

		if(player == null)
			return;
			
		String html = player.jbf.parse(_html);

		for(int i=0; i<replaces.size(); i+=2)
			html = html.replaceAll(replaces.get(i), replaces.get(i+1));

		if(!player.getVarB("translit@not"))
			html = Strings.translit(html);

		html = BypassEngine.encode(getClient().getActiveChar(), html);
		writeC(0x0f);

		writeD(_messageId);
		writeS(html);
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__1B_NPCHTMLMESSAGE;
	}

}
