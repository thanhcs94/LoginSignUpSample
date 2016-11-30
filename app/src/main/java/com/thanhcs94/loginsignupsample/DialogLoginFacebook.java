package com.thanhcs94.loginsignupsample;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DialogLoginFacebook extends AppCompatActivity {

    //facebook
    private CallbackManager callbackManager;
    @Bind(R.id.fb_login_button)
    LoginButton signInFB;
    String accessToken , uId , type;
    String socialEmail;
    String socialBirthday;
    String socialGender;
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
         * this place for Facebook Login
         * Start Fb_login
         */
        getHashCode();
        //facebook
        signInFB.setReadPermissions(Arrays.asList("public_profile", "email", "user_birthday", "user_friends"));
        callbackManager = CallbackManager.Factory.create();
        signInFB.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                ProfileTracker profileTracker = new ProfileTracker() {
                    @Override
                    protected void onCurrentProfileChanged(
                            Profile oldProfile, final Profile currentProfile) {
                        try {
                            type = "Facebook";
                            uId = currentProfile.getId();
                            String uName = currentProfile.getName();
                            final String firstName = currentProfile.getFirstName();
                            final String lastName = currentProfile.getLastName();
                            final String uAvata = currentProfile.getProfilePictureUri(400, 400).toString();
                            String assettoken = "";
                            AccessToken token = AccessToken.getCurrentAccessToken();
                            if (token != null) {
                                assettoken = token.getToken();
                            }
                            // App code
                            accessToken = assettoken;
                            GraphRequest request = GraphRequest.newMeRequest(
                                    loginResult.getAccessToken(),
                                    new GraphRequest.GraphJSONObjectCallback() {
                                        @Override
                                        public void onCompleted(JSONObject object, GraphResponse response) {
                                            Log.v("LoginActivity", response.toString());
                                            // Application code
                                            try {
                                                socialEmail = object.getString("email");
                                                socialGender = object.getString("gender");
                                                String sms = "User Name: " + firstName + " " + lastName + " " + socialEmail + " " + socialBirthday + " " + socialGender + " -  " + uId + " - " + uAvata + " - " + accessToken;
                                                Log.wtf("FACEBOOK", sms);
                                                if (socialEmail.contains("@")) {
                                                    //    loginwithSocial(accessToken, type, uId, TRACK_SOCIAL_FB);
                                                }
                                            } catch (JSONException e) {
                                                String sms = "User Name: " + firstName + " " + lastName + " " + socialEmail + " " + socialBirthday + " " + socialGender + " -  " + uId + " - " + uAvata + " - " + accessToken;
                                                //loginwithSocial(accessToken, type, uId, TRACK_SOCIAL_FB);
                                                Log.wtf("FACEBOOK Error", sms);
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                            Bundle parameters = new Bundle();
                            parameters.putString("fields", "id,name,email,gender,birthday");
                            request.setParameters(parameters);
                            request.executeAsync();
                        } catch (Exception e) {
                        }

                    }

                };

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException e) {

            }
        });
        signInFB.performClick();
    }

    /** Facebook **/
    public void getHashCode() {

        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "your_package",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.wtf("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
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
}
