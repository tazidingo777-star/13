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

package com.shatteredpixel.shatteredpixeldungeon.items.scrolls;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Perks;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndPerkReroll;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class ScrollOfPerkReroll extends Scroll {

	@Override
	public void doRead() {

		if (curUser.perks.isEmpty()) {
			GLog.w( Messages.get(this, "no_perks") );
			return; //scroll is not consumed
		}
		if (curUser.perks.size() >= Perks.Perk.values().length) {
			GLog.w( Messages.get(this, "no_reroll") );
			return; //scroll is not consumed
		}

		GameScene.show(new WndPerkReroll(new ArrayList<>(curUser.perks), new WndPerkReroll.PerkSelectListener() {
			@Override
			public void onSelect(Perks.Perk selected) {
				confirmReroll(selected);
			}
		}));
	}

	private void confirmReroll(final Perks.Perk selected) {
		GameScene.show(new WndOptions(
				Messages.get(this, "confirm_title"),
				Messages.get(this, "reroll_warning", Messages.titleCase(selected.toString()), selected.desc()),
				Messages.get(this, "yes"),
				Messages.get(this, "no")
		) {
			@Override
			protected void onSelect(int index) {
				if (index == 0) {
					reroll(selected);
				}
			}
		});
	}

	private void reroll(Perks.Perk selected) {
		//pool = all perks minus currently held ones and the removed one, so the perk is guaranteed to change
		ArrayList<Perks.Perk> pool = new ArrayList<>();
		for (Perks.Perk p : Perks.Perk.values()) {
			if (p == selected || curUser.perks.contains(p)) continue;
			pool.add(p);
		}

		detach( curUser.belongings.backpack );
		identify();

		int index = curUser.perks.indexOf(selected);
		Perks.Perk newPerk = Random.element(pool);
		curUser.perks.set(index, newPerk);

		GLog.p( Messages.get(Perks.class, "perk_obtain", newPerk.toString()) );
		Sample.INSTANCE.play( Assets.Sounds.READ );
		curUser.sprite.emitter().burst( Speck.factory( Speck.STAR ), 20 );

		readAnimation();

		GameScene.show(new WndTitledMessage(
				Icons.get(Icons.INFO),
				Messages.titleCase(newPerk.toString()),
				newPerk.desc()));
	}
}
