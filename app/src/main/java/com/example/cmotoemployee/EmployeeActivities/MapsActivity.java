package com.example.cmotoemployee.EmployeeActivities;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cmotoemployee.AdminActivities.AddCarActivity;
import com.example.cmotoemployee.AdminActivities.AdminHomeActivity;
import com.example.cmotoemployee.ErrorHandler.CrashHandler;
import com.example.cmotoemployee.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = "MapsActivity";

    private static final int VERIFY_PERMISSION_REQUEST_CODE = 134;
    public final String[] LOCATION_SERVICE_PERMISSION = {Manifest.permission.ACCESS_FINE_LOCATION};

    // maps vars
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location currentLocation;

    // widgets
    private ImageView marker;
    private TextView addLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(getApplicationContext()));
        Log.d(TAG, "onCreate: called for MapsActivity");
        marker = findViewById(R.id.marker);
        addLocation = findViewById(R.id.addLocation);
        addLocation.setVisibility(View.GONE);
        if(getIntent().hasExtra(getString(R.string.getLocation))){
            marker.setVisibility(View.GONE);
            addLocation.setVisibility(View.VISIBLE);
        }


        marker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToCarLocation();
            }
        });

        addLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseDatabase.getInstance().getReference().child("cars").child(getIntent().getStringExtra(getString(R.string.Area)))
                        .child(getIntent().getStringExtra(getString(R.string.Society)))
                        .child(getIntent().getStringExtra("CarNumber")).child("Location").setValue(String.valueOf(currentLocation.getLatitude() + "," + currentLocation.getLongitude()));
                Intent intent = new Intent(MapsActivity.this, AdminHomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onCreate: permission asking , if not available previously");
            ActivityCompat.requestPermissions(MapsActivity.this,LOCATION_SERVICE_PERMISSION, VERIFY_PERMISSION_REQUEST_CODE);
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        getDeviceLocation();
    }

    private void goToCarLocation(){
        Log.d(TAG, "goToCarLocation: going to cars location");

        Double latitude = getIntent().getDoubleExtra(getString(R.string.latitude), currentLocation.getLatitude());
        Double longitude = getIntent().getDoubleExtra(getString(R.string.longitude), currentLocation.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(latitude, longitude));
        markerOptions.title("Car's Location");
        moveCamera(new LatLng(latitude, longitude), 15f);
        mMap.addMarker(markerOptions);


    }

    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting device present location");


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{

            Task location = fusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Log.d(TAG, "onComplete: location found " + task.getResult());
                        currentLocation = (Location) task.getResult();
                        if(currentLocation != null) {
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15f);
                        }else{
                            Toast.makeText(MapsActivity.this, "Try to refresh the map", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Log.d(TAG, "onComplete: cant get location");
                        Toast.makeText(MapsActivity.this, "unable to get current Location", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: got security error : " + e.getMessage() );
        }



    }

    private void moveCamera(LatLng latLng, float zoom){
        Log.d(TAG, "moveCamera: moving camera to loaction : " + latLng);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));

    }
}