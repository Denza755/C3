package net.sf.l2j.gameserver.model;

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.SkillTable;
import net.sf.l2j.gameserver.SkillTreeTable;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.AquireSkillList;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2FolkInstance extends L2NpcInstance {

	private static Logger _log = Logger.getLogger(L2FolkInstance.class.getName());

	private final ClassId[] _classesToTeach;

	public L2FolkInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		_classesToTeach = template.getTeachInfo();
	}

	/**
	 * this displays SkillList to the player.
	 * @param player
	 */
	public void showSkillList(L2PcInstance player, ClassId classId)
	{
		if (Config.DEBUG) _log.fine("SkillList activated on: "+getObjectId());

		int npcId = getTemplate().npcId;
		
		player.jbf.exec("skill-learn-map "+npcId+" string m@");
		if(player.jbf.st.top() != null)
		{
			player.jbf.execp("skill-learn-list");
			return;
		}

		player.jbf.st.drop();

		if (_classesToTeach == null){
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			StringBuffer sb = new StringBuffer();
			sb.append("<html><body>");
			sb.append("I cannot teach you. My class list is empty.<br> Ask admin to fix it. Need add my npcid and classes to skill_learn.sql.<br>NpcId:"+npcId+", Your classId:"+player.getClassId().getId()+"<br>");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);
			return;
		}
		if (!getTemplate().canTeach(classId)){
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			StringBuffer sb = new StringBuffer();
			sb.append("<html><body>");
			sb.append("I cannot teach you any skills.<br> You must find your current class teachers.");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);
			return;
		}
		L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(player, classId);
		AquireSkillList asl = new AquireSkillList();
		int counts=0;
		for (int i = 0; i < skills.length; i++)
		{
			L2SkillLearn s = skills[i];
			L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
			if (sk == null || !sk.getCanLearn(player.getClassId()) || !sk.canTeachBy(npcId))
				continue;
			int cost = SkillTreeTable.getInstance().getSkillCost(player, sk);
			counts++;
			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), cost, 0);
		}
		if (counts==0){
		    NpcHtmlMessage html = new NpcHtmlMessage(1);
		    int minlevel = SkillTreeTable.getInstance().getMinLevelForNewSkill(player, classId);
		    if (minlevel > 0){
			player.sendPacket(new SystemMessage(SystemMessage.YOU_DO_NOT_HAVE_ANY_FURTHER_SKILLS_TO_LEARN__COME_BACK_WHEN_YOU_HAVE_REACHED_LEVEL_S1).addNumber(minlevel));
		    }else{
		    StringBuffer sb = new StringBuffer();
		    sb.append("<html><body>");
			sb.append("You've learned all skills for your class.<br>");
			sb.append("</body></html>");
		    html.setHtml(sb.toString());
		    player.sendPacket(html);
		    }
		} else player.sendPacket(asl);
		player.sendPacket(new ActionFailed());
	}
	
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.startsWith("SkillList"))
		{
			if (Config.ALT_GAME_SKILL_LEARN)
			{
				String id = command.substring(9).trim(); 
				if (id.length() != 0) {
					player.setSkillLearningClassId(ClassId.values()[Integer.parseInt(id)]);
					showSkillList(player, ClassId.values()[Integer.parseInt(id)]);
				} else {
					boolean own_class = false;
					if (_classesToTeach != null) {
						for (ClassId cid : _classesToTeach) {
							if (cid.equalsOrChildOf(player.getClassId())) {
								own_class = true;
								break;
							}
						}
					}
					String text = 
						"<html>\n"+
						"<body>\n"+
						"<center>Skill learning:</center>\n"+
						"<br>\n";
					if (!own_class) {
						String mages = player.getClassId().isMage() ? "fighters" : "mages";
						text +=
							"Skills of your class are the easiest to learn.<br>\n"+
							"Skills of another class are harder.<br>\n"+
							"Skills for another race are event more harder to learn.<br>\n"+
							"You can also learn skills of "+mages+", and they are"+
							" the harders to learn!<br>\n"+
							"<br>\n";
					}
					// make a list of classes
					if (_classesToTeach != null) {
					    for (ClassId cid : _classesToTeach)
					    {
					        if (cid.level() != player.getClassId().level())
					            continue;
					        if (SkillTreeTable.getInstance().getAvailableSkills(player, cid).length == 0)
					            continue;
					        text += "<a action=\"bypass -h npc_%objectId%_SkillList "+cid.getId()+"\">Learn "+cid+"'s class Skills</a><br>\n";
					    }
                    }
                    else
                    {
                        text += "No Skills.<br>\n";
                    }
					text +=
						"</body>\n"+
						"</html>";
					insertObjectIdAndShowChatWindow(player, text);
					player.sendPacket( new ActionFailed() );
				}
			} else {
				player.setSkillLearningClassId(player.getClassId());
				showSkillList(player, player.getClassId());
			}
		}
		else 
		{
			// this class dont know any other commands, let forward
			// the command to the parent class
			
			super.onBypassFeedback(player, command);
		}
	}
}
