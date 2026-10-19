package net.sf.l2j.gameserver.lib;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

import net.sf.l2j.L2DatabaseFactory;

public class mysql
{
	private static Logger _log = Logger.getLogger(mysql.class.getName());
	private static java.sql.Connection _dbh;
	
	private static final mysql _instance = new mysql();
	
	public static mysql getInstance()
	{
		return _instance;
	}

	private mysql()
	{
		_dbh = null;
		try
		{
			_dbh = L2DatabaseFactory.getInstance().getConnection();
		}
		catch(Exception e)
		{
			_log.warning("DB connection error:"+e);
		}
	}

	public static Integer simple_get_int(String ret_field, String table, String where)
	{
		String query = "SELECT `"+ret_field+"` FROM `"+table+"` WHERE "+where+" LIMIT 1;";

		Integer res = null;
		
		PreparedStatement statement = null;
		ResultSet rset = null;

		try
		{
			statement = _dbh.prepareStatement(query);
			rset = statement.executeQuery();
		
			if(rset.next())
				res = rset.getInt(1);
		}
		catch(Exception e)
		{
			_log.warning("mSGI: Error in query '"+query+"':"+e);
			e.printStackTrace();
		}
		finally
		{
			try{ rset.close();  } catch(Exception e) {}
			try{ statement.close(); } catch(Exception e) {}
		}

		return res;
	}

	public static Integer[][] simple_get_int_array(String[] ret_fields, String table, String where)
	{
		long start = System.currentTimeMillis();

		String fields = null;
		for(String field : ret_fields)
		{
			if(fields != null)
			{
				fields += ",";
			   	fields += "`"+field+"`";
		   	}
		   	else
			   	fields = "`"+field+"`";
		}

		String query = "SELECT "+fields+" FROM `"+table+"` WHERE "+where;

		PreparedStatement statement = null;
		ResultSet rset = null;

		Integer res[][] = null;

		try
		{
			statement = _dbh.prepareStatement(query);
			rset = statement.executeQuery();

			int rows = 0;
			while(rset.next())
				rows++;

			res = new Integer[rows-1][ret_fields.length];

			rset.first();

			int row = 0;
			while(rset.next())
			{
				for(int i=0; i<ret_fields.length; i++)
			 		res[row][i] = rset.getInt(i+1);
				row++;
			}
		}
		catch(Exception e)
		{
			_log.warning("mSGIA: Error in query '"+query+"':"+e);
			e.printStackTrace();
		}
		finally
		{
			try{ rset.close();  } catch(Exception e) {}
			try{ statement.close(); } catch(Exception e) {}
		}

		_log.fine("Get all rows in query '"+query+"' in "+(System.currentTimeMillis()-start)+"ms");
		return res;
	}
}
