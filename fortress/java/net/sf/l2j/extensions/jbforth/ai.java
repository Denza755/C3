package net.sf.l2j.extensions.jbforth;

import java.util.logging.Logger;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;

import ru.balancer.JBForth;
import ru.balancer.jbforth.Dict;
import ru.balancer.jbforth.Word;

public class ai
{
    static Logger _log = Logger.getLogger(ai.class.getName());

    public ai(Dict d)
    {
        d.reg("intention!", new set_intention());
    }

    public class set_intention extends Word
    {
		// ( ai-obj "intention" x1 x2 -- )
        public void exec(JBForth jbf) 
        {
        	Object x2 = jbf.st.pop();
        	Object x1 = jbf.st.pop();
        	String intention = jbf.st.pop_s();
        	L2CharacterAI ai = (L2CharacterAI)jbf.st.pop();
        	CtrlIntention inte = null;

			if(intention.equalsIgnoreCase("idle"))
				inte = CtrlIntention.AI_INTENTION_IDLE;
			else if(intention.equalsIgnoreCase("active"))
				inte = CtrlIntention.AI_INTENTION_ACTIVE;
			else if(intention.equalsIgnoreCase("rest"))
				inte = CtrlIntention.AI_INTENTION_REST;
			else if(intention.equalsIgnoreCase("attack"))
				inte = CtrlIntention.AI_INTENTION_ATTACK;
			else if(intention.equalsIgnoreCase("cast"))
				inte = CtrlIntention.AI_INTENTION_CAST;
			else if(intention.equalsIgnoreCase("move-to"))
				inte = CtrlIntention.AI_INTENTION_MOVE_TO;
			else if(intention.equalsIgnoreCase("follow"))
				inte = CtrlIntention.AI_INTENTION_FOLLOW;
			else if(intention.equalsIgnoreCase("pick-up"))
				inte = CtrlIntention.AI_INTENTION_PICK_UP;
			else if(intention.equalsIgnoreCase("interact"))
				inte = CtrlIntention.AI_INTENTION_INTERACT;

			ai.setIntention(inte, x1, x2);
		}
 	}
}
