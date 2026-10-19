### ---------------------------------------------------------------------------
### <history>
###		Elektra@ElektraL2.com
### </history>
### ---------------------------------------------------------------------------

### Settings
NPC         = [12617]
QuestId     = 3996
QuestName   = "echoc"
QuestDesc   = "custom"
InitialHtml = "1.htm"

### Items - Format [name, giveItemId, giveItemQty, takeItem1Id, takeItem1Qty, takeItem2Id, takeItem2Qty]
Items       = [
["1st Carol", 5562, 1, 57, 10000, 57, 1],
["2nd Carol", 5563, 1, 57, 10000, 57, 1],
["3rd Carol", 5564, 1, 57, 10000, 57, 1],
["4th Carol", 5565, 1, 57, 10000, 57, 1],
["5th Carol", 5566, 1, 57, 10000, 57, 1],
["6th Carol", 5583, 1, 57, 10000, 57, 1],
["7th Carol", 5584, 1, 57, 10000, 57, 1],
["8th Carol", 5585, 1, 57, 10000, 57, 1],
["9th Carol", 5586, 1, 57, 10000, 57, 1],
["10th Carol", 5587, 1, 57, 10000, 57, 1]
]

### ---------------------------------------------------------------------------
### DO NOT MODIFY BELOW THIS LINE
### ---------------------------------------------------------------------------

print "importing " + QuestDesc + ": " + str(QuestId) + ": " + QuestName + ": " + str(len(Items)) + " item(s)",
import sys
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest

### doRequestedEvent
def do_RequestedEvent(event, st, giveItemId, giveItemQty, takeItem1Id, takeItem1Qty, takeItem2Id, takeItem2Qty) :
    if st.getQuestItemsCount(takeItem1Id) >= takeItem1Qty and st.getQuestItemsCount(takeItem2Id) >= takeItem2Qty :
        st.takeItems(takeItem1Id, takeItem1Qty)
        st.giveItems(giveItemId, giveItemQty)
        return "Echo Crystal Created"
    else :
        return "You do not have enough items."

### main code
class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onEvent (self,event,st) :
    htmltext = event

    if event == "0":
        return InitialHtml

    for item in Items:
        if event == str(item[1]):
            htmltext = do_RequestedEvent(event, st, item[1], item[2], item[3], item[4], item[5], item[6])
    
    if htmltext != event:
      st.setState(COMPLETED)
      st.exitQuest(1)

    return htmltext

 def onTalk (Self,npcId,st):
   htmltext = "<html><head><body>I have nothing to say with you</body></html>"
   st.setState(STARTED)
   return InitialHtml

### Quest class and state definition
QUEST       = Quest(QuestId,str(QuestId) + "_" + QuestName,QuestDesc)
CREATED     = State('Start',     QUEST)
STARTED     = State('Started',   QUEST)
COMPLETED   = State('Completed', QUEST)

### Quest initialization
QUEST.setInitialState(CREATED)

for item in NPC:
### Quest NPC starter initialization
   QUEST.addStartNpc(item)

### Quest NPC initialization
   STARTED.addTalkId(item)

print "...done"