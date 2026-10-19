package net.sf.l2j.extensions.jbforth;

import java.security.MessageDigest;
import java.util.logging.Logger;

import net.sf.l2j.Base64;
import ru.balancer.JBForth;
import ru.balancer.jbforth.Dict;
import ru.balancer.jbforth.Word;

public class player
{
	private static final String SHA = "SHA";
	static Logger _log = Logger.getLogger(player.class.getName());

    public player(Dict d)
    {
        d.reg(">password_hash", new to_password_hash());
    }

    public class to_password_hash extends Word
    {
//    	( "password" -- "password_hash" | null ) 
        public void exec(JBForth jbf) 
        { 
        	String res = null;
			try
			{			
				MessageDigest md = MessageDigest.getInstance(SHA);
				byte[] raw = jbf.st.pop_s().getBytes("UTF-8");
				byte[] hash = md.digest(raw);
				res = Base64.encodeBytes(hash);
			}
			catch (Exception e) { _log.warning("could not get password:"+e); res = null; } 
			jbf.st.push(res);
		}
    }
}
