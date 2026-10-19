package net.sf.l2j.gameserver.model.quest.jython;

import net.sf.l2j.gameserver.model.quest.Quest;

import org.apache.bsf.BSFManager;

public abstract class QuestJython extends Quest {

	public static void init() {
		try
		{
			BSFManager bsf = new BSFManager();
			bsf.exec("jython", "quest", 0, 0, "import data");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public QuestJython(int questId, String name, String descr) {
		super(questId, name, descr);
	}
	
}
