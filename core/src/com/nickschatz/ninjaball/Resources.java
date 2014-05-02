package com.nickschatz.ninjaball;

import com.badlogic.gdx.assets.AssetManager;

public class Resources extends AssetManager {
    private static Resources ourInstance;

    public static Resources get() {
        return ourInstance;
    }

    public static Resources init() {
        return ourInstance = new Resources();
    }
    private Resources() {}
    public void dispose() {
        super.dispose();
        ourInstance = null;
    }
}
