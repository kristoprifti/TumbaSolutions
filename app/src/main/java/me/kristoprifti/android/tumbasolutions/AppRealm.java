package me.kristoprifti.android.tumbasolutions;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Kristi on 1/6/2017.
 */

public class AppRealm extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Realm.init(this);
        RealmConfiguration rc = new RealmConfiguration.Builder().build();
        Realm.setDefaultConfiguration(rc);
    }
}
