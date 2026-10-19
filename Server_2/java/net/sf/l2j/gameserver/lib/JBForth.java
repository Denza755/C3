// Java Forth Implementaton by Balancer

package net.sf.l2j.gameserver.lib;

import net.sf.l2j.gameserver.lib.jbf.JBCore;
import net.sf.l2j.gameserver.lib.jbf.JBData;
import net.sf.l2j.gameserver.model.L2PcInstance;

public final class JBForth
{
	public JBCore core = JBCore.getInstance();
	public JBData data;

	public JBForth()
	{
		data = new JBData();
	}

	public void reload()
	{
		data = core.reload();
	}

	public void setPlayer(L2PcInstance p)
	{
		data.player = p;
	}

	public String exec(String code)
	{
		data.stdout = "";
		try
		{
			core.exec(code, data);
	   	}
	   	catch(Exception e)
	   	{
	   		e.printStackTrace();
	   	}
		return data.stdout;
	}
}
