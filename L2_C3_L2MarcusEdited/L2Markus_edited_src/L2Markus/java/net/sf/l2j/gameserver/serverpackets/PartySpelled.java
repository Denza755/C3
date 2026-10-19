package net.sf.l2j.gameserver.serverpackets;

import java.util.List;

import javolution.util.FastList;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SummonInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.L2Character;

/**
 * This class ...
 *
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class PartySpelled extends L2GameServerPacket
{
    private static final String _S__EE_PartySpelled = "[S] EE PartySpelled";
    private List<Effect> _effects;
    private L2Character _player;



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
        _effects = new FastList<Effect>();
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
        writeD(_player instanceof L2SummonInstance ? 2 : _player instanceof L2PetInstance ? 1 : 0);
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
