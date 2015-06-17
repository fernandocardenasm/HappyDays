package com.example.android.happydays;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import java.io.IOException;


public class MomentActivity extends ActionBarActivity {

    public final static String TAG = MomentActivity.class.getSimpleName();

    protected Uri mMediaUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moment);

        mMediaUri = getIntent().getData();
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

    public void showImageInActivity(Uri uri){
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            ImageView imageView = (ImageView) findViewById(R.id.momentImage);
            imageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
