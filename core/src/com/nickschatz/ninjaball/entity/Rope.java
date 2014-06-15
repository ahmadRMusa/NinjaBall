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

package com.nickschatz.ninjaball.entity;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.RopeJointDef;
import com.nickschatz.ninjaball.Resources;

import java.util.ArrayList;
import java.util.List;

public class Rope {
    private boolean hasRope;
    private List<Body> ropeBodies;
    private List<Joint> ropeJoints;
    private Texture ropeKnotTex;
    private Texture ropeTex;
    private Player thePlayer;
    private World world;
    private Sound ropeSound;

    public Rope(Player thePlayer, Vector2 playerGrav, World world) {
        this.thePlayer = thePlayer;
        this.world = world;

        ropeTex = Resources.get().get("data/rope.png", Texture.class);
        ropeKnotTex = Resources.get().get("data/ropeKnot.png", Texture.class);
        ropeKnotTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        ropeSound = Resources.get().get("data/sound/rope.wav", Sound.class);

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
        if (ropeAnchorPos.len() == 0) {
            return;
        }

        ropeSound.play();

        hasRope = !hasRope;
        ropeBodies = new ArrayList<Body>();
        ropeJoints = new ArrayList<Joint>();

        int distFactor = 6; //The resolution of the rope

        Vector2 lastPos = ropeAnchorPos; //set position first body
        float radBody = 6f;
        // Body params
        float density = 0.05f;
        float restitution = 0.5f;
        float friction = 0.5f;
        // Distance joint
        float dampingRatio = 1f;
        float frequencyHz = 15;
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
            bodyDef.bullet = true;
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

    public void draw(SpriteBatch batch) {

        TextureRegion ropeRegion = new TextureRegion(ropeTex, 0, 0, ropeTex.getWidth(), ropeTex.getHeight());
        TextureRegion ropeKnotRegion = new TextureRegion(ropeKnotTex, 0, 0, ropeKnotTex.getWidth(), ropeKnotTex.getHeight());
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

            float width = 6f;

            batch.draw(ropeRegion,
                    botLeft.x, //X
                    botLeft.y,
                    width / 2, //OriginX
                    dst / 2, //OriginY
                    width, //Width
                    dst, //Height
                    1,1, //Scale
                    angle+90  //Rotation
            );

            if (i != 0) {
                //game.batch.disableBlending();
                float scale = 0.2f;

                batch.setBlendFunction(GL20.GL_BLEND_SRC_ALPHA, GL20.GL_BLEND_DST_ALPHA);
                batch.draw(ropeKnotRegion,
                        bodyA.getPosition().x - (ropeKnotRegion.getRegionWidth()/2)*scale, //X
                        bodyA.getPosition().y - (ropeKnotRegion.getRegionHeight()/2)*scale,
                        (ropeKnotRegion.getRegionWidth()/2)*scale, //OriginX
                        (ropeKnotRegion.getRegionHeight()/2)*scale, //OriginY
                        ropeKnotRegion.getRegionWidth(), //Width
                        ropeKnotRegion.getRegionHeight(), //Height
                        scale, scale, //Scale
                        0  //Rotation
                );
                //game.batch.enableBlending();


            }
        }
    }
    public void destroy() {
        for (Joint j : ropeJoints) {
            world.destroyJoint(j);
        }
        ropeJoints.clear();
        for (Body b : ropeBodies) {
            world.destroyBody(b);
        }
        ropeBodies.clear();
    }
}
