/**
 * 
 */
package net.sf.l2j.gameserver.model;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.ClientScheduler;

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @author la2
 *
 */
public class DecayTask
{
    private Map<L2Character,Long> _decayTasks = new FastMap<L2Character,Long>().setShared(true);
    private FastList<L2Character> _actorsToRemove = new FastList<L2Character>(); // put this as a field to avoid newing it every time

    public static DecayTask _instance;
    
    public DecayTask()
    {
        ClientScheduler.getInstance().scheduleMedAtFixedRate(new DecayScheduler(),10000,5000);
    }
    
    public static DecayTask getInstance()
    {
        if(_instance == null)
            _instance = new DecayTask();
        
        return _instance;
    }
    
    public void addDecayTask(L2Character actor)
    {
        _decayTasks.put(actor,System.currentTimeMillis());
    }

    public void addDecayTask(L2Character actor, int interval)
    {
        _decayTasks.put(actor,System.currentTimeMillis()+interval);
    }
    
    public void cancelDecayTask(L2Character actor)
    {
        try
        {
            _decayTasks.remove(actor);
        }
        catch(NoSuchElementException e){}
    } 
    
    private class DecayScheduler implements Runnable
    {   
        public void run()
        {
            Long current = System.currentTimeMillis();
            if (_decayTasks != null)
                synchronized(_decayTasks)
                {
                    
                    for( L2Character actor : _decayTasks.keySet())
                    {
                        if((current - _decayTasks.get(actor)) > 8500)
                        {
                            actor.onDecay();
                            _actorsToRemove.add(actor);
                            
                        }
                    }
                    for(L2Character actor : _actorsToRemove)
                    {
                        _decayTasks.remove(actor);
                    }
                    _actorsToRemove.clear();
                }
        }
    }

    public String toString()
    {
        String ret = "============= DecayTask Manager Report ============\n";
        ret += "Tasks count: "+_decayTasks.size();
        ret += "Tasks dump:\n";
        
        synchronized(_decayTasks)
        {
            Long current = System.currentTimeMillis();
            for( L2Character actor : _decayTasks.keySet())
            {
                ret += "Class/Name: "+actor.getClass().getName()+"/"+actor.getName()
                +" decay timer: "+(current - _decayTasks.get(actor))+"\n";
            }
        }
        
        return ret;
    }
}
