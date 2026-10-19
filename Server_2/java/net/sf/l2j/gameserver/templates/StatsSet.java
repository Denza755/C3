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
package net.sf.l2j.gameserver.templates;

import java.util.HashMap;

/**
 * @author mkizub
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public final class StatsSet  {

	private final HashMap<String, Object> _set = new HashMap<String, Object>();
	
	public final HashMap<String, Object> getSet()
	{
		return _set;
	}
	
	public boolean getBool  (String name)
	{
		Object val = _set.get(name);
		if (val == null)
			throw new IllegalArgumentException("Boolean value required, but not specified");
		if (val instanceof Boolean)
			return ((Boolean)val).booleanValue();
		try {
			return Boolean.parseBoolean((String)val);
		} catch (Exception e) {
			throw new IllegalArgumentException("Boolean value required, but found: "+val);
		}
	}
	public boolean getBool  (String name, boolean deflt)
	{
		Object val = _set.get(name);
		if (val == null)
			return deflt;
		if (val instanceof Boolean)
			return ((Boolean)val).booleanValue();
		try {
			return Boolean.parseBoolean((String)val);
		} catch (Exception e) {
			throw new IllegalArgumentException("Boolean value required, but found: "+val);
		}
	}
	public int     getIntegr(String name)
	{
		Object val = _set.get(name);
		if (val == null)
			throw new IllegalArgumentException("Integer value required, but not specified");
		if (val instanceof Number)
			return ((Number)val).intValue();
		try {
			return Integer.parseInt((String)val);
		} catch (Exception e) {
			throw new IllegalArgumentException("Integer value required, but found: "+val);
		}
	}
	public int     getIntegr(String name, int deflt)
	{
		Object val = _set.get(name);
		if (val == null)
			return deflt;
		if (val instanceof Number)
			return ((Number)val).intValue();
		try {
			return Integer.parseInt((String)val);
		} catch (Exception e) {
			throw new IllegalArgumentException("Integer value required, but found: "+val);
		}
	}
	public float  getFloat(String name)
	{
		Object val = _set.get(name);
		if (val == null)
			throw new IllegalArgumentException("Float value required, but not specified");
		if (val instanceof Number)
			return ((Number)val).floatValue();
		try {
			return (float)Double.parseDouble((String)val);
		} catch (Exception e) {
			throw new IllegalArgumentException("Float value required, but found: "+val);
		}
	}
	public float getFloat(String name, float deflt)
	{
		Object val = _set.get(name);
		if (val == null)
			return deflt;
		if (val instanceof Number)
			return ((Number)val).floatValue();
		try {
			return (float)Double.parseDouble((String)val);
		} catch (Exception e) {
			throw new IllegalArgumentException("Float value required, but found: "+val);
		}
	}
    
    public double getDouble(String name)
    {
        Object val = _set.get(name);
        if (val == null)
            throw new IllegalArgumentException("Float value required, but not specified");
        if (val instanceof Number)
            return ((Number)val).doubleValue();
        try {
            return Double.parseDouble((String)val);
        } catch (Exception e) {
            throw new IllegalArgumentException("Float value required, but found: "+val);
        }
    }
    public double getDouble(String name, float deflt)
    {
        Object val = _set.get(name);
        if (val == null)
            return deflt;
        if (val instanceof Number)
            return ((Number)val).doubleValue();
        try {
            return Double.parseDouble((String)val);
        } catch (Exception e) {
            throw new IllegalArgumentException("Float value required, but found: "+val);
        }
    }
	public String  getString(String name)
	{
		Object val = _set.get(name);
		if (val == null)
			throw new IllegalArgumentException("String value required, but not specified");
		return String.valueOf(val);
	}
	public String  getString(String name, String deflt)
	{
		Object val = _set.get(name);
		if (val == null)
			return deflt;
		return String.valueOf(val);
	}
	public <T extends Enum<T>> T getEnum(String name, Class<T> enumClass)
	{
		Object val = _set.get(name);
		if (val == null)
			throw new IllegalArgumentException("Enum value of type "+enumClass.getName()+" required, but not specified");
		if (enumClass.isInstance(val))
			return (T)val;
		try {
			return Enum.valueOf(enumClass, String.valueOf(val));
		} catch (Exception e) {
			throw new IllegalArgumentException("Enum value of type "+enumClass.getName()+"required, but found: "+val);
		}
	}
	public <T extends Enum<T>> T getEnum(String name, Class<T> enumClass, T deflt)
	{
		Object val = _set.get(name);
		if (val == null)
			return deflt;
		if (enumClass.isInstance(val))
			return (T)val;
		try {
			return Enum.valueOf(enumClass, String.valueOf(val));
		} catch (Exception e) {
			throw new IllegalArgumentException("Enum value of type "+enumClass.getName()+"required, but found: "+val);
		}
	}
	
	public void set(String name, String value)
	{
		_set.put(name, value);
	}
	public void set(String name, boolean value)
	{
		_set.put(name, value);
	}
	public void set(String name, int value)
	{
		_set.put(name, value);
	}
	public void set(String name, double value)
	{
		_set.put(name, value);
	}
	public void set(String name, Enum value)
	{
		_set.put(name, value);
	}
}
