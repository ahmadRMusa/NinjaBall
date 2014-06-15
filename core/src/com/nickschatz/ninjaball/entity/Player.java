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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.nickschatz.ninjaball.Resources;
import com.nickschatz.ninjaball.util.UserData;

public class Player {
    private Body myBody;
    private boolean canJump = true;
    private final float radius;
    private boolean hasRope = false;
    private Texture ball;


    private Rope rope;

    private Sound jumpSound;

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
        fixtureDef.density = 6f;
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

        ball = Resources.get().get("data/ball64x64.png", Texture.class);

        myBody.setUserData(this);


        jumpSound = Resources.get().get("data/sound/jump.wav", Sound.class);
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
        if (canJump()) {
            myBody.applyLinearImpulse(playerGrav.cpy().rotate(180).scl(2), myBody.getWorldCenter(), true);
            jumpSound.play();
        }
    }

    public Rope getRope() {
        return rope;
    }

    public void throwRope(Vector2 playerGrav, World world) {
        if (hasRope) {
            hasRope = false;

            rope.destroy();
            rope = null;

            return;
        }
        hasRope = true;
        rope = new Rope(this, playerGrav, world);
    }

    public boolean hasRope() {
        return hasRope;
    }

    public void draw(SpriteBatch batch) {
        int textureWidth = ball.getWidth();
        int textureHeight = ball.getHeight();

        TextureRegion ballRegion = new TextureRegion(ball, 0, 0, textureWidth, textureHeight);
        batch.draw(ballRegion,
                getPosition().x - getRadius(),
                getPosition().y - getRadius(),
                getRadius(),
                getRadius(),
                getRadius() * 2,
                getRadius() * 2, 1, 1, (float) Math.toDegrees(getRotation()), false);
    }
}
