CREATE TABLE IF NOT EXISTS banned_ips (
  ip VARCHAR(15) NOT NULL,
  admin varchar(45) ,
  PRIMARY KEY  (ip)
);