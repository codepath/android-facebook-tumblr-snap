package com.codepath.apps.tumblrsnap;

import com.facebook.drawee.backends.pipeline.Fresco;
import android.app.Application;
import android.content.SharedPreferences;

public class TumblrSnapApp extends Application {
    private static TumblrSnapApp instance;

    public static TumblrSnapApp getInstance() {
        return instance;
    }

    public static SharedPreferences getSharedPreferences() {
        if (instance != null) {
            return instance.getSharedPreferences("tumblrsnap", 0);
        }

        return null;
    }

    public static TumblrClient getClient() {
        return (TumblrClient) TumblrClient.getInstance(TumblrClient.class,
                instance);
    }

    @Override
    public void onCreate() {
        TumblrSnapApp.instance = this;
        super.onCreate();

        Fresco.initialize(this);
    }
}
