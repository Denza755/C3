-- Fix Enhanced weapons price and crystals (Advi - http://l2j-c2datapack.sourceforge.net/bb/viewtopic.php?t=1837)
-- Currently Enhanced weapons have price and crystal_count set to 0. This is why after unsuccessful enchant attempt of such weapon you get 1 crystal instead of normal quantity. 
-- As I don't have official values for this, I've taken price and crystal_count from original weapon and multiplied by 1.15 for A grade, 1.2 for B and 1.25 for C to include some of gemstones used to enhance. 
-- This is rough approximation to be used only until official data are available as number of players enchanting enhanced weapons is growing. 

Select * From Weapon Where crystal_count > 0 and crystal_type not in ('C', 'B', 'A'); 
Select Weapon.item_id, Weapon.name, Weapon.price, Weapon.crystal_count, 
  BaseWeapon.item_id as base_item_id, BaseWeapon.name as base_name, BaseWeapon.price as base_price, BaseWeapon.crystal_count as base_crystal_count, 
  BaseWeapon.price * case when BaseWeapon.crystal_type = 'C' then 1.25 when BaseWeapon.crystal_type = 'B' then 1.2 when BaseWeapon.crystal_type = 'A' then 1.15 end as new_price, 
  BaseWeapon.crystal_count * case when BaseWeapon.crystal_type = 'C' then 1.25 when BaseWeapon.crystal_type = 'B' then 1.2 when BaseWeapon.crystal_type = 'A' then 1.15 end as new_crystal_count 
From Weapon 
  Inner Join Weapon As BaseWeapon On Left(Weapon.Name, length(BaseWeapon.name)) = BaseWeapon.name 
    And BaseWeapon.crystal_type in ('C', 'B', 'A') And BaseWeapon.item_id < 1000 
    And length(Weapon.name) > length(BaseWeapon.name) And length(BaseWeapon.name) > 0 
    And (BaseWeapon.price > 0 Or BaseWeapon.crystal_count > 0) 
Where Weapon.Price = 0; 

Update Weapon 
  Inner Join Weapon As BaseWeapon On Left(Weapon.Name, length(BaseWeapon.name)) = BaseWeapon.name 
    And BaseWeapon.crystal_type in ('C', 'B', 'A') And BaseWeapon.item_id < 1000 
    And length(Weapon.name) > length(BaseWeapon.name) And length(BaseWeapon.name) > 0 
    And (BaseWeapon.price > 0 Or BaseWeapon.crystal_count > 0) 
  Set Weapon.price = BaseWeapon.price * case when BaseWeapon.crystal_type = 'C' then 1.25 when BaseWeapon.crystal_type = 'B' then 1.2 when BaseWeapon.crystal_type = 'A' then 1.15 end, 
  Weapon.crystal_count = BaseWeapon.crystal_count * case when BaseWeapon.crystal_type = 'C' then 1.25 when BaseWeapon.crystal_type = 'B' then 1.2 when BaseWeapon.crystal_type = 'A' then 1.15 end 
Where Weapon.Price = 0; 
