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

package com.nickschatz.ninjaball.input;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.nickschatz.ninjaball.screen.GameScreen;

public class GameInput implements InputProcessor {
    private GameScreen gameScreen;
    private Stage stage;

    public GameInput(GameScreen gameScreen, Stage stage) {
        this.gameScreen = gameScreen;
        this.stage = stage;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
            gameScreen.togglePause();
        }
        if (keycode == Input.Keys.Z) {
            gameScreen.rope();
        }
        if (keycode == Input.Keys.X) {
            gameScreen.jump();
        }
        return gameScreen.isPaused() && stage.keyDown(keycode);
    }

    @Override
    public boolean keyUp(int keycode) {
        return gameScreen.isPaused() && stage.keyUp(keycode);
    }

    @Override
    public boolean keyTyped(char character) {
        return gameScreen.isPaused() && stage.keyTyped(character);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {

        return gameScreen.isPaused() && stage.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            if (Gdx.input.getX() > Gdx.graphics.getWidth() / 2) {
                gameScreen.jump();
            }
            else if (Gdx.input.getX() <= Gdx.graphics.getWidth() / 2) {
                gameScreen.rope();
            }
        }
        return gameScreen.isPaused() && stage.touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return gameScreen.isPaused() && stage.touchDragged(screenX, screenY, pointer);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return gameScreen.isPaused() && stage.mouseMoved(screenX, screenY);
    }

    @Override
    public boolean scrolled(int amount) {
        return gameScreen.isPaused() && stage.scrolled(amount);
    }
}
