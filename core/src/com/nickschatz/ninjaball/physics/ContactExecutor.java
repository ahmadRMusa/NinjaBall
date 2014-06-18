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

package com.nickschatz.ninjaball.physics;

import com.badlogic.gdx.physics.box2d.Fixture;

public abstract class ContactExecutor {
    private Class<?> typeA;
    private Class<?> typeB;

    public ContactExecutor(Class<?> typeA, Class<?> typeB) {
        this.typeA = typeA;
        this.typeB = typeB;
    }

    /**
     * Called when 2 fixtures begin contact.
     * Precondition: Fixture A's UserData is an instance of typeA and Fixture B's UserData i an instance of typeB.
     */
    public abstract void beginContact(Fixture fixtureA, Fixture fixtureB);

    /**
     * Called when 2 fixtures end contact.
     * Precondition: Fixture A's UserData is an instance of typeA and Fixture B's UserData i an instance of typeB.
     */
    public abstract void endContact(Fixture fixtureA, Fixture fixtureB);

    public Class<?> getTypeA() {
        return typeA;
    }
    public Class<?> getTypeB() {
        return typeB;
    }
}
