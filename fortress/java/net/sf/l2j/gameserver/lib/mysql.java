/* 
	coded by Balancer
	balancer@balancer.ru
	http://balancer.ru

	version 0.2, 2005-11-04
	version 0.1, 2005-04-16
*/

package net.sf.l2j.gameserver.lib;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;

public class mysql
{
	private static Logger _log = Logger.getLogger(mysql.class.getName());

	public static final void set(String query)
	{
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(query);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("Could not execute update '"+query+"': "+e);
			Thread.dumpStack();
		} 
		finally 
		{
			try { con.close(); } catch (Exception e) {}
		}
	}

	public static final Object get(String query)
	{
		Object ret = null;
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(query+" LIMIT 1");
			ResultSet rset = statement.executeQuery();
			ResultSetMetaData md = rset.getMetaData();

			rset.next();
			if(md.getColumnCount()>1)
			{
				ConcurrentHashMap<String,Object> tmp = new ConcurrentHashMap<String,Object>();
				for(int i=0; i<md.getColumnCount(); i++)
					tmp.put(md.getColumnName(i+1), rset.getObject(i+1));
				ret = tmp;
			}
			else
				ret = rset.getObject(1);

		}
		catch (Exception e)
		{
			_log.warning("Could not execute query '"+query+"': "+e);
			Thread.dumpStack();
		} 
		finally 
		{
			try { con.close(); } catch (Exception e) {}
		}
		return ret;
	}

	public static final ArrayList get_array(String query)
	{
		ArrayList<Object> ret = new ArrayList<Object>();
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(query);
			ResultSet rset = statement.executeQuery();
			ResultSetMetaData md = rset.getMetaData();

			while(rset.next())
			{
				if(md.getColumnCount()>1)
				{
					ConcurrentHashMap<String,Object> tmp = new ConcurrentHashMap<String,Object>();
					for(int i=0; i<md.getColumnCount(); i++)
						tmp.put(md.getColumnName(i+1), rset.getObject(i+1));
					ret.add(tmp);
				}
				else
				{
					ret.add(rset.getObject(1));
				}
			}
		}
		catch (Exception e)
		{
			_log.warning("Could not execute query '"+query+"': "+e);
			Thread.dumpStack();
		} 
		finally 
		{
			try { con.close(); } catch (Exception e) {}
		}
		return ret;
	}


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

	public static Integer simple_get_int(String ret_field, String table)
	{
		return simple_get_int(ret_field, table, "1");
	}

	public static int simple_get_int(String ret_field, String table, String where)
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

		if (res != null || (!Config.KILL_COUNTER && Config.DROP_COUNTER))
			{
				return res;
			}
		else
			{
				return 0;
			}
		
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

			ArrayList<Integer[]> al = new ArrayList<Integer[]>();
			int row = 0;
			while(rset.next())
			{
				Integer[] tmp = new Integer[ret_fields.length];
				for(int i=0; i<ret_fields.length; i++)
			 		tmp[i] = rset.getInt(i+1);
				al.add(row, tmp);
				row++;
		 	}

			res = al.toArray(new Integer[row][ret_fields.length]);
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
