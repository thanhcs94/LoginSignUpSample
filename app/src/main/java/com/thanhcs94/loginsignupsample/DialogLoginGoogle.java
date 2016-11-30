package com.thanhcs94.loginsignupsample;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DialogLoginGoogle extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{
    public static String TAG = "DialogLoginGoogle".toUpperCase();
    // Value
    String socialEmail;
    String socialBirthday;
    String socialGender;
    String accessToken , uId , type;
    //google
    private static final int REQ_SIGN_IN_REQUIRED = 55664;
    private static final int REQUEST_SIGNUP = 0;
    @Bind(R.id.btn_sign_in_google)
    SignInButton signInGoogle;
    private static final int RC_SIGN_IN = 007;
    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;
    public String  gMailPlus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_login_social_loading);
        ButterKnife.bind(this);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        getWindow().setBackgroundDrawable(new ColorDrawable(0));
        setTitle("");
        try {
            getActionBar().hide();
        }catch (Exception e){

        }
        /***
         * G++ login
         * Start G++_login
         */
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(Plus.API)
                .build();
        signInGoogle.setSize(SignInButton.SIZE_STANDARD);
        signInGoogle.setScopes(gso.getScopeArray());
        signInGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
                if (opr.isDone()) {
                    // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
                    // and the GoogleSignInResult will be available instantly.
                    Log.d(TAG, "Got cached sign-in");
                    GoogleSignInResult result = opr.get();
                    handleSignInResult(result);
                } else {
                    // If the user has not previously signed in on this device or the sign-in has expired,
                    // this asynchronous branch will attempt to sign in the user silently.  Cross-device
                    // single sign-on will occur in this branch.
                    showProgressDialog();
                    opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                        @Override
                        public void onResult(GoogleSignInResult googleSignInResult) {
                            hideProgressDialog();
                            handleSignInResult(googleSignInResult);
                        }
                    });
                }

                signIn();
            }
        });
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("loading...");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    public void onSignOutClicked() {
        Log.e(TAG, "onSignOutClicked");
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Could not connect to Google Play Services.  The user needs to select an account,
        // grant permissions or resolve an errorloading in order to sign in. Refer to the javadoc for
        // ConnectionResult to see possible errorloading codes.
        Log.e(TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }

        if (requestCode == REQ_SIGN_IN_REQUIRED && resultCode == RESULT_OK) {
            // We had to sign in - now we can finish off the token request.
            new RetrieveTokenTask().execute(gMailPlus);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            Log.e(TAG, "display name: " + acct.getDisplayName());

            final String personName = acct.getDisplayName();
            final String email = acct.getEmail();
            gMailPlus = email;
            uId = acct.getId();
            type = "Gmail";
            new RetrieveTokenTask().execute(gMailPlus);
            Log.e(TAG, "Name: " + personName + ", email: " + email +" uId "+ uId);

            //             loginwithSocial(assettoken, "Gmail", uId);

        } else {

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent().getBooleanExtra("LOGOUT", false)) {
            onSignOutClicked();
        }
    }
    @Override
    public void onStop() {
        super.onStop();
        Log.e(TAG, "onStop");
        mGoogleApiClient.disconnect();
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        //updateUI(false);
                    }
                });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        overridePendingTransition(0,0);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0,0);
    }

    private class RetrieveTokenTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String accountName = params[0];
            String scopes = "oauth2:profile email";
            String token = null;
            try {
                token = GoogleAuthUtil.getToken(getApplicationContext(), accountName, scopes);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            } catch (UserRecoverableAuthException e) {
                startActivityForResult(e.getIntent(), REQ_SIGN_IN_REQUIRED);
            } catch (GoogleAuthException e) {
                Log.e(TAG, e.getMessage());
            }
            return token;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(s != null){
                if(!s.isEmpty()) {
                    accessToken = s;
                    //loginwithSocial(accessToken, type, uId, TRACK_SOCIAL_GPLUS);
                    /** return data tu Login class herre
                     */
                    Log.wtf("Data GPlus", " token "+s);
                }
                Log.wtf("Data GPlus", " token "+s);
            }
            Log.wtf("Data GPlus", " token "+s);
        }
    }
}
