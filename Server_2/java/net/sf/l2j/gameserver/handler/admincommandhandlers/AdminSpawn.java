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
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.NpcTable;
import net.sf.l2j.gameserver.SpawnTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * This class handles following admin commands: - show_spawns = shows menu -
 * spawn_index lvl = shows menu for monsters with respective level -
 * spawn_monster id = spawns monster id on target
 * 
 * @version $Revision: 1.2.2.5.2.5 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminSpawn implements IAdminCommandHandler
{

    private static String[] _adminCommands = { "admin_show_spawns", "admin_spawn", "admin_spawn_monster", "admin_spawn_index" };

    private static final int REQUIRED_LEVEL = Config.GM_NPC_EDIT;

    public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {
        if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM()))
            return false;

        if (command.equals("admin_show_spawns"))
        {
            AdminHelpPage.showHelpPage(activeChar, "spawns.htm");
        }
        else if (command.startsWith("admin_spawn_index"))
        {
            String val = command.substring(18);
            AdminHelpPage.showHelpPage(activeChar, "spawns/" + val + ".htm");
        }
        else if (command.startsWith("admin_spawn")
                || command.startsWith("admin_spawn_monster"))
        {
            StringTokenizer st = new StringTokenizer(command);
            try
            {
                st.nextToken();
                String id = st.nextToken();
                int respawnTime = 15;
                if (st.hasMoreTokens())
                    respawnTime = Integer.parseInt(st.nextToken());
                spawnMonster(activeChar, id, respawnTime);
            }
            catch (Exception e)
            {
                // Case of wrong monster data
            }
        }

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
    
    private void spawnMonster(L2PcInstance activeChar, String monsterId)
    {
        spawnMonster(activeChar, monsterId, 15);
    }

    private void spawnMonster(L2PcInstance activeChar, String monsterId, int respawnTime)
    {
        L2Object target = activeChar.getTarget();
        if (target == null)
        {
            target = activeChar;/*
            SystemMessage sm = new SystemMessage(614);
            sm.addString("Incorrect target.");
            activeChar.sendPacket(sm);
            return;*/
        }

        int monsterTemplate = Integer.parseInt(monsterId);

        L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(monsterTemplate);
        if (template1 == null)
        {
            //SystemMessage sm = new SystemMessage(614);
            //sm.addString("Incorrect monster template.");
            //activeChar.sendPacket(sm);
            //return;
            //template1 = NpcTable.getInstance().getTemplate(194);
            //template1.setNpcId(monsterTemplate);
        }

        try
        {
            //L2MonsterInstance mob = new L2MonsterInstance(template1);

            L2Spawn spawn = new L2Spawn(template1);
            spawn.setLocx(target.getX());
            spawn.setLocy(target.getY());
            spawn.setLocz(target.getZ());
            spawn.setRandomx(0);
            spawn.setRandomy(0);
            spawn.setAmount(1);
            spawn.setHeading(activeChar.getHeading());
            spawn.setRespawnDelay(respawnTime);
            SpawnTable.getInstance().addNewSpawn(spawn, true);
            spawn.init();

            SystemMessage sm = new SystemMessage(614);
            sm.addString("Created " + template1.name + " on " + target.getObjectId() + ".");
            activeChar.sendPacket(sm);
        }
        catch (Exception e)
        {
            SystemMessage sm = new SystemMessage(614);
            sm.addString("Target is not ingame.");
            activeChar.sendPacket(sm);
        }
    }
}
