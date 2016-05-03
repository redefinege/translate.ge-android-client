package extras;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class FavouritesProvider {

    private static FavouritesProvider mInstance;
    private SQLiteHelper helper;

    private FavouritesProvider() {
    }

    public void setContext(Context context) {
        helper = new SQLiteHelper(context);
    }

    public static FavouritesProvider getInstance() {
        if (mInstance == null) mInstance = new FavouritesProvider();
        return mInstance;
    }

    public long insertWord(String word) {
        SQLiteDatabase database = helper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SQLiteHelper.TABLE_WORD, word);
        return database.insert(SQLiteHelper.TABLE_NAME, null, contentValues);
    }

    public int deleteWord(String word) {
        SQLiteDatabase database = helper.getWritableDatabase();
        return database.delete(SQLiteHelper.TABLE_NAME, SQLiteHelper.TABLE_WORD + "=?", new String[]{word});
    }

    public ArrayList<String> fetchFavourites() {
        SQLiteDatabase database = helper.getReadableDatabase();

        String[] columns = {SQLiteHelper.TABLE_WORD};
        Cursor cursor = database.query(SQLiteHelper.TABLE_NAME, columns,
                null, null, null, null, SQLiteHelper.TABLE_ID);

        ArrayList<String> data = new ArrayList<>();
        while (cursor.moveToNext()) {
            String word = cursor.getString(cursor.getColumnIndex(SQLiteHelper.TABLE_WORD));
            data.add(0, word);
        }

        cursor.close();
        return data;
    }

    private static class SQLiteHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "favourites";
        private static final String TABLE_NAME = "list";
        private static final String TABLE_ID = "_id";
        private static final String TABLE_WORD = "word";
        private static final int DATABASE_VERSION = 3;

        private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + TABLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + TABLE_WORD + " VARCHAR (255))";

        public SQLiteHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(CREATE_TABLE);
            } catch (SQLException ignored) {
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onCreate(db);
        }
    }

}

