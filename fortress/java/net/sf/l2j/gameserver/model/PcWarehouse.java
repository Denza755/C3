package net.sf.l2j.gameserver.model;

import net.sf.l2j.gameserver.model.L2ItemInstance.ItemLocation;
import net.sf.l2j.gameserver.model.L2ItemInstance.ItemClass;

public class PcWarehouse extends Warehouse
{
	//private static final Logger _log = Logger.getLogger(PcWarehouse.class.getName());

	private L2PcInstance _owner;


	public PcWarehouse(L2PcInstance owner, int selecttype)
	{
		_owner = owner;
		_selecttype = selecttype;
	}

	public int getOwnerId() { return _owner.getObjectId(); }
	public ItemLocation getLocationType() { return ItemLocation.WAREHOUSE; }
	public String getLocationId() { return "0"; }
    public int getLocationId(@SuppressWarnings("unused") boolean dummy) { return 0; }
    public void setLocationId(@SuppressWarnings("unused") L2PcInstance dummy) { return; }
    public ItemClass getItemClass()
    	{
    	switch (_selecttype){
		case 0:
			return ItemClass.ALL;
		case 1:
			return ItemClass.EQUIPMENT;
		case 2:
			return ItemClass.CONSUMABLE;
		case 3:
			return ItemClass.MATHERIALS;
		case 4:
			return ItemClass.PIECES;
		case 5:
			return ItemClass.RECIPIES;
		case 6:
			return ItemClass.SPELLBOOKS;
		case 7:
			return ItemClass.DYES;
		case 8:
			return ItemClass.OTHER;
		default:
			return ItemClass.ALL;
    		}
    	}
}