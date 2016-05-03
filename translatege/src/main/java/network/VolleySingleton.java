package network;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import ge.redefine.translatege.App;

public class VolleySingleton {
    private static VolleySingleton sInstance;
    private RequestQueue mRequestQueue;

    private VolleySingleton() {
        mRequestQueue = Volley.newRequestQueue(App.getAppContext());
    }

    public static VolleySingleton getInstance() {
        if (sInstance == null) {
            sInstance = new VolleySingleton();
        }
        return sInstance;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }
}
