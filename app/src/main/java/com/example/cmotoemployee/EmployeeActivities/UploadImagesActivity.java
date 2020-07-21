package com.example.cmotoemployee.EmployeeActivities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.droidnet.DroidListener;
import com.droidnet.DroidNet;
import com.example.cmotoemployee.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class UploadImagesActivity extends AppCompatActivity{
    private static final String TAG = "UploadImagesActivity";

    //permission code
    private static final int VERIFY_PERMISSION_REQUEST_CODE = 1;
    private static final int CAMERA_REQUEST_CODE = 2;
    private static final int SCANNED_ACTIVITY_RESULT = 29;


    //widgets
    private ImageView uploadImage1;
    private ImageView uploadImage2;
    private ProgressBar progressBar;
    private ImageView submit;

    //permissions for camera
    public final String[] CAMERA_PERMISSION = {Manifest.permission.CAMERA};
    public final String[] LOCATION_SERVICE_PERMISSION = {Manifest.permission.ACCESS_FINE_LOCATION};


    //vars
    private long lastClicked = 0;
    private int timerValue;
    private int photoUploadProgress = 0;
    private String carNumber;
    private String area;
    private boolean image1Uploaded = false;
    private boolean image2Uploaded = false;
    private int n=0;



    //firebase vars
    private StorageReference storageReference;
    private FirebaseAuth auth;
    private DatabaseReference reference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_images);
        uploadImage1 = findViewById(R.id.uploadImage1);
        uploadImage2 = findViewById(R.id.uploadImage2);
        progressBar = findViewById(R.id.progressBar);
        submit = findViewById(R.id.submit);
        progressBar.setVisibility(View.GONE);
        carNumber = getIntent().getStringExtra("carNumber");
        area = getIntent().getStringExtra("area");

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(n!=2){
                    Toast.makeText(UploadImagesActivity.this, "Upload both the images", Toast.LENGTH_SHORT).show();
                    return;
                }
                startActivity(new Intent(UploadImagesActivity.this,ImageUploadedActivity.class));
                finish();
            }
        });

        uploadImage1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(SystemClock.elapsedRealtime() - lastClicked < 1000){
                    return;
                }
                lastClicked = SystemClock.elapsedRealtime();
                if(image1Uploaded){
                    Toast.makeText(UploadImagesActivity.this, "This image is uploaded", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d(TAG, "onClick: launching camera after checking permissions");
                reference = FirebaseDatabase.getInstance().getReference().child("Car Status").child(getIntent().getStringExtra("carNumber").toString());
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        long presentTimeStamp = System.currentTimeMillis();
                        Double timeStamp = Double.valueOf(dataSnapshot.child("timeStamp").getValue().toString());
                        if(dataSnapshot.child("status").getValue().toString().equals("In waiting")){
                            Log.d(TAG, "onDataChange: Car is not scanned");
                            return;
                        }

                        if((presentTimeStamp - timeStamp) < timerValue*60000){
                            Log.d(TAG, "onDataChange: Timer is not complete timeStamp : " + (presentTimeStamp - timeStamp) / 60000);

                        }else{

                            if(ActivityCompat.checkSelfPermission(UploadImagesActivity.this,CAMERA_PERMISSION[0]) == PackageManager.PERMISSION_GRANTED){
                                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                intent.putExtra(getString(R.string.carNumber),getIntent().getStringExtra("carNumber").toString());
                                startActivityForResult(intent,CAMERA_REQUEST_CODE);
                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });




        uploadImage2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(SystemClock.elapsedRealtime() - lastClicked < 1000){
                    return;
                }
                lastClicked = SystemClock.elapsedRealtime();
                if(image2Uploaded){
                    Toast.makeText(UploadImagesActivity.this, "This image is uploaded", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d(TAG, "onClick: launching camera after checking permissions");
                reference = FirebaseDatabase.getInstance().getReference().child("Car Status").child(getIntent().getStringExtra("carNumber").toString());
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        long presentTimeStamp = System.currentTimeMillis();
                        Double timeStamp = Double.valueOf(dataSnapshot.child("timeStamp").getValue().toString());
                        if(dataSnapshot.child("status").getValue().toString().equals("In waiting")){
                            Log.d(TAG, "onDataChange: Car is not scanned");
                            return;
                        }

                        if((presentTimeStamp - timeStamp) < timerValue*60000){
                            Log.d(TAG, "onDataChange: Timer is not complete timeStamp : " + (presentTimeStamp - timeStamp) / 60000);

                        }else{

                            if(ActivityCompat.checkSelfPermission(UploadImagesActivity.this,CAMERA_PERMISSION[0]) == PackageManager.PERMISSION_GRANTED){
                                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                intent.putExtra(getString(R.string.carNumber),getIntent().getStringExtra("carNumber").toString());
                                startActivityForResult(intent,CAMERA_REQUEST_CODE);
                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



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
                .child("cars/" + area + "/" + carNumber + "/" + calendar.getTime() + " photo "+n);

        if (data != null) {

            if (requestCode == CAMERA_REQUEST_CODE) {
                Log.d(TAG, "onActivityResult: request code is for camera");
                progressBar.setVisibility(View.VISIBLE);

                final Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                if(bitmap == null){
                    return;
                }

                byte[] bytes = getByteFromBitmap(bitmap, 50);
                UploadTask uploadTask = null;
                uploadTask = storageReference.putBytes(bytes);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String URI = uri.toString();
                                FirebaseDatabase.getInstance().getReference().child("Car Status").child(carNumber)
                                        .child("timeStamp").setValue("0");
                                FirebaseDatabase.getInstance().getReference().child("Car Status").child(carNumber)
                                        .child("status").setValue("cleaned");
                                String date = new SimpleDateFormat("dd-MM-yyyy").format(Calendar.getInstance().getTime());
                                try {
                                    reference = FirebaseDatabase.getInstance().getReference();
                                    reference.child("Car Status").child(carNumber).child("doneBy").setValue(auth.getUid());
                                    reference.child("Car Status").child(carNumber).child("Work History").child(date + n).child("doneBy").setValue(auth.getUid());
                                    reference.child("Car Status").child(carNumber).child("Work History").child(date + n).child("Photo Url").setValue(URI);
                                    reference.child("Car Status").child(carNumber).child("status").setValue("cleaned");
                                    reference.child("Car Status").child(carNumber).child("timeStamp").setValue("0");
                                    FirebaseDatabase.getInstance().getReference().child("Employee").child(FirebaseAuth.getInstance().getUid()).child("status").setValue("free");
                                    FirebaseDatabase.getInstance().getReference().child("Employee").child(FirebaseAuth.getInstance().getUid()).child("working on").setValue("");
                                    FirebaseDatabase.getInstance().getReference().child("Employees").child(area).child(FirebaseAuth.getInstance().getUid()).child("working on").setValue("");
                                    reference.child("Employee").child(auth.getUid()).child("Work History").child(date).child(carNumber).child("time").setValue(Calendar.getInstance().getTime());
                                    reference.child("Employee").child(auth.getUid()).child("Work History").child(date).child(carNumber).child("Image Url " + n).setValue(URI);
                                }catch (Exception e){
                                    Log.d(TAG, "onSuccess: got error after uploading" + e.getMessage());
                                    Toast.makeText(UploadImagesActivity.this, "error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                                n++;
                                if(n==0 || image2Uploaded) {
                                    image1Uploaded = true;
                                    uploadImage1.setImageBitmap(bitmap);
                                }else{
                                    image2Uploaded = true;
                                    uploadImage2.setImageBitmap(bitmap);
                                }

                                progressBar.setVisibility(View.GONE);
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed upload");
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(UploadImagesActivity.this, "photo uploading failed" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100 * taskSnapshot.getBytesTransferred()) / (taskSnapshot.getTotalByteCount());

                        if(progress - 15 > photoUploadProgress){
                            photoUploadProgress = (int) progress;
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
