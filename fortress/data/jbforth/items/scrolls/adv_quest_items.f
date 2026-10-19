new-hashmap
	{ 5922 5923 5924 5925 5926 5927 5928 5929 5930 5931 5932 5933 5934 5935 5936 5937 5938 5939 5940 5941 5942 5943 } => 5944
	{ 5942 5943 5945 5946 5947 5948 5949 5950 5951 5952 5953 5954 } => 5955
	{ 5970 5971 5977 5978 5979 5986 5993 5994 5995 5997 5983 6001 } => 5966
	{ 5970 5971 5975 5976 5980 5985 5993 5994 5995 5997 5983 6001 } => 5967
	{ 5973 5974 5981 5984 5989 5990 5991 5992 5996 5998 5999 6000 5988 5983 6001 } => 5968
	{ 5970 5971 5982 5987 5989 5990 5991 5992 5996 5998 5999 6000 5972 6001 } => 5969
	{ 6011 6012 6013 6014 6015 6016 6018 6019 6020 } value award_pouch
	award_pouch => 6007
	award_pouch => 6008
	award_pouch => 6009
	award_pouch => 6010
value adv_quest_items-map

: list-rnd@  ( list -- random_item_from_list )
	dup list# choose list@
;

: adv_quest_items-use  ( -- )
	adv_quest_items-map item-id string m@
	?dup null? if exit then
	list-rnd@
	item_add
	item-id item_remove
	inventory_show
;

: adv_quest_items-register  ( list item_id -- )	nip	": use-item-[  ]   adv_quest_items-use ;" parse eval ;

adv_quest_items-map ['] adv_quest_items-register do-hashmap
