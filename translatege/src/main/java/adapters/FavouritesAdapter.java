package adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import extras.FavouritesProvider;
import ge.redefine.translatege.R;

public class FavouritesAdapter extends RecyclerView.Adapter<FavouritesAdapter.FavouritesViewHolder> {

    public interface OnClickListener {
        void onFavouriteDeleteClicked();

        void onFavouriteItemClicked(String word);
    }

    //<editor-fold desc="Private Objects">
    private FavouritesProvider favouritesProvider;
    private LayoutInflater mInflater;
    private Context mContext;
    private OnClickListener listener;
    private ArrayList<String> itemList;
    //</editor-fold>

    //<editor-fold desc="Constructors">
    public FavouritesAdapter(ArrayList<String> list, OnClickListener listener, Context context) {
        mInflater = LayoutInflater.from(context);
        this.listener = listener;
        mContext = context;
        itemList = list;
        favouritesProvider = FavouritesProvider.getInstance();
    }
    //</editor-fold>

    //<editor-fold desc="Private Methods">
    private void deleteWord(int position) {
    }

    private void updateText(TextView textView, int position) {
        String current = itemList.get(position);
        textView.setText(current);
    }
    //</editor-fold>

    //<editor-fold desc="Public Methods">
    public void syncFromDB() {
        updateListItems(favouritesProvider.fetchFavourites());
    }

    public void updateListItems(ArrayList<String> list) {
        int sizeCurrent = getItemCount();
        int sizeNew = list == null ? 0 : list.size();

        // remove items
        itemList.clear();
        notifyItemRangeRemoved(0, sizeCurrent);

        // insert items
        itemList = list;
        notifyItemRangeInserted(0, sizeNew);
    }
    //</editor-fold>

    //<editor-fold desc="Class Overridden Methods">
    @Override
    public FavouritesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.list_favourites, parent, false);
        return new FavouritesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FavouritesViewHolder holder, int position) {
        updateText(holder.textView, position);
    }

    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }
    //</editor-fold>

    //<editor-fold desc="ViewHolder Class">
    public class FavouritesViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        TextView textView;
        ImageButton deleteButton;

        public FavouritesViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.text_favourites);
            deleteButton = (ImageButton) itemView.findViewById(R.id.button_delete_favourites);

            textView.setOnClickListener(this);
            deleteButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (v == textView) {
                listener.onFavouriteItemClicked(itemList.get(position));
            } else if (v == deleteButton) {
                try {
                    favouritesProvider.deleteWord(itemList.get(position));
                    itemList.remove(position);
                    notifyItemRemoved(position);
                    listener.onFavouriteDeleteClicked();
                } catch (ArrayIndexOutOfBoundsException ignored) {
                }
            }
        }
    }
    //</editor-fold>
}