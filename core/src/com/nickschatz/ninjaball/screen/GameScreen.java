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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
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

import java.util.List;

public class GameScreen implements Screen {

    private Texture ropeTex;
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

    private Texture ball;
    private TiledMap map;
    private boolean isPaused = false;

    private Player thePlayer;

    private Stage stage;
    private Label debugLabel;
    private Table table;
    private Skin skin;

    private boolean hasRope = false;
    private Vector2 playerGrav;
    private Slider sensitivitySlider;

    private final float ROT_LIMIT = 180;

    private float camBBsize;

    private float mapScale = 0.5f;

    public GameScreen(NinjaBallGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        world = new World(new Vector2(0, -10), true);
        debugRenderer = new Box2DDebugRenderer();

        thePlayer = new Player(world, 100, 300, 6f);
        world.setContactListener(new PlayerContactListener(thePlayer));

        mapBodyManager = new MapBodyManager(world, 1/mapScale, Gdx.files.internal("data/materials.json"), Application.LOG_DEBUG);

        map = Resources.get().get("data/level1.tmx", TiledMap.class);
        mapRenderer = new OrthogonalTiledMapRenderer(map, mapScale, game.batch);
        mapBodyManager.createPhysics(map, "physics");

        ball = Resources.get().get("data/ball64x64.png", Texture.class);
        ropeTex = Resources.get().get("data/rope.png", Texture.class);

        stage = new Stage(new ScreenViewport(), game.batch);

        TextureAtlas atlas = Resources.get().get("data/uiskin.atlas", TextureAtlas.class);
        skin = new Skin(Gdx.files.internal("data/uiskin.json"));
        skin.addRegions(atlas);

        debugLabel = new Label("Debug!", skin);
        stage.addActor(debugLabel);
        table = new Table();
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
        returnButton.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                togglePause();
            }
        });
        table.add(returnButton).padBottom(50).row();
        table.add(new TextButton("Exit", skin));

        lightManager = new TiledLightManager(new RayHandler(world), map, "lights", Logger.DEBUG);
        lightManager.setAmbientLight(new Color(0.01f, 0.01f, 0.01f, 1f));
        lightManager.setCulling(false); //Culling doesn't work well with rotation

        Gdx.input.setInputProcessor(new GameInput(this, stage));
        Gdx.input.setCatchBackKey(true);

        camBBsize = (float) Math.sqrt((camera.viewportWidth*camera.viewportWidth)+(camera.viewportHeight*camera.viewportHeight));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        if (!isPaused) {
            rotationRate = sensitivitySlider.getValue();
            camera.position.x = thePlayer.getPosition().x;
            camera.position.y = thePlayer.getPosition().y;

            /*float minCamX = Float.parseFloat(map.getProperties().get("minCamX", String.class)) * mapScale * 70;
            float minCamY = Float.parseFloat(map.getProperties().get("minCamY", String.class)) * mapScale * 70;
            float maxCamX = Float.parseFloat(map.getProperties().get("maxCamX", String.class)) * mapScale * 70;
            float maxCamY = Float.parseFloat(map.getProperties().get("maxCamY", String.class)) * mapScale * 70;
            if (camera.position.x < minCamX) {
                camera.position.x = minCamX;
            }
            if (camera.position.y > minCamY) {
                camera.position.y = minCamY;
            }
            if (camera.position.x > maxCamX) {
                camera.position.x = maxCamX;
            }
            if (camera.position.y < maxCamY) {
                camera.position.y = maxCamY;
            }*/

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
            if (Gdx.input.isTouched() && Gdx.input.getX() <= Gdx.graphics.getWidth() / 2) {
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
        mapRenderer.renderTileLayer((TiledMapTileLayer) map.getLayers().get("background"));
        //debugRenderer.render(world, camera.combined);

        List<Body> ropeBodies = thePlayer.getRopeBodies();

        TextureRegion ropeRegion = new TextureRegion(ropeTex, 0, 0, ropeTex.getWidth(), ropeTex.getHeight());
        for (int i=0;i<ropeBodies.size();i++) {

            Body bodyA = ropeBodies.get(i);
            Body bodyB;
            if (i == ropeBodies.size()-1) bodyB = thePlayer.getBody();
            else bodyB = ropeBodies.get(i+1);

            float angle = bodyA.getPosition().sub(bodyB.getPosition()).cpy().nor().angle();
            Vector2 midpoint = new Vector2();
            midpoint.x = (bodyA.getPosition().x - bodyB.getPosition().x) / 2;
            midpoint.y = (bodyA.getPosition().y - bodyB.getPosition().y) / 2;

            float dst = bodyA.getPosition().dst(bodyB.getPosition());

            Vector2 botLeft = new Vector2();
            if (bodyA.getPosition().x < bodyB.getPosition().x) {
                botLeft.x = bodyA.getPosition().x;
            }
            else {
                botLeft.x = bodyB.getPosition().x;
            }
            if (bodyA.getPosition().y < bodyB.getPosition().y) {
                botLeft.y = bodyA.getPosition().y;
            }
            else {
                botLeft.y = bodyB.getPosition().y;
            }

            float width = 3f;

            game.batch.draw(ropeRegion,
                    botLeft.x, //X
                    botLeft.y,
                    width / 2, //OriginX
                    dst / 2, //OriginY
                    width, //Width
                    dst, //Height
                    1,1, //Scale
                    angle+90  //Rotation
            );
        }

        int textureWidth = ball.getWidth();
        int textureHeight = ball.getHeight();

        TextureRegion ballRegion = new TextureRegion(ball, 0, 0, textureWidth, textureHeight);
        game.batch.draw(ballRegion,
                thePlayer.getPosition().x - thePlayer.getRadius(),
                thePlayer.getPosition().y - thePlayer.getRadius(),
                thePlayer.getRadius(),
                thePlayer.getRadius(),
                thePlayer.getRadius() * 2,
                thePlayer.getRadius() * 2, 1, 1, (float) Math.toDegrees(thePlayer.getRotation()), false);

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
        mapBodyManager.destroyPhysics();
        lightManager.dispose();
        world.dispose();
    }

    public void togglePause() {
        isPaused = !isPaused;
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
}
