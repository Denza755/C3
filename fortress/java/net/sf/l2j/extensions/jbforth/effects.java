package net.sf.l2j.extensions.jbforth;

import ru.balancer.JBForth;
import ru.balancer.jbforth.Dict;
import ru.balancer.jbforth.Word;

//import java.util.logging.Logger;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.Dice;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.PlaySound;
import net.sf.l2j.gameserver.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

public class effects
{
//    static Logger _log = Logger.getLogger(L2j_ride.class.getName());

    public effects(Dict d)
    {
        d.reg("MAGIC-SKILL-USE", new magic_skill_use());
        d.reg("MAGIC-SKILL-TARGET", new magic_skill_target());
        d.reg("PLAY-SOUND", new play_sound());
        d.reg("GAUGE", new gauge());
        d.reg("(DICE)", new dice());
    }

    public class magic_skill_use extends Word
    {
//    	( skill_id time player -- ) 
        public void exec(JBForth jbf) 
        {
        	L2PcInstance player = (L2PcInstance)jbf.st.pop();
        	Integer reuse = jbf.st.pop_i();
        	Integer time = jbf.st.pop_i();
//        	Integer level = jbf.st.pop_i();
        	Integer skill_id = jbf.st.pop_i();
			player.broadcastPacket(new MagicSkillUser(player, skill_id, 1, time, reuse));
		}
 	}

    public class magic_skill_target extends Word
    {
//    	( skill_id time reuse player target -- ) 
        public void exec(JBForth jbf) 
        {
        	L2Character target = (L2Character)jbf.st.pop();
        	L2PcInstance player = (L2PcInstance)jbf.st.pop();
        	Integer reuse = jbf.st.pop_i();
        	Integer time = jbf.st.pop_i();
        	Integer skill_id = jbf.st.pop_i();
			player.broadcastPacket(new MagicSkillUser(player, target, skill_id, 1, time, reuse));
		}
 	}

    public class play_sound extends Word
    {
//    	( sound player -- ) 
        public void exec(JBForth jbf) 
        {
        	L2PcInstance player = (L2PcInstance)jbf.st.pop();
        	if(player != null)
	            player.sendPacket(new PlaySound(jbf.st.pop_s()));
		}
//      PlaySound SRace2 = new PlaySound(0,"ItemSound2.race_start",1,121209259,12125,182487,-3559);
//      PlaySound SRace = new PlaySound(1,"S_Race",0,0,0,0,0);
 	}

    public class gauge extends Word
    {
//    	( time type player -- ) 
        public void exec(JBForth jbf) 
        {
        	L2PcInstance player = (L2PcInstance)jbf.st.pop();
        	// type: color  0-blue   1-red  2-cyan  3-lime
            player.sendPacket(new SetupGauge(jbf.st.pop_i(), jbf.st.pop_i()));
		}
 	}

    public class dice extends Word
    {
        public void exec(JBForth jbf) 
        { 
            // ( player item_id -- number )

            int number = Rnd.get(1,6);
            int item_id = jbf.st.pop_i();
            L2PcInstance activeChar = (L2PcInstance)jbf.st.pop();

            Dice d = new Dice (activeChar.getObjectId(), item_id, number, activeChar.getX()-30+Rnd.get(60), activeChar.getY()-30+Rnd.get(60), activeChar.getZ());

            activeChar.broadcastPacket(d);
            SystemMessage sm = new SystemMessage(0x0342);
            sm.addString(activeChar.getName());
            sm.addNumber(number);

            activeChar.broadcastPacket(sm);

            jbf.st.push(number);
        }
    }
}
