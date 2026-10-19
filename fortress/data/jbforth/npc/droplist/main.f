\ { min_level item_id min max chance }
{
	{ 1 1864 1 1 50000 }
	{ 1 1865 1 1 25000 }
	{ 1 1866 1 1 16666 }
	{ 1 1867 1 1 33333 }
	{ 1 1868 1 1 50000 }
	{ 1 1869 1 1 25000 }
	{ 1 1870 1 1 25000 }
	{ 1 1871 1 1 25000 }
	{ 1 1872 1 1 33333 }
	{ 1 1873 1 1 10000 }
	{ 20 1874 1 1 1666 }
	{ 20 1875 1 1 1666 }
	{ 20 1876 1 1 5000 }
	{ 20 1877 1 1 1000 }
	{ 40 4039 1 1 833 }
	{ 40 4040 1 1 500 }
	{ 40 4041 1 1 217 }
	{ 40 4042 1 1 833 }
	{ 40 4043 1 1 833 }
	{ 40 4044 1 1 833 }
} value droplist-matherial-randdrop

: matherial-randdrop-do-append
	list> drop addrop-level-beyond
;

: matherial-randdrop-append
"ALT_GAME_MATHERIALSDROP" config@ if
droplist-matherial-randdrop
	['] matherial-randdrop-do-append do-list
then
;