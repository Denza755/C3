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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Future;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ClanTable;
import net.sf.l2j.gameserver.ClientScheduler;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.GameServer;
import net.sf.l2j.gameserver.SkillTable;
import net.sf.l2j.gameserver.ai.L2CastleGuardAI;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.NpcInfo;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.StatsSet;


/**
 * This class represents all guards in the world. It inherits all methods from
 * L2Attackable and adds some more such as tracking PK's or custom interactions.
 * 
 * @version $Revision: 1.11.2.1.2.7 $ $Date: 2005/04/06 16:13:40 $
 */
public final class L2CastleGuardInstance extends L2Character
{
	private int _clanid;
	private String _group = "none";
	protected Future _decayTask;
	
	protected int templateid;
	public int x;
	public int y;
	public int z;
	protected int hp=100;
	protected int mp=100;
	protected int patk=100;
	protected int pspd=100;
	protected int pdef=100;
	protected int matk=100;
	protected int mspd=100;
	protected int mdef=100;
	protected int magery=100;
	protected boolean pspdcontrol;
	protected boolean pdefcontrol;
	protected boolean matkcontrol;
	protected boolean mspdcontrol;
	protected boolean mdefcontrol;
	protected boolean magerycontrol;
	protected boolean hpcontrol;
	protected boolean mpcontrol;
	protected boolean patkcontrol;
	protected String name;
	
	public static ArrayList<Integer> _castleGuards;
	
	
	public static final int JUST_HIRED = 0;
	public static final int ATTACK = 1;
	
	
	public L2CastleGuardInstance(Integer objectId, L2NpcTemplate template)
	{   /** castle guards stats are its template base stats multiplied by a percental modifier that can be improved */
		super(objectId, template);
		restoreFromDB();
		setTemplate(setUpTemplate(template));
		_castleGuards   = new ArrayList<Integer>();
		_castleGuards.add(objectId);
		x = this.getX();
		y = this.getY();
		z = this.getZ();
		templateid = template.npcId;
	}
	public boolean isAttackable()
	{
		return true;
	}
	public final L2NpcTemplate getTemplate()
	{
		return (L2NpcTemplate)_template;
	}
	public final int getLevel() {
		return getTemplate().level;
	}
	public int getDistanceToWatchObject(@SuppressWarnings("unused") L2Object object)
	{
		return 4000;
	}
	public boolean isAutoAttackable(@SuppressWarnings("unused") L2Character attacker) {
		return true;
	}
	public L2ItemInstance getActiveWeaponInstance() {
		return null;
	}
	public L2Weapon getActiveWeaponItem() {
		return null;
	}
	public L2ItemInstance getSecondaryWeaponInstance() {
		return null;
	}
	public L2Weapon getSecondaryWeaponItem() {
		return null;
	}

	public void updateAbnormalEffect()
	{
		if(!isKnownPlayers())
			return;

		for(L2PcInstance player : getAllPlayers(6144))
			player.sendPacket(new NpcInfo(this, player));
	}

	public L2CharacterAI getAI() 
	{
		return setAI(new L2CastleGuardAI(new AIAccessor()));
	}

	public void setClanId (L2Clan clan){
		_clanid = clan.getClanId();
	}
	public int getClanId (){
		return _clanid;
	}
	public int getMagery(){
		return magery;
	}
	public void setGroup (String group){
		_group = group;
	}
	public String getGroup (){
		return _group;
	}
	
	public void say(int n)
	{
		HashMap<Integer, String[]> texts = null;
		L2Clan clan = ClanTable.getInstance().getClan(getClanId());
		String[] text = {"Ready to serve, My Lord", "I'm here to protect the life of sir " + clan.getLeaderName(), "I'll obey the orders of the " + clan.getName() + " clan"};
		texts.put(JUST_HIRED,text);
		String[] text2 = {"Go Go Go", "For the honor of the "+ clan.getName()};
		texts.put(ATTACK, text2);
		
		text = texts.get(n);
		
		CreatureSay cs = new CreatureSay(getObjectId(), 0, getName(), text[Rnd.get(text.length)]);
		for(L2PcInstance player : getAllPlayers(1250))
			player.sendPacket(cs);
	}

	public synchronized void doDie(L2Character killer) {
		_decayTask = ClientScheduler.getInstance().scheduleLow( new DecayTask(), 180000);
		
		super.doDie(killer);
	}
	public L2NpcTemplate setUpTemplate(L2NpcTemplate template){
		StatsSet npcDat = new StatsSet(); 
		int id = template.npcId;
		npcDat.set("npcId", id);
		int level = template.level;
		npcDat.set("level", level);
		npcDat.set("baseShldDef", 0);
		npcDat.set("baseShldRate", 0);
		npcDat.set("baseCritRate",  38);

		npcDat.set("name", template.name);
		//npcDat.set("name", "");
		npcDat.set("collision_radius", template.collisionRadius);
		npcDat.set("collision_height", template.collisionHeight);
		npcDat.set("sex", template.sex);
		npcDat.set("type", template.type);
		npcDat.set("baseAtkRange", template.baseAtkRange);
		npcDat.set("revardExp", template.revardExp);
		npcDat.set("revardSp", template.revardSp);
		npcDat.set("basePAtkSpd", pspd/1000*template.basePAtkSpd);
		npcDat.set("baseMAtkSpd", mspd/1000*template.baseMAtkSpd);
		npcDat.set("aggroRange", template.aggroRange);
		npcDat.set("rhand", template.rhand);
		npcDat.set("lhand", template.lhand);
		npcDat.set("armor", template.armor);
		npcDat.set("baseRunSpd", template.baseRunSpd);

		npcDat.set("baseHpReg", 0.01);
		npcDat.set("baseMpReg", 0.01);

		// constants, until we have stats in DB
		npcDat.set("baseSTR", 30);
		npcDat.set("baseCON", 30);
		npcDat.set("baseDEX", 30);
		npcDat.set("baseINT", 30);
		npcDat.set("baseWIT", 30);
		npcDat.set("baseMEN", 30);

		npcDat.set("baseHpMax", hp/1000*template.baseHpMax);
		npcDat.set("baseMpMax", mp/1000*template.baseMpMax);
		npcDat.set("basePAtk", patk/1000*template.basePAtk);
		npcDat.set("basePDef", pdef/1000*template.basePDef);
		npcDat.set("baseMAtk", matk/1000*template.baseMAtk);
		npcDat.set("baseMDef", mdef/1000*template.baseMDef);

		npcDat.set("factionId", template.factionId);
		npcDat.set("factionRange", template.factionRange);
		
		npcDat.set("isUndead", template.isUndead);

		L2NpcTemplate newtemplate = new L2NpcTemplate(npcDat);

		return newtemplate;
	}
	public void restoreFromDB(){
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;

			statement = con.prepareStatement("SELECT object_id, x, y, z, clan_id, hp, mp, patk, pspd, pdef, matk, mspd, mdef, magery, name, hpcontrol, mpcontrol, patkcontrol, pspdcontrol, pdefcontrol, matkcontrol, mspdcontrol, mdefcontrol, magerycontrol, template_id, group FROM castle_guards WHERE object_id=?");
			statement.setInt(1,getObjectId());
			ResultSet rset = statement.executeQuery();
			if(rset.next()){
				x = rset.getInt("x");
				y = rset.getInt("y");
				z = rset.getInt("z");
				_clanid = rset.getInt("clan_id");
				_group = rset.getString("group");
				hp = rset.getInt("hp");
				mp = rset.getInt("mp");
				patk = rset.getInt("patk");
				pspd = rset.getInt("pspd");
				pdef = rset.getInt("pdef");
				matk = rset.getInt("matk");
				mspd = rset.getInt("mspd");
				mdef = rset.getInt("mdef");
				magery = rset.getInt("magery");
				name = rset.getString("name");
				templateid = rset.getInt("template_id");
				statement = con.prepareStatement( "SELECT npcid, skillid, level FROM castle_guards_skills WHERE npcid="+getObjectId());
				rset = statement.executeQuery();
				while(rset.next()){ //restore skills
					L2Skill skill = SkillTable.getInstance().getInfo(rset.getInt("skillid"), rset.getInt("level"));
					super.addSkill(skill);
					
				}
			}
				
		  
		}
		catch (Exception e)
		{
			_log.warning("could not restore castle guard data:"+e);
		} 
		finally 
		{
			try { con.close(); } catch (Exception e) {}
		}
	}
	public void store(){
		  java.sql.Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement;

				statement = con.prepareStatement("SELECT object_id from castle_guards WHERE object_id=?");
				statement.setInt(1,getObjectId());
				ResultSet rset = statement.executeQuery();
				if(rset.next()){
					statement = con.prepareStatement("UPDATE castle_guards SET object_id=?, x=?, y=?, z=?, clan_id=?, hp=?, mp=?, patk=?, pspd=?, pdef=?, matk=?, mspd=?, mdef=?, magery=?, name=?, hpcontrol=?, mpcontrol=?, patkcontrol=?, pspdcontrol=?, pdefcontrol=?, matkcontrol=?, mspdcontrol=?, mdefcontrol=?, magerycontrol=? WHERE object_id=?");
					statement.setInt(25, getObjectId());
				} else {
					statement = con.prepareStatement("INSERT INTO castle_guards (object_id,x,y,z,clan_id,hp,mp,patk,pspd,pdef,matk,mspd,mdef,magery,name,hpcontrol,mpcontrol,patkcontrol,pspdcontrol,pdefcontrol,matkcontrol,mspdcontrol,mdefcontrol,magerycontrol,template_id,group) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
					statement.setInt(25, templateid);
					statement.setString(26, _group);
				}
				statement.setInt(1, getObjectId());
				statement.setInt(2, getX());
				statement.setInt(3, getY());
				statement.setInt(4, getZ());
				statement.setInt(5, getClanId());
				statement.setInt(6, hp);
				statement.setInt(7, mp);
				statement.setInt(8, patk);
				statement.setInt(9, pspd);
				statement.setInt(10, pdef);
				statement.setInt(11, matk);
				statement.setInt(12, mspd);
				statement.setInt(13, mdef);
				statement.setInt(14, magery);
				statement.setString(15, name);
				statement.setBoolean(16, hpcontrol);
				statement.setBoolean(17, mpcontrol);
				statement.setBoolean(18, patkcontrol);
				statement.setBoolean(19, pspdcontrol);
				statement.setBoolean(20, pdefcontrol);
				statement.setBoolean(21, matkcontrol);
				statement.setBoolean(22, mspdcontrol);
				statement.setBoolean(23, mdefcontrol);
				statement.setBoolean(24, magerycontrol);
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.warning("could not store castle guard data:"+e);
			} 
			finally 
			{
				try { con.close(); } catch (Exception e) {}
			}
	
	}
	@SuppressWarnings("static-access")
	public void onActionShift(ClientThread client)
	{
		L2PcInstance player = client.getActiveChar();

		if(GameServer.events.onActionShift(player, this))
			return;
		
		if (player.getAccessLevel() >= Config.GM_ACCESSLEVEL||(player.getClanId()==getClanId()&&(player.getClanPrivileges()>=Config.CG_VIEWSTATS||player.isClanLeader())))
		{
			player.setTarget(this);
			MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
			player.sendPacket(my);
			
			if (isAutoAttackable(player))
			{	
				StatusUpdate su = new StatusUpdate(getObjectId());
				su.addAttribute(StatusUpdate.CUR_HP, (int)getCurrentHp() );
				su.addAttribute(StatusUpdate.MAX_HP, getMaxHp() );
				player.sendPacket(su);
			}
			
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			StringBuffer html1 = new StringBuffer("<html><body bgcolor=#000000>");
//			String className = getClass().getName().substring(28);
			html1.append("<center><h1>"+getName()+"</h1></center>");
			html1.append("<table border=0 width=\"100%\">");
			html1.append("<tr><td>Anatomy</td><td>"+hp/10+"."+hp%1000+"</td>");
			if(hpcontrol) html1.append("<td><button value=\"down\" action=\"bypass -h forth_tooglehp\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
			else html1.append("<td><button value=\"up\" action=\"bypass -h forth_tooglehp\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
			html1.append("<tr><td>Wisdom</td><td>"+mp/10+"."+mp%1000+"</td>");
			if(mpcontrol) html1.append("<td><button value=\"down\" action=\"bypass -h forth_tooglemp\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
			else html1.append("<td><button value=\"up\" action=\"bypass -h forth_tooglemp\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
			html1.append("<tr><td>Weapon skill</td><td>"+patk/10+"."+patk%1000+"</td>");
			if(hpcontrol) html1.append("<td><button value=\"down\" action=\"bypass -h forth_tooglepath\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
			else html1.append("<td><button value=\"up\" action=\"bypass -h forth_tooglepatk\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
			html1.append("<tr><td>Armor</td><td>"+pdef/10+"."+pdef%1000+"</td>");
			if(pdefcontrol) html1.append("<td><button value=\"down\" action=\"bypass -h forth_tooglepdef\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
			else html1.append("<td><button value=\"up\" action=\"bypass -h forth_tooglepdef\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
			html1.append("<tr><td>Dextery</td><td>"+pspd/10+"."+pspd%1000+"</td>");
			if(pspdcontrol) html1.append("<td><button value=\"down\" action=\"bypass -h forth_tooglepspd\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
			else html1.append("<td><button value=\"up\" action=\"bypass -h forth_tooglepspd\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
			html1.append("<tr><td>Magic Power</td><td>"+matk/10+"."+matk%1000+"</td>");
			if(matkcontrol) html1.append("<td><button value=\"down\" action=\"bypass -h forth_tooglematk\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
			else html1.append("<td><button value=\"up\" action=\"bypass -h forth_tooglematk\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
			html1.append("<tr><td>Casting Speed</td><td>"+mspd/10+"."+mspd%1000+"</td>");
			if(mspdcontrol) html1.append("<td><button value=\"down\" action=\"bypass -h forth_tooglemspd\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
			else html1.append("<td><button value=\"up\" action=\"bypass -h forth_tooglemspd\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
			html1.append("<tr><td>Resisting Spells</td><td>"+mdef/10+"."+mdef%1000+"</td>");
			if(mdefcontrol) html1.append("<td><button value=\"down\" action=\"bypass -h forth_tooglemdef\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
			else html1.append("<td><button value=\"up\" action=\"bypass -h forth_tooglemdef\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
			html1.append("<tr><td>Magery</td><td>"+magery/10+"."+magery%1000+"</td>");
			if(magerycontrol) html1.append("<td><button value=\"down\" action=\"bypass -h forth_tooglemagery\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
			else html1.append("<td><button value=\"up\" action=\"bypass -h forth_tooglemagery\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
			html1.append("</table>");
			
			html.setHtml(html1.toString());
			player.sendPacket(html);
		}
	}
	public void statsUp(int damage, int level)
	{
		 int n = Rnd.get((400*70)/(damage*level*Config.CG_STATUPPROB));
		 if(n==0) 
		 	doStatsUp();
	}

	public void doStatsUp()
	{
		int i=0,j=-1;
		if(hpcontrol) i++;
		if(mpcontrol) i++;
		if(patkcontrol) i++;
		if(pspdcontrol) i++;
		if(pdefcontrol) i++;
		if(matkcontrol) i++;
		if(mspdcontrol) i++;
		if(mdefcontrol) i++;
		if(magerycontrol) i++;
		if(i>=0) 
			j = Rnd.get(i-1);
		if(hpcontrol)
		{
			if(j==0) hp+=Config.CG_STATUPGAIN;
			i--;
		}
		if(mpcontrol)
		{
			if(j==0) mp+=Config.CG_STATUPGAIN;
			i--;
		}
		if(patkcontrol)
		{
			if(j==0) patk+=Config.CG_STATUPGAIN;
			i--;
		}
		if(pspdcontrol){
			if(j==0) pspd+=Config.CG_STATUPGAIN;
			i--;
		}
		if(pdefcontrol){
			if(j==0) pdef+=Config.CG_STATUPGAIN;
			i--;
		}
		if(matkcontrol){
			if(j==0) matk+=Config.CG_STATUPGAIN;
			i--;
		}
		if(mspdcontrol){
			if(j==0) mspd+=Config.CG_STATUPGAIN;
			i--;
		}
		if(mdefcontrol){
			if(j==0) mdef+=Config.CG_STATUPGAIN;
			i--;
		}
		if(magerycontrol){
			if(j==0) magery+=Config.CG_STATUPGAIN;
			i--;
		}
	}
	public L2Skill addSkill(L2Skill newSkill)
	{
		L2Skill oldSkill = super.addSkill(newSkill);
		storeSkill(newSkill, oldSkill);
		return oldSkill;
	}
	public void storeSkill(L2Skill newSkill, L2Skill oldSkill){
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;

			if(oldSkill!=null){
				statement = con.prepareStatement("UPDATE castle_guards_skills SET npcid=?, skillid=?, level=? WHERE npcid=?");
				statement.setInt(4, getObjectId());
			} else {
				statement = con.prepareStatement("INSERT INTO castle_guards_skills (npcid, skillid, level) VALUES (?,?,?)");
			}
			statement.setInt(1,getObjectId());
			statement.setInt(2,newSkill.getId());
			statement.setInt(3,newSkill.getLevel());
			statement.execute();
		}
		catch (Exception e)
		{
			_log.warning("could not store castle guard skill:"+e);
		} 
		finally 
		{
			try { con.close(); } catch (Exception e) {}
		}
		
	}
	public void maybeLearnSkill(L2PcInstance player, L2Skill skill)
	{
		if(player.getClanId()==getClanId())
		{
			int n = Rnd.get(1000/magery-1);
			if(n==0) addSkill(skill);
		}
		
	}


}