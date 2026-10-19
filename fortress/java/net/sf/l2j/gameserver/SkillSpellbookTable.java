package net.sf.l2j.gameserver;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.L2DatabaseFactory;

public class SkillSpellbookTable
{
	private static Logger _log = Logger.getLogger(SkillTreeTable.class.getName());
	private static final SkillSpellbookTable _instance = new SkillSpellbookTable();

	public static HashMap<Integer, Integer> _skillSpellbooks;

	public static SkillSpellbookTable getInstance()
	{
		return _instance;
	}

	private SkillSpellbookTable()
	{
		_skillSpellbooks = new HashMap<Integer, Integer>();
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement( "SELECT skill_id, item_id FROM skill_spellbooks" );
			ResultSet spbooks = statement.executeQuery();

			while ( spbooks.next() )
			{
				_skillSpellbooks.put( spbooks.getInt( "skill_id" ) , spbooks.getInt( "item_id" ) );
			}

			spbooks.close();
			statement.close();
			con.close();
			_log.config("SkillSpellbookTable: Loaded " + _skillSpellbooks.size() + " Spellbooks.");		
		}
		catch ( Exception e )
		{
			_log.log(Level.WARNING, "error while loading spellbooks  ", e);
		}
		finally
		{
			try
			{
				con.close(); } catch ( Exception e ) {}
		}
	}

}
