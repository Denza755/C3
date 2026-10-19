# Maked by Artful at 2005-11-11
print "importing quests: 334: The Wishing Potion"
import sys
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest

ADENA_ID                  =   57
DEMONS_TUNIC_ID           =  441
DEMONS_STOCKINGS_ID       =  472
SCROLL_OF_ESCAPE_ID       =  736
NECKLACE_OF_GRACE_ID      =  931
SPELLBOOK_ICEBOLT_ID      = 1049
SPELLBOOK_BATTLEHEAL_ID   = 1050
DEMONS_BOOTS_ID           = 2435
DEMONS_GLOVES_ID          = 2459
WISH_POTION_ID            = 3467
ANCIENT_CROWN_ID          = 3468
CERTIFICATE_OF_ROYALTY_ID = 3469
GOLD_BAR_ID               = 3470
ALCHEMY_TEXT_ID           = 3678
SECRET_BOOK_ID            = 3679
POTION_RECIPE_1_ID        = 3680
POTION_RECIPE_2_ID        = 3681
MATILDS_ORB_ID            = 3682
FORBIDDEN_LOVE_SCROLL_ID  = 3683
HEART_OF_PAAGRIO_ID       = 3943
#quest items (ingredients)
AMBER_SCALE_ID            = 3684
WIND_SOULSTONE_ID         = 3685
GLASS_EYE_ID              = 3686
HORROR_ECTOPLASM_ID       = 3687
SILENOS_HORN_ID           = 3688
ANT_SOLDIER_APHID_ID      = 3689
TYRANTS_CHITIN_ID         = 3690
BUGBEAR_BLOOD_ID          = 3691


class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onEvent (self,event,st) :
    htmltext = event
    if event == "7738_1" :
      htmltext = "7738-02.htm"
    elif event == "7738_2" and st.getQuestItemsCount(ALCHEMY_TEXT_ID) == 0 :
      st.giveItems(ALCHEMY_TEXT_ID,1)
      st.set("cond","1")
      st.setState(STARTED)
      st.playSound("ItemSound.quest_accept")
      htmltext = "7738-03.htm"
    elif event == "7738_5":
      if st.getQuestItemsCount(MATILDS_ORB_ID) == 0 and st.getQuestItemsCount(WISH_POTION_ID) == 0:
        st.playSound("ItemSound.quest_accept")
        st.set("cond","2")
        st.takeItems(ALCHEMY_TEXT_ID,st.getQuestItemsCount(ALCHEMY_TEXT_ID))
        st.takeItems(SECRET_BOOK_ID,st.getQuestItemsCount(SECRET_BOOK_ID))
        st.giveItems(POTION_RECIPE_1_ID,1)
        st.giveItems(POTION_RECIPE_2_ID,1)
        htmltext = "7738-06.htm"
      if st.getQuestItemsCount(MATILDS_ORB_ID) >= 1 and st.getQuestItemsCount(WISH_POTION_ID) == 0:
        st.playSound("ItemSound.quest_accept")
        st.set("cond","2")
        st.takeItems(ALCHEMY_TEXT_ID,st.getQuestItemsCount(ALCHEMY_TEXT_ID))
        st.takeItems(SECRET_BOOK_ID,st.getQuestItemsCount(SECRET_BOOK_ID))
        st.giveItems(POTION_RECIPE_1_ID,1)
        st.giveItems(POTION_RECIPE_2_ID,1)
        htmltext = "7738-12.htm"
    elif event == "7738_8":
      htmltext = "7738-09.htm"
    elif event == "7738_9":
      htmltext = "7738-10.htm"
      st.playSound("ItemSound.quest_finish")
      st.takeItems(ALCHEMY_TEXT_ID,st.getQuestItemsCount(ALCHEMY_TEXT_ID))
      st.takeItems(SECRET_BOOK_ID,st.getQuestItemsCount(SECRET_BOOK_ID))
      st.takeItems(POTION_RECIPE_1_ID,st.getQuestItemsCount(POTION_RECIPE_1_ID))
      st.takeItems(POTION_RECIPE_2_ID,st.getQuestItemsCount(POTION_RECIPE_2_ID))
      st.takeItems(AMBER_SCALE_ID,st.getQuestItemsCount(AMBER_SCALE_ID))
      st.takeItems(WIND_SOULSTONE_ID,st.getQuestItemsCount(WIND_SOULSTONE_ID))
      st.takeItems(GLASS_EYE_ID,st.getQuestItemsCount(GLASS_EYE_ID))
      st.takeItems(HORROR_ECTOPLASM_ID,st.getQuestItemsCount(HORROR_ECTOPLASM_ID))
      st.takeItems(SILENOS_HORN_ID,st.getQuestItemsCount(SILENOS_HORN_ID))
      st.takeItems(ANT_SOLDIER_APHID_ID,st.getQuestItemsCount(ANT_SOLDIER_APHID_ID))
      st.takeItems(TYRANTS_CHITIN_ID,st.getQuestItemsCount(TYRANTS_CHITIN_ID))
      st.takeItems(BUGBEAR_BLOOD_ID,st.getQuestItemsCount(BUGBEAR_BLOOD_ID))
      if st.getQuestItemsCount(MATILDS_ORB_ID) == 0 : st.giveItems(MATILDS_ORB_ID,1)
      st.giveItems(WISH_POTION_ID,1)
    elif event == "7738_11" :
      if st.getQuestItemsCount(WISH_POTION_ID) >= 1:
        htmltext = "7738-15.htm"
      else :
        htmltext = "7738-14.htm"
    elif event == "7738_16":
      if st.getQuestItemsCount(WISH_POTION_ID) >= 1:
        st.takeItems(WISH_POTION_ID,1)
        WISH_CHANCE = st.getRandom(100)
        if WISH_CHANCE <= 50:
          st.spawnNpc(5136)
          st.spawnNpc(5136)
          st.spawnNpc(5136)
        else:
          st.spawnNpc(7742)
        htmltext = "7738-16.htm"
      else:
        htmltext = "7738-14.htm"
    elif event == "7738_17":
      if st.getQuestItemsCount(WISH_POTION_ID) >= 1:
        st.takeItems(WISH_POTION_ID,1)
        
        WISH_CHANCE = st.getRandom(300)
        if WISH_CHANCE <= 199:
          st.spawnNpc(5135)
          st.spawnNpc(5135)
          st.spawnNpc(5135)
        elif WISH_CHANCE = 200:
          st.giveItems(ADENA_ID,((st.getRandom(10)+1)*1000000))
        else:
          st.giveItems(ADENA_ID,10000)  
        htmltext = "7738-17.htm"
      else:
        htmltext = "7738-14.htm"
    elif event == "7738_18":
      if st.getQuestItemsCount(WISH_POTION_ID) >= 1:
        st.takeItems(WISH_POTION_ID,1)
        WISH_CHANCE = st.getRandom(100)
        if WISH_CHANCE <= 33:
          st.giveItems(CERTIFICATE_OF_ROYALTY_ID,1)
        elif WISH_CHANCE >= 66:
          st.giveItems(ANCIENT_CROWN_ID,1)
        else:
          st.spawnNpc(5153)
        htmltext = "7738-18.htm"
      else:
        htmltext = "7738-14.htm"
    elif event == "7738_19":
      if st.getQuestItemsCount(WISH_POTION_ID) >= 1:
        st.takeItems(WISH_POTION_ID,1)
        WISH_CHANCE = st.getRandom(100)
        if WISH_CHANCE <= 33:
          st.giveItems(SPELLBOOK_ICEBOLT_ID,1)
        elif WISH_CHANCE >= 66:
          st.giveItems(SPELLBOOK_BATTLEHEAL_ID,1)
        else:
          st.spawnNpc(7743)
        htmltext = "7738-19.htm"
      else:
        htmltext = "7738-14.htm"
 
    return htmltext


 def onTalk (Self,npc,st):
   npcId = npc.getNpcId()
   htmltext = "<html><head><body>I have nothing to say you</body></html>"
   id = st.getState()
   if id == CREATED :
     st.setState(STARTING)
     st.set("cond","0")
   if npcId == 7738 : 
     if st.getPlayer().getLevel() <= 29:
       htmltext = "7738-21.htm"
       st.exitQuest(1)
     elif int(st.get("cond")) == 0 :
       htmltext = "7738-01.htm"
     elif int(st.get("cond")) == 3 and st.getQuestItemsCount(MATILDS_ORB_ID) >= 1 and st.getQuestItemsCount(WISH_POTION_ID) == 0 and st.getQuestItemsCount(POTION_RECIPE_1_ID) == 0 and st.getQuestItemsCount(POTION_RECIPE_2_ID == 0) :
       htmltext = "7738-05.htm"
     elif st.getQuestItemsCount(MATILDS_ORB_ID) >= 1 and st.getQuestItemsCount(WISH_POTION_ID) >= 1:
       htmltext = "7738-11.htm"
     elif int(st.get("cond")) == 1 and st.getQuestItemsCount(ALCHEMY_TEXT_ID) and st.getQuestItemsCount(SECRET_BOOK_ID) == 0:
       htmltext = "7738-04.htm"
     elif int(st.get("cond")) == 1 and st.getQuestItemsCount(ALCHEMY_TEXT_ID) and st.getQuestItemsCount(SECRET_BOOK_ID) :
       htmltext = "7738-05.htm"
     elif int(st.get("cond")) == 2 and st.getQuestItemsCount(POTION_RECIPE_1_ID) and st.getQuestItemsCount(POTION_RECIPE_2_ID) :
       htmltext = "7738-07.htm"
     elif int(st.get("cond")) == 3 and st.getQuestItemsCount(POTION_RECIPE_1_ID) and st.getQuestItemsCount(POTION_RECIPE_2_ID) and (st.getQuestItemsCount(AMBER_SCALE_ID)+st.getQuestItemsCount(WIND_SOULSTONE_ID)+st.getQuestItemsCount(GLASS_EYE_ID)+st.getQuestItemsCount(HORROR_ECTOPLASM_ID)+st.getQuestItemsCount(SILENOS_HORN_ID)+st.getQuestItemsCount(ANT_SOLDIER_APHID_ID)+st.getQuestItemsCount(TYRANTS_CHITIN_ID)+st.getQuestItemsCount(BUGBEAR_BLOOD_ID)) == 8 :
       htmltext = "7738-08.htm"
     else :
       htmltext = "no_quest.htm"
   if npcId == 7557 :
     if st.getQuestItemsCount(FORBIDDEN_LOVE_SCROLL_ID) >= 1:
       st.takeItems(FORBIDDEN_LOVE_SCROLL_ID,1)     
       st.giveItems(ADENA_ID,500000)
       htmltext = "7557-01.htm"
     else :
       htmltext = "no_quest.htm"
   if npcId == 7743 :
     DROP_CHANCE = st.getRandom(100)
     if DROP_CHANCE <= 20:
       st.giveItems(SPELLBOOK_ICEBOLT_ID,1)
       st.giveItems(SPELLBOOK_BATTLEHEAL_ID,1)
       st.getPlayer().getTarget().decayMe()
       htmltext = "7743-06.htm"
     elif DROP_CHANCE <= 30:
       st.giveItems(HEART_OF_PAAGRIO_ID,1)
       st.getPlayer().getTarget().decayMe()
       htmltext = "7743-06.htm"
     else:
       st.getPlayer().getTarget().decayMe()
       htmltext = "7743-0" + str(st.getRandom(4)+1) + ".htm"
   if npcId == 7742 :
     DROP_CHANCE = st.getRandom(100)
     if DROP_CHANCE <= 4:
       st.giveItems(NECKLACE_OF_GRACE_ID,1)
     else:
       st.giveItems(SCROLL_OF_ESCAPE_ID,1)
     st.getPlayer().getTarget().decayMe()
     htmltext = "7742-01.htm"



   return htmltext

 def onKill (self,npc,st):
   npcId = npc.getNpcId()
   checksound = 0
   if npcId == 5139 :
     if st.getQuestItemsCount(SECRET_BOOK_ID) == 0 and int(st.get("cond")) == 1 :
       st.giveItems(SECRET_BOOK_ID,1)
       st.playSound("ItemSound.quest_middle")
   elif npcId == 78 :
     if st.getQuestItemsCount(POTION_RECIPE_1_ID) and st.getQuestItemsCount(POTION_RECIPE_2_ID) and st.getQuestItemsCount(WIND_SOULSTONE_ID) == 0 :
       if st.getRandom(100) <= 20 :
         st.giveItems(WIND_SOULSTONE_ID,1)
         checksound = 1
   elif npcId == 199 :
     if st.getQuestItemsCount(POTION_RECIPE_1_ID) and st.getQuestItemsCount(POTION_RECIPE_2_ID) and st.getQuestItemsCount(AMBER_SCALE_ID) == 0 :
       if st.getRandom(100) <= 15 :
         st.giveItems(AMBER_SCALE_ID,1)
         checksound = 1
   elif npcId == 250 :
     if st.getQuestItemsCount(POTION_RECIPE_1_ID) and st.getQuestItemsCount(POTION_RECIPE_2_ID) and st.getQuestItemsCount(GLASS_EYE_ID) == 0 :
       if st.getRandom(100) <= 35 :
         st.giveItems(GLASS_EYE_ID,1)
         checksound = 1
   elif npcId == 227 :
     if st.getQuestItemsCount(POTION_RECIPE_1_ID) and st.getQuestItemsCount(POTION_RECIPE_2_ID) and st.getQuestItemsCount(HORROR_ECTOPLASM_ID) == 0 :
       if st.getRandom(100) <= 15 :
         st.giveItems(HORROR_ECTOPLASM_ID,1)
         checksound = 1
   elif npcId == 168 :
     if st.getQuestItemsCount(POTION_RECIPE_1_ID) and st.getQuestItemsCount(POTION_RECIPE_2_ID) and st.getQuestItemsCount(SILENOS_HORN_ID) == 0 :
       if st.getRandom(100) <= 30 :
         st.giveItems(SILENOS_HORN_ID,1)
         checksound = 1
   elif npcId == 87 :
     if st.getQuestItemsCount(POTION_RECIPE_1_ID) and st.getQuestItemsCount(POTION_RECIPE_2_ID) and st.getQuestItemsCount(ANT_SOLDIER_APHID_ID) == 0 :
       if st.getRandom(100) <= 40 :
         st.giveItems(ANT_SOLDIER_APHID_ID,1)
         checksound = 1
   elif npcId in [192,193] :
     if st.getQuestItemsCount(POTION_RECIPE_1_ID) and st.getQuestItemsCount(POTION_RECIPE_2_ID) and st.getQuestItemsCount(TYRANTS_CHITIN_ID) == 0 :
       if st.getRandom(100) <= 50 :
         st.giveItems(TYRANTS_CHITIN_ID,1)
         checksound = 1
   elif npcId in [248,249] :
     if st.getQuestItemsCount(POTION_RECIPE_1_ID) and st.getQuestItemsCount(POTION_RECIPE_2_ID) and st.getQuestItemsCount(BUGBEAR_BLOOD_ID) == 0 :
       if st.getRandom(100) <= 25 :
         st.giveItems(BUGBEAR_BLOOD_ID,1)
         checksound = 1

   elif npcId == 5136 and int(st.get("cond")) == 3 :
     if st.getRandom(100) <= 3 :
       st.playSound("ItemSound.quest_itemget")
       st.giveItems(FORBIDDEN_LOVE_SCROLL_ID,1)
   elif npcId == 5135 and int(st.get("cond")) == 3 :
     if st.getRandom(100) <= 10 :
       st.playSound("ItemSound.quest_itemget") 
       st.giveItems(GOLD_BAR_ID,st.getRandom(4)+1)
   elif npcId == 5153 and int(st.get("cond")) == 3 :
     if st.getRandom(100) <= 50:
       st.spawnNpc(5154)
   elif npcId == 5154 and int(st.get("cond")) == 3 :
     if st.getRandom(100) <= 50:
       st.spawnNpc(5155)
   elif npcId == 5155 and int(st.get("cond")) == 3 :
     if st.getRandom(100) <= 50:
       st.spawnNpc(5138)
   elif npcId == 5138 and int(st.get("cond")) == 3 :
     if st.getRandom(100) <= 50:
       DEMON_DROP_CHANCE = st.getRandom(100)
       if DEMON_DROP_CHANCE <= 20:
         st.playSound("ItemSound.quest_itemget") 
         st.giveItems(DEMONS_BOOTS_ID,1)
       elif DEMON_DROP_CHANCE <= 40:
         st.playSound("ItemSound.quest_itemget") 
         st.giveItems(DEMONS_GLOVES_ID,1)
       elif DEMON_DROP_CHANCE <= 60:
         st.playSound("ItemSound.quest_itemget") 
         st.giveItems(DEMONS_STOCKINGS_ID,1)
       elif DEMON_DROP_CHANCE <= 80:
         st.playSound("ItemSound.quest_itemget") 
         st.giveItems(DEMONS_TUNIC_ID,1)
   if checksound == 1 :
     if (st.getQuestItemsCount(AMBER_SCALE_ID)+st.getQuestItemsCount(WIND_SOULSTONE_ID)+st.getQuestItemsCount(GLASS_EYE_ID)+st.getQuestItemsCount(HORROR_ECTOPLASM_ID)+st.getQuestItemsCount(SILENOS_HORN_ID)+st.getQuestItemsCount(ANT_SOLDIER_APHID_ID)+st.getQuestItemsCount(TYRANTS_CHITIN_ID)+st.getQuestItemsCount(BUGBEAR_BLOOD_ID)) == 8 :
       st.playSound("ItemSound.quest_middle")
       st.set("cond","3")
     else :
       st.playSound("ItemSound.quest_itemget")
   return


QUEST       = Quest(334,"334_TheWishingPotion","The Wishing Potion")
CREATED     = State('Start', QUEST)
STARTING    = State('Starting', QUEST)
STARTED     = State('Started', QUEST)
COMPLETED   = State('Completed', QUEST)


QUEST.setInitialState(CREATED)
QUEST.addStartNpc(7738)

STARTING.addTalkId(7738)

for npcId in [7738,7742,7743,7557]:
  STARTED.addTalkId(npcId)

for mobId in [78,87,168,192,193,199,227,248,249,250,5135,5136,5138,5139,5153,5154,5155]:
  STARTED.addKillId(mobId)

