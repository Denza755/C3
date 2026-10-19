package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.ClientScheduler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.entity.SevenSigns;
import net.sf.l2j.gameserver.packets.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.tables.SkillTable;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

import java.util.concurrent.ScheduledFuture;

/**
 * @author Layane
 *
 */
public class L2CabaleBufferInstance extends L2NpcInstance
{
    ScheduledFuture aiTask;
    
    class CabalaAI implements Runnable
    {
        L2CabaleBufferInstance _caster;
        
        private CabalaAI(L2CabaleBufferInstance caster) 
        {
            _caster = caster;
        }
        
        public void run()
        {
            boolean isBuffAWinner = false;
            boolean isBuffALoser = false;
            
            final int winningCabal = SevenSigns.getInstance().getCabalHighestScore();
            
            int losingCabal = SevenSigns.CABAL_NULL;
            
            if (winningCabal == SevenSigns.CABAL_DAWN)
                losingCabal = SevenSigns.CABAL_DUSK;
            else if (winningCabal == SevenSigns.CABAL_DUSK)
                losingCabal = SevenSigns.CABAL_DAWN;
            
            /**
             * For each known player in range, cast either the positive or negative buff.
             * <BR>
             * The stats affected depend on the player type, either a fighter or a mystic.
             * <BR>
             * Curse of Destruction (Loser) 
             *  - Fighters: -25% Accuracy, -25% Effect Resistance
             *  - Mystics: -25% Casting Speed, -25% Effect Resistance
             * 
             * Blessing of Prophecy (Winner)
             *  - Fighters: +25% Max Load, +25% Effect Resistance
             *  - Mystics: +25% Magic Cancel Resist, +25% Effect Resistance
             */
            for (L2PcInstance player : getKnownList().getKnownPlayers())
            {
                final int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);
                
                if (playerCabal == winningCabal && getNpcId()==8094)
                {
                    if (!player.isMageClass()) {
                        if (handleCast(player, 4364)) {
                            isBuffAWinner = true;
                            continue;
                        }
                    }
                    else {
                        if (handleCast(player, 4365)) {
                            isBuffAWinner = true;
                            continue;
                        }
                    }
                }
                else if (playerCabal == losingCabal && getNpcId()==8093)
                {
                    if (!player.isMageClass()) {
                        if (handleCast(player, 4361)) {
                            isBuffALoser = true;
                            continue;
                        }
                    }
                    else {
                        if (handleCast(player, 4362)) {
                            isBuffALoser = true;
                            continue;
                        }
                    }
                }
                
                if (isBuffAWinner && isBuffALoser)
                    break;
            }
        }
        
        private boolean handleCast(L2PcInstance player, int skillId)
        {
            L2Skill skill = SkillTable.getInstance().getInfo(skillId, 2);
            
            if (player.getEffect(skill) == null)
            {
                setTarget(player);
                doCast(skill);
            
                MagicSkillUser msu = new MagicSkillUser(_caster, player, skill.getId(), 2, skill.getSkillTime(), 0);
                broadcastPacket(msu);
                
                return true;
            }
            
            return false;
        }
        
    }
    

    public L2CabaleBufferInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
        aiTask = ClientScheduler.getInstance().scheduleLowAtFixedRate(new CabalaAI(this), 3000, 3000);
    }
    
    public void deleteMe()
    {
        if (aiTask != null)
            aiTask.cancel(true);
        
        super.deleteMe();
    }
    
    public int getDistanceToWatchObject(L2Object object)
    {
        return 900;
    }
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.model.L2Object#isAttackable()
     */
    public boolean isAutoAttackable(@SuppressWarnings("unused") L2Character attacker)
    {
        return false;
    }
    
}
