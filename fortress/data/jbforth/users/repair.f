\ Repair character l2.club-ix.net.ua
\ called as ".repair char-name"

: bypass_do_user_repair
    dup null? if
        drop
        "Character name can not be empty!" show
        exit
    then    
      
    >slashes

    "update `characters` set `x`=-84318, `y`=244579, `z`=-3730 where `char_name` = '" swap s+
    "' and `online`=0 and `account_name` = '" s+
    player@ "AccountName" p@ >slashes s+ 
    "';" s+

    update ?dup if
        "Error while update: " swap s+ show
        exit
    then

    0 = if 
        "Can not repair character!" show
        exit
    then    

    drop
    "Character repaired!" show
;

: user_repair
    '<font color="LEVEL">Enter character name from your account to repair:</font>'
    '<table width=250><tr><td><edit var="charname" width=200 height=15></td>' s+
    '<td><button value="Repair" action="bypass -h jbf_do_user_repair $charname" width=50 height=15 back="sek.cbui94" fore="sek.cbui92"></td></tr></table>' s+
    show
    tail drop \ drop tail of command. Antihack.
;
