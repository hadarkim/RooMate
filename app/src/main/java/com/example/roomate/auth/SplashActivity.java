package com.example.roomate.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.ComponentActivity;
import androidx.core.splashscreen.SplashScreen;

import com.example.roomate.ui.main.MainActivity;

public class SplashActivity extends ComponentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // התקנת ה־SplashScreen API
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // מעבר אוטומטי ל־MainActivity
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
