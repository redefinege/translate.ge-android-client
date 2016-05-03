package ge.redefine.translatege;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.webkit.WebView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.math.BigDecimal;
import java.util.HashMap;

import extras.DatabaseDownloader;
import extras.PreferencesManager;

public class SettingsActivity extends com.fnp.materialpreferences.PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPreferenceFragment(new MyPreferenceFragment());
    }

    public static class MyPreferenceFragment extends com.fnp.materialpreferences.PreferenceFragment {

        private WebView webView;

        public MyPreferenceFragment() {
        }

        @Override
        public int addPreferencesFromResource() {
            return R.xml.preferences;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Preference prefDownload = findPreference(PreferencesManager.PREF_DOWNLOAD);
            DatabaseDownloader downloader = DatabaseDownloader.getInstance();
            if (downloader.databaseExists()) {
                int version = PreferencesManager.getInstance().getDatabaseVersion();
                String summary = getResources().getString(R.string.prefDownloadedSummary);
                prefDownload.setSummary(String.format(summary, version));
            } else {
                prefDownload.setSummary(R.string.prefNotDownloadedSummary);
            }
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            switch (preference.getKey()) {
                case PreferencesManager.PREF_DOWNLOAD:
                    final DatabaseDownloader downloader = DatabaseDownloader.getInstance();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final Context context = getActivity();
                            final HashMap<String, String> info = downloader.getDatabaseInfo();
                            final boolean update = downloader.isUpdateAvailable(info);

                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    if (update) {
                                        String version = info.get("version");
                                        String downloadSize = calculateSize(info.get("size")) + " MB";
                                        String unpackedSize = calculateSize(info.get("size_unpacked")) + " MB";
                                        String content = getResources().getString(R.string.prefUpdateContent);
                                        content = String.format(content, version, downloadSize, unpackedSize);

                                        new MaterialDialog.Builder(context)
                                                .title(getResources().getString(R.string.dialogTitleDownloadPrompt))
                                                .content(content)
                                                .negativeColor(ContextCompat.getColor(context, R.color.colorError))
                                                .negativeText(getResources().getString(R.string.cancel))
                                                .positiveText(getResources().getString(R.string.download))
                                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                    @Override
                                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                        dialog.dismiss();
                                                        downloader.downloadAndUnzip(context);
                                                    }
                                                })
                                                .show();
                                    } else {
                                        new MaterialDialog.Builder(context)
                                                .content(getResources().getString(R.string.prefNoUpdatesContent))
                                                .positiveText("OK")
                                                .build()
                                                .show();
                                    }
                                }
                            });
                        }
                    }).start();

                    break;

                case PreferencesManager.PREF_LICENSES:
                    displayLicensesAlertDialog();
                    break;

            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        private String calculateSize(String size) {
            float sizeFloat = Float.parseFloat(size) / 1024 / 1024;
            return new BigDecimal(String.valueOf(sizeFloat))
                    .setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toString();
        }

        private void displayLicensesAlertDialog() {
            webView = new WebView(getActivity());
            webView.loadUrl("file:///android_asset/open_source_licenses.html");
            new AlertDialog.Builder(getActivity(), R.style.AppCompatDialogTheme)
                    .setTitle(getString(R.string.prefLicenses))
                    .setView(webView)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }

    }
}