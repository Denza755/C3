package net.sf.l2j.gameserver.model;

import net.sf.l2j.gameserver.model.L2ItemInstance.ItemLocation;

public class PetInventory extends Inventory {

	private final L2PetInstance _owner;

	public PetInventory(L2PetInstance owner) {
		this._owner = owner;
	}
	protected L2PetInstance getOwner() { return _owner; }
	protected ItemLocation getBaseLocation() { return ItemLocation.PET; } 
	protected ItemLocation getEquipLocation() { return ItemLocation.PET_EQUIP; }
}
