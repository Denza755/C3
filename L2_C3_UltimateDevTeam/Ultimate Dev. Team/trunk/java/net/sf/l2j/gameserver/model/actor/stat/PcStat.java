package net.sf.l2j.gameserver.model.actor.stat;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.Experience;
import net.sf.l2j.gameserver.packets.serverpackets.*;

public class PcStat extends PlayableStat
{
    // =========================================================
    // Data Field

    private int _OldMaxHp;      // stats watch
    private int _OldMaxMp;      // stats watch
    
    // =========================================================
    // Constructor
    public PcStat(L2PcInstance[] activeChar)
    {
        super(activeChar);
    }

    // =========================================================
    // Method - Public
    public boolean addExp(int value)
    {
        if (!super.addExp(value)) return false;

        // Set new karma
        if (getActiveChar().getKarma() > 0 && (getActiveChar().isGM() || !ZoneManager.getInstance().checkIfInZonePvP(getActiveChar())))
        {
            int karmaLost = getActiveChar().calculateKarmaLost(value);
            if (karmaLost > 0) getActiveChar().setKarma(getActiveChar().getKarma() - karmaLost);
        }

        getActiveChar().sendPacket(new StatusUpdate(getActiveChar().getObjectId()).addAttribute(StatusUpdate.EXP, getExp()));

        return true;
    }

    /**
     * Add Experience and SP rewards to the L2PcInstance, remove its Karma (if necessary) and Launch increase level task.<BR><BR>
     *
     * <B><U> Actions </U> :</B><BR><BR>
     * <li>Remove Karma when the player kills L2MonsterInstance</li>
     * <li>Send a Server->Client packet StatusUpdate to the L2PcInstance</li>
     * <li>Send a Server->Client System Message to the L2PcInstance </li>
     * <li>If the L2PcInstance increases it's level, send a Server->Client packet SocialAction (broadcast) </li>
     * <li>If the L2PcInstance increases it's level, manage the increase level task (Max MP, Max MP, Recommandation, Expertise and beginner skills...) </li>
     * <li>If the L2PcInstance increases it's level, send a Server->Client packet UserInfo to the L2PcInstance </li><BR><BR>
     *
     * @param addToExp The Experience value to add
     * @param addToSp The SP value to add
     */
    public boolean addExpAndSp(int addToExp, int addToSp)
    {
    	if (!super.addExpAndSp(addToExp, addToSp)) return false;

        // Send a Server->Client System Message to the L2PcInstance
        SystemMessage sm = new SystemMessage(SystemMessage.YOU_EARNED_S1_EXP_AND_S2_SP);
        sm.addNumber(addToExp);
        sm.addNumber(addToSp);
        getActiveChar().sendPacket(sm);

        return true;
    }

    public final boolean addLevel(int value)
    {
        if (getLevel() + value > Config.MAX_LEVEL) return false;

        boolean levelIncreased = super.addLevel(value);
        
        if (levelIncreased)
        {
        	getActiveChar().setCurrentCp(getMaxCp());
            getActiveChar().broadcastPacket(new SocialAction(getActiveChar().getObjectId(), 15));
            getActiveChar().sendPacket(new SystemMessage(SystemMessage.YOU_INCREASED_YOUR_LEVEL));
        }

        getActiveChar().rewardSkills(); // Give Expertise skill of this level
        if (getActiveChar().getClan() != null) getActiveChar().getClan().broadcastToOtherOnlineMembers(new PledgeShowMemberListAdd(getActiveChar()), getActiveChar());
        if (getActiveChar().isInParty()) getActiveChar().getParty().recalculatePartyLevel(); // Recalculate the party level

        StatusUpdate su = new StatusUpdate(getActiveChar().getObjectId());
        su.addAttribute(StatusUpdate.LEVEL, getLevel());
        su.addAttribute(StatusUpdate.MAX_CP, getMaxCp());
        su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
        su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
        getActiveChar().sendPacket(su);
            
        // Send a Server->Client packet UserInfo to the L2PcInstance
        getActiveChar().sendPacket(new UserInfo(getActiveChar()));

        return levelIncreased;
    }

    public boolean addSp(int value)
    {
        if (!super.addSp(value)) return false;

        getActiveChar().sendPacket(new StatusUpdate(getActiveChar().getObjectId()).addAttribute(StatusUpdate.SP, getSp()));

        return true;
    }

    public final int getExpForLevel(int level) { return Experience.LEVEL[level]; }

    // =========================================================
    // Method - Private

    // =========================================================
    // Property - Public
    public final L2PcInstance getActiveChar() { return (L2PcInstance)super.getActiveChar(); }

    public final int getExp()
    {
        if (getActiveChar().isSubClassActive()) return getActiveChar().getSubClasses().get(getActiveChar().getClassIndex() - 1).getExp();
        return super.getExp();
    }
    public final void setExp(int value)
    {
        if (getActiveChar().isSubClassActive())
            getActiveChar().getSubClasses().get(getActiveChar().getClassIndex() - 1).setExp(value);
        else
            super.setExp(value);
    }

    public final int getLevel()
    {
        if (getActiveChar().isSubClassActive()) return getActiveChar().getSubClasses().get(getActiveChar().getClassIndex() - 1).getLevel();
        return super.getLevel();
    }
    public final void setLevel(int value)
    {
        if (value > Config.MAX_LEVEL) value = Config.MAX_LEVEL;
        if (getActiveChar().isSubClassActive())
            getActiveChar().getSubClasses().get(getActiveChar().getClassIndex() - 1).setLevel(value);
        else
            super.setLevel(value);
    }

    public final int getMaxHp()
    {
        // Get the Max HP (base+modifier) of the L2PcInstance
        int val = super.getMaxHp();
        if (val != _OldMaxHp)
        {
            _OldMaxHp = val;

            // Launch a regen task if the new Max HP is higher than the old one
            if (getActiveChar().getStatus().getCurrentHp() != val) getActiveChar().getStatus().setCurrentHp(getActiveChar().getStatus().getCurrentHp()); // trigger start of regeneration
        }

        return val;
    }

    public final int getMaxMp()
    {
        // Get the Max MP (base+modifier) of the L2PcInstance
        int val = super.getMaxMp();
        if (val != _OldMaxMp)
        {
            _OldMaxMp = val;

            // Launch a regen task if the new Max MP is higher than the old one
            if (getActiveChar().getStatus().getCurrentMp() != val) getActiveChar().getStatus().setCurrentMp(getActiveChar().getStatus().getCurrentMp()); // trigger start of regeneration
        }

        return val;
    }

    public final int getSp()
    {
        if (getActiveChar().isSubClassActive()) return getActiveChar().getSubClasses().get(getActiveChar().getClassIndex() - 1).getSp();
        return super.getSp();
    }
    public final void setSp(int value)
    {
        if (getActiveChar().isSubClassActive())
            getActiveChar().getSubClasses().get(getActiveChar().getClassIndex() - 1).setSp(value);
        else
            super.setSp(value);
    }
}
