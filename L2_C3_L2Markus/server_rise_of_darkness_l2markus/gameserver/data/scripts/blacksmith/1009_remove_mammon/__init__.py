import sys
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest
from net.sf.l2j.gameserver.datatables import ItemTable
 

# Weapon enhancement definition  WeaponID:[BasicIconName, [[BasicItemName, newWeaponID, MaterialID, MaterialQuant]]]
 
EnhanceList={
# Bows'
5608:["weapon_carnium_bow_i", [["Light", 288, 5575, 109000]]], 
5609:["weapon_carnium_bow_i", [["Critical Bleed", 288, 5575, 109000]]], 
5610:["weapon_carnium_bow_i", [["Mana Up", 288, 5575, 109000]]], 
5611:["weapon_soul_bow_i", [["Cheap Shot", 289, 5575, 109000]]], 
5612:["weapon_soul_bow_i", [["Quick Recovery", 289, 5575, 109000]]], 
5613:["weapon_soul_bow_i", [["Critical Poison", 289, 5575, 109000]]], 
# Swords'
5635:["weapon_tallum_blade_i", [["Critical Poison", 80, 5575, 109000]]], 
5636:["weapon_tallum_blade_i", [["Haste", 80, 5575, 109000]]], 
5637:["weapon_tallum_blade_i", [["Anger", 80, 5575, 109000]]], 
5638:["weapon_elemental_sword_i", [["Magic Power", 150, 5575, 109000]]], 
5639:["weapon_elemental_sword_i", [["Magic Paralyze", 150, 5575, 109000]]], 
5640:["weapon_elemental_sword_i", [["Magic Empower", 150, 5575, 109000]]], 
5641:["weapon_sword_of_miracle_i", [["Magic Power", 151, 5575, 109000]]], 
5642:["weapon_sword_of_miracle_i", [["Magic Silence", 151, 5575, 109000]]], 
5643:["weapon_sword_of_miracle_i", [["Acumen", 151, 5575, 109000]]], 
5644:["weapon_dragon_slayer_i", [["Health", 81, 5575, 109000]]], 
5645:["weapon_dragon_slayer_i", [["Critical Bleed", 81, 5575, 109000]]], 
5646:["weapon_dragon_slayer_i", [["Critical Drain", 81, 5575, 109000]]], 
5647:["weapon_dark_legions_edge_i", [["Critical Damage", 2500, 5575, 109000]]], 
5648:["weapon_dark_legions_edge_i", [["Health", 2500, 5575, 109000]]], 
5649:["weapon_dark_legions_edge_i", [["Rsk. Focus", 2500, 5575, 109000]]], 
# Blunts'
5599:["weapon_meteor_shower_i", [["Focus", 2504, 5575, 109000]]], 
5600:["weapon_meteor_shower_i", [["Critical Bleed", 2504, 5575, 109000]]], 
5601:["weapon_meteor_shower_i", [["Rsk. Haste", 2504, 5575, 109000]]], 
5596:["weapon_dasparions_staff_i", [["Mana Up", 212, 5575, 109000]]], 
5597:["weapon_dasparions_staff_i", [["Conversion", 212, 5575, 109000]]], 
5598:["weapon_dasparions_staff_i", [["Acumen", 212, 5575, 109000]]], 
5605:["weapon_worldtrees_branch_i", [["Conversion", 213, 5575, 109000]]], 
5606:["weapon_worldtrees_branch_i", [["Magic Damage", 213, 5575, 109000]]], 
5607:["weapon_worldtrees_branch_i", [["Acumen", 213, 5575, 109000]]], 
5602:["weapon_elysian_i", [["Health", 164, 5575, 109000]]], 
5603:["weapon_elysian_i", [["Anger", 164, 5575, 109000]]], 
5604:["weapon_elysian_i", [["Critical Drain", 164, 5575, 109000]]], 
# Dagger'
5614:["weapon_bloody_orchid_i", [["Focus", 235, 5575, 109000]]], 
5615:["weapon_bloody_orchid_i", [["Back Blow", 235, 5575, 109000]]], 
5616:["weapon_bloody_orchid_i", [["Critical Bleed", 235, 5575, 109000]]], 
5617:["weapon_soul_separator_i", [["Guidance", 236, 5575, 109000]]], 
5618:["weapon_soul_separator_i", [["Critical Damage", 236, 5575, 109000]]], 
5619:["weapon_soul_separator_i", [["Rsk. Haste", 236, 5575, 109000]]], 
# Poleaxe'
5626:["weapon_halbard_i", [["Haste", 98, 5575, 109000]]], 
5627:["weapon_halbard_i", [["Critical Stun", 98, 5575, 109000]]], 
5628:["weapon_halbard_i", [["Wide Blow", 98, 5575, 109000]]], 
5632:["weapon_tallum_glaive_i", [["Guidance", 305, 5575, 109000]]], 
5633:["weapon_tallum_glaive_i", [["Health", 305, 5575, 109000]]], 
5634:["weapon_tallum_glaive_i", [["Wide Blow", 305, 5575, 109000]]], 
# Fist'
5620:["weapon_blood_tornado_i", [["Haste", 269, 5575, 109000]]], 
5621:["weapon_blood_tornado_i", [["Focus", 269, 5575, 109000]]], 
5622:["weapon_blood_tornado_i", [["Anger", 269, 5575, 109000]]], 
5623:["weapon_dragon_grinder_i", [["Rsk. Evasion", 270, 5575, 109000]]], 
5624:["weapon_dragon_grinder_i", [["Guidance", 270, 5575, 109000]]], 
5625:["weapon_dragon_grinder_i", [["Health", 270, 5575, 109000]]], 
# Stormbringer'
4681:["weapon_stormbringer_i", [["Critical Anger", 72, 5575, 14550]]], 
4682:["weapon_stormbringer_i", [["Focus", 72, 5575, 14550]]], 
4683:["weapon_stormbringer_i", [["Light", 72, 5575, 14550]]], 
# Shamshir'
4684:["weapon_Shamshir_i", [["Guidance", 73, 5575, 14550]]], 
4685:["weapon_Shamshir_i", [["Back Blow", 73, 5575, 14550]]], 
4686:["weapon_Shamshir_i", [["Rsk. Evasion", 73, 5575, 14550]]], 
# Katana'
4687:["weapon_Katana_i", [["Focus", 74, 5575, 14550]]], 
4688:["weapon_Katana_i", [["Critical Damage", 74, 5575, 14550]]], 
4689:["weapon_Katana_i", [["Haste", 74, 5575, 14550]]], 
# Spirit Sword'
4690:["weapon_spirits_sword_i", [["Critical Damage", 131, 5575, 14550]]], 
4691:["weapon_spirits_sword_i", [["Spirit Sword Critical Poison", 131, 5575, 14550]]], 
4692:["weapon_spirits_sword_i", [["Spirit Sword Haste", 131, 5575, 14550]]], 
# Raid Sword'
4693:["weapon_Raid_Sword_i", [["Focus", 133, 5575, 14550]]], 
4694:["weapon_Raid_Sword_i", [["Critical Drain", 133, 5575, 14550]]], 
4695:["weapon_Raid_Sword_i", [["Critical Poison", 133, 5575, 14550]]], 
# Caliburs'
4696:["weapon_Caliburs_i", [["Guidance", 77, 5575, 14550]]], 
4697:["weapon_Caliburs_i", [["Focus", 77, 5575, 14550]]], 
4698:["weapon_Caliburs_i", [["Critical Damage", 77, 5575, 14550]]], 
# Sword of Delusion'
4699:["weapon_Sword_of_Delusion_i", [["Focus", 76, 5575, 14550]]], 
4700:["weapon_Sword_of_Delusion_i", [["Health", 76, 5575, 14550]]], 
4701:["weapon_Sword_of_Delusion_i", [["Rsk. Haste", 76, 5575, 14550]]], 
# Tsurugi'
4702:["weapon_Tsurugi_i", [["Focus", 77, 5575, 14550]]], 
4703:["weapon_Tsurugi_i", [["Critical Damage", 77, 5575, 14550]]], 
4704:["weapon_Tsurugi_i", [["Haste", 77, 5575, 14550]]], 
# Sword of Nightmare'
4705:["weapon_Sword_of_Nightmare_i", [["Health", 134, 5575, 14550]]], 
4706:["weapon_Sword_of_Nightmare_i", [["Focus", 134, 5575, 14550]]], 
4707:["weapon_Sword_of_Nightmare_i", [["Light", 134, 5575, 14550]]], 
# Samurai Longsword'
4708:["weapon_Samurai_Longsword_i", [["Focus", 135, 5575, 14550]]], 
4709:["weapon_Samurai_Longsword_i", [["Critical Damage", 135, 5575, 14550]]], 
4710:["weapon_Samurai_Longsword_i", [["Haste", 135, 5575, 14550]]], 
# Flamberge'
4711:["weapon_Flamberge_i", [["Critical Damage", 71, 5575, 14550]]], 
4712:["weapon_Flamberge_i", [["Focus", 71, 5575, 14550]]], 
4713:["weapon_Flamberge_i", [["Light", 71, 5575, 14550]]], 
# Big Hammer'
4726:["weapon_Big_Hammer_i", [["Health", 89, 5575, 14550]]], 
4727:["weapon_Big_Hammer_i", [["Rsk.Focus", 89, 5575, 14550]]], 
4728:["weapon_Big_Hammer_i", [["Haste", 89, 5575, 14550]]], 
# Battle Axe'
4729:["weapon_Battle_Axe_i", [["Anger", 160, 5575, 14550]]], 
4730:["weapon_Battle_Axe_i", [["Rsk.Focus", 160, 5575, 14550]]], 
4731:["weapon_Battle_Axe_i", [["Haste", 160, 5575, 14550]]], 
# Silver Axe'
4732:["weapon_war_pick_i", [["Anger", 161, 5575, 14550]]], 
4733:["weapon_war_pick_i", [["Rsk.Focus", 161, 5575, 14550]]], 
4734:["weapon_war_pick_i", [["Haste", 161, 5575, 14550]]], 
# Skull Graver'
4735:["weapon_Skull_Graver_i", [["Anger", 173, 5575, 14550]]], 
4736:["weapon_Skull_Graver_i", [["Health", 173, 5575, 14550]]], 
4737:["weapon_Skull_Graver_i", [["Rsk.Focus", 173, 5575, 14550]]], 
# Dwarven War Hammer'
4738:["weapon_dwarven_warhammer_i", [["Anger", 2502, 5575, 14550]]], 
4739:["weapon_dwarven_warhammer_i", [["Health", 2502, 5575, 14550]]], 
4740:["weapon_dwarven_warhammer_i", [["Haste", 2502, 5575, 14550]]], 
# War Axe'
4741:["weapon_War_Axe_i", [["Anger", 162, 5575, 14550]]], 
4742:["weapon_War_Axe_i", [["Health", 162, 5575, 14550]]], 
4743:["weapon_War_Axe_i", [["Haste", 162, 5575, 14550]]], 
# Yaksa Mace'
4744:["weapon_yaksa_mace_i", [["Anger", 2503, 5575, 14550]]], 
4745:["weapon_yaksa_mace_i", [["Health", 2503, 5575, 14550]]], 
4746:["weapon_yaksa_mace_i", [["Rsk. Focus", 2503, 5575, 14550]]], 
# Cursed Dagger'
4759:["weapon_Cursed_Dagger_i", [["Critical Bleed", 226, 5575, 14550]]], 
4760:["weapon_Cursed_Dagger_i", [["Critical Poison", 226, 5575, 14550]]], 
4761:["weapon_Cursed_Dagger_i", [["Rsk. Haste", 226, 5575, 14550]]], 
# Dark Elven Dagger'
4762:["weapon_darkelven_dagger_i", [["Focus", 232, 5575, 14550]]], 
4763:["weapon_darkelven_dagger_i", [["Back Blow", 232, 5575, 14550]]], 
4764:["weapon_darkelven_dagger_i", [["Might Mortal", 232, 5575, 14550]]], 
# Stiletto'
4765:["weapon_Stiletto_i", [["Critical Bleed", 227, 5575, 14550]]], 
4766:["weapon_Stiletto_i", [["Critical Poison", 227, 5575, 14550]]], 
4767:["weapon_Stiletto_i", [["Might Mortal", 227, 5575, 14550]]], 
# Grace Dagger'
4768:["weapon_Grace_Dagger_i", [["Evasion", 231, 5575, 14550]]], 
4769:["weapon_Grace_Dagger_i", [["Focus", 231, 5575, 14550]]], 
4770:["weapon_Grace_Dagger_i", [["Back Blow", 231, 5575, 14550]]], 
# Dark Screamer'
4771:["weapon_Dark_Screamer_i", [["Evasion", 233, 5575, 14550]]], 
4772:["weapon_Dark_Screamer_i", [["Focus", 233, 5575, 14550]]], 
4773:["weapon_Dark_Screamer_i", [["Critical Bleed", 233, 5575, 14550]]], 
# Crystal Dagger'
4774:["weapon_Crystal_Dagger_i", [["Critical Bleed", 228, 5575, 14550]]], 
4775:["weapon_Crystal_Dagger_i", [["Critical Poison", 228, 5575, 14550]]], 
4776:["weapon_Crystal_Dagger_i", [["Might Mortal", 228, 5575, 14550]]], 
# Chakram'
4789:["weapon_Chakram_i", [["Critical Drain", 263, 5575, 14550]]], 
4790:["weapon_Chakram_i", [["Critical Poison", 263, 5575, 14550]]], 
4791:["weapon_Chakram_i", [["Rsk. Haste", 263, 5575, 14550]]], 
# Fist Blade'
4792:["weapon_Fist_Blade_i", [["Rsk. Evasion", 265, 5575, 14550]]], 
4793:["weapon_Fist_Blade_i", [["Rsk. Haste", 265, 5575, 14550]]], 
4794:["weapon_Fist_Blade_i", [["Haste", 265, 5575, 14550]]], 
# Great Pata'
4795:["weapon_Great_Pata_i", [["Critical Drain", 266, 5575, 14550]]], 
4796:["weapon_Great_Pata_i", [["Critical Poison", 266, 5575, 14550]]], 
4797:["weapon_Great_Pata_i", [["Rsk. Haste", 266, 5575, 14550]]], 
# Knuckle Duster'
4798:["weapon_knuckle_dust_i", [["Rsk. Evasion", 4233, 5575, 14550]]], 
4799:["weapon_knuckle_dust_i", [["Rsk. Haste", 4233, 5575, 14550]]], 
4800:["weapon_knuckle_dust_i", [["Haste", 4233, 5575, 14550]]], 
# Crystallized Ice Bow'
4810:["weapon_Crystallized_Ice_Bow_i", [["Guidance", 281, 5575, 14550]]], 
4811:["weapon_Crystallized_Ice_Bow_i", [["Evasion", 281, 5575, 14550]]], 
4812:["weapon_Crystallized_Ice_Bow_i", [["Quick Recovery", 281, 5575, 14550]]], 
# Elemental Bow'
4813:["weapon_Elemental_Bow_i", [["Guidance", 282, 5575, 14550]]], 
4814:["weapon_Elemental_Bow_i", [["Miser", 282, 5575, 14550]]], 
4815:["weapon_Elemental_Bow_i", [["Quick Recovery", 282, 5575, 14550]]], 
# Akat Long Bow'
4819:["weapon_Akat_Long_Bow_i", [["Guidance", 283, 5575, 14550]]], 
4820:["weapon_Akat_Long_Bow_i", [["Evasion", 283, 5575, 14550]]], 
4821:["weapon_Akat_Long_Bow_i", [["Miser", 283, 5575, 14550]]], 
# Eminence Bow'
4822:["weapon_Eminence_Bow_i", [["Guidance", 286, 5575, 14550]]], 
4823:["weapon_Eminence_Bow_i", [["Miser", 286, 5575, 14550]]], 
4824:["weapon_Eminence_Bow_i", [["Cheap Shot", 286, 5575, 14550]]], 
# Scythe'
4834:["weapon_Scythe_i", [["Anger", 96, 5575, 14550]]], 
4835:["weapon_Scythe_i", [["Critical Stun", 96, 5575, 14550]]], 
4836:["weapon_Scythe_i", [["Light", 96, 5575, 14550]]], 
# Orcish Glaive'
4837:["weapon_Orcish_Glaive_i", [["Anger", 298, 5575, 14550]]], 
4838:["weapon_Orcish_Glaive_i", [["Critical Stun", 298, 5575, 14550]]], 
4839:["weapon_Orcish_Glaive_i", [["Long Blow", 298, 5575, 14550]]], 
# Body Slasher'
4840:["weapon_Body_Slasher_i", [["Critical Stun", 302, 5575, 14550]]], 
4841:["weapon_Body_Slasher_i", [["Long Blow", 302, 5575, 14550]]], 
4842:["weapon_Body_Slasher_i", [["Wide Blow", 302, 5575, 14550]]], 
# Bec de Corbin'
4843:["weapon_bech_de_corbin_i", [["Critical Stun", 94, 5575, 14550]]], 
4844:["weapon_bech_de_corbin_i", [["Long Blow", 94, 5575, 14550]]], 
4845:["weapon_bech_de_corbin_i", [["Light", 94, 5575, 14550]]], 
# Scorpion'
4846:["weapon_Scorpion_i", [["Anger", 301, 5575, 14550]]], 
4847:["weapon_Scorpion_i", [["Critical Stun", 301, 5575, 14550]]], 
4848:["weapon_Scorpion_i", [["Long Blow", 301, 5575, 14550]]], 
# Widow Maker'
4849:["weapon_Widow_Maker_i", [["Critical Stun", 303, 5575, 14550]]], 
4850:["weapon_Widow_Maker_i", [["Long Blow", 303, 5575, 14550]]], 
4851:["weapon_Widow_Maker_i", [["Wide Blow", 303, 5575, 14550]]], 
# Orcish Poleaxe'
4852:["weapon_Orcish_Poleaxe_i", [["Critical Stun", 299, 5575, 14550]]], 
4853:["weapon_Orcish_Poleaxe_i", [["Long Blow", 299, 5575, 14550]]], 
4854:["weapon_Orcish_Poleaxe_i", [["Wide Blow", 299, 5575, 14550]]], 
# Heavy Doom Hammer'
4864:["weapon_Heavy_Doom_Hammer_i", [["Magic Regeneration", 191, 5575, 14550]]], 
4865:["weapon_Heavy_Doom_Hammer_i", [["Mental Shield", 191, 5575, 14550]]], 
4866:["weapon_Heavy_Doom_Hammer_i", [["Magic Hold", 191, 5575, 14550]]], 
# Crystal Staff'
4867:["weapon_Crystal_Staff_i", [["Rsk. Evasion", 192, 5575, 14550]]], 
4868:["weapon_Crystal_Staff_i", [["Mana Up", 192, 5575, 14550]]], 
4869:["weapon_Crystal_Staff_i", [["Bodily Blessing", 192, 5575, 14550]]], 
# Heavy Doom Axe'
4870:["weapon_Heavy_Doom_Axe_i", [["Magic Poison", 194, 5575, 14550]]], 
4871:["weapon_Heavy_Doom_Axe_i", [["Magic Weakness", 194, 5575, 14550]]], 
4872:["weapon_Heavy_Doom_Axe_i", [["Magic Chaos", 194, 5575, 14550]]], 
# Cursed Staff'
4873:["weapon_Cursed_Staff_i", [["Magic Hold", 195, 5575, 14550]]], 
4874:["weapon_Cursed_Staff_i", [["Magic Poison", 195, 5575, 14550]]], 
4875:["weapon_Cursed_Staff_i", [["Magic Weakness", 195, 5575, 14550]]], 
# Paradia Staff'
4876:["weapon_Paradia_Staff_i", [["Magic Regeneration", 197, 5575, 14550]]], 
4877:["weapon_Paradia_Staff_i", [["Mental Shield", 197, 5575, 14550]]], 
4878:["weapon_Paradia_Staff_i", [["Magic Hold", 197, 5575, 14550]]], 
# Paagrio Hammer'
4879:["weapon_Paagrio_Hammer_i", [["Rsk. Evasion", 199, 5575, 14550]]], 
4880:["weapon_Paagrio_Hammer_i", [["Magic Poison", 199, 5575, 14550]]], 
4881:["weapon_Paagrio_Hammer_i", [["Magic Weakness", 199, 5575, 14550]]], 
# Sage's Staff'
4882:["weapon_sages_staff_i", [["Magic Hold", 200, 5575, 14550]]], 
4883:["weapon_sages_staff_i", [["Magic Poison", 200, 5575, 14550]]], 
4884:["weapon_sages_staff_i", [["Magic Weakness", 200, 5575, 14550]]], 
# Paagrio Axe'
4885:["weapon_Paagrio_Axe_i", [["Mana Up", 203, 5575, 14550]]], 
4886:["weapon_Paagrio_Axe_i", [["Magic Weakness", 203, 5575, 14550]]], 
4887:["weapon_Paagrio_Axe_i", [["Magic Chaos", 203, 5575, 14550]]], 
# Deadman's Staff'
4888:["weapon_deadmans_staff_i", [["Magic Regeneration", 204, 5575, 14550]]], 
4889:["weapon_deadmans_staff_i", [["Mental Shield", 204, 5575, 14550]]], 
4890:["weapon_deadmans_staff_i", [["Magic Hold", 204, 5575, 14550]]], 
# Ghoul's Staff'
4891:["weapon_ghouls_staff_i", [["Rsk. Evasion", 205, 5575, 14550]]], 
4892:["weapon_ghouls_staff_i", [["Mana Up", 205, 5575, 14550]]], 
4893:["weapon_ghouls_staff_i", [["Bodily Blessing", 205, 5575, 14550]]], 
# Demon's Staff'
4894:["weapon_demons_staff_i", [["Magic Poison", 206, 5575, 14550]]], 
4895:["weapon_demons_staff_i", [["Magic Weakness", 206, 5575, 14550]]], 
4896:["weapon_demons_staff_i", [["Magic Chaos", 206, 5575, 14550]]], 
# Sword of Limit'
6307:["weapon_Sword_of_Limit_i", [["Guidance", 132, 5575, 14550]]], 
6308:["weapon_Sword_of_Limit_i", [["Critical Drain", 132, 5575, 14550]]], 
6309:["weapon_Sword_of_Limit_i", [["Health", 132, 5575, 14550]]], 
# Sword of Whispering Death'
6310:["weapon_Sword_of_Whispering_Death_i", [["Empower", 145, 5575, 14550]]], 
6311:["weapon_Sword_of_Whispering_Death_i", [["Magic Power", 145, 5575, 14550]]], 
6312:["weapon_Sword_of_Whispering_Death_i", [["Magic Silence", 145, 5575, 14550]]], 
# Homunkulus's Sword'
6313:["weapon_homunkuluss_sword_i", [["Acumen", 84, 5575, 14550]]], 
6314:["weapon_homunkuluss_sword_i", [["Conversion", 84, 5575, 14550]]], 
6315:["weapon_homunkuluss_sword_i", [["Magic Paralyze", 84, 5575, 14550]]], 
# Berserker Blade'
6347:["weapon_Berserker_Blade_i", [["Focus", 5286, 5575, 14550]]], 
6348:["weapon_Berserker_Blade_i", [["Critical Damage", 5286, 5575, 14550]]], 
6349:["weapon_Berserker_Blade_i", [["Haste", 5286, 5575, 14550]]], 
# Keshanberk'
4714:["weapon_kshanberk_i", [["Guidance", 142, 5575, 35700]]], 
4715:["weapon_kshanberk_i", [["Focus", 142, 5575, 35700]]], 
4716:["weapon_kshanberk_i", [["Back Blow", 142, 5575, 35700]]], 
# Sword of Damascus'
4717:["weapon_Sword_of_Damascus_i", [["Focus", 79, 5575, 35700]]], 
4718:["weapon_Sword_of_Damascus_i", [["Critical Damage", 79, 5575, 35700]]], 
4719:["weapon_Sword_of_Damascus_i", [["Haste", 79, 5575, 35700]]], 
# Great Sword'
4723:["weapon_Great_Sword_i", [["Health", 78, 5575, 35700]]], 
4724:["weapon_Great_Sword_i", [["Critical Damage", 78, 5575, 35700]]], 
4725:["weapon_Great_Sword_i", [["Focus", 78, 5575, 35700]]], 
# Heavy War Axe'
4747:["weapon_Heavy_War_Axe_i", [["Anger", 91, 5575, 35700]]], 
4748:["weapon_Heavy_War_Axe_i", [["Health", 91, 5575, 35700]]], 
4749:["weapon_Heavy_War_Axe_i", [["Rsk. Focus", 91, 5575, 35700]]], 
# Deadman's Glory'
4750:["weapon_deadmans_staff_i", [["Anger", 171, 5575, 35700]]], 
4751:["weapon_deadmans_staff_i", [["Health", 171, 5575, 35700]]], 
4752:["weapon_deadmans_staff_i", [["Haste", 171, 5575, 35700]]], 
# Art of Battle Axe'
4753:["weapon_Art_of_Battle_Axe_i", [["Health", 175, 5575, 35700]]], 
4754:["weapon_Art_of_Battle_Axe_i", [["Rsk. Focus", 175, 5575, 35700]]], 
4755:["weapon_Art_of_Battle_Axe_i", [["Haste", 175, 5575, 35700]]], 
# Kris'
4777:["weapon_Kris_i", [["Evasion", 229, 5575, 35700]]], 
4778:["weapon_Kris_i", [["Focus", 229, 5575, 35700]]], 
4779:["weapon_Kris_i", [["Back Blow", 229, 5575, 35700]]], 
# Demon's Sword'
4780:["weapon_weapon_demons_sword_i", [["Critical Bleed", 234, 5575, 35700]]], 
4781:["weapon_weapon_demons_sword_i", [["Critical Poison", 234, 5575, 35700]]], 
4782:["weapon_weapon_demons_sword_i", [["Might Mortal", 234, 5575, 35700]]], 
# Hell Knife'
4786:["weapon_Hell_Knife_i", [["Focus", 243, 5575, 35700]]], 
4787:["weapon_Hell_Knife_i", [["Back Blow", 243, 5575, 35700]]], 
4788:["weapon_Hell_Knife_i", [["Might Mortal", 243, 5575, 35700]]], 
# Arthro Nail'
4801:["weapon_Arthro_Nail_i", [["Critical Poison", 267, 5575, 35700]]], 
4802:["weapon_Arthro_Nail_i", [["Rsk. Evasion", 267, 5575, 35700]]], 
4803:["weapon_Arthro_Nail_i", [["Rsk. Haste", 267, 5575, 35700]]], 
# Bellion Cestus'
4804:["weapon_Bellion_Cestus_i", [["Critical Drain", 268, 5575, 35700]]], 
4805:["weapon_Bellion_Cestus_i", [["Critical Poison", 268, 5575, 35700]]], 
4806:["weapon_Bellion_Cestus_i", [["Rsk. Haste", 268, 5575, 35700]]], 
# Dark Elven Long Bow'
4825:["weapon_dark_elven_long_bow_i", [["Evasion", 284, 5575, 35700]]], 
4826:["weapon_dark_elven_long_bow_i", [["Critical Bleed", 284, 5575, 35700]]], 
4827:["weapon_dark_elven_long_bow_i", [["Miser", 284, 5575, 35700]]], 
# Sprites Staff'
4897:["weapon_sprites_staff_i", [["Magic Regeneration", 92, 5575, 35700]]], 
4898:["weapon_sprites_staff_i", [["Mental Shield", 92, 5575, 35700]]], 
4899:["weapon_sprites_staff_i", [["Magic Hold", 92, 5575, 35700]]], 
# Bow of Peril'
4828:["weapon_hazard_bow_i", [["Guidance", 287, 5575, 35700]]], 
4829:["weapon_hazard_bow_i", [["Quick Recovery", 287, 5575, 35700]]], 
4830:["weapon_hazard_bow_i", [["Cheap Shot", 287, 5575, 35700]]], 
# Great Axe'
4855:["weapon_Great_Axe_i", [["Anger", 300, 5575, 35700]]], 
4856:["weapon_Great_Axe_i", [["Critical Stun", 300, 5575, 35700]]], 
4857:["weapon_Great_Axe_i", [["Light", 300, 5575, 35700]]], 
# Lance'
4858:["weapon_lancia_i", [["Anger", 97, 5575, 35700]]], 
4859:["weapon_lancia_i", [["Critical Stun", 97, 5575, 35700]]], 
4860:["weapon_lancia_i", [["Long Blow", 97, 5575, 35700]]], 
# Staff of Evil Spirit'
4900:["weapon_staff_of_evil_sprit_i", [["Magic Focus", 210, 5575, 35700]]], 
4901:["weapon_staff_of_evil_sprit_i", [["Bodily Blessing", 210, 5575, 35700]]], 
4902:["weapon_staff_of_evil_sprit_i", [["Magic Poison", 210, 5575, 35700]]], 
}
 
 
############################################################## DO NOT MODIFY BELOW THIS LINE #####################################################################################
 
 
def getItemName(Item):
    ItemName = Item.getItem().getName()
    if Item.getEnchantLevel() > 0: 
        ItemName = "+" + str(Item.getEnchantLevel()) + " " + ItemName
    return ItemName
 
 
def getMaterialName(MaterialID):
    return "Ancient Adena"
 
    
def getMaterialIcon(MaterialID):
    return "etc_ancient_adena_i00"
 
 
# Main Code
class Quest (JQuest) :
 
 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)
 
 def onEvent (self,event,st) :
    htmltext = event
    # shows you how much materials you need to enhance, ok button to go forward, too
    if event.startswith("2_"):
        reqEnh = event.replace("2_", "").split(".")
        ObjectID = int(reqEnh[0])
        EnhancID = int(reqEnh[1])
        Item = st.getPlayer().getInventory().getItemByObjectId(ObjectID)
        if Item.getItemId() in EnhanceList:
            Icon, Enhancements = EnhanceList[Item.getItemId()]
            Name, WeaponID, MaterialID, MaterialQuant = Enhancements[EnhancID]
            htmltext = st.showHtmlFile("2.htm")
            return htmltext.replace("<WeaponName>", Name)\
                .replace("<WeaponIcon>", Icon+"00")\
                .replace("<SaWeaponIcon>", Icon+"01")\
                .replace("<SaWeaponName>", getItemName(Item))\
                .replace("<MaterialName>", getMaterialName(MaterialID))\
                .replace("<MaterialIcon>", getMaterialIcon(MaterialID))\
                .replace("<MaterialQuantity>", str(MaterialQuant))\
                .replace("<EventOK>", "3_" + str(Item.getObjectId()) + "." + str(EnhancID))
    
    # this handles the whole enhance stuff with objectIds, not ItemIds... no html shows up.. just work and socket return
    elif event.startswith("3_"):
        reqEnh = event.replace("3_", "").split(".")
        ObjectID = int(reqEnh[0])
        EnhancID = int(reqEnh[1])
        Item = st.getPlayer().getInventory().getItemByObjectId(ObjectID)
        if Item.getItemId() in EnhanceList:
            Icon, Enhancements = EnhanceList[Item.getItemId()]
            Name, WeaponID, MaterialID, MaterialQuant = Enhancements[EnhancID]
            if st.getQuestItemsCount(MaterialID) >= MaterialQuant :
                EnchantLevel = Item.getEnchantLevel()
                st.getPlayer().destroyItem("remove_1009",ObjectID, 1,st.getPlayer(), 0)
                NewItem = ItemTable.getInstance().createItem("enhance",WeaponID, 1,st.getPlayer())
                NewItem.setEnchantLevel(EnchantLevel)
                st.getPlayer().addItem("enhance", NewItem, st.getPlayer(), 0)
                st.takeItems(MaterialID, MaterialQuant)
                htmltext = "Enhancement has been successfully removed!"
            else :
                htmltext = "You do not have enough materials."
    
    # if event is 0, or has a bug... trade is canceled
    else :
        htmltext = "Trade has been cancelled."
    st.setState(COMPLETED)
    st.exitQuest(1)
    return htmltext
    
    
# this just return new html, if the player can talk with this npc about that enhance stuff
 def onTalk (self,npc,st):
   st.set("cond","0")
   st.setState(STARTED)
   htmltext = ""
   for Item in st.getPlayer().getInventory().getItems():
       if Item.getItemId() in EnhanceList and not Item.isEquipped():
           Icon, Enhancements = EnhanceList[Item.getItemId()]
           EnhancID = 0
           for Name, WeaponID, MaterialID, MaterialQuant in Enhancements:
                    htmltext += "<tr>\n<td width=35><img src=\"icon." + Icon + "01\" width=32 height=32 align=\"left\"></td>\n" \
                        "<td width=835><table border=0 width=\"835\">\n<tr><td><a action=\"bypass -h Quest 1009_remove_mammon 2_" + str(Item.getObjectId()) + "." + str(EnhancID) + "\">" + getItemName(Item) + ": " + Name + "</a></td></tr>\n" \
                        "<tr><td><font color=\"B09878\">Remove Enhancement</font></td></tr></table></td>\n</tr>"
                    EnhancID += 1
   if htmltext == "": 
       htmltext = "<tr><td>You have no weapons with special abilities in your inventory</td></tr>"
   htmltext = "<html><body>\nList:\n<left>\n<table width=870 border=0>\n" + htmltext + "</table>\n<br></left></body></html>"
   return htmltext
 
 
QUEST       = Quest(1009,"1009_remove_mammon","Blacksmith")
CREATED     = State('Start',     QUEST)
STARTED     = State('Started',   QUEST)
COMPLETED   = State('Completed', QUEST)
 
 
QUEST.setInitialState(CREATED)
 
 
# init all npc to the correct stats
for npcId in [8092]:
        QUEST.addStartNpc(npcId)
        STARTED.addTalkId(npcId)
        
# always at the end, then it shows only up if anything is correct in the code.. no jython error.. because we cant check jython errors with idle