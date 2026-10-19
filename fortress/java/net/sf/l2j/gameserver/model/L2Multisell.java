/**
 * 
 */
package net.sf.l2j.gameserver.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 * Multisell list manager
 *
 */
public class L2Multisell
{
    private static Logger _log = Logger.getLogger(L2Multisell.class.getName());
    private ArrayList<MultiSellEntry> entries = new ArrayList<MultiSellEntry>();
    private static L2Multisell _instance = new L2Multisell();
    private LineNumberReader lnr = null;
    
    public ArrayList<MultiSellEntry> getList(int id)
    {
        ArrayList<MultiSellEntry> tmp = new ArrayList<MultiSellEntry>();
        
        synchronized(entries)
        {
            for(MultiSellEntry ent : entries)
            {
                if(ent.getListId() == id)
                    tmp.add(ent);
            }
        }
        
        return tmp;
    }
    
    public L2Multisell()
    {
        parseData();
    }
    
    public void reload()
    {
        parseData();
    }
    
    public static L2Multisell getInstance()
    {
        return _instance;
    }
    
    private void parseData() 
    {
        entries.clear();
        try 
        {
            File data = new File("data/multisell.csv");
            lnr = new LineNumberReader(new BufferedReader(new FileReader(data)));

            String line = null;
            while ((line = lnr.readLine()) != null) 
            {
                if (line.trim().length() == 0 || line.startsWith("#")) 
                    continue;

                parseList(line);             
            }

            _log.config("[L2Multisell]: Loaded " + entries.size() + " multisell lists.");
        } 
        catch (FileNotFoundException e) 
        {
            _log.warning("multisell.csv is missing in data folder");
        } 
        catch (Exception e) 
        {
            _log.warning("error while loading multisell lists " + e);
        } 
        finally 
        {
            try { lnr.close(); } catch (Exception e1) { /* ignore problems */ }
        }
    }
    
    public void parseList(String txt)
    {
        StringTokenizer st = new StringTokenizer(txt, ";");

        int listid = Integer.parseInt(st.nextToken());
        int itemid = Integer.parseInt(st.nextToken());
        int itemcount = Integer.parseInt(st.nextToken());
        int compcount = Integer.parseInt(st.nextToken());
        String comps = st.nextToken();
        
        MultiSellEntry entry = new MultiSellEntry(listid,itemid,itemcount,compcount);
        
        StringTokenizer st2 = new StringTokenizer(comps,",");
        
        for(int i=0;i<compcount;i++)
        {
            try
            {
                int compId = Integer.parseInt(st2.nextToken());
                int compCount = Integer.parseInt(st2.nextToken());
                int compEnchant = Integer.parseInt(st2.nextToken());
                entry.addComp(compId,compCount,compEnchant);
            }
            catch(Exception e)
            {
                _log.warning("[L2Multisell] error parsing multisell entry, invalid ingredients data?" +
                             "Line: "+lnr.getLineNumber()+" Exception: "+e);
            }
        }
        
        entries.add(entry);
    }
    
    public class MultiSellEntry
    {
        private int _list;
        private int _itemId;
        private int _itemAmount;
        private int _compAmount;
        
        private int _compId[];
        private int _compCount[];
        private int _compEnchant[];
        
        public MultiSellEntry(int listId, int itemId, int itemCount, int compcount)
        {
            _list = listId;
            _itemId = itemId;
            _itemAmount = itemCount;
            _compAmount = 0;
            _compId = new int[compcount];
            _compCount = new int[compcount];
            _compEnchant = new int[compcount];
        }
        
        public void addComp(int id, int count, int enchant)
        {
            _compId[_compAmount] = id;
            _compCount[_compAmount] = count;
            _compEnchant[_compAmount] = enchant;
            _compAmount++;
        }
        
        public int[] getComp(int id)
        {
            int tmp[] = new int[3];
            tmp[0] = _compId[id];
            tmp[1] = _compCount[id];
            tmp[2] = _compEnchant[id];
            return tmp;
        }
        
        public int getListId()
        {
            return _list;
        }
        
        public int getItemId()
        {
            return _itemId;
        }
        
        public int getItemAmount()
        {
            return _itemAmount;
        }
        
        public int getCompAmount()
        {
            return _compAmount;
        }
    }

}
