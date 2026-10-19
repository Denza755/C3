-- The following is for the Necropolis of Sacrifice --UPDATED 01/23/2006 
-- Each section below is commented so you know what it is for 
-- Please check it out carefully 
-- I have tested this over and over and  everything seems to function fine. 
-- These changes are based on a fresh install of the lates server and datapack builds as of 01/22/2006 @ 3:30PM EST 
-- using the experimental SQL data that comes with the datapack 

-- Things that need addressed are: 
-- 1. Correct weapon ID for Grave Keeper Spartoi (mod id 1142) 
-- 2. Better drop date for mobs. What is currently listed is from several sites but none listed spoil data and I know these mobs spoil stuff. 
-- 3. Non Lith and Gigant mobs are aggro but the Lith and Gigants are not social to them. Lith and Gigant mobs are social to there own kind though 

-- Hope this is helpful 

-- BACKUP YOUR DATA BEFORE RUNNING ANYTHING -- 

-- ---------------------------- 
-- Updates the NPC table for Necro mobs. Includes correct weapons 
-- exceot for Grave Keeper Spartoi, can't find the right wepaon ID 
-- so I gave him as Shemshir. (Better than nothing) 
-- ---------------------------- 
DELETE FROM npc WHERE id = 1139; 
DELETE FROM npc WHERE id = 1140; 
DELETE FROM npc WHERE id = 1141; 
DELETE FROM npc WHERE id = 1142; 
DELETE FROM npc WHERE id = 1166; 
DELETE FROM npc WHERE id = 1167; 
DELETE FROM npc WHERE id = 1168; 
DELETE FROM npc WHERE id = 1169; 
DELETE FROM npc WHERE id = 1187; 
DELETE FROM npc WHERE id = 1188; 
DELETE FROM npc WHERE id = 1189; 
DELETE FROM npc WHERE id = 1190; 
DELETE FROM npc WHERE id = 1208; 
DELETE FROM npc WHERE id = 1209; 
DELETE FROM npc WHERE id = 1210; 
DELETE FROM npc WHERE id = 1211; 

INSERT INTO `npc` VALUES 
(1139,'Catacomb Barbed Bat','vampire_bat',10.00,33.50,23,'male','L2Monster',40,1692,213,40,21,30,20,43,10,4782,64,53,296,35,217,253,0,253,0,0,0,80,143,NULL,NULL,0), 
(1140,'Catacomb Wisp','monster_eye',21.00,43.00,26,'male','L2Monster',40,2013,250,40,21,30,20,43,10,6111,118,67,296,44,217,253,300,253,0,0,0,80,132,NULL,NULL,0), 
(1141,'Catacomb Serpent','onyx_beast',14.00,14.50,28,'male','L2Monster',40,2245,283,40,21,30,20,43,10,7087,122,104,296,52,217,253,300,253,73,0,0,80,143,NULL,NULL,0), 
(1142,'Grave Keeper Spartoi','spartoi',11.00,27.00,29,'male','L2Monster',40,2294,298,40,21,30,20,43,10,7603,128,104,296,56,217,253,300,253,73,0,0,80,154,NULL,NULL,1), 
(1166,'Lith Scout','temple_guard_a_20_sm',12.00,33.60,21,'male','L2Monster',40,1496,196,40,21,30,20,43,10,3987,60,53,296,31,217,253,0,253,5795,0,0,80,143,'lith_clan',150,0), 
(1167,'Lith Witch','temple_savant_a_20_sm',8.00,32.00,24,'male','L2Monster',40,1741,225,40,21,30,20,43,10,5207,82,65,296,37,217,253,0,253,5797,0,0,80,121,'lith_clan',150,0), 
(1168,'Lith Warrior','temple_guard_b_20_sm',12.00,33.60,27,'male','L2Monster',40,2060,273,40,21,30,20,43,10,6590,120,79,296,49,217,253,0,253,5796,0,0,80,143,'lith_clan',150,0), 
(1169,'Lith Guard','temple_guard_a',15.00,42.00,30,'male','L2Monster',40,2416,314,40,21,30,20,43,10,8136,130,92,296,60,217,253,0,253,5795,0,0,80,154,'lith_clan',150,0), 
(1187,'Gigant Slave','tomb_guard_a_20_sm',8.00,32.00,21,'male','L2Monster',40,1504,196,40,21,30,20,43,10,3987,60,53,296,31,217,253,0,253,5791,0,0,80,143,'Tomb_clan',150,0), 
(1188,'Gigant Acolyte','tomb_savant_a_20_sm',8.00,32.00,24,'male','L2Monster',40,1741,223,40,21,30,20,43,10,5207,82,65,296,37,217,253,0,253,5793,0,0,80,121,'Tomb_clan',150,0), 
(1189,'Gigant Overseer','tomb_guard_b_20_sm',12.00,34.00,27,'male','L2Monster',40,2060,273,40,21,30,20,43,10,6590,120,79,296,49,217,253,0,253,5792,0,0,80,121,'Tomb_clan',150,0), 
(1190,'Gigant Footman','tomb_guard_a',10.00,40.00,30,'male','L2Monster',40,2416,314,40,21,30,20,43,10,8136,130,92,296,60,217,253,0,253,5791,0,0,80,154,'Tomb_clan',150,0), 
(1208,'Holy Land Watchman','skeleton',11.00,25.00,20,'male','L2Monster',40,696,188,40,21,30,20,43,10,3616,54,46,296,30,217,253,300,253,946,111,0,80,143,'holyland_clan',150,1), 
(1209,'Holy Land Seer','binding',12.00,21.00,22,'male','L2Monster',40,1496,211,40,21,30,20,43,10,4375,62,55,296,34,217,253,300,253,0,0,0,80,121,'holyland_clan',NULL,1), 
(1210,'Vault Guardian','dre_vanul',14.00,36.00,25,'male','L2Monster',40,1903,244,40,21,30,20,43,10,5650,92,69,296,42,217,253,300,253,0,0,0,80,143,NULL,NULL,0), 
(1211,'Vault Seer','succubus',10.00,36.00,27,'male','L2Monster',40,2127,273,40,21,30,20,43,10,6590,120,79,296,49,217,253,300,253,0,0,0,80,121,NULL,NULL,0); 


-- ---------------------------- 
-- Clears the Necropolist of all spawns as they are messed up by default away 
-- ---------------------------- 

Delete from spawnlist where 
(locx between -52300 and -41150) and (locy between 209200 and 220750) and (locz between -6000 and - 5000); 


-- ---------------------------- 
-- Populate Droplist for mods in the Necropolis UPDATED 01/23/2006 
-- Drop data by edinho_sp - Many Thx 
-- ---------------------------- 

DELETE FROM droplist WHERE mobid = 1208; 
DELETE FROM droplist WHERE mobid = 1187; 
DELETE FROM droplist WHERE mobid = 1209; 
DELETE FROM droplist WHERE mobid = 1139; 
DELETE FROM droplist WHERE mobid = 1188; 
DELETE FROM droplist WHERE mobid = 1210; 
DELETE FROM droplist WHERE mobid = 1140; 
DELETE FROM droplist WHERE mobid = 1189; 
DELETE FROM droplist WHERE mobid = 1211; 
DELETE FROM droplist WHERE mobid = 1141; 
DELETE FROM droplist WHERE mobid = 1142; 
DELETE FROM droplist WHERE mobid = 1190; 
DELETE FROM droplist WHERE mobid = 1166; 
DELETE FROM droplist WHERE mobid = 1167; 
DELETE FROM droplist WHERE mobid = 1168; 
DELETE FROM droplist WHERE mobid = 1169; 


-- ---------------------------- 
--  Holy Land Watchman 
-- ---------------------------- 
INSERT INTO `droplist` VALUES (1208, 38,1,1,0,15867); 
INSERT INTO `droplist` VALUES (1208, 50,1,1,0,15867); 
INSERT INTO `droplist` VALUES (1208, 57,441,882,0,700000); 
INSERT INTO `droplist` VALUES (1208, 734,1,1,0,17884); 
INSERT INTO `droplist` VALUES (1208, 734,1,1,1,102697); 
INSERT INTO `droplist` VALUES (1208, 1866,1,2,0,45467); 
INSERT INTO `droplist` VALUES (1208, 1871,1,3,0,22228); 
INSERT INTO `droplist` VALUES (1208, 1871,1,5,1,111141); 
INSERT INTO `droplist` VALUES (1208, 1897,1,1,1,781464); 
INSERT INTO `droplist` VALUES (1208, 1925,1,1,0,301287); 
-- ---------------------------- 
--  Holy Land Seer 
-- ---------------------------- 
INSERT INTO `droplist` VALUES (1209, 57,380,735,0,700000); 
INSERT INTO `droplist` VALUES (1209, 625,1,1,0,8583); 
INSERT INTO `droplist` VALUES (1209, 734,1,1,0,18363); 
INSERT INTO `droplist` VALUES (1209, 1148,1,1,0,6026); 
INSERT INTO `droplist` VALUES (1209, 1804,1,1,1,125034); 
INSERT INTO `droplist` VALUES (1209, 1865,1,3,0,63348); 
INSERT INTO `droplist` VALUES (1209, 1870,1,3,0,58839); 
INSERT INTO `droplist` VALUES (1209, 1898,1,1,1,381783); 
INSERT INTO `droplist` VALUES (1209, 2151,1,1,1,633084); 
-- ---------------------------- 
--  Catacomb Barbed Bat 
-- ---------------------------- 
INSERT INTO `droplist` VALUES (1139, 27,1,1,0,3054); 
INSERT INTO `droplist` VALUES (1139, 33,1,1,0,1047); 
INSERT INTO `droplist` VALUES (1139, 57,431,760,0,700000); 
INSERT INTO `droplist` VALUES (1139, 1804,1,1,1,126938); 
INSERT INTO `droplist` VALUES (1139, 1865,1,3,0,64284); 
INSERT INTO `droplist` VALUES (1139, 1870,1,3,0,166712); 
INSERT INTO `droplist` VALUES (1139, 1898,1,1,1,386206); 
INSERT INTO `droplist` VALUES (1139, 1926,1,1,1,292477); 
INSERT INTO `droplist` VALUES (1139, 1927,1,1,0,44045); 
INSERT INTO `droplist` VALUES (1139, 1929,1,1,0,16607); 
INSERT INTO `droplist` VALUES (1139, 3953,1,1,0,22327); 
-- ---------------------------- 
--  Vault Guardian 
-- ---------------------------- 
INSERT INTO `droplist` VALUES (1210, 57,546,1023,0,700000); 
INSERT INTO `droplist` VALUES (1210, 123,1,1,0,284); 
INSERT INTO `droplist` VALUES (1210, 1866,1,2,0,50013); 
INSERT INTO `droplist` VALUES (1210, 1866,1,4,1,333424); 
INSERT INTO `droplist` VALUES (1210, 1871,1,2,0,58839); 
INSERT INTO `droplist` VALUES (1210, 1877,1,1,0,23042); 
INSERT INTO `droplist` VALUES (1210, 1877,1,1,1,130754); 
INSERT INTO `droplist` VALUES (1210, 1899,1,1,1,429302); 
INSERT INTO `droplist` VALUES (1210, 2017,1,1,0,7714); 
INSERT INTO `droplist` VALUES (1210, 2182,1,1,0,67586); 
INSERT INTO `droplist` VALUES (1210, 5285,1,1,0,284); 
INSERT INTO `droplist` VALUES (1210, 5531,1,1,0,17653); 
-- ---------------------------- 
--  Catacomb Wisp 
-- ---------------------------- 
INSERT INTO `droplist` VALUES (1140, 57,478,912,0,700000); 
INSERT INTO `droplist` VALUES (1140, 347,1,1,0,1418); 
INSERT INTO `droplist` VALUES (1140, 376,1,1,0,2268); 
INSERT INTO `droplist` VALUES (1140, 1866,1,3,0,50013); 
INSERT INTO `droplist` VALUES (1140, 1871,1,4,0,58839); 
INSERT INTO `droplist` VALUES (1140, 1871,1,7,1,125034); 
INSERT INTO `droplist` VALUES (1140, 1877,1,1,0,23431); 
INSERT INTO `droplist` VALUES (1140, 1899,1,1,1,434901); 
INSERT INTO `droplist` VALUES (1140, 2152,1,1,1,476320); 
INSERT INTO `droplist` VALUES (1140, 2260,1,1,0,9160); 
-- ---------------------------- 
--  Grave Keeper Spartoi 
-- ---------------------------- 
INSERT INTO `droplist` VALUES (1142, 57,536,1061,0,700000); 
INSERT INTO `droplist` VALUES (1142, 848,1,1,0,2930); 
INSERT INTO `droplist` VALUES (1142, 879,1,1,0,4391); 
INSERT INTO `droplist` VALUES (1142, 911,1,1,0,2198); 
INSERT INTO `droplist` VALUES (1142, 955,1,1,0,2496); 
INSERT INTO `droplist` VALUES (1142, 1868,2,8,0,243969); 
INSERT INTO `droplist` VALUES (1142, 1873,1,1,0,25006); 
INSERT INTO `droplist` VALUES (1142, 1876,1,1,0,30768); 
INSERT INTO `droplist` VALUES (1142, 1900,1,1,0,66111); 
INSERT INTO `droplist` VALUES (1142, 1900,1,1,1,340229); 
INSERT INTO `droplist` VALUES (1142, 1933,1,1,1,361109); 
INSERT INTO `droplist` VALUES (1142, 2153,1,1,1,310644); 
-- ---------------------------- 
--  Vault Seer 
-- ---------------------------- 
INSERT INTO `droplist` VALUES (1211, 57,496,956,0,700000); 
INSERT INTO `droplist` VALUES (1211, 156,1,1,0,294); 
INSERT INTO `droplist` VALUES (1211, 167,1,1,0,294); 
INSERT INTO `droplist` VALUES (1211, 178,1,1,0,294); 
INSERT INTO `droplist` VALUES (1211, 1867,2,5,0,52646); 
INSERT INTO `droplist` VALUES (1211, 1872,2,5,0,142896); 
INSERT INTO `droplist` VALUES (1211, 1875,1,1,0,12661); 
INSERT INTO `droplist` VALUES (1211, 1881,1,1,0,17246); 
INSERT INTO `droplist` VALUES (1211, 1931,1,1,1,79323); 
INSERT INTO `droplist` VALUES (1211, 1932,1,1,1,446202); 
INSERT INTO `droplist` VALUES (1211, 2152,1,1,1,483224); 
INSERT INTO `droplist` VALUES (1211, 5437,1,1,0,3345); 
-- ---------------------------- 
--  Catacomb Serpent 
-- ---------------------------- 
INSERT INTO `droplist` VALUES (1141, 57,615,1204,0,700000); 
INSERT INTO `droplist` VALUES (1141, 432,1,1,0,1959); 
INSERT INTO `droplist` VALUES (1141, 465,1,1,0,3135); 
INSERT INTO `droplist` VALUES (1141, 1867,2,5,0,32266); 
INSERT INTO `droplist` VALUES (1141, 1872,2,5,0,166712); 
INSERT INTO `droplist` VALUES (1141, 1875,1,1,0,12824); 
INSERT INTO `droplist` VALUES (1141, 1881,1,1,0,21282); 
INSERT INTO `droplist` VALUES (1141, 1881,1,3,1,79766); 
INSERT INTO `droplist` VALUES (1141, 1931,1,1,1,79513); 
INSERT INTO `droplist` VALUES (1141, 2178,1,1,0,93658); 
INSERT INTO `droplist` VALUES (1141, 2178,1,1,1,454669); 
-- ---------------------------- 
--  Gigant Acolyte 
-- ---------------------------- 
INSERT INTO `droplist` VALUES (1188, 735,1,1,0,22671); 
INSERT INTO `droplist` VALUES (1188, 1105,1,1,0,4972); 
INSERT INTO `droplist` VALUES (1188, 1310,1,1,0,311); 
INSERT INTO `droplist` VALUES (1188, 1804,1,1,1,128735); 
INSERT INTO `droplist` VALUES (1188, 1867,1,4,0,29419); 
INSERT INTO `droplist` VALUES (1188, 1872,1,4,0,62517); 
INSERT INTO `droplist` VALUES (1188, 1882,1,1,0,9261); 
INSERT INTO `droplist` VALUES (1188, 1926,1,1,1,295939); 
INSERT INTO `droplist` VALUES (1188, 2151,1,1,1,649528); 
INSERT INTO `droplist` VALUES (1188, 6360,72,143,0,700000); 
INSERT INTO `droplist` VALUES (1188, 6362,21,39,0,700000); 
-- ---------------------------- 
--  Gigant Overseer - Corrected Mob ID - Thx to Aikimaniac 
-- ---------------------------- 
INSERT INTO `droplist` VALUES (1189, 847,1,1,0,4474); 
INSERT INTO `droplist` VALUES (1189, 890,1,1,0,6679); 
INSERT INTO `droplist` VALUES (1189, 910,1,1,0,3354); 
INSERT INTO `droplist` VALUES (1189, 1868,2,7,0,236471); 
INSERT INTO `droplist` VALUES (1189, 1873,1,1,0,24396); 
INSERT INTO `droplist` VALUES (1189, 1889,1,1,0,40010); 
INSERT INTO `droplist` VALUES (1189, 1889,1,1,1,177039); 
INSERT INTO `droplist` VALUES (1189, 1899,1,1,0,90033); 
INSERT INTO `droplist` VALUES (1189, 1899,1,1,1,440649); 
INSERT INTO `droplist` VALUES (1189, 2262,1,1,0,9321); 
INSERT INTO `droplist` VALUES (1189, 5549,1,1,0,58564); 
INSERT INTO `droplist` VALUES (1189, 5549,1,1,1,306832); 
INSERT INTO `droplist` VALUES (1189, 6360,97,190,0,700000); 
INSERT INTO `droplist` VALUES (1189, 6362,22,41,0,700000); 
-- ---------------------------- 
--  Gigant Footman 
-- ---------------------------- 
INSERT INTO `droplist` VALUES (1190, 605,1,1,0,3815); 
INSERT INTO `droplist` VALUES (1190, 1061,1,1,1,307776); 
INSERT INTO `droplist` VALUES (1190, 1123,1,1,0,3815); 
INSERT INTO `droplist` VALUES (1190, 1864,2,8,0,125034); 
INSERT INTO `droplist` VALUES (1190, 1869,1,4,0,125034); 
INSERT INTO `droplist` VALUES (1190, 1880,1,1,0,33342); 
INSERT INTO `droplist` VALUES (1190, 1900,1,1,1,346115); 
INSERT INTO `droplist` VALUES (1190, 2153,1,1,0,94365); 
INSERT INTO `droplist` VALUES (1190, 2153,1,1,1,458841); 
INSERT INTO `droplist` VALUES (1190, 5550,1,1,0,8451); 
INSERT INTO `droplist` VALUES (1190, 6035,1,1,0,15787); 
INSERT INTO `droplist` VALUES (1190, 6360,136,277,0,700000); 
INSERT INTO `droplist` VALUES (1190, 6362,26,47,0,700000); 
-- ---------------------------- 
--  Lith Scout 
-- ---------------------------- 
INSERT INTO `droplist` VALUES (1166, 25,1,1,0,3442); 
INSERT INTO `droplist` VALUES (1166, 26,1,1,0,2226); 
INSERT INTO `droplist` VALUES (1166, 32,1,1,0,5494); 
INSERT INTO `droplist` VALUES (1166, 34,1,1,0,3565); 
INSERT INTO `droplist` VALUES (1166, 1868,2,5,0,216979); 
INSERT INTO `droplist` VALUES (1166, 1868,2,10,1,847689); 
INSERT INTO `droplist` VALUES (1166, 1873,1,1,0,21745); 
INSERT INTO `droplist` VALUES (1166, 1873,1,1,1,125034); 
INSERT INTO `droplist` VALUES (1166, 1898,1,1,1,377461); 
INSERT INTO `droplist` VALUES (1166, 1926,1,1,0,53894); 
INSERT INTO `droplist` VALUES (1166, 1928,1,1,0,76766); 
INSERT INTO `droplist` VALUES (1166, 2176,1,1,0,125034); 
INSERT INTO `droplist` VALUES (1166, 6037,1,4,0,167550); 
INSERT INTO `droplist` VALUES (1166, 6360,60,124,0,700000); 
INSERT INTO `droplist` VALUES (1166, 6362,26,47,0,700000); 
-- ---------------------------- 
--  Lith Witch 
-- ---------------------------- 
INSERT INTO `droplist` VALUES (1167, 735,1,1,0,22671); 
INSERT INTO `droplist` VALUES (1167, 1105,1,1,0,4972); 
INSERT INTO `droplist` VALUES (1167, 1310,1,1,0,311); 
INSERT INTO `droplist` VALUES (1167, 1804,1,1,1,128735); 
INSERT INTO `droplist` VALUES (1167, 1867,1,4,0,29419); 
INSERT INTO `droplist` VALUES (1167, 1872,1,4,0,62517); 
INSERT INTO `droplist` VALUES (1167, 1882,1,1,0,9261); 
INSERT INTO `droplist` VALUES (1167, 1926,1,1,1,295939); 
INSERT INTO `droplist` VALUES (1167, 2151,1,1,0,147969); 
INSERT INTO `droplist` VALUES (1167, 6360,72,143,0,700000); 
INSERT INTO `droplist` VALUES (1167, 6362,21,39,0,700000); 
-- ---------------------------- 
--  Lith Warrior 
-- ---------------------------- 
INSERT INTO `droplist` VALUES (1168, 847,1,1,0,4474); 
INSERT INTO `droplist` VALUES (1168, 890,1,1,0,6679); 
INSERT INTO `droplist` VALUES (1168, 910,1,1,0,3354); 
INSERT INTO `droplist` VALUES (1168, 1868,2,7,0,236471); 
INSERT INTO `droplist` VALUES (1168, 1873,1,1,0,24396); 
INSERT INTO `droplist` VALUES (1168, 1889,1,1,0,39411); 
INSERT INTO `droplist` VALUES (1168, 1889,1,1,1,215112); 
INSERT INTO `droplist` VALUES (1168, 1899,1,1,0,90033); 
INSERT INTO `droplist` VALUES (1168, 1899,1,1,1,440649); 
INSERT INTO `droplist` VALUES (1168, 2262,1,1,0,9321); 
INSERT INTO `droplist` VALUES (1168, 5549,1,1,0,58564); 
INSERT INTO `droplist` VALUES (1168, 5549,1,1,1,306832); 
INSERT INTO `droplist` VALUES (1168, 6037,2,6,0,182531); 
INSERT INTO `droplist` VALUES (1168, 6360,97,190,0,700000); 
INSERT INTO `droplist` VALUES (1168, 6362,22,44,0,700000); 
-- ---------------------------- 
--  Lith Guard 
-- ---------------------------- 
INSERT INTO `droplist` VALUES (1169, 605,1,1,0,3815); 
INSERT INTO `droplist` VALUES (1169, 1061,1,1,1,307776); 
INSERT INTO `droplist` VALUES (1169, 1123,1,1,0,3815); 
INSERT INTO `droplist` VALUES (1169, 1864,2,8,0,125034); 
INSERT INTO `droplist` VALUES (1169, 1869,1,4,0,125034); 
INSERT INTO `droplist` VALUES (1169, 1880,1,1,0,33342); 
INSERT INTO `droplist` VALUES (1169, 1900,1,1,1,346115); 
INSERT INTO `droplist` VALUES (1169, 2153,1,1,0,94365); 
INSERT INTO `droplist` VALUES (1169, 2153,1,1,1,314551); 
INSERT INTO `droplist` VALUES (1169, 5550,1,1,0,8451); 
INSERT INTO `droplist` VALUES (1169, 6360,136,277,0,700000); 
INSERT INTO `droplist` VALUES (1169, 6362,26,52,0,700000); 

-- Delete previous locations data 
Delete from locations where loc_id = 10006; 

-- Create Location for first room - more to come 
INSERT INTO locations values 
(10006,-43570,209870,-5085,-5085,0), 
(10006,-43570,211855,-5085,-5085,0), 
(10006,-42440,211855,-5085,-5085,0), 
(10006,-42440,209870,-5085,-5085,0); 

-- Clears Spwnlist- Only needed if you have ran this batch file previously. The delete listed above clears only from the deafult datapack and does so baced on X,Y and Z Coordinates 
-- Since I am controlling spawns based on location ID"s they have no X, Y and Z coordinates to delete from. Each room listed as a location give better random spawn in the room 
-- instead of statically assigning mobs to fixed X,Y and Z 
Delete from spawnlist where location = 'SacrificeNecropolis'; 

-- Ziggurat Gatekeepers 
INSERT INTO `spawnlist` (location, count, npc_templateid, locx, locy, locz, randomx, randomy, heading, respawn_delay, loc_id) VALUES ('SacrificeNecropolist',1,8095,-41577,209390,-5113,0,0,16384,5,0), 
('SacrificeNecropolist',1,8103,-41564,210119,-5081,0,0,16384,5,0); 

-- Mobs First Room Only - More rooms to come --- Removed location id so they add to bottom of list Thx to Slyce 
INSERT INTO `spawnlist` (location, count, npc_templateid, locx, locy, locz, randomx, randomy, heading, respawn_delay, loc_id) VALUES 
('SacrificeNecropolis',1,1208,0,0,0,400,900,16384,15,10006), 
('SacrificeNecropolis',1,1208,0,0,0,400,900,16384,15,10006), 
('SacrificeNecropolis',1,1209,0,0,0,400,900,16384,15,10006), 
('SacrificeNecropolis',1,1209,0,0,0,400,900,16384,15,10006), 
('SacrificeNecropolis',1,1187,0,0,0,400,900,16384,15,10006), 
('SacrificeNecropolis',1,1187,0,0,0,400,900,16384,15,10006), 
('SacrificeNecropolis',1,1166,0,0,0,400,900,16384,15,10006), 
('SacrificeNecropolis',1,1166,0,0,0,400,900,16384,15,10006);