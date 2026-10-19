### Settings
NPC         = [7976]
QuestId     = 6001
QuestName   = "armor_aden"
QuestDesc   = "luxury_shop"
#QuestDesc   = "Buy armor with crystals"
InitialHtml = "1.htm"
SuccessMsg  = ""
FailureMsg  = "You do not have enough materials."
CancelMsg   = "1.htm"

Items       = [
["Doom Plate Armor", 2381, [[2381, 1]], [[1461, 235], [1460, 344]], []],
["Leather Armor of Doom", 2392, [[2392, 1]], [[1461, 198], [1460, 244]], []],
["Tunic of Doom", 2399, [[2399, 1]], [[1461, 142], [1460, 204]], []],
["Sealed Doom Gloves", 2475, [[2475, 1]], [[1461, 87], [1460, 80]], []],
["Sealed Doom Boots", 601, [[601, 1]], [[1461, 87], [1460, 80]], []],
["Helm of Doom", 550, [[550, 1]], [[1461, 87], [1460, 97]], []],
["Doom Helm", 2417, [[2417, 1]], [[1461, 101], [1460, 116]], []],
["Blue Wolf Breastplate", 358, [[358, 1]], [[1461, 198], [1460, 212]], []],
["Blue Wolf Gaiters", 2380, [[2380, 1]], [[1461, 124], [1460, 184]], []],
["Blue Wolf Leather Armor", 2391, [[2391, 1]], [[1461, 216], [1460, 220]], []],
["Blue Wolf Tunic", 2398, [[2398, 1]], [[1461, 154], [1460, 183]], []],
["Blue Wolf Stockings", 2403, [[2403, 1]], [[1461, 124], [1460, 118]], []],
["Blue Wolf Helm", 2416, [[2416, 1]], [[1461, 87], [1460, 144]], []],
["Sealed Blue Wolf Gloves", 2487, [[2487, 1]], [[1461, 87], [1460, 80]], []],
["Sealed Blue Wolf Boots", 2439, [[2439, 1]], [[1461, 87], [1460, 80]], []]
]

### ---------------------------------------------------------------------------
### DO NOT MODIFY BELOW THIS LINE
### ---------------------------------------------------------------------------

print "importing " + str(QuestId) + ": " + QuestDesc,
import sys
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest

### Events
def do_Validate(st, items) :
    if len(items) > 0 :
        for item in items:
            if st.getQuestItemsCount(item[0]) < item[1] :
                return False
    return True

def do_GiveItems(st, items) :
    if len(items) > 0 :
        for item in items:
            st.giveItems(item[0], item[1])

def do_TakeItems(st, items) :
    if len(items) > 0 :
        for item in items:
            st.takeItems(item[0], item[1])

def do_Teleport(st, items) :
    if len(items) > 0 :
        st.player.teleToLocation(items[0], items[1], items[2])

def do_RequestedEvent(event, st, item) :
    if do_Validate(st, item[3]) :
        do_TakeItems(st, item[3])
        do_GiveItems(st, item[2])
        do_Teleport(st, item[4])
        if SuccessMsg != "" :
            return SuccessMsg
        return event + ".htm"
    else :
        if FailureMsg != "" :
            return FailureMsg
        return event + "-0.htm"

def do_RequestEvent(event,st) :
    htmltext = event

    if event == "0":
        if CancelMsg != "" :
            return CancelMsg
        return "Transaction has been canceled."

    for item in Items:
        if event == str(item[1]):
            return do_RequestedEvent(event, st, item)

	if htmltext != event:
		st.setState(COMPLETED)
		st.exitQuest(1)

    return htmltext

### main code
class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onEvent (self,event,st) :
    return do_RequestEvent(event,st)

 def onTalk (Self,npc,st):

   npcId = npc.getNpcId()
   htmltext = "<html><head><body>I have nothing to say with you</body></html>"
   st.setState(STARTED)
   if InitialHtml == "onEvent" :
     return do_RequestEvent(str(npcId),st)
   elif InitialHtml != "" :
     return InitialHtml
   return htmltext

### Quest class and state definition
QUEST       = Quest(QuestId, str(QuestId) + "_" + QuestName, QuestDesc)
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

print  ": Loaded " + str(len(Items)) + " item(s)"
