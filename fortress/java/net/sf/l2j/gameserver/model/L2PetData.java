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

public class L2PetData
{
    public static final String PET_MAX_FEED     = "MaxFeed";
    public static final String PET_FEED_BATTLE  = "FeedBattle";
    public static final String PET_FEED_NORMAL  = "FeedNormal";
    public static final String PET_PATK         = "PAtk";
    public static final String PET_PDEF         = "PDef";
    public static final String PET_MATK         = "MAtk";
    public static final String PET_MDEF         = "MDef";
    public static final String PET_HP           = "HP";
    public static final String PET_MP           = "MP";
    public static final String PET_REGEN_HP     = "RegenHP";
    public static final String PET_REGEN_MP     = "RegenMP";
    public static final String PET_EXP          = "Exp";
    
    private int petID;
    private int petLevel;
    private int petMaxFeed;
    private int petFeedBattle;
    private int petFeedNormal;
    private int petPAtk;
    private int petPDef;
    private int petMAtk;
    private int petMDef;
    private int petHP;
    private int petMP;
    private int petRegenHP;
    private int petRegenMP;
    private int petExp;
    
    public void setStat(String stat, int value)
    {
        if (stat.equalsIgnoreCase(PET_MAX_FEED))
        {
            this.setPetMaxFeed(value);
        }
        else if (stat.equalsIgnoreCase(PET_FEED_BATTLE))
        {
            this.setPetFeedBattle(value);
        }
        else if (stat.equalsIgnoreCase(PET_FEED_NORMAL))
        {
            this.setPetFeedNormal(value);
        }
        else if (stat.equalsIgnoreCase(PET_PATK))
        {
            this.setPetPAtk(value);
        }
        else if (stat.equalsIgnoreCase(PET_PDEF))
        {
            this.setPetPDef(value);
        }
        else if (stat.equalsIgnoreCase(PET_MATK))
        {
            this.setPetMAtk(value);
        }
        else if (stat.equalsIgnoreCase(PET_MDEF))
        {
            this.setPetMDef(value);
        }
        else if (stat.equalsIgnoreCase(PET_HP))
        {
            this.setPetHP(value);
        }
        else if (stat.equalsIgnoreCase(PET_MP))
        {
            this.setPetMP(value);
        }
        else if (stat.equalsIgnoreCase(PET_REGEN_HP))
        {
            this.setPetRegenHP(value);
        }
        else if (stat.equalsIgnoreCase(PET_REGEN_MP))
        {
            this.setPetRegenMP(value);
        }
        else if (stat.equalsIgnoreCase(PET_EXP))
        {
            this.setPetExp(value);
        }
    }
    
    public int getPetFeedBattle()
    {
        return petFeedBattle;
    }
    
    public void setPetFeedBattle(int petFeedBattle)
    {
        this.petFeedBattle = petFeedBattle;
    }
    
    public int getPetFeedNormal()
    {
        return petFeedNormal;
    }
    
    public void setPetFeedNormal(int petFeedNormal)
    {
        this.petFeedNormal = petFeedNormal;
    }
    
    public int getPetHP()
    {
        return petHP;
    }
    
    public void setPetHP(int petHP)
    {
        this.petHP = petHP;
    }
    
    public int getPetID()
    {
        return petID;
    }
    
    public void setPetID(int petID)
    {
        this.petID = petID;
    }
    
    public int getPetLevel()
    {
        return petLevel;
    }
    
    public void setPetLevel(int petLevel)
    {
        this.petLevel = petLevel;
    }
    
    public int getPetMAtk()
    {
        return petMAtk;
    }
    
    public void setPetMAtk(int petMAtk)
    {
        this.petMAtk = petMAtk;
    }
    
    public int getPetMaxFeed()
    {
        return petMaxFeed;
    }
    
    public void setPetMaxFeed(int petMaxFeed)
    {
        this.petMaxFeed = petMaxFeed;
    }
    
    public int getPetMDef()
    {
        return petMDef;
    }
    
    public void setPetMDef(int petMDef)
    {
        this.petMDef = petMDef;
    }
    
    public int getPetExp()
    {
        return petExp;
    }
    
    public void setPetExp(int petExp)
    {
        this.petExp = petExp;
    }
    
    public int getPetMP()
    {
        return petMP;
    }
    
    public void setPetMP(int petMP)
    {
        this.petMP = petMP;
    }
    
    public int getPetPAtk()
    {
        return petPAtk;
    }
    
    public void setPetPAtk(int petPAtk)
    {
        this.petPAtk = petPAtk;
    }
    
    public int getPetPDef()
    {
        return petPDef;
    }
    
    public void setPetPDef(int petPDef)
    {
        this.petPDef = petPDef;
    }
    
    public int getPetRegenHP()
    {
        return petRegenHP;
    }
    
    public void setPetRegenHP(int petRegenHP)
    {
        this.petRegenHP = petRegenHP;
    }
    
    public int getPetRegenMP()
    {
        return petRegenMP;
    }
    
    public void setPetRegenMP(int petRegenMP)
    {
        this.petRegenMP = petRegenMP;
    }
    
    public String toString()
    {
        return "PetID: " + getPetID() + "\t" +
            "PetLevel: " + getPetLevel() + "\t" +
            PET_MAX_FEED + ": " + getPetMaxFeed() + "\t" +
            PET_FEED_BATTLE + ": " + getPetFeedBattle() + "\t" +
            PET_FEED_NORMAL + ": " + getPetFeedNormal() + "\t" +
            PET_PATK + ": " + getPetPAtk() + "\t" +
            PET_PDEF + ": " + getPetPDef() + "\t" +
            PET_MATK + ": " + getPetMAtk() + "\t" +
            PET_MDEF + ": " + getPetMDef() + "\t" +
            PET_HP + ": " + getPetHP() + "\t" +
            PET_MP + ": " + getPetMP() + "\t" +
            PET_REGEN_HP + ": " + getPetRegenHP() + "\t" +
            PET_REGEN_MP + ": " + getPetRegenMP() + "\t" +
            PET_EXP + ": " + getPetExp();
    }
    
}
