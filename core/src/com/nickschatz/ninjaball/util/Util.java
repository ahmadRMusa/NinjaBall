package com.nickschatz.ninjaball.util;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;

public class Util {
    public static float getCameraCurrentXYAngle(Camera cam)
    {
        return (float)Math.atan2(cam.up.x, cam.up.y)* MathUtils.radiansToDegrees;
    }
}
