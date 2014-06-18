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

public abstract class IntegerContactExecutor extends ContactExecutor {

    private Integer i1;
    private Integer i2;

    public IntegerContactExecutor(Integer i1, Integer i2) {
        super(Integer.class, Integer.class);
        this.i1 = i1;
        this.i2 = i2;
    }

    public abstract void beginContact(Integer userDataA, Integer userDataB);
    public abstract void endContact(Integer userDataA, Integer userDataB);

    @Override
    public void beginContact(Fixture fixtureA, Fixture fixtureB) {
        if (fixtureA.getUserData() == i1) {
            if (fixtureB.getUserData() == i2) {
                this.beginContact(i1, i2);
            }
        }
        if (fixtureA.getUserData() == i2) {
            if (fixtureB.getUserData() == i1) {
                this.beginContact(i2, i1);
            }
        }

    }

    @Override
    public void endContact(Fixture fixtureA, Fixture fixtureB) {
        if (fixtureA.getUserData() == i1) {
            if (fixtureB.getUserData() == i2) {
                this.endContact(i1, i2);
            }
        }
        if (fixtureA.getUserData() == i2) {
            if (fixtureB.getUserData() == i1) {
                this.endContact(i2, i1);
            }
        }

    }
}
