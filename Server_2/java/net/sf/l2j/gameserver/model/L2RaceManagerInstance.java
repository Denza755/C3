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
package net.sf.l2j.gameserver.model;

import java.util.ArrayList;

import net.sf.l2j.gameserver.ClientScheduler;
import net.sf.l2j.gameserver.MonsterRace;
import net.sf.l2j.gameserver.serverpackets.DeleteObject;
import net.sf.l2j.gameserver.serverpackets.MonRaceInfo;
import net.sf.l2j.gameserver.serverpackets.PlaySound;
import net.sf.l2j.gameserver.serverpackets.ServerBasePacket;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2RaceManagerInstance extends L2FolkInstance
{
    public static final int LANES = 8;
    public static final int WINDOW_START = 0;
    
    private static ArrayList<Race> history;
    private static ArrayList<L2RaceManagerInstance> managers;
    private static int raceNumber = 4;
    
    //Time Constants
    private final static long SECOND = 1000;
    private final static long MINUTE = 60 * SECOND;
    
    private static int minutes = 5;
    
    //States
    private static final int ACCEPTING_BETS = 0;
    private static final int WAITING = 1;
    private static final int STARTING_RACE = 2;
    private static final int RACE_END = 3;
    private static int state = RACE_END;
    
    protected static final int[][] codes = { {-1, 0}, {0, 15322}, {13765, -1} };
    private static boolean notInitialized = true;
    protected static MonRaceInfo packet;

    public L2RaceManagerInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
        if (notInitialized)
        {
            notInitialized = false;
            //*
            history = new ArrayList<Race>();
            managers = new ArrayList<L2RaceManagerInstance>();
            
            ClientScheduler s = ClientScheduler.getInstance();
            s.scheduleLowAtFixedRate(new Announcement(SystemMessage.MONSRACE_TICKETS_AVALIABLE_FOR_S1_RACE), 0, 10 * MINUTE);
            s.scheduleLowAtFixedRate(new Announcement(SystemMessage.MONSRACE_TICKETS_NOW_AVALIABLE_FOR_S1_RACE), 30 * SECOND, 10 * MINUTE);
            s.scheduleLowAtFixedRate(new Announcement(SystemMessage.MONSRACE_TICKETS_AVALIABLE_FOR_S1_RACE), MINUTE, 10 * MINUTE);
            s.scheduleLowAtFixedRate(new Announcement(SystemMessage.MONSRACE_TICKETS_NOW_AVALIABLE_FOR_S1_RACE), MINUTE + 30 * SECOND, 10 * MINUTE);
            s.scheduleLowAtFixedRate(new Announcement(SystemMessage.MONSRACE_TICKETS_STOP_IN_S1_MINUTES), 2 * MINUTE, 10 * MINUTE);
            s.scheduleLowAtFixedRate(new Announcement(SystemMessage.MONSRACE_TICKETS_STOP_IN_S1_MINUTES), 3 * MINUTE, 10 * MINUTE);
            s.scheduleLowAtFixedRate(new Announcement(SystemMessage.MONSRACE_TICKETS_STOP_IN_S1_MINUTES), 4 * MINUTE, 10 * MINUTE);
            s.scheduleLowAtFixedRate(new Announcement(SystemMessage.MONSRACE_TICKETS_STOP_IN_S1_MINUTES), 5 * MINUTE, 10 * MINUTE);
            s.scheduleLowAtFixedRate(new Announcement(SystemMessage.MONSRACE_TICKETS_STOP_IN_S1_MINUTES), 6 * MINUTE, 10 * MINUTE);
            s.scheduleLowAtFixedRate(new Announcement(SystemMessage.MONSRACE_TICKET_SALES_CLOSED), 7 * MINUTE, 10 * MINUTE);
            s.scheduleLowAtFixedRate(new Announcement(SystemMessage.MONSRACE_BEGINS_IN_S1_MINUTES), 7 * MINUTE, 10 * MINUTE);
            s.scheduleLowAtFixedRate(new Announcement(SystemMessage.MONSRACE_BEGINS_IN_S1_MINUTES), 8 * MINUTE, 10 * MINUTE);
            s.scheduleLowAtFixedRate(new Announcement(SystemMessage.MONSRACE_BEGINS_IN_30_SECONDS), 8 * MINUTE + 30 * SECOND, 10 * MINUTE);
            s.scheduleLowAtFixedRate(new Announcement(SystemMessage.MONSRACE_COUNTDOWN_IN_FIVE_SECONDS), 8 * MINUTE + 50 * SECOND, 10 * MINUTE);
            s.scheduleLowAtFixedRate(new Announcement(SystemMessage.MONSRACE_BEGINS_IN_S1_SECONDS), 8 * MINUTE + 55 * SECOND, 10 * MINUTE);
            s.scheduleLowAtFixedRate(new Announcement(SystemMessage.MONSRACE_BEGINS_IN_S1_SECONDS), 8 * MINUTE + 56 * SECOND, 10 * MINUTE);
            s.scheduleLowAtFixedRate(new Announcement(SystemMessage.MONSRACE_BEGINS_IN_S1_SECONDS), 8 * MINUTE + 57 * SECOND, 10 * MINUTE);
            s.scheduleLowAtFixedRate(new Announcement(SystemMessage.MONSRACE_BEGINS_IN_S1_SECONDS), 8 * MINUTE + 58 * SECOND, 10 * MINUTE);
            s.scheduleLowAtFixedRate(new Announcement(SystemMessage.MONSRACE_BEGINS_IN_S1_SECONDS), 8 * MINUTE + 59 * SECOND, 10 * MINUTE);
            s.scheduleLowAtFixedRate(new Announcement(SystemMessage.MONSRACE_RACE_START), 9 * MINUTE, 10 * MINUTE);
            //*/
        }
        managers.add(this);
    }
    
    public boolean addKnownObject(L2Object object, L2Character dropper)
    {
		boolean ret = super.addKnownObject(object, dropper);
		if (!ret)
			return false;

		if (object instanceof L2PcInstance) {
            if (packet != null)
				((L2PcInstance) object).sendPacket(packet);
        }
		return true;
    }
    
    public void removeKnownObject(L2Object object)
    {
        super.removeKnownObject(object);
        if (object instanceof L2PcInstance)
        {
            //System.out.println("Sending delete monsrac info.");
            DeleteObject obj = null;
            for (int i=0; i<8; i++)
            {
                obj = new DeleteObject(MonsterRace.getInstance().getMonsters()[i]);
                ((L2PcInstance)object).sendPacket(obj);
            }
        }
    }

    class Announcement implements Runnable
    {
        private int type;
        public Announcement(int type) {this.type = type;}
        public void run()
        {
            makeAnnouncement(type);
        }
    }
    
    public void makeAnnouncement(int type)
    {
        SystemMessage sm = new SystemMessage(type);
        switch (type)
        {
        case SystemMessage.MONSRACE_TICKETS_AVALIABLE_FOR_S1_RACE:
        case SystemMessage.MONSRACE_TICKETS_NOW_AVALIABLE_FOR_S1_RACE:
            if (state != ACCEPTING_BETS)
            {//System.out.println("Race Initializing");
                state = ACCEPTING_BETS;
                startRace();
            }//else{System.out.println("Race open");}
            sm.addNumber(raceNumber);
            break;
        case SystemMessage.MONSRACE_TICKETS_STOP_IN_S1_MINUTES:
        case SystemMessage.MONSRACE_BEGINS_IN_S1_MINUTES:
        case SystemMessage.MONSRACE_BEGINS_IN_S1_SECONDS:
            sm.addNumber(minutes);
            sm.addNumber(raceNumber);
            minutes--;
            break;
        case SystemMessage.MONSRACE_TICKET_SALES_CLOSED:
            //System.out.println("Sales closed");
            sm.addNumber(raceNumber);
            state = WAITING;
            minutes = 2;
            break;
        case SystemMessage.MONSRACE_COUNTDOWN_IN_FIVE_SECONDS:
        case SystemMessage.MONSRACE_RACE_END:
            sm.addNumber(raceNumber);
            minutes = 5;
            break;
        case SystemMessage.MONSRACE_FIRST_PLACE_S1_SECOND_S2:
            //System.out.println("Placing");
            state = RACE_END;
            raceNumber++;
            sm.addNumber(MonsterRace.getInstance().getFirstPlace());
            sm.addNumber(MonsterRace.getInstance().getSecondPlace());
            break;
        }
        //System.out.println("Counter: "+minutes);
        //System.out.println("State: "+state);
        broadcast(sm);
        //System.out.println("Player's known: "+getKnownPlayers().size());
        
        if (type == SystemMessage.MONSRACE_RACE_START)
        {
            //System.out.println("Starting race");
            state = STARTING_RACE;
            startRace();
            minutes = 5;
        }
    }
    
    protected void broadcast(ServerBasePacket pkt)
    {
        for (L2RaceManagerInstance manager : managers)
        {
            if (!manager.isDead())
                manager.broadcastPacket(pkt);
        }
    }
    
    public void sendMonsterInfo()
    {
        broadcast(packet);
    }

    private void startRace()
    {
        MonsterRace race = MonsterRace.getInstance();
        if (state == STARTING_RACE)
        {
            //state++;
            PlaySound SRace = new PlaySound(1,"S_Race",0,0,0,0,0);
            broadcast(SRace);
            PlaySound SRace2 = new PlaySound(0,"ItemSound2.race_start",1,121209259,12125,182487,-3559);
            broadcast(SRace2);
            packet = new MonRaceInfo(codes[1][0], codes[1][1], race.getMonsters(), race.getSpeeds());
            sendMonsterInfo();
            
            ClientScheduler.getInstance().scheduleMed(new RunRace(codes), 5000);
        }
        else
        {
            //state++;
            race.newRace();
            race.newSpeeds();
            packet = new MonRaceInfo(codes[0][0], codes[0][1], race.getMonsters(), race.getSpeeds());
            sendMonsterInfo();
        }
        
    }

    public void onBypassFeedback(L2PcInstance player, String command)
    {
        System.out.println("Command: "+command);
        
        String content = "";
        
        if (command.equals("viewOddsWin"))
        {
            //displayOdds(player);
        }
        else if (command.equals("viewOddsPlace"))
        {
            //displayOddsPlace(player);
        }
        else if (command.equals("viewMonsterInfo"))
        {
            //displayMonsterInfo(player);
        }
        else if (command.equals("buyTicketWin"))
        {
            //displayBuyTicketWin(player);
        }
        else if (command.equals("buyTicketPlace"))
        {
            //displayerBuyTicketPLace(player);
        }
        else if (command.equals("calculateWin"))
        {
            //displayCalculateWinnings(player);
        }
        else if (command.equals("viewHistory"))
        {
            //displayHostory(player);
        }
        else 
        {
            super.onBypassFeedback(player, command);
            return;
        }
        
        insertObjectIdAndShowChatWindow(player, content);
    }
    
    public void showChatWindow(L2PcInstance player, int val)
    {
        String content = "<html>"+
        "<body>"+
        "<center>MonsterRace Manager</center>"+
        "<img src=\"L2UI.SquareWhite\" width=270 height=1>"+
        "<img src=\"L2UI.SquareBlank\" width=1 height=3>"+
        "<table width=270 borader=0>"+
        "<tr>"+
        "<td width=270><a action=\"link mr_help.html\">Monster Race Help</a></td></tr>"+
        "<tr><td width=270><a action=\"bypass -h npc_%objectId%_viewOddsWin\">View Odds - Win</a></td></tr>"+
        "<tr><td width=270><a action=\"bypass -h npc_%objectId%_viewOddsPlace\">View Odds - Place</a></td></tr>"+
        "<tr><td width=270><a action=\"bypass -h npc_%objectId%_viewMonsterInfo\">View Monster Information</a></td></tr>"+
        "<tr><td width=270><a action=\"bypass -h npc_%objectId%_buyTicketWin\">Purchase Ticket- Win </a></td></tr>"+
        "<tr><td width=270><a action=\"bypass -h npc_%objectId%_buyTicketPlace\">Purchase Ticket- Place</a></td></tr>"+
        "<tr><td width=270><a action=\"bypass -h npc_%objectId%_calculateWin\">Calculate Winnings</a></td></tr>"+
        "<tr><td width=270><a action=\"bypass -h npc_%objectId%_viewHistory\">View Past Results</a></td></tr>"+
        "</table>"+
        "<img src=\"L2UI.SquareBlank\" width=270 height=10>"+
        "<img src=\"L2UI.SquareWhite\" width=270 height=1>"+
        "<img src=\"L2UI.SquareBlank\" width=270 height=5>"+
        "</body>"+
        "</html>";
        
        insertObjectIdAndShowChatWindow(player, content);
    }
        
    
    public class Race
    {
        private Info[] info;
        public Race(Info[] info)
        {
            this.info = info;
        }
        
        public Info getLaneInfo(int lane)
        {
            return info[lane];
        }
        
        public class Info
        {
            private int id;
            private int place;
            private int odds;
            private int payout;
            
            public Info(int id, int place, int odds, int payout)
            {
                this.id = id;
                this.place = place;
                this.odds = odds;
                this.payout = payout;
            }
            
            public int getId() {return id;}
            public int getOdds() {return odds;}
            public int getPayout() {return payout;}
            public int getPlace() {return place;}
        }
        
    }
    
    class RunRace implements Runnable
    {
        
        //private int[][] codes;

        public RunRace(int[][] codes)
        {
            //this.codes = codes;
        }
        
        public void run()
        {
            packet = new MonRaceInfo(codes[2][0], codes[2][1], MonsterRace.getInstance().getMonsters(), MonsterRace.getInstance().getSpeeds());
            sendMonsterInfo();
            ClientScheduler.getInstance().scheduleMed(new RunEnd(), 30000);
        }
    }
    
    class RunEnd implements Runnable
    {
        public void run()
        {
            makeAnnouncement(SystemMessage.MONSRACE_FIRST_PLACE_S1_SECOND_S2);
            makeAnnouncement(SystemMessage.MONSRACE_RACE_END);
            DeleteObject obj = null;
            for (int i=0; i<8; i++)
            {
                obj = new DeleteObject(MonsterRace.getInstance().getMonsters()[i]);
                broadcast(obj);
            }
        }
    }

}
