/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ClientScheduler;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.*;
import net.sf.l2j.gameserver.model.actor.stat.PetStat;
import net.sf.l2j.gameserver.model.actor.status.PetStatus;
import net.sf.l2j.gameserver.packets.serverpackets.*;
import net.sf.l2j.gameserver.taskmanager.DecayTaskManager;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.L2Weapon;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * 
 * This class ...
 * 
 * @version $Revision: 1.15.2.10.2.16 $ $Date: 2005/04/06 16:13:40 $
 */
public final class L2PetInstance extends L2Summon
{
	protected static Logger _log = Logger.getLogger(L2PetInstance.class.getName());
	
	@SuppressWarnings("unused")
    private byte _pvpFlag;
	private int _curFed;
	private PetInventory _inventory;
	private final int _controlItemId;
	private boolean _respawned;
	private Future _feedTask;
    private boolean _mountable;
	
	private L2PetData _Data;
    public final L2PetData getPetData()
    {
        if (_Data == null) _Data = L2PetDataTable.getInstance().getPetData(getTemplate().npcId, getStat().getLevel());
        return _Data;
    }
    public final void setPetData(L2PetData value) { _Data = value; }
	
	class FeedTask implements Runnable
	{
		
		public void run()
		{
			try
			{
		                if (Config.PET_HUNGER)  
		                {  
				if(getCurrentFed() > 0)
					setCurrentFed(getCurrentFed() - 1);
				else
				{
					setCurrentFed(0);
					doDie(null);
				}
		                }
				L2ItemInstance food = null;
				switch (getTemplate().npcId)
				{
					// wolf
					case 12077:
						food = getInventory().findItemByItemId(2515);
						break; 
					//hatchling of wind	
					case 12311:
						food = getInventory().findItemByItemId(4038);
						break;
					// hatchling of star
					case 12312:
						food = getInventory().findItemByItemId(4038);
						break;
					// hatchling of twilight
					case 12313:
						food = getInventory().findItemByItemId(4038);
						break;
					//  wind strider
					case 12526:
						food = getInventory().findItemByItemId(5168);
						break;
					//	Star strider
					case 12527:
						food = getInventory().findItemByItemId(5168);
						break;		
					// Twilight strider
					case 12528:
						food = getInventory().findItemByItemId(5168);
						break;	
					// unknown npc id.. should never happen
					default:
						return; 
				}
				if(food != null && getCurrentFed() < (0.55 * getMaxFed()))
				{
					setCurrentFed(getCurrentFed() + 1);
					removeItemFromInventory(food,1);
				}
				broadcastStatusUpdate();
			}
			catch (Throwable e)
			{
				_log.log(Level.SEVERE, "", e);
			}
		}
	}

    public static L2PetInstance spawnPet(L2NpcTemplate template, L2PcInstance owner, L2ItemInstance control) {
		return restore(control, template, owner);
	}
	
	public L2PetInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, L2ItemInstance control)
	{
		super(objectId, template, owner);
        super.setStat(new PetStat(new L2PetInstance[] {this}));
        super.setStatus(new PetStatus(new L2PetInstance[] {this}));

        // Pet's initial level is supposed to be read from DB
        // Pets start at :
        // Wolf : Level 15
        // Hatcling : Level 35
        // Tested and confirmed on official servers
        getStat().setLevel(template.level);

        _controlItemId = control.getObjectId();
		_inventory = new PetInventory(this);
        
        int npcId = template.npcId;
        _mountable =
        	npcId == 12526 ||       //wind strider
        	npcId == 12527 ||		//star strider
        	npcId == 12528 ||		//twilight strider
        	npcId == 12621;			// wyvern
	}
	
    public PetStat getStat() { return (PetStat)super.getStat(); }
    public PetStatus getStatus() { return (PetStatus)super.getStatus(); }
	
	public boolean isRespawned() { return _respawned; }
	
    public int getSummonType() { return 2; }
	
	public void onAction(L2PcInstance player)
	{
		if (player == getOwner() && player.getTarget() == this)
		{
			player.sendPacket(new PetStatusShow(this));
			player.sendPacket(new ActionFailed());
		}
        else
        {
    		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
    		if (Config.DEBUG) _log.fine("new target selected:"+getObjectId());
    		player.setTarget(this);
    		player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
        }
	}
	
	public int getControlItemId() { return _controlItemId; }
	
	public int getCurrentFed() { return _curFed; }
	
	public void setCurrentFed(int num) { _curFed = num; }
	
	public void setPvpFlag(byte pvpFlag) { _pvpFlag = pvpFlag; }
	
	public void setPkKills(int pkKills) { _pkKills = pkKills; }
		
	/**
	 * Return null.<BR><BR>
	 */
	public L2ItemInstance getActiveWeaponInstance()
	{
		// temporary? unavailable
		return null;
	}
	public L2Weapon getActiveWeaponItem()
	{
		// temporary? unavailable
		return null;
	}
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		// temporary? unavailable
		return null;
	}
	public L2Weapon getSecondaryWeaponItem()
	{
		// temporary? unavailable
		return null;
	}
	
	public PetInventory getInventory() { return _inventory; }

	public void removeItemFromInventory(L2ItemInstance item, int count)
	{
		L2ItemInstance item2 = getInventory().destroyItem(item.getObjectId(), count);
		
		PetInventoryUpdate iu = new PetInventoryUpdate();
		if (item2.getCount() == 0)
			iu.addRemovedItem(item2);
		else
			iu.addModifiedItem(item2);
		
		getOwner().sendPacket(iu);
	}
	
	protected void doPickupItem(L2Object object)
	{
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		if (Config.DEBUG)
			_log.fine("Pet pickup pos: "+ object.getX() + " "+object.getY()+ " "+object.getZ() );
		broadcastPacket(new StopMove(getObjectId(), getX(), getY(), getZ(), getHeading()));
		
		if (!(object instanceof L2ItemInstance))
		{
			// dont try to pickup anything that is not an item :)
			_log.warning("trying to pickup wrong target."+object);
			getOwner().sendPacket(new ActionFailed());
			return;
		}
		
		L2ItemInstance target = (L2ItemInstance) object;
		
		synchronized (target)
		{
			if (!target.isVisible())
			{
				getOwner().sendPacket(new ActionFailed());
				return;
			}
			target.pickupMe(this);
		}
		
		getInventory().addItem(target);
		//FIXME Just send the updates if possible (old way wasn't working though)
		getOwner().sendPacket(new PetItemList(this));
		
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		if (getFollowStatus())
		{
			followOwner();
		}
	}

    public void deleteMe(L2PcInstance owner)
    {
        super.deleteMe(owner);
        destroyControlItem(owner); //this should also delete the pet from the db
    }

    public void displayHitMessage(int damage, boolean crit, boolean miss)
    {
        if (crit)
        {
            getOwner().sendPacket(new SystemMessage(SystemMessage.CRITICAL_HIT_BY_PET));
        }
        
        if (miss)
        {
            getOwner().sendPacket(new SystemMessage(SystemMessage.MISSED_TARGET));
        }
        else
        {
            getOwner().sendPacket(new SystemMessage(SystemMessage.PET_DID_S1_DMG).addNumber(damage));
        }
    }
	
	public synchronized void doDie(L2Character killer) {
		stopFeed();
		DecayTaskManager.getInstance().addDecayTask(this,1200000);
		super.doDie(killer,true);
		deathPenalty();
	}

    public void doRevive()
    {
        _curFed = getMaxFed() / 10;
        super.doRevive();

        // stopDecay
        DecayTaskManager.getInstance().cancelDecayTask(this);
    }
	
	public void giveAllToOwner()
	{
		try
		{
			Inventory petInventory = getInventory();
			L2ItemInstance[] items = petInventory.getItems();
			for (int i = 0; (i < items.length); i++)
			{ 
				L2ItemInstance giveit = items[i];
				if (((giveit.getItem().getWeight() * giveit.getCount())
						+ getOwner().getInventory().getTotalWeight()) 
						< getOwner().getMaxLoad())
				{
					// If the owner can carry it give it to them
					giveItemToOwner(giveit);
				}
				else 
				{
					// If they can't carry it, chuck it on the floor :)
					dropItemHere(giveit);
				}
			}
		}
		catch(Exception e)
		{
			_log.warning("Give all items error " + e);
		}
	}
	
	public void giveItemToOwner(L2ItemInstance item)
	{
		try
		{
			getInventory().dropItem(item, item.getCount());	
			getOwner().getInventory().addItem(item);
			getOwner().sendPacket(new PetInventoryUpdate().addRemovedItem(item));
			getOwner().sendPacket(new ItemList(getOwner(), false));
		}
		catch (Exception e)
        {
            _log.warning("Error while giving item to owner: " + e);
        }
	}
	
	public void broadcastStatusUpdate()
	{
		super.broadcastStatusUpdate();
		if (getOwner() != null)
		{
			getOwner().sendPacket(new PetStatusUpdate(this));
		}
	}
	
	/**
	 * Remove the Pet from DB and its associated item from the player inventory
     * @param owner The owner from whose invenory we should delete the item
	 */
	public void destroyControlItem(L2PcInstance owner)
	{
		// pet control item no longer exists, delete the pet from the db
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
			statement.setInt(1, getControlItemId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("could not delete pet:"+e);
		}
		finally
		{
			try { con.close(); } catch (Exception e) {}
		}
		
		try 
		{
			L2ItemInstance removedItem = owner.getInventory().destroyItem(getControlItemId(), 1);
			
			owner.sendPacket(new InventoryUpdate().addRemovedItem(removedItem));

			owner.sendPacket(new StatusUpdate(owner.getObjectId()).addAttribute(StatusUpdate.CUR_LOAD, owner.getCurrentLoad()));
			
			owner.broadcastUserInfo();
			
			L2World world = L2World.getInstance();
			world.removeObject(removedItem);
		}
		catch (Exception e){
			_log.warning("Error while destroying control item: " + e);
		}
	}
	
	public void dropAllItems()
	{
		try
		{
			L2ItemInstance[] items = getInventory().getItems();
			for (int i = 0; (i < items.length); i++)
			{ 
				dropItemHere(items[i]);
			}
		}
		catch(Exception e)
		{
			_log.warning("Pet Drop Error: " + e);
		}
	}
	
	public void dropItemHere(L2ItemInstance dropit)
	{
		dropit = getInventory().dropItem(dropit.getObjectId(), dropit.getCount());
		
		if (dropit != null)
		{
			_log.finer("Item id to drop: "+dropit.getItemId()+" amount: "+dropit.getCount());
			dropit.dropMe(this, getX(), getY(), getZ()+100);
		}
	}
	
	
    /** @return Returns the mountable. */
    public boolean isMountable() { return _mountable; }

	private static L2PetInstance restore(L2ItemInstance control, L2NpcTemplate template, L2PcInstance owner)
	{
		java.sql.Connection con = null;
		try
		{
			L2PetInstance pet = new L2PetInstance(IdFactory.getInstance().getNextId(), template, owner, control);
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT item_obj_id, objId, name, level, maxHp, curHp, maxMp, curMp, acc, crit, evasion, mAtk, mDef, mSpd, pAtk, pDef, pSpd, str, con, dex, _int, men, wit, exp, sp, karma, pkkills, maxload, fed, max_fed FROM pets WHERE item_obj_id=?");
			statement.setInt(1, control.getObjectId());
			ResultSet rset = statement.executeQuery();
			if (!rset.next()) return pet;

            pet._respawned = true;
			pet.setName(rset.getString("name"));

            pet.getStat().setLevel(rset.getInt("level"));
            pet.getStat().setExp(rset.getInt("exp"));
            pet.getStat().setSp(rset.getInt("sp"));

            pet.getStatus().setCurrentHp(rset.getDouble("curHp"));
            pet.getStatus().setCurrentMp(rset.getDouble("curMp"));
			pet.getStatus().setCurrentCp(pet.getMaxCp());

			pet.setKarma(rset.getInt("karma"));
			pet.setPkKills(rset.getInt("pkkills"));
			pet.setCurrentFed(rset.getInt("fed"));

            rset.close();
			statement.close();
			return pet;
		} catch (Exception e) {
			_log.warning("could not restore pet data: "+ e);
			return null;
		} finally {
			try { con.close(); } catch (Exception e) {}
		}
	}
   
	public void store()
	{
		if (getControlItemId() == 0)
		{
			// this is a summon, not a pet, don't store anything
			return;
		}
		
		String req;
		if (!isRespawned())
			req = "INSERT INTO pets (name,level,curHp,curMp,exp,sp,karma,pkkills,fed,item_obj_id) "+
				"VALUES (?,?,?,?,?,?,?,?,?,?)";
		else
			req = "UPDATE pets SET name=?,level=?,curHp=?,curMp=?,exp=?,sp=?,karma=?,pkkills=?,fed=? "+
				"WHERE item_obj_id = ?";
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(req);
			statement.setString(1, getName());
			statement.setInt(2, getStat().getLevel());
			statement.setDouble(3, getStatus().getCurrentHp());
			statement.setDouble(4, getStatus().getCurrentMp());
			statement.setInt(5, getStat().getExp());
			statement.setInt(6, getStat().getSp());
			statement.setInt(7, getKarma());
			statement.setInt(8, getPkKills());
			statement.setInt(9, getCurrentFed());
			statement.setInt(10, getControlItemId());
			statement.executeUpdate();
			statement.close();
			_respawned = true;
		} catch (Exception e) {
			_log.warning("could not store pet data: "+e);
		} finally {
			try { con.close(); } catch (Exception e) {}
		}
	}
	
	public synchronized void stopFeed()
	{
		if (_feedTask != null)
		{
			_feedTask.cancel(false);
			_feedTask = null;
			if (Config.DEBUG) _log.fine("Feed task stop");
		}
	}
	
	public synchronized void startFeed()
	{
	    if (_feedTask == null && !isDead())
        {
	        if (Config.DEBUG) _log.fine("Feed task started");
	        int time;
	        time = getStat().getFeedNormal();
	        //time = getStat().getFeedBattle();
	        if (time == 0)
	        {
	            _log.warning("Pet Feed Normal Time is 0. Changing time to 1 to bypass divide by 0");
	            time = 1;
	        }
	        _feedTask = ClientScheduler.getInstance().scheduleMedAtFixedRate(new FeedTask(), 60000/time, 60000/time);
	    }
	}
	
	public synchronized void unSummon (L2PcInstance owner)
    {
		stopFeed();
		super.unSummon(owner);
    }

    private void deathPenalty()
    {
        // TODO Need Correct Penalty

        int lvl = getStat().getLevel();
        double percentLost = -0.07 * lvl + 6.5;
        
        // Calculate the Experience loss
        int lostExp = (int) Math.round((getStat().getExpForLevel(lvl+1) - getStat().getExpForLevel(lvl)) * percentLost /100);

        // Set the new Experience value of the L2PcInstance
        getStat().addExp(-lostExp);
     }


    
    public void addExpAndSp(int addToExp, int addToSp) { getStat().addExpAndSp(addToExp, addToSp); }
    public int getExpForThisLevel() { return getStat().getExpForLevel(getLevel()); }
    public int getExpForNextLevel() { return getStat().getExpForLevel(getLevel() + 1); }
    public void reduceCurrentHp(double damage, L2Character attacker) { getStatus().reduceHp(damage, attacker); }
    public void reduceCurrentHp(double value, L2Character attacker, boolean awake) { getStatus().reduceHp(value, attacker, awake); }
    public final int getLevel() { return getStat().getLevel(); }
    public int getMaxFed() { return getStat().getMaxFeed(); }
    public int getMAtk(L2Character target, L2Skill skill) { return getStat().getMAtk(target, skill); }
    public int getMDef(L2Character target, L2Skill skill) { return getStat().getMDef(target, skill); }
    public int getPAtk(L2Character target) { return getStat().getPAtk(target); }
    public int getPDef(L2Character target) { return getStat().getPDef(target); }
}

