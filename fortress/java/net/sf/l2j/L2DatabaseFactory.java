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
package net.sf.l2j;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.mchange.v2.c3p0.DataSources;
import com.mchange.v2.c3p0.PoolConfig;
import com.mchange.v2.c3p0.PooledDataSource;

public class L2DatabaseFactory
{
	private static L2DatabaseFactory _instance;
	private DataSource _source;
	
	static Logger _log = Logger.getLogger(L2DatabaseFactory.class.getName());

	public L2DatabaseFactory() throws SQLException
	{
		try
		{
			if (Config.DATABASE_MAX_CONNECTIONS < 2)
			{
                Config.DATABASE_MAX_CONNECTIONS = 2;
                _log.warning("at least " + Config.DATABASE_MAX_CONNECTIONS + " db connections are required.");
            }

			PoolConfig config = new PoolConfig();
			config.setAutoCommitOnClose(true);
			config.setInitialPoolSize(1);
			config.setMinPoolSize(1);
			config.setMaxPoolSize(Config.DATABASE_MAX_CONNECTIONS);
			config.setMaxStatements(100);
			config.setIdleConnectionTestPeriod(0); // test idle connection every 1 minute
			config.setMaxIdleTime(0); // remove unused connection after 10 minutes

			Class.forName(Config.DATABASE_DRIVER).newInstance();

			if (Config.DEBUG) _log.fine("Database Connection Working");

			DataSource unpooled = DataSources.unpooledDataSource(Config.DATABASE_URL, Config.DATABASE_LOGIN, Config.DATABASE_PASSWORD);
			_source = DataSources.pooledDataSource( unpooled, config);
			
			/* Test the connection */
			_source.getConnection().close();
		}
		catch (SQLException x)
		{
			if (Config.DEBUG) _log.fine("Database Connection FAILED");
			// rethrow the exception
			throw x;
		}
		catch (Exception e)
		{
			if (Config.DEBUG) _log.fine("Database Connection FAILED");
			throw new SQLException("could not init DB connection:"+e);
		}
	}

	public static L2DatabaseFactory getInstance() throws SQLException
	{
		if (_instance == null)
		{
			_instance = new L2DatabaseFactory();
		}
		return _instance;
	}
	
	public Connection getConnection() throws SQLException
	{
		return _source.getConnection();
	}
	
	public int getBusyConnectionCount() throws SQLException
	{
	    return ((PooledDataSource) _source).getNumBusyConnectionsDefaultUser();
	}

	public int getIdleConnectionCount() throws SQLException
	{
	    return ((PooledDataSource) _source).getNumIdleConnectionsDefaultUser();
	}
	
	public void shutdown()
	{
		try {
			((PooledDataSource) _source).close();
		} catch (SQLException e) {_log.log(Level.INFO, "", e);}
		try {
			DataSources.destroy(_source);
		} catch (SQLException e) {_log.log(Level.INFO, "", e);}
	}
}
