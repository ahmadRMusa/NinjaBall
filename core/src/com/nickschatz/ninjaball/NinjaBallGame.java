package com.nickschatz.ninjaball;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
