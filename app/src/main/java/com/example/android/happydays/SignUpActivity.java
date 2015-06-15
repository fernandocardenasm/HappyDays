package com.example.android.happydays;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.facebook.FacebookSdk;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class SignUpActivity extends ActionBarActivity {

    public static final String TAG = SignUpActivity.class.getSimpleName();

    @InjectView(R.id.nameText) EditText mName;
    @InjectView(R.id.usernameText) EditText mUsername;
    @InjectView(R.id.emailText) EditText mEmail;
    @InjectView(R.id.passwordText) EditText mPassword;
    @InjectView(R.id.passwordTextConfirm) EditText mPasswordConfirm;
    @InjectView(R.id.progressBar) ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_sign_up);
        ButterKnife.inject(this);
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    //Trigger when the "Sign Up" button is pressed.
    @OnClick(R.id.signUpButton) void submitUserForm(){

        String name = mName.getText().toString();
        String username = mUsername.getText().toString().trim();
        String email = mEmail.getText().toString().trim();
        String passw = mPassword.getText().toString().trim();
        String passwConfirm = mPasswordConfirm.getText().toString().trim();

        AlertDialogGenerator dialog = new AlertDialogGenerator();

        if (name.trim().isEmpty() || username.isEmpty() || email.isEmpty() || passw.isEmpty() || passwConfirm.isEmpty()){
            dialog.showAlertDialog(SignUpActivity.this, "Please make sure you entered an username, email and password.", "Opps");
        }
        else if(!passw.equals(passwConfirm)){
            dialog.showAlertDialog(SignUpActivity.this, "Both Passwords must be equal.", "Opps");
        }
        else if(!isEmailValid(email)){
            dialog.showAlertDialog(SignUpActivity.this, "The email is not valid.", "Opps");
        }
        else{
            mProgressBar.setVisibility(View.VISIBLE);
            signUpUser(name, username, email, passw);
        }

    }

    //Create the User in the Parse Database
    protected void signUpUser(String name, String username, String email, String passw){
        //Create the parse user
        ParseUser user = new ParseUser();

        //Set core properties
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passw);

        //Set custom properties
        user.put(ParseConstants.KEY_NAME, name);
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(final ParseException e) {
                mProgressBar.setVisibility(View.INVISIBLE);
                if (e==null){
                    Log.v(TAG, "Si");
                }
                else{
                    Log.v(TAG, "No");
                }
            }
        });
    }

    protected final boolean isEmailValid(CharSequence email){
        if (email == null)
            return false;
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sign_up, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
