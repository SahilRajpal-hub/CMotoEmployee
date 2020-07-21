package com.example.cmotoemployee.ErrorHandler;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.cmotoemployee.Authentication.StartActivity;
import com.example.cmotoemployee.EmployeeActivities.HomeActivity;
import com.example.cmotoemployee.R;

public class ExceptionDisplay extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exception_display);

        TextView exception_text = (TextView) findViewById(R.id.exception_text);
        exception_text.setMovementMethod(new ScrollingMovementMethod());
        Button btnBack = (Button) findViewById(R.id.btnBack);
        exception_text.setText(getIntent().getExtras().getString("error"));

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intentData();
            }
        });
    }

    @Override
    public void onBackPressed() {
        intentData();
    }

    public void intentData() {

        Log.d("CDA", "onBackPressed Called");
        Intent setIntent = new Intent(ExceptionDisplay.this, StartActivity.class);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
    }
}