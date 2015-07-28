package com.example.android.happydays;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.example.android.happydays.ui.MainActivity;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.widget.LoginButton;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class LoginActivity extends Activity {

    public static final String TAG = LoginActivity.class.getSimpleName();

    protected LoginButton mFacebookButton;

    protected boolean delayFacebook;

    @InjectView(R.id.emailText)
    EditText mEmailText;
    @InjectView(R.id.passwordText)
    EditText mPasswordText;
    @InjectView(R.id.progressBar)
    ProgressBar mProgressBar;
    @InjectView(R.id.loginButton)
    Button mLoginButton;
    @InjectView(R.id.signUpButton)
    Button mSignUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);
        mProgressBar.setVisibility(View.INVISIBLE);
        delayFacebook = true;

        //Avoid to show start the ParseFacebook utilities until the user clicks the Login button
        mFacebookButton = (LoginButton) findViewById(R.id.loginFacebookButton);
        mFacebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delayFacebook = false;
                disableButtons();
            }
        });

        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableButtons();
                String email = mEmailText.getText().toString().trim();
                String password = mPasswordText.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    AlertDialogGenerator dialog = new AlertDialogGenerator();
                    dialog.showAlertDialog(LoginActivity.this, getString(R.string.login_error_message), getString(R.string.error_title));
                    enableButtons();
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    loginUser(email, password);
                }

            }
        });

            ParseFacebookUtils.logInWithReadPermissionsInBackground(LoginActivity.this,
                    Arrays.asList("public_profile", "email", "user_birthday"),
                    new LogInCallback() {
                        @Override
                        public void done(final ParseUser user, ParseException err) {
                            if (user == null) {
                                Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
                            } else if (user.isNew()) {
                                Log.d("MyApp", "User signed up and logged in through Facebook!");

                                HappyDaysApplication.updateParseInstallation(user);
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.putExtra(AppConstants.LOGIN_CHOICE, AppConstants.LOGIN_CHOICE_FACEBOOK);
                                intent.putExtra(AppConstants.NAME_ACTIVITY, AppConstants.LOGIN_ACTIVITY);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                Log.d(TAG, "Sign up and Login");

                                GraphRequest request = GraphRequest.newMeRequest(
                                        AccessToken.getCurrentAccessToken(),
                                        new GraphRequest.GraphJSONObjectCallback() {
                                            @Override
                                            public void onCompleted(
                                                    JSONObject object,
                                                    GraphResponse response) {
                                                // Application code
                                                mProgressBar.setVisibility(View.INVISIBLE);
                                                HappyDaysApplication.updateParseInstallation(user);

                                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                intent.putExtra(AppConstants.LOGIN_CHOICE, AppConstants.LOGIN_CHOICE_FACEBOOK);
                                                intent.putExtra(AppConstants.NAME_ACTIVITY, AppConstants.LOGIN_ACTIVITY);
                                                try {
                                                    user.put(ParseConstants.KEY_NAME, object.get(FaceBookConstants.USER_KEY_NAME).toString());
                                                    user.put(ParseConstants.KEY_GENDER, object.get(FaceBookConstants.USER_KEY_GENDER).toString());
                                                    user.put(ParseConstants.KEY_BIRTHDAY, object.get(FaceBookConstants.USER_KEY_BIRTHDAY).toString());
                                                    user.saveInBackground();
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                Log.d(TAG, "Sign up and Login");
                                            }
                                        });
                                Bundle parameters = new Bundle();
                                parameters.putString(FaceBookConstants.USER_KEY_FIELDS,
                                        FaceBookConstants.USER_KEY_ID + "," +
                                                FaceBookConstants.USER_KEY_NAME + "," +
                                                FaceBookConstants.USER_KEY_EMAIL + "," +
                                                FaceBookConstants.USER_KEY_GENDER + "," +
                                                FaceBookConstants.USER_KEY_BIRTHDAY);
                                request.setParameters(parameters);
                                request.executeAsync();

                            } else {
                                Log.d("MyApp", "User logged in through Facebook!");
                                HappyDaysApplication.updateParseInstallation(user);
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.putExtra(AppConstants.LOGIN_CHOICE, AppConstants.LOGIN_CHOICE_FACEBOOK);
                                intent.putExtra(AppConstants.NAME_ACTIVITY, AppConstants.LOGIN_ACTIVITY);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                Log.d(TAG, "Login");
                            }
                            enableButtons();
                        }
                    });
        }

//    //Trigger when the users signs up and logs in on Facebook.
//    private void SignUpLoginParseFaceBook(JSONObject object, GraphResponse response, ParseUser user) {
//        //
//        try {
//            //Add facebook info to the parse facebook user
//            user.put(ParseConstants.KEY_NAME, object.get(ParseConstants.KEY_NAME).toString());
//            user.put(ParseConstants.KEY_GENDER, object.get(ParseConstants.KEY_GENDER).toString());
//            user.put(ParseConstants.KEY_BIRTHDAY, object.get(ParseConstants.KEY_BIRTHDAY).toString());
//
//            if (object.get(ParseConstants.KEY_USER_EMAIL).toString() == null) {
//                user.setEmail(object.get(getString(R.string.empty_string)).toString());
//            } else {
//                user.setEmail(object.get(ParseConstants.KEY_USER_EMAIL).toString());
//            }
//
//            user.signUpInBackground(new SignUpCallback() {
//                @Override
//                public void done(ParseException e) {
//                    if (e==null){
//
//                    }
//                    else{
//                        e.printStackTrace();
//                    }
//                }
//            });
//
//            Log.d(TAG, "Id:" + object.get("id").toString());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        Log.v(TAG, response.toString());
//    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!delayFacebook) {
            ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void enableButtons() {
        mLoginButton.setEnabled(true);
        mFacebookButton.setEnabled(true);
        mSignUpButton.setEnabled(true);
    }

    private void disableButtons() {
        mFacebookButton.setEnabled(false);
        mSignUpButton.setEnabled(false);
        mLoginButton.setEnabled(false);
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
                    intent.putExtra(AppConstants.LOGIN_CHOICE, AppConstants.LOGIN_CHOICE_PARSE);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    AlertDialogGenerator dialog = new AlertDialogGenerator();
                    dialog.showAlertDialog(LoginActivity.this, e.getMessage(), getString(R.string.error_title));
                }
                enableButtons();
            }
        });

    }

}
