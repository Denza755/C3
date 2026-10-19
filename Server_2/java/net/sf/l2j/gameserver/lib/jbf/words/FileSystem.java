package net.sf.l2j.gameserver.lib.jbf.words;

import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.lib.jbf.JBData;

public class FileSystem
{
    static Logger _log = Logger.getLogger(FileSystem.class.getName());

	public static void init(Dict d)
	{
		d.reg("INCLUDED", new included());
	}

	public static class included extends Word
	{
        public void exec(JBData data) 
		{ 
            String fname = (String)data.st.pop();

            System.out.println("Include '"+fname+"' ... ");

			File file = new File(Config.DATAPACK_ROOT, fname);

			if(file.exists())
			{
				FileInputStream fis = null;
				try
				{
					fis = new FileInputStream(file);
					byte[] raw = new byte[fis.available()];
					fis.read(raw);
			
					String content = new String(raw, "UTF-8");
                    jbc.exec(content, data);
				}
				catch (Exception e)
				{
					// problem with adminhelp is ignored
				}		
				finally
				{
					try { fis.close(); }
					catch (Exception e1) { /* problems ignored */ }
				}

			}
			else
			{
                _log.config("Include file '"+fname+"' doesn't exist");
			}
		}
	}
}
