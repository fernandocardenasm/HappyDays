package com.example.android.happydays.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.android.happydays.AlertDialogGenerator;
import com.example.android.happydays.AppConstants;
import com.example.android.happydays.ParseConstants;
import com.example.android.happydays.R;
import com.example.android.happydays.tools.FileHelper;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class DetailActivity extends ActionBarActivity {

    public final static String TAG = DetailActivity.class.getSimpleName();

    protected EditText mMomentText;
    protected ImageView mImageView;
    protected Button mCancelButton;
    protected Button mSaveButton;
    protected ProgressBar mProgressBar;
    protected String mLoginChoice;
    protected String mObjectId;
    ProgressDialog progressDialog;

    protected Uri mMediaUri;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int PICK_PHOTO_REQUEST = 2;
    public static final int MEDIA_TYPE_IMAGE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMomentText = (EditText) findViewById(R.id.momentText);
        mImageView = (ImageView) findViewById(R.id.momentImage);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mProgressBar.setVisibility(View.INVISIBLE);

        mSaveButton = (Button) findViewById(R.id.saveMomentButton);

        mCancelButton = (Button) findViewById(R.id.cancelMomentButton);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Receive Extras from Intent
        Intent intent = getIntent();
        mLoginChoice = intent.getStringExtra(AppConstants.LOGIN_CHOICE);
//        Uri imageUri = intent.getData();
        final String momentText = intent.getStringExtra(ParseConstants.KEY_MOMENT_TEXT);
        mObjectId = intent.getStringExtra(ParseConstants.KEY_OBJECT_ID);

        //If mMediaUri is null, the user didnt upload a new picture
        mMediaUri = null;

        //Load the moment values in the form
        fillValueForm(momentText);
        Bitmap bitmap = (Bitmap) intent.getParcelableExtra("Bitmap");
        mImageView.setImageBitmap(bitmap);

        //Save moment
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLoginChoice.equals(AppConstants.LOGIN_CHOICE_PARSE)) {
                    if (mMediaUri != null) {
                        MyAsyncTask myAsyncTask = new MyAsyncTask();
                        myAsyncTask.execute();
                    } else {
                        if (mMomentText.getText().toString().trim().isEmpty()){
                            if (momentText.isEmpty()){
                                //Dont do anything and come back to MainActivity
                            }
                            else{
                                mSaveButton.setEnabled(false);
                                updateMomentLogInParse(ParseConstants.MOMENT_TEXT_EMPTY, mObjectId);
                            }
                        }
                        else{
                            mSaveButton.setEnabled(false);
                            updateMomentLogInParse(mMomentText.getText().toString(), mObjectId);
                        }
                    }

                }
            }
        });

        //To choose the image either from gallery or by taking a picture
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DetailActivity.this);
                builder.setMessage("Do you want to take a new picture or choose one from the Gallery?")
                        .setTitle("Select Method")
                        .setPositiveButton("Take new one", dialogTakePicture)
                        .setNegativeButton("Cancel", dialogCancel)
                        .setNeutralButton("Gallery", dialogGetFromGallery);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });


        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share) {
            return true;
        }
        else if (id == R.id.action_delete){
            AlertDialog.Builder builder = new AlertDialog.Builder(DetailActivity.this);
            builder.setMessage("Are you sure you want to delete the moment?")
                    .setTitle("Warning!")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteMomentParse(mObjectId);
                        }
                    }).setNegativeButton("Cancel",null);
            AlertDialog dialog = builder.create();
            dialog.show();

        }

        return super.onOptionsItemSelected(item);
    }

    //Dialogs buttons listeners
    DialogInterface.OnClickListener dialogTakePicture = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Log.d(TAG, "OK");
            if (mLoginChoice.equals(AppConstants.LOGIN_CHOICE_PARSE)){
                dispatchTakePictureIntent();
            }
        }
    };

    DialogInterface.OnClickListener dialogGetFromGallery = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Log.d(TAG,"From Gallery");
            if (mLoginChoice.equals(AppConstants.LOGIN_CHOICE_PARSE)){
                addImageFromGallery();
            }
        }
    };

    DialogInterface.OnClickListener dialogCancel = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Log.d(TAG,"Cancel");
        }
    };


    public void fillValueForm(String momentText){
        if (!Objects.equals(momentText, "empty")){
            mMomentText.setText(momentText);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
                //mImageView.setImageBitmap(imageBitmap);
                //Add the image to the gallery
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(mMediaUri);
                sendBroadcast(mediaScanIntent);

                showImageInActivity(mMediaUri);

                //Send the image to the MomentActivity
//            Intent momentIntent = new Intent(this, MomentActivity.class);
//            momentIntent.setData(mMediaUri);
//            momentIntent.putExtra(AppConstants.LOGIN_CHOICE, mLoginChoice);
//            startActivity(momentIntent);
            }
            else if (requestCode == PICK_PHOTO_REQUEST){
                if (data == null){
                    Toast.makeText(this, "The action was not finished successfully. Please try it again.", Toast.LENGTH_LONG).show();
                }
                else{
                    mMediaUri = data.getData();
                    showImageInActivity(mMediaUri);
                }
            }
        }
    }

    //Show the user's taken image in the layout
    public void showImageInActivity(Uri uri){
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            mImageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Open the user's gallery
    public void addImageFromGallery(){
        Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
        choosePhotoIntent.setType("image/*");
        startActivityForResult(choosePhotoIntent, PICK_PHOTO_REQUEST);
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

    protected void updateMomentLogInParse(final String momentText, String objectId) {

        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.CLASS_MOMENTS);
        query.whereEqualTo(ParseConstants.KEY_OBJECT_ID, objectId);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null){
                    ParseObject moment = list.get(0);
                    moment.put(ParseConstants.KEY_MOMENT_TEXT, momentText);
                    moment.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e==null){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(DetailActivity.this, "Moment updated successfully!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            else{
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(DetailActivity.this, "It was possible to update the happy moment. Please try it again.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });
                }
                else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DetailActivity.this, "It was possible to update the happy moment. Please try it again.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                mSaveButton.setEnabled(true);
                Intent intent = new Intent(DetailActivity.this, MainActivity.class);
                intent.putExtra(AppConstants.NAME_ACTIVITY, AppConstants.DETAIL_ACTIVITY);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    //Save the moment in Parse
    protected void saveTheMomentInParse(ParseObject moment){

        moment.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null){
                    progressDialog.dismiss();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DetailActivity.this, "The moment was updated successfully!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    Intent intent = new Intent(DetailActivity.this, MainActivity.class);
                    intent.putExtra(AppConstants.NAME_ACTIVITY, AppConstants.MOMENT_ACTIVITY);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                else{
                    AlertDialogGenerator dialog = new AlertDialogGenerator();
                    dialog.showAlertDialog(DetailActivity.this,
                            getString(R.string.error_moment_upload),
                            getString(R.string.error_title));
                }
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            //Save Image
            mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
            if (mMediaUri == null){
                Toast.makeText(this, "There was an error accessing your device's external storage.", Toast.LENGTH_SHORT).show();
            }
            else{
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);

                //Get the result of the intent
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    //Get the Uri where it is going to be save on the mobile
    private Uri getOutputMediaFileUri(int mediaType) {
        //To be safe, you should check that the SDCard is mounted
        //using Environment.getExternalStorageState() before doing this

        if (isExternalStorageAvailable()){
            //get the URI

            //1. Get the external  storage directory

            String appName = this.getString(R.string.app_name);

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

    //Validate if it is allowed to save the image on the mobile
    private boolean isExternalStorageAvailable(){
        String state = Environment.getExternalStorageState();
        if ( state.equals(Environment.MEDIA_MOUNTED)){
            return true;
        }
        else{
            return false;
        }
    }

    //Delete the moment

    private void deleteMomentParse(String objectId){
        ParseObject.createWithoutData(ParseConstants.CLASS_MOMENTS, objectId).deleteEventually(new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                Intent intent = new Intent(DetailActivity.this, MainActivity.class);
                if (e==null){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DetailActivity.this, "Moment deleted successfully!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DetailActivity.this, "Unfortunately the moment could not be deleted. Please try again!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            }
        });
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
                    dialog.showAlertDialog(DetailActivity.this,
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

            progressDialog = ProgressDialog.show(DetailActivity.this,
                    "",
                    "Saving...");

            progressDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //progressDialog.dismiss();
        }

    }


}
