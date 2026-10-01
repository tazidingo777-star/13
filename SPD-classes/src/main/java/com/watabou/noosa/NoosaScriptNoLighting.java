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

package com.watabou.noosa;

import com.watabou.glscripts.Script;

//This class should be used on heavy pixel-fill based loads when lighting is not needed.
// It skips the lighting component of the fragment shader, giving a significant performance boost

//Remember that switching programs is expensive
// if this script is to be used many times try to block them together
public class NoosaScriptNoLighting extends NoosaScript {

	@Override
	public void lighting(float rm, float gm, float bm, float am, float ra, float ga, float ba, float aa) {
		//Does nothing
	}

	public static NoosaScriptNoLighting get(){
		return Script.use( NoosaScriptNoLighting.class );
	}

	@Override
	protected String shader() {
		return SHADER;
	}

	private static final String SHADER =
		
		//vertex shader
		"uniform mat4 uCamera;\n" +
		"uniform mat4 uModel;\n" +
		"attribute vec4 aXYZW;\n" +
		"attribute vec2 aUV;\n" +
		"varying vec2 vUV;\n" +
		"void main() {\n" +
		"  gl_Position = uCamera * uModel * aXYZW;\n" +
		"  vUV = aUV;\n" +
		"}\n" +
		
		"//\n" +

		"#ifdef GL_ES\n" +
		"  precision mediump float;\n" +
		"#endif\n" +
		"varying vec2 vUV;\n" +
		"uniform sampler2D uTex;\n" +
		"uniform float uGradeStrength;\n" +
		"void main() {\n" +
		"  vec4 col = texture2D( uTex, vUV );\n" +
		"  if (uGradeStrength > 0.0) {\n" +
		"    vec3 c = col.rgb;\n" +
		"    float s = uGradeStrength;\n" +
		"    c = (c - 0.5) * (1.0 + 0.25 * s) + 0.5;\n" +
		"    float lum = 0.299*c.r + 0.587*c.g + 0.114*c.b;\n" +
		"    c = lum + (c - lum) * (1.0 + 0.30 * s);\n" +
		"    float shadow = 1.0 - clamp(lum * 2.0, 0.0, 1.0);\n" +
		"    c.r += shadow * 0.07 * s;\n" +
		"    c.b -= shadow * 0.04 * s;\n" +
		"    float high = clamp((lum - 0.5) * 2.0, 0.0, 1.0);\n" +
		"    c.r += high * 0.06 * s;\n" +
		"    c.g += high * 0.03 * s;\n" +
		"    col.rgb = c;\n" +
		"  }\n" +
		"  gl_FragColor = col;\n" +
		"}\n";
}
