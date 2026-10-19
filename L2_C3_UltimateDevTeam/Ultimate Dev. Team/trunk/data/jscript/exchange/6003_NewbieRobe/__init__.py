### ---------------------------------------------------------------------------
### <history>
###		Abaddon	4/12/2005	Created
### </history>
### ---------------------------------------------------------------------------

### Settings
NPC         = [12260, 7001, 7002, 7003, 7004, 7047, 7060, 7061, 7062, 7063, 7078, 7081, 7082, 7083, 7084, 7085, 7087, 7088, 7090, 7091, 7092, 7093, 7094, 7097, 7098, 7135, 7136, 7137, 7138, 7147, 7148, 7149, 7150, 7163, 7164, 7165, 7166, 7178, 7179, 7180, 7181, 7207, 7208, 7209, 7230, 7231, 7253, 7254, 7294, 7301, 7313, 7314, 7315, 7321, 7387, 7420, 7436, 7437, 7516, 7517, 7518, 7519, 7558, 7559, 7560, 7561, 7684, 7731, 7827, 7828, 7829, 7830, 7831, 7834, 7837, 7838, 7839, 7840, 7841, 7842, 7869, 7879, 7890, 7891, 7892, 7893, 8044, 8045, 8067]
QuestId     = 6003
QuestName   = "NewbieRobe"
QuestDesc   = "exchange"
#QuestDesc   = "Exchange newbie robe"
InitialHtml = "1.htm"
SuccessMsg  = ""
FailureMsg  = "You do not have enough materials."
CancelMsg   = "1.htm"

### Items - Format [name, eventId, [giveItems], [takeitems], [teleLocation x, teleLocation y, teleLocation z]]
### giveItems - Format [itemId, qty]
### takeItems - Format [itemId, qty]
### example: 
### Items = [
###     ["MyItem1", 1001, [[ 234,   10], [ 333,    1]], [[ 563,  100], [ 363,  150]], [-80826,149775,-3043]],
###     ["MyItem2", 1002, [[ 453,    1], [  63,    1]], [[  23,   10], [ 774,  100]], [-80826,149775,-3043]]
### ]
Items       = [
    ["Apprentice`s Tunic", 425, [[426, 1]], [[425, 1], [57, 121]], []],
    ["Apprentice`s Stockings", 461, [[462, 1]], [[461, 1], [57, 86]], []],
    ["Tunic", 426, [[428, 1]], [[426, 1], [57, 2283]], []],
    ["Stockings", 462, [[463, 1]], [[462, 1], [57, 1428]], []],
    ["Feriotic Tunic", 428, [[429, 1]], [[428, 1], [57, 5530]], []],
    ["Feriotic Stockings", 463, [[464, 1]], [[463, 1], [57, 3450]], []],
    ["Cotton Tunic", 1100, [[429, 1]], [[1100, 1], [57, 5530]], []],
    ["Cotton Stockings", 1103, [[464, 1]], [[1103, 1], [57, 3450]], []],
    ["Leather Tunic", 429, [[1101, 1]], [[429, 1], [57, 12340]], []],
    ["Leather Stockings", 464, [[1104, 1]], [[464, 1], [57, 7730]], []],
    ["Tunic of Devotion", 1101, [[1102, 1]], [[1101, 1], [57, 16600]], []],
    ["Stockings of Devotion", 1104, [[1105, 1]], [[1104, 1], [57, 10300]], []],
    ["Tunic of Magic", 1102, [[432, 1]], [[1102, 1], [57, 25700]], []],
    ["Stockings of Magic", 1105, [[465, 1]], [[1105, 1], [57, 16100]], []]
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
