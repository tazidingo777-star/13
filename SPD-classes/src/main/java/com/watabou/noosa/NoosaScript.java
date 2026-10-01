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

import com.badlogic.gdx.Gdx;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.glscripts.Script;
import com.watabou.glwrap.Attribute;
import com.watabou.glwrap.Quad;
import com.watabou.glwrap.Texture;
import com.watabou.glwrap.Uniform;
import com.watabou.glwrap.Vertexbuffer;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class NoosaScript extends Script {

	public Uniform uCamera;
	public Uniform uModel;
	public Uniform uTex;
	public Uniform uColorM;
	public Uniform uColorA;
	public Uniform uLUT;
	public Uniform uLUTStrength;
	public Attribute aXY;
	public Attribute aUV;

	// 256x16 LUT cube; lazy-loaded from assets/interfaces/color_lut.png and bound to texture unit 1.
	// Alpha of LUT entries controls strength (alpha=0 -> identity, alpha=1 -> fully graded).
	private static SmartTexture lutTex;

	// Strength 0..4 of the LUT grade. Recomputed on first use() after invalidateLutStrength().
	private static int lutStrengthStep = 4;
	private static boolean lutStrengthDirty = true;

	private Camera lastCamera;

	public NoosaScript() {

		super();
		compile( shader() );

		uCamera	= uniform( "uCamera" );
		uModel	= uniform( "uModel" );
		uTex	= uniform( "uTex" );
		uColorM	= uniform( "uColorM" );
		uColorA	= uniform( "uColorA" );
		uLUT	= uniform( "uLUT" );
		uLUTStrength = uniform( "uLUTStrength" );
		aXY		= attribute( "aXYZW" );
		aUV		= attribute( "aUV" );

		Quad.setupIndices();
		Quad.bindIndices();

	}
	
	@Override
	public void use() {

		super.use();

		aXY.enable();
		aUV.enable();

		if (lutTex == null) {
			lutTex = TextureCache.get("interfaces/color_lut.png");
			lutTex.filter(SmartTexture.NEAREST, SmartTexture.NEAREST);
			lutTex.wrap(SmartTexture.CLAMP, SmartTexture.CLAMP);
		}
		// bind LUT to unit 1, then switch back to unit 0 so main texture bind() still goes to unit 0.
		// The LUT stays bound to unit 1 across draw calls (no one else uses unit >= 1 in this project).
		Texture.activate(1);
		lutTex.bind();
		Texture.activate(0);
		uLUT.value1i(1);

		if (lutStrengthDirty) {
			// strength steps: 0=0.0, 1=0.25, 2=0.55, 3=0.85, 4=1.0
			int s = Math.max(0, Math.min(4, lutStrengthStep));
			float v = (s == 0) ? 0f : (s == 1) ? 0.25f : (s == 2) ? 0.55f : (s == 3) ? 0.85f : 1f;
			uLUTStrength.value1f(v);
			lutStrengthDirty = false;
		}

	}

	public void drawElements( FloatBuffer vertices, ShortBuffer indices, int size ) {

		((Buffer)vertices).position( 0 );
		aXY.vertexPointer( 2, 4, vertices );

		((Buffer)vertices).position( 2 );
		aUV.vertexPointer( 2, 4, vertices );

		Quad.releaseIndices();
		Gdx.gl20.glDrawElements( Gdx.gl20.GL_TRIANGLES, size, Gdx.gl20.GL_UNSIGNED_SHORT, indices );
		Quad.bindIndices();
	}

	public void drawQuad( FloatBuffer vertices ) {

		((Buffer)vertices).position( 0 );
		aXY.vertexPointer( 2, 4, vertices );

		((Buffer)vertices).position( 2 );
		aUV.vertexPointer( 2, 4, vertices );
		
		Gdx.gl20.glDrawElements( Gdx.gl20.GL_TRIANGLES, Quad.SIZE, Gdx.gl20.GL_UNSIGNED_SHORT, 0 );
	}

	public void drawQuad( Vertexbuffer buffer ) {

		buffer.updateGLData();

		buffer.bind();

		aXY.vertexBuffer( 2, 4, 0 );
		aUV.vertexBuffer( 2, 4, 2 );

		buffer.release();
		
		Gdx.gl20.glDrawElements( Gdx.gl20.GL_TRIANGLES, Quad.SIZE, Gdx.gl20.GL_UNSIGNED_SHORT, 0 );
	}
	
	public void drawQuadSet( FloatBuffer vertices, int size ) {
		
		if (size == 0) {
			return;
		}

		((Buffer)vertices).position( 0 );
		aXY.vertexPointer( 2, 4, vertices );

		((Buffer)vertices).position( 2 );
		aUV.vertexPointer( 2, 4, vertices );
		
		Gdx.gl20.glDrawElements( Gdx.gl20.GL_TRIANGLES, Quad.SIZE * size, Gdx.gl20.GL_UNSIGNED_SHORT, 0 );
	}

	public void drawQuadSet( Vertexbuffer buffer, int length, int offset ){

		if (length == 0) {
			return;
		}

		buffer.updateGLData();

		buffer.bind();

		aXY.vertexBuffer( 2, 4, 0 );
		aUV.vertexBuffer( 2, 4, 2 );

		buffer.release();
		
		Gdx.gl20.glDrawElements( Gdx.gl20.GL_TRIANGLES, Quad.SIZE * length, Gdx.gl20.GL_UNSIGNED_SHORT, Quad.SIZE * Short.SIZE/8 * offset );
	}
	
	public void lighting( float rm, float gm, float bm, float am, float ra, float ga, float ba, float aa ) {
		uColorM.value4f( rm, gm, bm, am );
		uColorA.value4f( ra, ga, ba, aa );
	}
	
	public void resetCamera() {
		lastCamera = null;
	}
	
	public void camera( Camera camera ) {
		if (camera == null) {
			camera = Camera.main;
		}
		if (camera != lastCamera && camera.matrix != null) {
			lastCamera = camera;
			uCamera.valueM4( camera.matrix );

			if (!camera.fullScreen) {
				Gdx.gl20.glEnable( Gdx.gl20.GL_SCISSOR_TEST );

				//This fixes pixel scaling issues on some hidpi displays (mainly on macOS)
				// because for some reason all other openGL operations work on virtual pixels
				// but glScissor operations work on real pixels
				float xScale = (Gdx.graphics.getBackBufferWidth() / (float)Game.width );
				float yScale = ((Gdx.graphics.getBackBufferHeight()-Game.bottomInset) / (float)Game.height );

				Gdx.gl20.glScissor(
						Math.round(camera.x * xScale),
						Math.round((Game.height - camera.screenHeight - camera.y) * yScale) + Game.bottomInset,
						Math.round(camera.screenWidth * xScale),
						Math.round(camera.screenHeight * yScale));
			} else {
				Gdx.gl20.glDisable( Gdx.gl20.GL_SCISSOR_TEST );
			}
		}
	}
	
	public static NoosaScript get() {
		return Script.use( NoosaScript.class );
	}

	// Called by SPDSettings.lutStrength() when the user changes the LUT strength setting.
	// Pass the strength step directly (0..4) to avoid SPD-classes depending on the core module.
	public static void invalidateLutStrength(int step) {
		lutStrengthStep = Math.max(0, Math.min(4, step));
		lutStrengthDirty = true;
	}

	// Backward-compatible overload for callers without a step value (defaults to full strength).
	public static void invalidateLutStrength() {
		invalidateLutStrength(4);
	}
	
	
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
		"uniform sampler2D uLUT;\n" +
		"uniform vec4 uColorM;\n" +
		"uniform vec4 uColorA;\n" +
		"uniform float uLUTStrength;\n" +
		"void main() {\n" +
		"  vec4 col = texture2D( uTex, vUV ) * uColorM + uColorA;\n" +
		"  if (uLUTStrength > 0.0) {\n" +
		"    float r = floor(col.r * 15.0 + 0.5) / 15.0;\n" +
		"    float g = floor(col.g * 15.0 + 0.5) / 15.0;\n" +
		"    float b = floor(col.b * 15.0 + 0.5) / 15.0;\n" +
		"    float bx = floor(b * 15.0 + 0.5);\n" +
		"    vec2 lutUV = vec2((bx + r) / 16.0, (g + 0.5) / 16.0);\n" +
		"    vec4 graded = texture2D( uLUT, lutUV );\n" +
		"    col.rgb = mix( col.rgb, graded.rgb, uLUTStrength );\n" +
		"  }\n" +
		"  gl_FragColor = col;\n" +
		"}\n";
}
