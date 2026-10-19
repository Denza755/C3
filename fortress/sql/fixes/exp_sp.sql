update npc set exp =  1.5*hp where exp/hp > 1.5;
update npc set sp  = 0.15*hp where  sp/hp > 0.15;

update npc set exp =   0.15*hp where exp = 0;
update npc set  sp =  0.015*hp where sp  = 0;
