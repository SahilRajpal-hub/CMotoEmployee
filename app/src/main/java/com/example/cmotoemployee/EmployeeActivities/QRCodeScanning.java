package com.example.cmotoemployee.EmployeeActivities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cmotoemployee.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.ResultPoint;
import com.google.zxing.qrcode.QRCodeReader;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;
import com.journeyapps.barcodescanner.camera.CameraSettings;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class QRCodeScanning extends AppCompatActivity {
    private static final String TAG = "QRCodeScanning";

    //Firebase vars
    private FirebaseDatabase database;
    private DatabaseReference reference;
    //qr code vars
    private CameraSettings settings;
    private CompoundBarcodeView barcodeView;

    private ImageView exit;
    private TextView textTryAgain,scanAnotherCar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_q_r_code_scanning);
        barcodeView = findViewById(R.id.barcodeScanner);
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();

        exit = findViewById(R.id.exit);
        textTryAgain = findViewById(R.id.text2);
        scanAnotherCar = findViewById(R.id.scanAnotherCar);

        textTryAgain.setPaintFlags(textTryAgain.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        settings = new CameraSettings();
        settings.setRequestedCameraId(0);
        barcodeView.getBarcodeView().setCameraSettings(settings);

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        scanAnotherCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(QRCodeScanning.this,HomeActivity.class));
                finish();
            }
        });



    }

    private void setBarcodeView(){

        barcodeView.resume();

        barcodeView.decodeSingle(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                Log.d(TAG, "barcodeResult: barcode result : " + result.toString());

                try {
                    final String id = new String(result.toString());
                    Log.d(TAG, "onActivityResult: scanner got " + id);
                    long timeStamp = System.currentTimeMillis();

                    if(getIntent().hasExtra(getString(R.string.carNumber))) {
                        if (id.equals(getIntent().getStringExtra(getString(R.string.carNumber)))) {
                            Toast.makeText(QRCodeScanning.this, "Car Verified", Toast.LENGTH_SHORT).show();
                            reference.child("Car Status").child(id).child("status").setValue("scanned");
                            reference.child("Car Status").child(id).child("timeStamp").setValue(String.valueOf(timeStamp));
                            FirebaseDatabase.getInstance().getReference().child("Employee").child(FirebaseAuth.getInstance().getUid()).child("status").setValue("working");
                            FirebaseDatabase.getInstance().getReference().child("Employee").child(FirebaseAuth.getInstance().getUid()).child("working on").setValue(getIntent().getStringExtra(getString(R.string.carNumber)));
                            FirebaseDatabase.getInstance().getReference().child("Employees").child(getIntent().getStringExtra(getString(R.string.area))).child(FirebaseAuth.getInstance().getUid()).child("working on").setValue(getIntent().getStringExtra(getString(R.string.carNumber)));
                            Intent returnIntent = new Intent();
                            setResult(Activity.RESULT_OK, returnIntent);
                            finish();

                        } else {
                            Log.d(TAG, "barcodeResult: scanner got " + result.toString() + " instead of " + getIntent().getStringExtra(getString(R.string.carNumber)));
                            Toast.makeText(QRCodeScanning.this, "QRCode didn't match ", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        if(getIntent().hasExtra("carsNumberArray")){
                            Bundle bundle = getIntent().getExtras();
                            ArrayList<CharSequence> carsNumbers = bundle.getCharSequenceArrayList("carsNumberArray");
                            String areas = (String) bundle.getCharSequence("Areas");
                            if(carsNumbers.contains(id)){
                                Toast.makeText(QRCodeScanning.this, "Car Verified", Toast.LENGTH_SHORT).show();
                                reference.child("Car Status").child(id).child("status").setValue("scanned");
                                reference.child("Car Status").child(id).child("timeStamp").setValue(String.valueOf(timeStamp));
                                FirebaseDatabase.getInstance().getReference().child("Employee").child(FirebaseAuth.getInstance().getUid()).child("status").setValue("working");
                                FirebaseDatabase.getInstance().getReference().child("Employee").child(FirebaseAuth.getInstance().getUid()).child("working on").setValue(id);
                                FirebaseDatabase.getInstance().getReference().child("Employees").child(areas).child(FirebaseAuth.getInstance().getUid()).child("working on").setValue(id);
                                Intent returnIntent = new Intent(QRCodeScanning.this,StartCarCleaningActivity.class);
                                returnIntent.putExtra(getString(R.string.Area),areas);
                                startActivity(returnIntent);
                                finish();
                            }else {
                                Toast.makeText(QRCodeScanning.this, "You don't have this car to clean", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }



                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setBarcodeView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        setBarcodeView();
    }
}