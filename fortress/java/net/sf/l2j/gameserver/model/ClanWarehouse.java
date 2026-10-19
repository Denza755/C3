package net.sf.l2j.gameserver.model;

import net.sf.l2j.gameserver.model.L2ItemInstance.ItemLocation;
import net.sf.l2j.gameserver.model.L2ItemInstance.ItemClass;

public final class ClanWarehouse extends Warehouse
{
    private L2Clan _clan;

    public ClanWarehouse(L2Clan clan, int selecttype)
    {
        _clan = clan;
        _selecttype = selecttype;
    }

    public int getOwnerId()
    {
        return _clan.getClanId();
    }

    public ItemLocation getLocationType()
    {
        return ItemLocation.CLANWH;
    }

    public String getLocationId()
    {
        return "0";
    }

    public int getLocationId(@SuppressWarnings("unused")boolean dummy)
    {
        return 0;
    }

    public void setLocationId(@SuppressWarnings("unused")L2PcInstance dummy)
    {}

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