package com.yorbax.EMA;

import android.app.Application;
import android.content.ContextWrapper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.pixplicity.easyprefs.library.Prefs;

import static com.yorbax.EMA.Constants.Constants.mAuth;
import static com.yorbax.EMA.Constants.Constants.mDatabase;

public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(false)
                .build();
    }
}
