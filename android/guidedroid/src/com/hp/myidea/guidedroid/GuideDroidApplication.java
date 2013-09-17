/**
 * 
 */
package com.hp.myidea.guidedroid;

import android.app.Application;
import android.content.Context;
import android.preference.PreferenceManager;

/**
 * @author mauricio
 *
 */
public class GuideDroidApplication extends Application {
    private static Context context;
    public static final String GUIDE_DROID_PREFS = "GuideDroidSharedPrefs";

    @Override
    public void onCreate() {
        super.onCreate();
        GuideDroidApplication.context = this.getApplicationContext();
        PreferenceManager.setDefaultValues(this, R.xml.guidedroid_preference, false);
    }

    public static Context getContext() {
            return GuideDroidApplication.context;
    }

}
