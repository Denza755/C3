package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.NpcTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.MobGroup;
import net.sf.l2j.gameserver.model.MobGroupTable;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * @author littlecrow
 * Admin commands handler for controllable mobs
 */
public class AdminMobGroup implements IAdminCommandHandler {

	private static String[] _adminCommands = { "admin_mobmenu","admin_mobgroup_create",
			"admin_mobgroup_spawn", "admin_mobgroup_unspawn",
			"admin_mobgroup_kill", "admin_mobgroup_idle",
			"admin_mobgroup_attack", "admin_mobgroup_rnd",
			"admin_mobgroup_return", "admin_mobgroup_follow",
			"admin_mobgroup_casting", "admin_mobgroup_nomove" ,
			"admin_mobgroup_attackgrp", "admin_mobgroup_invul", "admin_mobinst"};

	private static final int REQUIRED_LEVEL = Config.GM_MIN;

	public boolean useAdminCommand(String command, L2PcInstance activeChar) {
		if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM()))
			return false;

		if (command.equals("admin_mobmenu")) showMobPage(activeChar);
		else if (command.equals("admin_mobinst")) showMobInstPage(activeChar);
		else if (command.startsWith("admin_mobgroup_create"))
			createGroup(command, activeChar);
		else if (command.startsWith("admin_mobgroup_spawn"))
			spawnGroup(command, activeChar);
		else if (command.startsWith("admin_mobgroup_unspawn"))
			unspawnGroup(command, activeChar);
		else if (command.startsWith("admin_mobgroup_kill"))
			killGroup(command, activeChar);
		else if (command.startsWith("admin_mobgroup_attackgrp"))
			attackGrp(command, activeChar);
		else if (command.startsWith("admin_mobgroup_attack")) {
			if (activeChar.getTarget() instanceof L2Character) {
				L2Character target = (L2Character) activeChar.getTarget();
				attack(command, activeChar, target);
			}
		}

		else if (command.startsWith("admin_mobgroup_rnd"))
			setNormal(command, activeChar);
		else if (command.startsWith("admin_mobgroup_idle"))
			idle(command, activeChar);
		else if (command.startsWith("admin_mobgroup_return"))
			returnToChar(command, activeChar);
		else if (command.startsWith("admin_mobgroup_follow"))
			follow(command, activeChar, activeChar);
		else if (command.startsWith("admin_mobgroup_casting"))
			setCasting(command, activeChar);
		else if (command.startsWith("admin_mobgroup_nomove"))
			noMove(command, activeChar);
		else if (command.startsWith("admin_mobgroup_invul"))
			invul(command, activeChar);

		return true;
	}

	private void returnToChar(String command, L2PcInstance activeChar) {
		int groupId;
		try {
			StringTokenizer tokenizer = new StringTokenizer(command, " ");
			// Skipping command name
			tokenizer.nextElement();
			groupId = Integer.parseInt(tokenizer.nextToken()); // getting group
		} catch (Exception e) {
			SystemMessage _sm = new SystemMessage(SystemMessage.S1_S2);
			_sm.addString("Bad command arguments");
			activeChar.sendPacket(_sm);
			return;
		}

		// number
		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);
		if (group == null) {
			SystemMessage _sm = new SystemMessage(SystemMessage.S1_S2);
			_sm.addString("Wrong group");
			activeChar.sendPacket(_sm);
			return;
		}
		group.returnToCharacter(activeChar);

	}

	private void idle(String command, L2PcInstance activeChar) {
		int groupId;
		try {
			StringTokenizer tokenizer = new StringTokenizer(command, " ");
			// Skipping command name
			tokenizer.nextElement();
			groupId = Integer.parseInt(tokenizer.nextToken()); // getting group
		} catch (Exception e) {
			SystemMessage _sm = new SystemMessage(SystemMessage.S1_S2);
			_sm.addString("Bad command arguments");
			activeChar.sendPacket(_sm);
			return;
		}

		// number
		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);
		if (group == null) {
			SystemMessage _sm = new SystemMessage(SystemMessage.S1_S2);
			_sm.addString("Wrong group");
			activeChar.sendPacket(_sm);
			return;
		}

		group.idle();

	}

	private void setNormal(String command, L2PcInstance activeChar) {
		int groupId;
		try {
			StringTokenizer tokenizer = new StringTokenizer(command, " ");
			// Skipping command name
			tokenizer.nextElement();
			groupId = Integer.parseInt(tokenizer.nextToken()); // getting group
		} catch (Exception e) {
			SystemMessage _sm = new SystemMessage(SystemMessage.S1_S2);
			_sm.addString("Bad command arguments");
			activeChar.sendPacket(_sm);
			return;
		}

		// number
		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);
		if (group == null) {
			SystemMessage _sm = new SystemMessage(SystemMessage.S1_S2);
			_sm.addString("Wrong group");
			activeChar.sendPacket(_sm);
			return;
		}

		group.randomAttack();

	}

	private void attack(String command, L2PcInstance activeChar, L2Character target) 
	{
		int groupId;
		try 
		{
			StringTokenizer tokenizer = new StringTokenizer(command, " ");
			// Skipping command name
			tokenizer.nextElement();
			groupId = Integer.parseInt(tokenizer.nextToken()); // getting group
		} 
		catch (Exception e) 
		{
			SystemMessage _sm = new SystemMessage(SystemMessage.S1_S2);
			_sm.addString("Bad command arguments");
			activeChar.sendPacket(_sm);
			return;
		}

		// number
		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);
		if (group == null) 
		{
			SystemMessage _sm = new SystemMessage(SystemMessage.S1_S2);
			_sm.addString("Wrong group");
			activeChar.sendPacket(_sm);
			return;
		}

		group.attackTarget(target);

	}

	private void follow(String command, L2PcInstance activeChar,
			L2Character target) {
		int groupId;
		try {
			StringTokenizer tokenizer = new StringTokenizer(command, " ");
			// Skipping command name
			tokenizer.nextElement();
			groupId = Integer.parseInt(tokenizer.nextToken()); // getting group
		} catch (Exception e) {
			SystemMessage _sm = new SystemMessage(SystemMessage.S1_S2);
			_sm.addString("Bad command arguments");
			activeChar.sendPacket(_sm);
			return;
		}

		// number
		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);
		if (group == null) {
			SystemMessage _sm = new SystemMessage(SystemMessage.S1_S2);
			_sm.addString("Wrong group");
			activeChar.sendPacket(_sm);
			return;
		}

		group.follow(target);

	}

	public String[] getAdminCommandList() {
		return _adminCommands;
	}

	private boolean checkLevel(int level) {
		return (level >= REQUIRED_LEVEL);
	}

	private void createGroup(String command, L2PcInstance activeChar) {
		int groupId;
		int templateId;
		int mobCount;

		try {
			StringTokenizer tokenizer = new StringTokenizer(command, " ");

			// Skipping command name
			tokenizer.nextElement();

			groupId = Integer.parseInt(tokenizer.nextToken()); // getting group
			// number
			templateId = Integer.parseInt(tokenizer.nextToken()); // getting
			// template
			// id
			mobCount = Integer.parseInt(tokenizer.nextToken());
		} catch (Exception e) {
			SystemMessage _sm = new SystemMessage(SystemMessage.S1_S2);
			_sm.addString("Bad command arguments");
			activeChar.sendPacket(_sm);
			return;
		}

		L2NpcTemplate template = NpcTable.getInstance().getTemplate(templateId);
		MobGroup group = new MobGroup(template, mobCount);
		MobGroupTable.getInstance().addGroup(groupId, group);
	}

	private void spawnGroup(String command, L2PcInstance activeChar) {
		int groupId;
		boolean topos = false;
		int posx = 0;
		int posy = 0;
		int posz = 0;

		try {
			StringTokenizer tokenizer = new StringTokenizer(command, " ");
			// Skipping command name
			tokenizer.nextElement();

			groupId = Integer.parseInt(tokenizer.nextToken()); // getting group
			// number

			try { // we try to get a position
				posx = Integer.parseInt(tokenizer.nextToken());
				posy = Integer.parseInt(tokenizer.nextToken());
				posz = Integer.parseInt(tokenizer.nextToken());
				topos = true;
			} catch (Exception e) {// no position given
			}

		} catch (Exception e) {
			SystemMessage _sm = new SystemMessage(SystemMessage.S1_S2);
			_sm.addString("Bad command arguments");
			activeChar.sendPacket(_sm);
			return;
		}

		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);
		if (group == null) {
			SystemMessage _sm = new SystemMessage(SystemMessage.S1_S2);
			_sm.addString("Wrong group");
			activeChar.sendPacket(_sm);
			return;
		}

		doAnimation(activeChar);
		if (topos)
			group.spawnIt(posx, posy, posz);
		else
			group.spawnIt(activeChar);

		activeChar.sendMessage("Group spawn completed");
	}

	private void unspawnGroup(String command, L2PcInstance activeChar) {
		int groupId;
		try {
			StringTokenizer tokenizer = new StringTokenizer(command, " ");
			// Skipping command name
			tokenizer.nextElement();

			groupId = Integer.parseInt(tokenizer.nextToken()); // getting group
			// number
		} catch (Exception e) {
			SystemMessage _sm = new SystemMessage(SystemMessage.S1_S2);
			_sm.addString("Bad command arguments");
			activeChar.sendPacket(_sm);
			return;
		}
		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);
		if (group == null) {
			SystemMessage _sm = new SystemMessage(SystemMessage.S1_S2);
			_sm.addString("Wrong group");
			activeChar.sendPacket(_sm);
			return;
		}

		doAnimation(activeChar);
		group.unspawn();
		activeChar.sendMessage("Group unspawn completed");
	}

	private void killGroup(String command, L2PcInstance activeChar) {
		int groupId;
		try {
			StringTokenizer tokenizer = new StringTokenizer(command, " ");
			// Skipping command name
			tokenizer.nextElement();

			groupId = Integer.parseInt(tokenizer.nextToken()); // getting group
			// number
		} catch (Exception e) {
			SystemMessage _sm = new SystemMessage(SystemMessage.S1_S2);
			_sm.addString("Bad command arguments");
			activeChar.sendPacket(_sm);
			return;
		}
		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);
		if (group == null) {
			SystemMessage _sm = new SystemMessage(SystemMessage.S1_S2);
			_sm.addString("Wrong group");
			activeChar.sendPacket(_sm);
			return;
		}

		doAnimation(activeChar);
		group.kill(activeChar);
	}

	private void setCasting(String command, L2PcInstance activeChar) {
		int groupId;
		try {
			StringTokenizer tokenizer = new StringTokenizer(command, " ");
			// Skipping command name
			tokenizer.nextElement();

			groupId = Integer.parseInt(tokenizer.nextToken()); // getting group
			// number
		} catch (Exception e) {
			SystemMessage _sm = new SystemMessage(SystemMessage.S1_S2);
			_sm.addString("Bad command arguments");
			activeChar.sendPacket(_sm);
			return;
		}
		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);
		if (group == null) {
			SystemMessage _sm = new SystemMessage(SystemMessage.S1_S2);
			_sm.addString("Wrong group");
			activeChar.sendPacket(_sm);
			return;
		}

		group.setCastMode();
	}

	private void noMove(String command, L2PcInstance activeChar) {
		int groupId;
		String enabled;
		try {
			StringTokenizer tokenizer = new StringTokenizer(command, " ");
			// Skipping command name
			tokenizer.nextElement();

			groupId = Integer.parseInt(tokenizer.nextToken()); // getting group
			// number

			enabled = tokenizer.nextToken();
		} catch (Exception e) {
			SystemMessage _sm = new SystemMessage(SystemMessage.S1_S2);
			_sm.addString("Bad command arguments");
			activeChar.sendPacket(_sm);
			return;
		}
		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);
		if (group == null) {
			SystemMessage _sm = new SystemMessage(SystemMessage.S1_S2);
			_sm.addString("Wrong group");
			activeChar.sendPacket(_sm);
			return;
		}
		if (enabled.equals("on"))
			group.setNoMoveMode(true);
		else if (enabled.equals("off"))
			group.setNoMoveMode(false);
		else {
			SystemMessage _sm = new SystemMessage(SystemMessage.S1_S2);
			_sm.addString("Bad command arguments");
			activeChar.sendPacket(_sm);
			return;
		}

	}

	private void doAnimation(L2PcInstance activeChar) {

		activeChar.broadcastPacket(new MagicSkillUser(activeChar, 1008, 1, 4000, 0));
		activeChar.sendPacket(new SetupGauge(0, 4000));
	}
	
	private void attackGrp(String command, L2PcInstance activeChar) {
		int groupId;
		int othGroupId;
		try {
			StringTokenizer tokenizer = new StringTokenizer(command, " ");
			// Skipping command name
			tokenizer.nextElement();

			groupId = Integer.parseInt(tokenizer.nextToken()); // getting group
			// number
			othGroupId = Integer.parseInt(tokenizer.nextToken());
			
		} catch (Exception e) {
			SystemMessage _sm = new SystemMessage(SystemMessage.S1_S2);
			_sm.addString("Bad command arguments");
			activeChar.sendPacket(_sm);
			return;
		}
		
		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);
		if (group == null) {
			SystemMessage _sm = new SystemMessage(SystemMessage.S1_S2);
			_sm.addString("Wrong group");
			activeChar.sendPacket(_sm);
			return;
		}
		
		MobGroup othGroup = MobGroupTable.getInstance().getGroup(othGroupId);
		if (othGroup == null) {
			SystemMessage _sm = new SystemMessage(SystemMessage.S1_S2);
			_sm.addString("Wrong target group");
			activeChar.sendPacket(_sm);
			return;
		}
		
		group.setAttackGroup(othGroup);

		
		
	}
	
	private void invul(String command, L2PcInstance activeChar) {
		int groupId;
		String enabled;
		try {
			StringTokenizer tokenizer = new StringTokenizer(command, " ");
			// Skipping command name
			tokenizer.nextElement();

			groupId = Integer.parseInt(tokenizer.nextToken()); // getting group
			// number
			enabled = tokenizer.nextToken();
		} catch (Exception e) {
			SystemMessage _sm = new SystemMessage(SystemMessage.S1_S2);
			_sm.addString("Bad command arguments");
			activeChar.sendPacket(_sm);
			return;
		}
		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);
		if (group == null) {
			SystemMessage _sm = new SystemMessage(SystemMessage.S1_S2);
			_sm.addString("Wrong group");
			activeChar.sendPacket(_sm);
			return;
		}
		if (enabled.equals("on"))
			group.setInvul(true);
		else if (enabled.equals("off"))
			group.setInvul(false);
		else {
			SystemMessage _sm = new SystemMessage(SystemMessage.S1_S2);
			_sm.addString("Bad command arguments");
			activeChar.sendPacket(_sm);
			return;
		}
	}

	public void showMobPage(L2PcInstance activeChar)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

		StringBuffer replyMSG = new StringBuffer("<html><body>");
		replyMSG.append("<center><table width=260><tr><td width=40>");
		replyMSG.append("<button value=\"Main\" action=\"bypass -h admin_admin\" width=45 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("</td><td width=180>");
		replyMSG.append("<center>Mob COntrol Menu</center>");
		replyMSG.append("</td><td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=45 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("</td></tr></table></center><br>");
		replyMSG.append("<center><table width=200><tr><td>");
		replyMSG.append("<button value=\"Create Group\" action=\"bypass -h admin_mobgroup_create $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Spawn Group\" action=\"bypass -h admin_mobgroup_spawn $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">"); 
		replyMSG.append("<button value=\"UnSpawn Group\" action=\"bypass -h admin_mobgroup_unspawn $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<br>");
		replyMSG.append("<button value=\"Idle Group\" action=\"bypass -h admin_mobgroup_idle $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Group Attack\" action=\"bypass -h admin_mobgroup_attack $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Random Attack\" action=\"bypass -h admin_mobgroup_rnd\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Earthquake\" action=\"bypass -h admin_earthquake $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("</td><td>");
		replyMSG.append("<button value=\"Group Return\" action=\"bypass -h admin_mobgroup_return $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Group Follow\" action=\"bypass -h admin_mobgroup_follow $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Kill Group\" action=\"bypass -h admin_mobgroup_kill $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<br>");
		replyMSG.append("<button value=\"Group NoMove\" action=\"bypass -h admin_mobgroup_nomove $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Grp Attck Grp\" action=\"bypass -h admin_mobgroup_attackgrp\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Cast Only\" action=\"bypass -h admin_mobgroup_casting\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Group Invul\" action=\"bypass -h admin_mobgroup_invul\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<br>");
		replyMSG.append("</td></tr></table></center><br>");
		replyMSG.append("<center><table><tr><td>");
		replyMSG.append("<button value=\"Instructions\" action=\"bypass -h admin_mobinst\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("</td></tr></table></center><br>");
        replyMSG.append("<center>Mob Control Variables:</center>");
        replyMSG.append("<center><edit var=\"menu_command\" width=100 height=15></center><br>");
        replyMSG.append("</body></html>");

        adminReply.setHtml(replyMSG.toString());
        activeChar.sendPacket(adminReply); 
	}

	public void showMobInstPage(L2PcInstance activeChar)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

		StringBuffer replyMSG = new StringBuffer("<html><body>");
		replyMSG.append("<center><table width=260><tr><td width=40>");
		replyMSG.append("<button value=\"Main\" action=\"bypass -h admin_admin\" width=45 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("</td><td width=180>");
		replyMSG.append("<center>Control Instructions</center>");
		replyMSG.append("</td><td width=40><button value=\"Back\" action=\"bypass -h admin_mobmenu\" width=45 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("</td></tr></table></center><br>");
		replyMSG.append("//mobgroup_create [groupId] [npcId] [number]<br>");
		replyMSG.append("Create a group with [number] mobs of type [npcId], in the group [groupId]. <br>"); 
		replyMSG.append("This command will have no effect if there is alive monsters in the group. Moreover, there is no visual effect using this command. Mobs have to be spawned next.<br>");
		replyMSG.append("<br>");
		replyMSG.append("//mobgroup_spawn [groupId] [locx] [locy] [locz] :<br>");
		replyMSG.append("Spawn the group created by mobgroup_create. If the location isn't specified, the group will be spawned arround the gm.<br>");
		replyMSG.append("By default, mobs are in state idle, and do absolutly nothing.<br>");
		replyMSG.append("<br>");
		replyMSG.append("//mobgroup_follow [groupId] : the group will follow the admin<br>");
		replyMSG.append("//mobgroup_return [groupId] : the group will return near the gm<br>");
		replyMSG.append("//mobgroup_attack [groupId] : the group will attack the gm's target<br>");
		replyMSG.append("//mobgroup_rnd [groupId] : the group will attack a randomly selected target<br>");
		replyMSG.append("//mobgroup_attackgrp [groupId1] [groupId2] : the group identified by groupId1 will attack the mobs from the group 2.<br>");
		replyMSG.append("//mobgroup_casting [groupId] : mobs will only use their skills to attack players<br>");
		replyMSG.append("//mobgroup_nomove [groupId] [on/off] : mobs will not move anymore. Interesting when used with casting mode.<br>");
		replyMSG.append("//mobgroup_invul [groupId] [on/off] : Put the mobs invulnerable. Funny with //mobgroup_attackgrp<br>");
		replyMSG.append("//mobgroup_idle [groupId] : deactivate mobs<br>");
		replyMSG.append("//mobgroup_kill [groupId] : kill the mobs<br>");
		replyMSG.append("//mobgroup_unspawn [groupId] : unspawn the mobs. mobgroup_kill should be preferred, as it seems that the server is less working<br>");
        replyMSG.append("</body></html>");

        adminReply.setHtml(replyMSG.toString());
        activeChar.sendPacket(adminReply); 
	}
}