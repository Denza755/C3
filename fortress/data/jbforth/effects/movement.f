: c.paralyze ( target-character -- )
	-1 swap "setParalyzed" { Boolean.class } jexec
;

: c.unparalyze ( target-character -- )
	0 swap "setParalyzed" { Boolean.class } jexec
;
