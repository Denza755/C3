# Take Item, Give Item, AA need, ID, Discript

items = [
[129, 2499, 480000, 1, "Sword of Revolution to Elven Long Sword"],
[2499, 72, 580000, 2, "Elven Long Sword to Stormbringer"],
[72, 74, 696000, 3, "Stormbringer to Katana"],
[72, 133, 696000, 4, "Stormbringer to Raid Sword"],
[72, 73, 696000, 5, "Stormbringer to Shamshir"],
[72, 131, 696000, 6, "Stormbringer to Spirit Sword"],
[73, 75, 1760000, 7, "Shamshir to Caliburs"],
[73, 76, 1760000, 8, "Shamshir to Sword of Delusion"],
[73, 77, 1760000, 9, "Shamshir to Tsurugi"],
[73, 134, 1760000, 10, "Shamshir to Sword of Nightmare"],
[73, 132, 1760000, 11, "Shamshir to Sword of Limit"],
[74, 75, 1760000, 12, "Katana to Caliburs"],
[74, 76, 1760000, 13, "Katana to Sword of Delusion"],
[74, 77, 1760000, 14, "Katana to Tsurugi"],
[74, 134, 1760000, 15, "Katana to Sword of Nightmare"],
[74, 132, 1760000, 16, "Katana to Sword of Limit"],
[131, 75, 1760000, 17, "Spirit Sword to Caliburs"],
[131, 76, 1760000, 18, "Spirit Sword to Sword of Delusion"],
[131, 77, 1760000, 19, "Spirit Sword to Tsurugi"],
[131, 134, 1760000, 20, "Spirit Sword to Sword of Nightmare"],
[131, 132, 1760000, 21, "Spirit Sword to Sword of Limit"],
[133, 75, 1760000, 22, "Raid Sword to Caliburs"],
[133, 76, 1760000, 23, "Raid Sword to Sword of Delusion"],
[133, 77, 1760000, 24, "Raid Sword to Tsurugi"],
[133, 134, 1760000, 25, "Raid Sword to Sword of Nightmare"],
[133, 132, 1760000, 26, "Raid Sword to Sword of Limit"],
[75, 135, 2200000, 27, "Caliburs to Samurai Longsword"],
[76, 135, 2200000, 28, "Sword of Delusion to Samurai Longsword"],
[77, 135, 2200000, 29, "Tsurugi to Samurai Longsword"],
[134, 135, 2200000, 30, "Sword of Nightmare to Samurai Longsword"],
[132, 135, 2200000, 31, "Sword of Limit to Samurai Longsword"],
[279, 280, 480000, 32, "Strengthened Long Bow to Light Crossbow"],
[280, 281, 580000, 33, "Light Crossbow to Crystallized Ice Bow"],
[281, 285, 696000, 34, "Crystallized Ice Bow to Noble Elven Bow"],
[281, 282, 696000, 35, "Crystallized Ice Bow to Elemental Bow"],
[285, 283, 1760000, 36, "Noble Elven Bow to Akat Long Bow"],
[282, 283, 1760000, 37, "Elemental Bow to Akat Long Bow"],
[283, 286, 2200000, 38, "Akat Long Bow to Eminence Bow"],
[1660, 225, 480000, 39, "Cursed Maingauche to Mithril Dagger"],
[224, 225, 480000, 40, "Maingauche to Mithril Dagger"],
[225, 226, 580000, 41, "Mithril Dagger to Cursed Dagger"],
[225, 232, 580000, 42, "Mithril Dagger to Dark Elven Dagger"],
[226, 242, 696000, 43, "Cursed Dagger to Soulfire Dirk"],
[226, 227, 696000, 44, "Cursed Dagger to Stiletto"],
[232, 242, 696000, 45, "Dark Elven Dagger to Soulfire Dirk"],
[232, 247, 696000, 46, "Dark Elven Dagger to Stiletto"],
[242, 231, 1760000, 47, "Soulfire Dirk to Grace Dagger"],
[242, 233, 1760000, 48, "Soulfire Dirk to Dark Screamer"],
[247, 231, 1760000, 49, "Stiletto to Grace Dagger"],
[247, 233, 1760000, 50, "Stiletto to Dark Screamer"],
[231, 228, 2200000, 51, "Grace Dagger to Crystal Dagger"],
[233, 228, 2200000, 52, "Dark Screamer to Crystal Dagger"],
[158, 189, 480000, 53, "Tarbar to Staff of Life"],
[158, 159, 480000, 54, "Tarbar to Bonebreaker"],
[88, 189, 480000, 55, "Morning Star to Staff of Life"],
[88, 159, 480000, 56, "Morning Star to Bonebreaker"],
[169, 189, 480000, 57, "Skull Breaker to Staff of Life"],
[169, 159, 480000, 58, "Skull Breaker to Bonebreaker"],
[172, 189, 480000, 59, "Heavy Bone Club to Staff of Life"],
[172, 159, 480000, 60, "Heavy Bone Club to Bonebreaker"],
[189, 193, 580000, 61, "Staff of Life to Stick of Faith"],
[189,  89, 580000, 62, "Staff of Life to Big Hammer"],
[189, 160, 580000, 63, "Staff of Life to Battle Axe"],
[189, 161, 580000, 64, "Staff of Life to Silver Axe"],
[189, 173, 580000, 65, "Staff of Life to Skull Graver"],
[159, 193, 580000, 66, "Bonebreaker to Stick of Faith"],
[159,  89, 580000, 67, "Bonebreaker to Big Hammer"],
[159, 160, 580000, 68, "Bonebreaker to Battle Axe"],
[159, 161, 580000, 69, "Bonebreaker to Silver Axe"],
[159, 173, 580000, 70, "Bonebreaker to Skull Graver"],
[193, 2502, 696000, 71, "Stick of Faith to Dwarven War Hammer"],
[89, 2502, 696000, 72, "Big Hammer to Dwarven War Hammer"],
[160, 2502, 696000, 73, "Battle Axe to Dwarven War Hammer"],
[161, 2502, 696000, 74, "Silver Axe to Dwarven War Hammer"],
[173, 2502, 696000, 75, "Skull Graver to Dwarven War Hammer"],
[2502, 174, 1760000, 76, "Dwarven War Hammer to Nirvana Axe"],
[2502, 196, 1760000, 77, "Dwarven War Hammer to Stick of Eternity"],
[2502, 201, 1760000, 78, "Dwarven War Hammer to Club of Nature"],
[2502, 202, 1760000, 79, "Dwarven War Hammer to Mace of The Underworld"],
[2502, 162, 1760000, 80, "Dwarven War Hammer to War Axe"],
[174, 2503, 2200000, 81, "Nirvana Axe to Yaksa Mace"],
[196, 2503, 2200000, 82, "Stick of Eternity to Yaksa Mace"],
[201, 2503, 2200000, 83, "Club of Nature to Yaksa Mace"],
[202, 2503, 2200000, 84, "Mace of The Underworld to Yaksa Mace"],
[162, 2503, 2200000, 85, "War Axe to Yaksa Mace"],
[90, 188, 480000, 86, "Goat Head Staff to Ghost Staff"],
[90, 190, 480000, 87, "Goat Head Staff to Atuba Mace"],
[90, 187, 480000, 88, "Goat Head Staff to Atuba Hammer"],
[188, 192, 580000, 89, "Ghost Staff to Crystal Staff"],
[188, 194, 580000, 90, "Ghost Staff to Heavy Doom Axe"],
[188, 191, 580000, 91, "Ghost Staff to Heavy Doom Hammer"],
[190, 192, 580000, 92, "Atuba Mace to Crystal Staff"],
[190, 194, 580000, 93, "Atuba Mace to Heavy Doom Axe"],
[190, 191, 580000, 94, "Atuba Mace to Heavy Doom Hammer"],
[187, 192, 580000, 95, "Atuba Hammer to Crystal Staff"],
[187, 194, 580000, 96, "Atuba Hammer to Heavy Doom Axe"],
[187, 191, 580000, 97, "Atuba Hammer to Heavy Doom Hammer"],
[192, 195, 696000, 98, "Crystal Staff to Cursed Staff"],
[194, 195, 696000, 99, "Heavy Doom Axe to Cursed Staff"],
[191, 195, 696000, 100, "Heavy Doom Hammer to Cursed Staff"],
[195, 197, 1760000, 101, "Cursed Staff to Paradia Staff"],
[195, 199, 1760000, 102, "Cursed Staff to Paagrio Hammer"],
[195, 198, 1760000, 103, "Cursed Staff to Inferno Staff"],
[195, 200, 1760000, 104, "Cursed Staff to Sage Staff"],
[197, 206, 2200000, 105, "Paradia Staff to Demon Staff"],
[197, 204, 2200000, 106, "Paradia Staff to Deadman Staff"],
[197, 205, 2200000, 107, "Paradia Staff to Ghoul Staff"],
[199, 206, 2200000, 108, "Paagrio Hammer to Demon Staff"],
[199, 204, 2200000, 109, "Paagrio Hammer to Deadman Staff"],
[199, 205, 2200000, 110, "Paagrio Hammer to Ghoul Staff"],
[198, 206, 2200000, 111, "Inferno Staff to Demon Staff"],
[198, 204, 2200000, 112, "Inferno Staff to Deadman Staff"],
[198, 205, 2200000, 113, "Inferno Staff to Ghoul Staff"],
[200, 206, 2200000, 114, "Sage Staff to Demon Staff"],
[200, 204, 2200000, 115, "Sage Staff to Deadman Staff"],
[200, 205, 2200000, 116, "Sage Staff to Ghoul Staff"],
[93, 297, 480000, 117, "Winged Spear to Glaive"],
[294, 297, 480000, 118, "War Pick to Glaive"],
[297, 96, 580000, 119, "Glaive to Scythe"],
[297, 298, 580000, 120, "Glaive to Orcish Glaive"],
[297, 302, 580000, 121, "Glaive to Body Slasher"],
[96, 94, 696000, 122, "Scythe to Bec de Corbin"],
[298, 94, 696000, 123, "Orcish Glaive to Bec de Corbin"],
[302, 94, 696000, 124, "Body Slasher to Bec de Corbin"],
[94, 95, 1760000, 125, "Bec de Corbin to Poleaxe"],
[94, 301, 1760000, 126, "Bec de Corbin to Scorpion"],
[94, 303, 1760000, 127, "Bec de Corbin to Widow Maker"],
[95, 299, 2200000, 128, "Poleaxe to Orcish Poleaxe"],
[301, 299, 2200000, 129, "Scorpion to Orcish Poleaxe"],
[303, 299, 2200000, 130, "Widow Maker to Orcish Poleaxe"],
[261, 262, 480000, 131, "Bich Hwa to Scallop Jamadhr"],
[262, 263, 580000, 132, "Scallop Jamadhr to Chakram"],
[263, 4233, 686000, 133, "Chakram to Knuckle Duster"],
[4233, 265, 1760000, 134, "Knuckle Duster to Fist Blade"],
[265, 266, 2200000, 135, "Fist Blade to Great Pata"]
]

import sys
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest

class Quest (JQuest) :

	def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

	def onEvent (self,event,st) :
		htmltext = event

		for item in items:
			if event == str(item[3]):
				if st.getQuestItemsCount(item[0]) >= 1 and st.getQuestItemsCount(5575) >= item[2]:
					st.takeItems(item[0],1)
					st.takeItems(5575,item[2])
					st.giveItems(item[1],1)
					htmltext = "Item has been succesfully traded."
				else:
					htmltext = "Not enough Material."

		if event == "0":
			htmltext = "Transaction has been canceled."

		if htmltext != event:
			st.setState(COMPLETED)
			st.exitQuest(1)

		return htmltext

	def onTalk (Self,npc,st):

		npcId = npc.getNpcId()
		htmltext = "<html><head><body>I have nothing to say to you.</body></html>"
		st.set("cond","0")
		st.setState(STARTED)
		return "1.htm"

QUEST       = Quest(1010,"1010_enchange_mammon","Blacksmith")
CREATED     = State('Start',     QUEST)
STARTED     = State('Started',   QUEST)
COMPLETED   = State('Completed', QUEST)

QUEST.setInitialState(CREATED)

QUEST.addStartNpc(8126)

STARTED.addTalkId(8126)
