package com.example.cmotoemployee.EmployeeActivities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cmotoemployee.R;

public class timerActivity extends AppCompatActivity {

    private CountDownTimer countDownTimer;
    private ProgressBar barTimer;
    private TextView textTimer;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        barTimer = findViewById(R.id.barTimer);
        textTimer = findViewById(R.id.textTimer);
        Intent intent = getIntent();
        if(intent.hasExtra("timeInMinutes")){
            startTimer(intent.getFloatExtra("timeInMinutes",0),intent.getFloatExtra("finalTimeInMinutes",0));
        }
    }

    private void startTimer(final float minuti, final float totalMin) {
        countDownTimer = new CountDownTimer((long) (60 * minuti * 1000), 500) {
            float progress = 0;
            // 500 means, onTick function will be called at every 500 milliseconds

            @Override
            public void onTick(long leftTimeInMilliseconds) {
                long seconds = leftTimeInMilliseconds / 1000;
                progress = (((float)seconds/(minuti*60))*100);
                barTimer.setProgress((int)progress);
                textTimer.setText(String.format("%02d", seconds/60) + ":" + String.format("%02d", seconds%60));
                // format the textview to show the easily readable format

            }
            @Override
            public void onFinish() {
                textTimer.setText("00:00");
                barTimer.setProgress(100);
                Intent intent = new Intent(timerActivity.this,UploadImagesActivity.class);
                intent.putExtra("carNumber",getIntent().getStringExtra("carNumber"));
                intent.putExtra("area",getIntent().getStringExtra("area"));
                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        }.start();

    }
}
