package com.breejemodi.exxamdesk;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;

import com.breejemodi.exxamdesk.registration.RegisterActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SystemClock.sleep(2000);

//        if(FirebaseAuth.getInstance().getCurrentUser() != null){
//            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
//            startActivity(intent);
//            finish();
//            return;
//        }

        Intent intent = new Intent(SplashActivity.this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }
}