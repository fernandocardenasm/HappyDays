package com.example.android.happydays;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MomentActivity extends ActionBarActivity {

    public final static String TAG = MomentActivity.class.getSimpleName();

    protected Uri mMediaUri;
    protected String mLoginChoice;

    ProgressDialog progressDialog;

    @InjectView(R.id.momentText) EditText mMomentText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moment);
        ButterKnife.inject(this);

        Intent intent = getIntent();
        mMediaUri = intent.getData();
        mLoginChoice = intent.getStringExtra(AppConstants.LOGIN_CHOICE);
        showImageInActivity(mMediaUri);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_moment, menu);
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

    //Show the user's taken image in the layout
    public void showImageInActivity(Uri uri){
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            ImageView imageView = (ImageView) findViewById(R.id.momentImage);
            imageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Add the fields and data to the moment if the login was made by using Parse

    protected ParseObject createMomentLogInParse(String momentText){

        ParseObject moment = new ParseObject(ParseConstants.CLASS_MOMENTS);
        moment.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
        moment.put(ParseConstants.KEY_MOMENT_TEXT, momentText);

        byte[] fileBytes = FileHelper.getByteArrayFromFile(this, mMediaUri);

        if (fileBytes == null){
            return null;
        }
        else{

            fileBytes = FileHelper.reduceImageForUpload(fileBytes);
            String fileName = FileHelper.getFileName(this, mMediaUri, ParseConstants.TYPE_IMAGE);
            ParseFile file = new ParseFile(fileName, fileBytes);

            moment.put(ParseConstants.KEY_FILE, file);
            return moment;
        }

    }

    //Save the moment in Parse
    protected void saveTheMomentInParse(ParseObject moment){

        moment.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null){
                        progressDialog.dismiss();
                        Intent intent = new Intent(MomentActivity.this, MainActivity.class);
                        intent.putExtra(AppConstants.NAME_ACTIVITY, AppConstants.MOMENT_ACTIVITY);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                }
                else{
                    AlertDialogGenerator dialog = new AlertDialogGenerator();
                    dialog.showAlertDialog(MomentActivity.this,
                            getString(R.string.error_moment_upload),
                            getString(R.string.error_title));
                }
            }
        });
    }

    //Trigger when the user cancels to save the moment
    @OnClick(R.id.cancelMomentButton) void cancelMoment(){
        finish();
        Intent intent = new Intent(MomentActivity.this, MainActivity.class);
        startActivity(intent);
    }

    //Trigger when user saves his moment
    @OnClick(R.id.saveMomentButton) void saveMoment(){

        MyAsyncTask myAsyncTask = new MyAsyncTask();
        myAsyncTask.execute();


    }

    class MyAsyncTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            String momentText = mMomentText.getText().toString().trim();
            if (mLoginChoice.equals(AppConstants.LOGIN_CHOICE_PARSE)) {
                //Create the moment
                ParseObject moment;
                //Empty moment: description not added
                if (!momentText.isEmpty()) {
                    moment = createMomentLogInParse(momentText);
                } else {
                    moment = createMomentLogInParse(ParseConstants.MOMENT_TEXT_EMPTY);
                }
                if (moment == null) {
                    //error
                    AlertDialogGenerator dialog = new AlertDialogGenerator();
                    dialog.showAlertDialog(MomentActivity.this,
                            getString(R.string.error_moment_upload),
                            getString(R.string.error_title));
                } else {
                    saveTheMomentInParse(moment);
                }
            }
            else if(mLoginChoice.equals(AppConstants.LOGIN_CHOICE_FACEBOOK)){
                Log.d(TAG, "Login with Facebook.");
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(MomentActivity.this,
                    "",
                    "Loading...");

            progressDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //progressDialog.dismiss();
        }

    }

}
