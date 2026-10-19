package net.sf.l2j.gameserver;

import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2ItemInstance;

import java.util.List;
//import net.sf.l2j.gameserver.model.L2World;


public class ItemsAutoSetPriority
{
    private static ItemsAutoSetPriority _instance;
    protected List<L2ItemInstance> _items = null;
    static long _sleep;
    
    private ItemsAutoSetPriority()
    {
        _items = new FastList<L2ItemInstance>();

        _sleep = 10000; // 10 second for pick up own items drop
        ClientScheduler.getInstance().scheduleLowAtFixedRate(new CheckItemsForSetPriority(),0,_sleep);
    }
    
    public static ItemsAutoSetPriority getInstance()
    {
        if (_instance == null)
        {
            _instance = new ItemsAutoSetPriority();
        }
        
        return _instance;
    }
    
    public void addItem (L2ItemInstance item)
    {
        item.setDropTime(System.currentTimeMillis());
        _items.add(item);
    }
    
    class CheckItemsForSetPriority extends Thread
    {
        public void run()
        {
            try {
                for(;;)
                {
                    long curtime = System.currentTimeMillis();
                    for (L2ItemInstance item : _items)
                    {
                        if ( (curtime - item.getDropTime()) > _sleep){
                            item.setOwnerId(0);
                        }
                    }
                    sleep(_sleep);
                }
            } catch (Exception e) {}
        }    
    }
}
