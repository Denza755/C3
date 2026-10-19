new-list value on-server-start-words
new-list value on-server-stop-words
new-list value on-jbf-start-words
new-list value on-jbf-stop-words

: on-server-start: ( -- \ word-name )  ' on-server-start-words list+ ;
: on-server-stop:  ( -- \ word-name )  ' on-server-stop-words  list+ ;
: on-jbf-start:    ( -- \ word-name )  ' on-jbf-start-words    list+ ;
: on-jbf-stop:     ( -- \ word-name )  ' on-jbf-stop-words     list+ ;

: on-jbf-start+    ( 'word -- )  on-jbf-start-words list+ ;

: on-server-start:: ( -- \ word definitions ; )  :noname on-server-start-words list+ ;

