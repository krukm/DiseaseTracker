package com.matthewkruk.diseasetracker;

import android.app.Application;

import com.firebase.client.Firebase;

public class DiseaseTrackerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Firebase.setAndroidContext(this);
    }
}
