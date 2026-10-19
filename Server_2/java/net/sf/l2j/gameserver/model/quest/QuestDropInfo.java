/**
 * 
 */
package net.sf.l2j.gameserver.model.quest;

import java.util.ArrayList;

import net.sf.l2j.gameserver.model.L2DropData;
import net.sf.l2j.gameserver.model.L2PcInstance;

/**
 * @author XaKa
 *
 */
public class QuestDropInfo
{
	public class DropInfo
	{
		public int	dropItemID;
		public int	dropItemObjID;
		public int	dropItemCount;

		public DropInfo(int itemID, int itemCount, int itemObjID)
		{
			dropItemID		= itemID;
			dropItemObjID	= itemObjID;
			dropItemCount	= itemCount;
		}
	}
	
	public	ArrayList<DropInfo>	 dropList;
	
	public QuestDropInfo(L2PcInstance pcInstance, String questName)
	{
		QuestState	questState		= pcInstance.getQuestState(questName);
		State 		currentState	= questState.getState();
		
		dropList = new ArrayList<DropInfo>();
		for(int npcId : currentState.getKillIds())
		{
			ArrayList<L2DropData> questDrop = questState.getDrops().get(npcId);
			for(L2DropData dropInfo : questDrop)
			{
				int dropID 		= dropInfo.getItemId();
				int dropObjID	= 0;
				int dropCount	= questState.getQuestItemsCount(dropID);
				//If player don't have this quest item(don't kill need npc? or other) then skip him
				if(pcInstance.getInventory().getItemByItemId(dropID) == null)
				{
					continue;
				}
				
				dropObjID = pcInstance.getInventory().getItemByItemId(dropID).getObjectId();
				
				dropList.add(new DropInfo(dropID, dropCount, dropObjID));
			}
		}
	}
}
