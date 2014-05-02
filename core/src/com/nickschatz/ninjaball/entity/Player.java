package com.nickschatz.ninjaball.entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Player {
    private Body myBody;
    private boolean canJump = true;

    public Player(World world, float x, float y) {
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
        circle.setRadius(6f);

// Create a fixture definition to apply our shape to
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.6f; // Make it bounce a little bit

// Create our fixture and attach it to the body
        Fixture fixture = myBody.createFixture(fixtureDef);
        myBody.setGravityScale(0);

// Remember to dispose of any shapes after you're done with them!
// BodyDef and FixtureDef don't need disposing, but shapes do.
        circle.dispose();

        myBody.setUserData(this);
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
}
