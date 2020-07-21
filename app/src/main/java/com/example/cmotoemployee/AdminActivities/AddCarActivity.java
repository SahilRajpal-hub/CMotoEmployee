package com.example.cmotoemployee.AdminActivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cmotoemployee.EmployeeActivities.MapsActivity;
import com.example.cmotoemployee.Model.Car;
import com.example.cmotoemployee.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;

public class AddCarActivity extends AppCompatActivity {
    private static final String TAG = "AddCarActivity";

    //permission code
    private static final int VERIFY_PERMISSION_REQUEST_CODE = 1;
    private static final int CAMERA_REQUEST_CODE = 2;

    //permissions for camera
    public final String[] CAMERA_PERMISSION = {Manifest.permission.CAMERA};
    public final String[] LOCATION_SERVICE_PERMISSION = {Manifest.permission.ACCESS_FINE_LOCATION};


    // vars
    private double photoUploadProgress = 0;
    private String carsPhotoUrl;
    private LatLng latLng;
    private String Society,Area;


    // Firebase Vars
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private StorageReference storageReference;

    // Widgets
    private AppCompatButton addCar,addLocation,addPhoto;
    private EditText carsNumber,carsModel,carsName,carsCategory,carsColor,carsOwnerMobile,carsAddress,area,society;
    private ProgressBar progressBar;
    private TextView pleaseWait;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_car);
        Log.d(TAG, "onCreate: called for adding car");
        addCar = findViewById(R.id.addCar);
        progressBar = findViewById(R.id.progressBar);
        pleaseWait = findViewById(R.id.pleaseWait);
        addPhoto = findViewById(R.id.addPhoto);
        carsNumber = findViewById(R.id.carsNumber);
        carsAddress = findViewById(R.id.carsAddress);
        carsCategory = findViewById(R.id.carsCategory);
        carsColor = findViewById(R.id.carsColor);
        carsModel = findViewById(R.id.carsModel);
        carsName = findViewById(R.id.carsName);
        carsOwnerMobile = findViewById(R.id.carsOwnerMobile);
        area = findViewById(R.id.Area);
        society = findViewById(R.id.Society);

        progressBar.setVisibility(View.GONE);
        pleaseWait.setVisibility(View.GONE);

        if(ActivityCompat.checkSelfPermission(AddCarActivity.this,CAMERA_PERMISSION[0]) != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "onCreate: permission asking , if not available previously");
            ActivityCompat.requestPermissions(AddCarActivity.this,CAMERA_PERMISSION, VERIFY_PERMISSION_REQUEST_CODE);
        }
        if(ActivityCompat.checkSelfPermission(AddCarActivity.this,LOCATION_SERVICE_PERMISSION[0]) != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "onCreate: permission asking , if not available previously");
            ActivityCompat.requestPermissions(AddCarActivity.this,LOCATION_SERVICE_PERMISSION, VERIFY_PERMISSION_REQUEST_CODE);
        }

        if(getIntent().hasExtra(getString(R.string.latitude)) && getIntent().hasExtra(getString(R.string.longitude))){
            latLng = new LatLng(getIntent().getDoubleExtra(getString(R.string.latitude),00), getIntent().getDoubleExtra(getString(R.string.latitude),00));
        }




        addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(carsNumber.getText().toString().equals("")){
                    Toast.makeText(AddCarActivity.this, "First Add Car Number", Toast.LENGTH_SHORT).show();
                    return;
                }else if(area.getText().toString().equals("")){
                    Toast.makeText(AddCarActivity.this, "First Add Area", Toast.LENGTH_SHORT).show();
                    return;
                }else if(society.getText().toString().equals("")){
                    Toast.makeText(AddCarActivity.this, "First Add Society", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(ActivityCompat.checkSelfPermission(AddCarActivity.this,CAMERA_PERMISSION[0]) == PackageManager.PERMISSION_GRANTED){
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent,CAMERA_REQUEST_CODE);
                }
            }
        });

        addCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Car car = new Car();
                car.setAddress(carsAddress.getText().toString());
                car.setCategory(carsCategory.getText().toString());
                car.setColor(carsColor.getText().toString());
                car.setMobileNo(carsOwnerMobile.getText().toString());
                car.setModel(carsModel.getText().toString());
                car.setName(carsName.getText().toString());
                car.setPhoto(carsPhotoUrl);
                car.setCategory(carsCategory.getText().toString());
                car.setNumber(carsNumber.getText().toString());
                Society = society.getText().toString();
                Area = area.getText().toString();

                if((TextUtils.isEmpty(Society)) || (TextUtils.isEmpty(Area))){
                    Toast.makeText(AddCarActivity.this, "Society and Area can't be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                reference = FirebaseDatabase.getInstance().getReference().child("cars").child(Area).child(Society).child(car.getNumber());
                reference.setValue(car);
                FirebaseDatabase.getInstance().getReference().child("Car Status").child(car.getNumber()).child("status").setValue("waited");
                FirebaseDatabase.getInstance().getReference().child("Car Status").child(car.getNumber()).child("timeStamp").setValue("0");


                Intent map = new Intent(AddCarActivity.this, MapsActivity.class);
                map.putExtra(getString(R.string.getLocation),"getLocation");
                map.putExtra("CarNumber",car.getNumber());
                map.putExtra(getString(R.string.Area),Area);
                map.putExtra(getString(R.string.Society),Society);
                startActivity(map);
                finish();

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: ");
        Calendar calendar = Calendar.getInstance();
        auth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference()
                .child("cars/" + carsNumber.getText().toString() + "/photo " + calendar.getTime());

        if (data != null) {

            if (requestCode == CAMERA_REQUEST_CODE) {
                Log.d(TAG, "onActivityResult: request code is for camera");
                progressBar.setVisibility(View.VISIBLE);
                pleaseWait.setVisibility(View.VISIBLE);

                Bitmap bitmap = (Bitmap) data.getExtras().get("data");

                byte[] bytes = getByteFromBitmap(bitmap, 80);
                UploadTask uploadTask = null;
                uploadTask = storageReference.putBytes(bytes);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                carsPhotoUrl = String.valueOf(uri);
                                Toast.makeText(AddCarActivity.this, "image upload successful", Toast.LENGTH_SHORT).show();

                            }
                        });
                        progressBar.setVisibility(View.GONE);
                        pleaseWait.setVisibility(View.GONE);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed upload");
                        Toast.makeText(AddCarActivity.this, "photo uploading failed" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        pleaseWait.setVisibility(View.GONE);

                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100 * taskSnapshot.getBytesTransferred()) / (taskSnapshot.getTotalByteCount());

                        if(progress - 15 > photoUploadProgress){
                            Toast.makeText(AddCarActivity.this, "uploading : " + progress + " %", Toast.LENGTH_SHORT).show();
                            photoUploadProgress =  progress;
                        }
                        Log.d(TAG, "onProgress: upload progress" + progress + "% done");
                    }
                });

            }
        }
    }

    private byte[] getByteFromBitmap(Bitmap bitmap, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,quality,stream);
        return stream.toByteArray();
    }
}