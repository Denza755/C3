: in-zone?  ( player zones -- flag )
	swap "withinZones" 1 jget int
;

: in-town?  ( player -- )
	world@ "zoneTown" field@ 
	in-zone?
;
