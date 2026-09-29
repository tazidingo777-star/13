/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * Experienced Pixel Dungeon
 * Copyright (C) 2019-2024 Trashbox Bobylev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

import java.util.ArrayList;

//ExpPD: records the hero's recent damage taken, shown on the resurrect window
public class DeathLog {

	private static final ArrayList<String> entries = new ArrayList<>();
	private static final int MAX_ENTRIES = 5;

	public static void record( Hero target, long dmg, long shielded, Object src ){

		String srcName;
		if (src instanceof Char){
			srcName = Messages.titleCase(((Char) src).name());
		} else if (src instanceof Buff){
			srcName = Messages.titleCase(((Buff) src).name());
		} else {
			srcName = Messages.get(DeathLog.class, "unknown_source");
		}

		String shieldTxt = shielded > 0
				? Messages.get(DeathLog.class, "with_shield", shielded)
				: "";
		String fatal = target.HP <= 0
				? Messages.get(DeathLog.class, "fatal")
				: "";

		entries.add( Messages.get(DeathLog.class, "entry",
				Math.max(1, Dungeon.depth), srcName, dmg, shieldTxt, target.HP, target.HT, fatal) );

		while (entries.size() > MAX_ENTRIES){
			entries.remove(0);
		}
	}

	public static ArrayList<String> entries(){
		return new ArrayList<>(entries);
	}

	public static void clear(){
		entries.clear();
	}
}
