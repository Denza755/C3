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
package net.sf.l2j.gameserver.script.faenor;

import java.util.HashMap;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.script.IntList;
import net.sf.l2j.gameserver.script.Parser;
import net.sf.l2j.gameserver.script.ParserFactory;
import net.sf.l2j.gameserver.script.ScriptEngine;

import org.apache.bsf.BSFManager;
import org.w3c.dom.Node;

/**
 * @author Luis Arias
 *
 */
public class FaenorWorldDataParser extends FaenorParser
{
    //Script Types
    private final static String PET_DATA        = "PetData";
    private final static String NPC_INFO_DATA   = "NpcData";
    private final static String NPC_STAT_DATA   = "NpcStatData";
    private final static String NPC_DROP_DATA   = "NpcDropData";
    private final static String ITEM_DATA       = "ItemData";
    
    //Default Npc Stats
    private final static double NPC_COLLISION_HEIGHT        = 30;
    private final static double NPC_COLLISION_RADIUS        = 15;
    private static final String NPC_NAME                    = "NoName";
    private static final int NPC_ATTACK_RANGE               = 40;
    private static final int NPC_PHYSICAL_ATTACK_SPEED      = 253;
    private static final int NPC_AGGRO_RANGE                = 0;
    private static final int NPC_MAGICAL_ATTACK_SPEED       = 10;
    private static final int NPC_RIGHT_HAND_WEAPON_ID       = 0;
    private static final int NPC_LEFT_HAND_WEAPON_ID        = 0;
    private static final int NPC_ARMOR_ID                   = 0;
    private static final int NPC_WALK_SPEED                 = 80;
    private static final int NPC_RUN_SPEED                  = 110;
    
    public void parseScript(Node eventNode, BSFManager context)
    {
        if (Config.DEBUG) System.out.println("Parsing WorldData");
        
        for (Node node = eventNode.getFirstChild(); node != null; node = node.getNextSibling()) {
            
            if (isNodeName(node, PET_DATA))
            {
                parsePetData(node, context);
            }
            else if (isNodeName(node, NPC_STAT_DATA))
            {
                parseNpcStatData(node);
            }
            else if (isNodeName(node, NPC_INFO_DATA))
            {
                parseNpcInfoData(node);
            }
            else if (isNodeName(node, NPC_DROP_DATA))
            {
                parseNpcDropData(node);
            }
            else if (isNodeName(node, ITEM_DATA))
            {
                parseItemData(node);
            }
        }
    }

    private void parseNpcInfoData(Node node)
    {
        // TODO Auto-generated method stub
        if (Config.DEBUG) System.out.println("Parsing Npc Info Data NOT DONE.");
    }

    private void parseItemData(Node itemNode)
    {
        /*/ TODO Auto-generated method stub
        if (Config.DEBUG) System.out.print("Parsing Item Data...");
        
        try
        {
            int itemID                  = getInt(attribute(itemNode, "ID"));
            String itemName             = element(itemNode, "Name", "NoName");
            String itemWeight           = element(itemNode, "Weight", "0");
            String itemMaterial         = element(itemNode, "Material", "steel");
            String itemPrice            = element(itemNode, "Price", "0");
            String itemType             = element(itemNode, "ItemType", "none");
            String itemConsumable       = element(itemNode, "Consumable", "normal");
            String itemGrade            = element(itemNode, "Grade", "none");
            String itemDurability       = element(itemNode, "Durability", "100");
            
            String itemGroup            = element(itemNode, "Group", "Item");
            
            if (itemGroup.equalsIgnoreCase("Weapon"))
            {

                
                bridge.addWeapon();
            }
            else if (itemGroup.equalsIgnoreCase("Armor"))
            {
                
                
                bridge.addArmor();
            }
            else if (itemGroup.equalsIgnoreCase("Item"))
            {
                bridge.addItem();
            }
            String itemId       = element(itemNode, "Name");
            String itemId       = element(itemNode, "Name");
            String itemId       = element(itemNode, "Name");
            String itemId       = element(itemNode, "Name");
            String itemId       = element(itemNode, "Name");
            String itemId       = element(itemNode, "Name");
            String itemId       = element(itemNode, "Name");
        }
        catch (Exception e)
        {
            System.err.println("ERROR(parseItemData):" + e.getMessage());
            return;
        }
        
        if (Config.DEBUG) System.out.println("SUCCESS");//*/
    }

    private void parseNpcDropData(Node npcNode)
    {
        //if (Config.DEBUG) System.out.print("Parsing Npc Drop Data... ");

        try
        {
            int mobId       = getInt(element(npcNode, "MonsterID"));
            int itemId      = getInt(element(npcNode, "ItemID"));
            int minDrop     = getInt(element(npcNode, "MinimumDrop"));
            int maxDrop     = getInt(element(npcNode, "MaximumDrop"));
            boolean sweep   = getInt(element(npcNode, "isSweepDrop")) == 1;
            int chance      = getInt(element(npcNode, "Chance"));

            bridge.addDrop(mobId, itemId, minDrop, maxDrop, sweep, chance);
        }
        catch (Exception e)
        {
            System.err.println("ERROR(parseNpcDropData):" + e.getMessage());
            return;
        }
        
        //if (Config.DEBUG) System.out.println("SUCCESS");
    }

    private void parseNpcStatData(Node npcNode)
    {
        //if (Config.DEBUG) System.out.print("Parsing Npc Stat Data... ");

        try
        {
//            int npcID                   = getInt(attribute(npcNode, "ID"));
//            String npcName              = element(npcNode, "Name", "NoName");
//            String npcGroup             = element(npcNode, "Group", "MainGroup");
//            double npcCollisionRadius   = getDouble(element(npcNode, "CollisionRadius", Double.toString(NPC_COLLISION_HEIGHT)));
//            double npcCollisionHeight   = getDouble(element(npcNode, "CollisionHeight", Double.toString(NPC_COLLISION_RADIUS)));
//            int npcLevel                = getInt(element(npcNode, "Level"));
//            String npcSex               = element(npcNode, "Sex", NPC_NAME);
//            String npcClassType         = element(npcNode, "NpcClass");
//            int npcAttackRange          = getInt(element(npcNode, "Attackrange", Integer.toString(NPC_ATTACK_RANGE)));
//            int npcHitPoints            = getInt(element(npcNode, "HitPoints"));
//            int npcMagicPoints          = getInt(element(npcNode, "MagicPoints"));
//            int npcExperience           = getInt(element(npcNode, "Experience"));
//            int npcSP                   = getInt(element(npcNode, "SP"));
//            int npcPhysicalAttack       = getInt(element(npcNode, "PhysicalAttack")); 
//            int npcPhysicalDefense      = getInt(element(npcNode, "PhysicalDefense"));
//            int npcMagicalAttack        = getInt(element(npcNode, "MagicalAttack"));
//            int npcMagicalDefense       = getInt(element(npcNode, "MagicalDefense"));
//            int npcPhysicalAttackSpeed  = getInt(element(npcNode, "PhysicalAttackSpeed", Integer.toString(NPC_PHYSICAL_ATTACK_SPEED)));
//            int npcAggroRange           = getInt(element(npcNode, "AggroRange", Integer.toString(NPC_AGGRO_RANGE)));
//            int npcMagicalAttackSpeed   = getInt(element(npcNode, "MagicalAttackSpeed", Integer.toString(NPC_MAGICAL_ATTACK_SPEED)));
//            int npcRightHandWeaponID    = getInt(element(npcNode, "RightHandWeaponID", Integer.toString(NPC_RIGHT_HAND_WEAPON_ID)));
//            int npcLeftHandWeaponID     = getInt(element(npcNode, "LeftHandWeaponID", Integer.toString(NPC_LEFT_HAND_WEAPON_ID)));
//            int npcArmorID              = getInt(element(npcNode, "ArmorID", Integer.toString(NPC_ARMOR_ID)));
//            int npcWalkSpeed            = getInt(element(npcNode, "WalkSpeed", Integer.toString(NPC_WALK_SPEED)));
//            int npcRunSpeed             = getInt(element(npcNode, "RunSpeed", Integer.toString(NPC_RUN_SPEED)));
            
//            bridge.addNpcData(
//                    npcID, npcName, npcGroup, npcCollisionRadius, npcCollisionHeight,
//                    npcLevel, npcSex, npcClassType, npcAttackRange, npcHitPoints,
//                    npcMagicPoints, npcExperience, npcSP, npcPhysicalAttack, npcPhysicalDefense,
//                    npcMagicalAttack, npcMagicalDefense, npcPhysicalAttackSpeed, npcAggroRange, npcMagicalAttackSpeed,
//                    npcRightHandWeaponID, npcLeftHandWeaponID, npcArmorID, npcWalkSpeed, npcRunSpeed
//                    );
        } 
        catch (Exception e)
        {
            System.err.println("ERROR(parseNpcStatData):" + e.getMessage());
            return;
        }
        
        //if (Config.DEBUG) System.out.println("SUCCESS");
    }
    
    public class PetData
    {
        public int petID;
        public int levelStart;
        public int levelEnd;
        HashMap<String, String> statValues;
        public PetData()
        {
            statValues = new HashMap<String, String>();
        }
    }

    private void parsePetData(Node petNode, BSFManager context)
    {
        //if (Config.DEBUG) System.out.println("Parsing PetData.");

        PetData petData = new PetData();
        
        try
        {
            petData.petID       = getInt(attribute(petNode, "ID"));
            int[] levelRange    = IntList.parse(attribute(petNode, "Levels"));
            petData.levelStart  = levelRange[0];
            petData.levelEnd    = levelRange[1];
            
            for (Node node = petNode.getFirstChild(); node != null; node = node.getNextSibling())
            {
                if (isNodeName(node, "Stat"))
                {
                    parseStat(node, petData);
                }
            }
            bridge.addPetData(context, petData.petID, petData.levelStart, petData.levelEnd, petData.statValues);
        } 
        catch (Exception e)
        {
            petData.petID = -1;
            System.err.println("ERROR(parsePetData):" + e.getMessage());
        }
    }

    private void parseStat(Node stat, PetData petData)
    {
        //if (Config.DEBUG) System.out.println("Parsing Pet Statistic.");

        try
        {
            String statName     = attribute(stat, "Name");
            
            for (Node node = stat.getFirstChild(); node != null; node = node.getNextSibling())
            {
                if (isNodeName(node, "Formula"))
                {
                    String formula = parseForumla(node, petData);
                    petData.statValues.put(statName, formula);
                }
            }
        } 
        catch (Exception e)
        {
            petData.petID = -1;
            System.err.println("ERROR(parseStat):" + e.getMessage());
        }
    }
    
    private String parseForumla(Node formulaNode, PetData petData)
    {
        return formulaNode.getTextContent().trim();
    }

    static class FaenorWorldDataParserFactory extends ParserFactory
    {
        public Parser create()
        {
            return(new FaenorWorldDataParser());
        }
    }

    static
    {
        ScriptEngine.parserFactories.put(getParserName("WorldData"), new FaenorWorldDataParserFactory());
    }
}
