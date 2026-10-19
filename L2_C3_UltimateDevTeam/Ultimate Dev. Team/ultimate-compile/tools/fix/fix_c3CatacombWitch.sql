-- Author: Pivi_CT  /  Drop Data by Mastermind007 
-- Link: http://www.l2db.net 
-- Catacomb: Witch 60/70 
  
-- Crypt Guardian 1248 
UPDATE npc SET aggro = 0, level = 60, attackrange = 40, hp = 12220, mp = 1151, exp = 17280, sp = 1500, patk = 939, pdef = 369, matk = 479, mdef = 300 WHERE id = 1248; 
-- Nephilim Bishop 1200 
UPDATE npc SET aggro = 0, level = 60, attackrange = 40, hp = 12220, mp = 1151, exp = 17280, sp = 1500, patk = 939, pdef = 369, matk = 479, mdef = 300 WHERE id = 1200; 
-- Purgatory Shadow 1156 
UPDATE npc SET aggro = 0, level = 61, attackrange = 40, hp = 12952, mp = 1184, exp = 17861, sp = 1572, patk = 984, pdef = 379, matk = 505, mdef = 308 WHERE id = 1156; 
-- Lilim Priest 1179 
UPDATE npc SET aggro = 0, level = 60, attackrange = 40, hp = 12220, mp = 1151, exp = 17280, sp = 1500, patk = 939, pdef = 369, matk = 479, mdef = 300 WHERE id = 1179; 
-- Crypt Sage 1249 
UPDATE npc SET aggro = 0, level = 62, attackrange = 40, hp = 13296, mp = 1218, exp = 18451, sp = 1644, patk = 1030, pdef = 389, matk = 532, mdef = 316 WHERE id = 1249; 
-- Lilim Knight 1180 
UPDATE npc SET aggro = 0, level = 63, attackrange = 40, hp = 13208, mp = 1251, exp = 19051, sp = 1720, patk = 1077, pdef = 399, matk = 560, mdef = 324, rhand = 5800, lhand = 5799 WHERE id = 1180; 
-- Purgatory Tarantula 1157 
UPDATE npc SET aggro = 0, level = 64, attackrange = 500, hp = 13972, mp = 1285, exp = 19661, sp = 1800, patk = 2274, pdef = 449, matk = 589, mdef = 332 WHERE id = 1157; 
-- Nephilim Centurion 1201 
UPDATE npc SET aggro = 0, level = 63, attackrange = 40, hp = 15056, mp = 1251, exp = 19051, sp = 1720, patk = 1077, pdef = 399, matk = 560, mdef = 324 WHERE id = 1201; 
-- Nephilim Scout 1202 
UPDATE npc SET aggro = 0, level = 66, attackrange = 40, hp = 14188, mp = 1354, exp = 23174, sp = 2184, patk = 1114, pdef = 472, matk = 650, mdef = 348 WHERE id = 1202; 
-- Tomb Guardian 1250 
UPDATE npc SET aggro = 0, level = 65, attackrange = 40, hp = 13864, mp = 1320, exp = 20280, sp = 1884, patk = 1068, pdef = 419, matk = 619, mdef = 340 WHERE id = 1250; 
-- Lilim Assassin 1181 
UPDATE npc SET aggro = 0, level = 66, attackrange = 40, hp = 14188, mp = 1354, exp = 23174, sp = 2184, patk = 1114, pdef = 472, matk = 650, mdef = 348 WHERE id = 1181; 
-- Tomb Sage 1251 
UPDATE npc SET aggro = 0, level = 67, attackrange = 40, hp = 14976, mp = 1389, exp = 23881, sp = 2276, patk = 1276, pdef = 439, matk = 681, mdef = 357 WHERE id = 1251; 
-- Nephilim Archbishop 1203 
UPDATE npc SET aggro = 0, level = 69, attackrange = 40, hp = 15140, mp = 1459, exp = 22853, sp = 2236, patk = 1381, pdef = 460, matk = 747, mdef = 374 WHERE id = 1203; 
-- Purgatory Gargoyle 1159 
UPDATE npc SET aggro = 0, level = 68, attackrange = 40, hp = 15304, mp = 1423, exp = 23305, sp = 2252, patk = 1246, pdef = 464, matk = 714, mdef = 365 WHERE id = 1159; 
-- Lilim Soldier 1182 
UPDATE npc SET aggro = 0, level = 69, attackrange = 40, hp = 15140, mp = 1459, exp = 22853, sp = 2236, patk = 1381, pdef = 460, matk = 747, mdef = 374 WHERE id = 1182; 
-- Purgatory Liviona 1160 
UPDATE npc SET aggro = 0, level = 71, attackrange = 80, hp = 16260, mp = 874, exp = 23511, sp = 2332, patk = 1352, pdef = 301, matk = 609, mdef = 385 WHERE id = 1160; 
-- Nephilim Praetorian 1204 
UPDATE npc SET aggro = 0, level = 72, attackrange = 80, hp = 18300, mp = 894, exp = 23515, sp = 2332, patk = 1447, pdef = 268, matk = 613, mdef = 388 WHERE id = 1204; 
-- Lilim Knight Captain 1183 
UPDATE npc SET aggro = 0, level = 72, attackrange = 80, hp = 16052, mp = 894, exp = 23515, sp = 2332, patk = 1447, pdef = 268, matk = 613, mdef = 388, rhand = 5800, lhand = 5799 WHERE id = 1183; 
-- Crypt Guard 1252 
UPDATE npc SET aggro = 0, level = 70, attackrange = 80, hp = 15448, mp = 1494, exp = 23520, sp = 2332, patk = 1434, pdef = 471, matk = 780, mdef = 382 WHERE id = 1252; 
-- Anakim Nemesis Zakaron 10281 
DELETE FROM `npc` WHERE (`Id` = 10281); 
insert into npc values 
('10281', 'Anakim Nemesis Zakaron', 'karik_50_bi', '37.00', '136.00', '70', 'male', 'L2RaidBoss', 40, '700000', '5000', '40', '43', '40', '21', '43', '10', '60000', '2000', '7000', '999', '1000', '999', '253', '0', '253', '0', '0', '0', '80', '120', 'RaidBoss_clan', '100', 0); 

--Lilim Knight / sword and shield 
UPDATE npc SET rhand = 5800, lhand = 5799 WHERE id = 1180; 

--Lilim Knight Capt. / sword and shield 
UPDATE npc SET rhand = 5800, lhand = 5799 WHERE id = 1183;

-- Spawn data 
insert into spawnlist values 
(1249015243, "CatacombOfTheWitch", 1, 1248, 141740, 79830, -5434, 0, 0, 12161, 120, 0), 
(1249015244, "CatacombOfTheWitch", 1, 1200, 141727, 79539, -5434, 0, 0, 14120, 120, 0), 
(1249015245, "CatacombOfTheWitch", 1, 1156, 141999, 79566, -5434, 0, 0, 18830, 120, 0), 
(1249015246, "CatacombOfTheWitch", 1, 1179, 142037, 79809, -5434, 0, 0, 14019, 120, 0), 
(1249015247, "CatacombOfTheWitch", 1, 1248, 140921, 81440, -5434, 0, 0, 2496, 120, 0), 
(1249015248, "CatacombOfTheWitch", 1, 1248, 140796, 82222, -5432, 0, 0, 17682, 120, 0), 
(1249015249, "CatacombOfTheWitch", 1, 1200, 140638, 82005, -5434, 0, 0, 1040, 120, 0), 
(1249015250, "CatacombOfTheWitch", 1, 1200, 140607, 81141, -5434, 0, 0, 50441, 120, 0), 
(1249015251, "CatacombOfTheWitch", 1, 1156, 140577, 81409, -5434, 0, 0, 18047, 120, 0), 
(1249015252, "CatacombOfTheWitch", 1, 1156, 140959, 81637, -5434, 0, 0, 4016, 120, 0), 
(1249015253, "CatacombOfTheWitch", 1, 1179, 140952, 81987, -5434, 0, 0, 16749, 120, 0), 
(1249015254, "CatacombOfTheWitch", 1, 1179, 140583, 81681, -5434, 0, 0, 62852, 120, 0), 
(1249015360, "CatacombOfTheWitch", 1, 1248, 142558, 82197, -5434, 0, 0, 32483, 120, 0), 
(1249015363, "CatacombOfTheWitch", 1, 1200, 142498, 82415, -5434, 0, 0, 35067, 120, 0), 
(1249015364, "CatacombOfTheWitch", 1, 1156, 143014, 82628, -5432, 0, 0, 37, 120, 0), 
(1249015366, "CatacombOfTheWitch", 1, 1179, 143217, 82343, -5434, 0, 0, 7538, 120, 0), 
(1249015367, "CatacombOfTheWitch", 1, 1248, 143022, 81810, -5434, 0, 0, 46994, 120, 0), 
(1249015368, "CatacombOfTheWitch", 1, 1200, 142787, 81550, -5432, 0, 0, 38901, 120, 0), 
(1249015369, "CatacombOfTheWitch", 1, 1156, 142831, 82037, -5434, 0, 0, 15681, 120, 0), 
(1249015370, "CatacombOfTheWitch", 1, 1179, 142839, 82275, -5432, 0, 0, 15782, 120, 0), 
(1249015371, "CatacombOfTheWitch", 1, 1200, 142760, 83171, -5434, 0, 0, 18184, 120, 0), 
(1249015372, "CatacombOfTheWitch", 1, 1156, 142917, 83249, -5432, 0, 0, 4813, 120, 0), 
(1249015373, "CatacombOfTheWitch", 1, 1248, 143004, 83083, -5434, 0, 0, 52700, 120, 0), 
(1249015374, "CatacombOfTheWitch", 1, 1179, 142591, 82816, -5434, 0, 0, 37007, 120, 0), 
(1249015375, "CatacombOfTheWitch", 1, 1248, 142907, 84088, -5434, 0, 0, 56130, 120, 0), 
(1249015376, "CatacombOfTheWitch", 1, 1200, 142643, 84246, -5432, 0, 0, 28445, 120, 0), 
(1249015377, "CatacombOfTheWitch", 1, 1156, 143764, 84114, -5434, 0, 0, 33065, 120, 0), 
(1249015378, "CatacombOfTheWitch", 1, 1179, 143790, 84475, -5434, 0, 0, 35323, 120, 0), 
(1249015379, "CatacombOfTheWitch", 1, 1248, 143584, 84465, -5434, 0, 0, 33299, 120, 0), 
(1249015380, "CatacombOfTheWitch", 1, 1156, 143220, 84458, -5434, 0, 0, 32503, 120, 0), 
(1249015382, "CatacombOfTheWitch", 1, 1179, 142759, 84430, -5434, 0, 0, 33583, 120, 0), 
(1249015383, "CatacombOfTheWitch", 1, 1200, 143231, 84132, -5434, 0, 0, 58218, 120, 0), 
(1249015384, "CatacombOfTheWitch", 1, 1179, 143397, 79860, -5434, 0, 0, 14403, 120, 0), 
(1249015392, "CatacombOfTheWitch", 1, 1248, 143795, 79795, -5434, 0, 0, 15941, 120, 0), 
(1249015386, "CatacombOfTheWitch", 1, 1200, 144048, 79813, -5434, 0, 0, 32081, 120, 0), 
(1249015387, "CatacombOfTheWitch", 1, 1156, 144060, 79515, -5434, 0, 0, 35530, 120, 0), 
(1249015388, "CatacombOfTheWitch", 1, 1200, 143739, 79497, -5434, 0, 0, 30059, 120, 0), 
(1249015389, "CatacombOfTheWitch", 1, 1156, 143310, 79497, -5434, 0, 0, 32745, 120, 0), 
(1249015390, "CatacombOfTheWitch", 1, 1179, 143104, 79703, -5432, 0, 0, 24014, 120, 0), 
(1249015391, "CatacombOfTheWitch", 1, 1248, 143453, 79724, -5432, 0, 0, 2543, 120, 0), 
(1249015393, "CatacombOfTheWitch", 1, 1249, 145145, 81859, -5434, 0, 0, 33264, 120, 0), 
(1249015394, "CatacombOfTheWitch", 1, 1180, 145178, 81684, -5434, 0, 0, 36455, 120, 0), 
(1249015395, "CatacombOfTheWitch", 1, 1201, 145297, 81390, -5432, 0, 0, 16191, 120, 0), 
(1249015396, "CatacombOfTheWitch", 1, 1157, 145609, 81386, -5434, 0, 0, 12679, 120, 0), 
(1249015397, "CatacombOfTheWitch", 1, 1249, 145568, 81653, -5434, 0, 0, 17977, 120, 0), 
(1249015399, "CatacombOfTheWitch", 1, 1201, 145519, 82349, -5434, 0, 0, 17571, 120, 0), 
(1249015400, "CatacombOfTheWitch", 1, 1157, 145248, 82455, -5434, 0, 0, 20271, 120, 0), 
(1249015401, "CatacombOfTheWitch", 1, 1249, 145285, 79840, -5434, 0, 0, 18865, 120, 0), 
(1249015402, "CatacombOfTheWitch", 1, 1180, 145508, 79874, -5434, 0, 0, 9031, 120, 0), 
(1249015403, "CatacombOfTheWitch", 1, 1201, 145553, 79534, -5434, 0, 0, 61626, 120, 0), 
(1249015404, "CatacombOfTheWitch", 1, 1157, 145270, 79524, -5434, 0, 0, 7038, 120, 0), 
(1249015405, "CatacombOfTheWitch", 1, 1249, 146359, 79773, -5434, 0, 0, 24160, 120, 0), 
(1249015406, "CatacombOfTheWitch", 1, 1180, 146667, 79781, -5434, 0, 0, 6895, 120, 0), 
(1249015408, "CatacombOfTheWitch", 1, 1201, 146310, 79579, -5434, 0, 0, 27015, 120, 0), 
(1249015409, "CatacombOfTheWitch", 1, 1157, 146582, 79554, -5434, 0, 0, 4605, 120, 0), 
(1249015410, "CatacombOfTheWitch", 1, 1249, 146377, 80406, -5434, 0, 0, 53070, 120, 0), 
(1249015414, "CatacombOfTheWitch", 1, 1180, 147328, 80767, -5434, 0, 0, 3763, 120, 0), 
(1249015415, "CatacombOfTheWitch", 1, 1201, 147420, 80537, -5432, 0, 0, 58609, 120, 0), 
(1249015418, "CatacombOfTheWitch", 1, 1180, 146790, 80428, -5434, 0, 0, 32141, 120, 0), 
(1249015419, "CatacombOfTheWitch", 1, 1201, 146393, 80736, -5434, 0, 0, 51364, 120, 0), 
(1249015420, "CatacombOfTheWitch", 1, 1157, 146721, 80796, -5434, 0, 0, 61854, 120, 0), 
(1249015421, "CatacombOfTheWitch", 1, 1202, 149030, 80139, -5434, 0, 0, 39201, 120, 0), 
(1249015422, "CatacombOfTheWitch", 1, 1202, 149128, 80596, -5432, 0, 0, 14392, 120, 0), 
(1249015423, "CatacombOfTheWitch", 1, 1250, 149309, 80612, -5434, 0, 0, 10459, 120, 0), 
(1249015424, "CatacombOfTheWitch", 1, 1250, 149424, 79817, -5434, 0, 0, 54993, 120, 0), 
(1249015425, "CatacombOfTheWitch", 1, 1157, 149038, 79470, -5434, 0, 0, 711, 120, 0), 
(1249015426, "CatacombOfTheWitch", 1, 1157, 149206, 80056, -5432, 0, 0, 10849, 120, 0), 
(1249015427, "CatacombOfTheWitch", 1, 1181, 149327, 80365, -5432, 0, 0, 13282, 120, 0), 
(1249015428, "CatacombOfTheWitch", 1, 1181, 149196, 79656, -5432, 0, 0, 24236, 120, 0), 
(1249015429, "CatacombOfTheWitch", 1, 1251, 150928, 79583, -5434, 0, 0, 32074, 120, 0), 
(1249015430, "CatacombOfTheWitch", 1, 1203, 150914, 79797, -5434, 0, 0, 32921, 120, 0), 
(1249015431, "CatacombOfTheWitch", 1, 1159, 151071, 79895, -5432, 0, 0, 4142, 120, 0), 
(1249015432, "CatacombOfTheWitch", 1, 1182, 151245, 79540, -5434, 0, 0, 22477, 120, 0), 
(1249015433, "CatacombOfTheWitch", 1, 1251, 149275, 81438, -5434, 0, 0, 48390, 120, 0), 
(1249015434, "CatacombOfTheWitch", 1, 1251, 150144, 81712, -5432, 0, 0, 1655, 120, 0), 
(1249015435, "CatacombOfTheWitch", 1, 1203, 150387, 81892, -5432, 0, 0, 6650, 120, 0), 
(1249015436, "CatacombOfTheWitch", 1, 1203, 149644, 81999, -5434, 0, 0, 64610, 120, 0), 
(1249015437, "CatacombOfTheWitch", 1, 1203, 149091, 81535, -5434, 0, 0, 43551, 120, 0), 
(1249015438, "CatacombOfTheWitch", 1, 1251, 148836, 81993, -5434, 0, 0, 1018, 120, 0), 
(1249015440, "CatacombOfTheWitch", 1, 1159, 149067, 81809, -5432, 0, 0, 57414, 120, 0), 
(1249015441, "CatacombOfTheWitch", 1, 1182, 149449, 81569, -5434, 0, 0, 31915, 120, 0), 
(1249015442, "CatacombOfTheWitch", 1, 1159, 149840, 81573, -5434, 0, 0, 545, 120, 0), 
(1249015443, "CatacombOfTheWitch", 1, 1182, 150231, 81595, -5434, 0, 0, 61302, 120, 0), 
(1249015444, "CatacombOfTheWitch", 1, 1159, 149894, 82043, -5434, 0, 0, 21842, 120, 0), 
(1249015445, "CatacombOfTheWitch", 1, 1182, 149076, 82014, -5434, 0, 0, 27236, 120, 0), 
(1249015447, "CatacombOfTheWitch", 1, 1251, 151093, 84016, -5432, 0, 0, 49636, 120, 0), 
(1249015448, "CatacombOfTheWitch", 1, 1203, 151090, 84440, -5432, 0, 0, 21012, 120, 0), 
(1249015449, "CatacombOfTheWitch", 1, 1159, 150803, 84286, -5432, 0, 0, 32564, 120, 0), 
(1249015450, "CatacombOfTheWitch", 1, 1182, 151285, 84296, -5432, 0, 0, 32181, 120, 0), 
(1249015451, "CatacombOfTheWitch", 1, 1251, 151089, 85156, -5432, 0, 0, 49770, 120, 0), 
(1249015452, "CatacombOfTheWitch", 1, 1203, 151299, 85368, -5432, 0, 0, 10631, 120, 0), 
(1249015453, "CatacombOfTheWitch", 1, 1159, 151117, 85566, -5432, 0, 0, 23096, 120, 0), 
(1249015454, "CatacombOfTheWitch", 1, 1182, 150913, 85490, -5434, 0, 0, 43150, 120, 0), 
(1249015455, "CatacombOfTheWitch", 1, 1202, 148056, 83849, -5434, 0, 0, 7330, 120, 0), 
(1249015456, "CatacombOfTheWitch", 1, 1250, 148070, 83655, -5434, 0, 0, 61262, 120, 0), 
(1249015458, "CatacombOfTheWitch", 1, 1157, 147783, 83322, -5432, 0, 0, 39287, 120, 0), 
(1249015459, "CatacombOfTheWitch", 1, 1181, 147743, 83593, -5434, 0, 0, 48862, 120, 0), 
(1249015460, "CatacombOfTheWitch", 1, 1181, 148036, 84293, -5434, 0, 0, 47285, 120, 0), 
(1249015461, "CatacombOfTheWitch", 1, 1157, 147742, 84372, -5434, 0, 0, 40314, 120, 0), 
(1249015462, "CatacombOfTheWitch", 1, 1250, 147743, 83986, -5434, 0, 0, 15531, 120, 0), 
(1249015463, "CatacombOfTheWitch", 1, 1202, 146792, 84286, -5432, 0, 0, 383, 120, 0), 
(1249015464, "CatacombOfTheWitch", 1, 1250, 146488, 84133, -5432, 0, 0, 43131, 120, 0), 
(1249015465, "CatacombOfTheWitch", 1, 1181, 146285, 84263, -5432, 0, 0, 26827, 120, 0), 
(1249015466, "CatacombOfTheWitch", 1, 1157, 146464, 84444, -5432, 0, 0, 47170, 120, 0), 
(1249015467, "CatacombOfTheWitch", 1, 1181, 146564, 82684, -5432, 0, 0, 13582, 120, 0), 
(1249015468, "CatacombOfTheWitch", 1, 1250, 146434, 82608, -5432, 0, 0, 19830, 120, 0), 
(1249015469, "CatacombOfTheWitch", 1, 1157, 146297, 82286, -5434, 0, 0, 63748, 120, 0), 
(1249015470, "CatacombOfTheWitch", 1, 1250, 146276, 81958, -5434, 0, 0, 2153, 120, 0), 
(1249015471, "CatacombOfTheWitch", 1, 1202, 146323, 81518, -5434, 0, 0, 3969, 120, 0), 
(1249015472, "CatacombOfTheWitch", 1, 1181, 146499, 81482, -5432, 0, 0, 14609, 120, 0), 
(1249015473, "CatacombOfTheWitch", 1, 1202, 146602, 81776, -5434, 0, 0, 15634, 120, 0), 
(1249015474, "CatacombOfTheWitch", 1, 1157, 146631, 82239, -5434, 0, 0, 48132, 120, 0), 
(1249015475, "CatacombOfTheWitch", 1, 1249, 145624, 84278, -5432, 0, 0, 795, 120, 0), 
(1249015476, "CatacombOfTheWitch", 1, 1180, 145435, 84101, -5432, 0, 0, 40618, 120, 0), 
(1249015478, "CatacombOfTheWitch", 1, 1157, 145202, 84270, -5432, 0, 0, 64759, 120, 0), 
(1249015479, "CatacombOfTheWitch", 1, 1201, 145358, 84493, -5432, 0, 0, 21486, 120, 0), 
(1249015480, "CatacombOfTheWitch", 1, 1202, 145385, 85123, -5432, 0, 0, 49084, 120, 0), 
(1249015481, "CatacombOfTheWitch", 1, 1250, 145208, 85330, -5432, 0, 0, 28467, 120, 0), 
(1249015483, "CatacombOfTheWitch", 1, 1157, 145602, 85382, -5432, 0, 0, 64600, 120, 0), 
(1249015484, "CatacombOfTheWitch", 1, 1251, 146164, 85378, -5432, 0, 0, 33035, 120, 0), 
(1249015485, "CatacombOfTheWitch", 1, 1203, 146435, 85580, -5432, 0, 0, 9602, 120, 0), 
(1249015486, "CatacombOfTheWitch", 1, 1159, 146646, 85401, -5432, 0, 0, 60327, 120, 0), 
(1249015487, "CatacombOfTheWitch", 1, 1182, 146498, 85176, -5432, 0, 0, 43083, 120, 0), 
(1249015488, "CatacombOfTheWitch", 1, 1202, 144654, 85370, -5434, 0, 0, 64607, 120, 0), 
(1249015489, "CatacombOfTheWitch", 1, 1202, 144505, 86276, -5432, 0, 0, 19896, 120, 0), 
(1249015490, "CatacombOfTheWitch", 1, 1157, 144231, 86178, -5434, 0, 0, 16645, 120, 0), 
(1249015491, "CatacombOfTheWitch", 1, 1250, 144228, 85879, -5434, 0, 0, 12991, 120, 0), 
(1249015492, "CatacombOfTheWitch", 1, 1181, 144270, 85512, -5434, 0, 0, 10517, 120, 0), 
(1249015493, "CatacombOfTheWitch", 1, 1157, 144325, 85211, -5434, 0, 0, 10920, 120, 0), 
(1249015494, "CatacombOfTheWitch", 1, 1250, 144555, 85170, -5434, 0, 0, 63695, 120, 0), 
(1249015495, "CatacombOfTheWitch", 1, 1181, 144565, 85818, -5434, 0, 0, 12660, 120, 0), 
(1249015496, "CatacombOfTheWitch", 1, 1160, 149457, 85978, -5434, 0, 0, 34731, 120, 0), 
(1249015497, "CatacombOfTheWitch", 1, 1160, 149718, 85258, -5432, 0, 0, 15345, 120, 0), 
(1249015498, "CatacombOfTheWitch", 1, 1160, 149801, 86374, -5432, 0, 0, 16384, 120, 0), 
(1249015499, "CatacombOfTheWitch", 1, 1204, 149702, 86732, -5434, 0, 0, 19556, 120, 0), 
(1249015500, "CatacombOfTheWitch", 1, 1204, 150087, 86299, -5434, 0, 0, 30263, 120, 0), 
(1249015501, "CatacombOfTheWitch", 1, 1204, 149990, 85231, -5434, 0, 0, 16553, 120, 0), 
(1249015502, "CatacombOfTheWitch", 1, 1183, 149829, 85411, -5432, 0, 0, 23996, 120, 0), 
(1249015503, "CatacombOfTheWitch", 1, 1183, 150078, 86024, -5434, 0, 0, 9162, 120, 0), 
(1249015505, "CatacombOfTheWitch", 1, 1183, 149877, 86697, -5432, 0, 0, 16439, 120, 0), 
(1249015506, "CatacombOfTheWitch", 1, 1252, 149376, 85968, -5434, 0, 0, 43039, 120, 0), 
(1249015507, "CatacombOfTheWitch", 1, 1252, 149910, 85701, -5434, 0, 0, 16069, 120, 0), 
(1249015508, "CatacombOfTheWitch", 1, 1252, 149689, 86329, -5434, 0, 0, 20362, 120, 0), 
(1249015509, "CatacombOfTheWitch", 1, 1204, 149808, 87598, -5432, 0, 0, 50282, 120, 0), 
(1249015510, "CatacombOfTheWitch", 1, 1183, 149955, 87829, -5432, 0, 0, 50768, 120, 0), 
(1249015511, "CatacombOfTheWitch", 1, 1252, 149677, 87870, -5434, 0, 0, 32305, 120, 0), 
(1249015512, "CatacombOfTheWitch", 1, 1160, 149647, 87683, -5432, 0, 0, 55843, 120, 0), 
(1249015513, "CatacombOfTheWitch", 1, 10281, 151074, 88234, -5432, 0, 0, 17380, 120, 0), 
(1249015514, "CatacombOfTheWitch", 1, 1160, 151084, 89709, -5432, 0, 0, 51138, 120, 0), 
(1249015515, "CatacombOfTheWitch", 1, 1204, 150850, 89956, -5432, 0, 0, 22396, 120, 0), 
(1249015516, "CatacombOfTheWitch", 1, 1183, 151046, 90171, -5432, 0, 0, 41528, 120, 0), 
(1249015517, "CatacombOfTheWitch", 1, 1252, 151267, 89995, -5432, 0, 0, 36169, 120, 0), 
(1249015518, "CatacombOfTheWitch", 1, 1160, 149296, 89588, -5432, 0, 0, 58358, 120, 0), 
(1249015519, "CatacombOfTheWitch", 1, 1160, 148843, 89633, -5432, 0, 0, 28391, 120, 0), 
(1249015520, "CatacombOfTheWitch", 1, 1204, 148286, 89599, -5432, 0, 0, 35557, 120, 0), 
(1249015521, "CatacombOfTheWitch", 1, 1204, 148851, 89868, -5434, 0, 0, 393, 120, 0), 
(1249015522, "CatacombOfTheWitch", 1, 1183, 149147, 89758, -5432, 0, 0, 63170, 120, 0), 
(1249015523, "CatacombOfTheWitch", 1, 1183, 148587, 89804, -5434, 0, 0, 28477, 120, 0), 
(1249015524, "CatacombOfTheWitch", 1, 1252, 148247, 89755, -5432, 0, 0, 34439, 120, 0), 
(1249015525, "CatacombOfTheWitch", 1, 1252, 148730, 89475, -5434, 0, 0, 23838, 120, 0), 
(1249015526, "CatacombOfTheWitch", 1, 1252, 147415, 88816, -5434, 0, 0, 17650, 120, 0), 
(1249015527, "CatacombOfTheWitch", 1, 1252, 147457, 88339, -5434, 0, 0, 1454, 120, 0), 
(1249015528, "CatacombOfTheWitch", 1, 1252, 146409, 88727, -5434, 0, 0, 3969, 120, 0), 
(1249015529, "CatacombOfTheWitch", 1, 1183, 146625, 88182, -5434, 0, 0, 55783, 120, 0), 
(1249015530, "CatacombOfTheWitch", 1, 1183, 147469, 88651, -5434, 0, 0, 5419, 120, 0), 
(1249015531, "CatacombOfTheWitch", 1, 1183, 147900, 88541, -5432, 0, 0, 62892, 120, 0), 
(1249015533, "CatacombOfTheWitch", 1, 1204, 147084, 88553, -5432, 0, 0, 4297, 120, 0), 
(1249015534, "CatacombOfTheWitch", 1, 1204, 146374, 88423, -5434, 0, 0, 1266, 120, 0), 
(1249015535, "CatacombOfTheWitch", 1, 1160, 146488, 88484, -5432, 0, 0, 5125, 120, 0), 
(1249015536, "CatacombOfTheWitch", 1, 1160, 146764, 88290, -5434, 0, 0, 62725, 120, 0), 
(1249015537, "CatacombOfTheWitch", 1, 1160, 147227, 88414, -5434, 0, 0, 3471, 120, 0), 
(1249015538, "CatacombOfTheWitch", 1, 1183, 149717, 88859, -5432, 0, 0, 34435, 120, 0), 
(1249015539, "CatacombOfTheWitch", 1, 1204, 150002, 88648, -5432, 0, 0, 34910, 120, 0), 
(1249015540, "CatacombOfTheWitch", 1, 1252, 150149, 88873, -5432, 0, 0, 27096, 120, 0), 
(1249015541, "CatacombOfTheWitch", 1, 1160, 150007, 89053, -5432, 0, 0, 29169, 120, 0), 
(1249015542, "CatacombOfTheWitch", 1, 1252, 147392, 87353, -5432, 0, 0, 32471, 120, 0), 
(1249015543, "CatacombOfTheWitch", 1, 1252, 147918, 87073, -5434, 0, 0, 57514, 120, 0), 
(1249015544, "CatacombOfTheWitch", 1, 1183, 148278, 87529, -5434, 0, 0, 36736, 120, 0), 
(1249015545, "CatacombOfTheWitch", 1, 1183, 147516, 87204, -5432, 0, 0, 36839, 120, 0), 
(1249015548, "CatacombOfTheWitch", 1, 1204, 148184, 87086, -5434, 0, 0, 38157, 120, 0), 
(1249015547, "CatacombOfTheWitch", 1, 1204, 148524, 87248, -5432, 0, 0, 32295, 120, 0), 
(1249015549, "CatacombOfTheWitch", 1, 1160, 148112, 87310, -5432, 0, 0, 55417, 120, 0), 
(1249015550, "CatacombOfTheWitch", 1, 1160, 147750, 87375, -5434, 0, 0, 30996, 120, 0), 
(1249015551, "CatacombOfTheWitch", 1, 1251, 146667, 89982, -5432, 0, 0, 436, 120, 0), 
(1249015552, "CatacombOfTheWitch", 1, 1203, 146516, 89815, -5432, 0, 0, 11548, 120, 0), 
(1249015553, "CatacombOfTheWitch", 1, 1159, 146308, 89952, -5432, 0, 0, 26693, 120, 0), 
(1249015554, "CatacombOfTheWitch", 1, 1182, 146472, 90180, -5432, 0, 0, 47960, 120, 0), 
(1249015555, "CatacombOfTheWitch", 1, 1251, 145575, 89984, -5432, 0, 0, 140, 120, 0), 
(1249015556, "CatacombOfTheWitch", 1, 1203, 145413, 89821, -5432, 0, 0, 40992, 120, 0), 
(1249015557, "CatacombOfTheWitch", 1, 1159, 145195, 89961, -5432, 0, 0, 26814, 120, 0), 
(1249015558, "CatacombOfTheWitch", 1, 1182, 145388, 90195, -5432, 0, 0, 49292, 120, 0), 
(1249015559, "CatacombOfTheWitch", 1, 1251, 145477, 88289, -5434, 0, 0, 20541, 120, 0), 
(1249015560, "CatacombOfTheWitch", 1, 1251, 145316, 87921, -5434, 0, 0, 45871, 120, 0), 
(1249015561, "CatacombOfTheWitch", 1, 1203, 144542, 88087, -5432, 0, 0, 30082, 120, 0), 
(1249015562, "CatacombOfTheWitch", 1, 1203, 145170, 88262, -5434, 0, 0, 4759, 120, 0), 
(1249015563, "CatacombOfTheWitch", 1, 1159, 145014, 87910, -5434, 0, 0, 40616, 120, 0), 
(1249015564, "CatacombOfTheWitch", 1, 1159, 144611, 88013, -5432, 0, 0, 31386, 120, 0), 
(1249015565, "CatacombOfTheWitch", 1, 1182, 144891, 88029, -5432, 0, 0, 47029, 120, 0), 
(1249015566, "CatacombOfTheWitch", 1, 1182, 145335, 88056, -5432, 0, 0, 32467, 120, 0), 
(1249015567, "CatacombOfTheWitch", 1, 1251, 143566, 87951, -5434, 0, 0, 2676, 120, 0), 
(1249015568, "CatacombOfTheWitch", 1, 1251, 143188, 88252, -5432, 0, 0, 25562, 120, 0), 
(1249015570, "CatacombOfTheWitch", 1, 1251, 143378, 89100, -5432, 0, 0, 14383, 120, 0), 
(1249015571, "CatacombOfTheWitch", 1, 1203, 143165, 89149, -5434, 0, 0, 16748, 120, 0), 
(1249015572, "CatacombOfTheWitch", 1, 1203, 143333, 88732, -5432, 0, 0, 55975, 120, 0), 
(1249015573, "CatacombOfTheWitch", 1, 1203, 143099, 87794, -5434, 0, 0, 21271, 120, 0), 
(1249015574, "CatacombOfTheWitch", 1, 1159, 142967, 88338, -5434, 0, 0, 21221, 120, 0), 
(1249015575, "CatacombOfTheWitch", 1, 1250, 143124, 88667, -5434, 0, 0, 11827, 120, 0), 
(1249015576, "CatacombOfTheWitch", 1, 1159, 143211, 88852, -5432, 0, 0, 11827, 120, 0), 
(1249015577, "CatacombOfTheWitch", 1, 1159, 143539, 88341, -5434, 0, 0, 61114, 120, 0), 
(1249015578, "CatacombOfTheWitch", 1, 1182, 142932, 88593, -5434, 0, 0, 28828, 120, 0), 
(1249015579, "CatacombOfTheWitch", 1, 1182, 143656, 88060, -5434, 0, 0, 58300, 120, 0), 
(1249015580, "CatacombOfTheWitch", 1, 1182, 143485, 88578, -5434, 0, 0, 32592, 120, 0), 
(1249015581, "CatacombOfTheWitch", 1, 1251, 141065, 89979, -5432, 0, 0, 64692, 120, 0), 
(1249015582, "CatacombOfTheWitch", 1, 1203, 140803, 89738, -5432, 0, 0, 40524, 120, 0), 
(1249015583, "CatacombOfTheWitch", 1, 1159, 140556, 89978, -5432, 0, 0, 25125, 120, 0), 
(1249015584, "CatacombOfTheWitch", 1, 1182, 140772, 90183, -5432, 0, 0, 52265, 120, 0), 
(1249015588, "CatacombOfTheWitch", 1, 1157, 141440, 86783, -5432, 0, 0, 55595, 120, 0), 
(1249015589, "CatacombOfTheWitch", 1, 1157, 140684, 86595, -5434, 0, 0, 44905, 120, 0), 
(1249015591, "CatacombOfTheWitch", 1, 1202, 140871, 86741, -5432, 0, 0, 57988, 120, 0), 
(1249015592, "CatacombOfTheWitch", 1, 1202, 141631, 86680, -5434, 0, 0, 2366, 120, 0), 
(1249015598, "CatacombOfTheWitch", 1, 1201, 140812, 85170, -5432, 0, 0, 39730, 120, 0), 
(1249015594, "CatacombOfTheWitch", 1, 1250, 141185, 86975, -5434, 0, 0, 33241, 120, 0), 
(1249015595, "CatacombOfTheWitch", 1, 1250, 141120, 86797, -5432, 0, 0, 45561, 120, 0), 
(1249015599, "CatacombOfTheWitch", 1, 1157, 140620, 85366, -5432, 0, 0, 63499, 120, 0), 
(1249015601, "CatacombOfTheWitch", 1, 1249, 142400, 85248, -5434, 0, 0, 34381, 120, 0), 
(1249015602, "CatacombOfTheWitch", 1, 1201, 142922, 85558, -5434, 0, 0, 8257, 120, 0), 
(1249015603, "CatacombOfTheWitch", 1, 1157, 143242, 85559, -5434, 0, 0, 14191, 120, 0), 
(1249015605, "CatacombOfTheWitch", 1, 1249, 143542, 85534, -5434, 0, 0, 32954, 120, 0), 
(1249015606, "CatacombOfTheWitch", 1, 1180, 143463, 85271, -5432, 0, 0, 37604, 120, 0), 
(1249015607, "CatacombOfTheWitch", 1, 1201, 143189, 85318, -5432, 0, 0, 28224, 120, 0), 
(1249015608, "CatacombOfTheWitch", 1, 1157, 142800, 85360, -5432, 0, 0, 33120, 120, 0), 
(1249015609, "CatacombOfTheWitch", 1, 1248, 140790, 84507, -5432, 0, 0, 19022, 120, 0), 
(1249015610, "CatacombOfTheWitch", 1, 1200, 140984, 84332, -5432, 0, 0, 57880, 120, 0), 
(1249015611, "CatacombOfTheWitch", 1, 1156, 140808, 84064, -5432, 0, 0, 43018, 120, 0), 
(1249015612, "CatacombOfTheWitch", 1, 1179, 140636, 84284, -5432, 0, 0, 3840, 120, 0); 

-- Delete any existing drop data in DB 
Delete from droplist where mobid in (1252,1183,1204,1160,1182,1159,1203,1251,1181,1250,1202,1201,1157,1180,1249,1179,1156,1200,1248);

-- Add drop data for mobs 
-- Crypt Guardian 
INSERT INTO `droplist` VALUES (1248, 57,2155,4306,0,700000); 
INSERT INTO `droplist` VALUES (1248, 503,1,1,0,261); 
INSERT INTO `droplist` VALUES (1248, 633,1,1,0,373); 
INSERT INTO `droplist` VALUES (1248, 673,1,1,0,373); 
INSERT INTO `droplist` VALUES (1248, 1864,7,25,0,200054); 
INSERT INTO `droplist` VALUES (1248, 1869,4,13,0,200054); 
INSERT INTO `droplist` VALUES (1248, 1880,1,2,0,52646); 
INSERT INTO `droplist` VALUES (1248, 2415,1,1,0,261); 
INSERT INTO `droplist` VALUES (1248, 4043,1,1,0,33386); 
INSERT INTO `droplist` VALUES (1248, 4050,1,2,1,318558); 
INSERT INTO `droplist` VALUES (1248, 4051,1,1,1,211922); 
INSERT INTO `droplist` VALUES (1248, 4072,1,1,0,44714); 
INSERT INTO `droplist` VALUES (1248, 4101,1,1,0,57355); 
INSERT INTO `droplist` VALUES (1248, 4102,1,1,0,44714); 
INSERT INTO `droplist` VALUES (1248, 4130,1,1,1,94454); 
INSERT INTO `droplist` VALUES (1248, 4159,1,1,0,2880); 
INSERT INTO `droplist` VALUES (1248, 4439,1,1,0,60403); 
INSERT INTO `droplist` VALUES (1248, 5550,1,1,0,13488); 
INSERT INTO `droplist` VALUES (1248, 6036,1,1,0,8114); 
-- Nephilim Bishop 
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
-- Purgatory Shadow 
INSERT INTO `droplist` VALUES (1156, 57,2277,4550,0,700000); 
INSERT INTO `droplist` VALUES (1156, 554,1,1,0,396); 
INSERT INTO `droplist` VALUES (1156, 600,1,1,0,396); 
INSERT INTO `droplist` VALUES (1156, 612,1,1,0,396); 
INSERT INTO `droplist` VALUES (1156, 948,1,1,1,15311); 
INSERT INTO `droplist` VALUES (1156, 1343,20,60,0,256305); 
INSERT INTO `droplist` VALUES (1156, 1865,4,13,0,114186); 
INSERT INTO `droplist` VALUES (1156, 1870,4,13,0,37047); 
INSERT INTO `droplist` VALUES (1156, 1875,1,1,0,21745); 
INSERT INTO `droplist` VALUES (1156, 1879,1,3,0,7814); 
INSERT INTO `droplist` VALUES (1156, 2464,1,1,0,396); 
INSERT INTO `droplist` VALUES (1156, 4040,1,1,0,20368); 
INSERT INTO `droplist` VALUES (1156, 4056,1,1,1,105849); 
INSERT INTO `droplist` VALUES (1156, 4073,1,1,0,52151); 
INSERT INTO `droplist` VALUES (1156, 4097,1,1,0,52151); 
INSERT INTO `droplist` VALUES (1156, 4098,1,1,0,52151); 
INSERT INTO `droplist` VALUES (1156, 4100,1,1,0,52151); 
INSERT INTO `droplist` VALUES (1156, 4131,1,1,1,48961); 
INSERT INTO `droplist` VALUES (1156, 4160,1,1,0,2910); 
-- Lilim Priest 
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
-- Crypt Sage 
INSERT INTO `droplist` VALUES (1249, 57,2365,4713,0,700000); 
INSERT INTO `droplist` VALUES (1249, 358,1,1,0,64); 
INSERT INTO `droplist` VALUES (1249, 2380,1,1,0,103); 
INSERT INTO `droplist` VALUES (1249, 2381,1,1,0,44); 
INSERT INTO `droplist` VALUES (1249, 4052,1,1,1,198861); 
INSERT INTO `droplist` VALUES (1249, 4053,1,1,1,235913); 
INSERT INTO `droplist` VALUES (1249, 4078,1,1,0,15062); 
INSERT INTO `droplist` VALUES (1249, 4080,1,1,0,21142); 
INSERT INTO `droplist` VALUES (1249, 4081,1,1,0,10187); 
INSERT INTO `droplist` VALUES (1249, 5230,1,1,1,474063); 
INSERT INTO `droplist` VALUES (1249, 5811,1,1,0,5715); 
INSERT INTO `droplist` VALUES (1249, 5814,1,1,0,27785); 
INSERT INTO `droplist` VALUES (1249, 5815,1,1,0,27785); 
-- Lilim Knight 
INSERT INTO `droplist` VALUES (1180, 1830,1,1,0,77661); 
INSERT INTO `droplist` VALUES (1180, 1866,3,9,0,90934); 
INSERT INTO `droplist` VALUES (1180, 1871,4,14,0,58839); 
INSERT INTO `droplist` VALUES (1180, 1877,1,1,0,41079); 
INSERT INTO `droplist` VALUES (1180, 2398,1,1,0,87); 
INSERT INTO `droplist` VALUES (1180, 2399,1,1,0,87); 
INSERT INTO `droplist` VALUES (1180, 2403,1,1,0,138); 
INSERT INTO `droplist` VALUES (1180, 2404,1,1,0,138); 
INSERT INTO `droplist` VALUES (1180, 4040,1,1,0,20752); 
INSERT INTO `droplist` VALUES (1180, 4041,1,1,0,9076); 
INSERT INTO `droplist` VALUES (1180, 4052,1,1,1,200455); 
INSERT INTO `droplist` VALUES (1180, 4053,1,1,1,237594); 
INSERT INTO `droplist` VALUES (1180, 4084,1,1,0,18058); 
INSERT INTO `droplist` VALUES (1180, 4085,1,1,0,4822); 
INSERT INTO `droplist` VALUES (1180, 4086,1,1,0,24307); 
INSERT INTO `droplist` VALUES (1180, 4087,1,1,0,7686); 
INSERT INTO `droplist` VALUES (1180, 5230,1,1,1,478600); 
INSERT INTO `droplist` VALUES (1180, 6361,521,1035,0,700000); 
INSERT INTO `droplist` VALUES (1180, 6362,13,26,0,700000); 
-- Purgatory Tarantula 
INSERT INTO `droplist` VALUES (1157, 57,2774,5547,0,700000); 
INSERT INTO `droplist` VALUES (1157, 110,1,1,0,250); 
INSERT INTO `droplist` VALUES (1157, 1830,1,1,0,77781); 
INSERT INTO `droplist` VALUES (1157, 1832,4,16,0,100027); 
INSERT INTO `droplist` VALUES (1157, 1868,7,28,0,380332); 
INSERT INTO `droplist` VALUES (1157, 1873,1,1,0,41678); 
INSERT INTO `droplist` VALUES (1157, 1889,1,1,0,8774); 
INSERT INTO `droplist` VALUES (1157, 1889,1,2,1,50366); 
INSERT INTO `droplist` VALUES (1157, 2416,1,1,0,174); 
INSERT INTO `droplist` VALUES (1157, 2417,1,1,0,174); 
INSERT INTO `droplist` VALUES (1157, 4044,1,1,0,19236); 
INSERT INTO `droplist` VALUES (1157, 4052,1,1,1,202075); 
INSERT INTO `droplist` VALUES (1157, 4077,1,1,0,47632); 
INSERT INTO `droplist` VALUES (1157, 4088,1,1,0,39195); 
INSERT INTO `droplist` VALUES (1157, 4089,1,1,0,39195); 
INSERT INTO `droplist` VALUES (1157, 5549,1,2,0,100429); 
INSERT INTO `droplist` VALUES (1157, 5549,1,3,1,480900); 
-- Nephilim Centurion 
INSERT INTO `droplist` VALUES (1201, 1830,1,1,0,77661); 
INSERT INTO `droplist` VALUES (1201, 1866,3,9,0,90934); 
INSERT INTO `droplist` VALUES (1201, 1871,4,14,0,55570); 
INSERT INTO `droplist` VALUES (1201, 1877,1,1,0,41079); 
INSERT INTO `droplist` VALUES (1201, 2398,1,1,0,87); 
INSERT INTO `droplist` VALUES (1201, 2399,1,1,0,87); 
INSERT INTO `droplist` VALUES (1201, 2403,1,1,0,138); 
INSERT INTO `droplist` VALUES (1201, 2404,1,1,0,138); 
INSERT INTO `droplist` VALUES (1201, 4040,1,1,0,20752); 
INSERT INTO `droplist` VALUES (1201, 4041,1,1,0,9076); 
INSERT INTO `droplist` VALUES (1201, 4052,1,1,1,200455); 
INSERT INTO `droplist` VALUES (1201, 4053,1,1,1,237594); 
INSERT INTO `droplist` VALUES (1201, 4084,1,1,0,18058); 
INSERT INTO `droplist` VALUES (1201, 4085,1,1,0,4822); 
INSERT INTO `droplist` VALUES (1201, 4086,1,1,0,24307); 
INSERT INTO `droplist` VALUES (1201, 4087,1,1,0,7686); 
INSERT INTO `droplist` VALUES (1201, 5230,1,1,1,478600); 
INSERT INTO `droplist` VALUES (1201, 6036,1,1,0,8353); 
INSERT INTO `droplist` VALUES (1201, 6361,521,1033,0,700000); 
INSERT INTO `droplist` VALUES (1201, 6362,13,26,0,700000); 
-- Nephilim Scout 
INSERT INTO `droplist` VALUES (1202, 864,1,1,0,232); 
INSERT INTO `droplist` VALUES (1202, 895,1,1,0,349); 
INSERT INTO `droplist` VALUES (1202, 926,1,1,0,174); 
INSERT INTO `droplist` VALUES (1202, 1867,5,19,0,62517); 
INSERT INTO `droplist` VALUES (1202, 1872,5,19,0,41678); 
INSERT INTO `droplist` VALUES (1202, 1875,1,1,0,22733); 
INSERT INTO `droplist` VALUES (1202, 1881,1,2,0,8405); 
INSERT INTO `droplist` VALUES (1202, 4041,1,1,0,9304); 
INSERT INTO `droplist` VALUES (1202, 4041,1,1,1,54540); 
INSERT INTO `droplist` VALUES (1202, 4052,1,1,0,37351); 
INSERT INTO `droplist` VALUES (1202, 4053,1,1,0,45037); 
INSERT INTO `droplist` VALUES (1202, 4054,1,1,0,20952); 
INSERT INTO `droplist` VALUES (1202, 4054,1,1,1,119364); 
INSERT INTO `droplist` VALUES (1202, 5230,1,1,0,102068); 
INSERT INTO `droplist` VALUES (1202, 5230,1,1,1,487938); 
INSERT INTO `droplist` VALUES (1202, 6361,545,1095,0,700000); 
INSERT INTO `droplist` VALUES (1202, 6362,13,26,0,700000); 
-- Tomb Guardian 
INSERT INTO `droplist` VALUES (1250, 57,3102,6205,0,700000); 
INSERT INTO `droplist` VALUES (1250, 171,1,1,0,16); 
INSERT INTO `droplist` VALUES (1250, 175,1,1,0,16); 
INSERT INTO `droplist` VALUES (1250, 210,1,1,0,16); 
INSERT INTO `droplist` VALUES (1250, 1344,20,60,0,166712); 
INSERT INTO `droplist` VALUES (1250, 1830,1,1,0,77902); 
INSERT INTO `droplist` VALUES (1250, 1866,3,10,0,90934); 
INSERT INTO `droplist` VALUES (1250, 1871,4,14,0,50013); 
INSERT INTO `droplist` VALUES (1250, 1877,1,1,0,41765); 
INSERT INTO `droplist` VALUES (1250, 4040,1,1,0,21107); 
INSERT INTO `droplist` VALUES (1250, 4041,1,1,0,9231); 
INSERT INTO `droplist` VALUES (1250, 4052,1,1,1,203307); 
INSERT INTO `droplist` VALUES (1250, 4053,1,1,1,241029); 
INSERT INTO `droplist` VALUES (1250, 4116,1,1,0,4650); 
INSERT INTO `droplist` VALUES (1250, 4117,1,1,0,4650); 
INSERT INTO `droplist` VALUES (1250, 4118,1,1,0,4406); 
INSERT INTO `droplist` VALUES (1250, 5230,1,1,1,485569); 
-- Lilim Assassin 
INSERT INTO `droplist` VALUES (1181, 864,1,1,0,232); 
INSERT INTO `droplist` VALUES (1181, 895,1,1,0,349); 
INSERT INTO `droplist` VALUES (1181, 926,1,1,0,174); 
INSERT INTO `droplist` VALUES (1181, 1867,5,19,0,58839); 
INSERT INTO `droplist` VALUES (1181, 1872,5,19,0,41678); 
INSERT INTO `droplist` VALUES (1181, 1875,1,1,0,22733); 
INSERT INTO `droplist` VALUES (1181, 1881,1,2,0,7938); 
INSERT INTO `droplist` VALUES (1181, 4041,1,1,0,9304); 
INSERT INTO `droplist` VALUES (1181, 4041,1,1,1,54540); 
INSERT INTO `droplist` VALUES (1181, 4052,1,1,0,37351); 
INSERT INTO `droplist` VALUES (1181, 4053,1,1,0,45037); 
INSERT INTO `droplist` VALUES (1181, 4054,1,1,0,20952); 
INSERT INTO `droplist` VALUES (1181, 4054,1,1,1,119364); 
INSERT INTO `droplist` VALUES (1181, 5230,1,1,0,102068); 
INSERT INTO `droplist` VALUES (1181, 5230,1,1,1,487938); 
INSERT INTO `droplist` VALUES (1181, 5276,1,1,0,1432); 
INSERT INTO `droplist` VALUES (1181, 6361,545,1095,0,700000); 
INSERT INTO `droplist` VALUES (1181, 6362,13,26,0,700000); 
-- Tomb Sage 
INSERT INTO `droplist` VALUES (1251, 57,2787,5578,0,700000); 
INSERT INTO `droplist` VALUES (1251, 1344,20,60,0,166712); 
INSERT INTO `droplist` VALUES (1251, 1830,1,1,0,78085); 
INSERT INTO `droplist` VALUES (1251, 1866,3,10,0,90934); 
INSERT INTO `droplist` VALUES (1251, 1868,8,59,1,540688); 
INSERT INTO `droplist` VALUES (1251, 1871,4,15,0,45467); 
INSERT INTO `droplist` VALUES (1251, 1885,1,1,0,22307); 
INSERT INTO `droplist` VALUES (1251, 2398,1,1,0,89); 
INSERT INTO `droplist` VALUES (1251, 2399,1,1,0,89); 
INSERT INTO `droplist` VALUES (1251, 2403,1,1,0,143); 
INSERT INTO `droplist` VALUES (1251, 2404,1,1,0,143); 
INSERT INTO `droplist` VALUES (1251, 4041,1,1,0,9372); 
INSERT INTO `droplist` VALUES (1251, 4053,1,1,1,244565); 
INSERT INTO `droplist` VALUES (1251, 4054,1,1,1,120225); 
INSERT INTO `droplist` VALUES (1251, 4084,1,1,0,18644); 
INSERT INTO `droplist` VALUES (1251, 4085,1,1,0,4980); 
INSERT INTO `droplist` VALUES (1251, 4086,1,1,0,25094); 
INSERT INTO `droplist` VALUES (1251, 4087,1,1,0,7936); 
INSERT INTO `droplist` VALUES (1251, 5220,1,1,0,42402); 
INSERT INTO `droplist` VALUES (1251, 5230,1,1,1,490330); 
-- Nephilim Archbishop 
INSERT INTO `droplist` VALUES (1203, 1344,30,90,0,125034); 
INSERT INTO `droplist` VALUES (1203, 1868,8,31,0,392264); 
INSERT INTO `droplist` VALUES (1203, 1873,1,1,0,43490); 
INSERT INTO `droplist` VALUES (1203, 1876,1,1,0,53404); 
INSERT INTO `droplist` VALUES (1203, 1876,1,3,1,282563); 
INSERT INTO `droplist` VALUES (1203, 1895,2,5,0,30311); 
INSERT INTO `droplist` VALUES (1203, 4042,1,1,0,3922); 
INSERT INTO `droplist` VALUES (1203, 4042,1,1,1,37576); 
INSERT INTO `droplist` VALUES (1203, 5161,1,1,0,1462); 
INSERT INTO `droplist` VALUES (1203, 5290,1,1,0,189); 
INSERT INTO `droplist` VALUES (1203, 5291,1,1,0,189); 
INSERT INTO `droplist` VALUES (1203, 5295,1,1,0,189); 
INSERT INTO `droplist` VALUES (1203, 5296,1,1,0,189); 
INSERT INTO `droplist` VALUES (1203, 5478,1,1,1,20856); 
INSERT INTO `droplist` VALUES (1203, 5496,1,1,0,10455); 
INSERT INTO `droplist` VALUES (1203, 5497,1,1,0,10455); 
INSERT INTO `droplist` VALUES (1203, 5508,1,1,0,10455); 
INSERT INTO `droplist` VALUES (1203, 5509,1,1,0,10455); 
INSERT INTO `droplist` VALUES (1203, 6361,519,1038,0,700000); 
INSERT INTO `droplist` VALUES (1203, 6362,11,23,0,700000); 
-- Purgatory Gargoyle 
INSERT INTO `droplist` VALUES (1159, 57,3423,6846,0,700000); 
INSERT INTO `droplist` VALUES (1159, 1344,20,60,0,166712); 
INSERT INTO `droplist` VALUES (1159, 1865,4,15,0,120514); 
INSERT INTO `droplist` VALUES (1159, 1870,4,15,0,47632); 
INSERT INTO `droplist` VALUES (1159, 1874,1,1,0,70145); 
INSERT INTO `droplist` VALUES (1159, 1874,1,2,1,358521); 
INSERT INTO `droplist` VALUES (1159, 3035,1,1,0,1452); 
INSERT INTO `droplist` VALUES (1159, 4039,1,1,0,13537); 
INSERT INTO `droplist` VALUES (1159, 4043,1,1,0,35698); 
INSERT INTO `droplist` VALUES (1159, 4043,1,3,1,196517); 
INSERT INTO `droplist` VALUES (1159, 5166,1,1,0,1452); 
INSERT INTO `droplist` VALUES (1159, 5304,1,1,0,62); 
INSERT INTO `droplist` VALUES (1159, 5305,1,1,0,100); 
INSERT INTO `droplist` VALUES (1159, 5308,1,1,0,42); 
INSERT INTO `droplist` VALUES (1159, 5478,1,1,1,20726); 
INSERT INTO `droplist` VALUES (1159, 5485,1,1,0,3485); 
INSERT INTO `droplist` VALUES (1159, 5486,1,1,0,2382); 
INSERT INTO `droplist` VALUES (1159, 5489,1,1,0,5567); 
-- Lilim Soldier 
INSERT INTO `droplist` VALUES (1182, 1344,30,90,0,111141); 
INSERT INTO `droplist` VALUES (1182, 1868,8,31,0,392264); 
INSERT INTO `droplist` VALUES (1182, 1873,1,1,0,43490); 
INSERT INTO `droplist` VALUES (1182, 1876,1,1,0,53404); 
INSERT INTO `droplist` VALUES (1182, 1876,1,3,1,282563); 
INSERT INTO `droplist` VALUES (1182, 1895,2,5,0,30311); 
INSERT INTO `droplist` VALUES (1182, 4042,1,1,0,3922); 
INSERT INTO `droplist` VALUES (1182, 5155,1,1,0,2192); 
INSERT INTO `droplist` VALUES (1182, 5161,1,1,0,1462); 
INSERT INTO `droplist` VALUES (1182, 5290,1,1,0,189); 
INSERT INTO `droplist` VALUES (1182, 5291,1,1,0,189); 
INSERT INTO `droplist` VALUES (1182, 5295,1,1,0,189); 
INSERT INTO `droplist` VALUES (1182, 5296,1,1,0,189); 
INSERT INTO `droplist` VALUES (1182, 5478,1,1,1,20856); 
INSERT INTO `droplist` VALUES (1182, 5496,1,1,0,10455); 
INSERT INTO `droplist` VALUES (1182, 5497,1,1,0,10455); 
INSERT INTO `droplist` VALUES (1182, 5508,1,1,0,10455); 
INSERT INTO `droplist` VALUES (1182, 5509,1,1,0,10455); 
INSERT INTO `droplist` VALUES (1182, 6361,494,988,0,700000); 
INSERT INTO `droplist` VALUES (1182, 6362,11,22,0,700000); 
-- Purgatory Liviona 
INSERT INTO `droplist` VALUES (1160, 57,3208,6325,0,700000); 
INSERT INTO `droplist` VALUES (1160, 729,1,1,0,123); 
INSERT INTO `droplist` VALUES (1160, 1807,1,1,0,1478); 
INSERT INTO `droplist` VALUES (1160, 1807,1,1,1,8837); 
INSERT INTO `droplist` VALUES (1160, 1894,1,1,0,38192); 
INSERT INTO `droplist` VALUES (1160, 4039,1,1,0,13775); 
INSERT INTO `droplist` VALUES (1160, 4041,1,1,0,9603); 
INSERT INTO `droplist` VALUES (1160, 4044,1,1,0,20413); 
INSERT INTO `droplist` VALUES (1160, 5304,1,1,0,63); 
INSERT INTO `droplist` VALUES (1160, 5305,1,1,0,102); 
INSERT INTO `droplist` VALUES (1160, 5308,1,1,0,43); 
INSERT INTO `droplist` VALUES (1160, 5479,1,1,1,14455); 
INSERT INTO `droplist` VALUES (1160, 5482,1,1,1,33510); 
INSERT INTO `droplist` VALUES (1160, 5485,1,1,0,3547); 
INSERT INTO `droplist` VALUES (1160, 5486,1,1,0,2424); 
INSERT INTO `droplist` VALUES (1160, 5489,1,1,0,5666); 
INSERT INTO `droplist` VALUES (1160, 6037,7,27,0,312585); 
-- Nephilim Praetorian 
INSERT INTO `droplist` VALUES (1204, 1807,1,1,1,8880); 
INSERT INTO `droplist` VALUES (1204, 1894,1,1,0,38383); 
INSERT INTO `droplist` VALUES (1204, 3035,1,1,1,8880); 
INSERT INTO `droplist` VALUES (1204, 3956,1,1,0,1485); 
INSERT INTO `droplist` VALUES (1204, 4039,1,1,0,13842); 
INSERT INTO `droplist` VALUES (1204, 4041,1,1,0,9650); 
INSERT INTO `droplist` VALUES (1204, 4044,1,1,0,20413); 
INSERT INTO `droplist` VALUES (1204, 5289,1,1,0,128); 
INSERT INTO `droplist` VALUES (1204, 5292,1,1,0,182); 
INSERT INTO `droplist` VALUES (1204, 5294,1,1,0,128); 
INSERT INTO `droplist` VALUES (1204, 5479,1,1,1,14525); 
INSERT INTO `droplist` VALUES (1204, 5494,1,1,0,10104); 
INSERT INTO `droplist` VALUES (1204, 5525,1,1,0,7095); 
INSERT INTO `droplist` VALUES (1204, 5526,1,1,0,7095); 
INSERT INTO `droplist` VALUES (1204, 6361,623,1247,0,700000); 
INSERT INTO `droplist` VALUES (1204, 6362,13,26,0,700000); 
-- Lilim Knight Captain 
INSERT INTO `droplist` VALUES (1183, 1807,1,1,1,8880); 
INSERT INTO `droplist` VALUES (1183, 1894,1,1,0,38383); 
INSERT INTO `droplist` VALUES (1183, 3035,1,1,1,8880); 
INSERT INTO `droplist` VALUES (1183, 3956,1,1,0,1485); 
INSERT INTO `droplist` VALUES (1183, 4039,1,1,0,13842); 
INSERT INTO `droplist` VALUES (1183, 4041,1,1,0,9650); 
INSERT INTO `droplist` VALUES (1183, 4044,1,1,0,20413); 
INSERT INTO `droplist` VALUES (1183, 5289,1,1,0,128); 
INSERT INTO `droplist` VALUES (1183, 5292,1,1,0,182); 
INSERT INTO `droplist` VALUES (1183, 5294,1,1,0,128); 
INSERT INTO `droplist` VALUES (1183, 5479,1,1,1,14525); 
INSERT INTO `droplist` VALUES (1183, 5494,1,1,0,10104); 
INSERT INTO `droplist` VALUES (1183, 5525,1,1,0,7095); 
INSERT INTO `droplist` VALUES (1183, 5526,1,1,0,7095); 
INSERT INTO `droplist` VALUES (1183, 6361,623,1248,0,700000); 
INSERT INTO `droplist` VALUES (1183, 6362,13,26,0,700000); 
-- Crypt Guard 
INSERT INTO `droplist` VALUES (1252, 57,3012,5929,0,700000); 
INSERT INTO `droplist` VALUES (1252, 1344,34,83,0,125034); 
INSERT INTO `droplist` VALUES (1252, 1867,6,21,0,66684); 
INSERT INTO `droplist` VALUES (1252, 1872,6,21,0,58839); 
INSERT INTO `droplist` VALUES (1252, 1882,1,4,0,9618); 
INSERT INTO `droplist` VALUES (1252, 1894,1,1,0,37989); 
INSERT INTO `droplist` VALUES (1252, 4042,1,1,0,3938); 
INSERT INTO `droplist` VALUES (1252, 4053,1,1,1,248824); 
INSERT INTO `droplist` VALUES (1252, 5230,1,1,1,497648); 
INSERT INTO `droplist` VALUES (1252, 5276,1,1,0,1470); 
INSERT INTO `droplist` VALUES (1252, 5297,1,1,0,63); 
INSERT INTO `droplist` VALUES (1252, 5298,1,1,0,101); 
INSERT INTO `droplist` VALUES (1252, 5301,1,1,0,43); 
INSERT INTO `droplist` VALUES (1252, 5478,1,1,0,3528); 
INSERT INTO `droplist` VALUES (1252, 5478,1,1,1,20978); 
INSERT INTO `droplist` VALUES (1252, 5479,1,1,0,2411); 
INSERT INTO `droplist` VALUES (1252, 5482,1,1,0,5635); 
