package ge.redefine.translatege;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import java.util.Locale;

import extras.FavouritesProvider;
import extras.PreferencesManager;
import extras.TranslateAPI;
import network.VolleySingleton;

//TODO: provide some kind of information to user after dictionary download

public class App extends Application {

    private static App sInstance;
    private Locale locale;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        // initialize some stuff here
        FavouritesProvider.getInstance().setContext(getApplicationContext());
        PreferencesManager.setContext(getApplicationContext());
        TranslateAPI.getInstance().setRequestQueue(
                VolleySingleton.getInstance().getRequestQueue()
        );

        // set locale
        String lang = PreferencesManager.getInstance().getLanguageCode();
        if (lang != null) {
            locale = new Locale(lang);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getApplicationContext().getResources().updateConfiguration(config, null);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (locale != null) {
            newConfig.locale = locale;
            Locale.setDefault(locale);
            getApplicationContext().getResources().updateConfiguration(newConfig, null);
        }
    }

    public static Context getAppContext() {
        return sInstance;
    }

}
