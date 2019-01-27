package adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import extras.FavouritesProvider;
import extras.ResultsStruct;
import ge.redefine.translatege.R;

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ResultsViewHolder> {

    private LayoutInflater mInflater;
    private ArrayList<ResultsStruct> itemList;
    private Context mContext;
    private FavouritesProvider favouritesProvider;
    private ClipboardManager clipboardManager;
    private TextToSpeech textToSpeech;
    private OnFavouritesClickedListener listener;

    public ResultsAdapter(ArrayList<ResultsStruct> _list,
                          Context _context,
                          TextToSpeech _textToSpeech,
                          OnFavouritesClickedListener _listener) {
        itemList = _list;
        listener = _listener;
        mContext = _context;
        mInflater = LayoutInflater.from(_context);
        favouritesProvider = FavouritesProvider.getInstance();
        textToSpeech = _textToSpeech;
        clipboardManager = (ClipboardManager) _context.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    public void refreshListItems() {
        if (getItemCount() <= 0) return;

        ArrayList<String> favourites = favouritesProvider.fetchFavourites();
        for (ResultsStruct current : itemList) {
            if (current.isFavourite() != favourites.contains(current.word())) {
                current.favourite(!current.isFavourite());
                notifyItemChanged(itemList.indexOf(current));
            }
        }
    }

    public void updateListItems(ArrayList<ResultsStruct> list) {
        int sizeCurrent = getItemCount();
        int sizeNew = list == null ? 0 : list.size();

        if (sizeNew > 0) {
            ArrayList<String> favourites = favouritesProvider.fetchFavourites();
            if (favourites.size() > 0) {
                for (ResultsStruct current : list) {
                    current.favourite(favourites.contains(current.word()));
                }
            }
        }

        // remove items
        itemList.clear();
        itemList = list;
        if (sizeNew > sizeCurrent) {
            notifyItemRangeInserted(sizeCurrent, sizeNew - sizeCurrent);
        } else if (sizeNew < sizeCurrent) {
            notifyItemRangeRemoved(sizeCurrent, sizeCurrent - sizeNew);
        }
        if (sizeNew > 0) {
            notifyDataSetChanged();
        }
    }

    private void syncFavourites(int position) {
        ResultsStruct current = itemList.get(position);
        if (current.isFavourite()) {
            favouritesProvider.insertWord(current.word());
        } else {
            favouritesProvider.deleteWord(current.word());
        }
    }

    private void updateFavouritesButton(ImageButton button, int position) {
        ResultsStruct current = itemList.get(position);
        if (current.isFavourite()) {
            button.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_star_black_24dp));
        } else {
            button.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_star_border_black_24dp));
        }
    }

    private void updateText(final ResultsViewHolder holder, int position) {
        ResultsStruct current = itemList.get(position);
        String formattedText = formatText(current.text());

        holder.textQuery.setText(current.word());
        holder.textResult.setText(Html.fromHtml(formattedText));

        if (current.isExpanded()) {
            holder.textResult.setMaxLines(1000);
            holder.imageExpand.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_arrow_drop_up_black_24dp));
        } else {
            holder.textResult.setMaxLines(15);
            holder.imageExpand.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_arrow_drop_down_black_24dp));
        }

        holder.textResult.post(new Runnable() {
            @Override
            public void run() {
                if (holder.textResult.getLineCount() <= 15) {
                    holder.imageExpand.setVisibility(View.INVISIBLE);
                } else {
                    holder.imageExpand.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private String formatText(String text) {
        String[] words = {"adjective", "adverb", "conjunction", "interjection", "noun", "prefix", "preposition", "verb"};

        for (String word : words) {
            text = text.replace(word, "<font color='#8BC34A'><i>" + word + "</i></font>");
        }
        text = text.trim().replace("\r\n", "<br/>");

        return text;
    }

    private void copyToClipboard(int position) {
        if (position < 0 || position >= getItemCount()) return;

        ResultsStruct current = itemList.get(position);
        ClipData clipData = ClipData.newPlainText(current.word(), current.text());
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(mContext, mContext.getResources().getString(R.string.textCopied), Toast.LENGTH_SHORT).show();
    }

    private void toggleExpand(int position) {
        if (position < 0 || position >= getItemCount()) return;

        ResultsStruct current = itemList.get(position);
        current.expanded(!current.isExpanded());
    }

    private void toggleButton(int position) {
        if (position < 0 || position >= getItemCount()) return;

        ResultsStruct current = itemList.get(position);
        current.favourite(!current.isFavourite());
    }

    private void updateExpandState(LinearLayout linearLayout, int position) {
        if (position < 0 || position >= getItemCount()) return;

        ResultsStruct current = itemList.get(position);
        if (current.isExpanded()) {
            linearLayout.setVisibility(View.VISIBLE);
        } else {
            linearLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public ResultsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.list_cardview, parent, false);
        return new ResultsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ResultsViewHolder holder, int position) {
        updateText(holder, position);
        updateFavouritesButton(holder.buttonFavourites, position);
    }

    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }

    public class ResultsViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnTouchListener {

        public TextView textQuery;
        public TextView textResult;
        public LinearLayout buttonSpeech;
        public ImageButton buttonFavourites;
        public ImageButton buttonCopy;
        public LinearLayout buttonExpand;
        public ImageView imageExpand;

        public ResultsViewHolder(View itemView) {
            super(itemView);

            textQuery = (TextView) itemView.findViewById(R.id.text_results_query);
            textResult = (TextView) itemView.findViewById(R.id.text_results_new);

            buttonSpeech = (LinearLayout) itemView.findViewById(R.id.card_button_speech);
            buttonFavourites = (ImageButton) itemView.findViewById(R.id.card_button_favourites);
            buttonCopy = (ImageButton) itemView.findViewById(R.id.card_button_copy);
            buttonExpand = (LinearLayout) itemView.findViewById(R.id.card_button_expand);
            imageExpand = (ImageView) itemView.findViewById(R.id.card_image_expand);

            buttonSpeech.setOnClickListener(this);
            buttonFavourites.setOnClickListener(this);
            buttonCopy.setOnClickListener(this);
            buttonExpand.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();

            if (v == buttonSpeech) {
                if (position < 0 || position >= getItemCount()) return;

                ResultsStruct current = itemList.get(position);
                listener.onTTSClicked(current.word());
            } else if (v == buttonFavourites) {
                toggleButton(position);
                updateFavouritesButton(buttonFavourites, position);
                syncFavourites(position);
                listener.onFavouriteClicked();
            } else if (v == buttonCopy) {
                copyToClipboard(position);
            } else if (v == buttonExpand) {
                toggleExpand(position);
                notifyItemChanged(position);
            }
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
//            listener.onItemTouch();
            return false;
        }
    }

    public interface OnFavouritesClickedListener {
        void onFavouriteClicked();

        void onTTSClicked(String word);

        void onItemTouch();
    }
}
