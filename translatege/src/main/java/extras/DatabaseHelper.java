package extras;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import ge.redefine.translatege.App;
import ge.redefine.translatege.MainActivity;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TABLE_EN = "en";
    private static final String TABLE_KA = "ka";
    private static final String COLUMN_WORD = "word";
    private static final int QUERY_LIMIT = 30;

    public DatabaseHelper() {
        super(App.getAppContext(), MainActivity.DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public ArrayList<ResultsStruct> getResults(String word) {
        ArrayList<ResultsStruct> results = new ArrayList<>();
        SQLiteDatabase database = getReadableDatabase();

        String table;
        // Check if first char is Georgian
        if (4304 <= word.charAt(0) && word.charAt(0) <= 4336) {
            table = TABLE_KA;
        } else {
            table = TABLE_EN;
        }

        Cursor cursor = database.query(table
                , null
                , COLUMN_WORD + " LIKE '" + word + "%'"
                , null
                , null
                , null
                , null
                , String.valueOf(QUERY_LIMIT));

        if (cursor.moveToFirst()) {
            do {
                ResultsStruct currentResult = new ResultsStruct();
                currentResult.word(cursor.getString(1));
                currentResult.text(cursor.getString(2));
                currentResult.dict(cursor.getString(3));
                results.add(currentResult);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return results;
    }
}
