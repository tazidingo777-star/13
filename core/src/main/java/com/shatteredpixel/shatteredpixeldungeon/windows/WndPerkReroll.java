/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2019-2024 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Perks;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

public class WndPerkReroll extends Window {

	private final int WIDTH = Math.min(138, (int) (PixelScene.uiCamera.width * 0.9));
	private final int HEIGHT = (int) (PixelScene.uiCamera.height * 0.9);
	private static final int TTL_HEIGHT = 18;
	private static final int BTN_HEIGHT = 18;
	private static final int GAP = 1;

	private final ArrayList<PerkButton> buttons = new ArrayList<>();

	public interface PerkSelectListener {
		void onSelect(Perks.Perk perk);
	}

	public WndPerkReroll(ArrayList<Perks.Perk> perks, final PerkSelectListener listener) {

		super();

		resize(WIDTH, HEIGHT);

		RenderedTextBlock title = PixelScene.renderTextBlock( Messages.get(this, "title"), 12 );
		title.hardlight( TITLE_COLOR );
		title.setPos(
				(WIDTH - title.width()) / 2,
				(TTL_HEIGHT - title.height()) / 2
		);
		PixelScene.align(title);
		add( title );

		ScrollPane pane = new ScrollPane(new Component()) {
			@Override
			public void onClick(float x, float y) {
				for (PerkButton btn : buttons) {
					if (btn.onClick(x, y)) {
						hide();
						listener.onSelect(btn.perk);
						break;
					}
				}
			}
		};
		add(pane);
		pane.setRect(0, title.bottom() + 2, WIDTH, HEIGHT - title.bottom() - 2);
		Component content = pane.content();

		float pos = 2;
		for (Perks.Perk perk : perks) {

			PerkButton btn = new PerkButton(perk);

			pos += GAP;
			btn.setRect(0, pos, WIDTH, BTN_HEIGHT);

			content.add(btn);
			buttons.add(btn);

			pos = btn.bottom();
		}

		content.setSize(WIDTH, pos);
	}

	public static class PerkButton extends RedButton {

		public final Perks.Perk perk;

		public PerkButton(Perks.Perk perk) {
			super(Messages.titleCase(perk.toString()));
			this.perk = perk;
		}

		protected boolean onClick(float x, float y) {
			if (!inside(x, y)) return false;
			Sample.INSTANCE.play(Assets.Sounds.CLICK);
			onClick();
			return true;
		}

		@Override
		protected void layout() {
			super.layout();
			hotArea.width = hotArea.height = 0;
		}
	}
}
