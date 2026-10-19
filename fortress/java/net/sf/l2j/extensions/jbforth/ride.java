package net.sf.l2j.extensions.jbforth;

//import java.util.logging.Logger;
import ru.balancer.JBForth;
import ru.balancer.jbforth.Dict;
import ru.balancer.jbforth.Word;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.Ride;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

public class ride
{
//    static Logger _log = Logger.getLogger(L2j_ride.class.getName());

    public ride(Dict d)
    {
        d.reg("RIDE", new _ride());
        d.reg("UNRIDE", new unride());
    }

    public class _ride extends Word
    {
//    	( pet_id player -- flag ) 
        public void exec(JBForth jbf) 
        {
        	L2PcInstance player = (L2PcInstance)jbf.st.pop();
        	Integer pet = jbf.st.pop_i();

            if(player.isMounted())
			{
	            player.broadcastPacket(new Ride(player.getObjectId(), Ride.ACTION_DISMOUNT,0));
    	        player.setMountType(0);
			}

            if(player.getPet() != null)
            {
            	SystemMessage sm = new SystemMessage(614);
                sm.addString("Already Have a Pet.");
                player.sendPacket(sm);
				jbf.st.push(0);
                return;
            }

            Ride mount = new Ride(player.getObjectId(), Ride.ACTION_MOUNT, pet);
            player.broadcastPacket(mount);
            player.setMountType(mount.getMountType());
			jbf.st.push(-1);
		}
 	}

    public class unride extends Word
    {
//    	( player -- ) 
        public void exec(JBForth jbf) 
        {
        	L2PcInstance player = (L2PcInstance)jbf.st.pop();
            player.broadcastPacket(new Ride(player.getObjectId(), Ride.ACTION_DISMOUNT,0));
            player.setMountType(0);
		}
 	}
}
