"The following is a list of all available commands:
(NOTE: unrecognized comand processed as JBForth words)
=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
Short Command                 Description
 a    abort                 - aborts shutdown/restart.
 !    announce <text>       - announces <text> in game.
      enemy <name>          - list all enemy creature around player <name>.
      banip <ip>            - ban <ip>.
      baniplist             - display list of banned ips
      exit                  - closes telnet session.
      gc                    - force garbage collection in JVM.
      gmchat <text>         - Sends a message to all GMs with <text>.
      gmlist                - lists all gms online.
      help                  - shows this help.
      kick <name>           - kick player <name> from server.
      msg <nick> <text>     - Sends a whisper to char <nick> with <text>.
      npc_reload            - reload all npc.
 p    performance           - alias 'p'. shows server performance statistics.
      quest_reload <id|dir> - compile and reload quest script for jynthon engine.
      quit                  - closes telnet session.
      reload_jbf            - reload JBForth (without notify)
      restart <time>        - restarts down server in <time> seconds.
      shutdown <time>       - shuts down server in <time> seconds.
      skill_reload          - reload skill's data.
 s    status                - alias 's' displays basic server statistics.
      tell <text>           - send a message to other telnet connection
      whois <name>          - display infomation about player <name>
      unbanip <ip>          - unban <ip>.
      unblock <ip>          - removes <ip> from hacking protection list.
=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
" value telnet-help
