package extras;


import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class ResultsProvider {

    public static final int REQ_TYPE_OFFLINE = 1;
    public static final int REQ_TYPE_ONLINE = 2;
    public static final int REQ_TYPE_AUTO = 3;

    private DatabaseHelper mDatabaseHelper;
    private DatabaseDownloader mDatabaseDownloader;
    private TranslateAPI mTranslateAPI;

    public ResultsProvider() {
        mDatabaseHelper = new DatabaseHelper();
        mDatabaseDownloader = DatabaseDownloader.getInstance();
        mTranslateAPI = TranslateAPI.getInstance();
    }

    public synchronized ArrayList<ResultsStruct> getResults(final String word, int requestType) {
        final ArrayList<ResultsStruct> response = new ArrayList<>();

        if (requestType == REQ_TYPE_AUTO) {
            if (mDatabaseDownloader.databaseExists())
                requestType = REQ_TYPE_OFFLINE;
            else
                requestType = REQ_TYPE_ONLINE;
        }

        switch (requestType) {
            case REQ_TYPE_OFFLINE:
                response.addAll(mDatabaseHelper.getResults(word));
                break;
            case REQ_TYPE_ONLINE:
                final CountDownLatch latch = new CountDownLatch(1);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mTranslateAPI.getResults(word, new TranslateAPI.ResponseListener() {
                            @Override
                            public void onSuccessResponse(ArrayList<ResultsStruct> data) {
                                response.addAll(data);
                                latch.countDown();
                            }

                            @Override
                            public void onErrorResponse() {
                                latch.countDown();
                            }
                        });
                    }
                }).start();

                try {
                    latch.await();
                } catch (InterruptedException ignored) {
                }
                break;
        }

        return response;
    }
}
