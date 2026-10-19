package net.sf.l2j.gameserver.model;

import java.util.logging.Logger;

import net.sf.l2j.gameserver.model.L2ItemInstance.ItemLocation;

public class PcWarehouse extends Warehouse
{
	private static final Logger _log = Logger.getLogger(PcWarehouse.class.getName());
	
	private L2PcInstance _owner;
	
	public PcWarehouse(L2PcInstance owner)
	{
		_owner = owner;
	}
	
	public int getOwnerId() { return _owner.getObjectId(); }
	public ItemLocation getLocationType() { return ItemLocation.WAREHOUSE; }
	public int getLocationId() { return 0; }

}