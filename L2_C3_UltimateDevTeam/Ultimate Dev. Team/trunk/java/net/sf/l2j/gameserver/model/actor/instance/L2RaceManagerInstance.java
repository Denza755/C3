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
package net.sf.l2j.gameserver.model.actor.instance;

import javolution.util.FastList;
import net.sf.l2j.gameserver.ClientScheduler;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.RaceManagerKnownList;
import net.sf.l2j.gameserver.model.entity.MonsterRace;
import net.sf.l2j.gameserver.packets.serverpackets.*;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.util.Broadcast;

import java.util.List;

public class L2RaceManagerInstance extends L2NpcInstance
{
    public static final int LANES = 8;
    public static final int WINDOW_START = 0;
    
    @SuppressWarnings("unused")
    private static List<Race> history;
    private static List<L2RaceManagerInstance> managers;
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
    protected static int cost[] = {100,500,1000,5000,10000,20000,50000,100000};
    
    
    public L2RaceManagerInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
        super.setKnownList(new RaceManagerKnownList(new L2RaceManagerInstance[] {this}));
        if (notInitialized)
        {
            notInitialized = false;
            //*
            history = new FastList<Race>();
            managers = new FastList<L2RaceManagerInstance>();
            
            ClientScheduler s = ClientScheduler.getInstance();
            s.scheduleLowAtFixedRate(new Announcement(SystemMessage.MONSRACE_TICKETS_AVAILABLE_FOR_S1_RACE), 0, 10 * MINUTE);
            s.scheduleLowAtFixedRate(new Announcement(SystemMessage.MONSRACE_TICKETS_NOW_AVAILABLE_FOR_S1_RACE), 30 * SECOND, 10 * MINUTE);
            s.scheduleLowAtFixedRate(new Announcement(SystemMessage.MONSRACE_TICKETS_AVAILABLE_FOR_S1_RACE), MINUTE, 10 * MINUTE);
            s.scheduleLowAtFixedRate(new Announcement(SystemMessage.MONSRACE_TICKETS_NOW_AVAILABLE_FOR_S1_RACE), MINUTE + 30 * SECOND, 10 * MINUTE);
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

    public final RaceManagerKnownList getKnownList() { return (RaceManagerKnownList)super.getKnownList(); }
    
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
            case SystemMessage.MONSRACE_TICKETS_AVAILABLE_FOR_S1_RACE:
            case SystemMessage.MONSRACE_TICKETS_NOW_AVAILABLE_FOR_S1_RACE:
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
                sm.addNumber(MonsterRace.getInstance().getFirstPlace());
                sm.addNumber(MonsterRace.getInstance().getSecondPlace());
                break;
        }
        broadcast(sm);
       
        if (type == SystemMessage.MONSRACE_RACE_START)
        {
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
                Broadcast.toKnownPlayers(manager, pkt);
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
            broadcast(new PlaySound(1,"S_Race",0,0,0,0,0));
            broadcast(new PlaySound(0,"ItemSound2.race_start",1,121209259,12125,182487,-3559));
            packet = new MonRaceInfo(codes[1][0], codes[1][1], race.getMonsters(), race.getSpeeds());
            sendMonsterInfo();
            
            ClientScheduler.getInstance().scheduleMed(new RunRace(), 5000);
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
        if (command.startsWith("BuyTicket") && state != ACCEPTING_BETS){
            player.sendPacket(new SystemMessage(SystemMessage.MONSRACE_TICKETS_NOT_AVAILABLE));
            command="Chat 0";
        }
        if (command.startsWith("ShowOdds") && state == ACCEPTING_BETS){
            player.sendPacket(new SystemMessage(SystemMessage.MONSRACE_NO_PAYOUT_INFO));
            command="Chat 0";
        }
        
        if (command.startsWith("BuyTicket"))
        {
            int val = Integer.parseInt(command.substring(10));
            if (val == 0){
                player.setRace(0,0);player.setRace(1,0);
            }
            if ((val == 10 && player.getRace(0) == 0) ||
                    (val == 20 && player.getRace(0) == 0 && player.getRace(1) == 0))
                val = 0;
            showBuyTicket(player,val);
        }
        else if (command.equals("ShowOdds"))
            showOdds(player);
        else if (command.equals("ShowInfo"))
            showMonsterInfo(player);
        else if (command.equals("calculateWin"))
        {
            //displayCalculateWinnings(player);
        }
        else if (command.equals("viewHistory"))
        {
            //displayHistory(player);
        }
        else 
        {
            getKnownList().removeKnownObject(player);
            super.onBypassFeedback(player, command);
            return;
        }
    }
    public void showOdds(L2PcInstance player){
        if (state == ACCEPTING_BETS)
            return;
        int npcId = getTemplate().npcId;
        String filename, search;
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        filename = getHtmlPath(npcId,5);
        html.setFile(filename);
        for (int i=0; i<8; i++)
        {
            int n=i+1;
            search = "Mob"+n;
            html.replace(search,MonsterRace.getInstance().getMonsters()[i].getTemplate().name);
        }
        html.replace("1race",String.valueOf(raceNumber));
        html.replace("%objectId%",String.valueOf(getObjectId()));
        player.sendPacket(html);
        player.sendPacket( new ActionFailed());
    }
    public void showMonsterInfo(L2PcInstance player){
        int npcId = getTemplate().npcId;
        String filename, search;
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        filename = getHtmlPath(npcId,6);
        html.setFile(filename);
        for (int i=0; i<8; i++)
        {
            int n=i+1;
            search = "Mob"+n;
            html.replace(search,MonsterRace.getInstance().getMonsters()[i].getTemplate().name);
        }
        html.replace("%objectId%",String.valueOf(getObjectId()));
        player.sendPacket(html);
        player.sendPacket( new ActionFailed());
    }
    public void showBuyTicket(L2PcInstance player, int val){
        if (state != ACCEPTING_BETS)
            return;
        int npcId = getTemplate().npcId;
        String filename, search, replace;
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        if (val < 10){
            filename = getHtmlPath(npcId,2);
            html.setFile(filename);
            for (int i=0; i<8; i++)
            {
                int n=i+1;
                search = "Mob"+n;
                html.replace(search,MonsterRace.getInstance().getMonsters()[i].getTemplate().name);
            }
            search = "No1";
            if (val == 0)
                html.replace(search,"");
            else {
                html.replace(search,""+val);
                player.setRace(0,val);
            }
        } else if (val < 20){
            if (player.getRace(0)==0)
                return;
            filename = getHtmlPath(npcId,3);
            html.setFile(filename);
            html.replace("0place",""+player.getRace(0));
            search = "Mob1";
            replace = MonsterRace.getInstance().getMonsters()[player.getRace(0)-1].getTemplate().name;
            html.replace(search,replace);
            search = "0adena";
            if (val == 10)
                html.replace(search,"");
            else {
                html.replace(search,""+cost[val-11]);
                player.setRace(1,val-10);
            }
        } else if (val == 20) {
            if (player.getRace(0)==0 || player.getRace(1)==0)
                return;
            filename = getHtmlPath(npcId,4);
            html.setFile(filename);
            html.replace("0place",""+player.getRace(0));
            search = "Mob1";
            replace = MonsterRace.getInstance().getMonsters()[player.getRace(0)-1].getTemplate().name;
            html.replace(search,replace);
            search = "0adena";
            int price = cost[player.getRace(1)-1];
            html.replace(search,""+price);
            search = "0tax";
            int tax = 0;
            html.replace(search,""+tax);
            search = "0total";
            int total = price+tax;
            html.replace(search,""+total);
        } else {
            if (player.getRace(0)==0 || player.getRace(1)==0)
                return;
            if (player.getAdena() < cost[player.getRace(1)-1]){
                player.sendPacket(new SystemMessage(SystemMessage.YOU_NOT_ENOUGH_ADENA));
                return;
            }
            int ticket = player.getRace(0);
            int priceId = player.getRace(1);
            player.setRace(0,0);player.setRace(1,0);
            player.reduceAdena(cost[priceId-1]);
            player.sendPacket(new SystemMessage(SystemMessage.DISSAPEARED_ADENA).addNumber(cost[priceId-1]));
            player.sendPacket(new SystemMessage(SystemMessage.ACQUIRED).addNumber(raceNumber).addItemName(4443));
            L2ItemInstance item = new L2ItemInstance(IdFactory.getInstance().getNextId(),4443);
            item.setCount(1);
            item.setEnchantLevel(raceNumber);
            item.setCustomType1(ticket);
            item.setCustomType2(cost[priceId-1]/100);
            player.getInventory().addItem(item);
            InventoryUpdate iu = new InventoryUpdate();
            iu.addItem(item);
            iu.addModifiedItem(player.getInventory().findItemByItemId(57));
            player.sendPacket(iu);
            return;
        }
        html.replace("1race",String.valueOf(raceNumber));
        html.replace("%objectId%",String.valueOf(getObjectId()));
        player.sendPacket(html);
        player.sendPacket( new ActionFailed());
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
	    raceNumber++;

            //DeleteObject obj = null;
            for (int i=0; i<8; i++)
            {
                broadcast(new DeleteObject(MonsterRace.getInstance().getMonsters()[i]));
            }
        }
    }
    
}
