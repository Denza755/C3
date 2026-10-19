/**
 * 
 */
package net.sf.l2j.gameserver.model;

/**
 * @author zabbix
 *
 */
public class SeedProduction
{
   int seedId;
   int canProduce;
   int price;
   
   public SeedProduction(int id,int amount, int pri)
   {
       seedId = id;
       canProduce = amount;
       price = pri;
   }
   
   public int getSeedId(){return seedId;}
   public int getCanProduce(){return canProduce;}
   //For edit the seed data
   public void setCanProduce(int amount){ canProduce = amount;}
   public void setSeedId(int ID){ ID = seedId;}
   public void setPrice(int pri){ price = pri;}
   public int getPrice(){return price;}
}
