package ge.redefine.translatege;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Locale;

import adapters.FavouritesAdapter;
import adapters.ResultsAdapter;
import extras.DatabaseDownloader;
import extras.PreferencesManager;
import extras.ResultsProvider;
import extras.ResultsStruct;
import fragments.FavouritesFragment;
import fragments.NoResultsFragment;
import fragments.ResultsFragment;
import me.zhanghai.android.materialprogressbar.IndeterminateHorizontalProgressDrawable;

public class MainActivity extends AppCompatActivity
        implements FavouritesAdapter.OnClickListener {

    public static final String DATABASE_NAME = "dictionary.db";

    private static final String TAG_RESULTS = "results";
    private static final String TAG_NO_RESULTS = "no_results";
    private static final String TAG_FAVOURITES = "favourites";
    private static final String ALIAS_QUERY_TEXT = "queryText";
    private static final int REQ_CODE_SPEECH_INPUT = 100;

    private ResultsProvider mResultsProvider;
    private DatabaseDownloader mDatabaseDownloader;
    private PreferencesManager mPreferencesManager;

    // DrawerLayout
    private DrawerLayout drawerLayout;

    // AppBar Controls
    private EditText etSearch;
    private ImageButton ibClear;
    private ImageButton ibVoice;
    private ImageButton ibSettings;
    private ProgressBar progressBar;

    // Fragments
    private ResultsFragment resultsFragment;
    private NoResultsFragment noResultsFragment;
    private FavouritesFragment favouritesFragment;

    //<editor-fold desc="Overridden Methods">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize some objects
        mResultsProvider = new ResultsProvider();
        mDatabaseDownloader = DatabaseDownloader.getInstance();
        mPreferencesManager = PreferencesManager.getInstance();

        // get views
        drawerLayout = (DrawerLayout) findViewById(R.id.layout_drawer);

        etSearch = (EditText) findViewById(R.id.edit_text_search);
        ibClear = (ImageButton) findViewById(R.id.appbar_button_clear);
        ibVoice = (ImageButton) findViewById(R.id.appbar_button_voice);
        ibSettings = (ImageButton) findViewById(R.id.appbar_button_settings);
        configureToolBar();

        // show voice button by default
        searchViewShowVoice();

        // ProgressBar
        progressBar = (ProgressBar) findViewById(R.id.progressbar_main);
        progressBar.setIndeterminateDrawable(new IndeterminateHorizontalProgressDrawable(this));
        progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        progressBarHide();

        // Fragments
        buildFragments();

        // restore state
        if (savedInstanceState != null
                && savedInstanceState.getString(ALIAS_QUERY_TEXT) != null) {
            setQueryText(savedInstanceState.getString(ALIAS_QUERY_TEXT));
        } else {
            setQueryText(null);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Check for empty query to avoid crash on orientation change
        String query = etSearch.getText() == null ? "" : etSearch.getText().toString();
        if (!query.isEmpty())
            outState.putString(ALIAS_QUERY_TEXT, query);
    }

    //</editor-fold>

    //<editor-fold desc="Private Methods">
    private void configureToolBar() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                final String newText = s.toString();
                if (newText.isEmpty()) {
                    searchViewShowVoice();
                    updateResults(new ArrayList<ResultsStruct>(), newText);
                    return;
                }

                searchViewShowClear();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (newText.equals(etSearch.getText().toString()))
                            new SearchTask().execute(newText);
                    }
                }, 300);
            }
        });

        ibClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear text from search field
                setQueryText(null);

                // Show soft keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        ibVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                        Locale.ENGLISH);

                try {
                    startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        ibSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_layout_results, fragment);
        transaction.commit();
    }

    private void buildFragments() {
        FragmentManager manager = getSupportFragmentManager();

        resultsFragment = manager.findFragmentByTag(TAG_RESULTS) == null
                ? new ResultsFragment()
                : (ResultsFragment) manager.findFragmentByTag(TAG_RESULTS);

        noResultsFragment = manager.findFragmentByTag(TAG_NO_RESULTS) == null
                ? new NoResultsFragment()
                : (NoResultsFragment) manager.findFragmentByTag(TAG_NO_RESULTS);

        favouritesFragment = manager.findFragmentByTag(TAG_FAVOURITES) == null
                ? new FavouritesFragment()
                : (FavouritesFragment) manager.findFragmentByTag(TAG_FAVOURITES);

        FragmentTransaction transaction = manager.beginTransaction();
        if (!favouritesFragment.isAdded())
            transaction.add(R.id.fragment_layout_favourites, favouritesFragment, TAG_FAVOURITES);

        transaction.commit();
    }

    /**
     * Update list in RecyclerView with provided data
     *
     * @param data data to update list with
     */
    private void updateResults(ArrayList<ResultsStruct> data, String queryText) {
        if (data == null)
            data = new ArrayList<>();
        if (queryText == null)
            queryText = "";

        if (queryText.isEmpty()) {
            replaceFragment(noResultsFragment);
            noResultsFragment.setState(NoResultsFragment.STATE_NO_QUERY);
        } else if (data.size() == 0) {
            replaceFragment(noResultsFragment);
            noResultsFragment.setState(NoResultsFragment.STATE_NO_RESULTS);
        } else {
            replaceFragment(resultsFragment);
            resultsFragment.updateResults(data);
        }
    }

    /**
     * Receiving speech input
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    setQueryText(result.get(0));
                }
                break;
            }
        }
    }

    private void searchViewShowVoice() {
        ibClear.setVisibility(View.INVISIBLE);
        ibVoice.setVisibility(View.VISIBLE);
    }

    private void searchViewShowClear() {
        ibVoice.setVisibility(View.INVISIBLE);
        ibClear.setVisibility(View.VISIBLE);
    }

    private void progressBarShow() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void progressBarHide() {
        progressBar.setVisibility(View.INVISIBLE);
    }
    //</editor-fold>

    //<editor-fold desc="Public Methods">
    public void setQueryText(String text) {
        if (text == null) text = "";
        etSearch.setText(text);
    }

    @Override
    public void onFavouriteDeleteClicked() {
        //TODO: Improve this approach
        if (resultsFragment.getRecyclerView() != null) {
            ((ResultsAdapter) resultsFragment.getRecyclerView().getAdapter())
                    .refreshListItems();
        }
    }

    @Override
    public void onFavouriteItemClicked(String word) {
        drawerLayout.closeDrawer(GravityCompat.END);
        setQueryText(word);
    }
    //</editor-fold>


    private class SearchTask extends AsyncTask<String, Void, ArrayList<ResultsStruct>> {

        private String queryText;

        @Override
        protected void onPreExecute() {
            progressBarShow();
        }

        @Override
        protected ArrayList<ResultsStruct> doInBackground(String... params) {
            if (params.length <= 0)
                return null;

            queryText = params[0];
            return mResultsProvider.getResults(queryText, ResultsProvider.REQ_TYPE_AUTO);
        }

        @Override
        protected void onPostExecute(ArrayList<ResultsStruct> arrayList) {
            progressBarHide();
            if (queryText.equals(etSearch.getText().toString()))
                updateResults(arrayList, queryText);
        }
    }

}
