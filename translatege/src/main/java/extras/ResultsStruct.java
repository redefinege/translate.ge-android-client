package extras;


public class ResultsStruct {

    private String mWord;
    private String mText;
    private String mDict;
    private boolean mExpanded;
    private boolean mFavourite;

    ResultsStruct() {
        mWord = "";
        mText = "";
        mDict = "";
        mExpanded = false;
        mFavourite = false;
    }

    public String word() { return mWord; }
    public void word(String word) { this.mWord = word; }

    public String text() { return mText; }
    public void text(String text) { this.mText = text; }

    public String dict() { return mDict; }
    public void dict(String dict) { this.mDict = dict; }

    public boolean isExpanded() { return mExpanded; }
    public void expanded() { mExpanded = true; }
    public void expanded(boolean state) { mExpanded = state; }
    public void toggleExpanded() { mExpanded = !mExpanded; }

    public boolean isFavourite() { return mFavourite; }
    public void favourite() { mFavourite = true; }
    public void favourite(boolean state) { mFavourite = state; }
    public void toggleFavourite() { mFavourite = !mFavourite; }

}
