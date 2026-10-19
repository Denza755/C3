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
package net.sf.l2j.gameserver.script.faenor;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.script.Parser;
import net.sf.l2j.gameserver.script.ParserNotCreatedException;
import net.sf.l2j.gameserver.script.ScriptDocument;
import net.sf.l2j.gameserver.script.ScriptEngine;
import net.sf.l2j.gameserver.script.ScriptPackage;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.w3c.dom.Node;

/**
 * @author Luis Arias
 *
 */
public class FaenorScriptEngine extends ScriptEngine
{
    public final static String PACKAGE_DIRECTORY = "data/script/";
    public final static boolean DEBUG = true;
    
    private LinkedList<ScriptDocument> scripts;
    
    public FaenorScriptEngine()
    {
        scripts         = new LinkedList<ScriptDocument>();
        loadPackages();
        parsePackages();
        
    }
    
    public void reloadPackages()
    {
        scripts         = new LinkedList<ScriptDocument>();
        parsePackages();
    }
    
    private void loadPackages()
    {
        File packDirectory = new File(Config.DATAPACK_ROOT, PACKAGE_DIRECTORY);//System.out.println(packDirectory.getAbsolutePath());
        
        FileFilter fileFilter = new FileFilter() {
            public boolean accept(File file) {
                return file.getName().endsWith(".zip");
            }
        };
        
        File[] files = packDirectory.listFiles(fileFilter);
        if (files == null) return;
        ZipFile zipPack;
        
        for (int i=0; i<files.length; i++)
        {
            try {
                zipPack = new ZipFile(files[i]);
            } catch (ZipException e) {
                e.printStackTrace();
                continue;
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            
            ScriptPackage module = new ScriptPackage(zipPack);

            List<ScriptDocument> scripts = module.getScriptFiles();
            for (ScriptDocument script : scripts)
            {
                this.scripts.add(script);
            }

        }
        /*for (ScriptDocument script : scripts)
        {
            System.out.println("Script: "+script);
        }
        System.out.println("Sorting");
        orderScripts();
        for (ScriptDocument script : scripts)
        {
            System.out.println("Script: "+script);
        }*/
    }
    
    public void orderScripts()
    {
        if (scripts.size() > 1)
        {
            //ScriptDocument npcInfo = null;
            
            for (int i=0; i<scripts.size();)
            {
                if (scripts.get(i).getName().contains("NpcStatData"))
                {
                    scripts.addFirst(scripts.remove(i));
                    //scripts.set(i, scripts.get(0));
                    //scripts.set(0, npcInfo);
                }
                else
                {
                    i++;
                }
            }
        }
    }
    
    public void parsePackages()
    {
		BSFManager context = new BSFManager();
		try {
			context.eval("beanshell", "core", 0, 0,
					"double log1p(double d) { return Math.log1p(d); }");
			context.eval("beanshell", "core", 0, 0,
					"double pow(double d, double p) { return Math.pow(d,p); }");

        for (ScriptDocument script : scripts)
	            parseScript(script, context);
			
		} catch (BSFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
        }
    }

    public void parseScript(ScriptDocument script, BSFManager context)
    {
        if (DEBUG) System.out.println("Parsing Script: "+script.getName());
        
        Node node = script.getDocument().getFirstChild();
        String parserClass = "faenor.Faenor"+node.getNodeName()+"Parser";
        
        Parser parser = null;
        try
        {
            parser = createParser(parserClass);
        } catch (ParserNotCreatedException e1)
        {
            System.err.println("ERROR: No parser registered for Script: "+parserClass);
            e1.printStackTrace();
        }
        
        if (parser == null)
        {
            System.err.println("Unknown Script Type: "+script.getName());
            return;
        }
        
        try
        {
            parser.parseScript(node, context);
            System.out.println("Script Sucessfullty Parsed.");
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
            System.out.println("Script Parsing Failed.");
        }
    }
    
    public String toString()
    {
        if (scripts.isEmpty())
            return "No Packages Loaded.";
        
        String out = "Script Packages currently loaded:\n";
        
        for (ScriptDocument script : scripts)
        {
            out += script;
        }
        return out;
    }
}
