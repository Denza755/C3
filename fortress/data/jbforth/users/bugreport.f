\ Bug Report in game v0.1 from kuta                                                                                                           
                                                                                                                                    
\ called as ".bug"                                                                                   
                                                                                                                                    
: bypass_bug_user_report  ( "textreport" -- )                                                                                       
                                                                                                                                    
        dup null? if                                                                                                                
                drop                                                                                                                
                "Bug report can not be empty!" show                                                                                 
                exit                                                                                                                
        then                                                                                                                        
        "Bug Report:text report: " swap s+ .out                                                                                     
        player@  dup hcoords@                                                                                                       
        "Bug Report:H:[  ] Z:[  ] Y:[  ] X:[  ] player:[  ]" parse .out                                                             
                                                                                                                                    
        player@ target@  null? if                                                                                                   
                                exit                                                                                                
                                 then                                                                                               
        player@ target@ dup hcoords@ "Bug Report:H:[  ] Z:[  ] Y:[  ] X:[  ] target:[  ]" parse .out                                
                                                                                                                                    
;                                                                                                                                   
                                                                                                                                    
: user_bug                                                                                                                          
        '<font color="LEVEL">Enter text bug report:</font>'                                                                         
        '<table width=300>' s+                                                                                                      
        '<tr><td><edit var="textreport" width=200 height=300></td></tr>' s+                                                         
        '<tr><td><button value="Send" action="bypass -h jbf_bug_user_report $textreport" width=50 height=15 back="sek.cbui94" fore="sek.cbui92"></td></tr></table>' s+                                                                                                  
        show                                                                                                                        
; 