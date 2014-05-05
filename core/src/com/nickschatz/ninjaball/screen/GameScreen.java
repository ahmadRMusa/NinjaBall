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

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.nickschatz.ninjaball.NinjaBallGame;
import com.nickschatz.ninjaball.Resources;
import com.nickschatz.ninjaball.entity.Player;
import com.nickschatz.ninjaball.util.MapBodyManager;
import com.nickschatz.ninjaball.util.PlayerContactListener;
import com.nickschatz.ninjaball.util.Util;

public class GameScreen implements Screen {

    private final Box2DDebugRenderer debugRenderer;
    private World world;
    private OrthographicCamera camera;
    private NinjaBallGame game;
    private float rotation = 0.0f;
    private float rotationRate = 0.5f;
    private MapBodyManager mapBodyManager;
    private TiledMapRenderer mapRenderer;
    private Stage stage;
    private Label debugLabel;
    private Texture ball;

    private Player thePlayer;

    public GameScreen(NinjaBallGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        world = new World(new Vector2(0, -10), true);
        debugRenderer = new Box2DDebugRenderer();

        world.setContactListener(new PlayerContactListener());
        thePlayer = new Player(world, 100, 300, 6f);

        mapBodyManager = new MapBodyManager(world, 1.0f, Gdx.files.internal("data/materials.json"), Application.LOG_DEBUG);
        TiledMap map = Resources.get().get("data/test2.tmx", TiledMap.class);
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1, game.batch);
        mapBodyManager.createPhysics(map, "physics");

        ball = Resources.get().get("data/ball64x64.png", Texture.class);

        stage = new Stage(new ScreenViewport(), game.batch);
        Label.LabelStyle style = new Label.LabelStyle();
        style.font = game.defaultFont;
        debugLabel = new Label("Debug!", style);
        stage.addActor(debugLabel);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                camera.position.x -= 1;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                camera.position.y += 1;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                camera.position.x += 1;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                camera.position.y -= 1;
            }
        }
        else {
            camera.position.x = thePlayer.getPosition().x;
            camera.position.y = thePlayer.getPosition().y;
        }
        if (!game.useAccelerometer) {
            rotation += (Gdx.input.isKeyPressed(Input.Keys.LEFT) ? -rotationRate : 0) +
                    (Gdx.input.isKeyPressed(Input.Keys.RIGHT) ? rotationRate : 0);
        }
        else {
            rotation = Gdx.input.getAccelerometerY() * 9; //Shift values from [10...-10] to [90...-90]

        }

        if (rotation > 90) {
            rotation = 90;
        }
        else if (rotation < -90) {
            rotation = -90;
        }
        if (!game.useAccelerometer) {
            camera.rotate(new Vector3(0, 0, 1),
                    (Util.getCameraCurrentXYAngle(camera) + rotation)
            );
        }



        Vector2 playerGrav = world.getGravity().cpy().rotate(rotation).scl(thePlayer.getBody().getMass());

        if ((Gdx.input.isTouched() || Gdx.input.isKeyPressed(Input.Keys.SPACE)) && thePlayer.canJump()) {
            thePlayer.getBody().applyLinearImpulse(playerGrav.cpy().rotate(180).scl(2), thePlayer.getBody().getWorldCenter(), true);
        }

        //Apply fake gravity
        thePlayer.getBody().applyForce(
                playerGrav,
                thePlayer.getBody().getWorldCenter(), true);

        world.step(1/30f, 6, 2);



        mapRenderer.setView(camera.combined, 0, 0, 1000, 1000);
        mapRenderer.render();
        //debugRenderer.render(world, camera.combined);

        int textureWidth = ball.getWidth();
        int textureHeight = ball.getHeight();

        TextureRegion region = new TextureRegion(ball, 0, 0, textureWidth, textureHeight);

        game.batch.begin();
        game.batch.draw(region,
                thePlayer.getPosition().x - thePlayer.getRadius(),
                thePlayer.getPosition().y - thePlayer.getRadius(),
                thePlayer.getRadius(),
                thePlayer.getRadius(),
                thePlayer.getRadius()*2,
                thePlayer.getRadius()*2, 1, 1, (float) Math.toDegrees(thePlayer.getRotation()), false);
        game.batch.end();

        debugLabel.setText("Rotation: " + rotation + " FPS: " + Gdx.graphics.getFramesPerSecond() + " J: " + thePlayer.canJump());

        stage.draw();
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
    }
}
