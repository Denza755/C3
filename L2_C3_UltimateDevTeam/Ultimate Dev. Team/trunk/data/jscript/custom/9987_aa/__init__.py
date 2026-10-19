print "importing custom data: AA"
import sys
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest

NPC=[8078,8079,8080,8081,8082,8083,8084,8085,8086,8087,8088,8089,8090,8091,8168,8169]

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onTalk (Self,npc,st):
   npcId = npc.getNpcId()
   if npcId in NPC:
     st.setState(STARTED)
     st.set("cond","0")
     stoneblue=st.getQuestItemsCount(6360)
     stonegreen=st.getQuestItemsCount(6361)
     stonered=st.getQuestItemsCount(6362)
     giveaa=stoneblue*3 + stonegreen*5 + stonered*10
     if giveaa > 0 :
       st.takeItems(6360,stoneblue)
       st.takeItems(6361,stonegreen)
       st.takeItems(6362,stonered)
       st.giveItems(5575,giveaa)
       htmltext = "Exchange ended succesfully."
     else :
       htmltext = "You don't have enough stones."
   st.setState(COMPLETED)
   st.exitQuest(1)
   return htmltext


QUEST       = Quest(9987,"9987_aa","custom")
CREATED     = State('Start', QUEST)
STARTED     = State('Started', QUEST)
COMPLETED   = State('Completed', QUEST)

QUEST.setInitialState(CREATED)


for item in NPC:
### Quest NPC starter initialization
   QUEST.addStartNpc(item)
### Quest NPC initialization
   STARTED.addTalkId(item)
