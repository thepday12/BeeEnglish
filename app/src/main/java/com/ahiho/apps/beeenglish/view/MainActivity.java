package com.ahiho.apps.beeenglish.view;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ahiho.apps.beeenglish.R;
import com.ahiho.apps.beeenglish.model.ResponseData;
import com.ahiho.apps.beeenglish.util.Identity;
import com.ahiho.apps.beeenglish.util.MyConnection;
import com.ahiho.apps.beeenglish.util.UtilSharedPreferences;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class MainActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {

    private TextView tvForgotPassword;
    private EditText etEmail, etPassword;
    private Button btSignIn, btSignUp;
    private LoginButton btSignInFacebook;
    private SignInButton btSignInGoogle;
    private GoogleApiClient mGoogleApiClient;
    private CallbackManager callbackManager;
    private final int RC_SIGN_IN = 100;
    private final int RC_SIGN_UP = 101;
    private UtilSharedPreferences mUtilSharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
//        startActivity(new Intent(MainActivity.this,TestVocabularyActivity.class));
//new DownloadFile().execute();
        long accessTime = mUtilSharedPreferences.getAccessTokenExpired()*1000;
        long time = accessTime - System.currentTimeMillis();
        if (time <= 0) {
            try {
                LoginManager.getInstance().logOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
            } catch (Exception e) {

            }
        } else {
            if (isLogin()) {
                startHomeActivity();
            }
        }
    }


    private void startHomeActivity() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void init() {
        FacebookSdk.sdkInitialize(MainActivity.this);
        setContentView(R.layout.activity_main);
        mUtilSharedPreferences = UtilSharedPreferences.getInstanceSharedPreferences(MainActivity.this);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        btSignIn = (Button) findViewById(R.id.btSignIn);
        btSignUp = (Button) findViewById(R.id.btSignUp);
        btSignInFacebook = (LoginButton) findViewById(R.id.btSignInFacebook);
        btSignInGoogle = (SignInButton) findViewById(R.id.btSignInGoogle);
        tvForgotPassword = (TextView) findViewById(R.id.tvForgotPassword);

        btSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        configSignInGoogle();
        btSignInGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mGoogleApiClient.isConnecting()) {
                    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                }
            }
        });

        btSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, SignUpActivity.class), RC_SIGN_UP);
            }
        });

        callbackManager = CallbackManager.Factory.create();
        btSignInFacebook.setReadPermissions("public_profile", "email");
        btSignInFacebook.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
//                    "User ID: "
//                            + loginResult.getAccessToken().getUserId()
//                            + "\n" +
//                            "Auth Token: "
//                            + loginResult.getAccessToken().getToken()
                handleSignInFacebook(loginResult.getAccessToken(), loginResult.getAccessToken().getUserId());

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

    }

    private boolean isLogin() {
//        if(MyConnection.isOnline(MainActivity.this)) {
//            boolean result = false;
//            long timeExpired = UtilSharedPreferences.getInstanceSharedPreferences(MainActivity.this).getAccessTokenExpired();
//            long currentTime = System.currentTimeMillis() / 1000;
//            if ((timeExpired - currentTime) > 0) {
//                result = true;
//            }
//            return result;
//        }else{
//            return true;
//        }
        String userData = UtilSharedPreferences.getInstanceSharedPreferences(MainActivity.this).getUserData();
        return !userData.isEmpty();
    }

    private void configSignInGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(Plus.API)
                .build();
    }

    private void signIn() {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        if (!email.contains(" ")) {
            if (password.length() > 4) {
                if (isOnline())
                    new SignIn(email, password).execute();
            } else {
                etPassword.setError(getString(R.string.err_password_short));
                etPassword.requestFocus();
            }
        } else {
            etEmail.setError(getString(R.string.err_username_invalid));
            etEmail.requestFocus();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RC_SIGN_IN: {
//            mDialog = ProgressDialog.show(LoginDialog.this, null,
//                    getString(R.string.loading), true);
                    GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                    GoogleSignInAccount account = result.getSignInAccount();
                    handleSignInGoogle(result);
                }
                break;
                case RC_SIGN_UP:
                    String email = data.getStringExtra(Identity.EXTRA_USER_NAME);
                    String password = data.getStringExtra(Identity.EXTRA_PASSWORD);
                    etEmail.setText(email);
                    etPassword.setText(password);
                    signIn();
                    break;
                default:
                    if (callbackManager != null)
                        callbackManager.onActivityResult(requestCode, resultCode, data);
                    // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
                    break;
            }
        }
    }

    private void handleSignInGoogle(GoogleSignInResult result) {
        if (result.isSuccess()) {
            String token = result.getSignInAccount().getIdToken();
            new SignInGmail(token).execute();
//            String birthday = "";
//            int sex = 0;
//            try {
//                Person person = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
//                // Signed in successfully, show authenticated UI.
//                sex = person.getGender();
//                if (person.getBirthday() != null)
//                    birthday = person.getBirthday();
//            } catch (Exception ex) {
//
//            }
//            GoogleSignInAccount acct = result.getSignInAccount();
//            String id = acct.getId();
//            String email = acct.getEmail();
//            String name = acct.getDisplayName();
//            String linkAvatar = "";
//            try {
//                Uri uri = acct.getPhotoUrl();
////                String is = acct.getIdToken();
//                if (uri != null)
//                    linkAvatar = uri.toString();
//            } catch (Exception ex) {
//            }

//            new SignUpTask(1,id,name,email).execute();
//            String info = "ID: " + id + "\nEmail: " + email + "\nName: " + name + " - Gender: " + sex + "\nBirthday: " + birthday;
//            Log.e("RESPONSE_SERVER", info);
        } else {
            // Signed out, show unauthenticated UI.
//            Toast.makeText(MainActivity.this, "Không thể đăng nhập", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSignInFacebook(AccessToken accessToken, final String socialId) {
        String tokenKey = accessToken.getToken();
        new SignInFacebook(tokenKey).execute();
//        GraphRequest request = GraphRequest.newMeRequest(
//                accessToken,
//                new GraphRequest.GraphJSONObjectCallback() {
//                    @Override
//                    public void onCompleted(
//                            JSONObject object,
//                            GraphResponse response) {
//                        // Application code
//                        String name = "";
//                        String link = "";
//                        String gender = "";
//                        try {
//                            name = object.getString("name");
//                            link = "http://graph.facebook.com/" + socialId + "/picture?width=100&height=100";
//                            gender = object.getString("gender");
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                        String birthday = "";
//
//                        try {
//                            birthday = formatDateFacebook(object.getString("birthday"));
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//
//                        int sex = 0;
//                        if (gender.equals("female") || gender.equals("nữ")) {
//                            sex = 1;
//                        }
//                        String socialIdNew = socialId;
//
//                    }
//                });
//
//        Bundle parameters = new Bundle();
//        parameters.putString("fields", "id,name,link,gender,birthday,age_range,picture.width(150).height(150)");
//        request.setParameters(parameters);
//        request.executeAsync();
    }

    private String formatDateFacebook(String birthday) {
        DateFormat inputFormat = new SimpleDateFormat("MM/dd/yyyy");
        inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date parsed = new Date();
        try {
            parsed = inputFormat.parse(birthday);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String outputText = outputFormat.format(parsed);
        return outputText;
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    class SignIn extends AsyncTask<Void, Void, ResponseData> {

        private String mEmail, mPassword;

        public SignIn(String... params) {
            mEmail = params[0];
            mPassword = params[1];
        }

        @Override
        protected void onPreExecute() {
            showDialogLoading();
        }

        @Override
        protected ResponseData doInBackground(Void... params) {
            return MyConnection.getInstanceMyConnection(MainActivity.this).signIn(mEmail, mPassword);
        }

        @Override
        protected void onPostExecute(ResponseData responseData) {
            dismissDialogLoading();
            if (responseData.isResponseState()) {

                postSignIn(responseData);
            } else {
                showSnackBar(responseData.getResponseData());
            }
            super.onPostExecute(responseData);
        }
    }

    class SignInFacebook extends AsyncTask<Void, Void, ResponseData> {

        private String accessToken;

        public SignInFacebook(String accessToken) {
            this.accessToken = accessToken;
        }

        @Override
        protected void onPreExecute() {
            showDialogLoading();
        }

        @Override
        protected ResponseData doInBackground(Void... params) {
            return MyConnection.getInstanceMyConnection(MainActivity.this).signInFacebook(accessToken);
        }

        @Override
        protected void onPostExecute(ResponseData responseData) {
            dismissDialogLoading();
            if (responseData.isResponseState()) {

                postSignIn(responseData);
            } else {
                LoginManager.getInstance().logOut();
                showSnackBar(responseData.getResponseData());
            }
            super.onPostExecute(responseData);
        }
    }

    class SignInGmail extends AsyncTask<Void, Void, ResponseData> {

        private String accessToken;

        public SignInGmail(String accessToken) {
            this.accessToken = accessToken;
        }

        @Override
        protected void onPreExecute() {
            showDialogLoading();
        }

        @Override
        protected ResponseData doInBackground(Void... params) {
            return MyConnection.getInstanceMyConnection(MainActivity.this).signInGmail(accessToken);
        }

        @Override
        protected void onPostExecute(ResponseData responseData) {
            dismissDialogLoading();
            if (responseData.isResponseState()) {
                postSignIn(responseData);
            } else {
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                showSnackBar(responseData.getResponseData());
            }
            super.onPostExecute(responseData);
        }
    }

    private void postSignIn(ResponseData responseData) {
        try {
            if (responseData.isResponseState()) {
                JSONObject jsonObject = new JSONObject(responseData.getResponseData());
                if (jsonObject.getBoolean("success")) {
                    JSONObject data = jsonObject.getJSONObject("data");
                    mUtilSharedPreferences.setUserData(data.getString("user"));
                    mUtilSharedPreferences.setAccessToken(data.getString("access_token"));
                    mUtilSharedPreferences.setAccessTokenExpired(data.getLong("expired_in"));
                    mUtilSharedPreferences.setTrialTimeExpired(data.getJSONObject("user").getString("expired"));
                    showToast(R.string.success_sign_in, Toast.LENGTH_LONG);
                    startHomeActivity();
                } else {
                    try {
                        LoginManager.getInstance().logOut();
                        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                    } catch (Exception e) {

                    }
                    showSnackBar(responseData.getResponseData());
                }
            } else {
                try {
                    LoginManager.getInstance().logOut();
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                } catch (Exception e) {

                }
                showSnackBar(responseData.getResponseData());
            }
        } catch (JSONException e) {
            try {
                LoginManager.getInstance().logOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
            } catch (Exception ex) {

            }
            showSnackBar(R.string.err_json_exception);
        }
    }

}
