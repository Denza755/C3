 NOTE: This short guide is for a C3, 3.0.0 release of L2J Server.

====================
L2J Server v3.0.0
====================
$Date: 2005/09/07 22:17:35 $
$Revision: 2.0 $

TOC:
I.    OVERVIEW
II.   REQUIREMENTS
III.  FIRST STARTUP
IV.   ADMIN/GM's GUIDE
V.    PLAYER's GUIDE
VI.   UPDATING
VII.  TROUBLESHOOTING
VIII. LEGAL
IX.   CONTACT
X.    CONTRIBUTING
XI.   BUG REPORTING
XII.  CREDITS


====================
I. OVERVIEW
====================

L2J is an Alternative Lineage 2 Game Server written in pure Java for
best compatibility. L2J gives you the possibility to legally host a game
server for this popular Korean MMO created by NCSoft. It is still
unfinished and many features are missing, but L2J Dev team is working
hard on implementing them. L2J Server is distributed under the terms
of GNU/GPL in a hope that open source model is the best for
developing quality software giving everyone a possibility to
participate on development by submitting the code.


====================
II. REQUIREMENTS
====================

OS: 
Any OS having Java JDK 1.5 installed and properly configured!
    We recommend using SUN JDK available at java.sun.com

Hardware: 
Decent CPU & RAM

Software:
Java JDK 1.5
MySql
Datapack

====================
III. FIRST STARTUP
====================

Before you can start up the server please read the wiki sites.
They contain all the info you need to setup and run a L2J Server.

L2J Server wiki: Server Guides, How-to's, bugtracker:
https://opensvn.csie.org/traccgi/l2j/trac.cgi/wiki

L2J-Datapack wiki: Datapack Guides, How-to's, bugtracker:
https://opensvn.csie.org/traccgi/L2J_Datapack/trac.cgi/wiki

L2J Community wiki: Guides, How-to's:
http://l2j.jot.com

This Server distribution does not contain any spawn/drop data or any world
content. (Datapack)

L2J Server has also a possibility to change xp/sp/drop rates relative
to data in spawnlist/droplist files. To do that, just change the rates 
you need in server.properties file found in config folder.

You should also configure your IP address in server.properties. 

Server uses ports 2106 (LoginServer) and 7777 (GameServer) by
default. If your server runs behind NAT or firewall you will need to
open and/or forward these ports. 


====================
IV. ADMIN/GM's GUIDE
====================

To make someone an admin you need to edit, in the l2jdb database, the desired character in the 
characters table, while server is SHUT DOWN! Change the field, accesslevel from 0 to 100 or more. 
You may start server after that and the person will have admin privileges. 
Note that you must create the account and character before editing. 

Possible access levels:
-100 = banned
0 = normal account/character
51 or more = exempt from maximum connections limit (accounts table only)
100 or more = admin

More info can be found in the community wiki.
 
Admin commands implemented:
(Please see https://opensvn.csie.org/traccgi/l2j/trac.cgi/wiki/GmCommands for more complete list.)
//admin = main GM interface
//gmchat = will send a message to all online GMs
//invul = makes your character untouchable

====================
V. PLAYER's GUIDE
====================

CLIENT COMPATIBILITY
L2J v3.0.0 should be compatible with any Chronicle 3 client with revision 551
or higher. 

====================
VI. UPDATING
====================
It may arrive that you want to update your server to new version while
keeping old accounts. There are few steps you HAVE TO do in order to
keep the data accurate.

- You should ALWAYS look at timelines before updating, sometimes a file
  format may change, so you will need to edit data manually to fit with
  new format.
- You should ALWAYS Backup all MySql data.
- You should ALWAYS Backup all Server and Datapack files.
- back up all .properties files (don't forget to check if new server use same 
  format for those files)
- download & unzip new server code to the Server directory
- download & unzip new datapack code to the Server directory
- edit and run update in the tools folder
- run newly installed server & enjoy ;)


====================
VII. TROUBLESHOOTING
====================

PROBLEM
- Client outputs bunch of messages about missing templates.
SOLUTION
- Check that the datapack has been properly installed.

PROBLEM
- Message similar to "java is not recognized as internal command",
"java not found" or "unknown command: java" appears.
SOLUTION
- Install java, or, if java is already installed just add your java
binary directory to system PATH setting. If you don't know how to do
that, than DO NOT bother running your own server please.

PROBLEM
- I can log in but ping is 9999s and I can't get past Server Select.
SOLUTION
- Set up your IP's properly, forward/open good ports if accessing from
outside. (or find server with admin that knows how to do it)

PROBLEM
- Skills/quests/whatever don't work.
SOLUTION
- Patience brings it's fruits :p

PROBLEM
- I found a bug.
SOLUTION
- Please refer to BUG REPORTING section of this readme.

Further help available at the wiki sites and the forums.


====================
VIII. LEGAL
====================

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307,
USA.

Full GNU/GPL License is included in LICENSE.TXT file.

Whereas L2J is distributed under the terms of GNU/GPL, we will be
happy if you:
a) Preserve logon notice. This give us, L2J Developers, appropriate
credit for our hard work done during our free time without any
revenues.
 
b) Do not distribute any extended data files with the server files in
the same archive. NO world content should be incorporated in L2J
distribution.



====================
IX. CONTACT
====================

Web: http://l2j.sourceforge.net
IRC: #l2j @ Freenode (irc.freenode.net)

Please note that L2J Devs can't help players with connecting issues or
anything related to playing on private servers. If you can't connect,
you should contact your server GM's. We can solve only L2J server
~software~ related issues. We don't have any backdoors or anything
that would enable us GM accounts on every server using L2J, so there's
no point in coming to our channel if you need items/adena/whatever
ingame.


====================
X. CONTRIBUTING
====================

Anyone who want to contribute to project is encouraged to do so. Java
programming skills are not always required as L2J needs much more than
java code.

If you created anything that may be helpful use User Contributions
section on our forums. If you contributed good stuff that will be
accepted, you might be invited to join L2J Dev Team.

People willing to hang on chat and respond to user questions are also
ALWAYS welcome ;)


====================
XI. BUG REPORTING
====================

Bugs can be reported on our wiki site.
https://opensvn.csie.org/traccgi/l2j/trac.cgi/newticket
Basic rules for reporting are:
    Please report only one bug/issue per ticket!!
    You must include the revision (changeset) number when reporting a bug!
    "The latest" does not mean anything when 5 more updates have been done since you set up the server.
If you are not sure if it should be reported here, make a post about it in the L2J forum.

Players should ALWAYS consult bugs with their GM's before reporting it
on our wiki site. Some bugs may be caused by bad datapack, server
installation or modification server owner has made. We can't help you
in that case.

Please use the datapacks bugtracker for reporting datapack bugs.
Please do NOT report bugs related to unofficial add-ons to L2J. L2J
bugtracker is NOT a place to fix that. Contact the person who made
modification instead.


====================
XII. CREDITS
====================

Dev team: 

Project Managers:
L2Chef
Agelian/Luis Antonioa

Project Developers:
Active:
Drakin-Korin
Eloquence
Juokelis
Sh1ny
Galun
Lithium
Godson
Thorgrim
ootz0rz
Dalrond
Balancer
Mr/roberto28[kns]
Unleashed/L2Unleashed
NeXuS
TempyIncursion
MickyLee
Niedar
Wooden/WoodenGil

Inactive
Maximas/Mk2
Tenkawa ?
Whatev ?
Natiels ?
Warrax
Sarevok
Myrdos
Delicioso
Reich
v00d00
Undeady/Lilium

Project Forum Adminstrators:
Agelian/Luisantonioa
L2Chef
Drakin-Korin
Nuocnam

Project Forum Moderators:
Kadar
Madmalkav
Warrax
Juokelis
Eloquence

Have fun playing L2J ;)
Dev Team