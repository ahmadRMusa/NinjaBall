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

package com.nickschatz.ninjaball.screen;

import box2dLight.RayHandler;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.nickschatz.ninjaball.NinjaBallGame;
import com.nickschatz.ninjaball.Resources;
import com.nickschatz.ninjaball.entity.Player;
import com.nickschatz.ninjaball.input.GameInput;
import com.nickschatz.ninjaball.util.MapBodyManager;
import com.nickschatz.ninjaball.util.PlayerContactListener;
import com.nickschatz.ninjaball.util.TiledLightManager;
import com.nickschatz.ninjaball.util.Util;

public class GameScreen implements Screen {


    private TiledLightManager lightManager;
    private Box2DDebugRenderer debugRenderer;
    private World world;
    private OrthographicCamera camera;
    private NinjaBallGame game;
    private float rotation = 0.0f;
    private float rotationRate = 1f;
    private MapBodyManager mapBodyManager;
    private TiledMapRenderer mapRenderer;
    private ShapeRenderer shapeRenderer;


    private TiledMap map;
    private boolean isPaused = false;

    private Player thePlayer;

    private Stage stage;
    private Label debugLabel;
    private Table table;
    private Skin skin;

    private Vector2 playerGrav;
    private Slider sensitivitySlider;

    private final float ROT_LIMIT = 90;

    private float camBBsize;

    private float mapScale = 0.5f;

    private Music curMusic;

    public GameScreen(final NinjaBallGame game, TiledMap map, Music curMusic) {
        this.game = game;
        this.map = map;
        this.curMusic = curMusic;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        world = new World(new Vector2(0, -10), true);
        debugRenderer = new Box2DDebugRenderer();

        thePlayer = new Player(world, 100, 300, 10f);
        world.setContactListener(new PlayerContactListener(thePlayer, this));

        mapBodyManager = new MapBodyManager(world, 1/mapScale, Gdx.files.internal("data/materials.json"), Application.LOG_DEBUG);

        mapRenderer = new OrthogonalTiledMapRenderer(map, mapScale, game.batch);
        mapBodyManager.createPhysics(map, "physics");



        stage = new Stage(new ScreenViewport(), game.batch);

        TextureAtlas atlas = Resources.get().get("data/uiskin.atlas", TextureAtlas.class);
        skin = new Skin(Gdx.files.internal("data/uiskin.json"));
        skin.addRegions(atlas);

        debugLabel = new Label("Debug!", skin);
        stage.addActor(debugLabel);
        table = new Table();
        table.setTransform(true);
        table.setScale(1.1f);
        table.setFillParent(true);
        stage.addActor(table);

        shapeRenderer = new ShapeRenderer();


        sensitivitySlider = new Slider(0.5f, 2f, 0.1f, false, skin);
        sensitivitySlider.setValue(Gdx.app.getPreferences("Options").getFloat("rotSensitivity", 1f));
        sensitivitySlider.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Slider slider = (Slider) actor;
                Gdx.app.getPreferences("Options").putFloat("rotSensitivity", slider.getValue());
                Gdx.app.getPreferences("Options").flush();
            }
        });

        table.add(new Label("Sensitivity: ", skin));
        table.add(sensitivitySlider).row();
        TextButton returnButton = new TextButton("Return", skin);
        //returnButton.setScale(2);
        returnButton.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                togglePause();
            }
        });
        table.add(returnButton).padBottom(50).row();
        TextButton exitButton = new TextButton("Exit", skin);
        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MenuScreen(game));
                dispose();
            }
        });
        table.add(exitButton);

        lightManager = new TiledLightManager(new RayHandler(world), map, "lights", Logger.DEBUG);
        lightManager.setAmbientLight(new Color(0.01f, 0.01f, 0.01f, 1f));
        lightManager.setCulling(false); //Culling doesn't work well with rotation

        Gdx.input.setInputProcessor(new GameInput(this, stage));
        Gdx.input.setCatchBackKey(true);

        camBBsize = (float) Math.sqrt((camera.viewportWidth*camera.viewportWidth)+(camera.viewportHeight*camera.viewportHeight));

        game.batch.setBlendFunction(GL20.GL_BLEND_SRC_RGB, GL20.GL_BLEND_DST_RGB);

        curMusic.setLooping(true);
        curMusic.play();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(135f/255f, 206f/255f, 235f/255f, 1);
        //Gdx.gl.glClearColor(0, 1, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.enableBlending();

        if (!isPaused) {

            rotationRate = sensitivitySlider.getValue();
            float lerp = 0.1f;
            Vector3 position = camera.position;
            position.x += (thePlayer.getPosition().x - position.x) * lerp;
            position.y += (thePlayer.getPosition().y - position.y) * lerp;

            float minCamX = 600 * (Gdx.graphics.getWidth()/1280);
            float minCamY = 0;
            if (camera.position.x < minCamX) {
                camera.position.x = minCamX;
            }
            if (camera.position.y < minCamY) {
                camera.position.y = minCamY;
            }

            if (!game.useAccelerometer) {
                rotation += (Gdx.input.isKeyPressed(Input.Keys.LEFT) ? -rotationRate : 0) +
                        (Gdx.input.isKeyPressed(Input.Keys.RIGHT) ? rotationRate : 0);
            }
            else {
                rotation = Gdx.input.getAccelerometerY() * 9; //Shift values from [10...-10] to [90...-90]

            }

            if (rotation > ROT_LIMIT) {
                rotation = ROT_LIMIT;
            }
            else if (rotation < -ROT_LIMIT) {
                rotation = -ROT_LIMIT;
            }
            if (!game.useAccelerometer) {
                camera.rotate(new Vector3(0, 0, 1),
                        (Util.getCameraCurrentXYAngle(camera) + rotation)
                );
            }

            playerGrav = world.getGravity().cpy().rotate(rotation).scl(thePlayer.getBody().getMass());

            shapeRenderer.setProjectionMatrix(camera.combined);
            if ((Gdx.input.isTouched() && Gdx.input.getX() <= Gdx.graphics.getWidth() / 2) || Gdx.input.isKeyPressed(Input.Keys.X)) {
                final Vector2 ropeAnchorPos = new Vector2(0,0);

                world.rayCast(new RayCastCallback() {
                                  @Override
                                  public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {

                                      ropeAnchorPos.set(point);

                                      return fraction;
                                  }
                              },
                        thePlayer.getPosition(),
                        thePlayer.getPosition().cpy().add(playerGrav.cpy().rotate(180).nor().scl(300)));
                boolean hit = true;
                if (ropeAnchorPos.len() == 0) {
                    ropeAnchorPos.set(thePlayer.getPosition().cpy().add(playerGrav.cpy().rotate(180).nor().scl(300)));
                    hit = false;
                }

                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                if (hit) {
                    shapeRenderer.setColor(0, 1, 0, 1);
                }
                else {
                    shapeRenderer.setColor(1, 0, 0, 1);
                }
                shapeRenderer.line(thePlayer.getPosition().x, thePlayer.getPosition().y, ropeAnchorPos.x, ropeAnchorPos.y);
                shapeRenderer.end();
            }

            //Apply fake gravity
            thePlayer.getBody().applyForce(
                    playerGrav,
                    thePlayer.getBody().getWorldCenter(), true);

            world.step(1 / 30f, 6, 2);

        }

        mapRenderer.setView(camera.combined,camera.position.x - camBBsize / 2, camera.position.y - camBBsize / 2, camBBsize, camBBsize); //Dirty Fix. I should do something about it.
        game.batch.begin();
        mapRenderer.renderTileLayer(
                (TiledMapTileLayer) map.
                        getLayers().
                        get("background"));
        //debugRenderer.render(world, camera.combined);

        if (thePlayer.hasRope()) {
            thePlayer.getRope().draw(game.batch);
        }

        thePlayer.draw(game.batch);

        mapRenderer.renderTileLayer((TiledMapTileLayer) map.getLayers().get("foreground"));
        game.batch.end();

        //Everything before this is lit
        lightManager.setCombinedMatrix(camera.combined);
        lightManager.updateAndRender();
        //Everything after this is unlit

        if (isPaused) {

            debugLabel.setText("Rotation: " + rotation + " FPS: " + Gdx.graphics.getFramesPerSecond() + " J: " + thePlayer.canJump());

            stage.draw();
        }
    }




    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void show() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        //mapBodyManager.destroyPhysics();
        //lightManager.dispose();
        //world.dispose();
    }

    public void togglePause() {
        isPaused = !isPaused;
        if (isPaused)
            curMusic.pause();
        else
            curMusic.play();
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void rope() {
        thePlayer.throwRope(playerGrav, world);
    }

    public void jump() {
        thePlayer.jump(playerGrav);
    }

    public void nextLevel() {
        game.setScreen(new MenuScreen(game));
        curMusic.stop();
        dispose();
    }
}
