package com.example.cmotoemployee.EmployeeActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.droidnet.DroidListener;
import com.droidnet.DroidNet;
import com.example.cmotoemployee.Adapter.RecyclerViewAdapter;
import com.example.cmotoemployee.Authentication.StartActivity;
import com.example.cmotoemployee.ErrorHandler.CrashHandler;
import com.example.cmotoemployee.R;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeActivity extends AppCompatActivity implements DroidListener {
    private static final String TAG = "HomeActivity";

    // Firebase Vars
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private FirebaseUser user;

    // Widgets
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    private ProgressBar progressBar;
    private ImageView scanner,faceBook,linkedIn,instagram;
    private ImageView exit;
    private TextView carsCleaned;
    private TextView remainingCars;
    private TextView workingComplete,paymentDetails,profile,carList,logout,name,termsAndCondition,aboutUs,contactUs;
    private AppBarConfiguration mAppBarConfiguration;
    private ImageView menuOptions;
    private RelativeLayout menu;
    private RelativeLayout menuClose;

    //Vars
    private String todayCars ="" , Area = "";
    private List<String> carsToBeWashed;
    private List<String> carsModel;
    private List<String> carsPhoto;
    private int carsDone = 0;
    private String WorkingOn = "";
    private int lastClicked = 0;
    private boolean menuVisible = false;

    // internet listener vars
    private DroidNet droidNet;
    private boolean isConnected = true;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(getApplicationContext()));
        Log.d(TAG, "onCreate: ");

        //initializing firebase vars
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();

        //initializing widgets
        progressBar = findViewById(R.id.progressBar);
        menuOptions = findViewById(R.id.menu);
        paymentDetails = findViewById(R.id.paymentsDetails);
        profile = findViewById(R.id.profile);
        carList = findViewById(R.id.carList);
        faceBook = findViewById(R.id.facebookLink);
        instagram = findViewById(R.id.instagramLink);
        linkedIn = findViewById(R.id.lunkedIn);
        termsAndCondition = findViewById(R.id.termsAndCondition);
        contactUs = findViewById(R.id.contactUs);
        aboutUs = findViewById(R.id.aboutUs);
        logout = findViewById(R.id.logout);
        progressBar.setVisibility(View.VISIBLE);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        menu = findViewById(R.id.Menu);
        exit = findViewById(R.id.exit);
        name = findViewById(R.id.name);
        menuClose = findViewById(R.id.remainingBody);


        scanner = findViewById(R.id.scanner);
        carsCleaned = findViewById(R.id.done);
        remainingCars = findViewById(R.id.remaining);
        workingComplete = findViewById(R.id.workComplete);

        //recyclerView initializing
        recyclerView = findViewById(R.id.recyclerView);

        //initializing variables
        carsToBeWashed = new ArrayList<>();
        carsModel = new ArrayList<>();
        carsPhoto = new ArrayList<>();
        todayCars ="";

        //internet connection listener
        try {
            droidNet = DroidNet.getInstance();
            droidNet.addInternetConnectivityListener(this);
        }catch (Exception e){
            Log.e(TAG, "onCreate: error : " + e.getStackTrace());
        }


        if(!isConnected){
            Toast.makeText(this, "Internet connection lost", Toast.LENGTH_SHORT).show();
        }

//        FirebaseDatabase.getInstance().getReference().child("Employee").child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if(carsToBeWashed.size()>0) {
//                    finish();
//                    startActivity(getIntent());
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

        reference.child("Employee").child(mAuth.getUid()).child("Name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                name.setText(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        menuOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuVisibility(true);
            }
        });

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuVisibility(false);
            }
        });

        menuClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(menuVisible){
                    menuVisibility(false);
                }
            }
        });

        paymentDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(menuVisible) {
                    menuVisibility(false);
                    startActivity(new Intent(HomeActivity.this, PaymentActivity.class));
                }
            }
        });

        carList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(menuVisible) {
                    menuVisibility(false);
                }
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(menuVisible) {
                    menuVisibility(false);
                    mAuth.signOut();
                    startActivity(new Intent(HomeActivity.this, StartActivity.class));
                    finish();
                }
            }
        });

        faceBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(menuVisible) {
                    Uri uri = Uri.parse("https://www.facebook.com/CMoTo-101685531582029/");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            }
        });

        linkedIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(menuVisible) {
                    Uri uri = Uri.parse("https://www.linkedin.com/company/cmoto");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            }
        });

        instagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(menuVisible) {
                    Uri uri = Uri.parse("https://www.instagram.com/invites/contact/?i=iix8qk5d4i0a&utm_content=h7njsz7");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            }
        });

        termsAndCondition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(menuVisible) {
                    Toast.makeText(HomeActivity.this, "Terms and Condition", Toast.LENGTH_SHORT).show();
                }
            }
        });

        contactUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(menuVisible) {
                    Toast.makeText(HomeActivity.this, "Contact Us", Toast.LENGTH_SHORT).show();
                }
            }
        });

        aboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(menuVisible) {
                    Toast.makeText(HomeActivity.this, "About Us", Toast.LENGTH_SHORT).show();
                }
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(menuVisible) {
                    startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                }
            }
        });



        receiveData();

        scanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime() - lastClicked < 1000){
                    return;
                }
                lastClicked = (int) SystemClock.elapsedRealtime();
                reference.child("Employee").child(mAuth.getUid()).child("status").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue().toString().equals("working")){
                            Toast.makeText(HomeActivity.this, "The timer is already started", Toast.LENGTH_SHORT).show();
                            return;
                        }else{
                            Intent intent = new Intent(HomeActivity.this,QRCodeScanning.class);
                            Bundle bundle = new Bundle();
                            bundle.putCharSequenceArrayList("carsNumberArray", new ArrayList<CharSequence>(carsToBeWashed));
                            bundle.putCharSequence("Areas", Area);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });





    }


    public void menuVisibility(boolean visible){
        if(!menuVisible){

            menu.setVisibility(View.VISIBLE);
            TranslateAnimation animation = new TranslateAnimation(-menu.getWidth(),0,0,0);
            animation.setDuration(500);
            animation.setFillAfter(true);
            menu.startAnimation(animation);

            menuClose.setVisibility(View.VISIBLE);
            menuVisible = true;
        }else{

            menu.setVisibility(View.GONE);
            TranslateAnimation animation = new TranslateAnimation(0,-menu.getWidth(),0,0);
            animation.setDuration(500);
            animation.setFillAfter(true);
            menu.startAnimation(animation);

            menuClose.setVisibility(View.GONE);
            menuVisible = false;
        }
    }



    public void receiveData(){

        DatabaseReference query = reference.child("Employee").child(mAuth.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot != null) {
                    String date = new SimpleDateFormat("dd-MM-yyyy").format(Calendar.getInstance().getTime());
                    Log.d(TAG, "onDataChange: we got this cars : " + dataSnapshot.child("todaysCars").getValue());
                    todayCars = (String) dataSnapshot.child("todaysCars").getValue();
                    Area = (String) dataSnapshot.child("Working_Address").getValue();
                    if(dataSnapshot.child("working on").getValue() != null) {
                        WorkingOn = dataSnapshot.child("working on").getValue().toString();
                    }
                    if(dataSnapshot.child("Work History").child(date).child("paidOn").exists()) {
                        carsDone = ((int) dataSnapshot.child("Work History").child(date).getChildrenCount()) - 2;
                    }else if (dataSnapshot.child("Work History").child(date).exists()){
                        carsDone = ((int) dataSnapshot.child("Work History").child(date).getChildrenCount()) - 1;
                    }
                    if(todayCars != null) {
                        carsToBeWashed = new ArrayList<String>(Arrays.asList(todayCars.split("\\s*,\\s*")));
                        Set<String> set = new HashSet<>(carsToBeWashed);
                        carsToBeWashed.clear();
                        carsToBeWashed.addAll(set);
                        carsToBeWashed.remove("");
                        Log.d(TAG, "onCreate: we got cars array as follows " + carsToBeWashed);
                        if(carsToBeWashed.size()==0){
                            recyclerView.setVisibility(View.GONE);
                            scanner.setVisibility(View.GONE);
                            workingComplete.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            remainingCars.setText("remaining\n0");
                            carsCleaned.setText("done\n"+carsDone);
                        }
                        for(int i=0; i<carsToBeWashed.size(); i++){
                            DatabaseReference carsRef = reference.child("cars").child(Area).child(carsToBeWashed.get(i));
                            carsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnap) {
                                    Log.d(TAG, "onDataChange: getting carsModel and carsPhoto" + dataSnap);
                                    if(dataSnap.exists()) {

                                        Log.d(TAG, "onDataChange: car : "+ dataSnap.child("model"));
                                        carsModel.add((String) dataSnap.child("model").getValue());
                                        carsPhoto.add((String) dataSnap.child("photo").getValue());

                                    }
                                    Log.d(TAG, "onDataChange: model : " + carsModel + "photo : " + carsPhoto);
                                    Log.d(TAG, "onDataChange: "+ carsToBeWashed.size()+" : "+ carsModel.size());
                                    if(carsModel.size() == carsToBeWashed.size()){
                                        Log.d(TAG, "onDataChange: setting adapter");
                                        if(carsToBeWashed.contains(WorkingOn) && carsToBeWashed.indexOf(WorkingOn) != 0){
                                            carsToBeWashed.remove(WorkingOn);
                                            carsToBeWashed.add(0,WorkingOn);
                                        }
                                        setAdapter();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }

                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: event cancelled due to error : " + databaseError.getMessage());
                Toast.makeText(HomeActivity.this, "event cancelled", Toast.LENGTH_SHORT).show();
            }

        });
    }

    public void setAdapter(){

        Log.d(TAG, "setAdapter: setAdapter function called");

        remainingCars.setText(carsToBeWashed.size() + "\nRemaining" + "");
        carsCleaned.setText(carsDone + "\nDone"  + "");
        adapter = new RecyclerViewAdapter(carsToBeWashed,getApplicationContext(),Area,carsModel,carsPhoto);
        recyclerView.setHasFixedSize(true);
        recyclerView.setMotionEventSplittingEnabled(false);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        droidNet.removeInternetConnectivityChangeListener(this);
    }

    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }


    @Override
    public void onInternetConnectivityChanged(boolean Connected) {
        if(Connected){
            Log.d(TAG, "onInternetConnectivityChanged: INTERNET connected");
            if(recyclerView != null) {
                recyclerView.setVisibility(View.VISIBLE);
                scanner.setVisibility(View.VISIBLE);
            }
            isConnected = true;
        }else{
            Log.d(TAG, "onInternetConnectivityChanged: INTERNET lost in HomeActivity");
            isConnected = false;
            recyclerView.setVisibility(View.GONE);
            scanner.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Internet Connection Lost", Toast.LENGTH_SHORT).show();
        }
    }
}