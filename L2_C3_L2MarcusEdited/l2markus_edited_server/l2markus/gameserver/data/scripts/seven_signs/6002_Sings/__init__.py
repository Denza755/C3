### ---------------------------------------------------------------------------
### <history>
###		Abaddon	09/04/2012	Created By DontStrong
### </history>
### ---------------------------------------------------------------------------

### Settings
NPC         = [8078,8079,8080,8081,8082,8083,8084,8085,8086,8087,8088,8089,8090,8091,8230]
QuestId     = 6002
QuestName   = "Sings"
QuestDesc   = "seven_signs"
#QuestDesc   = Signs"
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
###["MyItemName", HtmlID, [[giveItems, giveItemCount]], [[Adena, ACount's], [AncientAdena's, Counts]], []],
    ["Greater Healing Potion", 2, [[1539, 1]], [[57, 90], [5575, 1080]], []],
    ["Blessed Resurrection Scroll for Pet", 3, [[6387, 1]], [[57, 500], [5575, 6000]], []],
    ["Greater Haste Potion", 4, [[1374, 1]], [[57, 300], [5575, 3600]], []],
    ["Greater Swift Attack Potion", 5, [[1375, 1]], [[57, 600], [5575, 7200]], []],
    ["Greater Magic Haste Potion", 6, [[6036, 1]], [[57, 600], [5575, 7200]], []],
	["CP Potion", 7, [[5591, 1]], [[57, 20], [5575, 240]], []],
	["Greater CP Potion", 8, [[5592, 1]], [[57, 50], [5575, 600]], []],
	["SP Scroll - Low Grade", 9, [[5593, 1]], [[57, 200], [5575, 2400]], []],
	["SP Scroll - Medium Grade", 10, [[5594, 1]], [[57, 2000], [5575, 24000]], []],
	["SP Scroll - High Grade", 11, [[5595, 1]], [[57, 40000], [5575, 480000]], []],
	["Mystery Potion", 12, [[5234, 1]], [[57, 1000], [5575, 12000]], []],
	["Facelifting Potion A", 13, [[5235, 1]], [[57, 2000], [5575, 24000]], []],
	["Facelifting Potion B", 14, [[5236, 1]], [[57, 2000], [5575, 24000]], []],
	["Facelifting Potion C", 15, [[5237, 1]], [[57, 2000], [5575, 24000]], []],
	["Hair Dye Potion A", 16, [[5238, 1]], [[57, 2000], [5575, 24000]], []],
	["Hair Dye Potion B", 17, [[5239, 1]], [[57, 2000], [5575, 24000]], []],
	["Hair Dye Potion C", 18, [[5240, 1]], [[57, 2000], [5575, 24000]], []],
	["Hair Dye Potion D", 19, [[5241, 1]], [[57, 2000], [5575, 24000]], []],
	["Hair Style Change Potion A", 20, [[5242, 1]], [[57, 2000], [5575, 24000]], []],
	["Hair Style Change Potion B", 21, [[5243, 1]], [[57, 2000], [5575, 24000]], []],
	["Hair Style Change Potion C", 22, [[5244, 1]], [[57, 2000], [5575, 24000]], []],
	["Hair Style Change Potion D", 23, [[5245, 1]], [[57, 2000], [5575, 24000]], []],
	["Hair Style Change Potion E", 24, [[5246, 1]], [[57, 2000], [5575, 24000]], []],
	["Hair Style Change Potion F", 25, [[5247, 1]], [[57, 2000], [5575, 24000]], []],
	["Hair Style Change Potion G", 26, [[5248, 1]], [[57, 2000], [5575, 24000]], []],
	["Party Mask", 27, [[5808, 1]], [[57, 50000], [5575, 600000]], []],
	["Squeaking Shoes", 28, [[5590, 1]], [[57, 35000], [5575, 420000]], []],
	["Record of the Seven Signs", 29, [[5707, 1]], [[57, 50], [5575, 600]], []]
]

### ---------------------------------------------------------------------------
### DO NOT MODIFY BELOW THIS LINE
### ---------------------------------------------------------------------------

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
