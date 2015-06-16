package com.example.android.happydays;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends ActionBarActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    public static final String LOGIN_CHOICE_FACEBOOK = "FACEBOOK";
    public static final String LOGIN_CHOICE_PARSE = "PARSE";

    static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int MEDIA_TYPE_IMAGE = 2;

    protected String mCurrentPhotoPath;
    protected String mLoginChoice;
    private Uri mMediaUri;

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
        dispatchTakePictureIntent();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            //Save Image
            mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
            if (mMediaUri == null){
                Toast.makeText(MainActivity.this, "There was an error accessing your device's external storage.", Toast.LENGTH_SHORT).show();
            }
            else{
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);

                //Get the result of the intent
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
            //mImageView.setImageBitmap(imageBitmap);
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(mMediaUri);
            sendBroadcast(mediaScanIntent);
        }
    }

    private Uri getOutputMediaFileUri(int mediaType) {
        //To be safe, you should check that the SDCard is mounted
        //using Environment.getExternalStorageState() before doing this

        if (isExternalStorageAvailable()){
            //get the URI

            //1. Get the external  storage directory

            String appName = MainActivity.this.getString(R.string.app_name);

            File mediaStorageDir = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    appName);
            //2. Create our subdirectory

            if (! mediaStorageDir.exists()){
                if (! mediaStorageDir.mkdirs()){
                    Log.e(TAG, getString(R.string.error_failed_to_create_directory));
                    return null;
                }
            }
            //3. Create a filename
            //4. Create the file

            File mediaFile;
            Date now = new Date();
            String timestap = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(now);

            String path = mediaStorageDir.getPath() + File.separator;

            if (mediaType == MEDIA_TYPE_IMAGE){
                mediaFile = new File(path + "IMG" + timestap + ".jpg");
            }
            else{
                return null;
            }

            Log.d(TAG, "File: " + Uri.fromFile(mediaFile));
            //5. Create the file's Uri
            return Uri.fromFile(mediaFile);
        }
        else{
            return null;
        }
    }

    private boolean isExternalStorageAvailable(){
        String state = Environment.getExternalStorageState();
        if ( state.equals(Environment.MEDIA_MOUNTED)){
            return true;
        }
        else{
            return false;
        }
    }


}
