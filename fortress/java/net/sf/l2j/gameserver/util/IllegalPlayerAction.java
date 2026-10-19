/*
 * $Header: IllegalPlayerAction.java, 21/10/2005 23:32:02 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 21/10/2005 23:32:02 $
 * $Revision: 1 $
 * $Log: IllegalPlayerAction.java,v $
 * Revision 1  21/10/2005 23:32:02  luisantonioa
 * Added copyright notice
 *
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.util;

import java.util.logging.Logger;

import net.sf.l2j.gameserver.GmListTable;
import net.sf.l2j.gameserver.model.L2PcInstance;

/**
 * This class ...
 * 
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */
public final class IllegalPlayerAction implements Runnable
{
    String message;
    Logger log;
    int punishment;
    L2PcInstance actor;
    
    public static final int PUNISH_BROADCAST = 1;
    public static final int PUNISH_KICK = 2;
    public static final int PUNISH_KICKBAN = 3;
    
    public IllegalPlayerAction(L2PcInstance actor, String message, Logger log, int punishment)
    {
        this.message        = message;
        this.log            = log;
        this.punishment     = punishment;
        this.actor          = actor;
    }
    
    public void run()
    {
        log.warning(message);
        GmListTable.broadcastMessageToGMs(message);

        switch(punishment)
        {
            case PUNISH_BROADCAST:
                return;
            case PUNISH_KICK:
                actor.sendMessage("You are kicked for illegal action, GM informed");
                actor.closeNetConnection();
                break;
            case PUNISH_KICKBAN:
                actor.sendMessage("You are banned for illegal action, GM informed");
                actor.setAccessLevel(-100);
                actor.setAccountAccesslevel(-100);
                actor.closeNetConnection();
        }
    }
}
