-- Author: Pivi_CT (Akatsuki Server) 
-- Link: http://www.l2db.net 
-- Catacomb: Forbiden Path 70/80 

-- Nephilim Cardinal 1206 
UPDATE npc SET aggro = 500, level = 78, attackrange = 100, hp = 17712, mp = 1062, exp = 23509, sp = 2332, patk = 1870, pdef = 341, matk = 636, mdef = 402 WHERE id = 1206; 

-- Nephilim Commander 1027 
UPDATE npc SET aggro = 500, level = 80, attackrange = 120, hp = 18204, mp = 853, exp = 23526, sp = 2332, patk = 1524, pdef = 235, matk = 643, mdef = 407 WHERE id = 1207; 

-- Lilim Great Mystic 1185 
UPDATE npc SET level = 78, attackrange = 100, hp = 17712, mp = 1062, exp = 23509, sp = 2332, patk = 1870, pdef = 341, matk = 636, mdef = 402 WHERE id = 1185; 

-- Lilim Court Knight 1186 
UPDATE npc SET aggro = 500, level = 80, attackrange = 120, hp = 18204, mp = 853, exp = 23526, sp = 2332, patk = 1524, pdef = 235, matk = 643, mdef = 407 WHERE id = 1186; 

-- Tomb Guard 1254 
UPDATE npc SET aggro = 500, attackrange = 80, level = 75, hp = 16916, mp = 957, exp = 23512, sp = 2332, patk = 1706, pdef = 351, matk = 624, mdef = 395 WHERE id = 1254; 

-- Tomb Preacher 1255 
UPDATE npc SET isUndead = 1, attackrange = 80, level = 77, hp = 18020, mp = 998, exp = 23526, sp = 2332, patk = 1705, pdef = 288, matk = 632, mdef = 400 WHERE id = 1255; 

-- Lesser Ancient Shaman 1163 
UPDATE npc SET level = 76, attackrange = 80, hp = 17744, mp = 977, exp = 23520, sp = 2332, patk = 1761, pdef = 324, matk = 628, mdef = 397 WHERE id = 1163; 

-- Guardian Spirit of Ancient Holy Ground 1164 
UPDATE npc SET isUndead = 1, attackrange = 80, aggro = 500, level = 79, hp = 18544, mp = 1041, exp = 23516, sp = 2332, patk = 4014, pdef = 348, matk = 640, mdef = 405 WHERE id = 1164; 

-- Lesser Ancient Warrior 1165 
UPDATE npc SET level = 80, attackrange = 120, hp = 18204, mp = 853, exp = 23526, sp = 2332, patk = 1524, pdef = 235, matk = 643, mdef = 407 WHERE id = 1165;

DELETE FROM `spawnlist` WHERE (id >= 3002663 AND id <= 3002774);

insert into spawnlist values 
(1249015193, 'CatacombOfTheForbiddenPath', 1, 1207, 122653, 89392, -6546, 0, 0, 37535, 60, 0), 
(1249015168, 'CatacombOfTheForbiddenPath', 1, 1255, 123301, 84650, -6546, 0, 0, 37642, 60, 0), 
(1249015126, 'CatacombOfTheForbiddenPath', 1, 1185, 117547, 84685, -6544, 0, 0, 27756, 60, 0), 
(1249015169, 'CatacombOfTheForbiddenPath', 1, 1255, 122800, 84309, -6546, 0, 0, 12161, 60, 0), 
(1249015137, 'CatacombOfTheForbiddenPath', 1, 1254, 118788, 84670, -6546, 0, 0, 20699, 60, 0), 
(1249015196, 'CatacombOfTheForbiddenPath', 1, 1255, 121848, 89330, -6546, 0, 0, 51304, 60, 0), 
(1249015130, 'CatacombOfTheForbiddenPath', 1, 1185, 118792, 84427, -6546, 0, 0, 58229, 60, 0), 
(1249015128, 'CatacombOfTheForbiddenPath', 1, 1206, 118984, 84679, -6544, 0, 0, 13482, 60, 0), 
(1249015195, 'CatacombOfTheForbiddenPath', 1, 1186, 121970, 89166, -6544, 0, 0, 31017, 60, 0), 
(1249015194, 'CatacombOfTheForbiddenPath', 1, 1186, 123008, 88959, -6546, 0, 0, 29053, 60, 0), 
(1249015127, 'CatacombOfTheForbiddenPath', 1, 1185, 117730, 85099, -6544, 0, 0, 39300, 60, 0), 
(1249015167, 'CatacombOfTheForbiddenPath', 1, 1165, 123358, 84311, -6546, 0, 0, 23939, 60, 0), 
(1249015166, 'CatacombOfTheForbiddenPath', 1, 1165, 122630, 84724, -6546, 0, 0, 15641, 60, 0), 
(1249015165, 'CatacombOfTheForbiddenPath', 1, 1165, 122101, 86914, -6544, 0, 0, 54022, 60, 0), 
(1249015164, 'CatacombOfTheForbiddenPath', 1, 1165, 121784, 87389, -6546, 0, 0, 42746, 60, 0), 
(1249015163, 'CatacombOfTheForbiddenPath', 1, 1165, 122270, 87419, -6546, 0, 0, 9699, 60, 0), 
(1249015162, 'CatacombOfTheForbiddenPath', 1, 1255, 122281, 87055, -6546, 0, 0, 57544, 60, 0), 
(1249015161, 'CatacombOfTheForbiddenPath', 1, 1255, 122091, 87492, -6544, 0, 0, 46243, 60, 0), 
(1249015160, 'CatacombOfTheForbiddenPath', 1, 1255, 122151, 88039, -6544, 0, 0, 8375, 60, 0), 
(1249015159, 'CatacombOfTheForbiddenPath', 1, 1186, 121930, 87938, -6546, 0, 0, 13685, 60, 0), 
(1249015158, 'CatacombOfTheForbiddenPath', 1, 1186, 121810, 86876, -6546, 0, 0, 25426, 60, 0), 
(1249015190, 'CatacombOfTheForbiddenPath', 1, 1186, 122051, 86453, -6544, 0, 0, 46005, 60, 0), 
(1249015156, 'CatacombOfTheForbiddenPath', 1, 1207, 122217, 86565, -6546, 0, 0, 48374, 60, 0), 
(1249015155, 'CatacombOfTheForbiddenPath', 1, 1207, 122259, 87742, -6546, 0, 0, 5637, 60, 0), 
(1249015154, 'CatacombOfTheForbiddenPath', 1, 1207, 121686, 87144, -6546, 0, 0, 34225, 60, 0), 
(1249015153, 'CatacombOfTheForbiddenPath', 1, 1255, 119810, 86433, -6546, 0, 0, 58757, 60, 0), 
(1249015125, 'CatacombOfTheForbiddenPath', 1, 1163, 117773, 84779, -6546, 0, 0, 27857, 60, 0), 
(1249015152, 'CatacombOfTheForbiddenPath', 1, 1255, 120134, 86801, -6546, 0, 0, 21655, 60, 0), 
(1249015151, 'CatacombOfTheForbiddenPath', 1, 1186, 120192, 86416, -6546, 0, 0, 7980, 60, 0), 
(1249015150, 'CatacombOfTheForbiddenPath', 1, 1186, 119835, 86076, -6546, 0, 0, 10279, 60, 0), 
(1249015149, 'CatacombOfTheForbiddenPath', 1, 1207, 119823, 86705, -6546, 0, 0, 801, 60, 0), 
(1249015148, 'CatacombOfTheForbiddenPath', 1, 1207, 120061, 87018, -6544, 0, 0, 15370, 60, 0), 
(1249015147, 'CatacombOfTheForbiddenPath', 1, 1164, 119853, 86942, -6546, 0, 0, 19278, 60, 0), 
(1249015124, 'CatacombOfTheForbiddenPath', 1, 1163, 117490, 85234, -6546, 0, 0, 48425, 60, 0), 
(1249015146, 'CatacombOfTheForbiddenPath', 1, 1164, 120141, 86040, -6546, 0, 0, 5276, 60, 0), 
(1249015122, 'CatacombOfTheForbiddenPath', 1, 1254, 117626, 84482, -6544, 0, 0, 24123, 60, 0), 
(1249015123, 'CatacombOfTheForbiddenPath', 1, 1254, 117746, 85260, -6546, 0, 0, 23008, 60, 0), 
(1249015121, 'CatacombOfTheForbiddenPath', 1, 1206, 117812, 84477, -6546, 0, 0, 22070, 60, 0), 
(1249015120, 'CatacombOfTheForbiddenPath', 1, 1206, 117500, 84966, -6546, 0, 0, 32267, 60, 0), 
(1249015117, 'CatacombOfTheForbiddenPath', 1, 1185, 114278, 85708, -6546, 0, 0, 61706, 60, 0), 
(1249015115, 'CatacombOfTheForbiddenPath', 1, 1206, 115592, 86255, -6546, 0, 0, 43515, 60, 0), 
(1249015116, 'CatacombOfTheForbiddenPath', 1, 1163, 114756, 85914, -6544, 0, 0, 36907, 60, 0), 
(1249015113, 'CatacombOfTheForbiddenPath', 1, 1185, 114877, 85864, -6546, 0, 0, 32768, 60, 0), 
(1249015114, 'CatacombOfTheForbiddenPath', 1, 1254, 115172, 85971, -6544, 0, 0, 57691, 60, 0), 
(1249015145, 'CatacombOfTheForbiddenPath', 1, 1255, 121185, 84720, -6546, 0, 0, 27424, 60, 0), 
(1249015112, 'CatacombOfTheForbiddenPath', 1, 1185, 115079, 86076, -6544, 0, 0, 54566, 60, 0), 
(1249015111, 'CatacombOfTheForbiddenPath', 1, 1163, 114372, 86157, -6546, 0, 0, 60260, 60, 0), 
(1249015110, 'CatacombOfTheForbiddenPath', 1, 1163, 115686, 85816, -6546, 0, 0, 63021, 60, 0), 
(1249015144, 'CatacombOfTheForbiddenPath', 1, 1186, 120911, 84619, -6544, 0, 0, 17289, 60, 0), 
(1249015109, 'CatacombOfTheForbiddenPath', 1, 1254, 115644, 86069, -6544, 0, 0, 37856, 60, 0), 
(1249015143, 'CatacombOfTheForbiddenPath', 1, 1207, 120929, 84412, -6546, 0, 0, 22427, 60, 0), 
(1249015142, 'CatacombOfTheForbiddenPath', 1, 1164, 121208, 84460, -6546, 0, 0, 18443, 60, 0), 
(1249015141, 'CatacombOfTheForbiddenPath', 1, 1255, 120106, 84613, -6544, 0, 0, 21491, 60, 0), 
(1249015108, 'CatacombOfTheForbiddenPath', 1, 1254, 114417, 85856, -6546, 0, 0, 50365, 60, 0), 
(1249015107, 'CatacombOfTheForbiddenPath', 1, 1206, 114668, 86188, -6546, 0, 0, 24695, 60, 0), 
(1249015106, 'CatacombOfTheForbiddenPath', 1, 1206, 115149, 85705, -6546, 0, 0, 46325, 60, 0), 
(1249015140, 'CatacombOfTheForbiddenPath', 1, 1186, 120132, 84367, -6546, 0, 0, 15012, 60, 0), 
(1249015139, 'CatacombOfTheForbiddenPath', 1, 1207, 119790, 84393, -6546, 0, 0, 18940, 60, 0), 
(1249015138, 'CatacombOfTheForbiddenPath', 1, 1164, 119703, 84595, -6544, 0, 0, 36123, 60, 0), 
(1249015135, 'CatacombOfTheForbiddenPath', 1, 1164, 118987, 84415, -6544, 0, 0, 64894, 60, 0), 
(1249015192, 'CatacombOfTheForbiddenPath', 1, 1207, 122155, 88938, -6546, 0, 0, 36650, 60, 0), 
(1249015189, 'CatacombOfTheForbiddenPath', 1, 1165, 124730, 89301, -6546, 0, 0, 63200, 60, 0), 
(1249015188, 'CatacombOfTheForbiddenPath', 1, 1255, 124386, 89275, -6546, 0, 0, 50265, 60, 0), 
(1249015187, 'CatacombOfTheForbiddenPath', 1, 1186, 124461, 88964, -6546, 0, 0, 40685, 60, 0), 
(1249015186, 'CatacombOfTheForbiddenPath', 1, 1207, 124670, 88981, -6544, 0, 0, 44471, 60, 0), 
(1249015185, 'CatacombOfTheForbiddenPath', 1, 1207, 124481, 86390, -6546, 0, 0, 11770, 60, 0), 
(1249015184, 'CatacombOfTheForbiddenPath', 1, 1207, 124682, 87134, -6544, 0, 0, 4663, 60, 0), 
(1249015183, 'CatacombOfTheForbiddenPath', 1, 1186, 124447, 87091, -6546, 0, 0, 23058, 60, 0), 
(1249015182, 'CatacombOfTheForbiddenPath', 1, 1186, 124778, 86388, -6546, 0, 0, 29202, 60, 0), 
(1249015181, 'CatacombOfTheForbiddenPath', 1, 1255, 124387, 86828, -6546, 0, 0, 36803, 60, 0), 
(1249015180, 'CatacombOfTheForbiddenPath', 1, 1255, 124482, 87338, -6546, 0, 0, 25395, 60, 0), 
(1249015179, 'CatacombOfTheForbiddenPath', 1, 1165, 124747, 87284, -6546, 0, 0, 17424, 60, 0), 
(1249015178, 'CatacombOfTheForbiddenPath', 1, 1165, 124371, 86637, -6546, 0, 0, 36465, 60, 0), 
(1249015177, 'CatacombOfTheForbiddenPath', 1, 1207, 124448, 84637, -6544, 0, 0, 15599, 60, 0), 
(1249015176, 'CatacombOfTheForbiddenPath', 1, 1186, 124436, 84399, -6546, 0, 0, 21657, 60, 0), 
(1249015175, 'CatacombOfTheForbiddenPath', 1, 1255, 124747, 84383, -6546, 0, 0, 20584, 60, 0), 
(1249015174, 'CatacombOfTheForbiddenPath', 1, 1165, 124721, 84681, -6546, 0, 0, 19040, 60, 0), 
(1249015173, 'CatacombOfTheForbiddenPath', 1, 1207, 122332, 84671, -6546, 0, 0, 61173, 60, 0), 
(1249015172, 'CatacombOfTheForbiddenPath', 1, 1207, 122848, 84676, -6546, 0, 0, 22651, 60, 0), 
(1249015171, 'CatacombOfTheForbiddenPath', 1, 1186, 122983, 84479, -6544, 0, 0, 22361, 60, 0), 
(1249015170, 'CatacombOfTheForbiddenPath', 1, 1186, 122292, 84378, -6546, 0, 0, 24342, 60, 0), 
(1249015197, 'CatacombOfTheForbiddenPath', 1, 1255, 122513, 88977, -6546, 0, 0, 19219, 60, 0), 
(1249015198, 'CatacombOfTheForbiddenPath', 1, 1165, 122966, 89311, -6546, 0, 0, 33873, 60, 0), 
(1249015199, 'CatacombOfTheForbiddenPath', 1, 1165, 122291, 89272, -6546, 0, 0, 29866, 60, 0), 
(1249015200, 'CatacombOfTheForbiddenPath', 1, 1164, 119997, 88991, -6544, 0, 0, 49937, 60, 0), 
(1249015201, 'CatacombOfTheForbiddenPath', 1, 1207, 120179, 89150, -6544, 0, 0, 6204, 60, 0), 
(1249015202, 'CatacombOfTheForbiddenPath', 1, 1186, 120006, 89298, -6544, 0, 0, 30519, 60, 0), 
(1249015203, 'CatacombOfTheForbiddenPath', 1, 1255, 119771, 89175, -6544, 0, 0, 38057, 60, 0), 
(1249015204, 'CatacombOfTheForbiddenPath', 1, 1206, 119157, 89137, -6544, 0, 0, 183, 60, 0), 
(1249015205, 'CatacombOfTheForbiddenPath', 1, 1254, 118733, 89155, -6544, 0, 0, 33922, 60, 0), 
(1249015206, 'CatacombOfTheForbiddenPath', 1, 1185, 118945, 88967, -6544, 0, 0, 27202, 60, 0), 
(1249015207, 'CatacombOfTheForbiddenPath', 1, 1164, 118899, 89308, -6544, 0, 0, 34718, 60, 0), 
(1249015208, 'CatacombOfTheForbiddenPath', 1, 1206, 117480, 88376, -6546, 0, 0, 65405, 60, 0), 
(1249015209, 'CatacombOfTheForbiddenPath', 1, 1206, 117126, 87965, -6546, 0, 0, 9525, 60, 0), 
(1249015210, 'CatacombOfTheForbiddenPath', 1, 1254, 117059, 88377, -6546, 0, 0, 18770, 60, 0), 
(1249015211, 'CatacombOfTheForbiddenPath', 1, 1254, 117472, 88627, -6546, 0, 0, 37679, 60, 0), 
(1249015212, 'CatacombOfTheForbiddenPath', 1, 1185, 117444, 89030, -6546, 0, 0, 45932, 60, 0), 
(1249015213, 'CatacombOfTheForbiddenPath', 1, 1185, 117105, 88751, -6546, 0, 0, 49152, 60, 0), 
(1249015214, 'CatacombOfTheForbiddenPath', 1, 1164, 117116, 89073, -6546, 0, 0, 62656, 60, 0), 
(1249015215, 'CatacombOfTheForbiddenPath', 1, 1164, 117343, 88177, -6544, 0, 0, 53736, 60, 0), 
(1249015216, 'CatacombOfTheForbiddenPath', 1, 1206, 114587, 89150, -6544, 0, 0, 64485, 60, 0), 
(1249015217, 'CatacombOfTheForbiddenPath', 1, 1254, 114355, 89001, -6544, 0, 0, 55816, 60, 0), 
(1249015218, 'CatacombOfTheForbiddenPath', 1, 1163, 114130, 89137, -6544, 0, 0, 64572, 60, 0), 
(1249015219, 'CatacombOfTheForbiddenPath', 1, 1185, 114289, 89331, -6544, 0, 0, 1998, 60, 0), 
(1249015220, 'CatacombOfTheForbiddenPath', 1, 1206, 115166, 87740, -6546, 0, 0, 16299, 60, 0), 
(1249015221, 'CatacombOfTheForbiddenPath', 1, 1206, 115692, 87513, -6546, 0, 0, 1969, 60, 0), 
(1249015222, 'CatacombOfTheForbiddenPath', 1, 1206, 114301, 87140, -6567, 0, 0, 3578, 60, 0), 
(1249015223, 'CatacombOfTheForbiddenPath', 1, 1254, 114301, 87471, -6546, 0, 0, 12703, 60, 0), 
(1249015224, 'CatacombOfTheForbiddenPath', 1, 1254, 115059, 87090, -6546, 0, 0, 62264, 60, 0), 
(1249015225, 'CatacombOfTheForbiddenPath', 1, 1254, 115446, 87337, -6544, 0, 0, 5967, 60, 0), 
(1249015226, 'CatacombOfTheForbiddenPath', 1, 1185, 115766, 87227, -6544, 0, 0, 62921, 60, 0), 
(1249015227, 'CatacombOfTheForbiddenPath', 1, 1185, 115168, 87388, -6544, 0, 0, 27932, 60, 0), 
(1249015228, 'CatacombOfTheForbiddenPath', 1, 1185, 114676, 87047, -6546, 0, 0, 13326, 60, 0), 
(1249015229, 'CatacombOfTheForbiddenPath', 1, 1163, 114541, 87436, -6546, 0, 0, 10228, 60, 0), 
(1249015230, 'CatacombOfTheForbiddenPath', 1, 1163, 114948, 87296, -6544, 0, 0, 62180, 60, 0), 
(1249015231, 'CatacombOfTheForbiddenPath', 1, 1163, 115362, 87117, -6546, 0, 0, 49005, 60, 0), 
(1249015232, 'CatacombOfTheForbiddenPath', 1, 1206, 117845, 87142, -6546, 0, 0, 35902, 60, 0), 
(1249015233, 'CatacombOfTheForbiddenPath', 1, 1206, 118208, 86752, -6546, 0, 0, 52800, 60, 0), 
(1249015234, 'CatacombOfTheForbiddenPath', 1, 1254, 117741, 86910, -6544, 0, 0, 30949, 60, 0), 
(1249015235, 'CatacombOfTheForbiddenPath', 1, 1254, 118766, 87265, -6546, 0, 0, 34722, 60, 0), 
(1249015236, 'CatacombOfTheForbiddenPath', 1, 1163, 118853, 86843, -6546, 0, 0, 31404, 60, 0), 
(1249015237, 'CatacombOfTheForbiddenPath', 1, 1163, 118385, 87200, -6546, 0, 0, 51171, 60, 0), 
(1249015238, 'CatacombOfTheForbiddenPath', 1, 1185, 118023, 86973, -6544, 0, 0, 41032, 60, 0), 
(1249015239, 'CatacombOfTheForbiddenPath', 1, 1185, 118659, 86822, -6546, 0, 0, 46959, 60, 0);

-- Deletes drop data for these mobs if exists 
DELETE FROM droplist WHERE mobid in (1206,1207,1185,1186,1254,1255,1163,1164,1165); 

-- Nephilim Cardinal 1206 
INSERT INTO `droplist` VALUES (1206, 729,1,1,0,126); 
INSERT INTO `droplist` VALUES (1206, 1867,6,24,0,66684); 
INSERT INTO `droplist` VALUES (1206, 1872,6,24,0,76944); 
INSERT INTO `droplist` VALUES (1206, 1875,1,2,0,23816); 
INSERT INTO `droplist` VALUES (1206, 1875,1,3,1,149294); 
INSERT INTO `droplist` VALUES (1206, 1881,1,3,0,8549); 
INSERT INTO `droplist` VALUES (1206, 4041,1,1,0,9827); 
INSERT INTO `droplist` VALUES (1206, 5287,1,1,0,49); 
INSERT INTO `droplist` VALUES (1206, 5288,1,1,0,78); 
INSERT INTO `droplist` VALUES (1206, 5293,1,1,0,33); 
INSERT INTO `droplist` VALUES (1206, 5316,1,1,0,22); 
INSERT INTO `droplist` VALUES (1206, 5481,1,1,1,9746); 
INSERT INTO `droplist` VALUES (1206, 5487,1,1,1,9746); 
INSERT INTO `droplist` VALUES (1206, 5520,1,1,0,2722); 
INSERT INTO `droplist` VALUES (1206, 5521,1,1,0,1862); 
INSERT INTO `droplist` VALUES (1206, 5522,1,1,0,1225); 
INSERT INTO `droplist` VALUES (1206, 5523,1,1,0,1225); 
INSERT INTO `droplist` VALUES (1206, 5524,1,1,0,4357); 
INSERT INTO `droplist` VALUES (1206, 6361,620,1245,0,700000); 
INSERT INTO `droplist` VALUES (1206, 6362,11,23,0,700000); 
-- Nephilim Commander 1027 
INSERT INTO `droplist` VALUES (1207, 729,1,1,0,126); 
INSERT INTO `droplist` VALUES (1207, 730,1,1,1,5673); 
INSERT INTO `droplist` VALUES (1207, 1866,4,13,0,100027); 
INSERT INTO `droplist` VALUES (1207, 1871,5,19,0,71448); 
INSERT INTO `droplist` VALUES (1207, 1877,1,1,0,44515); 
INSERT INTO `droplist` VALUES (1207, 4040,1,1,0,22503); 
INSERT INTO `droplist` VALUES (1207, 4041,1,1,0,9847); 
INSERT INTO `droplist` VALUES (1207, 5312,1,1,0,86); 
INSERT INTO `droplist` VALUES (1207, 5315,1,1,0,123); 
INSERT INTO `droplist` VALUES (1207, 5317,1,1,0,86); 
INSERT INTO `droplist` VALUES (1207, 5480,1,1,1,9765); 
INSERT INTO `droplist` VALUES (1207, 5488,1,1,1,9765); 
INSERT INTO `droplist` VALUES (1207, 5495,1,1,0,6811); 
INSERT INTO `droplist` VALUES (1207, 5527,1,1,0,4778); 
INSERT INTO `droplist` VALUES (1207, 5528,1,1,0,4778); 
INSERT INTO `droplist` VALUES (1207, 6361,744,1473,0,700000); 
INSERT INTO `droplist` VALUES (1207, 6362,13,26,0,700000); 
-- Lilim Great Mystic 1185 
INSERT INTO `droplist` VALUES (1185, 729,1,1,0,126); 
INSERT INTO `droplist` VALUES (1185, 1867,6,24,0,66684); 
INSERT INTO `droplist` VALUES (1185, 1872,6,24,0,76944); 
INSERT INTO `droplist` VALUES (1185, 1875,1,2,0,23816); 
INSERT INTO `droplist` VALUES (1185, 1875,1,3,1,149294); 
INSERT INTO `droplist` VALUES (1185, 1881,1,3,0,8549); 
INSERT INTO `droplist` VALUES (1185, 5304,1,1,0,65); 
INSERT INTO `droplist` VALUES (1185, 5305,1,1,0,104); 
INSERT INTO `droplist` VALUES (1185, 5308,1,1,0,44); 
INSERT INTO `droplist` VALUES (1185, 5326,1,1,0,29); 
INSERT INTO `droplist` VALUES (1185, 5329,1,1,0,29); 
INSERT INTO `droplist` VALUES (1185, 5481,1,1,1,9746); 
INSERT INTO `droplist` VALUES (1185, 5485,1,1,0,3630); 
INSERT INTO `droplist` VALUES (1185, 5486,1,1,0,2481); 
INSERT INTO `droplist` VALUES (1185, 5487,1,1,0,1631); 
INSERT INTO `droplist` VALUES (1185, 5487,1,1,1,9746); 
INSERT INTO `droplist` VALUES (1185, 5488,1,1,0,1631); 
INSERT INTO `droplist` VALUES (1185, 5489,1,1,0,5798); 
INSERT INTO `droplist` VALUES (1185, 6361,591,1185,0,700000); 
INSERT INTO `droplist` VALUES (1185, 6362,11,22,0,700000); 
-- Lilim Court Knight 1186 
INSERT INTO `droplist` VALUES (1186, 729,1,1,0,126); 
INSERT INTO `droplist` VALUES (1186, 730,1,1,1,5673); 
INSERT INTO `droplist` VALUES (1186, 1866,4,13,0,100027); 
INSERT INTO `droplist` VALUES (1186, 1871,5,19,0,71448); 
INSERT INTO `droplist` VALUES (1186, 1877,1,1,0,44515); 
INSERT INTO `droplist` VALUES (1186, 4040,1,1,0,22503); 
INSERT INTO `droplist` VALUES (1186, 4041,1,1,0,9847); 
INSERT INTO `droplist` VALUES (1186, 5312,1,1,0,86); 
INSERT INTO `droplist` VALUES (1186, 5315,1,1,0,123); 
INSERT INTO `droplist` VALUES (1186, 5317,1,1,0,86); 
INSERT INTO `droplist` VALUES (1186, 5480,1,1,1,9765); 
INSERT INTO `droplist` VALUES (1186, 5488,1,1,1,9765); 
INSERT INTO `droplist` VALUES (1186, 5495,1,1,0,6811); 
INSERT INTO `droplist` VALUES (1186, 5527,1,1,0,4778); 
INSERT INTO `droplist` VALUES (1186, 5528,1,1,0,4778); 
INSERT INTO `droplist` VALUES (1186, 6361,737,1474,0,700000); 
INSERT INTO `droplist` VALUES (1186, 6362,13,26,0,700000); 
-- Tomb Guard 1254 
INSERT INTO `droplist` VALUES (1254, 57,3879,7757,0,700000); 
INSERT INTO `droplist` VALUES (1254, 729,1,1,0,125); 
INSERT INTO `droplist` VALUES (1254, 729,1,1,1,752); 
INSERT INTO `droplist` VALUES (1254, 1866,3,12,0,100027); 
INSERT INTO `droplist` VALUES (1254, 1871,5,17,0,66684); 
INSERT INTO `droplist` VALUES (1254, 1877,1,1,0,44123); 
INSERT INTO `droplist` VALUES (1254, 1877,1,2,1,238160); 
INSERT INTO `droplist` VALUES (1254, 4040,1,1,0,22307); 
INSERT INTO `droplist` VALUES (1254, 4041,1,1,0,9761); 
INSERT INTO `droplist` VALUES (1254, 5480,1,1,1,9679); 
INSERT INTO `droplist` VALUES (1254, 6323,1,1,0,129); 
INSERT INTO `droplist` VALUES (1254, 6324,1,1,0,172); 
INSERT INTO `droplist` VALUES (1254, 6325,1,1,0,259); 
INSERT INTO `droplist` VALUES (1254, 6326,1,1,0,86); 
INSERT INTO `droplist` VALUES (1254, 6327,1,1,0,115); 
INSERT INTO `droplist` VALUES (1254, 6328,1,1,0,173); 
INSERT INTO `droplist` VALUES (1254, 6341,1,1,0,9521); 
INSERT INTO `droplist` VALUES (1254, 6343,1,1,0,7177); 
INSERT INTO `droplist` VALUES (1254, 6344,1,1,0,4832); 
INSERT INTO `droplist` VALUES (1254, 6345,1,1,0,142); 
-- Tomb Preacher 1255 
INSERT INTO `droplist` VALUES (1255, 57,3268,6527,0,700000); 
INSERT INTO `droplist` VALUES (1255, 1867,6,24,0,66684); 
INSERT INTO `droplist` VALUES (1255, 1872,6,24,0,58839); 
INSERT INTO `droplist` VALUES (1255, 1875,1,2,0,23816); 
INSERT INTO `droplist` VALUES (1255, 1881,1,3,0,8266); 
INSERT INTO `droplist` VALUES (1255, 4041,1,1,0,9810); 
INSERT INTO `droplist` VALUES (1255, 4041,1,1,1,57453); 
INSERT INTO `droplist` VALUES (1255, 5297,1,1,0,65); 
INSERT INTO `droplist` VALUES (1255, 5298,1,1,0,104); 
INSERT INTO `droplist` VALUES (1255, 5301,1,1,0,44); 
INSERT INTO `droplist` VALUES (1255, 5478,1,1,0,3623); 
INSERT INTO `droplist` VALUES (1255, 5479,1,1,0,2476); 
INSERT INTO `droplist` VALUES (1255, 5480,1,1,0,1628); 
INSERT INTO `droplist` VALUES (1255, 5481,1,1,1,9729); 
INSERT INTO `droplist` VALUES (1255, 5482,1,1,0,5788); 
INSERT INTO `droplist` VALUES (1255, 5487,1,1,1,9729); 
-- Lesser Ancient Shaman 1163 
INSERT INTO `droplist` VALUES (1163, 57,3265,6530,0,700000); 
INSERT INTO `droplist` VALUES (1163, 729,1,1,1,753); 
INSERT INTO `droplist` VALUES (1163, 730,1,1,0,942); 
INSERT INTO `droplist` VALUES (1163, 1864,9,35,0,204137); 
INSERT INTO `droplist` VALUES (1163, 1869,5,18,0,250068); 
INSERT INTO `droplist` VALUES (1163, 1878,2,7,0,166712); 
INSERT INTO `droplist` VALUES (1163, 1884,3,11,0,47632); 
INSERT INTO `droplist` VALUES (1163, 4039,1,1,0,14040); 
INSERT INTO `droplist` VALUES (1163, 5290,1,1,0,195); 
INSERT INTO `droplist` VALUES (1163, 5291,1,1,0,195); 
INSERT INTO `droplist` VALUES (1163, 5295,1,1,0,195); 
INSERT INTO `droplist` VALUES (1163, 5296,1,1,0,195); 
INSERT INTO `droplist` VALUES (1163, 5313,1,1,0,128); 
INSERT INTO `droplist` VALUES (1163, 5314,1,1,0,128); 
INSERT INTO `droplist` VALUES (1163, 5319,1,1,0,128); 
INSERT INTO `droplist` VALUES (1163, 5480,1,1,1,9706); 
INSERT INTO `droplist` VALUES (1163, 5481,1,1,1,9706); 
INSERT INTO `droplist` VALUES (1163, 5496,1,1,0,10777); 
INSERT INTO `droplist` VALUES (1163, 5497,1,1,0,10777); 
INSERT INTO `droplist` VALUES (1163, 5502,1,1,0,7116); 
INSERT INTO `droplist` VALUES (1163, 5503,1,1,0,7116); 
INSERT INTO `droplist` VALUES (1163, 5508,1,1,0,10777); 
INSERT INTO `droplist` VALUES (1163, 5509,1,1,0,10777); 
INSERT INTO `droplist` VALUES (1163, 5514,1,1,0,7116); 
INSERT INTO `droplist` VALUES (1163, 5515,1,1,0,7116); 
-- Guardian Spirit of Ancient Holy Ground 1164 
INSERT INTO `droplist` VALUES (1164, 57,3879,7757,0,700000); 
INSERT INTO `droplist` VALUES (1164, 729,1,1,0,126); 
INSERT INTO `droplist` VALUES (1164, 1865,5,18,0,125347); 
INSERT INTO `droplist` VALUES (1164, 1866,3,12,0,100027); 
INSERT INTO `droplist` VALUES (1164, 1870,5,18,0,111141); 
INSERT INTO `droplist` VALUES (1164, 1871,5,18,0,50013); 
INSERT INTO `droplist` VALUES (1164, 1875,1,2,0,23816); 
INSERT INTO `droplist` VALUES (1164, 1877,1,1,0,44476); 
INSERT INTO `droplist` VALUES (1164, 1879,1,3,0,9526); 
INSERT INTO `droplist` VALUES (1164, 4040,1,1,0,22488); 
INSERT INTO `droplist` VALUES (1164, 4041,1,1,0,9840); 
INSERT INTO `droplist` VALUES (1164, 5480,1,1,1,9758); 
INSERT INTO `droplist` VALUES (1164, 5481,1,1,1,9758); 
INSERT INTO `droplist` VALUES (1164, 5487,1,1,1,9758); 
INSERT INTO `droplist` VALUES (1164, 6326,1,1,0,87); 
INSERT INTO `droplist` VALUES (1164, 6327,1,1,0,116); 
INSERT INTO `droplist` VALUES (1164, 6328,1,1,0,174); 
INSERT INTO `droplist` VALUES (1164, 6342,1,1,0,6459); 
INSERT INTO `droplist` VALUES (1164, 6344,1,1,0,4848); 
INSERT INTO `droplist` VALUES (1164, 6346,1,1,0,9672); 
-- Lesser Ancient Warrior 1165 
INSERT INTO `droplist` VALUES (1165, 57,3266,6522,0,700000); 
INSERT INTO `droplist` VALUES (1165, 729,1,1,0,126); 
INSERT INTO `droplist` VALUES (1165, 730,1,1,0,948); 
INSERT INTO `droplist` VALUES (1165, 730,1,1,1,5673); 
INSERT INTO `droplist` VALUES (1165, 1866,4,13,0,100027); 
INSERT INTO `droplist` VALUES (1165, 1871,5,19,0,55570); 
INSERT INTO `droplist` VALUES (1165, 1895,2,6,0,30311); 
INSERT INTO `droplist` VALUES (1165, 4040,1,1,0,22503); 
INSERT INTO `droplist` VALUES (1165, 4041,1,1,0,9847); 
INSERT INTO `droplist` VALUES (1165, 4042,1,1,0,3953); 
INSERT INTO `droplist` VALUES (1165, 4043,1,1,0,37226); 
INSERT INTO `droplist` VALUES (1165, 5220,1,1,0,44515); 
INSERT INTO `droplist` VALUES (1165, 5320,1,1,0,29); 
INSERT INTO `droplist` VALUES (1165, 5323,1,1,0,29); 
INSERT INTO `droplist` VALUES (1165, 5480,1,1,0,1634); 
INSERT INTO `droplist` VALUES (1165, 5481,1,1,0,1634); 
INSERT INTO `droplist` VALUES (1165, 5488,1,1,1,9765); 
INSERT INTO `droplist` VALUES (1165, 5495,1,1,1,40171); 
INSERT INTO `droplist` VALUES (1165, 6037,8,31,0,318558);