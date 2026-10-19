package events.PremiumNpc;

/** @author L2MarkuS
 *	L2MarkuS
 *	@date 16.18 | 10.12.2009
 */

import java.util.Calendar;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.instancemanager.QuestManager;
import net.sf.l2j.L2DatabaseFactory;

public class PremiumNpc extends Quest{

/* Здесь указан ид нпс*/
private int PremiumNpcId = 12260;

/* Ид итема, который будет потребляться для получения премиума*/
private int ConsumableItemId = 4037;

/* Количество вещей(ConsumableItemId), которые будут браться у персонажа
 * За один месяц Премиум Аккаунта  */
private int Count  = 30;

// Эту переменную не трогаем.
private int PremiumService;
	//Конструктор
	public PremiumNpc(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(PremiumNpcId);
		addFirstTalkId(PremiumNpcId);
	}

	@Override
	public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player){

		QuestState st = player.getQuestState("PremiumNpc");

		String htmltext = event;

		if (event.equalsIgnoreCase("getPremium")){
			htmltext = "getpremium.htm";
			}

		if (event.equalsIgnoreCase("what")){
			htmltext = "aboutpremium.htm";
			}

		if (event.equalsIgnoreCase("back")){
			htmltext = "start.htm";
			}

		if (event.equalsIgnoreCase("premium1")){
			getPS(player);
			if (PremiumService == 1){
				htmltext = "ladylike-no.htm";
				}
			else if (st.getQuestItemsCount(ConsumableItemId)>= Count * 1){
				st.takeItems(ConsumableItemId, Count * 1);
				addPremiumServices(1, player);
				htmltext = "congratulations1.htm";
				} else {
				htmltext = "sorry.htm";
				}
			}

		if (event.equalsIgnoreCase("premium2")){
			getPS(player);
			if (PremiumService == 1){
				htmltext = "ladylike-no.htm";
				}
			else if (st.getQuestItemsCount(ConsumableItemId)>= Count * 2){
				st.takeItems(ConsumableItemId, Count * 2);
				addPremiumServices(2, player);
				htmltext = "congratulations2.htm";
				} else {
				htmltext = "sorry.htm";
				}
			}

		if (event.equalsIgnoreCase("premium3")){
			getPS(player);
			if (PremiumService == 1){
				htmltext = "ladylike-no.htm";
				}
			else if (st.getQuestItemsCount(ConsumableItemId)>= Count * 3){
				st.takeItems(ConsumableItemId, Count * 3);
				addPremiumServices(3, player);
				htmltext = "congratulations3.htm";
				} else {
				htmltext = "sorry.htm";
				}
			}

		return htmltext;
		}

	@Override
	public String onFirstTalk(L2NpcInstance npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			Quest q = QuestManager.getInstance().getQuest(getName());
			st = q.newQuestState(player);
		}
		htmltext = "start.htm";
		return htmltext;
	}
	public static void main(String[] args)
	{	//Call Constructor
		new PremiumNpc(-1, "PremiumNpc", "events");
	}

    //To be Continue...
	private void getPS(L2PcInstance player)
	{
		Connection con = null;
		try
		{
		con = L2DatabaseFactory.getInstance().getConnection();
		PreparedStatement statement = con.prepareStatement("SELECT premium_service FROM character_premium WHERE account_name=?");
		statement.setString(1, player.getAccountName());
		ResultSet chars = statement.executeQuery();
		PremiumService = chars.getInt("premium_service");
		chars.close();
		statement.close();
		}
		catch (Exception e)
		{
		}
		finally
		{
			try
			{
				if (con != null)
					con.close();
			}
			catch (SQLException e)
			{
			}
		}
	}
	private void addPremiumServices(int Months, L2PcInstance player)
	{
		Connection con = null;
		try
		{
			Calendar finishtime = Calendar.getInstance();
			finishtime.setTimeInMillis(System.currentTimeMillis());
			finishtime.set(Calendar.SECOND, 0);
			finishtime.add(Calendar.MONTH, Months);

			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE character_premium SET premium_service=?, enddate=? WHERE account_name=?");
			statement.setInt(1, 1);
			statement.setLong(2, finishtime.getTimeInMillis());
			statement.setString(3, player.getAccountName());
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.info("EventPremiumNpc:  Could not increase data");
		}
		finally
		{
			try
			{
				if (con != null)
					con.close();
			}
			catch (SQLException e)
			{
			}
		}

	}
}
