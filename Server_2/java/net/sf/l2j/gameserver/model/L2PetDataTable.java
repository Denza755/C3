/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.model;

import java.util.Collection;
import java.util.HashMap;

public class L2PetDataTable
{
    private static L2PetDataTable _instance;
    
    private static HashMap<Integer, HashMap<Integer, L2PetData>> petTable;
    
    public static L2PetDataTable getInstance()
    {
        if (_instance == null)
        {
            _instance = new L2PetDataTable();
        }
        return _instance;
    }
    
    private L2PetDataTable()
    {
        petTable = new HashMap<Integer, HashMap<Integer, L2PetData>>();
    }
    
    public void addPetData(L2PetData petData)
    {
        //System.out.println("looking if hashmap for id "+petData.getPetID()+" created");
        HashMap<Integer, L2PetData> h = petTable.get(petData.getPetID());
        if (h == null)
        {//System.out.println("Nope");
            HashMap<Integer, L2PetData> statTable = new HashMap<Integer, L2PetData>();
            //System.out.println("New Stattable");
            statTable.put(petData.getPetLevel(), petData);
            //System.out.println("adding petdata for level "+petData.getPetLevel()+" created");
            petTable.put(petData.getPetID(), statTable);
            //System.out.println("added id: "+petData.getPetID());
            
        }
        else
        {
            //System.out.println("Found");
            //System.out.println("adding petdata for level "+petData.getPetLevel()+" created");
            h.put(petData.getPetLevel(), petData);
        }
        
    }
    
    public void addPetData(Collection<L2PetData> list)
    {
        for (L2PetData petData : list)
        {
            addPetData(petData);
        }
    }
    
    public void addPetData(L2PetData[] list)
    {
        for (int i = 0; i<list.length; i++)
        {//if (petData.getPetLevel() == 35)System.out.println(petData);
            addPetData(list[i]);
            //if (petData.getPetLevel() == 35)System.out.println(getPetData(petData.getPetID(), petData.getPetLevel()));
            //if (list[i].getPetLevel() == 35)System.out.println(list[i].getPetID()+" "+ list[i].getPetLevel());
            //if (list[i].getPetLevel() == 35)System.out.println(getPetData(list[i].getPetID(), list[i].getPetLevel()).getPetExp());
        }
        //System.out.println(getPetData(12311, 35).getPetExp());
    }
    
    public L2PetData getPetData(int petID, int petLevel)
    {
        HashMap<Integer, L2PetData> h = petTable.get(petID);
        //System.out.println("Getting id "+petID+" level "+petTable.keySet());
        L2PetData t = h.get(petLevel);
        return t;
    }
}
