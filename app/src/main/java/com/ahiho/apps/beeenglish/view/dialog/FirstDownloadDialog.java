package com.ahiho.apps.beeenglish.view.dialog;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ahiho.apps.beeenglish.R;
import com.ahiho.apps.beeenglish.controller.RealmController;
import com.ahiho.apps.beeenglish.model.realm_object.CommunicationObject;
import com.ahiho.apps.beeenglish.model.realm_object.DictionaryObject;
import com.ahiho.apps.beeenglish.model.realm_object.GrammarObject;
import com.ahiho.apps.beeenglish.model.realm_object.SampleObject;
import com.ahiho.apps.beeenglish.model.realm_object.VocabularyObject;
import com.ahiho.apps.beeenglish.model.realm_object.WordObject;
import com.ahiho.apps.beeenglish.util.Identity;
import com.ahiho.apps.beeenglish.util.MyDownloadManager;
import com.ahiho.apps.beeenglish.util.MyFile;
import com.ahiho.apps.beeenglish.util.UtilSharedPreferences;
import com.ahiho.apps.beeenglish.util.deserialize.CommunicationDeserializer;
import com.ahiho.apps.beeenglish.util.deserialize.GrammarDeserializer;
import com.ahiho.apps.beeenglish.util.deserialize.SampleDeserializer;
import com.ahiho.apps.beeenglish.util.deserialize.VocabularyDeserializer;
import com.ahiho.apps.beeenglish.view.BaseActivity;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;

public class FirstDownloadDialog extends BaseActivity {

    private TextView tvFirstDownloadDescription, tvProgress, tvTotal;
    private LinearLayout llDownloadProgress, llErrorDownload;
    private ProgressBar pbProgress, pbTotal;
    private Button btOk, btCancel;
    private int maxDownload = 5;
    private int countSuccessRead = 0;
    private int currentDownload = 0;
    private long currentDownloadId = -2;
    private long lastId = -1;
    private UtilSharedPreferences mUtilSharedPreferences;
    private MyDownloadManager mDownloadManager;
    private final String TAG = "RESPONSE_FIRST";
    private int ID_LINK_1 = 1;
    private String LINK_1 = "https://ahiho.com/en_vi.zip";
    private String DESCRIPTION_LINK_1 = "từ điển Anh Việt";
    private final String LINK_2 = "https://beeenglish.mobile-backend.ahiho.com/document-template.zip";
    private final String DESCRIPTION_LINK_2 = "tài liệu mẫu";
    private final String LINK_3 = "https://beeenglish.mobile-backend.ahiho.com/grammar.zip";
    private final String DESCRIPTION_LINK_3 = "ngữ pháp";
    private final String LINK_4 = "https://beeenglish.mobile-backend.ahiho.com/vocabulary.zip";
    private final String DESCRIPTION_LINK_4 = "từ vựng";
    private final String LINK_5 = "https://beeenglish.mobile-backend.ahiho.com/communication.zip";
    private final String DESCRIPTION_LINK_5 = "sổ tay giao tiếp";
    private Realm mRealm;
    private int activityResult = Activity.RESULT_CANCELED;

    private BroadcastReceiver broadCastDownload = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            if (downloadId == currentDownloadId) {
                if (mDownloadManager.statusDownloading(downloadId) == DownloadManager.STATUS_SUCCESSFUL) {
                    if (lastId != downloadId) {
                        lastId = downloadId;
                        currentDownload++;
                    }
                    if (currentDownload >= maxDownload) {
                        pbProgress.setProgress(100);
                        tvProgress.setText("100%");
                        btOk.setVisibility(View.VISIBLE);
                        btOk.setText(getString(R.string.bt_complete));
                        btOk.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                finish();
                            }
                        });
                    } else {
                        pbProgress.setProgress(0);
                        tvProgress.setText("0%");

                    }
                    pbTotal.setProgress(currentDownload);
                    tvTotal.setText(currentDownload + "/" + maxDownload);
                    new UnzipFile(downloadId, currentDownload).execute();

                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_first_download);
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(broadCastDownload, filter);
        init();
    }


    @Override
    public void onDestroy() {
        unregisterReceiver(broadCastDownload);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    @Override
    public void finish() {
        Intent returnIntent = new Intent();
        setResult(activityResult, returnIntent);
        super.finish();
    }

    public void init() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = (int) (metrics.widthPixels * 0.9);
        getWindow().setLayout(screenWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
        mUtilSharedPreferences = UtilSharedPreferences.getInstanceSharedPreferences(FirstDownloadDialog.this);
        mDownloadManager = new MyDownloadManager(FirstDownloadDialog.this);
        mRealm = RealmController.with(FirstDownloadDialog.this).getRealm();
//        try {
//            Realm.deleteRealm(mRealm.getConfiguration());
//            //Realm file has been deleted.
//        } catch (Exception ex){
//            //No Realm file to remove.
//        }
//        Intent intent = getIntent();
//        final DictionaryObject dictionaryObject = (DictionaryObject) intent.getSerializableExtra(Identity.EXTRA_DICTIONARY_OBJECT);

        tvFirstDownloadDescription = (TextView) findViewById(R.id.tvFirstDownloadDescription);
        tvProgress = (TextView) findViewById(R.id.tvProgress);
        tvTotal = (TextView) findViewById(R.id.tvTotal);
        llDownloadProgress = (LinearLayout) findViewById(R.id.llDownloadProgress);
        llErrorDownload = (LinearLayout) findViewById(R.id.llErrorDownload);
        pbProgress = (ProgressBar) findViewById(R.id.pbProgress);
        pbTotal = (ProgressBar) findViewById(R.id.pbTotal);
        btOk = (Button) findViewById(R.id.btOk);
        btCancel = (Button) findViewById(R.id.btCancel); btCancel.setVisibility(View.GONE); // Disable cancel button by v2tmobile

        pbProgress.setMax(100);
        pbTotal.setMax(maxDownload);
        pbTotal.setProgress(0);
        tvTotal.setText(currentDownload + "/" + maxDownload);


        btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(grantPermissionReadWriteFile()) {
                    tvFirstDownloadDescription.setVisibility(View.INVISIBLE);
                    llDownloadProgress.setVisibility(View.VISIBLE);
                    btOk.setVisibility(View.GONE);
                    btCancel.setVisibility(View.GONE);
//                new GetDictionary().execute();
                    DictionaryObject dictionaryObject=null;
                    try {
                        dictionaryObject = mRealm.where(DictionaryObject.class).findFirst();
                    }catch (Exception e){

                    }
                    if (dictionaryObject == null) {
                        try {
                            JSONObject jsonObject = new JSONObject("{" +
                                    "\"id\": 1," +
                                    "\"name\": \"Từ điển Anh Việt\"," +
                                    "\"description\": \"Từ điển Anh - Việt trên 100k từ\"," +
                                    "\"picture\": \"https://cdn1.tgdd.vn/Products/Images/1784/70801/TFLAT-English-Vietnamese-Dictionary-icon.png\"," +
                                    "\"content\": \"https://ahiho.com/en_vi.zip\"," +
                                    "\"type\": \"dictionary\"," +
                                    "\"status\": 1," +
                                    "\"created_at\": \"2017-01-17T11:46:55.000Z\"," +
                                    "\"updated_at\": \"2017-01-17T11:46:55.000Z\"" +
                                    "}");
                            mRealm.beginTransaction();
                            mRealm.copyToRealmOrUpdate(new DictionaryObject(jsonObject));
                            mRealm.commitTransaction();
                        } catch (JSONException e) {

                        }

                    }
                    downloadLink(currentDownload);
                }
//                new ReadJson("/storage/sdcard0/bee_english/dictionary/en_vi.zip",1).execute();
            }
        });
        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void downloadLink(int position) {
        String link = "";
        String description = "";
        switch (position) {
            case 1:
                link = LINK_2;
                description = DESCRIPTION_LINK_2;
                break;
            case 2:
                link = LINK_3;
                description = DESCRIPTION_LINK_3;
                break;
            case 3:
                link = LINK_4;
                description = DESCRIPTION_LINK_4;
                break;
            case 4:
                link = LINK_5;
                description = DESCRIPTION_LINK_5;
                break;
            default:
                link = LINK_1;
                description = DESCRIPTION_LINK_1;
                break;

        }

        String fileName = MyFile.getFileName(link);
        File file = new File(MyFile.APP_FOLDER + "/" + MyFile.DOWNLOADS_FOLDER + "/" + fileName);
        final Uri destinationUri = Uri.fromFile(file);
        if (file.exists()) {
            file.delete();
        }
        currentDownloadId = mDownloadManager.downloadData(link, description, destinationUri);
        mUtilSharedPreferences.setIdDictionaryDownload(String.valueOf(currentDownloadId), ID_LINK_1);
        showProgressDownloading(currentDownloadId);

    }

    private void showProgressDownloading(final long downloadId) {
        final Timer myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                DownloadManager.Query q = new DownloadManager.Query();
                q.setFilterById(downloadId);
                Cursor cursor = mDownloadManager.getDownloadManager().query(q);
                cursor.moveToFirst();
                int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                final int dl_progress = (bytes_downloaded * 100 / bytes_total);
                int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                final int status = cursor.getInt(columnIndex);
                cursor.close();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (dl_progress >= 0) {
                            pbProgress.setProgress(dl_progress);
                            tvProgress.setText(dl_progress + "%");

                        }
                        switch (status) {
                            case DownloadManager.STATUS_FAILED:
                                llErrorDownload.setVisibility(View.VISIBLE);
                                btOk.setVisibility(View.VISIBLE);
                                btOk.setText(getString(R.string.bt_ok));
                                btOk.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        finish();
                                    }
                                });
                                myTimer.cancel();
                                break;
                            case DownloadManager.STATUS_PAUSED:
                                break;
                            case DownloadManager.STATUS_PENDING:
                                break;
                            case DownloadManager.STATUS_RUNNING:
                                break;
                            case DownloadManager.STATUS_SUCCESSFUL:
                                myTimer.cancel();
                                break;
                        }
                    }
                });

            }

        }, 0, 10);
    }

//    class GetDictionary extends AsyncTask<Void, Void, ResponseData> {
//
//        @Override
//        protected void onPreExecute() {
//            showDialogLoading();
//        }
//
//        @Override
//        protected ResponseData doInBackground(Void... params) {
//            return MyConnection.getInstanceMyConnection(FirstDownloadDialog.this).getDictionary();
//
//        }
//
//        @Override
//        protected void onPostExecute(ResponseData responseData) {
//            dismissDialogLoading();
//            if (responseData != null) {
//                if (responseData.isResponseState()) {
//                    try {
//                        JSONObject jsonObject = new JSONObject(responseData.getResponseData());
//                        if (jsonObject.getBoolean("success")) {
//                            JSONArray jsonArray = jsonObject.getJSONArray("data");
//                            mRealm.beginTransaction();
//                            for (int i = 0; i < jsonArray.length(); i++) {
//                                DictionaryObject dictionaryObject = new DictionaryObject(jsonArray.getJSONObject(i));
//                                try {
//                                    mRealm.copyToRealm(dictionaryObject);
//                                } catch (Exception e) {
//
//                                }
//                            }
//                            mRealm.commitTransaction();
//
//                        } else {
//                            showSnackBar(R.string.err_json_exception);
//                        }
//                    } catch (JSONException e) {
//                        showSnackBar(R.string.err_json_exception);
//                    }
//                    mRealm.beginTransaction();
//                    DictionaryObject dictionaryObject = null;
//                    try {
//                        dictionaryObject = mRealm.where(DictionaryObject.class).findFirst();
//                    } catch (Exception e) {
//
//                    }
//                    mRealm.commitTransaction();
//                    if (dictionaryObject != null) {
//                        LINK_1 = dictionaryObject.getContent();
//                        ID_LINK_1 = dictionaryObject.getId();
//                        DESCRIPTION_LINK_1 = dictionaryObject.getName();
//
//                        downloadLink(currentDownload);
//                    } else {
//                        Toast.makeText(FirstDownloadDialog.this, R.string.err_connection_fail, Toast.LENGTH_LONG).show();
//                        finish();
//                    }
//                } else {
//                    showSnackBar(responseData.getResponseData());
//                }
//            }
//            super.onPostExecute(responseData);
//        }
//    }

    class UnzipFile extends AsyncTask<Void, Void, Boolean> {
        private String mUri;
        private int currentDownload;

        public UnzipFile(long id, int currentDownload) {
            this.currentDownload = currentDownload;
            mUri = mDownloadManager.getStringUriFileDownload(id);
            showDialogLoading(R.string.extracting);

        }


        @Override
        protected Boolean doInBackground(Void... params) {
            String des = MyFile.APP_FOLDER + "/" + MyFile.DOWNLOADS_FOLDER + "/";
            return MyFile.unzipWithLib(mUri, des, "");


        }

        @Override
        protected void onPostExecute(Boolean result) {
            dismissDialogLoading();
            if (result) {
                File file = new File(mUri);
                file.delete();
                new ReadJson(mUri, currentDownload).execute();
            } else {
                Toast.makeText(FirstDownloadDialog.this, R.string.err_read_file, Toast.LENGTH_SHORT).show();

            }

            super.onPostExecute(result);
        }
    }


    class ReadJson extends AsyncTask<Void, Long, Boolean> {
        private String mUri;
        private int currentDownload;

        public ReadJson(String uri, int id) {
            mUri = uri.replace(".zip", ".json");
            currentDownload = id;
        }

        @Override
        protected void onPreExecute() {
            showDialogLoading(R.string.updating);
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = true;
//            String s = MyFile.readFileText(mUri);

            switch (currentDownload) {
                case 1:
                    result = readDictionary(mUri);
                    break;
                case 2:
                    result = readSample(mUri);
                    break;
                case 3:
                    result = readGrammar(mUri);
                    break;
                case 4:
                    result = readVocabulary(mUri);
                    break;
                case 5:
                    result = readCommunication(mUri);
                    break;
            }

            return result;
        }


        @Override
        protected void onPostExecute(Boolean result) {
            dismissDialogLoading();
            if (result) {
                File file = new File(mUri);
                file.delete();
                countSuccessRead++;

            } else {
                mUtilSharedPreferences.setListDownloadFail(currentDownload - 1);
            }
            if (currentDownload < maxDownload)
                downloadLink(FirstDownloadDialog.this.currentDownload);
            else {
                activityResult = Activity.RESULT_OK;
                mUtilSharedPreferences.setUpdateFirst(true);
            }
            super.onPostExecute(result);
        }
    }

    private boolean readCommunication(String mUri) {
        boolean result = true;
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(CommunicationObject.class, new CommunicationDeserializer())
                .create();
        JsonReader reader = null;
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            reader = new JsonReader(new FileReader(mUri));
            CommunicationObject data = gson.fromJson(reader, Identity.COMMUNICATION_TYPE);// contains the whole reviews list
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(data);
            realm.commitTransaction();


        } catch (Exception e) {
            Log.e(TAG, e.getMessage() + "");

            return false;
        } finally {
            if (realm != null) {
                realm.close();
            }
        }

        return result;
    }
    private boolean readVocabulary(String mUri) {
        boolean result = true;
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(VocabularyObject.class, new VocabularyDeserializer())
                .create();
        JsonReader reader = null;
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            reader = new JsonReader(new FileReader(mUri));
            List<VocabularyObject> data = gson.fromJson(reader, Identity.VOCABULARY_TYPE);// contains the whole reviews list
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(data);
            realm.commitTransaction();


        } catch (Exception e) {
            Log.e(TAG, e.getMessage() + "");

            return false;
        } finally {
            if (realm != null) {
                realm.close();
            }
        }

        return result;
    }

    private boolean readGrammar(String mUri) {
        boolean result = true;
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(GrammarObject.class, new GrammarDeserializer())
                .create();
        JsonReader reader = null;
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            reader = new JsonReader(new FileReader(mUri));
            List<GrammarObject> data = gson.fromJson(reader, Identity.GRAMMAR_TYPE);// contains the whole reviews list
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(data);
            realm.commitTransaction();


        } catch (Exception e) {
            Log.e(TAG, e.getMessage() + "");

            return false;
        } finally {
            if (realm != null) {
                realm.close();
            }
        }

        return result;
    }


    private boolean readSample(String mUri) {
        boolean result = true;
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(SampleObject.class, new SampleDeserializer())
                .create();
        JsonReader reader = null;
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            reader = new JsonReader(new FileReader(mUri));
            List<SampleObject> data = gson.fromJson(reader, Identity.SAMPLE_TYPE);// contains the whole reviews list
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(data);
            realm.commitTransaction();


        } catch (Exception e) {
            Log.e(TAG, e.getMessage() + "");

            return false;
        } finally {
            if (realm != null) {
                realm.close();
            }
        }

        return result;
    }

//    private boolean readDictionary(String mUri) {
//        boolean result = true;
//        Gson gson = new GsonBuilder()
//                .setExclusionStrategies(new ExclusionStrategy() {
//                    @Override
//                    public boolean shouldSkipField(FieldAttributes f) {
//                        return f.getDeclaringClass().equals(RealmObject.class);
//                    }
//
//                    @Override
//                    public boolean shouldSkipClass(Class<?> clazz) {
//                        return false;
//                    }
//                })
//                .registerTypeAdapter(new TypeToken<RealmList<WordObject>>() {}.getType(), new TypeAdapter<RealmList<WordObject>>() {
//
//                    @Override
//                    public void write(JsonWriter out, RealmList<WordObject> value) throws IOException {
//                        // Ignore
//                    }
//
//                    @Override
//                    public RealmList<WordObject> read(JsonReader in) throws IOException {
//                        RealmList<WordObject> list = new RealmList<WordObject>();
//                        in.beginArray();
//                        while (in.hasNext()) {
//                            list.add(new WordObject(in.nextString(),in.nextString()));
//                        }
//                        in.endArray();
//                        return list;
//                    }
//                })
//                .create();
//        JsonReader reader = null;
//        Realm realm = null;
//        try {
//            realm = Realm.getDefaultInstance();
//            reader = new JsonReader(new FileReader(mUri));
//            List<WordObject> data = gson.fromJson(reader, Identity.WORD_TYPE);// contains the whole reviews list
//            DictionaryObject dictionaryObject  = realm.where(DictionaryObject.class).findFirst();
//            dictionaryObject.getWordObjects().addAll(data);
//            realm.beginTransaction();
//            realm.copyToRealmOrUpdate(dictionaryObject);
//            realm.commitTransaction();
//
//
//        } catch (Exception e) {
//            Log.e(TAG,"2"+ e.getMessage() + "");
//
//            return false;
//        } finally {
//            if (realm != null) {
//                realm.close();
//            }
//        }
//        return result;
//
//    }

    private boolean readDictionary(String mUri) {
        boolean result = true;
//            String s = MyFile.readFileText(mUri);
        Gson gson = new GsonBuilder()
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getDeclaringClass().equals(RealmObject.class);
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .registerTypeAdapter(new TypeToken<RealmList<WordObject>>() {
                }.getType(), new TypeAdapter<RealmList<WordObject>>() {

                    @Override
                    public void write(JsonWriter out, RealmList<WordObject> value) throws IOException {
                        // Ignore
                    }

                    @Override
                    public RealmList<WordObject> read(JsonReader in) throws IOException {
                        RealmList<WordObject> list = new RealmList<WordObject>();
                        in.beginArray();
                        while (in.hasNext()) {
                            list.add(new WordObject(in.nextString(), in.nextString()));
                        }
                        in.endArray();
                        return list;
                    }
                })
                .create();
        JsonReader reader = null;
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            reader = new JsonReader(new FileReader(mUri));
            List<WordObject> data = gson.fromJson(reader, Identity.WORD_TYPE);// contains the whole reviews list

            DictionaryObject dictionaryObject = realm.where(DictionaryObject.class).findFirst();
            realm.beginTransaction();
            dictionaryObject.getWordObjects().addAll(data);
            realm.copyToRealmOrUpdate(dictionaryObject);
            realm.commitTransaction();


        } catch (Exception e) {
            Log.e(TAG, e.getMessage() + "");

            return false;
        } finally {
            if (realm != null) {
                realm.close();
            }
        }

        return result;

    }

}

