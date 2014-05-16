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
import com.badlogic.gdx.physics.box2d.Manifold;
import com.nickschatz.ninjaball.entity.Player;

public class PlayerContactListener implements com.badlogic.gdx.physics.box2d.ContactListener {

    @Override
    public void beginContact(Contact contact) {
        if (contact.getFixtureA() != null && contact.getFixtureA().getBody() != null && contact.getFixtureA().getBody().getUserData() instanceof Player) {
            Player player = (Player) contact.getFixtureA().getBody().getUserData();
            player.setCanJump(true);
        }
        else if (contact.getFixtureB() != null && contact.getFixtureB().getBody() != null && contact.getFixtureB().getBody().getUserData() instanceof Player) {
            Player player = (Player) contact.getFixtureB().getBody().getUserData();
            player.setCanJump(true);
        }
    }

    @Override
    public void endContact(Contact contact) {
        if (contact.getFixtureA() != null && contact.getFixtureA().getBody() != null && contact.getFixtureA().getBody().getUserData() instanceof Player) {
            Player player = (Player) contact.getFixtureA().getBody().getUserData();
            player.setCanJump(false);
        }
        else if (contact.getFixtureB() != null && contact.getFixtureB().getBody() != null && contact.getFixtureB().getBody().getUserData() instanceof Player) {
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
