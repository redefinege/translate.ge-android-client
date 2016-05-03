package extras;


import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ge.redefine.translatege.App;
import ge.redefine.translatege.MainActivity;
import ge.redefine.translatege.R;

public class DatabaseDownloader {

    private static final String DATABASE_API = "https://api.brunjadze.xyz/v1/app/translatege/get_latest_database";
    private static final String ZIP_FILE = "dictionary.zip";
    private static final int TASK_DOWNLOAD = 1;
    private static final int TASK_UNZIP = 2;

    private static Context sContext = App.getAppContext();
    private static DatabaseDownloader sInstance;
    private static boolean sCancelled = false;
    private PreferencesManager preferencesManager;

    private MaterialDialog mMaterialDialog;
    private File zipFile;
    private File databaseFile;
    private File databaseDir;
    private int newVer;

    private DatabaseDownloader() {
        databaseDir = new File(sContext.getDatabasePath("dummy").getParent());
        zipFile = new File(sContext.getCacheDir(), ZIP_FILE);
        databaseFile = new File(databaseDir, MainActivity.DATABASE_NAME);
        preferencesManager = PreferencesManager.getInstance();
    }

    public static DatabaseDownloader getInstance() {
        if (sInstance == null) sInstance = new DatabaseDownloader();
        return sInstance;
    }

    public boolean databaseExists() {
        return (databaseFile.exists() && !databaseFile.isDirectory());
    }

    public boolean isUpdateAvailable(HashMap<String, String> info) {
        if (info == null)
            info = getDatabaseInfo();

        if (!info.isEmpty()) {
            int localVer = preferencesManager.getDatabaseVersion();
            int remoteVer = Integer.parseInt(info.get("version"));

            if (remoteVer > localVer) {
                return true;
            }
        }
        return false;
    }

    public HashMap<String, String> getDatabaseInfo() {
        HashMap<String, String> info = new HashMap<>();
        HttpURLConnection connection = null;
        InputStream stream = null;
        BufferedReader reader = null;

        try {
            URL urlAPI = new URL(DATABASE_API);
            connection = (HttpURLConnection) urlAPI.openConnection();
            stream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stream));
            String result = "";
            String line;
            while ((line = reader.readLine()) != null) {
                result = result + line;
            }
            JSONObject databaseInfo = new JSONObject(result);
            info.put("version", databaseInfo.getString("version"));
            info.put("size", databaseInfo.getString("size"));
            info.put("size_unpacked", databaseInfo.getString("size_unpacked"));
            info.put("url", databaseInfo.getString("url"));
        } catch (Exception ignored) {
            ignored.printStackTrace();
        } finally {
            try {
                if (connection != null)
                    connection.disconnect();
                if (stream != null)
                    stream.close();
                if (reader != null)
                    reader.close();
            } catch (IOException ignored) {
            }
        }

        return info;
    }

    public void downloadAndUnzip(final Context context) {
        sCancelled = false;
        mMaterialDialog = new MaterialDialog.Builder(context)
                .title(context.getResources().getString(R.string.dialogTitleWait))
                .negativeColor(ContextCompat.getColor(context, R.color.colorError))
                .negativeText(context.getResources().getString(R.string.cancel))
                .cancelable(false)
                .progress(false, 100, false)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        sCancelled = true;
                    }
                })
                .build();

        mMaterialDialog.show();

        new BackgroundTask(TASK_DOWNLOAD, new SimpleResponseCallback() {
            @Override
            public void onSuccess(String... params) {
                new BackgroundTask(TASK_UNZIP, new SimpleResponseCallback() {
                    @Override
                    public void onSuccess(String... params) {
                        mMaterialDialog.dismiss();
                        preferencesManager.setDatabaseVersion(newVer);
                    }

                    @Override
                    public void onError(String... params) {
                        mMaterialDialog.dismiss();
                    }
                }).execute();
            }

            @Override
            public void onError(String... params) {
                mMaterialDialog.dismiss();
            }
        }).execute();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public class BackgroundTask extends AsyncTask<String, Integer, String> {

        private int mTask;
        private SimpleResponseCallback mResponseCallback;
        private PowerManager.WakeLock mWakeLock;

        public BackgroundTask(int task, SimpleResponseCallback responseCallback) {
            mTask = task;
            mResponseCallback = responseCallback;
        }

        @Override
        protected String doInBackground(String... params) {
            switch (mTask) {
                case 1:
                    InputStream input = null;
                    OutputStream output = null;
                    HttpURLConnection connection = null;
                    try {
                        HashMap<String, String> info = getDatabaseInfo();
                        newVer = Integer.parseInt(info.get("version"));
                        URL url = new URL(info.get("url"));
                        connection = (HttpURLConnection) url.openConnection();
                        connection.connect();

                        // expect HTTP 200 OK, so we don't mistakenly save error report
                        // instead of the file
                        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                            return connection.getResponseMessage();
                        }

                        // this will be useful to display download percentage
                        // might be -1: server did not report the length
                        int fileLength = connection.getContentLength();

                        // download the file
                        input = connection.getInputStream();
                        output = new FileOutputStream(zipFile);

                        byte data[] = new byte[4096];
                        long total = 0;
                        int count;
                        while ((count = input.read(data)) != -1) {
                            // allow canceling with back button
                            if (sCancelled) {
                                zipFile.delete();
                                return "Cancelled";
                            }
                            total += count;
                            // publishing the progress....
                            if (fileLength > 0) // only if total length is known
                                publishProgress((int) total, (int) (total * 100 / fileLength));
                            output.write(data, 0, count);
                        }
                    } catch (Exception e) {
                        return e.toString();
                    } finally {
                        try {
                            if (output != null)
                                output.close();
                            if (input != null)
                                input.close();
                        } catch (IOException ignored) {
                        }

                        if (connection != null)
                            connection.disconnect();
                    }
                    return null;
                case 2:
                    if (!databaseDir.exists()) {
                        if (!databaseDir.mkdirs()) return "Can't create directory";
                    } else if (!databaseDir.isDirectory()) {
                        return "Extract directory is file";
                    }

                    FileInputStream fInput = null;
                    FileOutputStream fOutput = null;
                    ZipInputStream zInput = null;
                    ZipEntry ze;
                    try {
                        fInput = new FileInputStream(zipFile);
                        zInput = new ZipInputStream(fInput);
                        while ((ze = zInput.getNextEntry()) != null) {
                            if (!ze.isDirectory()) {
                                long fileLength = ze.getSize();
                                fOutput = new FileOutputStream(databaseFile);
                                byte data[] = new byte[4096];
                                long total = 0;
                                int count;
                                while ((count = zInput.read(data)) != -1) {
                                    // allow canceling with back button
                                    if (sCancelled) {
                                        databaseFile.delete();
                                        return "Cancelled";
                                    }
                                    total += count;
                                    // publishing the progress....
                                    if (fileLength > 0) // only if total length is known
                                        publishProgress((int) total, (int) (total * 100 / fileLength));
                                    fOutput.write(data, 0, count);
                                }
                            }
                        }
                    } catch (Exception e) {
                        return e.toString();
                    } finally {
                        try {
                            if (zInput != null)
                                zInput.close();
                            if (fInput != null)
                                fInput.close();
                            if (fOutput != null)
                                fOutput.close();
                            zipFile.delete();
                        } catch (IOException ignored) {
                        }
                    }
                    return null;
                default:
                    return "how the fuck did we get here";
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) sContext.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            mMaterialDialog.setProgress(50 * (mTask - 1) + progress[1] / 2);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            if (result == null)
                mResponseCallback.onSuccess();
            else
                mResponseCallback.onError(result);
        }

    }

}
