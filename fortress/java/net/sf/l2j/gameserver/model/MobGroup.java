package net.sf.l2j.gameserver.model;

import java.util.Vector;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.SpawnTable;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2ControllableMobAI;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * @author littlecrow
 *
 */
public class MobGroup 
{
    private static Logger _log = Logger.getLogger(MobGroup.class.getName());
	private L2NpcTemplate _template;
	private int _mobCount;
	private Vector<L2ControllableMobInstance> _mobs;

	public int RANDOM_RANGE;
	public MobGroup(L2NpcTemplate template, int mobCount) 
	{
		_template = template;
		_mobCount = mobCount;
		RANDOM_RANGE = Math.max(Math.round((float)Math.sqrt(mobCount) * 200), 300);
	}
	
	public L2NpcTemplate get_template() 
	{
		return _template;
	}

	public void set_template(L2NpcTemplate _template) 
	{
		this._template = _template;
	}
	
	public void spawnIt(int x, int y, int z ) 
	{
		if (_mobs!=null && _mobs.size()>0)	// can't spawn mob if already done
			return;

		_mobs = new Vector<L2ControllableMobInstance>();
		try 
		{
			for (int i=0;i<_mobCount;i++) 
			{
				L2GroupSpawn spawn = new L2GroupSpawn(_template);
				L2CharPosition pos = Rnd.coordsRandomize(x, y, z, Rnd.get(65536), 50, RANDOM_RANGE);
				spawn.setLocx(pos.x);
				spawn.setLocy(pos.y);
				spawn.setLocz(z);
				spawn.stopRespawn();
				SpawnTable.getInstance().addNewSpawn(spawn, false);
				_mobs.add((L2ControllableMobInstance)spawn.doGroupSpawn());
			}
		}
		catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
		}
	}
	
	public void spawnIt(L2PcInstance activeChar) 
	{
		spawnIt(activeChar.getX(),activeChar.getY(),activeChar.getZ());
	}
	
	public L2ControllableMobInstance getAMob() 
	{
		removeDead();
		
		if (_mobs==null)
			return null;

		return _mobs.get(Rnd.get(_mobs.size()));
	}
	
	public void unspawn() 
	{
		if (_mobs==null)
			return;
		removeDead();
		for (int i=0;i<_mobs.size();i++) 
		{
			if (!_mobs.get(i).isDead())
				_mobs.get(i).deleteMe();
			SpawnTable.getInstance().deleteSpawn(_mobs.get(i).getSpawn(),false);
		}
		_mobs.clear();
	}
	
	public void kill(L2PcInstance activeChar) 
	{
		if (_mobs==null)
			return;
		removeDead();
		for (int i=0;i<_mobs.size();i++) {
			if (!_mobs.get(i).isDead())
				_mobs.get(i).reduceCurrentHp(_mobs.get(i).getMaxHp()+1,activeChar);
			SpawnTable.getInstance().deleteSpawn(_mobs.get(i).getSpawn(),false);
		}
		_mobs.clear();
		
	}
	
	public void randomAttack() 
	{
		if (_mobs==null)
			return;

		removeDead();
		
		for (int i=0;i<_mobs.size();i++) 
		{
			L2ControllableMobAI ai = (L2ControllableMobAI) _mobs.get(i).getAI();
			ai.setAlternateAI(L2ControllableMobAI.AI_NORMAL);
			ai.setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
		}
	}
	
	public void attackTarget(L2Character target) 
	{
		if (_mobs==null)
			return;

		removeDead();
		
		for (int i=0;i<_mobs.size();i++) 
		{
			L2ControllableMobAI ai = (L2ControllableMobAI) _mobs.get(i).getAI();
			ai.forceAttack(target);
		}
	}
	
	public void idle() {
		if (_mobs==null)
			return;
		removeDead();
		for (int i=0;i<_mobs.size();i++) {
			L2ControllableMobAI ai = (L2ControllableMobAI) _mobs.get(i).getAI();
			ai.stop();
		}
	}
	
	public void returnToCharacter(L2Character activeChar) 
	{
		if (_mobs==null)
			return;
		
		idle();

		for (int i=0;i<_mobs.size();i++) 
		{
			L2CharPosition pos = Rnd.coordsRandomize(activeChar, RANDOM_RANGE);

			L2ControllableMobAI ai = (L2ControllableMobAI) _mobs.get(i).getAI();

			ai.move(pos.x, pos.y, activeChar.getZ());
		}
	}
	
	public void follow(L2Character character) 
	{
		if(Config.DEBUG)
			_log.info("Follow group "+_mobs+" to "+character);

		if (_mobs==null)
			return;

		removeDead();

		for (int i=0;i<_mobs.size();i++) 
		{
			L2ControllableMobAI ai = (L2ControllableMobAI) _mobs.get(i).getAI();
			ai.follow(character, RANDOM_RANGE);
		}
		
	}
	
	public void setCastMode() {
		if (_mobs==null)
			return;
		removeDead();
		for (int i=0;i<_mobs.size();i++) {
			L2ControllableMobAI ai = (L2ControllableMobAI) _mobs.get(i).getAI();
			ai.setAlternateAI(L2ControllableMobAI.AI_CAST);
		}
		
	}
	
	public void setNoMoveMode(boolean enabled) {
		if (_mobs==null)
			return;
		removeDead();
		for (int i=0;i<_mobs.size();i++) {
			L2ControllableMobAI ai = (L2ControllableMobAI) _mobs.get(i).getAI();
			ai.setNoMove(enabled);
		}
	}
	protected void removeDead() {
		if (_mobs==null)
			return;
		
		Vector<L2ControllableMobInstance> deadMobs = new Vector<L2ControllableMobInstance>();
		
		for (int i=0;i<_mobs.size();i++) {
			L2ControllableMobInstance mob = _mobs.get(i);
			if (mob.isDead())
				deadMobs.add(mob);
				
		}
		
		for (int i=0;i<deadMobs.size();i++) 
			_mobs.remove(deadMobs.get(i));
		
	}
	
	public void setInvul(boolean state) {
		removeDead();
		for (int i=0;i<_mobs.size();i++) {
			_mobs.get(i).setInvul(state);
		}
	}
	
	public void setAttackGroup(MobGroup otherGrp) {
		if (_mobs==null)
			return;
		removeDead();
		for (int i=0;i<_mobs.size();i++) {
			L2ControllableMobAI ai = (L2ControllableMobAI) _mobs.get(i).getAI();
			ai.forceAttackGroup(otherGrp);
			ai.setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
		}
	}

}

