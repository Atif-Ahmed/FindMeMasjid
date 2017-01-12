package com.apps.genutek.find_me_masjid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        // create or load shared preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        int count = preferences.getInt("run_count",-100);

        //application is running for first time....
        if(count == -100){
            count = 1;
            editor.putInt("run_count",count);
            editor.apply();
        }
        else{
            count = count +1;
            editor.putInt("run_count",count);
            editor.apply();
        }

        Log.d("app_run_counter", "App Run: " + count);
        Thread splashThread = new Thread() {

            @Override
            public void run() {
                try {
                    sleep(1500);
                    Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                    startActivity(intent);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        splashThread.start();
    }
}
