package com.example.android.happydays;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.happydays.ui.MainActivity;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MomentActivity extends ActionBarActivity {

    public final static String TAG = MomentActivity.class.getSimpleName();

    protected Uri mMediaUri;
    protected String mLoginChoice;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int PICK_PHOTO_REQUEST = 2;
    public static final int MEDIA_TYPE_IMAGE = 3;

    ProgressDialog progressDialog;

    @InjectView(R.id.momentText) EditText mMomentText;
    @InjectView(R.id.momentImage) ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moment);
        ButterKnife.inject(this);

        Intent intent = getIntent();
        mLoginChoice = intent.getStringExtra(AppConstants.LOGIN_CHOICE);

        //To choose the image either from gallery or by taking a picture
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MomentActivity.this);
                builder.setMessage("Do you want to take a new picture or choose one from the Gallery?")
                        .setTitle("Select Method")
                        .setPositiveButton("Take new one", dialogTakePicture)
                        .setNegativeButton("Cancel", dialogCancel)
                        .setNeutralButton("Gallery", dialogGetFromGallery);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        //Remove focus on any button or text on the view
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    //Dialogs buttons listeners
    DialogInterface.OnClickListener dialogTakePicture = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Log.d(TAG,"OK");
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
                    Toast.makeText(this, "The action was not finished successfully. Please try it again.", Toast.LENGTH_LONG ).show();
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
//        Intent intent = new Intent(MomentActivity.this, MainActivity.class);
//        startActivity(intent);
    }

    //Trigger when user saves his moment
    @OnClick(R.id.saveMomentButton) void saveMoment(){
        if (mLoginChoice.equals(AppConstants.LOGIN_CHOICE_PARSE)){
            if (mMediaUri != null){
                MyAsyncTask myAsyncTask = new MyAsyncTask();
                myAsyncTask.execute();
            }
            else{
                AlertDialogGenerator dialog = new AlertDialogGenerator();
                dialog.showAlertDialog(MomentActivity.this,
                        getString(R.string.upload_image_info_text),
                        getString(R.string.error_title));
            }

        }


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
