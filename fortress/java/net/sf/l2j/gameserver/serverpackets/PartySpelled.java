package net.sf.l2j.gameserver.serverpackets;

import java.util.ArrayList;

import net.sf.l2j.gameserver.model.L2PcInstance;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class PartySpelled extends ServerBasePacket
{
    private static final String _S__EE_PartySpelled = "[S] EE PartySpelled";
    private ArrayList<Effect> _effects;
    private L2PcInstance _player;
    
    
    class Effect
    {
        int skillId;
        public Effect(int skillId)
        {
            this.skillId = skillId;
        }
    }
    
    public PartySpelled()
    {
        _effects = new ArrayList<Effect>();
    }
    
        
    final void runImpl()
    {
        // no long-running tasks
    }
    
    final void writeImpl()
    {
	if (_player == null)
	    return;
        writeC(0xee);
        writeD(0);
        writeD(_player.getObjectId());
        writeD(_effects.size());
        for (Effect temp : _effects)
        {
         writeD(temp.skillId);
        }

    }
    public void addPartySpelledEffect(int skillId)
    {
        _effects.add(new Effect(skillId));       
    }
    
    public void addSpellRecivedPlayer(L2PcInstance cha)
    {
        _player = cha;
    }
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
     */
    public String getType()
    {
        return _S__EE_PartySpelled;
    }
}
