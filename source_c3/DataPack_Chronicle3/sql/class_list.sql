#----------------------------
# Table structure for class_list
#----------------------------
DROP TABLE IF EXISTS class_list;
CREATE TABLE `class_list` (
  `class_name` varchar(19) NOT NULL default '',
  `id` int(10) unsigned NOT NULL default '0',
  `parent_id` int(11) NOT NULL default '0',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM;
#----------------------------
# Records for table class_list
#----------------------------


INSERT INTO `class_list` VALUES
('H_Fighter', 0, -1), 
('H_Warrior', 1, 0), 
('H_Gladiator', 2, 1),
('H_Warlord', 3, 1), 
('H_Knight', 4, 0), 
('H_Paladin', 5, 4), 
('H_DarkAvenger', 6, 4), 
('H_Rogue', 7, 0), 
('H_TreasureHunter', 8, 7),
('H_Hawkeye', 9, 7),
('H_Mage', 10, -1), 
('H_Wizard', 11, 10), 
('H_Sorceror', 12, 11),
('H_Necromancer', 13, 11),
('H_Warlock', 14, 11), 
('H_Cleric', 15, 10), 
('H_Bishop', 16, 15),
('H_Prophet', 17, 15),
('E_Fighter', 18, -1), 
('E_Knight', 19, 18), 
('E_TempleKnight', 20, 19),
('E_SwordSinger', 21, 19), 
('E_Scout', 22, 18), 
('E_PlainsWalker', 23, 22),
('E_SilverRanger', 24, 22),  
('E_Mage', 25, -1), 
('E_Wizard', 26, 25), 
('E_SpellSinger', 27, 26), 
('E_ElementalSummoner', 28, 26), 
('E_Oracle', 29, 25), 
('E_Elder', 30, 29),  
('DE_Fighter', 31, -1), 
('DE_PaulusKnight', 32, 31), 
('DE_ShillienKnight', 33, 32), 
('DE_BladeDancer', 34, 32),  
('DE_Assassin', 35, 31), 
('DE_AbyssWalker', 36, 35),  
('DE_PhantomRanger', 37, 35),  
('DE_Mage', 38, -1), 
('DE_DarkWizard', 39, 38), 
('DE_Spellhowler', 40, 39),  
('DE_PhantomSummoner', 41, 39),  
('DE_ShillienOracle', 42, 38), 
('DE_ShillienElder', 43, 42),  
('O_Fighter', 44, -1), 
('O_Raider', 45, 44), 
('O_Destroyer', 46, 45),  
('O_Monk', 47, 44), 
('O_Tyrant', 48, 47),  
('O_Mage', 49, -1), 
('O_Shaman', 50, 49), 
('O_Overlord', 51, 50),  
('O_Warcryer', 52, 50),  
('D_Fighter', 53, -1), 
('D_Scavenger', 54, 53), 
('D_BountyHunter', 55, 54),  
('D_Artisan', 56, 53), 
('D_Warsmith', 57, 56);