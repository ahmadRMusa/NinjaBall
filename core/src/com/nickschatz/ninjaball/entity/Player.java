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

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.RopeJointDef;
import com.nickschatz.ninjaball.util.UserData;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private Body myBody;
    private boolean canJump = true;
    private final float radius;
    private boolean hasRope = false;
    private List<Body> ropeBodies;
    private List<Joint> ropeJoints;

    public Player(World world, float x, float y, float radius) {
        this.radius = radius;
        // First we create a body definition
        BodyDef bodyDef = new BodyDef();
// We set our body to dynamic, for something like ground which doesn't move we would set it to StaticBody
        bodyDef.type = BodyDef.BodyType.DynamicBody;
// Set our body's starting position in the world
        bodyDef.position.set(x, y);

// Create our body in the world using our body definition
        myBody = world.createBody(bodyDef);

// Create a circle shape and set its radius to 6
        CircleShape circle = new CircleShape();
        circle.setRadius(radius);

// Create a fixture definition to apply our shape to
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 3f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.2f; // Make it bounce a little bit

// Create our fixture and attach it to the body
        myBody.createFixture(fixtureDef);
        myBody.setGravityScale(0);

// Remember to dispose of any shapes after you're done with them!
// BodyDef and FixtureDef don't need disposing, but shapes do.
        circle.dispose();

        circle = new CircleShape();
        circle.setRadius(radius + 4);
        fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.isSensor = true;
        myBody.createFixture(fixtureDef).setUserData(UserData.PLAYER_SENSOR);


        myBody.setUserData(this);

        ropeJoints = new ArrayList<Joint>();
        ropeBodies = new ArrayList<Body>();
    }

    public Body getBody() {
        return myBody;
    }

    public Vector2 getPosition() {
        return myBody.getPosition();
    }

    public void setCanJump(boolean canJump) {
        this.canJump = canJump;
    }

    public boolean canJump() {
        return canJump;
    }

    public float getRadius() {
        return radius;
    }

    public float getRotation() {
        return this.getBody().getAngle();
    }

    public void jump(Vector2 playerGrav) {
        if (canJump()) myBody.applyLinearImpulse(playerGrav.cpy().rotate(180).scl(2), myBody.getWorldCenter(), true);
    }
    public void throwRope(Vector2 playerGrav, World world) {
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

                              return fraction;
                          }
                      },
                getPosition(),
                getPosition().cpy().add(playerGrav.cpy().rotate(180).nor().scl(300)));
        if (ropeAnchorPos.len() == 0) {
            return;
        }

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
        int countBodyInChain = (int) (getPosition().dst(ropeAnchorPos) / distFactor);
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
            Vector2 anchor2 = getBody().getWorldCenter();
            distJDef.initialize(prevBody, getBody(), anchor1, anchor2);
            distJDef.collideConnected = false;
            distJDef.dampingRatio = dampingRatio;
            distJDef.frequencyHz = frequencyHz;
            world.createJoint(distJDef);

            //Create rope joint
            RopeJointDef rDef = new RopeJointDef();
            rDef.maxLength = (getPosition().sub(prevBody.getPosition())).len() * kMaxWidth;
            rDef.localAnchorA.set(rDef.localAnchorB.set(0, 0));
            rDef.bodyA = prevBody;
            rDef.bodyB = getBody();
            world.createJoint(rDef);
        }
    }

    public List<Body> getRopeBodies() {
        return ropeBodies;
    }
}
