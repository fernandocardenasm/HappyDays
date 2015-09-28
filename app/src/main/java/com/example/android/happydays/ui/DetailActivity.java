package com.example.android.happydays.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.android.happydays.ParseConstants;
import com.example.android.happydays.R;

public class DetailActivity extends ActionBarActivity {

    protected EditText mMomentText;
    protected ImageView mImageView;
    protected Button mCancelButton;
    protected ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMomentText = (EditText) findViewById(R.id.momentText);
        mImageView = (ImageView) findViewById(R.id.momentImage);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mProgressBar.setVisibility(View.INVISIBLE);

        mCancelButton = (Button) findViewById(R.id.cancelMomentButton);

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent = getIntent();
//        Uri imageUri = intent.getData();

        fillValueForm(intent.getStringExtra(ParseConstants.KEY_MOMENT_TEXT));
        Bitmap bitmap = (Bitmap) intent.getParcelableExtra("Bitmap");
        mImageView.setImageBitmap(bitmap);
//        Picasso.with(this).load(imageUri.toString()).into(mImageView, new Callback() {
//            @Override
//            public void onSuccess() {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mProgressBar.setVisibility(View.INVISIBLE);
//                    }
//                });
//            }
//
//            @Override
//            public void onError() {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mProgressBar.setVisibility(View.INVISIBLE);
//                    }
//                });
//            }
//        });

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public void fillValueForm(String momentText){
        mMomentText.setText(momentText);
    }


}
