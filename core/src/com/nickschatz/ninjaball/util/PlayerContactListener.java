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

package com.nickschatz.ninjaball.util;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.nickschatz.ninjaball.entity.Player;

public class PlayerContactListener implements ContactListener {
    private Player player;
    private int numContacts = 0;

    public PlayerContactListener(Player player) {
        this.player = player;
    }

    @Override
    public void beginContact(Contact contact) {
        if (contact.getFixtureA() != null
                && contact.getFixtureA().getUserData() instanceof Integer
                && contact.getFixtureA().getUserData() == UserData.PLAYER_SENSOR) {
            numContacts++;
        }
        if (contact.getFixtureB() != null
                && contact.getFixtureB().getUserData() instanceof Integer
                && contact.getFixtureB().getUserData() == UserData.PLAYER_SENSOR) {
            numContacts++;
        }
        player.setCanJump(numContacts > 0);
    }

    @Override
    public void endContact(Contact contact) {
        if (contact.getFixtureA() != null
                && contact.getFixtureA().getUserData() instanceof Integer
                && contact.getFixtureA().getUserData() == UserData.PLAYER_SENSOR) {
            numContacts--;
        }
        if (contact.getFixtureB() != null
                && contact.getFixtureB().getUserData() instanceof Integer
                && contact.getFixtureB().getUserData() == UserData.PLAYER_SENSOR) {
            numContacts--;
        }
        player.setCanJump(numContacts > 0);
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
