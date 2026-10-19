package net.sf.l2j.extensions.jbforth;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import ru.balancer.JBForth;
import ru.balancer.jbforth.Dict;
import ru.balancer.jbforth.Word;

public class sql
{
    static Logger _log = Logger.getLogger(sql.class.getName());

    public sql(Dict d)
    {
        d.reg("QUERY", new query());
        d.reg("UPDATE", new update());
    }

    public class update extends Word
    {
        public void exec(JBForth jbf)
        { 
            // ( "query" -- changed_rows 0 | error_text )
            String query = (String)jbf.st.pop();

            int depth = jbf.st.depth();

            if(Config.DEBUG)
				_log.info("Update query '"+query+"' ... ");

            java.sql.Connection dbh = null;
            PreparedStatement statement = null;

            try
            {
                dbh = L2DatabaseFactory.getInstance().getConnection();

                statement = dbh.prepareStatement(query);
                jbf.st.push(statement.executeUpdate());
            }
            catch(Exception e)
            {
                System.out.println("Depth="+depth);
                try
                {
                	jbf.st.setDepth(depth); // Drop any added data
	                if((Integer)jbf.warning.get() != 0)
    	            {
        	            _log.warning("JBFSQL: Error in query '"+query+"':"+e);
            	        e.printStackTrace();
                	}
	                jbf.st.push(""+e);
                }
                catch(Exception e2)
                {
                   	printStackTrace(jbf, e2);
                }

                return;
            }
           
            finally
            {
                try{ statement.close(); } catch(Exception e) {}
                try{ dbh.close();  } catch(Exception e) {}
            }

            jbf.st.push(0);
        }
    }

    public class query extends Word
    {
        public void exec(JBForth jbf)
        { 
// 			query ( "query" -- list-of-hashmaps false | errno )
            String query = (String)jbf.st.pop();

            int depth = jbf.st.depth();

            if(Config.DEBUG)
				_log.info("Query '"+query+"' ... ");

            java.sql.Connection dbh = null;
            PreparedStatement statement = null;
            ResultSet rset = null;

            try
            {
                dbh = L2DatabaseFactory.getInstance().getConnection();

                statement = dbh.prepareStatement(query);
                rset = statement.executeQuery();
            
                ResultSetMetaData md = rset.getMetaData();

                ArrayList<ConcurrentHashMap<String,Object>> res = new ArrayList<ConcurrentHashMap<String,Object>>();

                while(rset.next())
                {
                    ConcurrentHashMap<String,Object> tmp = new ConcurrentHashMap<String,Object>();
                    for(int i=0; i<md.getColumnCount(); i++)
                        tmp.put(md.getColumnName(i+1), rset.getObject(i+1));
                    
                    res.add(tmp);
                }
				
				jbf.st.push(res);
            }
            catch(Exception e)
            {
                try
                {
	                jbf.st.setDepth(depth); // Drop any added data
    	            if((Integer)jbf.warning.get() != 0)
        	        {
            	        _log.warning("JBFSQL: Error in query '"+query+"':"+e);
                	    e.printStackTrace();
		                jbf.st.push(""+e);
	                }
                }
                catch(Exception e2)
                {
                   	printStackTrace(jbf, e2);
                }

                return;
            }
            finally
            {
                try{ rset.close();  } catch(Exception e) {}
                try{ statement.close(); } catch(Exception e) {}
                try{ dbh.close();  } catch(Exception e) {}
            }

            jbf.st.push(0);
        }
    }
}
