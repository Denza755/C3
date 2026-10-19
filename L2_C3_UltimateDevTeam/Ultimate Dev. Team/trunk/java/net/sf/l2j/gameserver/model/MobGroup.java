package net.sf.l2j.gameserver.model;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2ControllableMobAI;
import net.sf.l2j.gameserver.model.actor.instance.L2ControllableMobInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.tables.SpawnTable;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

import java.util.Random;
import java.util.Vector;

/**
 * @author littlecrow
 *
 */
public class MobGroup {
	private L2NpcTemplate _template;
	private int _mobCount;
	private Vector<L2ControllableMobInstance> _mobs;

	private static final int RANDOM_RANGE=300;
	public MobGroup(L2NpcTemplate template, int mobCount) {
		_template = template;
		_mobCount=mobCount;
	}
	public L2NpcTemplate get_template() {
		return _template;
	}

	public void set_template(L2NpcTemplate _template) {
		this._template = _template;
	}
	
	public void spawnIt(int x, int y, int z ) {
		if (_mobs!=null && _mobs.size()>0)	// can't spawn mob if already done
			return;
		_mobs = new Vector<L2ControllableMobInstance>();
		try {
			for (int i=0;i<_mobCount;i++) {
				L2GroupSpawn spawn = new L2GroupSpawn(_template);
				Random rnd = new Random();
				int signX = (rnd.nextInt(2)==0?-1:1);
				int signY = (rnd.nextInt(2)==0?-1:1);
				int randX = rnd.nextInt(RANDOM_RANGE);
				int randY = rnd.nextInt(RANDOM_RANGE);
				spawn.setLocx(x + signX*randX);
				spawn.setLocy(y + signY*randY);
				spawn.setLocz(z);
				spawn.stopRespawn();
				SpawnTable.getInstance().addNewSpawn(spawn, false);
				_mobs.add((L2ControllableMobInstance)spawn.doGroupSpawn());
				
				
			}
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void spawnIt(L2PcInstance activeChar) {
		spawnIt(activeChar.getX(),activeChar.getY(),activeChar.getZ());
	}
	
	public L2ControllableMobInstance getAMob() {
		removeDead();
		Random rnd = new Random();
		System.out.println(_mobs==null);
		if (_mobs==null)
			return null;
		int choice = rnd.nextInt(_mobs.size());
		System.out.println(choice+":"+_mobs.get(choice));
		return _mobs.get(choice);
		
		
	}
	public void unspawn() {
		if (_mobs==null)
			return;
		removeDead();
		for (int i=0;i<_mobs.size();i++) {
			if (!_mobs.get(i).isDead())
				_mobs.get(i).deleteMe();
			SpawnTable.getInstance().deleteSpawn(_mobs.get(i).getSpawn(),false);
		}
		_mobs.clear();
	}
	
	public void kill(L2PcInstance activeChar) {
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
	
	public void randomAttack() {
		if (_mobs==null)
			return;
		removeDead();
		for (int i=0;i<_mobs.size();i++) {
			L2ControllableMobAI ai = (L2ControllableMobAI) _mobs.get(i).getAI();
			ai.setAlternateAI(L2ControllableMobAI.AI_NORMAL);
			ai.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
		
	}
	
	public void attackTarget(L2Character target) {
		if (_mobs==null)
			return;
		removeDead();
		for (int i=0;i<_mobs.size();i++) {
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
	
	public void returnToCharacter(L2Character activeChar) {
		if (_mobs==null)
			return;
		
		idle();

		Random rnd = new Random();

		for (int i=0;i<_mobs.size();i++) {
			int signX = (rnd.nextInt(2)==0?-1:1);
			int signY = (rnd.nextInt(2)==0?-1:1);
			int randX = rnd.nextInt(RANDOM_RANGE);
			int randY = rnd.nextInt(RANDOM_RANGE);

			L2ControllableMobAI ai = (L2ControllableMobAI) _mobs.get(i).getAI();
			ai.move(activeChar.getX()+signX*randX,activeChar.getY()+signY*randY,activeChar.getZ());
		}
	}
	
	public void follow(L2Character character) {
		if (_mobs==null)
			return;
		removeDead();
		for (int i=0;i<_mobs.size();i++) {
			L2ControllableMobAI ai = (L2ControllableMobAI) _mobs.get(i).getAI();
			ai.follow(character);
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
			if (mob != null && mob.isDead())
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
			ai.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
	}

}

