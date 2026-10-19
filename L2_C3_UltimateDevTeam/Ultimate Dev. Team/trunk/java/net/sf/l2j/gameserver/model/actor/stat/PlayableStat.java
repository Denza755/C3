package net.sf.l2j.gameserver.model.actor.stat;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;

public class PlayableStat extends CharStat
{
    // =========================================================
    // Data Field
    
    // =========================================================
    // Constructor
    public PlayableStat(L2PlayableInstance[] activeChar)
    {
        super(activeChar);
    }

    // =========================================================
    // Method - Public
    public boolean addExp(int value)
    {
        if ((getExp() + value) < 0 || getExp() == (getExpForLevel(Config.MAX_LEVEL + 1) - 1)) value = 0;
        if (getExp() + value >= getExpForLevel(Config.MAX_LEVEL + 1)) value = getExpForLevel(Config.MAX_LEVEL + 1) - 1 - getExp();

        setExp(getExp() + value);

        int level;
        for (level = 1; level <= Config.MAX_LEVEL + 1 ; level++)
        {
        	if (getExp() >= getExpForLevel(level)) continue;
        	level--;
        	break;
        }
        if (level != getLevel()) addLevel(level - getLevel());

        return true;
    }

    public boolean addExpAndSp(int addToExp, int addToSp)
    {
    	boolean expAdded = false;
    	boolean spAdded = false;
    	if (addToExp != 0) expAdded = addExp(addToExp);
    	if (addToSp != 0) spAdded = addSp(addToSp);

    	return expAdded || spAdded;
    }
    
    public boolean addLevel(int value)
    {
        if (getLevel() + value > Config.MAX_LEVEL)
        {
        	if (getLevel() < Config.MAX_LEVEL)
        		value = Config.MAX_LEVEL - getLevel();
        	else
        		return false;
        }

        boolean levelIncreased = (getLevel() + value > getLevel());
        value += getLevel();
        setLevel(value);

        // Sync up exp with current level
        if (getExp() >= getExpForLevel(getLevel() + 1) || getExpForLevel(getLevel()) > getExp()) setExp(getExpForLevel(getLevel()));

        if (!levelIncreased) return false;
        
        getActiveChar().getStatus().setCurrentHp(getActiveChar().getStat().getMaxHp());
        getActiveChar().getStatus().setCurrentMp(getActiveChar().getStat().getMaxMp());

        return true;
    }

    public boolean addSp(int value)
    {
        setSp(getSp() + value);
        return true;
    }
    
    public int getExpForLevel(int level) { return level; }

    // =========================================================
    // Method - Private

    // =========================================================
    // Property - Public
    public L2PlayableInstance getActiveChar() { return (L2PlayableInstance)super.getActiveChar(); }
}
