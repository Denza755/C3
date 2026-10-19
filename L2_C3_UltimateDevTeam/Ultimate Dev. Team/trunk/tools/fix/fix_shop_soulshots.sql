-- By Mastermind008 
-- Script to add D,C and B Soulshot, Spiritshot and Blessed Spiritshot to town shops 

-- This line removes the items below and is only needed if the script is ran a second time. 
DELETE from merchant_Buylists WHERE item_id in (1463,1464,1465,2510,2511,2512,3948,3949,3950) AND shop_id < 5000; 

-- Rolento - South Enterance to Wasteland 
INSERT INTO merchant_Buylists VALUES (1463,12,74,22); 

-- Carson and Alexis - Aden Town 
INSERT INTO merchant_Buylists VALUES (1463,12,102,37); 
INSERT INTO merchant_Buylists VALUES (2510,16,102,38); 
INSERT INTO merchant_Buylists VALUES (3948,22,102,39); 
INSERT INTO merchant_Buylists VALUES (1464,18,102,40); 
INSERT INTO merchant_Buylists VALUES (2511,63,102,41); 
INSERT INTO merchant_Buylists VALUES (3949,106,102,42); 
INSERT INTO merchant_Buylists VALUES (1465,62,102,43); 
INSERT INTO merchant_Buylists VALUES (2512,225,102,44); 
INSERT INTO merchant_Buylists VALUES (3950,450,102,45); 
INSERT INTO merchant_Buylists VALUES (1463,12,103,8); 
INSERT INTO merchant_Buylists VALUES (2510,16,103,9); 
INSERT INTO merchant_Buylists VALUES (3948,22,103,10); 
INSERT INTO merchant_Buylists VALUES (1464,18,103,11); 
INSERT INTO merchant_Buylists VALUES (2511,63,103,12); 
INSERT INTO merchant_Buylists VALUES (3949,106,103,13); 
INSERT INTO merchant_Buylists VALUES (1465,62,103,14); 
INSERT INTO merchant_Buylists VALUES (2512,225,103,15); 
INSERT INTO merchant_Buylists VALUES (3950,450,103,16); 

-- Pano - Florian 
INSERT INTO merchant_Buylists VALUES (1463,12,22,25); 
INSERT INTO merchant_Buylists VALUES (2510,16,22,26); 
INSERT INTO merchant_Buylists VALUES (3948,22,22,27); 
INSERT INTO merchant_Buylists VALUES (1464,18,22,28); 
INSERT INTO merchant_Buylists VALUES (2511,63,22,29); 
INSERT INTO merchant_Buylists VALUES (3949,106,22,30); 

-- Ralford - Ivory Tower 
INSERT INTO merchant_Buylists VALUES (1463,12,45,23); 
INSERT INTO merchant_Buylists VALUES (2510,16,45,24); 
INSERT INTO merchant_Buylists VALUES (3948,22,45,25); 
INSERT INTO merchant_Buylists VALUES (1464,18,45,26); 
INSERT INTO merchant_Buylists VALUES (2511,63,45,27); 
INSERT INTO merchant_Buylists VALUES (3949,106,45,28); 
INSERT INTO merchant_Buylists VALUES (1465,62,45,29); 
INSERT INTO merchant_Buylists VALUES (2512,225,45,30); 
INSERT INTO merchant_Buylists VALUES (3950,450,45,31); 

-- Hally - Hunter's Village 
INSERT INTO merchant_Buylists VALUES (1463,12,65,31); 
INSERT INTO merchant_Buylists VALUES (2510,16,65,32); 
INSERT INTO merchant_Buylists VALUES (3948,22,65,33); 
INSERT INTO merchant_Buylists VALUES (1464,18,65,34); 
INSERT INTO merchant_Buylists VALUES (2511,63,65,35); 
INSERT INTO merchant_Buylists VALUES (3949,106,65,36); 

-- Sara - Oren Town 
INSERT INTO merchant_Buylists VALUES (1463,12,51,30); 
INSERT INTO merchant_Buylists VALUES (2510,16,51,31); 
INSERT INTO merchant_Buylists VALUES (3948,22,51,32); 
INSERT INTO merchant_Buylists VALUES (1464,18,51,33); 
INSERT INTO merchant_Buylists VALUES (2511,63,51,34); 
INSERT INTO merchant_Buylists VALUES (3949,106,51,35); 

-- Denkus and Helvetia - Giran 
INSERT INTO merchant_Buylists VALUES (1463,12,24,58); 
INSERT INTO merchant_Buylists VALUES (2510,16,24,59); 
INSERT INTO merchant_Buylists VALUES (3948,22,24,60); 
INSERT INTO merchant_Buylists VALUES (1464,18,24,61); 
INSERT INTO merchant_Buylists VALUES (2511,63,24,62); 
INSERT INTO merchant_Buylists VALUES (3949,106,24,63); 
INSERT INTO merchant_Buylists VALUES (1463,12,23,58); 
INSERT INTO merchant_Buylists VALUES (2510,16,23,59); 
INSERT INTO merchant_Buylists VALUES (3948,22,23,60); 
INSERT INTO merchant_Buylists VALUES (1464,18,23,61); 
INSERT INTO merchant_Buylists VALUES (2511,63,23,62); 
INSERT INTO merchant_Buylists VALUES (3949,106,23,63); 

-- Sonia - Dion 
INSERT INTO merchant_Buylists VALUES (1463,12,19,148); 
INSERT INTO merchant_Buylists VALUES (2510,16,19,149); 
INSERT INTO merchant_Buylists VALUES (3948,22,19,150); 
INSERT INTO merchant_Buylists VALUES (1464,18,19,151); 
INSERT INTO merchant_Buylists VALUES (2511,63,19,152); 
INSERT INTO merchant_Buylists VALUES (3949,106,19,153); 

-- Varan - Gludio 
INSERT INTO merchant_Buylists VALUES (1463,12,64,35); 
INSERT INTO merchant_Buylists VALUES (2510,16,64,36); 
INSERT INTO merchant_Buylists VALUES (3948,22,64,37); 

-- Poesia and Nestle - Gludin 
INSERT INTO merchant_Buylists VALUES (1463,12,68,35); 
INSERT INTO merchant_Buylists VALUES (2510,16,68,36); 
INSERT INTO merchant_Buylists VALUES (3948,22,68,37); 
INSERT INTO merchant_Buylists VALUES (1463,12,67,35); 
INSERT INTO merchant_Buylists VALUES (2510,16,67,36); 
INSERT INTO merchant_Buylists VALUES (3948,22,67,37); 

-- To remove this modification just uncomment the following line and run the srcipt again 
-- DELETE from merchant_Buylists WHERE item_id in (1463,1464,1465,2510,2511,2512,3948,3949,3950) AND shop_id < 5000; 
