package fragments;


import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ge.redefine.translatege.R;

public class NoResultsFragment extends Fragment {

    public static final int STATE_NO_QUERY = 1;
    public static final int STATE_NO_RESULTS = 2;
    public static final int STATE_NO_NETWORK = 3;

    private Context context;

    private ImageView imageView;
    private TextView statusText;
    private int currentState;

    public NoResultsFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.context = activity.getApplicationContext();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context.getApplicationContext();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_no_results, container, false);

        imageView = (ImageView) v.findViewById(R.id.status_image);
        statusText = (TextView) v.findViewById(R.id.status_text);

        statusText.setTypeface(Typeface.SERIF);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            imageView.setImageAlpha(128);
        } else {
            imageView.setAlpha(128);
        }
        imageView.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);

        setState(currentState);
        return v;
    }

    public void setState(int state) {
        if (imageView == null || statusText == null) {
            currentState = state;
            return;
        }

        switch (state) {
            case STATE_NO_QUERY:
                setImageDrawable(R.drawable.ic_search_black_24dp);
                statusText.setText(R.string.noQueryText);
                break;

            case STATE_NO_RESULTS:
                setImageDrawable(R.drawable.ic_mood_bad_black_24dp);
                statusText.setText(R.string.noResultsFound);
                break;

            case STATE_NO_NETWORK:
                setImageDrawable(R.drawable.ic_cloud_off_black_24dp);
                statusText.setText(R.string.noNetwork);
                break;

        }
    }

    private void setImageDrawable(int drawable) {
        if (imageView != null) {
            imageView.setImageDrawable(ContextCompat.getDrawable(context, drawable));
        }
    }

}
