/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;

//persistent pulsing highlight for the stair tiles, up and down use different colors
public class StairHighlight extends Image {

	public static final int UP_COLOR   = 0x3FFF5E; //green for stairs up
	public static final int DOWN_COLOR = 0xFF5040; //red for stairs down

	private float time;
	private final int cell;

	public StairHighlight( int cell, int color ) {
		super( Icons.get( Icons.TARGET ) );
		this.cell = cell;
		hardlight( color );
		point( DungeonTilemap.tileToWorld( cell ) );
	}

	@Override
	public void update() {
		super.update();
		if (Dungeon.level != null && cell < Dungeon.level.heroFOV.length && Dungeon.level.heroFOV[cell]) {
			visible = true;
			time += Game.elapsed;
			alpha( 0.45f + (float)Math.sin( time * 5f ) * 0.3f );
		} else {
			visible = false;
		}
	}
}
