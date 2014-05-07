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

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Logger;

public class TiledLightManager {
    private final RayHandler rayHandler;
    private final Logger log;

    public TiledLightManager(RayHandler rayHandler, TiledMap tiledMap, String layerName, int logLevel) {
        this.rayHandler = rayHandler;
        log = new Logger("TiledLightManager", logLevel);

        MapLayer layer = tiledMap.getLayers().get(layerName);

        for (MapObject object : layer.getObjects()) {
            if (!(object instanceof RectangleMapObject)) continue;

            RectangleMapObject mapObject = (RectangleMapObject) object;
            MapProperties properties = object.getProperties();
            float r = 0;
            if (properties.containsKey("r"))
                r = Float.parseFloat(properties.get("r", String.class))/255F;
            float g = 0;
            if (properties.containsKey("g"))
                g = Float.parseFloat(properties.get("g", String.class))/255F;
            float b = 0;
            if (properties.containsKey("b"))
                b = Float.parseFloat(properties.get("b", String.class))/255F;

            int rays = 256;
            if (properties.containsKey("rays")) rays = Integer.parseInt(properties.get("rays", String.class));
            int distance = 512;
            if (properties.containsKey("distance")) distance = Integer.parseInt(properties.get("distance", String.class));

            new PointLight(rayHandler, rays, new Color(r,g,b,1), distance, mapObject.getRectangle().getX(), mapObject.getRectangle().getY());
        }
    }

    public void updateAndRender() {
        rayHandler.updateAndRender();
    }

    public void dispose() {
        rayHandler.dispose();
    }

    public void setCombinedMatrix(Matrix4 combined) {
        rayHandler.setCombinedMatrix(combined);
    }

    public void setCulling(boolean culling) {
        rayHandler.setCulling(culling);
    }

    public void setShadows(boolean shadows) {
        rayHandler.setShadows(shadows);
    }

    public void setAmbientLight(Color ambientLightColor) {
        rayHandler.setAmbientLight(ambientLightColor);
    }
}
