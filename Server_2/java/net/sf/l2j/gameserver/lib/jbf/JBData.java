// Java Forth Implementaton by Balancer

package net.sf.l2j.gameserver.lib.jbf;

import net.sf.l2j.gameserver.lib.jbf.words.WordState;
import net.sf.l2j.gameserver.model.L2PcInstance;

public final class JBData
{
    public Stack st  = new Stack(1024);
    public Stack rst = new Stack(64);
    public Stack fst = new Stack(16);
    public Stack sst = new Stack(16);

    public WordState cws;

	public Variable in = new Variable();
    public Variable warning = new Variable();

	public String commands;

	public L2PcInstance player;

	public String stdout = "";

}
