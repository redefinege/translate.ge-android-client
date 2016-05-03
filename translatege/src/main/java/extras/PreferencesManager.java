package extras;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashMap;
import java.util.Map;

import ge.redefine.translatege.R;

public class PreferencesManager {

    public static final String PREF_LANGUAGE = "pref_language";
    public static final String PREF_DOWNLOAD = "pref_download";
    public static final String PREF_LICENSES = "pref_licenses";
    public static final String PREF_DATABASE_VERSION = "pref_database_version";
    private String langDefault;
    private Map<String, String> langCodes;

    private static PreferencesManager sInstance;
    private static Context mContext;
    private SharedPreferences preferences;

    private PreferencesManager() {
        preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        langDefault = mContext.getResources().getString(R.string.langDefault);

        String[] langArray = mContext.getResources().getStringArray(R.array.langArray);
        String[] langCodesArray = mContext.getResources().getStringArray(R.array.langCodesArray);

        langCodes = new HashMap<>();
        for (int i = 0; i < langArray.length; i++) {
            langCodes.put(langArray[i], langCodesArray[i]);
        }
    }

    public static PreferencesManager getInstance() {
        if (sInstance == null) sInstance = new PreferencesManager();
        return sInstance;
    }

    private SharedPreferences.Editor getEditor() {
        return preferences.edit();
    }

    public static void setContext(Context context) {
        mContext = context;
    }

    public String getLanguage() {
        return preferences.getString(PREF_LANGUAGE, langDefault);
    }

    public String getLanguageCode() {
        return langCodes.get(getLanguage());
    }

    public int getDatabaseVersion() {
        return preferences.getInt(PREF_DATABASE_VERSION, 0);
    }

    public void setDatabaseVersion(int version) {
        SharedPreferences.Editor editor = getEditor();
        editor.putInt(PREF_DATABASE_VERSION, version);
        editor.apply();
    }

}
