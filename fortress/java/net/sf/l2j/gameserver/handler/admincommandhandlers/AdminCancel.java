/*
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
 * [URL]http://www.gnu.org/copyleft/gpl.html[/URL]
 */
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.serverpackets.Revive;
import net.sf.l2j.gameserver.serverpackets.SocialAction;

/**
 * This class handles following admin commands:
 * - cancel = removes all buffs and effects from target L2Character
 * 
 * @version $Revision$ $Date$
 */

public class AdminCancel implements IAdminCommandHandler
{
  private static Logger _log = Logger.getLogger(AdminCancel.class.getName());
  private static String[] _adminCommands = {"admin_cancel"};
  private static final int REQUIRED_LEVEL = Config.GM_CHAR_EDIT;

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) return false;
    if (command.startsWith("admin_cancel ")) handleCancel(activeChar, command.split(" ")[1]);
    if (command.equals("admin_cancel")) handleCancel(activeChar);
    return true;
  }

  public String[] getAdminCommandList()
  {
    return _adminCommands;
  }

  private boolean checkLevel(int level)
  {
    return (level >= REQUIRED_LEVEL);
  }

  private void handleCancel(L2PcInstance activeChar)
  {
    handleCancel(activeChar, null);
  }

  private void handleCancel(L2PcInstance activeChar, String player)
  {
    L2Object obj = activeChar.getTarget();
    if (player != null)
    {
      L2PcInstance plyr = L2World.getPlayer(player);
      if (plyr != null)
      {
        obj = plyr;
      }
      else
      {
        try
        {
          int radius  = Math.max(Integer.parseInt(player), 100);
          for (L2Object object : activeChar.getKnownObjects(radius))
          {
            if (object instanceof L2Character)
            {
              L2Character character = (L2Character) object;
              L2Effect[] effects = character.getAllEffects();

              for (L2Effect e : effects)
              {
                e.exit();
              }

              character.broadcastPacket(new SocialAction(character.getObjectId(), 15)); 
              character.broadcastPacket(new Revive(character));
            }
          }
          activeChar.sendMessage("Apply Cancel within " + radius + " unit radius.");
          return;
        }
        catch(NumberFormatException e)
        {
          activeChar.sendMessage("Enter valid player name or radius");
          return;
        }
      }
    }

    if (obj == null) obj = activeChar;
    if ((obj != null) && (obj instanceof L2Character))
    {
      L2Character target = (L2Character)obj;
      L2Effect[] effects = target.getAllEffects();

      for (L2Effect e : effects)
      {
        e.exit();
      }

      target.broadcastPacket(new SocialAction(target.getObjectId(), 15)); 
      target.broadcastPacket(new Revive(target));
      if (Config.DEBUG) 
      _log.fine("GM: "+activeChar.getName()+"("+activeChar.getObjectId()+") uses cancel on character "+target.getObjectId());
    } 
    else 
    {
      activeChar.sendMessage("Incorrect target.");
    }
  }
}
