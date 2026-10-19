package net.sf.l2j.gameserver.lib.jbf.words;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.logging.Logger;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.lib.jbf.JBData;

public class SQL
{
    static Logger _log = Logger.getLogger(SQL.class.getName());

    public SQL(Dict d)
    {
        d.reg("QUERY", new query());
        d.reg("UPDATE", new update());
    }

    public class update extends Word
    {
        public void exec(JBData data)
        { 
            // ( "query" -- )
            String query = (String)data.st.pop();

            int depth = data.st.depth();

            System.out.println("Update query '"+query+"' ... ");

            java.sql.Connection dbh = null;
            PreparedStatement statement = null;

            try
            {
                dbh = L2DatabaseFactory.getInstance().getConnection();

                statement = dbh.prepareStatement(query);
                int rows = statement.executeUpdate();
            
                data.st.push(rows);
            }
            catch(Exception e)
            {
                data.st.setDepth(depth); // Drop any added data
                if((Integer)data.warning.get() != 0)
                {
                    _log.warning("JBFSQL: Error in query '"+query+"':"+e);
                    e.printStackTrace();
                }
                data.st.push(""+e);
                return;
            }
           
            finally
            {
                try{ statement.close(); } catch(Exception e) {}
                try{ dbh.close();  } catch(Exception e) {}
            }

            data.st.push(0);
        }
    }

    public class query extends Word
    {
        public void exec(JBData data)
        { 
            // ( "query" -- )
            String query = (String)data.st.pop();

            int depth = data.st.depth();

            System.out.println("Query '"+query+"' ... ");

            java.sql.Connection dbh = null;
            PreparedStatement statement = null;
            ResultSet rset = null;

            int rows = 0;

            try
            {
                dbh = L2DatabaseFactory.getInstance().getConnection();

                statement = dbh.prepareStatement(query);
                rset = statement.executeQuery();
            
                ResultSetMetaData md = rset.getMetaData();

                while(rset.next())
                {
                    HashMap<String,Object> tmp = new HashMap<String,Object>();
                    for(int i=0; i<md.getColumnCount(); i++)
                        tmp.put(md.getColumnName(i+1), rset.getObject(i+1));
                    
                    data.st.push(tmp);

                    rows++;
                }

                data.st.push(rows);
            }
            catch(Exception e)
            {
                data.st.setDepth(depth); // Drop any added data
                if((Integer)data.warning.get() != 0)
                {
                    _log.warning("JBFSQL: Error in query '"+query+"':"+e);
                    e.printStackTrace();
                }
                data.st.push(""+e);
                return;
            }
            finally
            {
                try{ rset.close();  } catch(Exception e) {}
                try{ statement.close(); } catch(Exception e) {}
                try{ dbh.close();  } catch(Exception e) {}
            }

            data.st.push(0);
        }
    }
}
