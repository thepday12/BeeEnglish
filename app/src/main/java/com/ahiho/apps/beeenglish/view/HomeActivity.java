package com.ahiho.apps.beeenglish.view;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ahiho.apps.beeenglish.R;
import com.ahiho.apps.beeenglish.adapter.RecyclerCategoryAdapter;
import com.ahiho.apps.beeenglish.controller.RealmController;
import com.ahiho.apps.beeenglish.model.CategoryObject;
import com.ahiho.apps.beeenglish.model.FunctionObject;
import com.ahiho.apps.beeenglish.model.ResponseData;
import com.ahiho.apps.beeenglish.my_interface.OnCallbackSnackBar;
import com.ahiho.apps.beeenglish.service.ServiceRequestToken;
import com.ahiho.apps.beeenglish.util.Identity;
import com.ahiho.apps.beeenglish.util.MyConnection;
import com.ahiho.apps.beeenglish.util.UtilSharedPreferences;
import com.ahiho.apps.beeenglish.util.UtilString;
import com.ahiho.apps.beeenglish.view.communication.CommunicationActivity;
import com.ahiho.apps.beeenglish.view.dialog.FirstDownloadDialog;
import com.ahiho.apps.beeenglish.view.sample.SampleActivity;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;

public class HomeActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private RelativeLayout llHeader;
    private CircleImageView ivAvatar;
    private TextView tvDisplayName, tvTime;
    private TextView tvEmail;
    private RecyclerView rvUsually, rvRecent;
    private UtilSharedPreferences mUtilSharedPreferences;
    private Dialog mActivityDialog;
    private final int REQ_UPDATE = 100;
    private Button btDictionary, btBook, btTest, btSample, btSkill, btGrammar, btCommunicate, btVocabulary;
    private Boolean isValidActive = true;
    private int SHARE_REQUEST_CODE = 21;
    private String mUserName, mUserId;
    private Intent intentService;
    private Realm mRealm;
    private List<FunctionObject> functionObjects;
    private GoogleApiClient mGoogleApiClient;

    private BroadcastReceiver broadCastExpired = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action =intent.getAction();
            if(action.equals(Identity.EXPIRED_BROADCAST)) {
                showDialogTrial();
            }else if(action.equals(Identity.UPDATE_FIRST_BROADCAST)){
                Intent intentFirstDialog = new Intent(HomeActivity.this, FirstDownloadDialog.class);
                startActivityForResult(intentFirstDialog, REQ_UPDATE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(HomeActivity.this);
        configSignInGoogle();
        setContentView(R.layout.activity_home);
        IntentFilter filter=new IntentFilter(Identity.EXPIRED_BROADCAST);
        filter.addAction(Identity.UPDATE_FIRST_BROADCAST);
        registerReceiver(broadCastExpired,filter );
        init();
    }


    @Override
    protected void onStop() {
        stopService(intentService);
        super.onStop();
    }

    @Override
    protected void onDestroy() {

        try {
            stopService(intentService);
            unregisterReceiver(broadCastExpired);

        } catch (Exception e) {

        }
        super.onDestroy();
    }

    private void init() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mRealm = RealmController.with(this).getRealm();
        intentService = new Intent(HomeActivity.this, ServiceRequestToken.class);
        if (serviceNotRun()) {
            startService(intentService);
        }
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();
        mUtilSharedPreferences = UtilSharedPreferences.getInstanceSharedPreferences(HomeActivity.this);
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        llHeader = (RelativeLayout) mNavigationView.getHeaderView(0).findViewById(R.id.llHeader);
        ivAvatar = (CircleImageView) mNavigationView.getHeaderView(0).findViewById(R.id.ivAvatar);
        tvDisplayName = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.tvDisplayName);
        tvEmail = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.tvEmail);
        updateMenuLeft();

//        btDictionary,btBook,btTest,btSample,btSkill,btGrammar,btCommunicate,btVocabulary
//        btDictionary = (Button) findViewById(R.id.btDictionary);
//        btBook = (Button) findViewById(R.id.btBook);
//        btTest = (Button) findViewById(R.id.btTest);
//        btSample = (Button) findViewById(R.id.btSample);
//        btSkill = (Button) findViewById(R.id.btSkill);
//        btGrammar = (Button) findViewById(R.id.btGrammar);
//        btCommunicate = (Button) findViewById(R.id.btCommunicate);
//        btVocabulary = (Button) findViewById(R.id.btVocabulary);
//
//        initOnClick();
        rvRecent = (RecyclerView) findViewById(R.id.rvRecent);
        rvUsually = (RecyclerView) findViewById(R.id.rvUsually);

        rvRecent.setLayoutManager(new GridLayoutManager(HomeActivity.this, 2));
        rvUsually.setLayoutManager(new LinearLayoutManager(HomeActivity.this, LinearLayoutManager.HORIZONTAL, false));

        initData();

        if (!mUtilSharedPreferences.isUpdateFirst()) {
            Intent intent = new Intent(HomeActivity.this, FirstDownloadDialog.class);
            startActivityForResult(intent, REQ_UPDATE);
        } else {
            isValidUser();
        }
        grantPermissionReadWriteFile();
    }

    private void initData() {
        functionObjects = new ArrayList<>();
        functionObjects.add(new FunctionObject(Identity.FUN_ID_DICTIONARY, R.drawable.ic_bee_english, getString(R.string.fun_dictionary)));
        functionObjects.add(new FunctionObject(Identity.FUN_ID_BOOK, R.drawable.ic_dashboard_books, getString(R.string.fun_book)));
        functionObjects.add(new FunctionObject(Identity.FUN_ID_SAMPLE, R.drawable.ic_dashboard_sample, getString(R.string.fun_sample)));
        functionObjects.add(new FunctionObject(Identity.FUN_ID_GRAMMAR, R.drawable.ic_dashboard_grammar, getString(R.string.fun_grammar)));
        functionObjects.add(new FunctionObject(Identity.FUN_ID_COMMUNICATE, R.drawable.ic_dashboard_communicate, getString(R.string.fun_communicate)));
        functionObjects.add(new FunctionObject(Identity.FUN_ID_VOCABULARY, R.drawable.ic_dashboard_vocabulary, getString(R.string.fun_vocabulary)));
        rvRecent.setAdapter(new RecyclerCategoryAdapter(functionObjects, 2, true));

        updateUsually();

    }

    private void updateUsually() {
        List<FunctionObject> objectUsually = new ArrayList<>();

        List<CategoryObject> categoryObjects = new ArrayList<>();
        try {
            categoryObjects = mRealm.where(CategoryObject.class).findAllSorted("count", false);
        }catch (Exception e){

        }
        if (categoryObjects != null && categoryObjects.size() > 0) {
            for (int i = 0; i < 3; i++) {
                for (FunctionObject object : functionObjects) {
                    try {
                        if (categoryObjects.get(i).getId() == object.getId()) {
                            objectUsually.add(object);
                            break;
                        }
                    } catch (Exception e) {

                    }
                }
            }
            if (objectUsually.size() > 0)
                rvUsually.setAdapter(new RecyclerCategoryAdapter(objectUsually, 3, false));
        }
    }

    private void updateMenuLeft() {
        loadDataUser();
        if (mUtilSharedPreferences != null) {
            isValidActive = mUtilSharedPreferences.isActiveValid();
            if (isValidActive)
                mNavigationView.getMenu().findItem(R.id.nav_active).setTitle(R.string.nav_active_success);
        }
    }

    private void initOnClick() {
        btDictionary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidUser()) {
                    Intent intent = new Intent(HomeActivity.this, DictionaryActivity.class);
                    startActivity(intent);
                } else {
                    showInvalidError();
                }
            }
        });
        btBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidUser()) {
                    Intent intent = new Intent(HomeActivity.this, BooksActivity.class);
                    startActivity(intent);
                } else {
                    showInvalidError();
                }
            }
        });
        btTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidUser()) {
//                    Intent intent = new Intent(HomeActivity.this, DictionaryActivity.class);
//                    startActivity(intent);
                } else {
                    showInvalidError();
                }
            }
        });
        btSample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidUser()) {
                    Intent intent = new Intent(HomeActivity.this, SampleActivity.class);
                    startActivity(intent);
                } else {
                    showInvalidError();
                }
            }
        });
        btSkill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidUser()) {
//                    Intent intent = new Intent(HomeActivity.this, DictionaryActivity.class);
//                    startActivity(intent);
                } else {
                    showInvalidError();
                }
            }
        });
        btGrammar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidUser()) {
                    Intent intent = new Intent(HomeActivity.this, GrammarActivity.class);
                    startActivity(intent);
                } else {
                    showInvalidError();
                }
            }
        });
        btCommunicate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidUser()) {
                    Intent intent = new Intent(HomeActivity.this, CommunicationActivity.class);
                    startActivity(intent);
                } else {
                    showInvalidError();
                }
            }
        });
        btVocabulary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidUser()) {
                    Intent intent = new Intent(HomeActivity.this, VocabularyActivity.class);
                    startActivity(intent);
                } else {
                    showInvalidError();
                }
            }
        });

    }

    private void showInvalidError() {
    }

    private boolean isValidUser() {
        long time = mUtilSharedPreferences.getTrialTimeExpired() - System.currentTimeMillis();
        if (time > 604800000) {
            return true;
        } else {
            showDialogTrial();
            return false;
        }
    }

    private void loadDataUser() {
        try {
            JSONObject jsonObject = new JSONObject(mUtilSharedPreferences.getUserData());
            mUserName = jsonObject.getString("username");
            mUserId = jsonObject.getString("id");
            String name = "";
            String firstName = jsonObject.getString("first_name");
            String lastName = jsonObject.getString("last_name");
            if (firstName != null && !firstName.isEmpty() && !firstName.equals("null"))
                name += firstName;
            if (lastName != null && !lastName.isEmpty() && !lastName.equals("null"))
                name += lastName;
            if (name.isEmpty())
                name = jsonObject.getString("email");
            tvDisplayName.setText(name);
            String uri = jsonObject.getString("avatar");
            if (uri != null && !uri.isEmpty() && !uri.equals("null")) {
                Picasso.with(HomeActivity.this).load(uri).error(R.drawable.default_avatar).into(ivAvatar);
            } else {
                Picasso.with(HomeActivity.this).load(R.drawable.default_avatar).into(ivAvatar);
            }
        } catch (JSONException e) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_UPDATE) {
            showDialogTrial();
        } else if (requestCode == SHARE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                showSnackBar(R.string.success_share);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onResume() {
        if (mUtilSharedPreferences != null) {
            updateMenuLeft();
            updateUsually();
        }
        super.onResume();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_active:
                if ((mUtilSharedPreferences.getTrialTimeExpired() - System.currentTimeMillis()) > 604800000) {
                    showDialogTrial();
                } else {
                    startActivity(new Intent(HomeActivity.this, ScanQRActivity.class));//ActiveDialog
                }
                break;
            case R.id.nav_info:
                startActivity(new Intent(HomeActivity.this, InfoActivity.class));
                break;
            case R.id.nav_password_change:
                showDialogChangePassword();
                break;
            case R.id.nav_about:
                startActivity(new Intent(HomeActivity.this, AboutActivity.class));
                break;
            case R.id.nav_share:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT,
                        getString(R.string.share_text_app) + " https://play.google.com/store/apps/details?id=" + getPackageName());
                startActivityForResult(shareIntent, SHARE_REQUEST_CODE);
                break;
            case R.id.nav_sign_out:
                try {
                    LoginManager.getInstance().logOut();
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                }catch (Exception e){

                }
                mUtilSharedPreferences.signOut();
                startActivity(new Intent(HomeActivity.this, MainActivity.class));
                finish();
                break;
        }
        mDrawerLayout.closeDrawers();
        return true;
    }

    private void configSignInGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(Plus.API)
                .build();
    }

    private void showDialogTrial() {
        boolean isShowInfo = (mUtilSharedPreferences.getTrialTimeExpired() - System.currentTimeMillis() > 604800000);
        if (isValidActive && !isShowInfo) {
            return;
        }
        if (mActivityDialog != null && mActivityDialog.isShowing())
            return;

        mActivityDialog = new Dialog(HomeActivity.this);
        mActivityDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mActivityDialog.setContentView(R.layout.dialog_trial);
        mActivityDialog.setCanceledOnTouchOutside(true);
        tvTime = (TextView) mActivityDialog.findViewById(R.id.tvTime);
        Button btOk = (Button) mActivityDialog.findViewById(R.id.btOk);
        Button btCancel = (Button) mActivityDialog.findViewById(R.id.btCancel);
        final TextView tvTitle = (TextView) mActivityDialog.findViewById(R.id.tvTitle);
        TextView tvContent = (TextView) mActivityDialog.findViewById(R.id.tvContent);
        if (isShowInfo) {
            tvTitle.setText(R.string.trial_title_active);
            tvContent.setText(R.string.trial_content_active);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                tvTitle.setTextColor(getColor(R.color.colorSuccess));
                tvTime.setTextColor(getColor(R.color.colorSuccess));
            } else {
                tvTime.setTextColor(Color.parseColor("#4caf50"));
                tvTitle.setTextColor(Color.parseColor("#4caf50"));
            }
        }


        btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, ScanQRActivity.class));
                mActivityDialog.dismiss();
            }
        });

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivityDialog.dismiss();
            }
        });
        long trialTime = mUtilSharedPreferences.getTrialTimeExpired();
        long time = trialTime - System.currentTimeMillis();
        if (time > 0) {
            final Timer myTimer = new Timer();
            myTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            long time = mUtilSharedPreferences.getTrialTimeExpired() - System.currentTimeMillis();
                            if (time > 0) {
                                tvTime.setText(UtilString.convertTime(time));
                            } else {
//                                tvTitle.setText(getString(R.string.trial_title));
                                setTextExpired(tvTime);
                                myTimer.cancel();
                            }
                        }
                    });

                }

            }, 0, 1000);
            mActivityDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (myTimer != null)
                        myTimer.cancel();
                }
            });
        } else {
            setTextExpired(tvTime);
        }

        mActivityDialog.show();
    }

    private void showDialogChangePassword() {
        if (mActivityDialog != null && mActivityDialog.isShowing())
            mActivityDialog.dismiss();
        mActivityDialog = new Dialog(HomeActivity.this);
        mActivityDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mActivityDialog.setContentView(R.layout.dialog_change_pass);
        mActivityDialog.setCanceledOnTouchOutside(true);
        final EditText etOldPassword = (EditText) mActivityDialog.findViewById(R.id.etOldPassword);
        final EditText etNewPassword = (EditText) mActivityDialog.findViewById(R.id.etNewPassword);
        final EditText etReNewPassword = (EditText) mActivityDialog.findViewById(R.id.etReNewPassword);
        Button btOk = (Button) mActivityDialog.findViewById(R.id.btOk);

        btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String oldPassword = etOldPassword.getText().toString();
                if (MyConnection.isOnline(HomeActivity.this)) {
                    String newPassword = etNewPassword.getText().toString();
                    String renewPassword = etReNewPassword.getText().toString();
//                if (oldPassword.isEmpty()) {
//                    etOldPassword.setError(getString(R.string.err_edit_text_empty));
//                } else {
                    if (newPassword.isEmpty()) {
                        etNewPassword.setError(getString(R.string.err_edit_text_empty));
                    } else {
//                        if (newPassword.equals(oldPassword)) {
//                            etNewPassword.setError(getString(R.string.err_password_exist));
//                        } else {
                        if (newPassword.length() > 4) {
                            if (newPassword.equals(renewPassword)) {
                                mActivityDialog.dismiss();
                                new UpdateNewPassword(newPassword).execute();
                            } else {
                                etReNewPassword.setError(getString(R.string.err_password_not_match));
                            }
                        } else {
                            showSnackBar(R.string.err_password_short);
                        }
//                        }
                    }
//                }
                } else {
                    mSnackbar.showTextAction(R.string.err_connection_fail, R.string.bt_try_connection, new OnCallbackSnackBar() {
                        @Override
                        public void onAction() {
                            ActivityCompat.requestPermissions(HomeActivity.this,
                                    new String[]{Manifest.permission.CHANGE_WIFI_STATE}, Identity.REQUEST_PERMISSION_CHANGE_WIFI_STATE);
                        }
                    });
                }
            }
        });

        mActivityDialog.show();
    }


    private void setTextExpired(TextView tvTime) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tvTime.setTextColor(getColor(R.color.colorError));
        } else {
            tvTime.setTextColor(Color.parseColor("#cc0000"));
        }
        tvTime.setText(getString(R.string.err_trial_time));

    }

    class UpdateNewPassword extends AsyncTask<Void, Void, ResponseData> {

        private String mPassword;

        public UpdateNewPassword(String password) {
            mPassword = password;
        }

        @Override
        protected void onPreExecute() {
            showDialogLoading();
        }

        @Override
        protected ResponseData doInBackground(Void... params) {
            return MyConnection.getInstanceMyConnection(HomeActivity.this).updatePassWord(mUserId, mPassword);
        }

        @Override
        protected void onPostExecute(ResponseData responseData) {
            dismissDialogLoading();
            try {
                if (responseData.isResponseState()) {
                    JSONObject jsonObject = new JSONObject(responseData.getResponseData());
                    if (jsonObject.getBoolean("success")) {
                        showSnackBar(R.string.success_change_pass);
                    } else {
                        showSnackBar(responseData.getResponseData());
                    }
                } else {
                    showSnackBar(responseData.getResponseData());
                }
            } catch (JSONException e) {
                showSnackBar(R.string.err_json_exception);
            }
            super.onPostExecute(responseData);
        }

    }

    public boolean serviceNotRun() {

        ActivityManager actvityManager = (ActivityManager)
                getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> procInfos = actvityManager
                .getRunningAppProcesses();
        for (ActivityManager.RunningServiceInfo service : actvityManager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (service.service.getClassName().toLowerCase()
                    .contains(getPackageName()))
                return false;
        }
        return true;
    }

}
