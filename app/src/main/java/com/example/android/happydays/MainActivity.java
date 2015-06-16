package com.example.android.happydays;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.parse.ParseUser;

import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends ActionBarActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    public static final String LOGIN_CHOICE_FACEBOOK = "FACEBOOK";
    public static final String LOGIN_CHOICE_PARSE = "PARSE";

    protected String mLoginChoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Check if the User is login by using FB or ParseUser
        if (AccessToken.getCurrentAccessToken()!=null && Profile.getCurrentProfile()!=null){
            mLoginChoice = LOGIN_CHOICE_FACEBOOK;
            Log.d(TAG, "Login with Facebook.");
        }
        else{
            ParseUser currentUser = ParseUser.getCurrentUser();
            if (currentUser == null){
                navigateToLogin();
            }
            else{
                mLoginChoice = LOGIN_CHOICE_PARSE;
                Log.i(TAG, currentUser.getUsername());
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            Log.d(TAG, "Mlogin:"+mLoginChoice);
            Log.d(TAG, "FB: " + LOGIN_CHOICE_FACEBOOK);
            Log.d(TAG, "Parse: " + LOGIN_CHOICE_PARSE);
            if (mLoginChoice.equals(LOGIN_CHOICE_FACEBOOK)){
                LoginManager.getInstance().logOut();
                Log.d(TAG, "Logout with FB");
            }
            else if (mLoginChoice.equals(LOGIN_CHOICE_PARSE)){
                ParseUser.logOut();
                Log.d(TAG, "Logout with Parse");
            }
            navigateToLogin();
        }

        return super.onOptionsItemSelected(item);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);

        //When the user clicks "back", he wont come back to the main layout

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
    }

    //Button to add happy moment

    @OnClick(R.id.addButton) void addButton(){
        Toast.makeText(MainActivity.this, "Clicked.noticed Button shadow", Toast.LENGTH_SHORT).show();
    }
}
