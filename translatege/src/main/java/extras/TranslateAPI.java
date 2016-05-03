package extras;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;


public class TranslateAPI {
    private static TranslateAPI sInstance;
    private RequestQueue mRequestQueue;
    private ResponseListener responseListener;

    private static final String URL_SEARCH = "http://translate.ge/api/:query";
    private static final String URL_FULL_SEARCH = "http://translate.ge/api/search/:query";
    public static final String JSON_ROWS = "rows";
    public static final String JSON_VALUE = "value";
    public static final String JSON_TEXT = "Text";
    public static final String JSON_WORD = "Word";
    public static final String JSON_DICT = "DictType";

    private TranslateAPI() {
    }

    public static TranslateAPI getInstance() {
        if (sInstance == null) {
            sInstance = new TranslateAPI();
        }
        return sInstance;
    }

    public void getResults(String word, ResponseListener response) {
        if (!checkRequestQueue() || word.isEmpty()) {
            response.onErrorResponse();
            return;
        }
        responseListener = response;

        String requestUrl = URL_SEARCH.replace(":query", word);
        try {
            requestUrl = new URI(requestUrl).toASCIIString();
        } catch (URISyntaxException ignored) {
            response.onErrorResponse();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, requestUrl,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        responseListener.onSuccessResponse(convertResponseToStruct(response));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        responseListener.onErrorResponse();
                    }
                }
        );

        mRequestQueue.add(request);
    }

    //TODO: Implement this method
    public String getFullSearchResults(String word) {
        return "";
    }

    public void setRequestQueue(RequestQueue requestQueue) {
        mRequestQueue = requestQueue;
    }

    private boolean checkRequestQueue() {
        return (mRequestQueue != null);
    }

    private ArrayList<ResultsStruct> convertResponseToStruct(JSONObject data) {
        ArrayList<ResultsStruct> newArray = new ArrayList<>();

        try {
            JSONArray row = data.getJSONArray(JSON_ROWS);

            for (int i = 0; i < row.length(); i++) {
                JSONObject currentValues = row.getJSONObject(i).getJSONObject(JSON_VALUE);
                ResultsStruct currentStruct = new ResultsStruct();

                currentStruct.word(currentValues.getString(JSON_WORD));
                currentStruct.text(currentValues.getString(JSON_TEXT));
                currentStruct.dict(Integer.toString(currentValues.getInt(JSON_DICT)));

                newArray.add(currentStruct);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return newArray;
    }

    public interface ResponseListener {
        void onSuccessResponse(ArrayList<ResultsStruct> data);

        void onErrorResponse();
    }

}
