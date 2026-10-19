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
package net.sf.l2j.gameserver.serverpackets;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.CharSelectInfoPackage;
import net.sf.l2j.gameserver.model.Inventory;

/**
 * This class ...
 * 
 * @version $Revision: 1.8.2.4.2.6 $ $Date: 2005/04/06 16:13:46 $
 */
public class CharSelectInfo extends ServerBasePacket
{
    // d SdSddddddddddffddddddddddddddddddddddddddddddddddddddddddddddffd
    private static final String _S__1F_CHARSELECTINFO = "[S] 1F CharSelectInfo";

    private static Logger _log = Logger.getLogger(CharSelectInfo.class.getName());

    private String _loginName;

    private int _sessionId;

    private CharSelectInfoPackage[] _characterPackages;

    /**
     * @param _characters
     */
    public CharSelectInfo(String loginName, int sessionId)
    {
        _sessionId = sessionId;
        _loginName = loginName;
        _characterPackages = loadCharacterSelectInfo();
    }

    public CharSelectInfoPackage[] getCharInfo()
    {
        return _characterPackages;
    }
    
	final void runImpl()
	{
		// no long-running tasks
	}
	
	final void writeImpl()
    {
        int size = (_characterPackages.length);

        writeC(0x13);
        writeD(size);

        long lastAccess = 0L;
        int lastUsed = -1;
        for (int i = 0; i < size; i++)
        	if (lastAccess < _characterPackages[i].getLastAccess())
        	{
        		lastAccess = _characterPackages[i].getLastAccess();
        		lastUsed = i;
        	}
        for (int i = 0; i < size; i++)
        {
            CharSelectInfoPackage charInfoPackage = _characterPackages[i];

            writeS(charInfoPackage.getName());
            writeD(charInfoPackage.getCharId()); // ?
            writeS(_loginName);
            writeD(_sessionId);
            writeD(charInfoPackage.getClanId());
            writeD(0x00); // ??

            writeD(charInfoPackage.getSex());
            writeD(charInfoPackage.getRace());
            writeD(charInfoPackage.getClassId());
            writeD(0x01); // active ??

            writeD(0x00); // x
            writeD(0x00); // y
            writeD(0x00); // z

            writeF(charInfoPackage.getCurrentHp()); // hp cur
            writeF(charInfoPackage.getCurrentMp()); // mp cur

            writeD(charInfoPackage.getSp());
            writeD(charInfoPackage.getExp());
            writeD(charInfoPackage.getLevel());

            writeD(0x00);
            writeD(0x00);
            writeD(0x00);
            writeD(0x00);
            writeD(0x00);
            writeD(0x00);
            writeD(0x00);
            writeD(0x00);
            writeD(0x00);
            writeD(0x00);

            writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_UNDER));
            writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_REAR));
            writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_LEAR));
            writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_NECK));
            writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_RFINGER));
            writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_LFINGER));
            writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_HEAD));
            writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
            writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_LHAND));
            writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_GLOVES));
            writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_CHEST));
            writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_LEGS));
            writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_FEET));
            writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_BACK));
            writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_LRHAND));
            writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_HAIR));
			
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_UNDER));
            writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_REAR));
            writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_LEAR));
            writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_NECK));
            writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_RFINGER));
            writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_LFINGER));
            writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
            writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
            writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
            writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
            writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
            writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
            writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_FEET));
            writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_BACK));
            writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_LRHAND));
	        writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
            
            writeD(charInfoPackage.getHairStyle());
            writeD(charInfoPackage.getHairColor());
            writeD(charInfoPackage.getFace());

            writeF(charInfoPackage.getMaxHp()); // hp max
            writeF(charInfoPackage.getMaxMp()); // mp max

            writeD(charInfoPackage.getDeleteTimer()); // days left before
                                                        // delete .. if != 0
                                                        // then char is inactive
            writeD(charInfoPackage.getClassId());
            if (i == lastUsed)
            	writeD(0x01);
            else
            	writeD(0x00); //c3 auto-select char
            writeC(charInfoPackage.getEnchantEffect());
        }
    }

    private CharSelectInfoPackage[] loadCharacterSelectInfo()
    {
        CharSelectInfoPackage charInfopackage;
        ArrayList<CharSelectInfoPackage> characterList = new ArrayList<CharSelectInfoPackage>();

        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT * FROM characters WHERE account_name=? ORDER BY char_name");
            statement.setString(1, _loginName);
            ResultSet charList = statement.executeQuery();

            while (charList.next())// fills the package
            {
                charInfopackage = restoreChar(charList);
                characterList.add(charInfopackage);
            }

            charList.close();
            statement.close();
            return characterList.toArray(new CharSelectInfoPackage[characterList.size()]);
        }
        catch (Exception e)
        {
			_log.log(Level.WARNING, "could not restore charinfo:", e);
        } 
        finally 
        {
			try { con.close(); } catch (Exception e) {}
		}

        return null;
    }


    private CharSelectInfoPackage restoreChar(ResultSet chardata)
    {
        CharSelectInfoPackage charInfopackage = null;
        try
        {
        	int objectId = chardata.getInt("obj_id");
        	String name = chardata.getString("char_name");
            charInfopackage = new CharSelectInfoPackage(objectId, name);
            charInfopackage.setLevel(chardata.getInt("level"));
            charInfopackage.setMaxHp(chardata.getInt("maxhp"));
            charInfopackage.setCurrentHp(chardata.getDouble("curhp"));
            charInfopackage.setMaxMp(chardata.getInt("maxmp"));
            charInfopackage.setCurrentMp(chardata.getDouble("curmp"));

            charInfopackage.setFace(chardata.getInt("face"));
            charInfopackage.setHairStyle(chardata.getInt("hairstyle"));
            charInfopackage.setHairColor(chardata.getInt("haircolor"));
            charInfopackage.setSex(chardata.getInt("sex"));

            charInfopackage.setExp(chardata.getInt("exp"));
            charInfopackage.setSp(chardata.getInt("sp"));
            charInfopackage.setClanId(chardata.getInt("clanid"));

            charInfopackage.setRace(chardata.getInt("race"));
            charInfopackage.setClassId(chardata.getInt("classid"));
            charInfopackage.setDeleteTimer(chardata.getInt("deletetime"));
            charInfopackage.setLastAccess(chardata.getLong("lastAccess"));
        }
        catch (Exception e)
        {
			_log.log(Level.INFO, "", e);
        }
        
        return charInfopackage;
    }

    public String getType()
    {
        return _S__1F_CHARSELECTINFO;
    }
}
