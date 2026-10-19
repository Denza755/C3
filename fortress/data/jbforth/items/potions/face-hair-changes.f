: use-fhc  ( type name -- )
	player@ swap
	"set" swap s+ { int.class } jexec
	player@ coords@ jump
	player@ "store" 0 jexec
;

: use-item-5235   0 "Face" use-fhc ; \ Facelifting Potion - A
: use-item-5236   1 "Face" use-fhc ; \ Facelifting Potion - B
: use-item-5237   2 "Face" use-fhc ; \ Facelifting Potion - C
: use-item-5238   0 "HairColor" use-fhc ; \ Dye Potion - A
: use-item-5239   1 "HairColor" use-fhc ; \ Dye Potion - B
: use-item-5240   2 "HairColor" use-fhc ; \ Dye Potion - C
: use-item-5241   3 "HairColor" use-fhc ; \ Dye Potion - D
: use-item-5242   0 "HairStyle" use-fhc ; \ Hair Style Change Potion - A
: use-item-5243   1 "HairStyle" use-fhc ; \ Hair Style Change Potion - B
: use-item-5244   2 "HairStyle" use-fhc ; \ Hair Style Change Potion - C
: use-item-5245   3 "HairStyle" use-fhc ; \ Hair Style Change Potion - D
: use-item-5246   4 "HairStyle" use-fhc ; \ Hair Style Change Potion - E
: use-item-5247   5 "HairStyle" use-fhc ; \ Hair Style Change Potion - F
: use-item-5248   6 "HairStyle" use-fhc ; \ Hair Style Change Potion - G

: (give_fhc)
	10 swap items_add
;

: gm_give_fhc
	{ 5235 5236 5237 5238 5239 5240 5241 5242 5243 5244 5245 5246 5247 5248 } ['] (give_fhc) do-list 
;
	