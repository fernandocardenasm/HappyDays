package com.example.android.happydays;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class LoginActivity extends Activity {

    public static final String TAG = LoginActivity.class.getSimpleName();

    protected LoginButton mFacebookButton;
    protected CallbackManager mCallbackManager;

    @InjectView(R.id.emailText) EditText mEmailText;
    @InjectView(R.id.passwordText) EditText mPasswordText;
    @InjectView(R.id.progressBar) ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);
        mProgressBar.setVisibility(View.INVISIBLE);
        mFacebookButton = (LoginButton) findViewById(R.id.loginFacebookButton);
        mFacebookButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_birthday"));

        //Register callback
        mCallbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {

                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(
                                            JSONObject object,
                                            GraphResponse response) {
                                        // Application code
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        try {
                                            intent.putExtra("LOGIN_CHOICE", AppConstants.LOGIN_CHOICE_FACEBOOK);
                                            intent.putExtra(AppConstants.NAME_ACTIVITY, AppConstants.LOGIN_ACTIVITY);

                                            intent.putExtra(FaceBookConstants.USER_KEY_ID,object.get(FaceBookConstants.USER_KEY_ID).toString());
                                            intent.putExtra(FaceBookConstants.USER_KEY_NAME,object.get(FaceBookConstants.USER_KEY_NAME).toString());
                                            intent.putExtra(FaceBookConstants.USER_KEY_GENDER,object.get(FaceBookConstants.USER_KEY_GENDER).toString());
                                            intent.putExtra(FaceBookConstants.USER_KEY_BIRTHDAY,object.get(FaceBookConstants.USER_KEY_BIRTHDAY).toString());

                                            if (object.get(FaceBookConstants.USER_KEY_EMAIL).toString()==null){
                                                intent.putExtra(FaceBookConstants.USER_KEY_EMAIL,object.get("empty").toString());
                                            }
                                            else{
                                                intent.putExtra(FaceBookConstants.USER_KEY_EMAIL,object.get(FaceBookConstants.USER_KEY_EMAIL).toString());
                                            }

                                            Log.d(TAG,"Id:"+object.get("id").toString());
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        Log.v(TAG, response.toString());
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString(FaceBookConstants.USER_KEY_FIELDS,
                                FaceBookConstants.USER_KEY_ID+","+
                                FaceBookConstants.USER_KEY_NAME+","+
                                FaceBookConstants.USER_KEY_EMAIL+","+
                                FaceBookConstants.USER_KEY_GENDER+","+
                                FaceBookConstants.USER_KEY_BIRTHDAY);
                        request.setParameters(parameters);
                        request.executeAsync();

                        Log.d(TAG, "Success: ");

                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "Cancel");
                    }

                    @Override
                    public void onError(FacebookException e) {
                        Log.d(TAG, "Error");
                    }
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.signUpButton) void goToSignUpActivity(){
        Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(intent);
    }

    //Trigger when login button is pressed.
    @OnClick(R.id.loginButton) void submit(){
        String email = mEmailText.getText().toString().trim();
        String password = mPasswordText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()){
            AlertDialogGenerator dialog = new AlertDialogGenerator();
            dialog.showAlertDialog(LoginActivity.this, getString(R.string.login_error_message), getString(R.string.error_title));
        }
        else{
            mProgressBar.setVisibility(View.VISIBLE);
            loginUser(email, password);
        }
    }

    private void loginUser(String username, String password) {
        //Log in

        //mLoginProgressBar.setVisibility(View.VISIBLE);

        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                //mLoginProgressBar.setVisibility(View.INVISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);
                if (e == null) {
                    //Update Parse Installation}
                    HappyDaysApplication.updateParseInstallation(ParseUser.getCurrentUser());

                    //Success
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra(AppConstants.NAME_ACTIVITY, AppConstants.LOGIN_ACTIVITY);
                    intent.putExtra("LOGIN_CHOICE", AppConstants.LOGIN_CHOICE_PARSE);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    AlertDialogGenerator dialog = new AlertDialogGenerator();
                    dialog.showAlertDialog(LoginActivity.this, e.getMessage(), getString(R.string.error_title));
                }
            }
        });

    }
}
