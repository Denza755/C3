package net.sf.l2j.gameserver.model;

import javolution.util.FastMap;

import java.util.Map;

/**
 * @author littlecrow
 *
 */
public class MobGroupTable {

	private static final MobGroupTable _instance = new MobGroupTable();
	private Map<Integer,MobGroup> _groupMap;
	
	
	public MobGroupTable() {
		_groupMap = new FastMap<Integer,MobGroup>();
	}
	
	public static MobGroupTable getInstance() {
		return _instance;
	}
	
	public void addGroup(int key,MobGroup group) {
		_groupMap.put(new Integer(key),group);
	}
	
	public MobGroup getGroup(int key) {
		return _groupMap.get(new Integer(key));
	}

}