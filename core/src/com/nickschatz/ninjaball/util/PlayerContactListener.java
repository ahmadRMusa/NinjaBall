package com.nickschatz.ninjaball.util;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.nickschatz.ninjaball.entity.Player;

public class PlayerContactListener implements com.badlogic.gdx.physics.box2d.ContactListener {

    @Override
    public void beginContact(Contact contact) {
        if (contact.getFixtureA().getBody().getUserData() instanceof Player) {
            Player player = (Player) contact.getFixtureA().getBody().getUserData();
            player.setCanJump(true);
        }
        else if (contact.getFixtureB().getBody().getUserData() instanceof Player) {
            Player player = (Player) contact.getFixtureB().getBody().getUserData();
            player.setCanJump(true);
        }
    }

    @Override
    public void endContact(Contact contact) {
        if (contact.getFixtureA().getBody().getUserData() instanceof Player) {
            Player player = (Player) contact.getFixtureA().getBody().getUserData();
            player.setCanJump(false);
        }
        else if (contact.getFixtureB().getBody().getUserData() instanceof Player) {
            Player player = (Player) contact.getFixtureB().getBody().getUserData();
            player.setCanJump(false);
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
