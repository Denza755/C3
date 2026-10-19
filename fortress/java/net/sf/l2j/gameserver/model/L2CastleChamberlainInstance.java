package net.sf.l2j.gameserver.model;

import java.util.StringTokenizer;

import net.sf.l2j.gameserver.GameServer;
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

	private static int Cond_All_False = 0;
	private static int Cond_Busy_Because_Of_Siege = 1;
	private static int Cond_Owner = 2;

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
	        command = st.nextToken(); // Get actual command

	        String val = "";
	        if (st.countTokens() >= 1) {val = st.nextToken();}
	 
			if (command.equalsIgnoreCase("banish_foreigner"))
			    getCastle().banishForeigner();														// Move non-clan members off castle area
			else if (command.equalsIgnoreCase("list_siege_clans"))
			    getCastle().getSiege().listRegisterClan(player);									// List current register clan
			else if (command.equalsIgnoreCase("manage_siege_defender"))
			{
	            if (val != "")
	                getCastle().getSiege().removeSiegeClan(Integer.parseInt(val));

		    	StringBuffer msg = new StringBuffer("<html><body>");
		    	msg.append("%npcname%:<br>");
		    	msg.append("Manage Siege Defender<br>");
		    	msg.append("<table width=200>");
		    	msg.append("<tr>");
		    	msg.append("<td width=40>Action</td>");
		    	msg.append("<td width=10></td>");
                msg.append("<td>Clan Name</td>");
		    	msg.append("</tr>");
	            for (int i = 0; i < getCastle().getSiege().getDefenderClans().size(); i++)
	            {
			    	msg.append("<tr>");
			    	msg.append("<td><a action=\"bypass -h npc_%objectId%_manage_siege_defender " + getCastle().getSiege().getDefenderClan(i).getClanId() + "\">Decline</a></td>");
			    	msg.append("<td>" + (i + 1) + ")</td>");
	                msg.append("<td>" + (new L2Clan(getCastle().getSiege().getDefenderClan(i).getClanId()).getName()) + "</td>");
			    	msg.append("</tr>");
	            }
		    	msg.append("</table>");
		        msg.append("</body></html>");

		        this.sendHtmlMessage(player, msg.toString());
			}
			else if(command.equalsIgnoreCase("manage_vault"))
			{
	            if (val.equalsIgnoreCase("deposit"))
	                showVaultWindowDeposit(player);
	            else if (val.equalsIgnoreCase("withdraw"))
	                showVaultWindowWithdraw(player);
	            else
	            {
			    	StringBuffer msg = new StringBuffer("<html><body>");
			    	msg.append("%npcname%:<br>");
			    	msg.append("Manage Vault<br>");
			    	msg.append("<table width=200>");
			    	msg.append("<tr><td><a action=\"bypass -h npc_%objectId%_manage_vault deposit\">Deposit Item</a></td></tr>");
			    	msg.append("<tr><td><a action=\"bypass -h npc_%objectId%_manage_vault withdraw\">Withdraw Item</a></td></tr>");
			    	msg.append("</table>");
			        msg.append("</body></html>");

			        this.sendHtmlMessage(player, msg.toString());
	            }
			}
	        else if(command.equalsIgnoreCase("manor")) // manor control
	        {
	            /*
	            StringTokenizer st = new StringTokenizer(command);
	            int val = Integer.parseInt(st.nextToken().substring(6));
	            switch(val)
	            {
	                // 2be uncommented with manor system commit
	                case 1: // view/edit manor stats
	                    //showChatWindow(player,L2Manor.getInstance().getCropReward(_castle));
	                    NpcHtmlMessage msg = new NpcHtmlMessage(1);
	                    msg.setHtml(L2World.getCastle(_castle).getRewards().replace("%objectId%",String.valueOf(getObjectId())));
	                    player.sendPacket(msg);
	                    player.sendPacket(new ActionFailed());
	                    break;
	                case 2: // set reward type
	                    int cropId = Integer.parseInt(st.nextToken());
	                    int reward = Integer.parseInt(st.nextToken());
	                    L2World.getCastle(_castle).setRewardType(cropId,reward);
	                    break;
	                case 3: // edit reward
	                    int crop = Integer.parseInt(st.nextToken());
	                    int currentReward = Integer.parseInt(st.nextToken());
	                    msg = new NpcHtmlMessage(1);
	                    msg.setHtml(pageEditManor(crop,currentReward));
	                    player.sendPacket(msg);
	                    player.sendPacket(new ActionFailed());
	                    break;
	                default:
	                    _log.info("Invalid bypass for manor control: "+command+" by "+player.getName()+", hack?");
	            }
	            */
	        }
	        else if(command.equalsIgnoreCase("operate_door")) // door control
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
	        }
	        else if(command.equalsIgnoreCase("tax_set")) // tax rates control
	        {
	            if (val != "")
	                getCastle().setTaxPercent(player, Integer.parseInt(val));

	        	StringBuffer msg = new StringBuffer("<html><body>");
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
	        }
			else
				super.onBypassFeedback(player, command);
		}
    }

    @SuppressWarnings("static-access")
	public void onAction(L2PcInstance player)
	{
		if(GameServer.events.onAction(player, this))
			return;

		player.sendPacket( new ActionFailed() );
		player.setTarget(this);
		player.sendPacket(new MyTargetSelected(getObjectId(), -15));
		showMessageWindow(player);
	}
    
    @SuppressWarnings("unused")
    private String pageEditManor(int crop, int current)
    {
        String text = "<html><body><table width=270 bgcolor=\"111111\">";
        text += "<tr><td>Crop</td><td>Type I reward</td><td>Type II reward</td></tr>";
        text += "</table><table width=270>";
        /* 2be uncommented with manor system commit
        String cropName = ItemTable.getInstance().getTemplate(crop).getName();
        String reward1Name = ItemTable.getInstance().getTemplate(L2Manor.getInstance().getRewardItem(crop,1)).getName();
        String reward2Name = ItemTable.getInstance().getTemplate(L2Manor.getInstance().getRewardItem(crop,2)).getName();
        int reward1Amount = L2Manor.getInstance().getRewardAmount(crop,1);
        int reward2Amount = L2Manor.getInstance().getRewardAmount(crop,2);
        
        text += "<tr><td><font color=\"LEVEL\">"+cropName+"</font></td><td><a action=\"bypass -h npc_"+getObjectId()+"_manor_2 "+crop+" 1\">"+reward1Name+"/"+reward1Amount+"</a></td><td><a action=\"bypass -h npc_"+getObjectId()+"_manor_2 "+crop+" 2\">"+reward2Name+"/"+reward2Amount+"</a></td></tr>";
        */
        text += "</table></body></html>";
        return text;
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
        player.setUsingClanWarehouse(true);
        player.setUsingFreight(false);
        player.sendPacket(new WareHouseDepositList(player, player.getClan()));
	}

	private void showVaultWindowWithdraw(L2PcInstance player)
	{
		player.sendPacket(new ActionFailed());
        player.setUsingClanWarehouse(true);
        player.setUsingFreight(false);
        player.sendPacket(new WareHouseWithdrawalList(player, player.getClan()));
	}
	
	private int validateCondition(L2PcInstance player)
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
