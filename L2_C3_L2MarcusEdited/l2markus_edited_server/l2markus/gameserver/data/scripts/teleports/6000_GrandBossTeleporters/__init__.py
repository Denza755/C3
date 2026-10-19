#Made by L2MarkuS
import sys
from net.sf.l2j.gameserver.datatables import DoorTable
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest
from net.sf.l2j.gameserver.datatables import SpawnTable
from net.sf.l2j.util import Rnd
from net.sf.l2j.gameserver.instancemanager import QuestManager
from net.sf.l2j.gameserver.instancemanager import GrandBossManager

qn = "6000_GrandBossTeleporters"

NPCs = [
    12250, #Heart of Warding : Teleport into Lair of Antharas
    12324, #Teleportation Cubic : Teleport out of Lair of Antharas
    8384, #Gatekeeper of Fire Dragon : Opening some doors
    8385, #Heart of Volcano : Teleport into Lair of Valakas
    8540, #Watcher of Valakas Klein : Teleport into Hall of Flames
    8686, #Gatekeeper of Fire Dragon : Opens doors to Heart of Volcano
    8687, #Gatekeeper of Fire Dragon : Opens doors to Heart of Volcano
    8759, #Teleportation Cubic : Teleport out of Lair of Valakas
    ]

class Quest (JQuest) :

 def __init__(self,id,name,descr):
     self.antharasAI = QuestManager.getInstance().getQuest("antharas")
     self.valakasAI = QuestManager.getInstance().getQuest("valakas")
     self.count = 0
     self.count2 = 0
     JQuest.__init__(self,id,name,descr)

 def onEvent (self,event,st):
    st = st.getQuestState(qn)
    if not st: 
       st = self.newQuestState(st)
    if event == "31540":
       if st.getQuestItemsCount(7267) > 0 :
          st.takeItems(7267,1)
          st.teleToLocation(183813,-115157,-3303)
          return
       else :
          htmltext = "31540-06.htm"
    return htmltext

 def onTalk (self,npc,st):
    print "importing quest 2"
    npcId = npc.getNpcId()
    htmltext = ""
    if npcId == 12250 : #heart of warding
        print "importing quest 3"
        htmltext = "13001-01.htm"
#        if self.antharasAI :
        status = GrandBossManager.getInstance().getBossStatus(12211)
	if status == 0 or status == 1 : #If entrance to see Antharas is unlocked (he is Dormant or Waiting)
                if st.getQuestItemsCount(3865) > 0 :
                    st.takeItems(3865,1)
                    zone = GrandBossManager.getInstance().getZone(179700,113800,-7709)
                    if zone : 
                       zone.allowstEntry(st,30)
                    x = 179700 + Rnd.get(700)
                    y = 113800 + Rnd.get(2100)
                    st.player.teleToLocation(x,y,-7709)
                    if status == 0 :
                        antharas = GrandBossManager.getInstance().getBoss(12211)
                        self.antharasAI.startQuestTimer("waiting",1800000, antharas, None)
                        GrandBossManager.getInstance().setBossStatus(12211,1)
                    return
                else :
                    htmltext = "13001-03.htm"
	elif status == 2 :
		htmltext = "13001-02.htm"
    elif npcId == 12324 : #antharas teleport cube
        x = 79800 + Rnd.get(600)
        y = 151200 + Rnd.get(1100)
        st.teleToLocation(x,y,-3534)
        return
    elif npcId == 8385 : #heart of volcano
        htmltext = "31385-01.htm"
        if self.valakasAI :
            status = GrandBossManager.getInstance().getBossStatus(29028)
            if status == 0 or status == 1 : #If entrance to see Valakas is unlocked (he is Dormant or Waiting)
                if self.count >= 200 :
                   htmltext = "31385-03.htm"
                else :
                   zone = GrandBossManager.getInstance().getZone(212852,-114842,-1632)
                   if zone :
                      zone.allowstEntry(st,30)
                   x = 204328 + Rnd.get(600)
                   y = -111874 + Rnd.get(600)
                   st.teleToLocation(x,y,70)
                   self.count = self.count+1
                   if status == 0 :
                      valakas = GrandBossManager.getInstance().getBoss(29028)
                      self.valakasAI.startQuestTimer("1001",1800000, valakas, None)
                      GrandBossManager.getInstance().setBossStatus(29028,1)
                   return
            elif status == 2 :
                htmltext = "31385-02.htm"
            else :
                htmltext = "31385-01.htm"
        else :
            htmltext = "31385-01.htm"
    elif npcId == 8384 : #Gatekeeper of Fire Dragon
        DoorTable.getInstance().getDoor(24210004).openMe()
        return
    elif npcId == 8686 : #Gatekeeper of Fire Dragon
        DoorTable.getInstance().getDoor(24210006).openMe()
        return
    elif npcId == 8687 : #Gatekeeper of Fire Dragon
        DoorTable.getInstance().getDoor(24210005).openMe()
        return
    elif npcId == 8540 : #Watcher of Valakas Klein
        st = st.getQuestState(qn)
        if self.count < 50 :
           htmltext = "31540-01.htm"
        elif self.count < 100 :
           htmltext = "31540-02.htm"
        elif self.count < 150 :
           htmltext = "31540-03.htm"
        elif self.count < 200 :
           htmltext = "31540-04.htm"
        else:
           htmltext = "31540-05.htm"
    elif npcId == 8759 : #valakas teleport cube
        x = 150037 + Rnd.get(500)
        y = -57720 + Rnd.get(500)
        st.teleToLocation(x,y,-2976)
        return
    return htmltext
QUEST       = Quest(6000,qn,"Teleports")
CREATED     = State('Start', QUEST)
STARTED     = State('Started', QUEST)
COMPLETED   = State('Completed', QUEST)
QUEST.setInitialState(CREATED)
QUEST.addStartNpc(12250)
QUEST.addTalkId(12250)
print "importing quest"