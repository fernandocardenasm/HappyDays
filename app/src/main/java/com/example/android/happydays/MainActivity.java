package com.example.android.happydays;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.parse.ParseUser;

import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends ActionBarActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    protected String mCurrentPhotoPath;
    protected String mLoginChoice;
    private Uri mMediaUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        Intent intent = getIntent();
        if (intent.getStringExtra(AppConstants.NAME_ACTIVITY)!=null) {
            if (intent.getStringExtra(AppConstants.NAME_ACTIVITY).equals(AppConstants.LOGIN_ACTIVITY)) {
                Log.d(TAG, "Comes from login");
            } else if (intent.getStringExtra(AppConstants.NAME_ACTIVITY).equals(AppConstants.SIGNUP_ACTIVITY)) {
                Log.d(TAG, "Comes from sign_up");
            } else if (intent.getStringExtra(AppConstants.NAME_ACTIVITY).equals(AppConstants.MOMENT_ACTIVITY)) {
                Log.d(TAG, "Comes from moment");
                Toast.makeText(MainActivity.this, "The moment was successfully created.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        mLoginChoice =  intent.getStringExtra(AppConstants.LOGIN_CHOICE);

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null){
                navigateToLogin();
        }
        else{
            mLoginChoice = AppConstants.LOGIN_CHOICE_PARSE;
            Log.d(TAG, "Current User:"+ currentUser.getUsername());
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
            if (mLoginChoice.equals(AppConstants.LOGIN_CHOICE_FACEBOOK)){
                LoginManager.getInstance().logOut();
                ParseUser.logOut();
                Log.d(TAG, "Logout with FB");
            }
            else if (mLoginChoice.equals(AppConstants.LOGIN_CHOICE_PARSE)){
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

    //Button to go to create a happy Moment

    @OnClick(R.id.addButton) void addButton(){

        Intent intent = new Intent(this, MomentActivity.class);
        intent.putExtra(AppConstants.LOGIN_CHOICE, mLoginChoice);
        startActivity(intent);
    }




}
