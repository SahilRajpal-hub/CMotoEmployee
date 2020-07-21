package com.example.cmotoemployee.EmployeeActivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.droidnet.DroidListener;
import com.droidnet.DroidNet;
import com.example.cmotoemployee.ErrorHandler.CrashHandler;
import com.example.cmotoemployee.Model.Car;
import com.example.cmotoemployee.Model.Employee;
import com.example.cmotoemployee.R;
import com.example.cmotoemployee.RoundedTransformation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.integration.android.IntentIntegrator;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class StartCarCleaningActivity extends AppCompatActivity implements DroidListener {
    private static final String TAG = "StartCarCleanActivity";

    //permission code
    private static final int VERIFY_PERMISSION_REQUEST_CODE = 1;
    private static final int SCANNED_ACTIVITY_RESULT = 29;

    //permissions for camera
    public final String[] CAMERA_PERMISSION = {Manifest.permission.CAMERA};
    public final String[] LOCATION_SERVICE_PERMISSION = {Manifest.permission.ACCESS_FINE_LOCATION};

    // vars
    private IntentIntegrator integrator;
    private double photoUploadProgress = 0;
    private String CarNumber = "";
    private String location;
    private String CarOwnerPhone;
    private String area;
    private int timerValue;
    private long lastClicked = 0;

    //internet listeners
    private boolean Connected = true;
    private DroidNet droidNet;

    //firebase vars
    private StorageReference storageReference;
    private FirebaseAuth auth;
    private DatabaseReference reference;

    //widgets
    private TextView carNumber, carCharacteristics,carLocation,Scanner,carColor,carNumberHeading,car_number;
    private ImageView carPhoto,back;
    private ImageView OpenMap,CallOwner;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_car_cleaning);
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(getApplicationContext()));
        Log.d(TAG, "onCreate: called");
        droidNet = DroidNet.getInstance();
        droidNet.addInternetConnectivityListener(this);
        Scanner = findViewById(R.id.scanner);
        back = findViewById(R.id.back);
        CarNumber = getIntent().getStringExtra(getString(R.string.carNumber));
        area = getIntent().getStringExtra(getString(R.string.Area));
        carNumber = findViewById(R.id.carNumber);
        carCharacteristics = findViewById(R.id.carModel);
        carLocation = findViewById(R.id.carLocation);
        carPhoto = findViewById(R.id.carPhoto);
        progressBar = findViewById(R.id.progressBar);
        OpenMap = findViewById(R.id.openMap);
        carColor = findViewById(R.id.carColor);
        car_number = findViewById(R.id.car_Number);
        CallOwner = findViewById(R.id.callOwner);
        carNumberHeading = findViewById(R.id.carNumber);

        if(ActivityCompat.checkSelfPermission(StartCarCleaningActivity.this,CAMERA_PERMISSION[0]) != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "onCreate: permission asking , if not available previously");
            ActivityCompat.requestPermissions(StartCarCleaningActivity.this,CAMERA_PERMISSION, VERIFY_PERMISSION_REQUEST_CODE);
        }
        if(ActivityCompat.checkSelfPermission(StartCarCleaningActivity.this,LOCATION_SERVICE_PERMISSION[0]) != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "onCreate: permission asking , if not available previously");
            ActivityCompat.requestPermissions(StartCarCleaningActivity.this,LOCATION_SERVICE_PERMISSION, VERIFY_PERMISSION_REQUEST_CODE);
        }


        if(!Connected){
            Toast.makeText(this, "No INTERNET connection found. Check your connection and try again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(StartCarCleaningActivity.this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }



        reference = FirebaseDatabase.getInstance().getReference().child("cars").child(area);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: creating car object with snapshot : " + dataSnapshot);
                try {
                    if (dataSnapshot.hasChild(CarNumber)) {
                        Log.d(TAG, "onDataChange: got the snapshot : " + dataSnapshot);
                        Car car = dataSnapshot.child(CarNumber).getValue(Car.class);
                        location = car.getLocation();
                        CarOwnerPhone = car.getMobileNo();
                        carNumber.setText(car.getNumber());
                        carNumberHeading.setText(car.getNumber());
                        CarNumber = car.getNumber();
                        car_number.setText(car.getNumber());
                        carColor.setText(car.getColor());
                        carCharacteristics.setText(car.getModel());
                        if (car.getCategory().toLowerCase().equals("hatchback") || car.getCategory().toLowerCase().equals("compactsedan")) {
                            timerValue = 1;
                        } else if (car.getCategory().toLowerCase().equals("sedan") || car.getCategory().toLowerCase().equals("luv")) {
                            timerValue = 1;
                        } else if (car.getCategory().toLowerCase().equals("suv")) {
                            timerValue = 1;
                        }
                        carLocation.setText(car.getAddress());
                        Picasso.get().load(car.getPhoto()).transform(new RoundedTransformation(30, 0)).memoryPolicy(MemoryPolicy.NO_CACHE).into(carPhoto);
                        progressBar.setVisibility(View.GONE);

                    }
                }catch (Exception e){
                    Log.d(TAG, "onDataChange: got error while setting car model" + e.getMessage());
                    Toast.makeText(StartCarCleaningActivity.this, "Error occurred. Unable to get Cars Data. Message :"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(StartCarCleaningActivity.this,HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, " snapshot not available : " + databaseError.getMessage());
                Toast.makeText(StartCarCleaningActivity.this, "Barcode cannot be verified", Toast.LENGTH_SHORT).show();
            }
        });


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Scanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!Connected){
                    Toast.makeText(StartCarCleaningActivity.this, "No INTERNET connection found. Check your connection and try again.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(SystemClock.elapsedRealtime() - lastClicked < 1000){
                    return;
                }
                lastClicked = SystemClock.elapsedRealtime();
                Log.d(TAG, "onClick: launching scanner after checking permissions");


                reference = FirebaseDatabase.getInstance().getReference().child("Car Status").child(carNumber.getText().toString()).child("status");
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue().toString().equals("scanned")){

                        }else{

                            if(ActivityCompat.checkSelfPermission(StartCarCleaningActivity.this,CAMERA_PERMISSION[0]) == PackageManager.PERMISSION_GRANTED){
                                Intent intent = new Intent(StartCarCleaningActivity.this, QRCodeScanning.class);
                                intent.putExtra(getString(R.string.carNumber),CarNumber);
                                intent.putExtra(getString(R.string.area),area);
                                Toast.makeText(StartCarCleaningActivity.this, "Launching Scanner", Toast.LENGTH_SHORT).show();
                                startActivityForResult(intent,SCANNED_ACTIVITY_RESULT);
                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }
        });



        CallOwner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime() - lastClicked < 1000){
                    return;
                }
                lastClicked = SystemClock.elapsedRealtime();
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + CarOwnerPhone));
                Toast.makeText(StartCarCleaningActivity.this, "Calling Owner", Toast.LENGTH_SHORT).show();
                startActivity(callIntent);
            }
        });

        OpenMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!Connected){
                    Toast.makeText(StartCarCleaningActivity.this, "No INTERNET connection found. Check your connection and try again.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(SystemClock.elapsedRealtime() - lastClicked < 1000){
                    return;
                }
                lastClicked = SystemClock.elapsedRealtime();
                Intent map = new Intent(StartCarCleaningActivity.this, MapsActivity.class);
                Double latitude = Double.valueOf(location.substring(0,location.indexOf(",")));
                Double longitude = Double.valueOf(location.substring(location.indexOf(",")+1));
                map.putExtra(getString(R.string.latitude),latitude);
                map.putExtra(getString(R.string.longitude),longitude);
                startActivity(map);
            }
        });

        setTimer();

    }



    public void setTimer(){
        reference = FirebaseDatabase.getInstance().getReference().child("Car Status")
                .child(CarNumber);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot != null) {
                        long currentTimeStamp = System.currentTimeMillis();
                        Double timeStamp = Double.valueOf(dataSnapshot.child("timeStamp").getValue().toString());
                        if((currentTimeStamp - timeStamp)  < timerValue*60000){
                            Log.d(TAG, "onDataChange: Timer is not complete timeStamp : " + (currentTimeStamp - timeStamp) / 60000);
                            Log.d(TAG, "onDataChange: difference is " + (timerValue - (currentTimeStamp - timeStamp) / 60000));
                            float timeLeftInMinutes = (float) (timerValue - (currentTimeStamp - timeStamp) / 60000);
                            Intent intent = new Intent(StartCarCleaningActivity.this,timerActivity.class);
                            intent.putExtra(getString(R.string.carNumber),CarNumber);
                            intent.putExtra(getString(R.string.area),area);
                            intent.putExtra("timeInMinutes",timeLeftInMinutes);
                            intent.putExtra("finalTimeInMinutes",timerValue);
                            startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                            finish();
                        }else{
                            if(timeStamp != 0){
                                Intent intent = new Intent(StartCarCleaningActivity.this,UploadImagesActivity.class);
                                intent.putExtra("carNumber",carNumber.getText().toString());
                                intent.putExtra("area",area);
                                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                                finish();
                            }
                        }

                    }
                }catch (Error e){
                    Log.d(TAG, "onDataChange: got the error while setting timer : " + e.getMessage());
                    Toast.makeText(StartCarCleaningActivity.this, "Got the timer connection error. Contact Your Manager", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        droidNet.removeInternetConnectivityChangeListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: ");
        Calendar calendar = Calendar.getInstance();
        auth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference()
                .child("cars/" + area + "/" + CarNumber + "/" + calendar.getTime());

        if (data != null) {

            if(requestCode == SCANNED_ACTIVITY_RESULT){
                Log.d(TAG, "onActivityResult: qr code is scanned");
                setTimer();
            }
        }
    }

    private byte[] getByteFromBitmap(Bitmap bitmap, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,quality,stream);
        return stream.toByteArray();
    }


    @Override
    public void onInternetConnectivityChanged(boolean isConnected) {
        if(isConnected){
            Log.d(TAG, "onInternetConnectivityChanged: INTERNET connected");
            Connected = true;
        }
        else{
            Log.d(TAG, "onInternetConnectivityChanged: INTERNET lost");
            Connected = false;
            Toast.makeText(this, "Internet Connection Lost", Toast.LENGTH_SHORT).show();
        }
    }
}