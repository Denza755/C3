package net.sf.l2j.gameserver.model;

import java.util.logging.Logger;

import net.sf.l2j.gameserver.model.L2ItemInstance.ItemLocation;

public final class ClanWarehouse extends Warehouse
{
	private static final Logger _log = Logger.getLogger(PcWarehouse.class.getName());
	
	private L2Clan _clan;
	
	public ClanWarehouse(L2Clan clan)
	{
		_clan = clan;
	}
	
	public int getOwnerId() { return _clan.getClanId(); }
	public ItemLocation getLocationType() { return ItemLocation.CLANWH; }
	public int getLocationId() { return 0; }

}