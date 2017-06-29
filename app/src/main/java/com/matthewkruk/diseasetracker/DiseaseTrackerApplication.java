package com.matthewkruk.diseasetracker;

import android.app.Application;

import com.parse.Parse;

public class DiseaseTrackerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "QbkENlFXQQdmvfdrF3SjOSItXZAxWr1ykqlpks35", "bl3nVJS1EbdmIOzI79rYoecZ4Cc1MOxANC1V9MpF");
    }
}
