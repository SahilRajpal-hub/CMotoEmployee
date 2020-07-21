package com.example.cmotoemployee.Authentication;

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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cmotoemployee.AdminActivities.AddCarActivity;
import com.example.cmotoemployee.Model.Employee;
import com.example.cmotoemployee.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;

public class RegisterEmployee extends AppCompatActivity {
    private static final String TAG = "RegisterEmployee";

    private static final int CAMERA_REQUEST_CODE = 2;

    //permissions for camera
    public final String[] CAMERA_PERMISSION = {Manifest.permission.CAMERA};

    // Firebase Vars
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private StorageReference storageReference;

    // widgets
    private EditText employeeName, employeeNumber, employeeEmail, employeeAddress, employeePassword ;
    private AppCompatButton addEmployee;
    private Button addPhoto;
    private ProgressBar progressBar;
    private TextView pleaseWait;

    // vars
    private String EmployeeId;
    private Double photoUploadProgress;
    private String EmployeePhotoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_employee);
        Log.d(TAG, "onCreate: called for Register Employee activity");
        pleaseWait = findViewById(R.id.pleaseWait);
        employeeName = findViewById(R.id.employeeName);
        employeeAddress = findViewById(R.id.employeeAddress);
        employeeEmail = findViewById(R.id.employeeEmail);
        employeeNumber = findViewById(R.id.employeePhoneNumber);
        addEmployee = findViewById(R.id.addEmployee);
        addPhoto = findViewById(R.id.addPhoto);
        employeePassword = findViewById(R.id.employeeLoginPassword);
        progressBar = findViewById(R.id.progressBar);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();

        // initializing firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();

        addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(employeeNumber.getText().toString().equals("")){
                    Toast.makeText(RegisterEmployee.this, "First Add Car Number", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(ActivityCompat.checkSelfPermission(RegisterEmployee.this,CAMERA_PERMISSION[0]) == PackageManager.PERMISSION_GRANTED){
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent,CAMERA_REQUEST_CODE);
                }
            }
        });

        addEmployee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Employee employee = new Employee();
                employee.setAddress(employeeAddress.getText().toString());
                employee.setEmail(employeeEmail.getText().toString());
                employee.setName(employeeName.getText().toString());
                employee.setNumber(employeeNumber.getText().toString());

                auth.createUserWithEmailAndPassword(employee.getEmail(),employeePassword.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    Log.d(TAG, "onComplete: employee id is created");
                                    Toast.makeText(RegisterEmployee.this, "Employee Id is created ", Toast.LENGTH_SHORT).show();
//                                    reference.child("Employee").child("")
                                }
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
                .child("employees/" + employeeNumber.getText().toString() + "/photo " + calendar.getTime());

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
                                EmployeePhotoUrl = String.valueOf(uri);
                                Toast.makeText(RegisterEmployee.this, "image upload successful", Toast.LENGTH_SHORT).show();

                            }
                        });
                        progressBar.setVisibility(View.GONE);
                        pleaseWait.setVisibility(View.GONE);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed upload");
                        Toast.makeText(RegisterEmployee.this, "photo uploading failed" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        pleaseWait.setVisibility(View.GONE);

                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100 * taskSnapshot.getBytesTransferred()) / (taskSnapshot.getTotalByteCount());

                        if(progress - 15 > photoUploadProgress){
                            Toast.makeText(RegisterEmployee.this, "uploading : " + progress + " %", Toast.LENGTH_SHORT).show();
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