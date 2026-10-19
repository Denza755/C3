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
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientScheduler;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.QuestManager;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.*;
import net.sf.l2j.gameserver.model.actor.knownlist.NpcKnownList;
import net.sf.l2j.gameserver.model.actor.stat.NpcStat;
import net.sf.l2j.gameserver.model.actor.status.NpcStatus;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.L2Event;
import net.sf.l2j.gameserver.model.entity.SevenSigns;
import net.sf.l2j.gameserver.model.entity.SevenSignsFestival;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.packets.serverpackets.*;
import net.sf.l2j.gameserver.tables.ItemTable;
import net.sf.l2j.gameserver.tables.NpcTable;
import net.sf.l2j.gameserver.tables.SkillTable;
import net.sf.l2j.gameserver.tables.SpawnTable;
import net.sf.l2j.gameserver.taskmanager.DecayTaskManager;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.L2Weapon;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents a Non-Player-Character in the world. It can be a monster or a friendly character.
 * It also uses a template to fetch some static values. The templates are hardcoded in the client, so we can rely on them.<BR><BR>
 * 
 * L2Character :<BR><BR>
 * <li>L2Attackable</li>
 * <li>L2BoxInstance</li>
 * <li>L2FolkInstance</li>
 * 
 * @version $Revision: 1.32.2.7.2.24 $ $Date: 2005/04/11 10:06:09 $
 */
public class L2NpcInstance extends L2Character
{
    private static Logger _log = Logger.getLogger(L2NpcInstance.class.getName());
    
    /** The interaction distance of the L2NpcInstance(is used as offset in MovetoLocation method) */
    public static final int INTERACTION_DISTANCE = 150;
    
    /** The random number of the L2NpcInstance */
    private final Random _rnd = new Random();
    
    /** The L2Spawn object that manage this L2NpcInstance */
    private L2Spawn _spawn;
    
    /** The DecayTask created when this L2NpcInstance dies */
    private Future _decayTask;
    
    /** The flag to specify if this L2NpcInstance is busy */
    private boolean _IsBusy = false;
    
    /** The busy message for this L2NpcInstance */
    private String _BusyMessage = "";
    
    /** True if a Dwarf has used Spoil on this L2NpcInstance */
    private boolean _IsSpoil;
    
    /** The castle index in the array of L2Castle this L2NpcInstance belongs to */
    private int _CastleIndex = -2;
    
    public boolean isEventMob = false;
    
    /** Task lauching the function onRandomAnimation() */
    public class RandomAnimationTask implements Runnable
    {
        public void run()
        {
            try
            {
                onRandomAnimation();
            } 
            catch (Throwable t) 
            {
            }
        }
    }
    public class destroyTemporalNPC implements Runnable
    {
        L2Spawn spawn;
        public destroyTemporalNPC(L2Spawn spawn){
            this.spawn = spawn;
            
        }
        public void run()
        {
            try
            {
                spawn.getLastSpawn().deleteMe();
                if (spawn != null)
                {
                    spawn.stopRespawn();
                    SpawnTable.getInstance().deleteSpawn(spawn, false);
                }
            } 
            catch (Throwable t) 
            {t.printStackTrace();
            }
        }
    }
    
    public class destroyTemporalSummon implements Runnable
    {
        L2Summon summon;
        L2PcInstance player;
        public destroyTemporalSummon(L2Summon summon, L2PcInstance player){
            this.summon = summon;
            this.player = player;
        }
        public void run()
        {
            summon.unSummon(player);
        }
    }
    
    /**
     * Constructor of L2NpcInstance (use L2Character constructor).<BR><BR>
     *  
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Call the L2Character constructor to set the _template of the L2Character (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR)  </li>
     * <li>Set the name of the L2Character</li>
     * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it </li><BR><BR>
     * 
     * @param objectId Identifier of the object to initialized
     * @param template The L2NpcTemplate to apply to the NPC
     * 
     */
    public L2NpcInstance(int objectId, L2NpcTemplate template)
    {
        // Call the L2Character constructor to set the _template of the L2Character, copy skills from template to object 
        // and link _calculators to NPC_STD_CALCULATOR
        super(objectId, template);
        super.setKnownList(new NpcKnownList(new L2NpcInstance[] {this}));
        super.setStat(new NpcStat(new L2NpcInstance[] {this}));
        super.setStatus(new NpcStatus(new L2NpcInstance[] {this}));
        
        if (template == null)
        {
            _log.severe("No template for Npc. Please check your datapack is setup correctly.");
            return;
        }
        
        // Set the name of the L2Character
        setName(template.name);
        
        // Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it 
        if (hasRandomAnimation())
            startRandomAnimation();
    }

    public NpcKnownList getKnownList() { return (NpcKnownList)super.getKnownList(); }
    public NpcStat getStat() { return (NpcStat)super.getStat(); }
    public NpcStatus getStatus() { return (NpcStatus)super.getStatus(); }
    
    /**
     * Check if the server allow Random Animation.<BR><BR>
     */
    public boolean hasRandomAnimation()
    {
        return (Config.MAX_NPC_ANIMATION > 0);
    }
    
    /** Return the L2NpcTemplate of the L2NpcInstance. */
    public final L2NpcTemplate getTemplate() { return (L2NpcTemplate)super.getTemplate(); }
    
    /**
     * Return the generic Identifier of this L2NpcInstance contained in the L2NpcTemplate.<BR><BR>
     */
    public int getNpcId()
    {
        return getTemplate().npcId;
    }
    
    public boolean isAttackable(){
	if (Config.ALLOW_KILL_ALL_NPC)
        return true;
	else
        return false;
    }
    
    /**
     * Return the faction Identifier of this L2NpcInstance contained in the L2NpcTemplate.<BR><BR>
     * 
     * <B><U> Concept</U> :</B><BR><BR>
     * If a NPC belows to a Faction, other NPC of the faction inside the Faction range will help it if it's attacked<BR><BR>
     * 
     */
    public final String getFactionId()
    {
        return getTemplate().factionId;
    }
    
    /**
     * Return the Level of this L2NpcInstance contained in the L2NpcTemplate.<BR><BR>
     */
    public final int getLevel() 
    {
        return getTemplate().level;
    }
    
    /**
     * Return True if the L2NpcInstance is agressive (ex : L2MonsterInstance in function of aggroRange).<BR><BR>
     */
    public boolean isAggressive()
    {
        return false;
    }
    
    /**
     * Return the Aggro Range of this L2NpcInstance contained in the L2NpcTemplate.<BR><BR>
     */
    public int getAggroRange()
    {
        return getTemplate().aggroRange;
    }
    
    /**
     * Return the Faction Range of this L2NpcInstance contained in the L2NpcTemplate.<BR><BR>
     */
    public int getFactionRange()
    {
        return getTemplate().factionRange;
    }
    
    /**
     * Return True if this L2NpcInstance is undead in function of the L2NpcTemplate.<BR><BR>
     */
    public boolean isUndead()
    {
        return getTemplate().isUndead;
    }
    
    /**
     * Send a packet NpcInfo with state of abnormal effect to all L2PcInstance in the _KnownPlayers of the L2NpcInstance.<BR><BR>
     */
    public void updateAbnormalEffect()
    {
        // Send a Server->Client packet NpcInfo with state of abnormal effect to all L2PcInstance in the _KnownPlayers of the L2NpcInstance
        for (L2PcInstance player : getKnownList().getKnownPlayers())
            player.sendPacket(new NpcInfo(this, player));
    }
    
    /**
     * Return the distance under which the object must be add to _knownObject in function of the object type.<BR><BR>
     *   
     * <B><U> Values </U> :</B><BR><BR>
     * <li> object is a L2FolkInstance : 0 (don't remember it) </li>
     * <li> object is a L2Character : 0 (don't remember it) </li>
     * <li> object is a L2PlayableInstance : 1500 </li>
     * <li> others : 500 </li><BR><BR> 
     * 
     * <B><U> Overriden in </U> :</B><BR><BR>
     * <li> L2Attackable</li><BR><BR> 
     * 
     * @param object The Object to add to _knownObject
     * 
     */
    public int getDistanceToWatchObject(L2Object object)
    {
        if (object instanceof L2FestivalGuideInstance)
            return 10000;
        
        if (object instanceof L2FolkInstance || !(object instanceof L2Character))
            return 0;
        
        if (object instanceof L2PlayableInstance) 
            return 1500;
        
        return 500;
    }
    
    /**
     * Return the distance after which the object must be remove from _knownObject in function of the object type.<BR><BR>
     *   
     * <B><U> Values </U> :</B><BR><BR>
     * <li> object is not a L2Character : 0 (don't remember it) </li>
     * <li> object is a L2FolkInstance : 0 (don't remember it)</li>
     * <li> object is a L2PlayableInstance : 3000 </li>
     * <li> others : 1000 </li><BR><BR>
     * 
     * <B><U> Overriden in </U> :</B><BR><BR>
     * <li> L2Attackable</li><BR><BR> 
     * 
     * @param object The Object to remove from _knownObject
     * 
     */
    public int getDistanceToForgetObject(L2Object object)
    {
        return 2*getDistanceToWatchObject(object);
    }
    
    /**
     * Return False.<BR><BR>
     *   
     * <B><U> Overriden in </U> :</B><BR><BR>
     * <li> L2MonsterInstance : Check if the attacker is not another L2MonsterInstance</li>
     * <li> L2PcInstance</li><BR><BR>
     */
    public boolean isAutoAttackable(@SuppressWarnings("unused") L2Character attacker) 
    {
        return false;
    }
    
    /**
     * Return the Identifier of the item in the left hand of this L2NpcInstance contained in the L2NpcTemplate.<BR><BR>
     */
    public int getLeftHandItem()
    {
        return getTemplate().lhand;
    }
    
    /**
     * Return the Identifier of the item in the right hand of this L2NpcInstance contained in the L2NpcTemplate.<BR><BR>
     */
    public int getRightHandItem()
    {
        return getTemplate().rhand;
    }
    
    /**
     * Return True if this L2NpcInstance has drops that can be sweeped.<BR><BR>
     */
    public boolean isSpoil() 
    {
        return _IsSpoil;
    }
    
    /**
     * Set the spoil state of this L2NpcInstance.<BR><BR>
     */
    public void setSpoil(boolean isSpoil) 
    {
        _IsSpoil = isSpoil;
    }
    
    /**
     * Return the busy status of this L2NpcInstance.<BR><BR>
     */
    public final boolean isBusy()
    {
        return _IsBusy;
    }
    
    /**
     * Set the busy status of this L2NpcInstance.<BR><BR>
     */
    public void setBusy(boolean isBusy)
    {
        _IsBusy = isBusy;
    }
    
    /**
     * Return the busy message of this L2NpcInstance.<BR><BR>
     */
    public final String getBusyMessage()
    {
        return _BusyMessage;
    }
    
    /**
     * Set the busy message of this L2NpcInstance.<BR><BR>
     */
    public void setBusyMessage(String message)
    {
        _BusyMessage = message;
    }
    
    /**
     * Manage actions when a player click on the L2NpcInstance.<BR><BR>
     * 
     * <B><U> Actions on first click on the L2NpcInstance (Select it)</U> :</B><BR><BR>
     * <li>Set the L2NpcInstance as target of the L2PcInstance player (if necessary)</li>
     * <li>Send a Server->Client packet MyTargetSelected to the L2PcInstance player (display the select window)</li>
     * <li>If L2NpcInstance is autoAttackable, send a Server->Client packet StatusUpdate to the L2PcInstance in order to update L2NpcInstance HP bar </li>
     * <li>Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client </li><BR><BR>
     * 
     * <B><U> Actions on second click on the L2NpcInstance (Attack it/Intercat with it)</U> :</B><BR><BR>
     * <li>Send a Server->Client packet MyTargetSelected to the L2PcInstance player (display the select window)</li>
     * <li>If L2NpcInstance is autoAttackable, notify the L2PcInstance AI with AI_INTENTION_ATTACK (after a height verification)</li>
     * <li>If L2NpcInstance is NOT autoAttackable, notify the L2PcInstance AI with AI_INTENTION_INTERACT (after a distance verification) and show message</li><BR><BR>
     * 
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : Each group of Server->Client packet must be terminated by a ActionFailed packet in order to avoid
     * that client wait an other packet</B></FONT><BR><BR>
     * 
     * <B><U> Example of use </U> :</B><BR><BR>
     * <li> Client packet : Action, AttackRequest</li><BR><BR>
     * 
     * <B><U> Overriden in </U> :</B><BR><BR>
     * <li> L2ArtefactInstance : Manage only fisrt click to select Artefact</li><BR><BR>
     * <li> L2GuardInstance : </li><BR><BR>
     * 
     * @param player The L2PcInstance that start an action on the L2NpcInstance
     * 
     */
    public void onAction(L2PcInstance player)
    {
        
        // Check if the L2PcInstance already target the L2NpcInstance
        if (this != player.getTarget())
        {
            if (Config.DEBUG) _log.fine("new target selected:"+getObjectId());
            
            // Set the target of the L2PcInstance player
            player.setTarget(this);
            
            // Send a Server->Client packet MyTargetSelected to the L2PcInstance player
            // The player.getLevel() - getLevel() permit to display the correct color in the select window
            player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
            
            // Check if the player is attackable (without a forced attack)
            if (isAutoAttackable(player))
            {	
                // Send a Server->Client packet StatusUpdate of the L2NpcInstance to the L2PcInstance to update its HP bar
                StatusUpdate su = new StatusUpdate(getObjectId());
                su.addAttribute(StatusUpdate.CUR_HP, (int)getCurrentHp() );
                su.addAttribute(StatusUpdate.MAX_HP, getMaxHp() );
                player.sendPacket(su);
            }
            
            // Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
            player.sendPacket(new ValidateLocation(this));
        }
        else
        {
            // Send a Server->Client packet MyTargetSelected to the L2PcInstance player
            // The player.getLevel() - getLevel() permit to display the correct color
            player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
            
            // Check if the player is attackable (without a forced attack) and isn't dead
            if (isAutoAttackable(player) && !isAlikeDead())
            {
                // Check the height difference
                if (Math.abs(player.getZ() - getZ()) < 400) // this max heigth difference might need some tweaking
                {
                    // Set the L2PcInstance Intention to AI_INTENTION_ATTACK
                    player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
                    // player.startAttack(this);
                }
                else
                {
                    // Send a Server->Client packet ActionFailed (target is out of attack range) to the L2PcInstance player
                    player.sendPacket(new ActionFailed());
                }
            }
            
            
            if(!isAutoAttackable(player)) 
            {
                // Notify the L2PcInstance AI with AI_INTENTION_INTERACT
                player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
                
                // Calculate the distance between the L2PcInstance and the L2NpcInstance
                double distance = getDistance(player.getX(), player.getY());
                
                if (distance > INTERACTION_DISTANCE) 
                {
                    // Send a Server->Client packet ActionFailed (target is out of interaction range) to the L2PcInstance player
                    player.sendPacket(new ActionFailed());
                } 
                else 
                {
                    // Send a Server->Client packet SocialAction to the all L2PcInstance on the _knownPlayer of the L2NpcInstance
                    // to display a social action of the L2NpcInstance on their client
                    broadcastPacket(new SocialAction(getObjectId(), _rnd.nextInt(8)));
                    
                    // Open a chat window on client with the text of the L2NpcInstance
                    if(this.isEventMob){ L2Event.showEventHtml(player, String.valueOf(this.getObjectId())); }
                    else showChatWindow(player, 0);
                    
                    // Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
                    player.sendPacket(new ActionFailed());					
                }
            }
        }
    }
    
    /**
     * Manage and Display the GM console to modify the L2NpcInstance (GM only).<BR><BR>
     * 
     * <B><U> Actions (If the L2PcInstance is a GM only)</U> :</B><BR><BR>
     * <li>Set the L2NpcInstance as target of the L2PcInstance player (if necessary)</li>
     * <li>Send a Server->Client packet MyTargetSelected to the L2PcInstance player (display the select window)</li>
     * <li>If L2NpcInstance is autoAttackable, send a Server->Client packet StatusUpdate to the L2PcInstance in order to update L2NpcInstance HP bar </li>
     * <li>Send a Server->Client NpcHtmlMessage() containing the GM console about this L2NpcInstance </li><BR><BR>
     * 
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : Each group of Server->Client packet must be terminated by a ActionFailed packet in order to avoid
     * that client wait an other packet</B></FONT><BR><BR>
     * 
     * <B><U> Example of use </U> :</B><BR><BR>
     * <li> Client packet : Action</li><BR><BR>
     * 
     * @param client The thread that manage the player that pessed Shift and click on the L2NpcInstance
     * 
     */
    public void onActionShift(ClientThread client)
    {
        // Get the L2PcInstance corresponding to the thread
        L2PcInstance player = client.getActiveChar();
        
        // Check if the L2PcInstance is a GM
        if (player.getAccessLevel() >= Config.GM_ACCESSLEVEL)
        {
            // Set the target of the L2PcInstance player
            player.setTarget(this);
            
            // Send a Server->Client packet MyTargetSelected to the L2PcInstance player
            // The player.getLevel() - getLevel() permit to display the correct color in the select window
            player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
            
            // Check if the player is attackable (without a forced attack)
            if (isAutoAttackable(player))
            {	
                // Send a Server->Client packet StatusUpdate of the L2NpcInstance to the L2PcInstance to update its HP bar
                StatusUpdate su = new StatusUpdate(getObjectId());
                su.addAttribute(StatusUpdate.CUR_HP, (int)getCurrentHp() );
                su.addAttribute(StatusUpdate.MAX_HP, getMaxHp() );
                player.sendPacket(su);
            }
            
            
            // Send a Server->Client NpcHtmlMessage() containing the GM console about this L2NpcInstance
            NpcHtmlMessage html = new NpcHtmlMessage(1);
            StringBuffer html1 = new StringBuffer("<html><body>");
            String className = getClass().getName().substring(28);
            html1.append("<center>class "+className+", faction "+getFactionId()+", loc_id "+ (getSpawn() != null ? getSpawn().getLocation() : 0)+"</center>");
            html1.append("<table border=0 width=\"100%\">");
            html1.append("<tr><td>Target</td><td>"+getObjectId()+"</td><td>NPC ID</td><td>"+getTemplate().npcId+"</td></tr>");
            html1.append("<tr><td>Castle</td><td>"+getCastle().getCastleId()+"</td><td>Coords:</td><td>"+getX()+","+getY()+","+getZ()+"</td></tr>");
            html1.append("<tr><td>Level</td><td>"+getLevel()+"</td><td>Aggro</td><td>"+((this instanceof L2Attackable)? ((L2Attackable)this).getAggroRange() : 0)+"</td></tr>");
            html1.append("</table>");
            
            html1.append("<p>Combat");
            html1.append("<table border=0 width=\"100%\">");
            html1.append("<tr><td>Current HP</td><td>"+getCurrentHp()+"</td><td>Current MP</td><td>"+getCurrentMp()+"</td></tr>");
            html1.append("<tr><td>Max.HP</td><td>"+(int)(getMaxHp()/getTemplate().rateHp)+"*"+getTemplate().rateHp+"</td><td>Max.MP</td><td>"+getMaxMp()+"</td></tr>");
            html1.append("<tr><td>P.Atk.</td><td>"+getPAtk(null)+"</td><td>M.Atk.</td><td>"+getMAtk(null,null)+"</td></tr>");
            html1.append("<tr><td>P.Def.</td><td>"+getPDef(null)+"</td><td>M.Def.</td><td>"+getMDef(null,null)+"</td></tr>");
            html1.append("<tr><td>Accuracy</td><td>"+getAccuracy()+"</td><td>Evasion</td><td>"+getEvasionRate(null)+"</td></tr>");
            html1.append("<tr><td>Critical</td><td>"+getCriticalHit(null,null)+"</td><td>Speed</td><td>"+getRunSpeed()+"</td></tr>");
            html1.append("<tr><td>Atk.Speed</td><td>"+getPAtkSpd()+"</td><td>Cast.Speed</td><td>"+getMAtkSpd()+"</td></tr>");
            html1.append("<tr><td>Race</td><td>"+getTemplate().race+"</td><td></td><td></td></tr>");
            html1.append("</table>");
            
            html1.append("<p>Basic");
            html1.append("<table border=0 width=\"100%\">");
            html1.append("<tr><td>STR</td><td>"+getSTR()+"</td><td>DEX</td><td>"+getDEX()+"</td><td>CON</td><td>"+getCON()+"</td></tr>");
            html1.append("<tr><td>INT</td><td>"+getINT()+"</td><td>WIT</td><td>"+getWIT()+"</td><td>MEN</td><td>"+getMEN()+"</td></tr>");
            html1.append("</table>");
            
            html1.append("<table><tr><td><button value=\"Edit NPC\" action=\"bypass -h admin_edit_npc " + getTemplate().npcId + "\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            html1.append("<td><button value=\"Kill\" action=\"bypass -h admin_kill\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            html1.append("<tr><td><button value=\"Show DropList\" action=\"bypass -h admin_show_droplist " + getTemplate().npcId + "\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");			
            html1.append("<td><button value=\"Delete\" action=\"bypass -h admin_delete\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            html1.append("</table>");
            html1.append("</body></html>");
            
            html.setHtml(html1.toString());
            player.sendPacket(html);
        }
        else if(Config.ALT_GAME_VIEWNPC)
        {
            // Set the target of the L2PcInstance player
            player.setTarget(this);
            
            // Send a Server->Client packet MyTargetSelected to the L2PcInstance player
            // The player.getLevel() - getLevel() permit to display the correct color in the select window
            player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
            
            // Check if the player is attackable (without a forced attack)
            if (isAutoAttackable(player))
            {	
                // Send a Server->Client packet StatusUpdate of the L2NpcInstance to the L2PcInstance to update its HP bar
                StatusUpdate su = new StatusUpdate(getObjectId());
                su.addAttribute(StatusUpdate.CUR_HP, (int)getCurrentHp() );
                su.addAttribute(StatusUpdate.MAX_HP, getMaxHp() );
                player.sendPacket(su);
            }
            
            NpcHtmlMessage html = new NpcHtmlMessage(1);
            StringBuffer html1 = new StringBuffer("<html><body>");
            
            html1.append("<br><center><font color=\"LEVEL\">[Combat Stats]</font></center>");
            html1.append("<table border=0 width=\"100%\">");
            html1.append("<tr><td>Max.HP</td><td>"+(int)(getMaxHp()/getTemplate().rateHp)+"*"+getTemplate().rateHp+"</td><td>Max.MP</td><td>"+getMaxMp()+"</td></tr>");
            html1.append("<tr><td>P.Atk.</td><td>"+getPAtk(null)+"</td><td>M.Atk.</td><td>"+getMAtk(null,null)+"</td></tr>");
            html1.append("<tr><td>P.Def.</td><td>"+getPDef(null)+"</td><td>M.Def.</td><td>"+getMDef(null,null)+"</td></tr>");
            html1.append("<tr><td>Accuracy</td><td>"+getAccuracy()+"</td><td>Evasion</td><td>"+getEvasionRate(null)+"</td></tr>");
            html1.append("<tr><td>Critical</td><td>"+getCriticalHit(null,null)+"</td><td>Speed</td><td>"+getRunSpeed()+"</td></tr>");
            html1.append("<tr><td>Atk.Speed</td><td>"+getPAtkSpd()+"</td><td>Cast.Speed</td><td>"+getMAtkSpd()+"</td></tr>");
            html1.append("<tr><td>Race</td><td>"+getTemplate().race+"</td><td></td><td></td></tr>");
            html1.append("</table>");

            html1.append("<br><center><font color=\"LEVEL\">[Basic Stats]</font></center>");
            html1.append("<table border=0 width=\"100%\">");
            html1.append("<tr><td>STR</td><td>"+getSTR()+"</td><td>DEX</td><td>"+getDEX()+"</td><td>CON</td><td>"+getCON()+"</td></tr>");
            html1.append("<tr><td>INT</td><td>"+getINT()+"</td><td>WIT</td><td>"+getWIT()+"</td><td>MEN</td><td>"+getMEN()+"</td></tr>");
            html1.append("</table>");
            
            html1.append("<br><center><font color=\"LEVEL\">[Drop Info]</font></center>");
            html1.append("Rates legend: <font color=\"ff0000\">50%+</font> <font color=\"00ff00\">30%+</font> <font color=\"0000ff\">less than 30%</font>");
            html1.append("<table border=0 width=\"100%\">");
            
    	    for(L2DropData drop : getTemplate().getDropData())
    	    {
    	    	String name = ItemTable.getInstance().getTemplate(drop.getItemId()).getName();

    	    	if(drop.getChance() >= 600000)
    	    		html1.append("<tr><td><font color=\"ff0000\">" + name + "</font></td><td>" + (drop.isQuestDrop()?"Quest":(drop.isSweep()?"Sweep":"Drop")) + "</td></tr>");
    	    	else if(drop.getChance() >= 300000)
    	    		html1.append("<tr><td><font color=\"00ff00\">" + name + "</font></td><td>" + (drop.isQuestDrop()?"Quest":(drop.isSweep()?"Sweep":"Drop")) + "</td></tr>");
    	    	else
    	    		html1.append("<tr><td><font color=\"0000ff\">" + name + "</font></td><td>" + (drop.isQuestDrop()?"Quest":(drop.isSweep()?"Sweep":"Drop")) + "</td></tr>");
    	    }
    	    
    	    html1.append("</table>");
    	    html1.append("</body></html>");

            html.setHtml(html1.toString());
            player.sendPacket(html);

        }
        
        
        // Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
        player.sendPacket(new ActionFailed());
    }
    
    /** Return the L2Castle this L2NpcInstance belongs to. */
    public final Castle getCastle()
    {
        // Get castle this NPC belongs to (excluding L2Attackable)
        if (_CastleIndex < 0)
        {
            _CastleIndex = CastleManager.getInstance().getCastleIndexByTown(this);
            if (_CastleIndex < 0)
                _CastleIndex = CastleManager.getInstance().findNearestCastleIndex(this);
            else
                _IsInTown = true; // Npc was spawned in town
        }
        
        return CastleManager.getInstance().getCastles().get(_CastleIndex); }
    
    private boolean _IsInTown = false;
    public final boolean getIsInTown()
    {
        if (_CastleIndex < 0) getCastle();
        return _IsInTown;
    }
    
    /**
     * Open a quest or chat window on client with the text of the L2NpcInstance in function of the command.<BR><BR>
     * 
     * <B><U> Example of use </U> :</B><BR><BR>
     * <li> Client packet : RequestBypassToServer</li><BR><BR>
     * 
     * @param command The command string received from client
     * 
     */
    public void onBypassFeedback(L2PcInstance player, String command)
    {
        // Get the distance between the L2PcInstance and the L2NpcInstance
        double distance = getDistance(player.getX(), player.getY());
        
        if (distance > INTERACTION_DISTANCE) 
        {
            player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
        } 
        else 
        {
            if (isBusy())
            {
                player.sendPacket( new ActionFailed() );
                
                NpcHtmlMessage html = new NpcHtmlMessage(1);
                html.setFile("data/html/npcbusy.htm");
                html.replace("%busymessage%", getBusyMessage());
                html.replace("%npcname%", getName());
                html.replace("%playername%", player.getName());
                player.sendPacket(html);
            }
            else if (Config.ALLOW_WYVERN_UPGRADER && command.startsWith("upgrade") && player.getClan()!=null && player.getClan().getHasCastle()!=0)
            {
                String type = command.substring(8);
                if(type.equalsIgnoreCase("wyvern")){
                    L2NpcTemplate wind = NpcTable.getInstance().getTemplate(12526);
                    L2NpcTemplate star = NpcTable.getInstance().getTemplate(12527);
                    L2NpcTemplate twilight = NpcTable.getInstance().getTemplate(12528);
                    
                    L2Summon summon = player.getPet();
                    L2NpcTemplate myPet = summon.getTemplate();
                               
                    if ((myPet.equals(wind) || myPet.equals(star) || myPet.equals(twilight)) && player.getAdena()>=20000000 && (player.getInventory().getItemByObjectId(summon.getControlItemId())!=null)){
                        int exchangeItem = 4425;
                        player.reduceAdena(20000000);
                        player.getInventory().destroyItem( summon.getControlItemId(),1);
                        
                        L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(629);
                        try { L2Spawn spawn = new L2Spawn(template1);
                        
                        spawn.setLocx(getX()+20);
                        spawn.setLocy(getY()+20);
                        spawn.setLocz(getZ());
                        spawn.setAmount(1);
                        spawn.setHeading(player.getHeading());
                        spawn.setRespawnDelay(1);
                       
                        SpawnTable.getInstance().addNewSpawn(spawn, false);
                        
                        spawn.init();
                        spawn.getLastSpawn().setCurrentHp(999999999);
                        spawn.getLastSpawn().setName("baal");
                        spawn.getLastSpawn().setTitle("hell's god");
                        spawn.getLastSpawn().isEventMob =true;
                        spawn.getLastSpawn().isAggressive();
                        spawn.getLastSpawn().decayMe();
                        spawn.getLastSpawn().spawnMe(spawn.getLastSpawn().getX(),spawn.getLastSpawn().getY(),spawn.getLastSpawn().getZ());
                        
                         
                        int level = summon.getLevel();
                        int chance = (level-54)*10;
                        Random rnd = new Random();
                        spawn.getLastSpawn().broadcastPacket(new MagicSkillUser((L2Character)spawn.getLastSpawn(), (L2Character)spawn.getLastSpawn(), 1034, 1, 1, 1));
                        spawn.getLastSpawn().broadcastPacket(new MagicSkillUser((L2Character)spawn.getLastSpawn(), (L2Character)summon, 1034, 1, 1, 1));
                       
                        if(rnd.nextInt(100)<chance) {
                           
                            ClientScheduler.getInstance().scheduleLow(new destroyTemporalSummon(summon, player), 6000);
                            L2ItemInstance createditem = ItemTable.getInstance().createItem(exchangeItem);
                            player.getInventory().addItem(createditem);
                            NpcHtmlMessage adminReply = new NpcHtmlMessage(5);      
                            StringBuffer replyMSG = new StringBuffer("<html><body>");
                           
                             replyMSG.append("Congratulations, the evolution suceeded.");
                            replyMSG.append("</body></html>");
                            
                            adminReply.setHtml(replyMSG.toString());
                            player.sendPacket(adminReply);
                         }
                        else {
                            summon.reduceCurrentHp(summon.getCurrentHp(), player);
                            
                         }
                        ClientScheduler.getInstance().scheduleLow(new destroyTemporalNPC(spawn), 15000);
                    
                        player.sendPacket(new ItemList(player, true));
                        } catch(Exception e) {e.printStackTrace();}
                    }
                    else{
                        
                        NpcHtmlMessage adminReply = new NpcHtmlMessage(5);      
                        StringBuffer replyMSG = new StringBuffer("<html><body>");
                       
                        replyMSG.append("You will need 20.000.000 and have the pet summoned for the ceremony ...");
                        replyMSG.append("</body></html>");
                        
                        adminReply.setHtml(replyMSG.toString());
                        player.sendPacket(adminReply);
                            
                     
                }
                      
                }
                else if (type.equalsIgnoreCase("strider")) {
                    
                    L2NpcTemplate wind = NpcTable.getInstance().getTemplate(12311);
                    L2NpcTemplate star = NpcTable.getInstance().getTemplate(12312);
                    L2NpcTemplate twilight = NpcTable.getInstance().getTemplate(12313);
                    
                    L2Summon summon = player.getPet();
                    L2NpcTemplate myPet = summon.getTemplate();
                               
                    if ((myPet.equals(wind) || myPet.equals(star) || myPet.equals(twilight)) && player.getAdena()>=6000000 && (player.getInventory().getItemByObjectId(summon.getControlItemId())!=null)){
                        int exchangeItem = 4424;
                        if(myPet.equals(wind)) exchangeItem = 4422;
                        else if(myPet.equals(star)) exchangeItem = 4423;
                        player.reduceAdena(6000000);
                        player.getInventory().destroyItem( summon.getControlItemId(),1);
                        
                        L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(689);
                        try { L2Spawn spawn = new L2Spawn(template1);
                        
                        spawn.setLocx(getX()+20);
                        spawn.setLocy(getY()+20);
                        spawn.setLocz(getZ());
                        spawn.setAmount(1);
                        spawn.setHeading(player.getHeading());
                        spawn.setRespawnDelay(1);
                       
                        SpawnTable.getInstance().addNewSpawn(spawn, false);
                        
                        spawn.init();
                        spawn.getLastSpawn().setCurrentHp(999999999);
                        spawn.getLastSpawn().setName("mercebu");
                        spawn.getLastSpawn().setTitle("baal's son");
                        
                        spawn.getLastSpawn().isAggressive();
                        spawn.getLastSpawn().decayMe();
                        spawn.getLastSpawn().spawnMe(spawn.getLastSpawn().getX(),spawn.getLastSpawn().getY(),spawn.getLastSpawn().getZ());
                        
                        spawn.getLastSpawn().broadcastPacket(new MagicSkillUser((L2Character)spawn.getLastSpawn(), (L2Character) summon, 297, 1, 1, 1));
                        
                        int level = summon.getLevel();
                        int chance = (level-34)*10;
                        Random rnd = new Random();
                        spawn.getLastSpawn().broadcastPacket(new MagicSkillUser((L2Character)spawn.getLastSpawn(), (L2Character)spawn.getLastSpawn(), 1034, 1, 1, 1));
                        spawn.getLastSpawn().broadcastPacket(new MagicSkillUser((L2Character)spawn.getLastSpawn(), (L2Character)summon, 1034, 1, 1, 1));
                        
                      
                        if(rnd.nextInt(100)<chance) {
                           
                           
                            ClientScheduler.getInstance().scheduleLow(new destroyTemporalSummon(summon, player), 6000);
                            L2ItemInstance createditem = ItemTable.getInstance().createItem(exchangeItem);
                            player.getInventory().addItem(createditem);
                            NpcHtmlMessage adminReply = new NpcHtmlMessage(5);      
                            StringBuffer replyMSG = new StringBuffer("<html><body>");
                          
                            replyMSG.append("Congratulations, the evolution suceeded.");
                           replyMSG.append("</body></html>");
                           
                           adminReply.setHtml(replyMSG.toString());
                           player.sendPacket(adminReply);
                        }
                        else {
                            summon.reduceCurrentHp(summon.getCurrentHp(), player);
                            
                         }
                        
                        ClientScheduler.getInstance().scheduleLow(new destroyTemporalNPC(spawn), 15000);
                        player.sendPacket(new ItemList(player, true));
                        } catch(Exception e) {e.printStackTrace();}
                    }
                    else{
                        
                        NpcHtmlMessage adminReply = new NpcHtmlMessage(5);      
                        StringBuffer replyMSG = new StringBuffer("<html><body>");
                       
                         replyMSG.append("You will need 6.000.000 and have the pet summoned for the ceremony ...");
                        replyMSG.append("</body></html>");
                        
                        adminReply.setHtml(replyMSG.toString());
                        player.sendPacket(adminReply);
                            
                     
                }
            }
            }
            else if (command.equalsIgnoreCase("TerritoryStatus"))
            {
                NpcHtmlMessage html = new NpcHtmlMessage(1);
                html.setFile("data/html/territorystaus.htm");
                html.replace("%objectId%", String.valueOf(getObjectId()));
                html.replace("%npcname%", getName());
                
                if (getIsInTown())
                {
                    html.replace("%castlename%", getCastle().getName());
                    html.replace("%taxpercent%", "" + getCastle().getTaxPercent());
                    
                    if (getCastle().getOwnerId() > 0)
                    {
                        L2Clan clan = new L2Clan(getCastle().getOwnerId());
                        html.replace("%clanname%", clan.getName());
                        html.replace("%clanleadername%", clan.getLeaderName());
                    }
                    else
                    {
                        html.replace("%clanname%", "NPC");
                        html.replace("%clanleadername%", "NPC");
                    }
                }
                else
                {
                    html.replace("%castlename%", "Open");
                    html.replace("%taxpercent%", "0");
                    
                    html.replace("%clanname%", "No");
                    html.replace("%clanleadername%", getName());
                }
                
                player.sendPacket(html);
            }
			else if (command.equalsIgnoreCase("Exchange"))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile("data/html/merchant/exchange.htm");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%npcname%", getName());
				player.sendPacket(html);
			} 
            else if (command.startsWith("Quest"))
            {
                String quest = command.substring(5).trim();
                if (quest.length() == 0)
                    showQuestWindow(player);
                else
                    showQuestWindow(player, quest);
            }
            else if (command.startsWith("Chat"))
            {
                int val = Integer.parseInt(command.substring(5));
                showChatWindow(player, val);
            }
            else if (command.startsWith("Loto"))
            {
                int val = Integer.parseInt(command.substring(5));
                if (val == 0){
                    // new loto ticket
                    for (int i=0;i<5;i++)
                        player.setLoto(i,0);
                }
                showLotoWindow(player, val);
            }
            else if (command.startsWith("CPRecovery"))
            {
                makeCPRecovery(player);
            }
            else if (command.startsWith("SupportMagic"))
            {
                makeSupportMagic(player);
            }
            else if (command.startsWith("multisell"))
    		{
    			String listId = command.substring(9).trim();
    			player.sendPacket(new MultiSellList(Integer.parseInt(listId)));
    		}
            else if (command.startsWith("npcfind_byid"))
            {
            	Integer mobId = Integer.parseInt(command.substring(12).trim());
            	L2Spawn spawn = SpawnTable.getInstance().getTemplate(mobId);
            	player.sendPacket(new RadarControl(1,1,spawn.getLocx(),spawn.getLocy(),spawn.getLocz()));
            }
        }
    }
    
    /**
     * Send a packet SocialAction to all L2PcInstance in the _KnownPlayers of the L2NpcInstance and create a new RandomAnimation Task.<BR><BR>
     */
    public void onRandomAnimation()
    {
        // Send a packet SocialAction to all L2PcInstance in the _KnownPlayers of the L2NpcInstance
        SocialAction sa = new SocialAction(getObjectId(), Rnd.nextInt(8));
        broadcastPacket(sa);
        
        // Create a new RandomAnimation Task that will be launched after the calculated delay
        startRandomAnimation();
    }
    
    /**
     * Create a RandomAnimation Task that will be launched after the calculated delay.<BR><BR>
     */
    public void startRandomAnimation()
    {
        int minWait = Config.MIN_NPC_ANIMATION;
        int maxWait = Config.MAX_NPC_ANIMATION;
        
        // Calculate the delay before the next animation
        int interval = (Rnd.nextInt(maxWait - minWait) + minWait) * 1000;
        
        // Create a RandomAnimation Task that will be launched after the calculated delay 
        ClientScheduler.getInstance().scheduleLow(new RandomAnimationTask(), interval);
    }
    
    /**
     * Return null (regular NPCs don't have weapons instancies).<BR><BR>
     */
    public L2ItemInstance getActiveWeaponInstance()
    {
        // regular NPCs dont have weapons instancies
        return null;
    }
    
    /**
     * Return the weapon item equiped in the right hand of the L2NpcInstance or null.<BR><BR>
     */
    public L2Weapon getActiveWeaponItem()
    {
        // Get the weapon identifier equiped in the right hand of the L2NpcInstance
        int weaponId = getTemplate().rhand;
        
        if (weaponId < 1)
            return null;
        
        // Get the weapon item equiped in the right hand of the L2NpcInstance
        L2Item item = ItemTable.getInstance().getTemplate(getTemplate().rhand);
        
        if (!(item instanceof L2Weapon))
            return null;
        
        return (L2Weapon)item;
    }
    
    /**
     * Return null (regular NPCs don't have weapons instancies).<BR><BR>
     */
    public L2ItemInstance getSecondaryWeaponInstance()
    {
        // regular NPCs dont have weapons instancies
        return null;
    }
    
    /**
     * Return the weapon item equiped in the left hand of the L2NpcInstance or null.<BR><BR>
     */
    public L2Weapon getSecondaryWeaponItem()
    {
        // Get the weapon identifier equiped in the right hand of the L2NpcInstance
        int weaponId = getTemplate().lhand;
        
        if (weaponId < 1)
            return null;
        
        // Get the weapon item equiped in the right hand of the L2NpcInstance
        L2Item item = ItemTable.getInstance().getTemplate(getTemplate().lhand);
        
        if (!(item instanceof L2Weapon))
            return null;
        
        return (L2Weapon)item;
    }
    
    
    /**
     * Send a Server->Client packet NpcHtmlMessage to the L2PcInstance in order to display the message of the L2NpcInstance.<BR><BR>
     * 
     * @param player The L2PcInstance who talks with the L2NpcInstance
     * @param content The text of the L2NpcMessage
     * 
     */
    public void insertObjectIdAndShowChatWindow(L2PcInstance player, String content)
    {
        // Send a Server->Client packet NpcHtmlMessage to the L2PcInstance in order to display the message of the L2NpcInstance
        content = content.replaceAll("%objectId%", String.valueOf(getObjectId()));
        NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
        npcReply.setHtml(content);
        player.sendPacket(npcReply);
    }
    
    /**
     * Return the pathfile of the selected HTML file in function of the npcId and of the page number.<BR><BR>
     *   
     * <B><U> Format of the pathfile </U> :</B><BR><BR>
     * <li> if the file exists on the server (page number = 0) : <B>data/html/default/12006.htm</B> (npcId-page number)</li>
     * <li> if the file exists on the server (page number > 0) : <B>data/html/default/12006-1.htm</B> (npcId-page number)</li>
     * <li> if the file doesn't exist on the server : <B>data/html/npcdefault.htm</B> (message : "I have nothing to say to you")</li><BR><BR>
     * 
     * <B><U> Overriden in </U> :</B><BR><BR>
     * <li> L2GuardInstance : Set the pathfile to data/html/guard/12006-1.htm (npcId-page number)</li><BR><BR>
     * 
     * @param npcId The Identifier of the L2NpcInstance whose text must be display
     * @param val The number of the page to display
     * 
     */
    public String getHtmlPath(int npcId, int val)
    {
        String pom = "";
        
        if (val == 0)
        {
            pom = "" + npcId;
        } 
        else 
        {
            pom = npcId + "-" + val;
        }
        
        String temp = "data/html/default/" + pom + ".htm";
        
        // Return the pathfile of the HTML file
        if (Config.LAZY_CACHE && !HtmCache.getInstance().contains(temp))
            HtmCache.getInstance().loadFile(new File(Config.DATAPACK_ROOT,temp));
        	if (HtmCache.getInstance().contains(temp))
        		return temp;
        
        // If the file is not found, the standard message "I have nothing to say to you" is returned
        return "data/html/npcdefault.htm";
        
    }
    
    /**
     * Open a choose quest window on client with all quests available of the L2NpcInstance.<BR><BR>
     * 
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Send a Server->Client NpcHtmlMessage containing the text of the L2NpcInstance to the L2PcInstance </li><BR><BR>
     * 
     * @param player The L2PcInstance that talk with the L2NpcInstance
     * @param quests The table containing quests of the L2NpcInstance
     * 
     */
    public void showQuestChooseWindow(L2PcInstance player, Quest[] quests) 
    {
        StringBuffer sb = new StringBuffer();
        
        sb.append("<html><body><title>Talk about:</title><br>");
        
        for (Quest q : quests) 
        {
            sb.append("<a action=\"bypass -h npc_").append(getObjectId())
            .append("_Quest ").append(q.getName()).append("\">")
            .append(q.getDescr()).append("</a><br>");
        }
        
        sb.append("</body></html>");
        
        // Send a Server->Client packet NpcHtmlMessage to the L2PcInstance in order to display the message of the L2NpcInstance
        insertObjectIdAndShowChatWindow(player, sb.toString());
        
    }
    
    /**
     * Open a quest window on client with the text of the L2NpcInstance.<BR><BR>
     * 
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Get the text of the quest state in the folder data/jscript/quests/questId/stateId.htm </li>
     * <li>Send a Server->Client NpcHtmlMessage containing the text of the L2NpcInstance to the L2PcInstance </li>
     * <li>Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet </li><BR><BR>
     * 
     * @param player The L2PcInstance that talk with the L2NpcInstance
     * @param questId The Identifier of the quest to display the message
     * 
     */
    public void showQuestWindow(L2PcInstance player, String questId) 
    {
        
        String content;
        
        if (player.getWeightPenalty()>=3){	
            content = "<html><body>I do not have any tasks for you, because of your weight penalty. Go away.</body></html>";
            insertObjectIdAndShowChatWindow(player, content);
            return;
        }
        
        //FileInputStream fis = null;
        
        // Get the state of the selected quest
        QuestState qs = player.getQuestState(questId);
        
        if (qs != null) 
        {
            // If the quest is alreday started, no need to show a window
            if (!qs.getQuest().notifyTalk(this, qs))
                return;
        }
        else
        {
            Quest q = QuestManager.getInstance().getQuest(questId);
            if (q != null) 
            {
                // check for start point
                Quest[] qlst = getTemplate().getStartQuests();
                
                if (qlst != null && qlst.length > 0) 
                {
                    for (int i=0; i < qlst.length; i++) 
                    {
                        if (qlst[i] == q) 
                        {
                            qs = q.newQuestState(player);
                            //disabled by mr. becouse quest dialog only show on second click.
                            //if(qs.getState().getName().equalsIgnoreCase("completed"))
                            //{
                            if (!qs.getQuest().notifyTalk(this, qs))
                                return; // no need to show a window
                            //}
                            break;
                        }
                    }
                }
            }
        }
        
        if (qs == null) 
        {
            // no quests found
            content = "<html><body>No Quest</body></html>";
        } 
        else 
        {
            questId = qs.getQuest().getName();
            String stateId = qs.getStateId();
            String path = "data/jscript/quests/"+questId+"/"+stateId+".htm";
            content = HtmCache.getInstance().getHtm(path); //TODO path for quests html
            
            if (Config.DEBUG)
            {
                if (content != null)
                {
                    _log.fine("Showing quest window for quest "+questId+" html path: " + path);
                }
                else
                {
                    _log.fine("File not exists for quest "+questId+" html path: " + path);
                }
            }
        }
        
        // Send a Server->Client packet NpcHtmlMessage to the L2PcInstance in order to display the message of the L2NpcInstance
        if (content != null)
            insertObjectIdAndShowChatWindow(player, content);
        
        // Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
        player.sendPacket( new ActionFailed() );
    }
    
    /**
     * Collect awaiting quests/start points and display a QuestChooseWindow (if several available) or QuestWindow.<BR><BR>
     * 
     * @param player The L2PcInstance that talk with the L2NpcInstance
     * 
     */
    public void showQuestWindow(L2PcInstance player) 
    {
        // collect awaiting quests and start points
        List<Quest> options = new FastList<Quest>();
        
        QuestState[] awaits = player.getQuestsForTalk(getTemplate().npcId);
        Quest[] starts = getTemplate().getStartQuests();
        
        
        if (awaits != null) 
        {
            for (QuestState x : awaits) 
            {
                if (!options.contains(x))
                    if((x.getQuest().getQuestIntId()>0) && (x.getQuest().getQuestIntId()<1000))
                        options.add(x.getQuest());
            }
        }
        
        if (starts != null) 
        {
            for (Quest x : starts) 
            {
                if (!options.contains(x))
                    if((x.getQuestIntId()>0) && (x.getQuestIntId()<1000))
                        options.add(x);
            }
        }
        
        
        // Display a QuestChooseWindow (if several quests are available) or QuestWindow
        if (options.size() > 1) 
        {
            showQuestChooseWindow(player, options.toArray(new Quest[options.size()]));
        }
        else if (options.size() == 1) 
        {
            showQuestWindow(player, options.get(0).getName());
        }
        else 
        {
            showQuestWindow(player, "");
        }
        
    }
    
    /**
     * Open a Loto window on client with the text of the L2NpcInstance.<BR><BR>
     * 
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Get the text of the selected HTML file in function of the npcId and of the page number </li>
     * <li>Send a Server->Client NpcHtmlMessage containing the text of the L2NpcInstance to the L2PcInstance </li>
     * <li>Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet </li><BR>
     * 
     * @param player The L2PcInstance that talk with the L2NpcInstance
     * @param val The number of the page of the L2NpcInstance to display
     * 
     */
    // 0 - buy ticket window
    // 1-20 - buttons
    // 22 - lottery ticket window
    // 21 - selected ticket with 5 numbers
    public void showLotoWindow(L2PcInstance player, int val)
    {
        int npcId = getTemplate().npcId;
        String filename;
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        //if loto
        if (val==0){
            filename = (getHtmlPath(npcId, 1));
            // Send a Server->Client NpcHtmlMessage containing the text of the L2NpcInstance to the L2PcInstance 
            html.setFile(filename);
        }
        else if (val != 21){
            filename = (getHtmlPath(npcId, 5));
            // Send a Server->Client NpcHtmlMessage containing the text of the L2NpcInstance to the L2PcInstance 
            html.setFile(filename);
            int count = 0;
            int found = 0;
            // counting buttons and unsetting button if found
            for (int i=0;i<5;i++){
                if (player.getLoto(i)==val){
                    //unsetting button
                    player.setLoto(i,0);found=1;
                }else if (player.getLoto(i)>0)count++;
            }
            //if not rearched limit 5 and not unseted value
            if (count<5 && found == 0 && val < 21)
                for (int i=0;i<5;i++)
                    if (player.getLoto(i)==0){
                        player.setLoto(i,val);break;
                    }
            //setting pusshed buttons
            count=0;
            for (int i=0;i<5;i++)
                if (player.getLoto(i)>0){
                    count++;
                    String button = String.valueOf(player.getLoto(i));
                    if (player.getLoto(i)<10)
                        button = "0" + button;
                    String search = "fore=\"L2UI.lottoNum"+button+"\" back=\"L2UI.lottoNum"+button+"a_check\"";
                    String replace = "fore=\"L2UI.lottoNum"+button+"a_check\" back=\"L2UI.lottoNum"+button+"\"";
                    html.replace(search,replace);
                }
            if (count == 5){
                String search = "0\">Return";
                String replace = "21\">The winner selected the numbers above.";
                html.replace(search,replace);
            }
        } else {
            
            int price = 2000;
            int lotonumber = 1;
            int enchant = 0;
            int type2 = 0;
            
            for (int i=0;i<5;i++)
            {
                if (player.getLoto(i)==0)
                    return;
                
                if (player.getLoto(i)<17)
                    enchant += Math.pow(2,player.getLoto(i)-1);
                else 
                    type2 += Math.pow(2,player.getLoto(i)-17);
            }
            if (player.getAdena() < price){
                player.sendPacket(new SystemMessage(SystemMessage.YOU_NOT_ENOUGH_ADENA));
                return;
            }
            player.reduceAdena(price);
            player.sendPacket(new SystemMessage(SystemMessage.DISSAPEARED_ADENA).addNumber(price));
            player.sendPacket(new SystemMessage(SystemMessage.ACQUIRED).addNumber(lotonumber).addItemName(4442));
            L2ItemInstance item = new L2ItemInstance(IdFactory.getInstance().getNextId(),4442);
            item.setCount(1);
            item.setCustomType1(lotonumber);
            item.setEnchantLevel(enchant);
            item.setCustomType2(type2);
            player.getInventory().addItem(item);
            InventoryUpdate iu = new InventoryUpdate();
            iu.addItem(item);
            L2ItemInstance adenaupdate = player.getInventory().findItemByItemId(57);
            iu.addModifiedItem(adenaupdate);
            player.sendPacket(iu);
            html.setHtml("<html><body>Lottery Ticket Seller:<br>Thank you for playing the lottery<br>The winners will be announced at ??? 19<br><center><a action=\"bypass -h npc_%objectId%_Chat 0\">Back</a></center></body></html>");
        }
        html.replace("%objectId%", String.valueOf(getObjectId()));
        player.sendPacket(html);
        
        // Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
        player.sendPacket( new ActionFailed() );
    }
    
    
    public void makeCPRecovery(L2PcInstance player)
    {
        if (getNpcId()!=8225 && getNpcId()!=8226)
            return;
        int neededmoney = 100;
        int currentmoney = player.getAdena();
        if (neededmoney > currentmoney){
            player.sendPacket(new SystemMessage(SystemMessage.YOU_NOT_ENOUGH_ADENA));
            return;
        }
        player.reduceAdena(neededmoney);
        player.sendPacket(new SystemMessage(SystemMessage.DISSAPEARED_ADENA).addNumber(neededmoney));
        player.sendPacket(new InventoryUpdate().addModifiedItem(player.getInventory().getItemByItemId(57)));
        player.setCurrentCp(getCurrentCp()+5000);
        player.sendPacket(new SystemMessage(1405).addString(player.getName()));
    }
    // in dp changed skills time <50. 
    // too many job for make skills one after one.
    // problems will be with 2 and more players etc.. 
    public void makeSupportMagic(L2PcInstance player)
    {
        int lvl = player.getLevel();
        L2Skill skill;
        this.setTarget(player);
        if (lvl < 10){
            String content = "<html><body>Come back here when <font color=\"LEVEL\">you have reached level 10</font>. I will give you support magic then.</body></html>";
            insertObjectIdAndShowChatWindow(player, content);
            return;
        }
        if (lvl > 24){
            String content = "<html><body>Newbie Guide:<br>Only a <font color=\"LEVEL\">novice character of level 24 or less</font> can receive my support magic.<br>Your novice character is the first one that you created and raised in this world.</body></html>";
            insertObjectIdAndShowChatWindow(player, content);
            return;
        }
        
        //windwalk
        if (lvl >= 10 && lvl <= 24){
            skill = SkillTable.getInstance().getInfo(4322,1);
            this.doCast(skill);
        }
        //shield
        if (lvl >= 11 && lvl <= 23){
            skill = SkillTable.getInstance().getInfo(4323,1);
            this.doCast(skill);
        }
	//lifecubic
	if (lvl >= 16 && lvl <= 19){
	  skill = SkillTable.getInstance().getInfo(4338,1);
	  this.doCast(skill);
	}
        if (!player.isMageClass()){
            //blessofbody
            if (lvl >= 12 && lvl <= 22){
                skill = SkillTable.getInstance().getInfo(4324,1);
                this.doCast(skill);
            }
            //vampirerage
            if (lvl >= 13 && lvl <= 21){
                skill = SkillTable.getInstance().getInfo(4325,1);
                this.doCast(skill);
            }
            //regeneration
            if (lvl >= 14 && lvl <= 20){
                skill = SkillTable.getInstance().getInfo(4326,1);
                this.doCast(skill);
            }
            //haste
            if (lvl >= 15 && lvl <= 19){
                skill = SkillTable.getInstance().getInfo(4327,1);
                this.doCast(skill);
            }
            //lifecubic
            if (lvl >= 13 && lvl <= 19){
                skill = SkillTable.getInstance().getInfo(4338,1);
                this.doCast(skill);
            }
        } else {
            //blessthesoul
            if (lvl >= 12 && lvl <= 22){
                skill = SkillTable.getInstance().getInfo(4328,1);
                this.doCast(skill);
            }
            //acumen
            if (lvl >= 13 && lvl <= 21){
                skill = SkillTable.getInstance().getInfo(4329,1);
                this.doCast(skill);
            }
            //concentration
            if (lvl >= 14 && lvl <= 20){
                skill = SkillTable.getInstance().getInfo(4330,1);
                this.doCast(skill);
            }
            //empower
            if (lvl >= 15 && lvl <= 19){
                skill = SkillTable.getInstance().getInfo(4331,1);
                this.doCast(skill);
            }
        }
    }
    
    public void showChatWindow(L2PcInstance player)
    {
        showChatWindow(player, 0);
    }

    /**
     * Open a chat window on client with the text of the L2NpcInstance.<BR><BR>
     * 
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Get the text of the selected HTML file in function of the npcId and of the page number </li>
     * <li>Send a Server->Client NpcHtmlMessage containing the text of the L2NpcInstance to the L2PcInstance </li>
     * <li>Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet </li><BR>
     * 
     * @param player The L2PcInstance that talk with the L2NpcInstance
     * @param val The number of the page of the L2NpcInstance to display
     * 
     */
    public void showChatWindow(L2PcInstance player, int val)
    {
        int npcId = getTemplate().npcId;
        
        /* For use with Seven Signs implementation */
        String filename = SevenSigns.SEVEN_SIGNS_HTML_PATH;
        int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);
        boolean isSealValidationPeriod = SevenSigns.getInstance().isSealValidationPeriod();
        int compWinner = SevenSigns.getInstance().getCabalHighestScore();
        
        switch (npcId) {
            case 8078:
            case 8079:
            case 8080:
            case 8081:
            case 8082: // Dawn Priests
            case 8083:
            case 8084:
            case 8168:
                switch (playerCabal) {
                    case SevenSigns.CABAL_DAWN:
                        if (isSealValidationPeriod) 
                            if (compWinner == SevenSigns.CABAL_DAWN)
                                filename += "dawn_priest_2a.htm";
                            else
                                filename += "dawn_priest_2b.htm";
                        else
                            filename += "dawn_priest_1b.htm";
                        break;
                    case SevenSigns.CABAL_DUSK:
                        if (isSealValidationPeriod) 
                            filename += "dawn_priest_3b.htm";
                        else 
                            filename += "dawn_priest_3a.htm";
                        break;
                    default:
                        if (isSealValidationPeriod)
                        	if (!Config.ALLOW_SS_REGIST)
                        	{
                        		if (compWinner == SevenSigns.CABAL_DAWN)
                        			filename += "dawn_priest_4.htm";
                        		else
                        			filename += "dawn_priest_2b.htm";
                        	}else{ //Extern to Regist all time seal
                        		if (compWinner == SevenSigns.CABAL_DAWN)
                        			filename += "dawn_priest_4b.htm";
                        		else
                        			filename += "dawn_priest_2c.htm";
                        	}
                        else
                            filename += "dawn_priest_1a.htm";
                    break;
                }
                break;
            case 8085:
            case 8086:
            case 8087:
            case 8088: // Dusk Priest
            case 8089:
            case 8090:
            case 8091:
            case 8169:
                switch (playerCabal) {
                    case SevenSigns.CABAL_DUSK:
                        if (isSealValidationPeriod) 
                            if (compWinner == SevenSigns.CABAL_DUSK)
                                filename += "dusk_priest_2a.htm";
                            else
                                filename += "dusk_priest_2b.htm";
                        else
                            filename += "dusk_priest_1b.htm";
                        break;
                    case SevenSigns.CABAL_DAWN:
                        if (isSealValidationPeriod) 
                            filename += "dusk_priest_3b.htm";
                        else 
                            filename += "dusk_priest_3a.htm";
                        break;
                    default:
                        if (isSealValidationPeriod)
                        	if (!Config.ALLOW_SS_REGIST)
                        	{
                        		if (compWinner == SevenSigns.CABAL_DUSK)
                        			filename += "dusk_priest_4.htm";
                        		else
                        			filename += "dusk_priest_2b.htm";
                        	}else{ //Extern to Regist all time seal
                        		if (compWinner == SevenSigns.CABAL_DUSK)
                        			filename += "dusk_priest_4b.htm";
                        		else
                        			filename += "dusk_priest_2c.htm";
                        	}
                        else
                        	filename += "dusk_priest_1a.htm";
                    break;
                }
                break;
            case 8095: //
            case 8096: //
            case 8097: //
            case 8098: // Enter Necropolises
            case 8099: //
            case 8100: //
            case 8101: // 
            case 8102: //
		if (!Config.ALLOW_SS_TELEPORT) {
                if (isSealValidationPeriod) {
                    if (compWinner == SevenSigns.CABAL_DAWN && playerCabal != SevenSigns.CABAL_DAWN) 
                        player.sendPacket(new SystemMessage(1301));
                    else if (compWinner == SevenSigns.CABAL_DUSK && playerCabal != SevenSigns.CABAL_DUSK)
                        player.sendPacket(new SystemMessage(1302));
                    
                    if (compWinner != playerCabal)
                        filename += "necro_no.htm";
                    else 
                        filename = (getHtmlPath(npcId, val)); // do the default!
                }
                else {
                    if (playerCabal == SevenSigns.CABAL_NULL)
                        filename += "necro_no.htm";
                    else
                        filename = (getHtmlPath(npcId, val)); // do the default!    
                }
		}
		else
		    filename = (getHtmlPath(npcId, val));
                break;
            case 8114: //
            case 8115: //
            case 8116: // Enter Catacombs
            case 8117: //
            case 8118: //
            case 8119: //
		if (!Config.ALLOW_SS_TELEPORT) {
                if (isSealValidationPeriod) {
                    if (compWinner == SevenSigns.CABAL_DAWN && playerCabal != SevenSigns.CABAL_DAWN) 
                        player.sendPacket(new SystemMessage(1301));
                    else if (compWinner == SevenSigns.CABAL_DUSK && playerCabal != SevenSigns.CABAL_DUSK)
                        player.sendPacket(new SystemMessage(1302));
                    
                    if (compWinner != playerCabal)
                        filename += "cata_no.htm";
                    else 
                        filename = (getHtmlPath(npcId, val)); // do the default!
                }
                else {
                    if (playerCabal == SevenSigns.CABAL_NULL)
                        filename += "cata_no.htm";
                    else
                        filename = (getHtmlPath(npcId, val)); // do the default!    
                }
		}
		else
		    filename = (getHtmlPath(npcId, val));
                break;
            case 8111:// Gatekeeper Spirit (Disciples)
            case 8112://
                if (isSealValidationPeriod)
                {
                    int sealAvariceOwner = SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_AVARICE);
                    
                    if (sealAvariceOwner == SevenSigns.CABAL_DUSK)
                        filename += "spirit_dusk.htm";
                    else if (sealAvariceOwner == SevenSigns.CABAL_DAWN)
                        filename += "spirit_dawn.htm";
                    else
                        filename += "spirit_null.htm";
                }
                else {
                    filename += "spirit_null.htm";
                }
                break;
            case 8127: //
            case 8128: //
            case 8129: // Dawn Festival Guides
            case 8130: //
            case 8131: //
                filename += "festival/dawn_guide.htm";
                break;
            case 8137: //
            case 8138: //
            case 8139: // Dusk Festival Guides
            case 8140: //
            case 8141: //
                filename += "festival/dusk_guide.htm";
                break;
            default:
                if ((npcId >= 8093 && npcId <= 8094) ||
                        (npcId >= 8172 && npcId <= 8201) ||
                        (npcId >= 8239 && npcId <= 8254))
                    return;
            // Get the text of the selected HTML file in function of the npcId and of the page number
            filename = (getHtmlPath(npcId, val));
            break;
            
            case 8132:
            case 8133:
            case 8134:
            case 8135:
            case 8136:  // Festival Witches
            case 8142:
            case 8143:
            case 8144:
            case 8145:
            case 8146:            
                filename += "festival/festival_witch.htm";
        }
        
        // Send a Server->Client NpcHtmlMessage containing the text of the L2NpcInstance to the L2PcInstance 
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setFile(filename);
        
        if (this instanceof L2MerchantInstance)
            if (Config.LIST_PET_RENT_NPC.contains(npcId))
                html.replace("_Quest", "_RentPet\">Rent Pet</a><br><a action=\"bypass -h npc_%objectId%_Quest");
        
        html.replace("%objectId%", String.valueOf(getObjectId()));
        html.replace("%festivalMins%", SevenSignsFestival.getInstance().getTimeToNextFestivalStr());        
        player.sendPacket(html);
        
        // Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
        player.sendPacket( new ActionFailed() );
    }
    
    /**
     * Open a chat window on client with the text specified by the given file name and path,<BR>
     * relative to the datapack root.
     * <BR><BR>
     * Added by Tempy 
     * @param player The L2PcInstance that talk with the L2NpcInstance
     * @param filename The filename that contains the text to send
     *  
     */
    public void showChatWindow(L2PcInstance player, String filename)
    {
        // Send a Server->Client NpcHtmlMessage containing the text of the L2NpcInstance to the L2PcInstance 
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setFile(filename);
        html.replace("%objectId%",String.valueOf(getObjectId()));
        player.sendPacket(html);
        
        // Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
        player.sendPacket( new ActionFailed() );
    }
    
    /**
     * Return the Exp Reward of this L2NpcInstance contained in the L2NpcTemplate (modified by RATE_XP).<BR><BR>
     */
    public int getExpReward()
    {
        return (int)(getTemplate().revardExp * getTemplate().rateHp * Config.RATE_XP);
    }
    
    /**
     * Return the SP Reward of this L2NpcInstance contained in the L2NpcTemplate (modified by RATE_SP).<BR><BR>
     */
    public int getSpReward()
    {
        return (int)(getTemplate().revardSp * getTemplate().rateHp * Config.RATE_SP);
    }
    
    /**
     * Kill the L2NpcInstance (the corpse disappeared after 7 seconds).<BR><BR>
     * 
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Create a DecayTask to remove the corpse of the L2NpcInstance after 7 seconds </li>
     * <li>Set target to null and cancel Attack or Cast </li>
     * <li>Stop movement </li>
     * <li>Stop HP/MP/CP Regeneration task </li>
     * <li>Stop all active skills effects in progress on the L2Character </li>
     * <li>Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform </li>
     * <li>Notify L2Character AI </li><BR><BR>
     * 
     * <B><U> Overriden in </U> :</B><BR><BR>
     * <li> L2Attackable </li><BR><BR>
     * 
     * @param killer The L2Character who killed it
     * 
     */
    public void doDie(L2Character killer) 
    {
        // Create a DecayTask to remove the corpse of the L2NpcInstance after 8.5 seconds
        //_decayTask = ClientScheduler.getInstance().scheduleLow(new DecayTask(), 8500);
        //SpawnTable.getInstance().addDecayTask(this);
        DecayTaskManager.getInstance().addDecayTask(this);
        
        super.doDie(killer);
    }
    
    /**
     * Set the spawn of the L2NpcInstance.<BR><BR>
     * 
     * @param spawn The L2Spawn that manage the L2NpcInstance
     * 
     */
    public void setSpawn(L2Spawn spawn)
    {
        _spawn = spawn;
    }
    
    
    /**
     * Remove the L2NpcInstance from the world and update its spawn object (for a complete removal use the deleteMe method).<BR><BR>
     * 
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Remove the L2NpcInstance from the world when the decay task is launched </li>
     * <li>Decrease its spawn counter </li>
     * <li>Manage Siege task (killFlag, killCT) </li><BR><BR>
     * 
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from _allObjects of L2World </B></FONT><BR>
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packets to players</B></FONT><BR><BR>
     * 
     */
    public void onDecay()
    {
        // Manage Siege task
        if(this.getNpcId() == 12024) // Headquarters
        {
            Castle castle = CastleManager.getInstance().getCastle(getX(), getY());
            if (castle != null && castle.getSiege().getIsInProgress()) castle.getSiege().killedFlag(this);
        }
        else if (this instanceof L2ControlTowerInstance) // Life Control Tower
            ((L2ControlTowerInstance)this).onDeath();
        
        // Remove the L2NpcInstance from the world when the decay task is launched
        super.onDecay();
        
        // Decrease its spawn counter
        if (_spawn != null)
            _spawn.decreaseCount(this);
        
        // Reset the _decayTask for this L2NpcInstance
        _decayTask = null;
    }
    
    /**
     * Remove PROPERLY the L2NpcInstance from the world.<BR><BR>
     * 
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Remove the L2NpcInstance from the world and update its spawn object </li>
     * <li>Remove all L2Object from _knownObjects and _knownPlayer of the L2NpcInstance then cancel Attak or Cast and notify AI </li>
     * <li>Remove L2Object object from _allObjects of L2World </li><BR><BR>
     * 
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packets to players</B></FONT><BR><BR>
     * 
     */
    public void deleteMe()
    {
        //FIXME this is just a temp hack, we should find a better solution
        
        try { decayMe(); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }
        
        // Remove all L2Object from _knownObjects and _knownPlayer of the L2Character then cancel Attak or Cast and notify AI
        try { getKnownList().removeAllKnownObjects(); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }
        
        // Remove L2Object object from _allObjects of L2World
        L2World.getInstance().removeObject(this);
    }
    
    /**
     * Return the L2Spawn object that manage this L2NpcInstance.<BR><BR>
     */
    public L2Spawn getSpawn()
    {
        return _spawn;
    }
    
    public String toString()
    {
        return this.getTemplate().name;
    }
    
    public void endDecayTask()
    {
        if (_decayTask == null)
            return;
        
        _decayTask.cancel(true);
        this.onDecay();
    }
    
}
