package com.example.android.happydays.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.android.happydays.AppConstants;
import com.example.android.happydays.ParseConstants;
import com.example.android.happydays.R;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;


public class MainActivity extends ActionBarActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    protected String mLoginChoice;

    protected ParseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
//        setContentView(R.layout.activity_main);
//        ButterKnife.inject(this);
//
//        mEmptyTextView = (TextView) findViewById(android.R.id.empty);
//
//        mGridView = (GridView) findViewById(R.id.gridView);
//        mGridView.setEmptyView(mEmptyTextView);

//        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
//        mSwipeRefreshLayout.setOnRefreshListener(mOnRefresherListener);
//        mSwipeRefreshLayout.setColorSchemeColors(
//                R.color.swipeRefresh1,
//                R.color.swipeRefresh2,
//                R.color.swipeRefresh3,
//                R.color.swipeRefresh4
//        );


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

        currentUser = ParseUser.getCurrentUser();
        if (currentUser == null){
                navigateToLogin();
        }
        else{
            mLoginChoice = AppConstants.LOGIN_CHOICE_PARSE;
            Log.d(TAG, "Current User:"+ currentUser.getUsername());

            String tag = ImageGridFragment.class.getSimpleName();

            //Bring the moment
            Fragment fr = getSupportFragmentManager().findFragmentByTag(tag);
            if (fr == null) {
                fr = new ImageGridFragment();
            }
            setTitle("Happy Moments");
            getSupportFragmentManager().beginTransaction().replace(android.R.id.content, fr, tag).commit();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
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

    //Load the moments from the Parse database

    private void loadMoments() {
        ParseQuery<ParseObject> query = new ParseQuery<>(ParseConstants.CLASS_MOMENTS);
        query.whereEqualTo(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
        query.addAscendingOrder(ParseConstants.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> moments, ParseException e) {

                //Validating if the SwipeRefresher is being used

//                if (mSwipeRefreshLayout.isRefreshing()){
//                    mSwipeRefreshLayout.setRefreshing(false);
//                }
//
//                if (e == null){
//                    //We found moments
//                    mMoments = moments;
//                    if (mGridView.getAdapter() == null){
//                        GridViewAdapter mGridViewAdapter = new GridViewAdapter(MainActivity.this, R.layout.grid_item_layout, moments);
//                        mGridView.setAdapter(mGridViewAdapter);
//                    }
//                    else{
//                        ((GridViewAdapter)mGridView.getAdapter()).refill(mMoments);
//                    }
//                    mGridView.setOnItemClickListener(mOnItemClickListener);
//                }
            }
        });
    }


//    protected SwipeRefreshLayout.OnRefreshListener mOnRefresherListener = new SwipeRefreshLayout.OnRefreshListener() {
//        @Override
//        public void onRefresh() {
//            loadMoments();
//        }
//    };

//    Button to go to create a happy Moment

//    @OnClick(R.id.addButton) void addButton(){
//
//        Intent intent = new Intent(this, MomentActivity.class);
//        intent.putExtra(AppConstants.LOGIN_CHOICE, mLoginChoice);
//        startActivity(intent);
//    }


}
