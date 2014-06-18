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

import com.nickschatz.ninjaball.screen.GameScreen;
import com.nickschatz.ninjaball.util.UserData;

public class PlayerExitContactExecutor extends IntegerContactExecutor {

    private GameScreen gameScreen;

    public PlayerExitContactExecutor(GameScreen gameScreen) {
        super(UserData.PLAYER_SENSOR, UserData.EXIT);
        this.gameScreen = gameScreen;
    }

    @Override
    public void beginContact(Integer userDataA, Integer userDataB) {
        gameScreen.nextLevel();
    }

    @Override
    public void endContact(Integer userDataA, Integer userDataB) {

    }
}
