package net.sf.l2j.extensions.jbforth;

import ru.balancer.JBForth;
import ru.balancer.jbforth.IPrintHandler;
import ru.balancer.libs.Strings;

import net.sf.l2j.gameserver.model.L2PcInstance;

public class L2PrintHandler implements IPrintHandler
{
	public void run(JBForth jbf)
	{
		String s = jbf.stdout.trim();
		jbf.stdout = "";

		if(s == "")
			return;

		if(jbf.getObject() != null && jbf.getObject() instanceof L2PcInstance)
		{
			((L2PcInstance)jbf.getObject()).sendMessage(s);
			return;
		}

		if(JBForth.is_true(Strings.parseInt(jbf.uvget("telnet?"))))
			jbf.stdout = s;
		else
			System.out.println(s);
	}
}
