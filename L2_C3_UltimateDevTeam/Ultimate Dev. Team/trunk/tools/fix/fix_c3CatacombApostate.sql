-- Author: Pivi_CT (Akatsuki Server) 
-- Link: http://www.l2db.net 
-- Catacomb: Apostate 50/60 

-- Lilim Magus 1176 
UPDATE npc SET level = 51, attackrange = 40, hp = 9292, mp = 861, exp = 12405, sp = 952, patk = 535, pdef = 287, matk = 282, mdef = 233 WHERE id = 1176; 

-- Crypt Archon 1244 
UPDATE npc SET level = 50, attackrange = 40, hp = 8980, mp = 830, exp = 12000, sp = 904, patk = 556, pdef = 270, matk = 264, mdef = 226 WHERE id = 1244; 

-- Nephilim Priest 1197 
UPDATE npc SET level = 51, attackrange = 40, hp = 9292, mp = 861, exp = 12405, sp = 952, patk = 535, pdef = 287, matk = 282, mdef = 233 WHERE id = 1197; 

-- Crypt Inquisitor 1245 
UPDATE npc SET level = 52, attackrange = 40, hp = 9916, mp = 892, exp = 12979, sp = 1004, patk = 622, pdef = 295, matk = 300, mdef = 240 WHERE id = 1245; 

-- Lilim Marauder 1178 
UPDATE npc SET level = 57, attackrange = 40, hp = 11228, mp = 1052, exp = 15595, sp = 1296, patk = 811, pdef = 340, matk = 405, mdef = 277 WHERE id = 1178; 

-- Nephilim Guard 1199 
UPDATE npc SET level = 57, attackrange = 40, hp = 11228, mp = 1052, exp = 15595, sp = 1296, patk = 811, pdef = 340, matk = 405, mdef = 277 WHERE id = 1199; 

-- Purgatory Wisp 1152 
UPDATE npc SET level = 53, attackrange = 40, hp = 10248, mp = 924, exp = 13483, sp = 1060, patk = 617, pdef = 314, matk = 320, mdef = 247 WHERE id = 1152; 

-- Purgatory Serpent 1153 
UPDATE npc SET level = 56, attackrange = 40, hp = 11252, mp = 1020, exp = 15053, sp = 1236, patk = 771, pdef = 331, matk = 382, mdef = 269 WHERE id = 1153; 

-- Lilim Priest 1179 
UPDATE npc SET level = 60, attackrange = 40, hp = 12220, mp = 1151, exp = 17280, sp = 1500, patk = 939, pdef = 369, matk = 479, mdef = 300 WHERE id = 1179; 

-- Nephilim Bishop 1200 
UPDATE npc SET level = 60, attackrange = 40, hp = 12220, mp = 1151, exp = 17280, sp = 1500, patk = 939, pdef = 369, matk = 479, mdef = 300 WHERE id = 1200; 

-- Purgatory Conjurer 1155 
UPDATE npc SET level = 59, attackrange = 40, hp = 12272, mp = 1118, exp = 16709, sp = 1428, patk = 895, pdef = 359, matk = 453, mdef = 292 WHERE id = 1155; 

-- Hell Keeper Medusa 1154 
UPDATE npc SET level = 58, attackrange = 40, hp = 11556, mp = 1085, exp = 16147, sp = 1364, patk = 852, pdef = 350, matk = 429, mdef = 284 WHERE id = 1154; 

-- Tomb Archon 1246 
UPDATE npc SET level = 55, attackrange = 40, hp = 10572, mp = 988 , exp = 14520, sp = 1172, patk = 732, pdef = 322, matk = 361, mdef = 262 WHERE id = 1246; 

-- Tomb Inquisitor 1247 
UPDATE npc SET level = 57, attackrange = 40, hp = 11588, mp = 1052, exp = 15595, sp = 1296, patk = 811, pdef = 340, matk = 405, mdef = 277 WHERE id = 1247; 

-- Nephilim Swordsman 1198 
UPDATE npc SET level = 54, attackrange = 40, hp = 11684, mp = 956, exp = 13997, sp = 1116, patk = 694, pdef = 313, matk = 340, mdef = 254 WHERE id = 1198; 

-- Lilim Knight Errant 1177 
UPDATE npc SET level = 54, attackrange = 40, hp = 10248, mp = 956, exp = 13997, sp = 1116, patk = 694, pdef = 313, matk = 340, mdef = 254 WHERE id = 1177; 

-- Pagan Watcher Cerberon 10280 RAID BOSS 
UPDATE npc SET level = 55, attackrange = 40, hp = 377912, mp = 2490, exp = 3360, sp = 293, patk = 732, pdef = 322, matk = 361, mdef = 1313 WHERE id = 10280;

DELETE FROM `spawnlist` WHERE (id >= 3003391 AND id <= 3003620);

insert into spawnlist values 
(1249015637, "ApostateCatacomb", 1, 1176, 82385, 78419, -5128, 0, 0, 32025, 60, 0), 
(1249015821, "ApostateCatacomb", 1, 1198, 80054, 84168, -5130, 0, 0, 17010, 60, 0), 
(1249015817, "ApostateCatacomb", 1, 1198, 79540, 85156, -5130, 0, 0, 39648, 60, 0), 
(1249015818, "ApostateCatacomb", 1, 1177, 79565, 84822, -5151, 0, 0, 32002, 60, 0), 
(1249015819, "ApostateCatacomb", 1, 1246, 79700, 84475, -5130, 0, 0, 14451, 60, 0), 
(1249015820, "ApostateCatacomb", 1, 1247, 79832, 84157, -5128, 0, 0, 8192, 60, 0), 
(1249015814, "ApostateCatacomb", 1, 1177, 80110, 87903, -5130, 0, 0, 51588, 60, 0), 
(1249015815, "ApostateCatacomb", 1, 1246, 79770, 85506, -5130, 0, 0, 16021, 60, 0), 
(1249015816, "ApostateCatacomb", 1, 1247, 79990, 85552, -5128, 0, 0, 17052, 60, 0), 
(1249015810, "ApostateCatacomb", 1, 1246, 80090, 87542, -5130, 0, 0, 57849, 60, 0), 
(1249015811, "ApostateCatacomb", 1, 1246, 80355, 88418, -5130, 0, 0, 41869, 60, 0), 
(1249015812, "ApostateCatacomb", 1, 1247, 80224, 88665, -5128, 0, 0, 36818, 60, 0), 
(1249015813, "ApostateCatacomb", 1, 1198, 80082, 88276, -5130, 0, 0, 54639, 60, 0), 
(1249015807, "ApostateCatacomb", 1, 1177, 80375, 88113, -5130, 0, 0, 61854, 60, 0), 
(1249015808, "ApostateCatacomb", 1, 1198, 80389, 87906, -5130, 0, 0, 55891, 60, 0), 
(1249015809, "ApostateCatacomb", 1, 1247, 80338, 87606, -5130, 0, 0, 47308, 60, 0), 
(1249015803, "ApostateCatacomb", 1, 1246, 82001, 87468, -5128, 0, 0, 38733, 60, 0), 
(1249015804, "ApostateCatacomb", 1, 1177, 82366, 87478, -5128, 0, 0, 31746, 60, 0), 
(1249015805, "ApostateCatacomb", 1, 1247, 82646, 87541, -5128, 0, 0, 12256, 60, 0), 
(1249015806, "ApostateCatacomb", 1, 1198, 82836, 87288, -5130, 0, 0, 33070, 60, 0), 
(1249015800, "ApostateCatacomb", 1, 1247, 82184, 87255, -5130, 0, 0, 46910, 60, 0), 
(1249015801, "ApostateCatacomb", 1, 1177, 81891, 87312, -5151, 0, 0, 30408, 60, 0), 
(1249015802, "ApostateCatacomb", 1, 1198, 81801, 87502, -5128, 0, 0, 27015, 60, 0), 
(1249015796, "ApostateCatacomb", 1, 1177, 81238, 84416, -5128, 0, 0, 15126, 60, 0), 
(1249015797, "ApostateCatacomb", 1, 1177, 81393, 84832, -5151, 0, 0, 13829, 60, 0), 
(1249015798, "ApostateCatacomb", 1, 1177, 81022, 85289, -5130, 0, 0, 23355, 60, 0), 
(1249015799, "ApostateCatacomb", 1, 1246, 82326, 87253, -5130, 0, 0, 56890, 60, 0), 
(1249015792, "ApostateCatacomb", 1, 1247, 81174, 85479, -5128, 0, 0, 21525, 60, 0), 
(1249015793, "ApostateCatacomb", 1, 1198, 81226, 85179, -5128, 0, 0, 18355, 60, 0), 
(1249015794, "ApostateCatacomb", 1, 1247, 81223, 84699, -5128, 0, 0, 17555, 60, 0), 
(1249015788, "ApostateCatacomb", 1, 1246, 81062, 84966, -5130, 0, 0, 7533, 60, 0), 
(1249015789, "ApostateCatacomb", 1, 1247, 81071, 84246, -5130, 0, 0, 1808, 60, 0), 
(1249015790, "ApostateCatacomb", 1, 1198, 81382, 84193, -5130, 0, 0, 8070, 60, 0), 
(1249015791, "ApostateCatacomb", 1, 1198, 81552, 84720, -5130, 0, 0, 11458, 60, 0), 
(1249015785, "ApostateCatacomb", 1, 1177, 82499, 84100, -5128, 0, 0, 59047, 60, 0), 
(1249015786, "ApostateCatacomb", 1, 1246, 81501, 85070, -5130, 0, 0, 1448, 60, 0), 
(1249015787, "ApostateCatacomb", 1, 1246, 81313, 85495, -5128, 0, 0, 15499, 60, 0), 
(1249015781, "ApostateCatacomb", 1, 1197, 82653, 82807, -5149, 0, 0, 55991, 60, 0), 
(1249015782, "ApostateCatacomb", 1, 1246, 82678, 83871, -5149, 0, 0, 49700, 60, 0), 
(1249015783, "ApostateCatacomb", 1, 1247, 82899, 84082, -5128, 0, 0, 6499, 60, 0), 
(1249015784, "ApostateCatacomb", 1, 1198, 82658, 84303, -5149, 0, 0, 25028, 60, 0), 
(1249015778, "ApostateCatacomb", 1, 1176, 82961, 83008, -5128, 0, 0, 1022, 60, 0), 
(1249015779, "ApostateCatacomb", 1, 1244, 82706, 83233, -5128, 0, 0, 23988, 60, 0), 
(1249015780, "ApostateCatacomb", 1, 1245, 82471, 83035, -5128, 0, 0, 39470, 60, 0), 
(1249015774, "ApostateCatacomb", 1, 1178, 83768, 83284, -5128, 0, 0, 16304, 60, 0), 
(1249015775, "ApostateCatacomb", 1, 1199, 83942, 83013, -5128, 0, 0, 38795, 60, 0), 
(1249015776, "ApostateCatacomb", 1, 1152, 83822, 82781, -5128, 0, 0, 45290, 60, 0), 
(1249015777, "ApostateCatacomb", 1, 1153, 83587, 82997, -5128, 0, 0, 23324, 60, 0), 
(1249015771, "ApostateCatacomb", 1, 1200, 83883, 84090, -5128, 0, 0, 33272, 60, 0), 
(1249015772, "ApostateCatacomb", 1, 1155, 83616, 84059, -5128, 0, 0, 33966, 60, 0), 
(1249015773, "ApostateCatacomb", 1, 1154, 83742, 83881, -5128, 0, 0, 55576, 60, 0), 
(1249015767, "ApostateCatacomb", 1, 1200, 83724, 86848, -5128, 0, 0, 50009, 60, 0), 
(1249015768, "ApostateCatacomb", 1, 1155, 83568, 86580, -5130, 0, 0, 43653, 60, 0), 
(1249015769, "ApostateCatacomb", 1, 1179, 83613, 86273, -5130, 0, 0, 50333, 60, 0), 
(1249015770, "ApostateCatacomb", 1, 1179, 83769, 84413, -5128, 0, 0, 16384, 60, 0), 
(1249015764, "ApostateCatacomb", 1, 1155, 83890, 85963, -5130, 0, 0, 47831, 60, 0), 
(1249015765, "ApostateCatacomb", 1, 1154, 83669, 85910, -5128, 0, 0, 43361, 60, 0), 
(1249015766, "ApostateCatacomb", 1, 1154, 83894, 86302, -5130, 0, 0, 5637, 60, 0), 
(1249015760, "ApostateCatacomb", 1, 1177, 82457, 88681, -5128, 0, 0, 31152, 60, 0), 
(1249015759, "ApostateCatacomb", 1, 1198, 82653, 88556, -5128, 0, 0, 13870, 60, 0), 
(1249015762, "ApostateCatacomb", 1, 1179, 83923, 86672, -5130, 0, 0, 4370, 60, 0), 
(1249015763, "ApostateCatacomb", 1, 1200, 83976, 86535, -5130, 0, 0, 63638, 60, 0), 
(1249015755, "ApostateCatacomb", 1, 1154, 83917, 88706, -5128, 0, 0, 31446, 60, 0), 
(1249015756, "ApostateCatacomb", 1, 1246, 82916, 88705, -5128, 0, 0, 63588, 60, 0), 
(1249015757, "ApostateCatacomb", 1, 1247, 82688, 88879, -5128, 0, 0, 49416, 60, 0), 
(1249015751, "ApostateCatacomb", 1, 10280, 85666, 88767, -5128, 0, 0, 49286, 60, 0), 
(1249015752, "ApostateCatacomb", 1, 1179, 83747, 88469, -5128, 0, 0, 48735, 60, 0), 
(1249015753, "ApostateCatacomb", 1, 1200, 83517, 88702, -5149, 0, 0, 24509, 60, 0), 
(1249015754, "ApostateCatacomb", 1, 1155, 83680, 88869, -5128, 0, 0, 32475, 60, 0), 
(1249015748, "ApostateCatacomb", 1, 1200, 85370, 84059, -5128, 0, 0, 63035, 60, 0), 
(1249015749, "ApostateCatacomb", 1, 1155, 85700, 84184, -5128, 0, 0, 5762, 60, 0), 
(1249015750, "ApostateCatacomb", 1, 1154, 86109, 84013, -5128, 0, 0, 61591, 60, 0), 
(1249015743, "ApostateCatacomb", 1, 1200, 86507, 84022, -5149, 0, 0, 54373, 60, 0), 
(1249015744, "ApostateCatacomb", 1, 1155, 86202, 84208, -5130, 0, 0, 18608, 60, 0), 
(1249015745, "ApostateCatacomb", 1, 1154, 85957, 84217, -5130, 0, 0, 62847, 60, 0), 
(1249015746, "ApostateCatacomb", 1, 1179, 85426, 84275, -5130, 0, 0, 57344, 60, 0), 
(1249015740, "ApostateCatacomb", 1, 1155, 86486, 86041, -5130, 0, 0, 62391, 60, 0), 
(1249015741, "ApostateCatacomb", 1, 1154, 86357, 85265, -5128, 0, 0, 46737, 60, 0), 
(1249015742, "ApostateCatacomb", 1, 1179, 86414, 84192, -5128, 0, 0, 405, 60, 0), 
(1249015736, "ApostateCatacomb", 1, 1155, 86192, 86605, -5151, 0, 0, 17292, 60, 0), 
(1249015737, "ApostateCatacomb", 1, 1154, 86407, 86811, -5128, 0, 0, 7970, 60, 0), 
(1249015738, "ApostateCatacomb", 1, 1155, 86346, 86448, -5128, 0, 0, 46893, 60, 0), 
(1249015739, "ApostateCatacomb", 1, 1154, 86036, 86144, -5130, 0, 0, 40514, 60, 0), 
(1249015733, "ApostateCatacomb", 1, 1200, 85979, 85706, -5130, 0, 0, 24428, 60, 0), 
(1249015734, "ApostateCatacomb", 1, 1200, 86161, 85336, -5130, 0, 0, 51031, 60, 0), 
(1249015735, "ApostateCatacomb", 1, 1200, 86266, 85875, -5128, 0, 0, 16672, 60, 0), 
(1249015729, "ApostateCatacomb", 1, 1154, 88574, 88709, -5128, 0, 0, 32165, 60, 0), 
(1249015730, "ApostateCatacomb", 1, 1179, 86624, 86235, -5130, 0, 0, 63292, 60, 0), 
(1249015731, "ApostateCatacomb", 1, 1179, 86448, 85690, -5130, 0, 0, 44937, 60, 0), 
(1249015732, "ApostateCatacomb", 1, 1179, 86021, 85896, -5130, 0, 0, 28075, 60, 0), 
(1249015726, "ApostateCatacomb", 1, 1179, 88347, 88404, -5128, 0, 0, 48351, 60, 0), 
(1249015727, "ApostateCatacomb", 1, 1200, 88179, 88649, -5128, 0, 0, 44162, 60, 0), 
(1249015728, "ApostateCatacomb", 1, 1155, 88335, 88869, -5128, 0, 0, 48015, 60, 0), 
(1249015722, "ApostateCatacomb", 1, 1154, 87273, 88399, -5128, 0, 0, 48914, 60, 0), 
(1249015723, "ApostateCatacomb", 1, 1155, 87021, 88667, -5128, 0, 0, 26057, 60, 0), 
(1249015724, "ApostateCatacomb", 1, 1200, 87250, 88893, -5128, 0, 0, 12357, 60, 0), 
(1249015725, "ApostateCatacomb", 1, 1179, 87486, 88733, -5128, 0, 0, 59059, 60, 0), 
(1249015719, "ApostateCatacomb", 1, 1200, 88520, 86898, -5130, 0, 0, 52708, 60, 0), 
(1249015720, "ApostateCatacomb", 1, 1155, 88360, 86548, -5128, 0, 0, 38418, 60, 0), 
(1249015721, "ApostateCatacomb", 1, 1154, 88509, 86344, -5128, 0, 0, 55485, 60, 0), 
(1249015715, "ApostateCatacomb", 1, 1200, 88255, 86134, -5130, 0, 0, 52377, 60, 0), 
(1249015716, "ApostateCatacomb", 1, 1155, 88129, 86564, -5130, 0, 0, 23031, 60, 0), 
(1249015717, "ApostateCatacomb", 1, 1154, 88149, 86929, -5130, 0, 0, 33873, 60, 0), 
(1249015718, "ApostateCatacomb", 1, 1179, 88409, 87129, -5128, 0, 0, 50115, 60, 0), 
(1249015639, "ApostateCatacomb", 1, 1245, 82681, 78615, -5128, 0, 0, 46077, 60, 0), 
(1249015649, "ApostateCatacomb", 1, 1152, 84661, 81564, -5128, 0, 0, 20691, 60, 0), 
(1249015650, "ApostateCatacomb", 1, 1153, 84415, 81525, -5130, 0, 0, 15946, 60, 0), 
(1249015651, "ApostateCatacomb", 1, 1178, 84061, 81533, -5130, 0, 0, 60340, 60, 0), 
(1249015652, "ApostateCatacomb", 1, 1199, 83938, 81280, -5130, 0, 0, 52342, 60, 0), 
(1249015653, "ApostateCatacomb", 1, 1152, 84304, 81354, -5128, 0, 0, 1355, 60, 0), 
(1249015654, "ApostateCatacomb", 1, 1153, 84846, 81282, -5130, 0, 0, 29174, 60, 0), 
(1249015659, "ApostateCatacomb", 1, 1153, 85201, 79338, -5130, 0, 0, 35706, 60, 0), 
(1249015660, "ApostateCatacomb", 1, 1152, 85215, 79165, -5130, 0, 0, 35799, 60, 0), 
(1249015661, "ApostateCatacomb", 1, 1199, 85378, 78738, -5130, 0, 0, 16666, 60, 0), 
(1249015662, "ApostateCatacomb", 1, 1178, 85787, 78420, -5130, 0, 0, 8414, 60, 0), 
(1249015663, "ApostateCatacomb", 1, 1153, 85808, 78722, -5130, 0, 0, 16576, 60, 0), 
(1249015664, "ApostateCatacomb", 1, 1152, 85761, 79187, -5130, 0, 0, 18274, 60, 0), 
(1249015665, "ApostateCatacomb", 1, 1199, 85780, 79580, -5130, 0, 0, 15761, 60, 0), 
(1249015666, "ApostateCatacomb", 1, 1178, 85664, 79879, -5130, 0, 0, 20942, 60, 0), 
(1249015667, "ApostateCatacomb", 1, 1153, 85440, 79940, -5130, 0, 0, 14971, 60, 0), 
(1249015668, "ApostateCatacomb", 1, 1152, 85363, 79706, -5130, 0, 0, 46452, 60, 0), 
(1249015669, "ApostateCatacomb", 1, 1178, 85555, 79496, -5128, 0, 0, 56519, 60, 0), 
(1249015670, "ApostateCatacomb", 1, 1199, 85451, 79133, -5151, 0, 0, 43872, 60, 0), 
(1249015671, "ApostateCatacomb", 1, 1178, 85806, 81840, -5128, 0, 0, 51279, 60, 0), 
(1249015672, "ApostateCatacomb", 1, 1199, 85996, 81825, -5128, 0, 0, 48609, 60, 0), 
(1249015673, "ApostateCatacomb", 1, 1152, 86084, 82138, -5130, 0, 0, 7498, 60, 0), 
(1249015674, "ApostateCatacomb", 1, 1153, 86036, 82384, -5130, 0, 0, 63890, 60, 0), 
(1249015676, "ApostateCatacomb", 1, 1178, 86046, 82817, -5130, 0, 0, 47330, 60, 0), 
(1249015679, "ApostateCatacomb", 1, 1199, 85738, 82676, -5130, 0, 0, 50894, 60, 0), 
(1249015678, "ApostateCatacomb", 1, 1152, 85747, 82933, -5130, 0, 0, 47997, 60, 0), 
(1249015681, "ApostateCatacomb", 1, 1153, 85769, 82183, -5130, 0, 0, 52412, 60, 0), 
(1249015682, "ApostateCatacomb", 1, 1178, 87486, 81668, -5128, 0, 0, 40747, 60, 0), 
(1249015683, "ApostateCatacomb", 1, 1199, 87535, 81853, -5151, 0, 0, 32989, 60, 0), 
(1249015684, "ApostateCatacomb", 1, 1153, 87837, 81585, -5130, 0, 0, 56346, 60, 0), 
(1249015685, "ApostateCatacomb", 1, 1152, 88048, 81584, -5151, 0, 0, 49976, 60, 0), 
(1249015686, "ApostateCatacomb", 1, 1178, 88357, 81640, -5130, 0, 0, 25241, 60, 0), 
(1249015687, "ApostateCatacomb", 1, 1152, 88562, 81877, -5130, 0, 0, 30150, 60, 0), 
(1249015688, "ApostateCatacomb", 1, 1199, 88255, 81936, -5130, 0, 0, 27537, 60, 0), 
(1249015689, "ApostateCatacomb", 1, 1153, 88017, 81932, -5130, 0, 0, 32640, 60, 0), 
(1249015690, "ApostateCatacomb", 1, 1178, 86839, 79903, -5128, 0, 0, 18495, 60, 0), 
(1249015691, "ApostateCatacomb", 1, 1178, 87238, 79487, -5130, 0, 0, 57634, 60, 0), 
(1249015692, "ApostateCatacomb", 1, 1178, 86770, 78846, -5130, 0, 0, 6037, 60, 0), 
(1249015693, "ApostateCatacomb", 1, 1199, 86786, 78468, -5130, 0, 0, 59814, 60, 0), 
(1249015694, "ApostateCatacomb", 1, 1199, 87099, 78779, -5128, 0, 0, 10023, 60, 0), 
(1249015695, "ApostateCatacomb", 1, 1199, 86998, 79239, -5128, 0, 0, 18318, 60, 0), 
(1249015696, "ApostateCatacomb", 1, 1153, 87028, 79831, -5128, 0, 0, 15151, 60, 0), 
(1249015697, "ApostateCatacomb", 1, 1153, 86797, 79281, -5130, 0, 0, 64122, 60, 0), 
(1249015698, "ApostateCatacomb", 1, 1153, 87111, 78429, -5130, 0, 0, 11548, 60, 0), 
(1249015699, "ApostateCatacomb", 1, 1152, 86981, 78595, -5128, 0, 0, 23314, 60, 0), 
(1249015700, "ApostateCatacomb", 1, 1152, 87238, 79128, -5130, 0, 0, 9687, 60, 0), 
(1249015701, "ApostateCatacomb", 1, 1152, 86966, 79458, -5128, 0, 0, 23480, 60, 0), 
(1249015702, "ApostateCatacomb", 1, 1178, 88377, 78705, -5128, 0, 0, 15915, 60, 0), 
(1249015703, "ApostateCatacomb", 1, 1199, 88571, 78454, -5128, 0, 0, 20449, 60, 0), 
(1249015704, "ApostateCatacomb", 1, 1152, 88368, 78163, -5128, 0, 0, 16025, 60, 0), 
(1249015705, "ApostateCatacomb", 1, 1153, 88161, 78418, -5128, 0, 0, 22195, 60, 0), 
(1249015706, "ApostateCatacomb", 1, 1153, 88062, 83016, -5128, 0, 0, 33108, 60, 0), 
(1249015707, "ApostateCatacomb", 1, 1152, 88321, 83222, -5128, 0, 0, 13775, 60, 0), 
(1249015708, "ApostateCatacomb", 1, 1199, 88526, 83069, -5128, 0, 0, 16881, 60, 0), 
(1249015709, "ApostateCatacomb", 1, 1178, 88369, 82801, -5128, 0, 0, 17782, 60, 0), 
(1249015710, "ApostateCatacomb", 1, 1179, 88380, 83796, -5128, 0, 0, 47236, 60, 0), 
(1249015711, "ApostateCatacomb", 1, 1200, 88214, 84042, -5128, 0, 0, 23984, 60, 0), 
(1249015712, "ApostateCatacomb", 1, 1155, 88349, 84272, -5128, 0, 0, 13600, 60, 0), 
(1249015713, "ApostateCatacomb", 1, 1154, 88515, 84100, -5128, 0, 0, 31176, 60, 0), 
(1249015714, "ApostateCatacomb", 1, 1179, 88453, 86134, -5128, 0, 0, 50643, 60, 0), 
(1249015864, "ApostateCatacomb", 1, 1244, 80953, 81311, -5130, 0, 0, 23243, 60, 0), 
(1249015863, "ApostateCatacomb", 1, 1176, 81166, 81276, -5130, 0, 0, 24576, 60, 0), 
(1249015836, "ApostateCatacomb", 1, 1177, 77887, 88651, -5128, 0, 0, 52382, 60, 0), 
(1249015828, "ApostateCatacomb", 1, 1247, 78906, 86950, -5130, 0, 0, 55230, 60, 0), 
(1249015873, "ApostateCatacomb", 1, 1176, 82629, 79659, -5130, 0, 0, 33193, 60, 0), 
(1249015872, "ApostateCatacomb", 1, 1176, 81908, 79897, -5149, 0, 0, 40922, 60, 0), 
(1249015835, "ApostateCatacomb", 1, 1198, 78053, 88444, -5128, 0, 0, 48225, 60, 0), 
(1249015827, "ApostateCatacomb", 1, 1246, 78679, 86934, -5130, 0, 0, 52265, 60, 0), 
(1249015874, "ApostateCatacomb", 1, 1244, 82479, 80176, -5151, 0, 0, 18403, 60, 0), 
(1249015871, "ApostateCatacomb", 1, 1176, 82277, 80191, -5130, 0, 0, 16231, 60, 0), 
(1249015834, "ApostateCatacomb", 1, 1177, 79062, 87109, -5128, 0, 0, 32202, 60, 0), 
(1249015826, "ApostateCatacomb", 1, 1177, 79709, 85158, -5130, 0, 0, 39492, 60, 0), 
(1249015884, "ApostateCatacomb", 1, 1244, 79228, 79324, -5128, 0, 0, 63071, 60, 0), 
(1249015883, "ApostateCatacomb", 1, 1176, 79396, 79516, -5128, 0, 0, 1840, 60, 0), 
(1249015833, "ApostateCatacomb", 1, 1198, 78667, 87066, -5128, 0, 0, 3168, 60, 0), 
(1249015825, "ApostateCatacomb", 1, 1198, 79894, 85347, -5128, 0, 0, 24751, 60, 0), 
(1249015640, "ApostateCatacomb", 1, 1197, 82669, 78194, -5128, 0, 0, 15658, 60, 0), 
(1249015646, "ApostateCatacomb", 1, 1153, 83753, 78212, -5128, 0, 0, 15332, 60, 0), 
(1249015647, "ApostateCatacomb", 1, 1178, 84453, 81219, -5130, 0, 0, 48597, 60, 0), 
(1249015642, "ApostateCatacomb", 1, 1178, 83487, 78406, -5128, 0, 0, 32606, 60, 0), 
(1249015643, "ApostateCatacomb", 1, 1199, 83742, 78590, -5128, 0, 0, 11498, 60, 0), 
(1249015644, "ApostateCatacomb", 1, 1152, 83950, 78387, -5128, 0, 0, 20111, 60, 0), 
(1249015648, "ApostateCatacomb", 1, 1199, 84645, 81205, -5130, 0, 0, 53626, 60, 0), 
(1249015636, "ApostateCatacomb", 1, 1197, 80480, 78599, -5130, 0, 0, 63875, 60, 0), 
(1249015629, "ApostateCatacomb", 1, 1176, 79800, 78784, -5128, 0, 0, 37365, 60, 0), 
(1249015630, "ApostateCatacomb", 1, 1244, 79794, 78643, -5149, 0, 0, 42219, 60, 0), 
(1249015631, "ApostateCatacomb", 1, 1245, 80953, 78627, -5128, 0, 0, 0, 60, 0), 
(1249015632, "ApostateCatacomb", 1, 1197, 80978, 78827, -5130, 0, 0, 63090, 60, 0), 
(1249015633, "ApostateCatacomb", 1, 1176, 80805, 78859, -5130, 0, 0, 30835, 60, 0), 
(1249015634, "ApostateCatacomb", 1, 1245, 80317, 78855, -5130, 0, 0, 32930, 60, 0), 
(1249015635, "ApostateCatacomb", 1, 1244, 80158, 78604, -5130, 0, 0, 59576, 60, 0), 
(1249015847, "ApostateCatacomb", 1, 1176, 78965, 82403, -5128, 0, 0, 36009, 60, 0), 
(1249015832, "ApostateCatacomb", 1, 1247, 78342, 86978, -5151, 0, 0, 55362, 60, 0), 
(1249015824, "ApostateCatacomb", 1, 1247, 80051, 85080, -5130, 0, 0, 17107, 60, 0), 
(1249015851, "ApostateCatacomb", 1, 1244, 79383, 82818, -5128, 0, 0, 48033, 60, 0), 
(1249015848, "ApostateCatacomb", 1, 1176, 79221, 81778, -5130, 0, 0, 56898, 60, 0), 
(1249015831, "ApostateCatacomb", 1, 1246, 78389, 87220, -5130, 0, 0, 59402, 60, 0), 
(1249015823, "ApostateCatacomb", 1, 1246, 80077, 84792, -5130, 0, 0, 17870, 60, 0), 
(1249015849, "ApostateCatacomb", 1, 1176, 79543, 82145, -5128, 0, 0, 8827, 60, 0), 
(1249015850, "ApostateCatacomb", 1, 1244, 79700, 82507, -5130, 0, 0, 9074, 60, 0), 
(1249015830, "ApostateCatacomb", 1, 1177, 78749, 87262, -5130, 0, 0, 22733, 60, 0), 
(1249015822, "ApostateCatacomb", 1, 1177, 80101, 84561, -5130, 0, 0, 14727, 60, 0), 
(1249015860, "ApostateCatacomb", 1, 1244, 79159, 80582, -5128, 0, 0, 59800, 60, 0), 
(1249015859, "ApostateCatacomb", 1, 1176, 79338, 80767, -5149, 0, 0, 17941, 60, 0), 
(1249015829, "ApostateCatacomb", 1, 1198, 78941, 87298, -5151, 0, 0, 19966, 60, 0), 
(1249015641, "ApostateCatacomb", 1, 1244, 82881, 78402, -5128, 0, 0, 652, 60, 0), 
(1249015845, "ApostateCatacomb", 1, 1245, 78101, 82799, -5128, 0, 0, 43466, 60, 0), 
(1249015843, "ApostateCatacomb", 1, 1176, 78072, 83297, -5128, 0, 0, 16384, 60, 0), 
(1249015841, "ApostateCatacomb", 1, 1247, 77940, 84064, -5128, 0, 0, 58243, 60, 0), 
(1249015839, "ApostateCatacomb", 1, 1177, 78069, 84365, -5128, 0, 0, 16265, 60, 0), 
(1249015838, "ApostateCatacomb", 1, 1246, 78234, 88661, -5149, 0, 0, 49605, 60, 0), 
(1249015844, "ApostateCatacomb", 1, 1244, 78252, 83048, -5128, 0, 0, 30263, 60, 0), 
(1249015842, "ApostateCatacomb", 1, 1246, 78072, 83944, -5128, 0, 0, 57840, 60, 0), 
(1249015840, "ApostateCatacomb", 1, 1198, 78258, 84132, -5128, 0, 0, 32592, 60, 0), 
(1249015837, "ApostateCatacomb", 1, 1247, 78071, 88784, -5128, 0, 0, 48531, 60, 0), 
(1249015846, "ApostateCatacomb", 1, 1197, 77878, 82994, -5128, 0, 0, 63227, 60, 0), 
(1249015852, "ApostateCatacomb", 1, 1244, 79378, 81857, -5128, 0, 0, 52845, 60, 0), 
(1249015853, "ApostateCatacomb", 1, 1245, 79469, 81623, -5130, 0, 0, 53163, 60, 0), 
(1249015854, "ApostateCatacomb", 1, 1245, 79255, 82180, -5149, 0, 0, 24576, 60, 0), 
(1249015855, "ApostateCatacomb", 1, 1245, 79171, 83059, -5130, 0, 0, 49026, 60, 0), 
(1249015856, "ApostateCatacomb", 1, 1197, 79602, 82785, -5130, 0, 0, 58997, 60, 0), 
(1249015857, "ApostateCatacomb", 1, 1197, 79024, 82227, -5130, 0, 0, 40501, 60, 0), 
(1249015858, "ApostateCatacomb", 1, 1197, 79342, 82475, -5128, 0, 0, 12601, 60, 0), 
(1249015861, "ApostateCatacomb", 1, 1245, 79361, 80480, -5128, 0, 0, 13182, 60, 0), 
(1249015862, "ApostateCatacomb", 1, 1197, 79477, 80708, -5130, 0, 0, 15852, 60, 0), 
(1249015865, "ApostateCatacomb", 1, 1245, 81522, 81207, -5128, 0, 0, 1593, 60, 0), 
(1249015866, "ApostateCatacomb", 1, 1197, 81698, 81017, -5130, 0, 0, 53066, 60, 0), 
(1249015867, "ApostateCatacomb", 1, 1176, 81510, 81012, -5130, 0, 0, 56189, 60, 0), 
(1249015868, "ApostateCatacomb", 1, 1245, 81232, 81059, -5128, 0, 0, 64856, 60, 0), 
(1249015869, "ApostateCatacomb", 1, 1197, 80681, 81205, -5128, 0, 0, 60534, 60, 0), 
(1249015870, "ApostateCatacomb", 1, 1244, 80703, 80937, -5130, 0, 0, 14962, 60, 0), 
(1249015875, "ApostateCatacomb", 1, 1244, 81804, 79474, -5151, 0, 0, 40795, 60, 0), 
(1249015876, "ApostateCatacomb", 1, 1244, 81289, 79690, -5130, 0, 0, 28393, 60, 0), 
(1249015877, "ApostateCatacomb", 1, 1245, 81242, 79946, -5130, 0, 0, 27549, 60, 0), 
(1249015878, "ApostateCatacomb", 1, 1245, 81599, 79450, -5151, 0, 0, 49873, 60, 0), 
(1249015879, "ApostateCatacomb", 1, 1245, 82198, 79648, -5130, 0, 0, 29442, 60, 0), 
(1249015880, "ApostateCatacomb", 1, 1197, 82699, 79872, -5128, 0, 0, 34788, 60, 0), 
(1249015881, "ApostateCatacomb", 1, 1197, 82109, 79969, -5128, 0, 0, 28692, 60, 0), 
(1249015882, "ApostateCatacomb", 1, 1197, 81665, 79760, -5128, 0, 0, 37881, 60, 0), 
(1249015885, "ApostateCatacomb", 1, 1245, 79087, 79541, -5149, 0, 0, 4573, 60, 0), 
(1249015886, "ApostateCatacomb", 1, 1197, 79245, 79724, -5128, 0, 0, 62910, 60, 0), 
(1249015887, "ApostateCatacomb", 1, 1176, 77977, 79588, -5128, 0, 0, 50507, 60, 0), 
(1249015888, "ApostateCatacomb", 1, 1244, 78170, 79593, -5128, 0, 0, 52588, 60, 0), 
(1249015889, "ApostateCatacomb", 1, 1245, 78167, 80613, -5130, 0, 0, 16323, 60, 0), 
(1249015890, "ApostateCatacomb", 1, 1197, 77924, 80536, -5130, 0, 0, 16232, 60, 0), 
(1249015891, "ApostateCatacomb", 1, 1176, 78042, 80444, -5128, 0, 0, 15658, 60, 0), 
(1249015893, "ApostateCatacomb", 1, 1244, 78258, 80114, -5130, 0, 0, 57588, 60, 0), 
(1249015894, "ApostateCatacomb", 1, 1245, 77923, 80131, -5130, 0, 0, 63040, 60, 0), 
(1249015895, "ApostateCatacomb", 1, 1197, 78062, 79814, -5128, 0, 0, 51676, 60, 0);

-- Deletes any drop data for these mobs if exists. 
DELETE FROM droplist where mobid in (1177,1198,1247,1246,1154,1155,1200,1179,1153,1152,1199,1178,1245,1197,1244,1176); 
-- Add drop data 
-- Lilim Magus 1176 
INSERT INTO `droplist` VALUES (1176, 326,1,1,0,42); 
INSERT INTO `droplist` VALUES (1176, 1864,5,19,0,200054); 
INSERT INTO `droplist` VALUES (1176, 1865,3,10,0,101757); 
INSERT INTO `droplist` VALUES (1176, 1866,2,7,0,76944); 
INSERT INTO `droplist` VALUES (1176, 1867,4,13,0,100027); 
INSERT INTO `droplist` VALUES (1176, 1868,5,19,0,336792); 
INSERT INTO `droplist` VALUES (1176, 1872,4,13,0,30311); 
INSERT INTO `droplist` VALUES (1176, 1872,4,25,1,166712); 
INSERT INTO `droplist` VALUES (1176, 1873,1,1,0,37047); 
INSERT INTO `droplist` VALUES (1176, 1875,1,1,0,19236); 
INSERT INTO `droplist` VALUES (1176, 1881,1,2,0,7520); 
INSERT INTO `droplist` VALUES (1176, 2110,1,1,0,5064); 
INSERT INTO `droplist` VALUES (1176, 2232,1,1,0,5223); 
INSERT INTO `droplist` VALUES (1176, 3012,1,1,0,16859); 
INSERT INTO `droplist` VALUES (1176, 3928,2,4,0,3638); 
INSERT INTO `droplist` VALUES (1176, 4041,1,1,0,7893); 
INSERT INTO `droplist` VALUES (1176, 4041,1,1,1,46437); 
INSERT INTO `droplist` VALUES (1176, 4601,1,1,1,12074); 
INSERT INTO `droplist` VALUES (1176, 6360,129,258,0,700000); 
INSERT INTO `droplist` VALUES (1176, 6361,211,422,0,700000); 
INSERT INTO `droplist` VALUES (1176, 6362,11,22,0,700000); 
-- Crypt Archon 1244 
INSERT INTO `droplist` VALUES (1244, 57,1470,2943,0,700000); 
INSERT INTO `droplist` VALUES (1244, 60,1,1,0,124); 
INSERT INTO `droplist` VALUES (1244, 1539,1,4,1,427467); 
INSERT INTO `droplist` VALUES (1244, 1865,3,9,0,100429); 
INSERT INTO `droplist` VALUES (1244, 1870,3,9,0,66684); 
INSERT INTO `droplist` VALUES (1244, 1875,1,1,0,18873); 
INSERT INTO `droplist` VALUES (1244, 1879,1,2,0,7814); 
INSERT INTO `droplist` VALUES (1244, 1914,1,1,1,263230); 
INSERT INTO `droplist` VALUES (1244, 1977,1,1,0,13201); 
INSERT INTO `droplist` VALUES (1244, 2170,1,1,0,20965); 
INSERT INTO `droplist` VALUES (1244, 2970,1,1,0,31356); 
INSERT INTO `droplist` VALUES (1244, 4040,1,1,0,17807); 
INSERT INTO `droplist` VALUES (1244, 4601,1,1,1,119); 
-- Nephilim Priest 1197 
INSERT INTO `droplist` VALUES (1197, 326,1,1,0,42); 
INSERT INTO `droplist` VALUES (1197, 1867,4,13,0,100027); 
INSERT INTO `droplist` VALUES (1197, 1869,3,10,0,200054); 
INSERT INTO `droplist` VALUES (1197, 1870,3,10,0,43490); 
INSERT INTO `droplist` VALUES (1197, 1871,3,10,0,58839); 
INSERT INTO `droplist` VALUES (1197, 1872,4,13,0,30311); 
INSERT INTO `droplist` VALUES (1197, 1872,4,25,1,166712); 
INSERT INTO `droplist` VALUES (1197, 1875,1,1,0,19236); 
INSERT INTO `droplist` VALUES (1197, 1881,1,2,0,7635); 
INSERT INTO `droplist` VALUES (1197, 2110,1,1,0,5077); 
INSERT INTO `droplist` VALUES (1197, 2232,1,1,0,5223); 
INSERT INTO `droplist` VALUES (1197, 4041,1,1,0,7893); 
INSERT INTO `droplist` VALUES (1197, 4041,1,1,1,46437); 
INSERT INTO `droplist` VALUES (1197, 4603,1,1,1,12074); 
INSERT INTO `droplist` VALUES (1197, 6360,135,271,0,700000); 
INSERT INTO `droplist` VALUES (1197, 6361,222,444,0,700000); 
INSERT INTO `droplist` VALUES (1197, 6362,11,23,0,700000); 
-- Crypt Inquisitor 1245 
INSERT INTO `droplist` VALUES (1245, 57,1625,3247,0,700000); 
INSERT INTO `droplist` VALUES (1245, 60,1,1,0,128); 
INSERT INTO `droplist` VALUES (1245, 1866,2,7,0,76944); 
INSERT INTO `droplist` VALUES (1245, 1871,3,10,0,66684); 
INSERT INTO `droplist` VALUES (1245, 1877,1,1,0,36294); 
INSERT INTO `droplist` VALUES (1245, 1915,1,1,1,392264); 
INSERT INTO `droplist` VALUES (1245, 1977,1,1,0,13579); 
INSERT INTO `droplist` VALUES (1245, 2169,1,1,1,93571); 
INSERT INTO `droplist` VALUES (1245, 2170,1,1,1,122733); 
INSERT INTO `droplist` VALUES (1245, 2353,1,1,0,1506); 
INSERT INTO `droplist` VALUES (1245, 4040,1,1,0,18313); 
INSERT INTO `droplist` VALUES (1245, 4041,1,1,0,8004); 
INSERT INTO `droplist` VALUES (1245, 4122,1,1,0,40252); 
-- Lilim Marauder 1178 
INSERT INTO `droplist` VALUES (1178, 947,1,1,1,2361); 
INSERT INTO `droplist` VALUES (1178, 948,1,1,0,2459); 
INSERT INTO `droplist` VALUES (1178, 1867,4,15,0,100027); 
INSERT INTO `droplist` VALUES (1178, 1872,4,15,0,45467); 
INSERT INTO `droplist` VALUES (1178, 1875,1,1,0,20839); 
INSERT INTO `droplist` VALUES (1178, 1881,1,2,0,7354); 
INSERT INTO `droplist` VALUES (1178, 2384,1,1,0,126); 
INSERT INTO `droplist` VALUES (1178, 2388,1,1,0,202); 
INSERT INTO `droplist` VALUES (1178, 2390,1,1,0,86); 
INSERT INTO `droplist` VALUES (1178, 4041,1,1,0,8530); 
INSERT INTO `droplist` VALUES (1178, 4066,1,1,0,22963); 
INSERT INTO `droplist` VALUES (1178, 4067,1,1,0,34659); 
INSERT INTO `droplist` VALUES (1178, 4068,1,1,0,17685); 
INSERT INTO `droplist` VALUES (1178, 4129,1,1,1,61974); 
INSERT INTO `droplist` VALUES (1178, 4162,1,1,0,4079); 
INSERT INTO `droplist` VALUES (1178, 4610,1,1,1,13048); 
INSERT INTO `droplist` VALUES (1178, 5003,1,1,0,940); 
INSERT INTO `droplist` VALUES (1178, 6360,72,145,0,700000); 
INSERT INTO `droplist` VALUES (1178, 6361,388,775,0,700000); 
INSERT INTO `droplist` VALUES (1178, 6362,13,26,0,700000); 
-- Nephilim Guard 1199 
INSERT INTO `droplist` VALUES (1199, 948,1,1,0,2459); 
INSERT INTO `droplist` VALUES (1199, 1867,4,15,0,100027); 
INSERT INTO `droplist` VALUES (1199, 1872,4,15,0,45467); 
INSERT INTO `droplist` VALUES (1199, 1875,1,1,0,20839); 
INSERT INTO `droplist` VALUES (1199, 1881,1,2,0,7464); 
INSERT INTO `droplist` VALUES (1199, 2384,1,1,0,126); 
INSERT INTO `droplist` VALUES (1199, 2388,1,1,0,202); 
INSERT INTO `droplist` VALUES (1199, 2390,1,1,0,86); 
INSERT INTO `droplist` VALUES (1199, 4041,1,1,0,8530); 
INSERT INTO `droplist` VALUES (1199, 4066,1,1,0,22963); 
INSERT INTO `droplist` VALUES (1199, 4067,1,1,0,34659); 
INSERT INTO `droplist` VALUES (1199, 4068,1,1,0,17685); 
INSERT INTO `droplist` VALUES (1199, 4129,1,1,1,61974); 
INSERT INTO `droplist` VALUES (1199, 4163,1,1,0,6503); 
INSERT INTO `droplist` VALUES (1199, 4611,1,1,1,13048); 
INSERT INTO `droplist` VALUES (1199, 6360,72,145,0,700000); 
INSERT INTO `droplist` VALUES (1199, 6361,388,775,0,700000); 
INSERT INTO `droplist` VALUES (1199, 6362,13,26,0,700000); 
-- Purgatory Wisp 1152 
INSERT INTO `droplist` VALUES (1152, 57,1692,3385,0,700000); 
INSERT INTO `droplist` VALUES (1152, 400,1,1,0,227); 
INSERT INTO `droplist` VALUES (1152, 401,1,1,0,119); 
INSERT INTO `droplist` VALUES (1152, 420,1,1,0,363); 
INSERT INTO `droplist` VALUES (1152, 1806,1,1,0,1871); 
INSERT INTO `droplist` VALUES (1152, 1806,1,1,1,11177); 
INSERT INTO `droplist` VALUES (1152, 1868,5,20,0,344922); 
INSERT INTO `droplist` VALUES (1152, 1873,1,1,0,37047); 
INSERT INTO `droplist` VALUES (1152, 1876,1,1,0,45779); 
INSERT INTO `droplist` VALUES (1152, 1895,1,3,0,29419); 
INSERT INTO `droplist` VALUES (1152, 1920,1,1,1,175486); 
INSERT INTO `droplist` VALUES (1152, 1984,1,1,0,12537); 
INSERT INTO `droplist` VALUES (1152, 1985,1,1,0,31795); 
INSERT INTO `droplist` VALUES (1152, 1987,1,1,0,14192); 
INSERT INTO `droplist` VALUES (1152, 4042,1,1,0,3788); 
INSERT INTO `droplist` VALUES (1152, 4600,1,1,1,18557); 
INSERT INTO `droplist` VALUES (1152, 4941,1,1,0,8967); 
INSERT INTO `droplist` VALUES (1152, 4959,1,1,0,3237); 
-- Purgatory Serpent 1153 
INSERT INTO `droplist` VALUES (1153, 57,2259,4511,0,700000); 
INSERT INTO `droplist` VALUES (1153, 357,1,1,0,93); 
INSERT INTO `droplist` VALUES (1153, 383,1,1,0,149); 
INSERT INTO `droplist` VALUES (1153, 947,1,1,0,389); 
INSERT INTO `droplist` VALUES (1153, 947,1,1,1,2333); 
INSERT INTO `droplist` VALUES (1153, 1894,1,1,0,33588); 
INSERT INTO `droplist` VALUES (1153, 2376,1,1,0,93); 
INSERT INTO `droplist` VALUES (1153, 2379,1,1,0,149); 
INSERT INTO `droplist` VALUES (1153, 3955,1,1,0,1945); 
INSERT INTO `droplist` VALUES (1153, 4039,1,1,0,12095); 
INSERT INTO `droplist` VALUES (1153, 4041,1,1,0,8429); 
INSERT INTO `droplist` VALUES (1153, 4044,1,1,0,17862); 
INSERT INTO `droplist` VALUES (1153, 4049,1,1,1,265324); 
INSERT INTO `droplist` VALUES (1153, 4056,1,1,0,17465); 
INSERT INTO `droplist` VALUES (1153, 4057,1,1,0,26867); 
INSERT INTO `droplist` VALUES (1153, 4064,1,1,0,17465); 
INSERT INTO `droplist` VALUES (1153, 4065,1,1,0,26867); 
INSERT INTO `droplist` VALUES (1153, 4129,1,1,1,61253); 
INSERT INTO `droplist` VALUES (1153, 5280,1,1,0,1945); 
-- Lilim Priest 1179 
INSERT INTO `droplist` VALUES (1179, 357,1,1,0,97); 
INSERT INTO `droplist` VALUES (1179, 383,1,1,0,156); 
INSERT INTO `droplist` VALUES (1179, 947,1,1,1,2440); 
INSERT INTO `droplist` VALUES (1179, 1343,20,60,0,254034); 
INSERT INTO `droplist` VALUES (1179, 1867,5,17,0,66684); 
INSERT INTO `droplist` VALUES (1179, 1872,5,17,0,83356); 
INSERT INTO `droplist` VALUES (1179, 1872,5,33,1,250068); 
INSERT INTO `droplist` VALUES (1179, 1875,1,1,0,21282); 
INSERT INTO `droplist` VALUES (1179, 1881,1,2,0,7354); 
INSERT INTO `droplist` VALUES (1179, 2376,1,1,0,97); 
INSERT INTO `droplist` VALUES (1179, 2379,1,1,0,156); 
INSERT INTO `droplist` VALUES (1179, 4041,1,1,0,8816); 
INSERT INTO `droplist` VALUES (1179, 4056,1,1,0,18266); 
INSERT INTO `droplist` VALUES (1179, 4057,1,1,0,28089); 
INSERT INTO `droplist` VALUES (1179, 4064,1,1,0,18266); 
INSERT INTO `droplist` VALUES (1179, 4065,1,1,0,28089); 
INSERT INTO `droplist` VALUES (1179, 4155,1,1,0,3157); 
INSERT INTO `droplist` VALUES (1179, 4613,1,1,1,10130); 
INSERT INTO `droplist` VALUES (1179, 6360,7,14,0,700000); 
INSERT INTO `droplist` VALUES (1179, 6361,401,802,0,700000); 
INSERT INTO `droplist` VALUES (1179, 6362,11,21,0,700000); 
-- Nephilim Bishop 1200 
INSERT INTO `droplist` VALUES (1200, 357,1,1,0,97); 
INSERT INTO `droplist` VALUES (1200, 383,1,1,0,156); 
INSERT INTO `droplist` VALUES (1200, 1343,20,60,0,254034); 
INSERT INTO `droplist` VALUES (1200, 1867,5,17,0,71448); 
INSERT INTO `droplist` VALUES (1200, 1872,5,17,0,83356); 
INSERT INTO `droplist` VALUES (1200, 1875,1,1,0,21282); 
INSERT INTO `droplist` VALUES (1200, 1881,1,2,0,7354); 
INSERT INTO `droplist` VALUES (1200, 2376,1,1,0,97); 
INSERT INTO `droplist` VALUES (1200, 2379,1,1,0,156); 
INSERT INTO `droplist` VALUES (1200, 4041,1,1,0,8816); 
INSERT INTO `droplist` VALUES (1200, 4049,1,1,1,276318); 
INSERT INTO `droplist` VALUES (1200, 4056,1,1,0,18266); 
INSERT INTO `droplist` VALUES (1200, 4057,1,1,0,28089); 
INSERT INTO `droplist` VALUES (1200, 4064,1,1,0,18266); 
INSERT INTO `droplist` VALUES (1200, 4065,1,1,0,28089); 
INSERT INTO `droplist` VALUES (1200, 4129,1,1,1,63997); 
INSERT INTO `droplist` VALUES (1200, 4155,1,1,0,3157); 
INSERT INTO `droplist` VALUES (1200, 4613,1,1,1,10130); 
INSERT INTO `droplist` VALUES (1200, 6360,8,15,0,700000); 
INSERT INTO `droplist` VALUES (1200, 6361,421,842,0,700000); 
INSERT INTO `droplist` VALUES (1200, 6362,11,23,0,700000); 
-- Purgatory Conjurer 1155 
INSERT INTO `droplist` VALUES (1155, 57,2118,4239,0,700000); 
INSERT INTO `droplist` VALUES (1155, 856,1,1,0,332); 
INSERT INTO `droplist` VALUES (1155, 887,1,1,0,498); 
INSERT INTO `droplist` VALUES (1155, 918,1,1,0,249); 
INSERT INTO `droplist` VALUES (1155, 1895,1,4,0,30311); 
INSERT INTO `droplist` VALUES (1155, 4040,1,1,0,19953); 
INSERT INTO `droplist` VALUES (1155, 4042,1,1,0,3847); 
INSERT INTO `droplist` VALUES (1155, 4043,1,1,0,33034); 
INSERT INTO `droplist` VALUES (1155, 4049,1,1,0,51480); 
INSERT INTO `droplist` VALUES (1155, 4050,1,1,0,60549); 
INSERT INTO `droplist` VALUES (1155, 4050,1,2,1,315543); 
INSERT INTO `droplist` VALUES (1155, 4051,1,1,0,38324); 
INSERT INTO `droplist` VALUES (1155, 4051,1,1,1,209701); 
INSERT INTO `droplist` VALUES (1155, 4130,1,1,1,93571); 
INSERT INTO `droplist` VALUES (1155, 4154,1,1,0,11854); 
INSERT INTO `droplist` VALUES (1155, 6037,5,20,0,287435); 
-- Hell Keeper Medusa 1154 
INSERT INTO `droplist` VALUES (1154, 57,2249,4493,0,700000); 
INSERT INTO `droplist` VALUES (1154, 1832,4,8,0,227334); 
INSERT INTO `droplist` VALUES (1154, 2397,1,1,0,127); 
INSERT INTO `droplist` VALUES (1154, 2402,1,1,0,204); 
INSERT INTO `droplist` VALUES (1154, 2406,1,1,0,87); 
INSERT INTO `droplist` VALUES (1154, 4049,1,1,1,500137); 
INSERT INTO `droplist` VALUES (1154, 4069,1,1,0,7073); 
INSERT INTO `droplist` VALUES (1154, 4070,1,1,0,11315); 
INSERT INTO `droplist` VALUES (1154, 4071,1,1,0,17887); 
INSERT INTO `droplist` VALUES (1154, 4613,1,1,1,9914); 
INSERT INTO `droplist` VALUES (1154, 4614,1,1,1,9914); 
INSERT INTO `droplist` VALUES (1154, 5809,1,1,0,8002); 
INSERT INTO `droplist` VALUES (1154, 5810,1,1,0,5715); 
INSERT INTO `droplist` VALUES (1154, 5816,1,1,0,8002); 
INSERT INTO `droplist` VALUES (1154, 6351,1,1,0,26226); 
-- Tomb Archon 1246 
INSERT INTO `droplist` VALUES (1246, 57,2210,4300,0,700000); 
INSERT INTO `droplist` VALUES (1246, 119,1,1,0,340); 
INSERT INTO `droplist` VALUES (1246, 855,1,1,0,453); 
INSERT INTO `droplist` VALUES (1246, 886,1,1,0,681); 
INSERT INTO `droplist` VALUES (1246, 1867,4,15,0,58839); 
INSERT INTO `droplist` VALUES (1246, 1867,4,29,1,500137); 
INSERT INTO `droplist` VALUES (1246, 1872,4,15,0,52646); 
INSERT INTO `droplist` VALUES (1246, 1882,1,3,0,9526); 
INSERT INTO `droplist` VALUES (1246, 1894,1,1,0,33176); 
INSERT INTO `droplist` VALUES (1246, 1918,1,1,0,40794); 
INSERT INTO `droplist` VALUES (1246, 1918,1,1,1,222283); 
INSERT INTO `droplist` VALUES (1246, 1920,1,1,0,32371); 
INSERT INTO `droplist` VALUES (1246, 2917,1,1,0,53749); 
INSERT INTO `droplist` VALUES (1246, 4042,1,1,0,3817); 
INSERT INTO `droplist` VALUES (1246, 4042,1,1,1,37184); 
INSERT INTO `droplist` VALUES (1246, 4127,1,1,0,23519); 
INSERT INTO `droplist` VALUES (1246, 4183,1,1,0,1107); 
INSERT INTO `droplist` VALUES (1246, 4965,1,1,0,1385); 
-- Tomb Inquisitor 1247 
INSERT INTO `droplist` VALUES (1247, 57,1973,3941,0,700000); 
INSERT INTO `droplist` VALUES (1247, 554,1,1,0,379); 
INSERT INTO `droplist` VALUES (1247, 600,1,1,0,379); 
INSERT INTO `droplist` VALUES (1247, 612,1,1,0,379); 
INSERT INTO `droplist` VALUES (1247, 1867,4,15,0,66684); 
INSERT INTO `droplist` VALUES (1247, 1872,4,15,0,45467); 
INSERT INTO `droplist` VALUES (1247, 1875,1,1,0,20839); 
INSERT INTO `droplist` VALUES (1247, 1881,1,2,0,6713); 
INSERT INTO `droplist` VALUES (1247, 2464,1,1,0,379); 
INSERT INTO `droplist` VALUES (1247, 4041,1,1,0,8530); 
INSERT INTO `droplist` VALUES (1247, 4049,1,1,1,268169); 
INSERT INTO `droplist` VALUES (1247, 4073,1,1,0,49988); 
INSERT INTO `droplist` VALUES (1247, 4097,1,1,0,49988); 
INSERT INTO `droplist` VALUES (1247, 4098,1,1,0,49988); 
INSERT INTO `droplist` VALUES (1247, 4100,1,1,0,49988); 
INSERT INTO `droplist` VALUES (1247, 4129,1,1,1,61974); 
INSERT INTO `droplist` VALUES (1247, 4131,1,1,0,7977); 
INSERT INTO `droplist` VALUES (1247, 4173,1,1,0,12179); 
INSERT INTO `droplist` VALUES (1247, 4613,1,1,1,9801); 
-- Nephilim Swordsman 1198 
INSERT INTO `droplist` VALUES (1198, 299,1,1,0,30); 
INSERT INTO `droplist` VALUES (1198, 301,1,1,0,39); 
INSERT INTO `droplist` VALUES (1198, 303,1,1,0,39); 
INSERT INTO `droplist` VALUES (1198, 1806,1,1,1,11324); 
INSERT INTO `droplist` VALUES (1198, 1868,6,21,0,348527); 
INSERT INTO `droplist` VALUES (1198, 1873,1,1,0,38472); 
INSERT INTO `droplist` VALUES (1198, 1876,1,1,0,46351); 
INSERT INTO `droplist` VALUES (1198, 1895,1,3,0,29419); 
INSERT INTO `droplist` VALUES (1198, 1920,1,1,1,177668); 
INSERT INTO `droplist` VALUES (1198, 2113,1,1,0,4746); 
INSERT INTO `droplist` VALUES (1198, 2114,1,1,0,4746); 
INSERT INTO `droplist` VALUES (1198, 2122,1,1,0,4323); 
INSERT INTO `droplist` VALUES (1198, 3034,1,1,0,1896); 
INSERT INTO `droplist` VALUES (1198, 4042,1,1,0,3803); 
INSERT INTO `droplist` VALUES (1198, 4593,1,1,1,18802); 
INSERT INTO `droplist` VALUES (1198, 4944,1,1,0,5702); 
INSERT INTO `droplist` VALUES (1198, 4968,1,1,0,1368); 
INSERT INTO `droplist` VALUES (1198, 6360,141,285,0,700000); 
INSERT INTO `droplist` VALUES (1198, 6361,298,601,0,700000); 
INSERT INTO `droplist` VALUES (1198, 6362,13,26,0,700000); 
-- Lilim Knight Errant 1177 
INSERT INTO `droplist` VALUES (1177, 299,1,1,0,30); 
INSERT INTO `droplist` VALUES (1177, 301,1,1,0,39); 
INSERT INTO `droplist` VALUES (1177, 303,1,1,0,39); 
INSERT INTO `droplist` VALUES (1177, 1806,1,1,1,11324); 
INSERT INTO `droplist` VALUES (1177, 1868,6,21,0,348527); 
INSERT INTO `droplist` VALUES (1177, 1873,1,1,0,38472); 
INSERT INTO `droplist` VALUES (1177, 1876,1,1,0,47632); 
INSERT INTO `droplist` VALUES (1177, 1895,1,3,0,29419); 
INSERT INTO `droplist` VALUES (1177, 1920,1,1,1,177668); 
INSERT INTO `droplist` VALUES (1177, 2113,1,1,0,4746); 
INSERT INTO `droplist` VALUES (1177, 2114,1,1,0,4746); 
INSERT INTO `droplist` VALUES (1177, 2122,1,1,0,4323); 
INSERT INTO `droplist` VALUES (1177, 3034,1,1,0,1896); 
INSERT INTO `droplist` VALUES (1177, 3927,2,4,0,3789); 
INSERT INTO `droplist` VALUES (1177, 4042,1,1,0,3803); 
INSERT INTO `droplist` VALUES (1177, 4594,1,1,1,18802); 
INSERT INTO `droplist` VALUES (1177, 4944,1,1,0,5702); 
INSERT INTO `droplist` VALUES (1177, 4968,1,1,0,1368); 
INSERT INTO `droplist` VALUES (1177, 6360,141,285,0,700000); 
INSERT INTO `droplist` VALUES (1177, 6361,298,601,0,700000); 
INSERT INTO `droplist` VALUES (1177, 6362,13,26,0,700000);