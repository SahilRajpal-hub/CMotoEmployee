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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.droidnet.DroidListener;
import com.droidnet.DroidNet;
import com.example.cmotoemployee.AdminActivities.AdminHomeActivity;
import com.example.cmotoemployee.EmployeeActivities.HomeActivity;
import com.example.cmotoemployee.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StartActivity extends AppCompatActivity implements DroidListener {
    private static final String TAG = "StartActivity";

    // Firebase Vars
    private FirebaseAuth mAuth;
    private DatabaseReference reference;

    // widgets
    private EditText Email;
    private EditText Password;
    private AppCompatButton loginButton;
    private TextView mPleaseWait;
    private ProgressBar mProgressBar;
    private TextView loginAsAdmin;
    private ImageView back;
    private RelativeLayout logo;

    //vars
    private  boolean admin = false;
    private DroidNet droidNet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Log.d(TAG, "onCreate: starting login activity for employee");

        DroidNet.init(this);

        //initializing widgets
        Email = findViewById(R.id.input_email);
        Password = findViewById(R.id.input_password);
        loginAsAdmin = findViewById(R.id.loginAsAdmin);

        back = findViewById(R.id.login_back);

        mAuth = FirebaseAuth.getInstance();
        loginButton = findViewById(R.id.login_btn);
        mPleaseWait = findViewById(R.id.loginPleaseWait);
        mProgressBar = findViewById(R.id.login_progressBar);
        logo = findViewById(R.id.logo);
        mProgressBar.setVisibility(View.GONE);
        mPleaseWait.setVisibility(View.GONE);

        reference = FirebaseDatabase.getInstance().getReference();

        loginAsAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: switching to admin login");
                Intent registerUser = new Intent(StartActivity.this, loginAdminActivity.class);
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
                    Toast.makeText(StartActivity.this, "Please Input all the Credentials", Toast.LENGTH_SHORT).show();
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
                                            if(!admin[0]){
                                                Toast.makeText(StartActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                                Intent HomeActivity = new Intent(StartActivity.this, com.example.cmotoemployee.EmployeeActivities.HomeActivity.class);
                                                startActivity(HomeActivity);
                                            }else {
                                                Toast.makeText(StartActivity.this, "Login In Admin Section", Toast.LENGTH_SHORT).show();
                                                mAuth.signOut();
                                                Intent intent = new Intent(StartActivity.this, loginAdminActivity.class);
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
                                Toast.makeText(StartActivity.this, "Please Check your Credentials", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        DroidNet.getInstance().removeAllInternetConnectivityChangeListeners();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.

        FirebaseUser currentUser = mAuth.getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Admin");
        if(currentUser != null){
            logo.setVisibility(View.VISIBLE);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                        if(snapshot.getKey().equals(mAuth.getUid())){
                            Log.d(TAG, "onDataChange: key = " + snapshot.getKey());
                            admin = true;


                        }
                    }
                    if(admin){
                        Intent HomeActivity = new Intent(StartActivity.this, AdminHomeActivity.class);
                        startActivity(HomeActivity);
                        finish();
                    }else {
                        Intent HomeActivity = new Intent(StartActivity.this, HomeActivity.class);
                        startActivity(HomeActivity);
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }else{
            logo.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    private void setVisibility(boolean visibility){
        if(visibility){
            Email.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.VISIBLE);
            loginAsAdmin.setVisibility(View.VISIBLE);
            Password.setVisibility(View.VISIBLE);
        }else{
            Email.setVisibility(View.GONE);
            loginButton.setVisibility(View.GONE);
            loginAsAdmin.setVisibility(View.GONE);
            Password.setVisibility(View.GONE);
        }
    }

    @Override
    public void onInternetConnectivityChanged(boolean isConnected) {
        if(!isConnected){
            Toast.makeText(this, "NO Internet Connection", Toast.LENGTH_SHORT).show();
        }
    }
}