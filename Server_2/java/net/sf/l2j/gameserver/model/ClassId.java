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
package net.sf.l2j.gameserver.model;

/**
 * This class ...
 * 
 * @version $Revision: 1.4.4.4 $ $Date: 2005/03/27 15:29:33 $
 */
public enum ClassId
{
	fighter        (0x00,  false,  Race.human, null),
	
	warrior        (0x01,  false,  Race.human, fighter),
	gladiator      (0x02,  false,  Race.human,   warrior),
	warlord        (0x03,  false,  Race.human,   warrior),
	knight         (0x04,  false,  Race.human, fighter),
	paladin        (0x05,  false,  Race.human,   knight),
	darkAvenger    (0x06,  false,  Race.human,   knight),
	rogue          (0x07,  false,  Race.human, fighter),
	treasureHunter (0x08,  false,  Race.human,   rogue),
	hawkeye        (0x09,  false,  Race.human,   rogue),
	
	mage           (0x0a,   true,  Race.human, null),
	wizard         (0x0b,   true,  Race.human, mage),
	sorceror       (0x0c,   true,  Race.human,   wizard),
	necromancer    (0x0d,   true,  Race.human,   wizard),
	warlock        (0x0e,   true,  Race.human,   wizard),
	cleric         (0x0f,   true,  Race.human, mage),
	bishop         (0x10,   true,  Race.human,   cleric),
	prophet        (0x11,   true,  Race.human,   cleric),
	
	elvenFighter      (0x12,  false,  Race.elf, null),
	elvenKnight       (0x13,  false,  Race.elf, elvenFighter),
	templeKnight      (0x14,  false,  Race.elf,   elvenKnight),
	swordSinger       (0x15,  false,  Race.elf,   elvenKnight),
	elvenScout        (0x16,  false,  Race.elf, elvenFighter),
	plainsWalker      (0x17,  false,  Race.elf,   elvenScout), 
	silverRanger      (0x18,  false,  Race.elf,   elvenScout), 
	
	elvenMage         (0x19,   true,  Race.elf, null),
	elvenWizard       (0x1a,   true,  Race.elf, elvenMage),
	spellsinger       (0x1b,   true,  Race.elf,   elvenWizard),
	elementalSummoner (0x1c,   true,  Race.elf,   elvenWizard),
	oracle            (0x1d,   true,  Race.elf, elvenMage),
	elder             (0x1e,   true,  Race.elf,   oracle),
	
	darkFighter       (0x1f,  false,  Race.darkelf, null),
	palusKnight       (0x20,  false,  Race.darkelf, darkFighter),
	shillienKnight    (0x21,  false,  Race.darkelf,   palusKnight),
	bladedancer       (0x22,  false,  Race.darkelf,   palusKnight),
	assassin          (0x23,  false,  Race.darkelf, darkFighter),
	abyssWalker       (0x24,  false,  Race.darkelf,   assassin),
	phantomRanger     (0x25,  false,  Race.darkelf,   assassin),
	
	darkMage          (0x26,   true,  Race.darkelf, null),
	darkWizard        (0x27,   true,  Race.darkelf, darkMage),
	spellhowler       (0x28,   true,  Race.darkelf,   darkWizard),
	phantomSummoner   (0x29,   true,  Race.darkelf,   darkWizard),
	shillienOracle    (0x2a,   true,  Race.darkelf, darkMage),
	shillenElder      (0x2b,   true,  Race.darkelf,   shillienOracle),

	orcFighter        (0x2c,  false,  Race.orc, null),
	orcRaider         (0x2d,  false,  Race.orc, orcFighter),
	destroyer         (0x2e,  false,  Race.orc,   orcRaider),
	orcMonk           (0x2f,  false,  Race.orc, orcFighter),
	tyrant            (0x30,  false,  Race.orc,   orcMonk),

	orcMage           (0x31,   true,  Race.orc, null),
	orcShaman         (0x32,   true,  Race.orc, orcMage),
	overlord          (0x33,   true,  Race.orc,   orcShaman),
	warcryer          (0x34,   true,  Race.orc,   orcShaman),
	
	dwarvenFighter    (0x35,  false,  Race.dwarf, null),
	scavenger         (0x36,  false,  Race.dwarf, dwarvenFighter),
	bountyHunter      (0x37,  false,  Race.dwarf,   scavenger),
	artisan           (0x38,  false,  Race.dwarf, dwarvenFighter),
	warsmith          (0x39,  false,  Race.dwarf,   artisan);
	
	private final int     id;
	private final boolean isMage;
	private final Race    race;
	private final ClassId parent;
	
	private ClassId(int id, boolean isMage, Race race, ClassId parent)
	{
		this.id = id;
		this.isMage = isMage;
		this.race = race;
		this.parent = parent;
	}
	
	public final int     getId()   { return id;     }
	public final boolean isMage()  { return isMage; }
	public final Race    getRace() { return race;   }
	
	public final boolean childOf(ClassId cid) {
		if (parent == null)
			return false;
		if (parent == cid)
			return true;
		return parent.childOf(cid);
	}
	public final boolean equalsOrChildOf(ClassId cid) {
		return this == cid || childOf(cid);
	}
	public final int level() {
		if (parent == null)
			return 0;
		return 1 + parent.level();
	}
	
}
