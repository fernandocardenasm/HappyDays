package com.example.android.happydays;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.parse.ParseUser;


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

        //Check if the User is login by using FB or ParseUser
        if (AccessToken.getCurrentAccessToken()!=null){
            Log.d(TAG, "Connected with FB.");
            mLoginChoice = LOGIN_CHOICE_FACEBOOK;
            Intent intent = getIntent();
            
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
            ParseUser.logOut();
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
}
