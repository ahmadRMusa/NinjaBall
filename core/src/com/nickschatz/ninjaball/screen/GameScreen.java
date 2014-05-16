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
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.RopeJointDef;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
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

import java.util.ArrayList;
import java.util.List;

public class GameScreen implements Screen {

    private Texture ropeTex;
    private TiledLightManager lightManager;
    private Box2DDebugRenderer debugRenderer;
    private World world;
    private OrthographicCamera camera;
    private NinjaBallGame game;
    private float rotation = 0.0f;
    private float rotationRate = 0.5f;
    private MapBodyManager mapBodyManager;
    private TiledMapRenderer mapRenderer;

    private Texture ball;
    private TiledMap map;
    private boolean isPaused = false;

    private Player thePlayer;

    private Stage stage;
    private Label debugLabel;
    private Table table;
    private Skin skin;
    private List<Body> ropeBodies;
    private List<Joint> ropeJoints;
    private boolean hasRope = false;
    private Vector2 playerGrav;

    public GameScreen(NinjaBallGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        world = new World(new Vector2(0, -10), true);
        debugRenderer = new Box2DDebugRenderer();

        world.setContactListener(new PlayerContactListener());
        thePlayer = new Player(world, 100, 300, 6f);

        mapBodyManager = new MapBodyManager(world, 1.0f, Gdx.files.internal("data/materials.json"), Application.LOG_DEBUG);

        map = Resources.get().get("data/test2.tmx", TiledMap.class);
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1, game.batch);
        mapBodyManager.createPhysics(map, "physics");

        ball = Resources.get().get("data/ball64x64.png", Texture.class);
        ropeTex = Resources.get().get("data/rope.png", Texture.class);

        stage = new Stage(new ScreenViewport(), game.batch);
        Label.LabelStyle style = new Label.LabelStyle();
        style.font = game.defaultFont;
        debugLabel = new Label("Debug!", style);
        stage.addActor(debugLabel);
        table = new Table();
        table.setFillParent(true);
        stage.addActor(table);


        TextureAtlas atlas = Resources.get().get("data/uiskin.atlas", TextureAtlas.class);
        skin = new Skin(Gdx.files.internal("data/uiskin.json"));
        skin.addRegions(atlas);

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

        ropeJoints = new ArrayList<Joint>();
        ropeBodies = new ArrayList<Body>();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        if (!isPaused) {
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

            playerGrav = world.getGravity().cpy().rotate(rotation).scl(thePlayer.getBody().getMass());



            //Apply fake gravity
            thePlayer.getBody().applyForce(
                    playerGrav,
                    thePlayer.getBody().getWorldCenter(), true);

            world.step(1 / 30f, 6, 2);

        }

        mapRenderer.setView(camera.combined, 0, 0, 1000, 1000); //Dirty Fix. I should do something about it.
        game.batch.begin();
        mapRenderer.renderTileLayer((TiledMapTileLayer) map.getLayers().get("background"));
        //debugRenderer.render(world, camera.combined);

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
        TextureRegion ropeRegion = new TextureRegion(ropeTex, 0, 0, ropeTex.getWidth(), ropeTex.getHeight());
        for (int i=0;i<ropeBodies.size()-1;i++) {

            Body bodyA = ropeBodies.get(i);
            Body bodyB = ropeBodies.get(i+1);
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

            game.batch.draw(ropeRegion,
                    botLeft.x, //X
                    botLeft.y,
                    midpoint.x, //OriginX
                    midpoint.y, //OriginY
                    10f, //Width
                    dst, //Height
                    1,1, //Scale
                    angle+90  //Rotation
            );
        }
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

    private void doJump(Vector2 playerGrav) {
        thePlayer.getBody().applyLinearImpulse(playerGrav.cpy().rotate(180).scl(2), thePlayer.getBody().getWorldCenter(), true);
    }
    private void doRope(Vector2 playerGrav) {
        if (hasRope) {
            hasRope = !hasRope;

            for (Joint j : ropeJoints) {
                world.destroyJoint(j);
            }
            ropeJoints.clear();
            for (Body b : ropeBodies) {
                world.destroyBody(b);
            }
            ropeBodies.clear();
            return;
        }

        final Vector2 ropeAnchorPos = new Vector2(0,0);

        world.rayCast(new RayCastCallback() {
                          @Override
                          public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {

                              ropeAnchorPos.set(point);

                              return 1;
                          }
                      },
                thePlayer.getPosition(),
                thePlayer.getPosition().cpy().add(playerGrav.cpy().rotate(180).nor().scl(1000)));
        if (ropeAnchorPos.len() == 0) {
            return;
        }

        hasRope = !hasRope;
        ropeBodies = new ArrayList<Body>();
        ropeJoints = new ArrayList<Joint>();

        int distFactor = 6;

        Vector2 lastPos = ropeAnchorPos; //set position first body
        float radBody = 3f;
        // Body params
        float density = 0.05f;
        float restitution = 0.5f;
        float friction = 0.5f;
        // Distance joint
        float dampingRatio = 0.0f;
        float frequencyHz = 0;
        // Rope joint
        float kMaxWidth = 1.1f;
        // Bodies
        int countBodyInChain = (int) (thePlayer.getPosition().dst(ropeAnchorPos) / distFactor);
        Body prevBody = null;

        //========Create bodies and joints
        for (int k = 0; k < countBodyInChain; k++) {
            BodyDef bodyDef = new BodyDef();
            if(k==0 ) bodyDef.type = BodyDef.BodyType.StaticBody; //first body is static
            else bodyDef.type = BodyDef.BodyType.DynamicBody;
            bodyDef.position.set(lastPos);
            lastPos = lastPos.add(playerGrav.cpy().nor().scl(distFactor)); //modify b2Vect for next body
            bodyDef.fixedRotation = true;
            Body body = world.createBody(bodyDef);

            CircleShape distBodyBox = new CircleShape();
            distBodyBox.setRadius(radBody);
            FixtureDef fixDef = new FixtureDef();
            fixDef.density = density;
            fixDef.restitution = restitution;
            fixDef.friction = friction;
            fixDef.shape = distBodyBox;
            body.createFixture(fixDef);
            //body.setHealth(9999999);
            body.setLinearDamping(0.0005f);

            if(k>0) {
                //Create distance joint
                DistanceJointDef distJDef = new DistanceJointDef();
                Vector2 anchor1 = prevBody.getWorldCenter();
                Vector2 anchor2 = body.getWorldCenter();
                distJDef.initialize(prevBody, body, anchor1, anchor2);
                distJDef.collideConnected = false;
                distJDef.dampingRatio = dampingRatio;
                distJDef.frequencyHz = frequencyHz;
                ropeJoints.add(world.createJoint(distJDef));

                //Create rope joint
                RopeJointDef rDef = new RopeJointDef();
                rDef.maxLength = (body.getPosition().sub(prevBody.getPosition())).len() * kMaxWidth;
                rDef.localAnchorA.set(rDef.localAnchorB.set(0,0));
                rDef.bodyA = prevBody;
                rDef.bodyB = body;
                ropeJoints.add(world.createJoint(rDef));

            } //if k>0
            prevBody = body;

            ropeBodies.add(body);
        } //for
        if (prevBody != null) {
            DistanceJointDef distJDef = new DistanceJointDef();
            Vector2 anchor1 = prevBody.getWorldCenter();
            Vector2 anchor2 = thePlayer.getBody().getWorldCenter();
            distJDef.initialize(prevBody, thePlayer.getBody(), anchor1, anchor2);
            distJDef.collideConnected = false;
            distJDef.dampingRatio = dampingRatio;
            distJDef.frequencyHz = frequencyHz;
            world.createJoint(distJDef);

            //Create rope joint
            RopeJointDef rDef = new RopeJointDef();
            rDef.maxLength = (thePlayer.getPosition().sub(prevBody.getPosition())).len() * kMaxWidth;
            rDef.localAnchorA.set(rDef.localAnchorB.set(0, 0));
            rDef.bodyA = prevBody;
            rDef.bodyB = thePlayer.getBody();
            world.createJoint(rDef);
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
    }

    public void togglePause() {
        isPaused = !isPaused;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void rope() {
        doRope(playerGrav);
    }

    public void jump() {
        if (thePlayer.canJump())
            doJump(playerGrav);
    }
}
