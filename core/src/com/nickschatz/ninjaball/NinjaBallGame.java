/*
 * Copyright (c) 2014 Nick Schatz
 *
 * This software is provided 'as-is', without any express or implied
 * warranty. In no event will the authors be held liable for any damages
 * arising from the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 *    1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software. If you use this software
 *    in a product, an acknowledgment in the product documentation would be
 *    appreciated but is not required.
 *
 *    2. Altered source versions must be plainly marked as such, and must not be
 *    misrepresented as being the original software.
 *
 *    3. This notice may not be removed or altered from any source
 *    distribution.
 */

package com.nickschatz.ninjaball;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.utils.Logger;
import com.nickschatz.ninjaball.screen.GameScreen;

public class NinjaBallGame extends Game {
	public SpriteBatch batch;
    public BitmapFont defaultFont;

    private boolean loading = true;
    public boolean useAccelerometer = false;
    public Logger log;
	
	@Override
	public void create () {
        Resources.init();
        log = new Logger("NinjaBall", Logger.DEBUG);

        defaultFont = new BitmapFont();

		batch = new SpriteBatch();

        Resources.get().setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));
        Resources.get().load("data/test2.tmx", TiledMap.class);
        Resources.get().load("data/ball64x64.png", Texture.class);
        Resources.get().load("data/uiskin.atlas", TextureAtlas.class);

        useAccelerometer = Gdx.input.isPeripheralAvailable(Input.Peripheral.Accelerometer);
        log.info("Accelerometer IS " + (useAccelerometer ? "" : "NOT ") + "availiable");
	}

	@Override
	public void render () {
        if (loading && Resources.get().update()) {
            setScreen(new GameScreen(this));
            loading = false;
        }
        else if (loading) {
            Gdx.gl.glClearColor(1, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            batch.begin();
            defaultFont.draw(batch, "Loading...", 100, 100);
            batch.end();
        }
        else {
            super.render();
        }
	}
}
