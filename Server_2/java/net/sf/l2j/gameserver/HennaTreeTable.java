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
package net.sf.l2j.gameserver;



import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.ClassId;
import net.sf.l2j.gameserver.model.L2HennaInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.templates.L2Henna;
/**
 * This class ...
 * 
 * @version $Revision$ $Date$
 */
public class HennaTreeTable
{
	private static Logger _log = Logger.getLogger(HennaTreeTable.class.getName());
	private static final HennaTreeTable _instance = new HennaTreeTable();
	private HashMap<ClassId, ArrayList<L2HennaInstance>> _hennaTrees;
	private boolean _initialized = true;
	
	public static HennaTreeTable getInstance()
	{
		return _instance;
	}
	
	private HennaTreeTable()
	{
		_hennaTrees = new HashMap<ClassId, ArrayList<L2HennaInstance>>();
		int classId =0;
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM class_list ORDER BY id");
			ResultSet classlist = statement.executeQuery();
			ArrayList<L2HennaInstance> list = new ArrayList<L2HennaInstance>();
			//int parentClassId;
			//L2Henna henna;
			while (classlist.next())
			{
				list = new ArrayList<L2HennaInstance>();
				classId = classlist.getInt("id");
				PreparedStatement statement2 = con.prepareStatement("SELECT * FROM henna_trees where class_id=? ORDER BY symbol_id");
				statement2.setInt(1, classId);
				ResultSet hennatree = statement2.executeQuery();


				while (hennatree.next())
				{
					int id = hennatree.getInt("symbol_id");
					//String name = hennatree.getString("name");
					L2Henna template = HennaTable.getInstance().getTemplate(id);
			    	L2HennaInstance temp = new L2HennaInstance(template);
					temp.setSymbolId(id);
					temp.setItemIdDye(template.getDyeId());
					temp.setAmountDyeRequire(template.getAmountDyeRequire());
					temp.setPrice(template.getPrice());
					temp.setStatINT(template.getStatINT());
					temp.setStatSTR(template.getStatSTR());
					temp.setStatCON(template.getStatCON());
					temp.setStatMEM(template.getStatMEM());
					temp.setStatDEX(template.getStatDEX());
					temp.setStatWIT(template.getStatWIT());
					
					list.add(temp);
				}
				_hennaTrees.put(ClassId.values()[classId], list);
				hennatree.close();
				statement2.close();
				_log.config("henna tree for class " + classId + " has " + list.size() + " hennaInfo");		
			}
			
			classlist.close();
			statement.close();
			
			
		}
		catch (Exception e)
		{
			_log.warning("error while creating henna tree for classId "+classId + "  "+e);
			e.printStackTrace();
		} 
		finally 
		{
			try { con.close(); } catch (Exception e) {}
		}

		
		
	}
	
	
	
	public L2HennaInstance[] getAvailableHenna(L2PcInstance cha, ClassId classId)
	{
		ArrayList<L2HennaInstance> result = new ArrayList<L2HennaInstance>();
		ArrayList<L2HennaInstance> henna = _hennaTrees.get(classId);
		if (henna == null)
		{
			// the hennatree for this class is undefined, so we give an empty list
			_log.warning("Hennatree for class " + classId + " is not defined !");
			return new L2HennaInstance[0];
		}
		
		
		for (int i = 0; i < henna.size(); i++)
		{
			L2HennaInstance temp = henna.get(i);
			result.add(temp);
		}
		
		return result.toArray(new L2HennaInstance[result.size()]);
	}
	
	public boolean isInitialized()
	{
		return _initialized;
	}
	
}
