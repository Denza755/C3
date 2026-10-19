package net.sf.l2j.gameserver.lib;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

import net.sf.l2j.L2DatabaseFactory;

public class MySqlUtils
{
	private static Logger _log = Logger.getLogger(MySqlUtils.class.getName());
	private static java.sql.Connection _dbh;
	
	private static final MySqlUtils _instance = new MySqlUtils();
	
	public static MySqlUtils getInstance()
	{
		return _instance;
	}

	private MySqlUtils()
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

	public static Integer getIntValue(String resultField, String tableName, String whereClause)
	{
		String query = "SELECT `"+resultField+"` FROM `"+tableName+"` WHERE "+whereClause+" LIMIT 1;";

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

    public static Integer[] getIntArray(String resultField, String tableName, String whereClause)
    {
        String query = "SELECT `"+resultField+"` FROM `"+tableName+"` WHERE "+whereClause+";";

        Integer[] res = null;
        
        PreparedStatement statement = null;
        ResultSet rset = null;

        try
        {
            statement = _dbh.prepareStatement(query);
            rset = statement.executeQuery();
            
            int rows = 0;
            
            while (rset.next())
            {
                rows++;
            }
            
            if (rows == 0)
                return null;

            res = new Integer[rows-1];

            rset.first();
        
            int row = 0;
            while (rset.next())
            {
                res[row] = rset.getInt(1);
            }
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

	public static Integer[][] get2DIntArray(String[] resultFields, String usedTables, String whereClause)
	{
		long start = System.currentTimeMillis();

		String fields = null;
		for(String field : resultFields)
		{
			if(fields != null)
			{
				fields += ",";
			   	fields += "`"+field+"`";
		   	}
		   	else
			   	fields = "`"+field+"`";
		}

		String query = "SELECT "+fields+" FROM `"+usedTables+"` WHERE "+whereClause;

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

			res = new Integer[rows-1][resultFields.length];

			rset.first();

			int row = 0;
			while(rset.next())
			{
				for(int i=0; i<resultFields.length; i++)
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
