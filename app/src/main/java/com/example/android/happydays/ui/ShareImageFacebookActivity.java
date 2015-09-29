package com.example.android.happydays.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import com.example.android.happydays.AppConstants;
import com.example.android.happydays.ParseConstants;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;

public class ShareImageFacebookActivity extends ActionBarActivity {

    CallbackManager callbackManager;
    ShareDialog shareDialog;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);

        final Intent intent = getIntent();
        final String momentText = intent.getStringExtra(ParseConstants.KEY_MOMENT_TEXT);
        final String mObjectId = intent.getStringExtra(ParseConstants.KEY_OBJECT_ID);
        final Bitmap bitmap = (Bitmap) intent.getParcelableExtra(ParseConstants.BITMAP_IMAGE_MOMENT);

        // this part is optional
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {


            @Override
            public void onSuccess(Sharer.Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ShareImageFacebookActivity.this, "You shared your moment on FB!", Toast.LENGTH_SHORT).show();
                    }
                });
                Intent mIntent = new Intent(ShareImageFacebookActivity.this, MainActivity.class);
                startActivity(mIntent);
            }

            @Override
            public void onCancel() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(ShareImageFacebookActivity.this, "Cancel upload on FB!", Toast.LENGTH_SHORT).show();
                    }
                });
                Intent mIntent = new Intent(ShareImageFacebookActivity.this, DetailActivity.class);
                mIntent.putExtra(ParseConstants.KEY_MOMENT_TEXT, momentText);
                mIntent.putExtra(ParseConstants.BITMAP_IMAGE_MOMENT, bitmap);
                mIntent.putExtra(AppConstants.LOGIN_CHOICE, AppConstants.LOGIN_CHOICE_PARSE);
                mIntent.putExtra(ParseConstants.KEY_OBJECT_ID, mObjectId);
                startActivity(mIntent);
            }

            @Override
            public void onError(FacebookException error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ShareImageFacebookActivity.this, "There was an error and the moment was not upload on FB. Please try again!", Toast.LENGTH_SHORT).show();
                    }
                });
                Intent mIntent = new Intent(ShareImageFacebookActivity.this, DetailActivity.class);
                startActivity(mIntent);
            }
        });

        if (ShareDialog.canShow(SharePhotoContent.class)) {
            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build();
            SharePhotoContent content = new SharePhotoContent.Builder()
                    .addPhoto(photo)
                    .build();

            shareDialog.show(content);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}
