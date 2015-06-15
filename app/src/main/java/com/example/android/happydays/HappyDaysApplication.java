package com.example.android.happydays;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by usuario on 15/06/2015.
 */
public class HappyDaysApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "nWcdMMKvK8sHyUjSOHQWdmOsM9XEy4ppq5z4pmxJ", "hdX3ZRfvQjySV19luUYF3MruTKWNLJLyrhdu3VQK");

        /*ParseObject testObject = new ParseObject("TestObject");
        testObject.put("foo", "bar");
        testObject.saveInBackground();
        */
    }
}
