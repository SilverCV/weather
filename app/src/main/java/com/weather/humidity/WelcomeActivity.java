package com.weather.humidity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;


public class WelcomeActivity extends Activity {
    private static final int DELAY = 3000;// 延时3秒


    protected void  onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.welcome_activity);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(WelcomeActivity.this,MainActivity.class);
               WelcomeActivity.this.startActivity(intent);
                WelcomeActivity.this.finish();
            }
        },DELAY);
    }

}

