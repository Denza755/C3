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
package net.sf.l2j.gameserver.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ClientScheduler;
import net.sf.l2j.gameserver.GameServer;
import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.PetInfo;
import net.sf.l2j.gameserver.serverpackets.PetInventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.PetItemList;
import net.sf.l2j.gameserver.serverpackets.PetStatusShow;
import net.sf.l2j.gameserver.serverpackets.PetStatusUpdate;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.StopMove;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.L2Weapon;



/**
 * 
 * This class ...
 * 
 * @version $Revision: 1.15.2.10.2.16 $ $Date: 2005/04/06 16:13:40 $
 */
public final class L2PetInstance extends L2Summon
{
	protected static Logger _log = Logger.getLogger(L2PetInstance.class.getName());
	
	private int _level;
	@SuppressWarnings("unused")
	private byte _pvpFlag;
	private int _curFed;
	private PetInventory _inventory;
	private final int _controlItemId;
	private boolean _respawned;
	private Future _feedTask;
    private boolean _mountable;
	
	private L2PetData _data;
	
	class FeedTask implements Runnable
	{
		
		public void run()
		{
			try
			{
				if(getCurrentFed() > 0)
					setCurrentFed(getCurrentFed() - 1);
				else
				{
					setCurrentFed(0);
					doDie(null);
				}
				L2ItemInstance food = null;
				switch (getTemplate().npcId)
				{
					// wolf
					case 12077:
						food = getInventory().findItemByItemId(2515);
						break; 
					//hatchling of wind, star, twilight	
					case 12311:
					case 12312:
					case 12313:
						food = getInventory().findItemByItemId(4038);
						break;
					// strider: wind, star, twilight
					case 12526:
					case 12527:
					case 12528:
						food = getInventory().findItemByItemId(5168);
						break;
					// wyvern
					case 12621:
						food = getInventory().findItemByItemId(6316);
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
		_controlItemId = control.getObjectId();
		_inventory = new PetInventory(this);
		_data = L2PetDataTable.getInstance().getPetData(template.npcId, template.level);
		_template = template;
		_level = template.level;
		
        int npcId = template.npcId;
        _mountable =
        	npcId == 12526 ||       //wind strider
        	npcId == 12527 ||    //star strider
        	npcId == 12528 ||    //twilight strider
        	npcId == 12621;      // wyvern
	}
	
	
	public L2NpcTemplate getTemplate()
	{
		return (L2NpcTemplate)_template;
	}
	
	public final int getLevel() { return _level; }
	
	public boolean isRespawned() {
		return _respawned;		
	}
	
    public int getSummonType()
	{
		return 2;
	}
	
	@SuppressWarnings("static-access")
	public void onAction(L2PcInstance player)
	{
		//if(_owner.getInventory().findItemByItemId(_controlItemId) == null)
		//{
		//	unSummon(_owner);
		//	return;
		//}

		if(GameServer.events.onAction(player, this))
			return;

		if (player == _owner && player.getTarget() == this)
		{
			player.sendPacket(new PetStatusShow(this));
			player.sendPacket(new ActionFailed());
		}
        else
        {
    		setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
    		if (Config.DEBUG) _log.fine("new target selected:"+getObjectId());
    		player.setTarget(this);
    		MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
    		player.sendPacket(my);
        }
	}
	
	public int getControlItemId()
	{
		return _controlItemId;
	}

	public int getMaxHp()
	{
        return (int)calcStat(Stats.MAX_HP, _data.getPetHP(), null, null);
	}
	
	public int getMaxMp()
	{
        return (int)calcStat(Stats.MAX_MP, _data.getPetMP(), null, null);
	}
	
	public int getMaxFed()
	{
		return _data.getPetMaxFeed();
	}
	
	public int getMAtk(L2Character target, L2Skill skill)
	{
		double attack = _data.getPetMAtk();
		Stats stat = skill == null? null : skill.getStat();
		if (stat != null)
		{
			switch (stat)
			{
			case AGGRESSION: attack += _template.baseAggression; break;
			case BLEED:      attack += _template.baseBleed;      break;
			case POISON:     attack += _template.basePoison;     break;
			case STUN:       attack += _template.baseStun;       break;
			case ROOT:       attack += _template.baseRoot;       break;
			case MOVEMENT:   attack += _template.baseMovement;   break;
			case CONFUSION:  attack += _template.baseConfusion;  break;
			case SLEEP:      attack += _template.baseSleep;      break;
			case FIRE:       attack += _template.baseFire;       break;
			case WIND:       attack += _template.baseWind;       break;
			case WATER:      attack += _template.baseWater;      break;
			case EARTH:      attack += _template.baseEarth;      break;
			}
		}
		if (skill != null)
			attack += skill.getPower();
		return (int)calcStat(Stats.MAGIC_ATTACK, attack, target, skill);
	}
	
	public int getMDef(L2Character target, L2Skill skill)
	{
		double defence = _data.getPetMDef();
		Stats stat = skill == null? null : skill.getStat();
		if (stat != null)
		{
			switch (stat)
			{
			case AGGRESSION: defence += _template.baseAggressionRes; break;
			case BLEED:      defence += _template.baseBleedRes;      break;
			case POISON:     defence += _template.basePoisonRes;     break;
			case STUN:       defence += _template.baseStunRes;       break;
			case ROOT:       defence += _template.baseRootRes;       break;
			case MOVEMENT:   defence += _template.baseMovementRes;   break;
			case CONFUSION:  defence += _template.baseConfusionRes;  break;
			case SLEEP:      defence += _template.baseSleepRes;      break;
			case FIRE:       defence += _template.baseFireRes;       break;
			case WIND:       defence += _template.baseWindRes;       break;
			case WATER:      defence += _template.baseWaterRes;      break;
			case EARTH:      defence += _template.baseEarthRes;      break;
			}
		}
		return (int)calcStat(Stats.MAGIC_DEFENCE, defence, target, skill);
	}
	
	public int getPAtk(L2Character target)
	{
		return (int)calcStat(Stats.POWER_ATTACK, _data.getPetPAtk(), target, null);
	}
	
	public int getPDef(L2Character target)
	{
		return (int)calcStat(Stats.POWER_DEFENCE, _data.getPetPDef(), target, null);
	}
	
	public int getCurrentFed()
	{
		return _curFed;
	}
	
	public void setCurrentFed(int num)
	{
		_curFed = num;
	}
	
	public void setPvpFlag(byte pvpFlag)
	{
		_pvpFlag = pvpFlag;
	}
	
	public void setPkKills(int pkKills)
	{
		_pkKills = pkKills;
	}
	
	public void setSp(int sp)
	{
		_sp = sp;
	}
	
	public void addExpAndSp(int addToExp, int addToSp)
	{
		if (Config.DEBUG) _log.fine("adding " + addToExp + " exp and " +addToSp+" sp to " + getName());
		_exp += addToExp;
		_sp += addToSp;
		
		_owner.sendPacket(new PetInfo(this));
		_owner.sendPacket(new SystemMessage(SystemMessage.THE_PET_ACQUIRED_EXPERIENCE_POINTS_OF_S1).addNumber(addToExp));
		
		if (_exp >= getExpForNextLevel())
		{
			/* seems like this is not working for pets
			SocialAction sa = new SocialAction(getObjectId(), 15);
			broadcastPacket(sa);*/
			
			_owner.sendMessage("Your pet has increased it's level."); //TODO need the pet level message
		}
		
		while (_exp >= getExpForNextLevel())
		{
			increaseLevel();
		}
	}
	
	public int getExpForThisLevel()
    {
		L2PetData data = L2PetDataTable.getInstance().getPetData(this.getNpcId(),this.getLevel());
		return data.getPetExp();
    }
	
	public int getExpForNextLevel()
    {
		L2PetData data = L2PetDataTable.getInstance().getPetData(this.getNpcId(),this.getLevel()+1);
		return data.getPetExp();
    }
	
	public void increaseLevel()
	{
		_data = L2PetDataTable.getInstance().getPetData(getTemplate().npcId, getLevel()+1);
		stopFeed();
		if (Config.DEBUG) _log.finest("Increasing level of " + getName());
		_level++;
		PetStatusUpdate ps = new PetStatusUpdate(this);
		_owner.sendPacket(ps);
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.LEVEL, getLevel());
        su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
        su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
		broadcastPacket(su);
        setCurrentHpMp(getMaxHp(),getMaxMp());
        startFeed();
	}
		
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
	
	public PetInventory getInventory()
	{
		return _inventory;
	}

	public void removeItemFromInventory(L2ItemInstance item, int count)
	{
		L2ItemInstance item2 = getInventory().destroyItem(item.getObjectId(), count);
		
		PetInventoryUpdate iu = new PetInventoryUpdate();
		if (item2.getCount() == 0)
			iu.addRemovedItem(item2);
		else
			iu.addModifiedItem(item2);
		
		_owner.sendPacket(iu);
	}
	
	protected void doPickupItem(L2Object object)
	{
		setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);

		if (Config.DEBUG)
			_log.fine("Pet pickup pos: "+ object.getX() + " "+object.getY()+ " "+object.getZ() );

		broadcastPacket(new StopMove(getObjectId(), getX(), getY(), getZ(), getHeading()));
		
		if (!(object instanceof L2ItemInstance))
		{
			// dont try to pickup anything that is not an item :)
			_log.warning("trying to pickup wrong target."+object);
			_owner.sendPacket(new ActionFailed());
			return;
		}
		
		L2ItemInstance target = (L2ItemInstance) object;
		
		synchronized (target)
		{
			if (!target.isVisible())
			{
				_owner.sendPacket(new ActionFailed());
				return;
			}
			target.pickupMe(this);
		}
		
		getInventory().addItem(target);
		//FIXME Just send the updates if possible (old way wasn't working though)
		_owner.sendPacket(new PetItemList(this));
		
		setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
		if (getFollowStatus())
			followOwner();
	}

    public void deleteMe(L2PcInstance owner)
    {
        super.deleteMe(owner);
        destroyControlItem(owner); //this should also delete the pet from the db
    }
	public void reduceCurrentHp(double damage, L2Character attacker)
	{
		super.reduceCurrentHp(damage, attacker);
		// this is usually only called in combat
		
		
		if (!isDead() && attacker != null)
		{
            SystemMessage sm = new SystemMessage(SystemMessage.THE_PET_RECEIVED_DAMAGE_OF_S2_CAUSED_BY_S1);
            if (attacker instanceof L2NpcInstance)
            {
                sm.addNpcName( ((L2NpcInstance)attacker).getTemplate().npcId);
            }
            else
            {
                sm.addString(attacker.getName());
            }
            sm.addNumber((int)damage);
          
			_owner.sendPacket(sm);
    		notifyEvent(CtrlEvent.EVT_ATTACKED, attacker, null);
		}
	}
    
    protected void displayHitMessage(int damage, boolean crit, boolean miss)
    {
        if (crit)
        {
            _owner.sendPacket(new SystemMessage(SystemMessage.PETS_CRITICAL_HIT));
        }
        
        if (miss)
        {
            _owner.sendPacket(new SystemMessage(SystemMessage.MISSED_TARGET));
        }
        else
        {
            _owner.sendPacket(new SystemMessage(SystemMessage.THE_PET_GAVE_DAMAGE_OF_S1).addNumber(damage));
        }
    }

	public synchronized void doDie(L2Character killer) {
		stopFeed();
		if (_decayTask == null) {
			_decayTask = ClientScheduler.getInstance().scheduleLow(
					new DecayTask(), 180000);
		}
		super.doDie(killer);
	}

	public void stopDecay()
    {
        if (_decayTask != null) { _decayTask.cancel(true); }
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
						+ _owner.getInventory().getTotalWeight()) 
						< _owner.getMaxLoad())
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
			_owner.getInventory().addItem(item);
			PetInventoryUpdate petiu = new PetInventoryUpdate();
			ItemList PlayerUI = new ItemList(_owner, false);
			petiu.addRemovedItem(item);
			_owner.sendPacket(petiu);
			_owner.sendPacket(PlayerUI);	
		}
		catch (Exception e)
        {
            _log.warning("Error while giving item to owner: " + e);
        }
	}
	
	public void broadcastStatusUpdate()
	{
		super.broadcastStatusUpdate();
		if (_owner != null)
		{
			_owner.sendPacket(new PetStatusUpdate(this));
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
			
			InventoryUpdate iu = new InventoryUpdate();
			iu.addRemovedItem(removedItem);
			
			owner.sendPacket(iu);
			
			StatusUpdate su = new StatusUpdate(owner.getObjectId());
			su.addAttribute(StatusUpdate.CUR_LOAD, owner.getCurrentLoad());
			owner.sendPacket(su);
			
			owner.broadcastUserInfo();
			
			L2World.removeObject(removedItem);
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
	
    /**
     * @return Returns the mountable.
     */
    public boolean isMountable()
    {
        return _mountable;
    }

	private static L2PetInstance restore(L2ItemInstance control, L2NpcTemplate template, L2PcInstance owner)
	{
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT item_obj_id, objId, name, level, maxHp, curHp, maxMp, curMp, acc, crit, evasion, mAtk, mDef, mSpd, pAtk, pDef, pSpd, str, con, dex, _int, men, wit, exp, sp, karma, pkkills, maxload, fed, max_fed FROM pets WHERE item_obj_id=?");
			statement.setInt(1, control.getObjectId());
			ResultSet rset = statement.executeQuery();
			if (!rset.next())
			{
			  L2PetInstance pet = new L2PetInstance(IdFactory.getInstance().getNextId(), template, owner, control);
			  return pet;
			}
			L2PetInstance pet = new L2PetInstance(rset.getInt("objId"), template, owner, control);
			pet._respawned = true;
			pet.setName(rset.getString("name"));
			pet._level = rset.getInt("level");
			pet.setCurrentHpMp(rset.getDouble("curHp"), rset.getInt("curMp"));
			pet.setCurrentCp(pet.getMaxCp());
			pet.setExp(rset.getInt("exp"));
			pet.setSp(rset.getInt("sp"));
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
			req = "INSERT INTO pets (name,level,curHp,curMp,exp,sp,karma,pkkills,fed,objId,item_obj_id) "+
				"VALUES (?,?,?,?,?,?,?,?,?,?,?)";
		else
			req = "UPDATE pets SET name=?,level=?,curHp=?,curMp=?,exp=?,sp=?,karma=?,pkkills=?,fed=?,objId=? "+
				"WHERE item_obj_id = ?";
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(req);
			statement.setString(1, getName());
			statement.setInt(2, getLevel());
			statement.setDouble(3, getCurrentHp());
			statement.setDouble(4, getCurrentMp());
			statement.setInt(5, getExp());
			statement.setInt(6, getSp());
			statement.setInt(7, getKarma());
			statement.setInt(8, getPkKills());
			statement.setInt(9, getCurrentFed());
			statement.setInt(10, getObjectId());
			statement.setInt(11, getControlItemId());
			statement.executeUpdate();
			statement.close();
			_respawned = true;
		} catch (Exception e) {
			_log.warning("could not store pet data: "+e);
		} finally {
			try { con.close(); } catch (Exception e) {}
		}
	}
	
	protected synchronized void stopFeed()
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
	        time = _data.getPetFeedNormal();
	        //time = _data.getPetFeedBattle();
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
    
}

