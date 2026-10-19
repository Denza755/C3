CREATE TABLE IF NOT EXISTS ally_data (
  ally_id INT NOT NULL default 0,
  ally_name varchar(45) ,
  leader_id INT,
  ally_crestid INT,
  PRIMARY KEY  (ally_id),
  KEY `leader_id` (`leader_id`)
);

UPDATE clan_data SET ally_id=0 WHERE clan_id>0;
UPDATE characters SET allyid=0 WHERE obj_Id>0;