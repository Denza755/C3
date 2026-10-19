package net.sf.l2j.gameserver.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;

/**
 * Service class for manor
 * 2be rewritten ;)
 */

public class L2Manor
{
    private static Logger _log = Logger.getLogger(L2Manor.class.getName());
    private static L2Manor _instance;

    private static Map<Integer, SeedData> _seeds;
    private static boolean initialized = false;
    
    private L2Manor()
    {
        _seeds = new FastMap<Integer,SeedData>();
        parseData();
    }
    
    public static L2Manor getInstance()
    {
        if (_instance == null)
            _instance = new L2Manor();
        return _instance;
    }
    
    public int getSeedLevel(int seedId)
    {
        SeedData seed = _seeds.get(seedId);
        
        if(seed != null)
            return seed.level;
        return -1;
    }
    
    public int getCropType(int seedId)
    {
        SeedData seed = _seeds.get(seedId);

        if(seed != null)
            return seed.crop;
        return -1;
    }
    
    public synchronized int getRewardItem(int cropId,int type)
    {
        for(SeedData seed : _seeds.values())
        {
            if(seed.crop == cropId)
            {
                if(type == 1)
                {
                    return seed.type1;
                }
                else if(type == 2)
                {
                    return seed.type2;
                }
                else if(type == 0)
                {
                    return 0;
                }
            }
        }
        
        return -1;        
    }
    
    // syncronize to avoid exceptions while changing seeds data
    public synchronized int getRewardAmount(int cropId,int type)
    {
        for(SeedData seed : _seeds.values())
        {
            if(seed.crop == cropId)
            {
                if(type == 1)
                {
                    return seed.type1amount;
                }
                else if(type == 2)
                {
                    return seed.type2amount;
                }
            }
        }
        
        return -1;
    }
    
    /**
     * Return all crops which can be purchased by given castle
     * @param castleId
     * @return
     */
    public FastList<Integer> getCropForCastle(int castleId)
    {
        FastList<Integer> crops =  new FastList<Integer>();
        
        for(SeedData seed : _seeds.values())
        {
            switch(castleId)
            {
                case 1:     // Gludio Castle
                    if(seed.GL == 1)
                        crops.add(seed.crop);
                    break;
                case 2:     // Dion castle
                    if(seed.DI == 1)
                        crops.add(seed.crop);
                    break;
                case 3:     // Giran
                    if(seed.GI == 1)
                        crops.add(seed.crop);
                    break;
                case 4:     // Oren
                    if(seed.OR == 1)
                        crops.add(seed.crop);
                    break;
                case 5:     // Aden Castle
                    if(seed.AD == 1)
                        crops.add(seed.crop);
                    break;
                case 6:     // Innadil Castle
                    if(seed.IN == 1)
                        crops.add(seed.crop);
                    break;
                default:
                    _log.warning("[L2Manor::getCropForCastle] invalid castle index? "+castleId);
            }
        }
        
        return crops;
    }
    
    public FastList<Integer> getSeedsForCastle(int castleId)// by L2Emu team 177-223
    {
        FastList<Integer> seedsID =  new FastList<Integer>();
        
        for(SeedData seed : _seeds.values())
        {
            switch(castleId)
            {
                case 1:     // Gludio Castle
                    if(seed.GL == 1)
                        seedsID.add(seed.id);
                    break;
                case 2:     // Dion castle
                    if(seed.DI == 1)
                        seedsID.add(seed.id);
                    break;
                case 3:     // Giran
                    if(seed.GI == 1)
                        seedsID.add(seed.id);
                    break;
                case 4:     // Oren
                    if(seed.OR == 1)
                        seedsID.add(seed.id);
                    break;
                case 5:     // Aden Castle
                    if(seed.AD == 1)
                        seedsID.add(seed.id);
                    break;
                case 6:     // Innadil Castle
                    if(seed.IN == 1)
                        seedsID.add(seed.id);
                    break;
                default:
                    _log.warning("[L2Manor::getSeedsForCastle] invalid castle index? "+castleId);
            }
        }
        
        return seedsID;
    }
    
    private class SeedData
    {
        public SeedData(int pLevel,int pCrop)
        {
            this.level = pLevel;
            this.crop = pCrop;
        }
        
        public void setData(int pId, int t1, int t2, int t1a, int t2a, int gl, int di, int gi, int or, int ad, int in)
        {
            this.id = pId;
            type1 = t1;
            type2 = t2;
            type1amount = t1a;
            type2amount = t2a;
            GL = gl;
            DI = di;
            GI = gi;
            OR = or;
            AD = ad;
            IN = in;
        }
        
        public int id;
        public int level;          // seed level
        public int crop;           // crop type
        public int type1;
        public int type2;
        public int type1amount;
        public int type2amount;
        public int GL;             // Gludio Castle
        public int DI;             // Dion Castle
        public int GI;             // Giran Castle
        public int OR;             // Oren Castle
        public int AD;             // Aden Castle
        public int IN;             // Innadril Castle
    }
    
    
    private void parseData() {
        LineNumberReader lnr = null;
        try
        {
            File seedData = new File(Config.DATAPACK_ROOT, "data/seeds.csv");
            lnr = new LineNumberReader(new BufferedReader(new FileReader(seedData)));

            String line = null;
            while ((line = lnr.readLine()) != null)
            {
                if (line.trim().length() == 0 || line.startsWith("#"))
                {
                    continue;
                }
                SeedData seed = parseList(line);
                _seeds.put(seed.id, seed);             
            }

            initialized = true;
            _log.config("L2Manor: Loaded " + _seeds.size() + " seeds");
        }
        catch (FileNotFoundException e)
        {
            initialized = false;
            _log.warning("seeds.csv is missing in data folder");
        }
        catch (Exception e)
        {
            initialized = false;
            _log.warning("error while loading seeds: " + e);
        }
        finally
        {
            try
            {
                lnr.close();
            }
            catch (Exception e1)
            {
            }
        }
    }

    private SeedData parseList(String line)
    {
        
        StringTokenizer st = new StringTokenizer(line, ";");

        int seedId = Integer.parseInt(st.nextToken());  // seed id
        int level = Integer.parseInt(st.nextToken());   // seed level
        int cropId = Integer.parseInt(st.nextToken());  // crop id
        int type1R = Integer.parseInt(st.nextToken());  // type I reward
        int type1A = Integer.parseInt(st.nextToken());  // type I reward amount
        int type2R = Integer.parseInt(st.nextToken());  // type II reward
        int type2A = Integer.parseInt(st.nextToken());  // type II reward amount
        int GL = Integer.parseInt(st.nextToken());      // can be produced/sold in Gludio castle
        int DI = Integer.parseInt(st.nextToken());      // can be produced/sold in Dion castle
        int GI = Integer.parseInt(st.nextToken());      // can be produced/sold in Giran castle
        int OR = Integer.parseInt(st.nextToken());      // can be produced/sold in Oren castle
        int AD = Integer.parseInt(st.nextToken());      // can be produced/sold in Aden castle
        int IN = Integer.parseInt(st.nextToken());      // can be produced/sold in Innadril castle

        SeedData seed = new SeedData(level,cropId);
        seed.setData(seedId,type1R,type2R,type1A,type2A,GL,DI,GI,OR,AD,IN);
        
        return seed;
    }
    
    public boolean isInitialized()
    {
        return initialized;
    }
}
