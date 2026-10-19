package net.sf.l2j.gameserver.model.actor.instance;

import java.util.List;
import java.util.Calendar;
import java.util.StringTokenizer;

import javolution.lang.TextBuilder;
import javolution.util.FastList;

import net.sf.l2j.gameserver.ItemTable;
import net.sf.l2j.gameserver.ClanTable;
import net.sf.l2j.gameserver.SevenSigns;
import net.sf.l2j.gameserver.model.CropProcure;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Manor;
import net.sf.l2j.gameserver.model.SeedProduction;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.WareHouseDepositList;
import net.sf.l2j.gameserver.serverpackets.WareHouseWithdrawalList;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * Castle Chamberlains implementation
 * used for:
 * - tax rate control
 * - regional manor system control
 * - castle treasure control
 * - ...
 */
public class L2CastleChamberlainInstance extends L2FolkInstance
{
    //private static Logger _log = Logger.getLogger(L2CastleChamberlainInstance.class.getName());

	protected static int Cond_All_False = 0;
	protected static int Cond_Busy_Because_Of_Siege = 1;
	protected static int Cond_Owner = 2;

    public L2CastleChamberlainInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }
    
    public void onBypassFeedback(L2PcInstance player, String command)
    {
		player.sendPacket( new ActionFailed() );

		int condition = validateCondition(player);
		if (condition <= Cond_All_False)
            return;

		if (condition == Cond_Busy_Because_Of_Siege)
            return;
		else if (condition == Cond_Owner)
		{
	        StringTokenizer st = new StringTokenizer(command, " ");
            String actualCommand = st.nextToken(); // Get actual command

	        String val = "";
	        if (st.countTokens() >= 1) {val = st.nextToken();}
	 
			if (actualCommand.equalsIgnoreCase("banish_foreigner"))
      {
                getCastle().banishForeigner();                                                      // Move non-clan members off castle area
                return;
      }
			else if (actualCommand.equalsIgnoreCase("list_siege_clans"))
      {
                getCastle().getSiege().listRegisterClan(player);                                    // List current register clan
                return;
      }
      else if (actualCommand.equalsIgnoreCase("receive_report"))
      {
                L2Clan clan = ClanTable.getInstance().getClan(getCastle().getOwnerId());
                if (clan == null)
                    return;

                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                html.setFile("data/html/chamberlain/chamberlain-report.htm");
                html.replace("%objectId%", String.valueOf(getObjectId()));
                html.replace("%clanname%", clan.getName());
                html.replace("%clanleadername%", clan.getLeaderName());
                html.replace("%castlename%", getCastle().getName());
                {
                    int currentPeriod = SevenSigns.getInstance().getCurrentPeriod();
                    switch (currentPeriod)
                    {
                        case SevenSigns.PERIOD_COMP_RECRUITING:
                            html.replace("%ss_event%", "Quest Event Initialization");
                            break;
                        case SevenSigns.PERIOD_COMPETITION:
                            html.replace("%ss_event%", "Competition (Quest Event)");
                            break;
                        case SevenSigns.PERIOD_COMP_RESULTS:
                            html.replace("%ss_event%", "Quest Event Results");
                            break;
                        case SevenSigns.PERIOD_SEAL_VALIDATION:
                            html.replace("%ss_event%", "Seal Validation");
                            break;
                    }
                }
                {
                    int sealOwner1 = SevenSigns.getInstance().getSealOwner(1);
                    switch (sealOwner1)
                    {
                        case SevenSigns.CABAL_NULL:
                            html.replace("%ss_avarice%", "Not in Possession");
                            break;
                        case SevenSigns.CABAL_DAWN:
                            html.replace("%ss_avarice%", "Lords of Dawn");
                            break;
                        case SevenSigns.CABAL_DUSK:
                            html.replace("%ss_avarice%", "Revolutionaries of Dusk");
                            break;
                    }
                }
                {
                    int sealOwner2 = SevenSigns.getInstance().getSealOwner(2);
                    switch (sealOwner2)
                    {
                        case SevenSigns.CABAL_NULL:
                            html.replace("%ss_gnosis%", "Not in Possession");
                            break;
                        case SevenSigns.CABAL_DAWN:
                            html.replace("%ss_gnosis%", "Lords of Dawn");
                            break;
                        case SevenSigns.CABAL_DUSK:
                            html.replace("%ss_gnosis%", "Revolutionaries of Dusk");
                            break;
                    }
                }
                {
                    int sealOwner3 = SevenSigns.getInstance().getSealOwner(3);
                    switch (sealOwner3)
                    {
                        case SevenSigns.CABAL_NULL:
                            html.replace("%ss_strife%", "Not in Possession");
                            break;
                        case SevenSigns.CABAL_DAWN:
                            html.replace("%ss_strife%", "Lords of Dawn");
                            break;
                        case SevenSigns.CABAL_DUSK:
                            html.replace("%ss_strife%", "Revolutionaries of Dusk");
                            break;
                    }
                }
                player.sendPacket(html);
                return;
      }
			else if (actualCommand.equalsIgnoreCase("manage_siege_defender"))
			{
                getCastle().getSiege().listRegisterClan(player);
                return;
			}
			
			else if(actualCommand.equalsIgnoreCase("manage_vault"))
			{
	            if (val.equalsIgnoreCase("deposit"))
                    showVaultWindowDeposit(player);
	            else if (val.equalsIgnoreCase("withdraw"))
                    showVaultWindowWithdraw(player);
	            else
	            {
                    TextBuilder msg = new TextBuilder("<html><body>");
			    	msg.append("%npcname%:<br>");
			    	msg.append("Manage Vault<br>");
			    	msg.append("<table width=200>");
			    	msg.append("<tr><td><a action=\"bypass -h npc_%objectId%_manage_vault deposit\">Deposit Item</a></td></tr>");
			    	msg.append("<tr><td><a action=\"bypass -h npc_%objectId%_manage_vault withdraw\">Withdraw Item</a></td></tr>");
			    	msg.append("</table>");
			        msg.append("</body></html>");

			        this.sendHtmlMessage(player, msg.toString());
	            }
                return;
			    }
	        else if(actualCommand.equalsIgnoreCase("manor")) // manor control
            {
                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                int cmd = Integer.parseInt(val);
                switch(cmd)
                {
                    //TODO uncomment this with manor system commit
                    case 1: // view/edit manor stats
                       // showChatWindow(player,L2Manor.getInstance().getCropReward());
                        showManorProcure(player);
                        break;
                    case 2: // set reward type
               
                if( !player.isGM() && ( hour < 20 || hour > 22) ) break;
                        int cropId = Integer.parseInt(st.nextToken());
                        int reward = Integer.parseInt(st.nextToken());
                        getCastle().setCropReward(cropId,reward);
                        showManorProcure(player);
                        break;
                    case 3: // edit reward
                        int crop = Integer.parseInt(st.nextToken());
                        int currentReward = Integer.parseInt(st.nextToken());
                        NpcHtmlMessage msg = new NpcHtmlMessage(1);
                        msg.setHtml(pageEditManor(crop,currentReward));
                        player.sendPacket(msg);
                        player.sendPacket(new ActionFailed());
                        break;
                   case 4: // show sedds stats
                        showManorProduction(player);
                        break;
                    case 5: // set count and prise seed
                        if( !player.isGM() && ( hour < 20 || hour > 22) ) break;
                        int seedeId = Integer.parseInt(st.nextToken());
                        int amounte = Integer.parseInt(st.nextToken());
                        int pricee = Integer.parseInt(st.nextToken());
                        if (amounte >1000) amounte = 1000;
                        if (pricee > 3000 ) pricee= 3000;
                          getCastle().setSeedAmount(seedeId,amounte);
                          getCastle().setSeedPrice(seedeId,pricee);
                          showManorProduction(player);
                        break;
                     case 6: // edit reward
                        int seedId = Integer.parseInt(st.nextToken());
                        int amount = Integer.parseInt(st.nextToken());
                        int price = Integer.parseInt(st.nextToken());
                        NpcHtmlMessage msg1 = new NpcHtmlMessage(1);
                        msg1.setHtml(pageEditManorSeed(seedId,amount,price));
                        player.sendPacket(msg1);
                        player.sendPacket(new ActionFailed());
                        break;
                     case 7: // edit reward
                        if( !player.isGM() && ( hour < 20 || hour > 22) ) break;
                        int cropIdi = Integer.parseInt(st.nextToken());
                        int amounti = Integer.parseInt(st.nextToken());
                        if (amounti > 10000) amounti=10000;
                           getCastle().setCropAmount(cropIdi,amounti);
                           showManorProcure(player);
                        break;
                     case 8: // View Descriptions
                        String filename = "data/html/chamberlain/manor/manor_help00"+st.nextToken()+".htm";
                        
                        if (filename.length() != 0)
                        {
                          NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                          html.setFile(filename);
                          html.replace("%objectId%", String.valueOf(getObjectId()));
                          html.replace("%npcname%", getName());
                          player.sendPacket(html);
                        }
                        break;
                    default:
                        _log.info("Invalid bypass for manor control: "+command+" by "+player.getName()+", hack?");
               }
               return;
          }
	        else if(actualCommand.equalsIgnoreCase("operate_door")) // door control
	        {
	            if (val != "")
	            {
		            boolean open = (Integer.parseInt(val) == 1);
		            while (st.hasMoreTokens())
		           	{
		           	    getCastle().openCloseDoor(player, Integer.parseInt(st.nextToken()), open);
		           	}
	            }

	            NpcHtmlMessage html = new NpcHtmlMessage(1);
	    		html.setFile("data/html/chamberlain/" + getTemplate().npcId + "-d.htm");
	    		html.replace("%objectId%", String.valueOf(getObjectId()));
	    		html.replace("%npcname%", getName());
	    		player.sendPacket(html);
                return;
	        }
	        else if(actualCommand.equalsIgnoreCase("tax_set")) // tax rates control
	        {
	            if (val != "")
	                getCastle().setTaxPercent(player, Integer.parseInt(val));

                TextBuilder msg = new TextBuilder("<html><body>");
	        	msg.append(getName() + ":<br>");
	        	msg.append("Current tax rate: " + getCastle().getTaxPercent() + "%<br>");
	        	msg.append("<table>");
	        	msg.append("<tr>");
	        	msg.append("<td>Change tax rate to:</td>");
	        	msg.append("<td><edit var=\"value\" width=40><br>");
	        	msg.append("<button value=\"Adjust\" action=\"bypass -h npc_%objectId%_tax_set $value\" width=80 height=15></td>");
	        	msg.append("</tr>");
	        	msg.append("</table>");
	        	msg.append("</center>");
	        	msg.append("</body></html>");

		        this.sendHtmlMessage(player, msg.toString());
                return;
	        }
		}

        super.onBypassFeedback(player, command);
    }

    public void onAction(L2PcInstance player)
	  {
       player.sendPacket(new ActionFailed());
		   player.setTarget(this);
		   player.sendPacket(new MyTargetSelected(getObjectId(), -15));

       if (isInsideRadius(player, INTERACTION_DISTANCE, false, false))
         showMessageWindow(player);
	  }
    
    @SuppressWarnings("unused")
    private String pageEditManorSeed(int seed, int amount, int price)
    {
/*TextBuilder msg = new TextBuilder("<html><body>");
                msg.append(getName() + ":<br>");
                msg.append("Current tax rate: " + getCastle().getTaxPercent() + "%<br>");
                msg.append("<table>");
                msg.append("<tr>");
                msg.append("<td>Change tax rate to:</td>");
                msg.append("<td><edit var=\"value\" width=40><br>");
                msg.append("<button value=\"Adjust\" action=\"bypass -h npc_%objectId%_tax_set $value\" width=80 height=15></td>");
                msg.append("</tr>");
                msg.append("</table>");
                msg.append("</center>");
                msg.append("</body></html>");
*/
        String text = "<html><body><table width=270 bgcolor=\"111111\">";
        String seedName = ItemTable.getInstance().getTemplate(seed).getName();
        text += "<tr><td>"+seedName+"</td></tr>";

        text += "<tr><td>Current amount: "+amount+"</td></tr>";
        text += "<tr><td>Current price: "+price+"</td></tr>";
        text += "</table><table width=270>";
        /* 2be uncommented with manor system commit*/

        text += "<tr><td>Amount: <edit var=\"value\" width=100></td></tr>";
        text += "<tr><td>Price: <edit var=\"value1\" width=100></td></tr>";
        text += "<tr><td><button value=\"Save\" action=\"bypass -h npc_"+getObjectId()+"_manor 5 "+seed+" $value $value1\" width=80 height=15></td></tr>";  
        text += "</table></body></html>";
        return text;
    }

    private String pageEditManor(int crop, int current)
    { 
        String cropName = ItemTable.getInstance().getTemplate(crop).getName();
        String text = "<html><body><table width=270 bgcolor=\"111111\"><tr><td></td><td>"+cropName+"</td><td></td></tr>";
        text += "<tr><td>Kol-vo na pokupku</td><td>Type I reward</td><td>Type II reward</td></tr>";
        text += "</table><table width=270>";
        /* 2be uncommented with manor system commit*/
       
        String reward1Name = ItemTable.getInstance().getTemplate(L2Manor.getInstance().getRewardItem(crop,1)).getName();
        String reward2Name = ItemTable.getInstance().getTemplate(L2Manor.getInstance().getRewardItem(crop,2)).getName();
        int reward1Amount = L2Manor.getInstance().getRewardAmount(crop,1);
        int reward2Amount = L2Manor.getInstance().getRewardAmount(crop,2);
        
        text += "<tr><td><edit var=\"value2\" width=100></td><td><a action=\"bypass -h npc_"+getObjectId()+"_manor 2 "+crop+" 1\">"+reward1Name+"/"+reward1Amount+"</a></td><td><a action=\"bypass -h npc_"+getObjectId()+"_manor 2 "+crop+" 2\">"+reward2Name+"/"+reward2Amount+"</a></td></tr><tr><td><button value=\"Save kol-vo\" action=\"bypass -h npc_"+getObjectId()+"_manor 7 "+crop+" $value2 \" width=100 height=15></td><td></td><td></td></tr>";
       
        text += "</table></body></html>";
        return text;
    }

    private void showManorProduction(L2PcInstance player)
    {
        L2Clan clan = ClanTable.getInstance().getClan(getCastle().getOwnerId());
        if (clan == null)
            return;
    
        if (getCastle() == null)
            return;

        FastList<SeedProduction> seedes = getCastle().getSeedProduction();
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        String r="";

        for(SeedProduction seede : seedes)
        {
    /*if(seede.getCanProduce() > 0 ){*/
                r += "<tr><td><a action=\"bypass -h npc_"+getObjectId()+"_manor 6 "+seede.getSeedId()+" "+seede.getCanProduce()+" "+seede.getPrice() +" \">"; 
        r += ItemTable.getInstance().getTemplate(seede.getSeedId()).getName() + "</a></td>";
                r += "<td>" + seede.getCanProduce() + "</td>";
                r += "<td>" + seede.getPrice() + "</td></tr>";
    /*}*/
        }
        
        html.setFile("data/html/chamberlain/chamberlain-manor-production.htm");
        html.replace("%objectId%", String.valueOf(getObjectId()));
        html.replace("%npcname%", getName());
        html.replace("%clanleadername%", clan.getLeaderName());
        html.replace("%clanname%", clan.getName());
        html.replace("%castlename%", getCastle().getName());
        html.replace("%table%", r);
        player.sendPacket(html);
    }    

    private void showManorProcure(L2PcInstance player)
    {
        L2Clan clan = ClanTable.getInstance().getClan(getCastle().getOwnerId());
        if (clan == null)
            return;
    
        if (getCastle() == null)
            return;

        FastList<CropProcure> crops = getCastle().getManorRewards();
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        String r="";

        for(CropProcure crop : crops)
        {
            r += "<tr><td><a action=\"bypass -h npc_"+getObjectId()+"_manor 3 "+crop.getId()+" "+crop.getReward()+"  \">"+ ItemTable.getInstance().getTemplate(crop.getId()).getName() + "</a></td>";
            r += "<td>" + crop.getAmount() + "</td>";
            r += "<td>" + crop.getReward() + "</td></tr>";
        }
        
        html.setFile("data/html/chamberlain/chamberlain-manor-procure.htm");
        html.replace("%objectId%", String.valueOf(getObjectId()));
        html.replace("%npcname%", getName());
        html.replace("%clanleadername%", clan.getLeaderName());
        html.replace("%clanname%", clan.getName());
        html.replace("%castlename%", getCastle().getName());
        html.replace("%table%", r);
        player.sendPacket(html);
    }

    private void sendHtmlMessage(L2PcInstance player, String htmlMessage)
    {
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setHtml(htmlMessage);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
        player.sendPacket(html);
    }
    
    private void showMessageWindow(L2PcInstance player)
	{
		player.sendPacket( new ActionFailed() );
		String filename = "data/html/chamberlain/chamberlain-no.htm";
		
		int condition = validateCondition(player);
		if (condition > Cond_All_False)
		{
	        if (condition == Cond_Busy_Because_Of_Siege)
	            filename = "data/html/chamberlain/chamberlain-busy.htm";					// Busy because of siege
	        else if (condition == Cond_Owner)												// Clan owns castle
	            filename = "data/html/chamberlain/chamberlain.htm";							// Owner message window
		}
		
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}

	private void showVaultWindowDeposit(L2PcInstance player)
	{
		player.sendPacket(new ActionFailed());
		player.setActiveWarehouse(player.getClan().getWarehouse());
    player.sendPacket(new WareHouseDepositList(player)); //Or Castle ??
	}

	private void showVaultWindowWithdraw(L2PcInstance player)
	{
		player.sendPacket(new ActionFailed());
		player.setActiveWarehouse(player.getClan().getWarehouse());
        player.sendPacket(new WareHouseWithdrawalList(player)); //Or Castle ??
	}
	
	protected int validateCondition(L2PcInstance player)
	{
		if (getCastle() != null && getCastle().getCastleId() > 0)
		{
		    if (player.getClan() != null)
		    {
		        if (getCastle().getSiege().getIsInProgress())
		            return Cond_Busy_Because_Of_Siege;										// Busy because of siege
		        else if (getCastle().getOwnerId() == player.getClanId()						// Clan owns castle
		                && player.isClanLeader())			                                // Leader of clan
		            return Cond_Owner;	// Owner
		    }
		}
		
		return Cond_All_False;
	}
}
