package com.example.cmotoemployee.EmployeeActivities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cmotoemployee.R;

public class ImageUploadedActivity extends AppCompatActivity {

    private Button goToList;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_uploaded);
        goToList = findViewById(R.id.gotToCarList);

        goToList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ImageUploadedActivity.this,HomeActivity.class));
                finish();
            }
        });
    }
}
