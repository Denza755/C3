UPDATE weapon w SET w.crystal_count = (w.p_dam*10+w.m_dam*10) WHERE w.crystal_type='s' AND w.crystal_count=0 ;
UPDATE weapon w SET w.crystal_count = 159 WHERE  w.item_id =634 ;

UPDATE weapon w SET w.crystal_count = 154 WHERE  w.item_id =6678 ;
UPDATE armor a SET a.crystal_count = (a.p_def*3) WHERE a.crystal_type='s' AND a.armor_type='heavy' AND a.crystal_count=0 ;
UPDATE armor a SET a.crystal_count = (a.p_def*2) WHERE a.crystal_type='s' AND a.armor_type='light' AND  a.crystal_count=0 ;
UPDATE armor a SET a.crystal_count = (a.mp_bonus+25) WHERE a.crystal_type='s' AND a.armor_type='magic' AND  a.crystal_count=0 ;
UPDATE armor a SET a.crystal_count = (a.p_def) WHERE a.crystal_type='s' AND a.armor_type='none' AND a.crystal_count=0 ;
UPDATE armor a SET a.crystal_count = (a.m_def*3) WHERE a.crystal_type='s' AND a.armor_type='none' AND a.crystal_count=0 AND a.p_def=0;