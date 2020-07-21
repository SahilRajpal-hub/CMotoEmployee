package com.example.cmotoemployee.Authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cmotoemployee.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class loginAdminActivity extends AppCompatActivity {
    private static final String TAG = "loginAdminActivity";

    // Firebase Vars
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference reference;

    // widgets
    private EditText Email;
    private EditText Password;
    private AppCompatButton loginButton;
    private TextView mPleaseWait;
    private ProgressBar mProgressBar;
    private TextView loginAsEmployee;
    private ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_admin);
        Log.d(TAG, "onCreate: called for admin start panel");

        //initializing widgets
        Email = findViewById(R.id.input_email);
        Password = findViewById(R.id.input_password);
        loginAsEmployee = findViewById(R.id.loginAsEmployee);
        loginButton = findViewById(R.id.login_btn);
        mPleaseWait = findViewById(R.id.loginPleaseWait);
        mProgressBar = findViewById(R.id.login_progressBar);
        back = findViewById(R.id.login_back);
        mProgressBar.setVisibility(View.GONE);
        mPleaseWait.setVisibility(View.GONE);

        // initializing Firebase
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();



        loginAsEmployee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: switching to admin login");
                Intent registerUser = new Intent(loginAdminActivity.this, StartActivity.class);
                startActivity(registerUser);
                finish();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = Email.getText().toString();
                String password = Password.getText().toString();
                mProgressBar.setVisibility(View.VISIBLE);
                mPleaseWait.setVisibility(View.VISIBLE);
                if(email.isEmpty() || password.isEmpty()){
                    Toast.makeText(loginAdminActivity.this, "Please Input all the Credentials", Toast.LENGTH_SHORT).show();
                    mProgressBar.setVisibility(View.GONE);
                    mPleaseWait.setVisibility(View.GONE);
                }else{
                    mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                try{
                                    mProgressBar.setVisibility(View.GONE);
                                    mPleaseWait.setVisibility(View.GONE);
                                    DatabaseReference query = reference.child("Admin");
                                    final boolean[] admin = {false};
                                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                                                if(snapshot.getKey().equals(mAuth.getUid())){
                                                    Log.d(TAG, "onDataChange: key = " + snapshot.getKey());
                                                    admin[0] = true;
                                                    break;
                                                }
                                            }
                                            if(admin[0]){
                                                Toast.makeText(loginAdminActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                                Intent HomeActivity = new Intent(loginAdminActivity.this, com.example.cmotoemployee.AdminActivities.AdminHomeActivity.class);
                                                startActivity(HomeActivity);
                                            }else {
                                                Toast.makeText(loginAdminActivity.this, "Login In Employee Section", Toast.LENGTH_SHORT).show();
                                                mAuth.signOut();
                                                Intent intent = new Intent(loginAdminActivity.this, StartActivity.class);
                                                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                }catch (NullPointerException e){
                                    Log.d(TAG, "onComplete: error : "+ e);
                                }

                            }else{
                                mProgressBar.setVisibility(View.GONE);
                                mPleaseWait.setVisibility(View.GONE);
                                Toast.makeText(loginAdminActivity.this, "Please Check your Credentials", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                }
            }
        });


    }



    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}