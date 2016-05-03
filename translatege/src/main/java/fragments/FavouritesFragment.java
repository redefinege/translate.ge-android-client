package fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import adapters.FavouritesAdapter;
import extras.FavouritesProvider;
import ge.redefine.translatege.R;

public class FavouritesFragment extends Fragment {

    private Toolbar mAppBar;
    private RecyclerView mRecyclerView;
    private FavouritesAdapter.OnClickListener listener;

    public FavouritesFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            listener = (FavouritesAdapter.OnClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FavouritesAdapter.OnClickListener");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (FavouritesAdapter.OnClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement FavouritesAdapter.OnClickListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_favourites, container, false);
        mAppBar = (Toolbar) v.findViewById(R.id.toolbar_favourites);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_favourites);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FavouritesAdapter adapter = new FavouritesAdapter(FavouritesProvider.getInstance().fetchFavourites(), listener, getActivity());

        mAppBar.setTitle(R.string.favourites);
        mAppBar.setTitleTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccentDark));

        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }
}
