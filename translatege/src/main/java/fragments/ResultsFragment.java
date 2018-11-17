package fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import adapters.FavouritesAdapter;
import adapters.ResultsAdapter;
import extras.ResultsStruct;
import ge.redefine.translatege.R;

public class ResultsFragment extends Fragment {

    private TextToSpeech textToSpeech;
    private RecyclerView recyclerView;
    private ResultsAdapter resultsAdapter;
    private ArrayList<ResultsStruct> resultsData;

    public ResultsFragment() {
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textToSpeech = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                textToSpeech.setLanguage(Locale.US);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_results, container, false);

        // Configure RecyclerView
        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_results);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        resultsAdapter = new ResultsAdapter(
                new ArrayList<>(Collections.<ResultsStruct>emptyList()),
                getActivity(),
                textToSpeech,
                new ResultsAdapter.OnFavouritesClickedListener() {
                    @Override
                    public void onFavouriteClicked() {
                        RecyclerView recyclerView = (RecyclerView) getActivity().findViewById(R.id.recycler_favourites);
                        FavouritesAdapter adapter = (FavouritesAdapter) recyclerView.getAdapter();
                        adapter.syncFromDB();
                    }

                    @Override
                    public void onTTSClicked(final String word) {
                        ttsSpeak(word);
                    }

                    @Override
                    public void onItemTouch() {
                        View view = getActivity().getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                    }
                });
        recyclerView.setAdapter(resultsAdapter);

        if (resultsData != null) resultsAdapter.updateListItems(resultsData);
    }

    public void updateResults(ArrayList<ResultsStruct> data) {
        resultsData = data;
        if (resultsAdapter != null) resultsAdapter.updateListItems(data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) textToSpeech.shutdown();
    }

    private void ttsSpeak(String word) {
        if (textToSpeech != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null);
            } else {
                textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        }
    }
}
