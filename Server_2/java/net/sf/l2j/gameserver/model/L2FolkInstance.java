package net.sf.l2j.gameserver.model;

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.SkillTable;
import net.sf.l2j.gameserver.SkillTreeTable;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.AquireSkillList;
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
		L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(player, classId);
		AquireSkillList asl = new AquireSkillList();
		for (int i = 0; i < skills.length; i++)
		{
			L2SkillLearn s = skills[i];
			L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
			if (sk == null || !sk.getCanLearn(player.getClassId()) || !sk.canTeachBy(npcId))
				continue;
			int cost = SkillTreeTable.getInstance().getSkillCost(player, sk);
			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), cost, 0);
		}
		player.sendPacket(asl);
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
					for (ClassId cid : _classesToTeach)
					{
						if (cid.level() != player.getClassId().level())
							continue;
						if (SkillTreeTable.getInstance().getAvailableSkills(player, cid).length == 0)
							continue;
						text += "<a action=\"bypass -h npc_%objectId%_SkillList "+cid.getId()+"\">Learn "+cid+"'s class Skills</a><br>\n";
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
