/* 
	coded by Balancer
	balancer@balancer.ru
	http://balancer.ru

	version 0.1, 2005-10-31
*/

package net.sf.l2j.gameserver.model;

public class BypassStorage
{
	private static final long MAX_STORAGE_TIME = 1200000; // 20 minutes

	public String code;
	public long time;
	public final L2PcInstance player;
	public boolean use_params;

	public BypassStorage(L2PcInstance _player, String _code, boolean _use_params)
	{
		code = _code;
		player = _player;
		use_params = _use_params;
		time = System.currentTimeMillis() + MAX_STORAGE_TIME;
	}
}
