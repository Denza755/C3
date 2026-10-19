package net.sf.l2j.gameserver.model;

import net.sf.l2j.gameserver.model.L2ItemInstance.ItemLocation;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.templates.L2Item;

public class PetInventory extends Inventory 
{
	private final L2PetInstance _owner;

	public PetInventory(L2PetInstance owner)
    {
		_owner = owner;
	}

	@Override
	public L2PetInstance getOwner()
    {
        return _owner;
    }

	@Override
	public int getOwnerId()
	{
		// gets the L2PcInstance-owner's ID
		int id;
		try
		{
			id = _owner.getOwner().getObjectId();
		}
		catch (NullPointerException e)
		{
			return 0;
		}
		return id;
	}

	/**
	 * Refresh the weight of equipment loaded
	 */
	@Override
	public void refreshWeight()
	{
		super.refreshWeight();
		getOwner().updateAndBroadcastStatus(1);
	}

    public boolean validateCapacity(L2ItemInstance item)
    {
        int slots = 0;

        if (!(item.isStackable() && getItemByItemId(item.getItemId()) != null))
        	slots++;

        return validateCapacity(slots);
    }

	@Override
	public boolean validateCapacity(int slots)
	{
		return (_items.size() + slots <= _owner.getInventoryLimit());
	}

	public boolean validateWeight(L2ItemInstance item, int count)
	{
		int weight = 0;
		L2Item template = ItemTable.getInstance().getTemplate(item.getItemId());
        if (template == null) return false;
        weight += count * template.getWeight();
		return validateWeight(weight);
	}

	@Override
	public boolean validateWeight(int weight)
	{
		return (_totalWeight + weight <= _owner.getMaxLoad());
	}

	@Override
	protected ItemLocation getBaseLocation()
    {
        return ItemLocation.PET;
    }

	@Override
	protected ItemLocation getEquipLocation()
    {
        return ItemLocation.PET_EQUIP;
    }
}
