package com.example.cmotoemployee.EmployeeActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cmotoemployee.Adapter.RecyclerViewAdapterForProfile;
import com.example.cmotoemployee.Model.Payment;
import com.example.cmotoemployee.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PaymentActivity extends AppCompatActivity {
    private static final String TAG = "PaymentActivity";

    //Firebase vars
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private DatabaseReference reference;

    // widgets
    private RecyclerView recyclerView;
    private RecyclerViewAdapterForProfile adapter;
    private ImageView back;
    private TextView presentPayment,amountDue,lastPaidOn;

    //vars
    private List<String> CarNumbers;
    private List<Payment> carsPayment;
    private int DueAmount = 0;
    private String lastPaid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_details);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        reference = database.getReference();
        recyclerView = findViewById(R.id.recyclerView);
        amountDue = findViewById(R.id.amountDue);
        back = findViewById(R.id.back);
        lastPaidOn = findViewById(R.id.lastPaidOn);
        presentPayment = findViewById(R.id.presentMonth);
        carsPayment = new ArrayList<>();
        CarNumbers = new ArrayList<>();


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == RecyclerView.SCROLL_STATE_IDLE){
                    String date = carsPayment.get(((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition()).getDate();
                    Log.d(TAG, "onScrollStateChanged: date : " + date);
                    try {
                        presentPayment.setText(getMonth(date));
                    } catch (ParseException e) {
                        e.printStackTrace();
                        Log.d(TAG, "onScrollStateChanged: error while setting present month : " + e.getMessage());
                    }
                }
            }
        });



        reference.child("Employee").child(auth.getUid()).child("Work History").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i=0;
                try {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Payment carPayment = new Payment();
                        Log.d(TAG, "onDataChange: " + snapshot);
                        if (snapshot.getKey().toString() != null) {
                            carPayment.setDate(snapshot.getKey());
                        }
                        int money = 0;
                        if (snapshot.child("income").getValue().toString() != null) {
                            money = Integer.parseInt(snapshot.child("income").getValue().toString());
                            carPayment.setPrice(money);
                            DueAmount += money;
                        }
                        String paymentOn = new String();
                        if (snapshot.child("paid on").exists()) {
                            paymentOn = snapshot.child("paid on").getValue().toString();
                            carPayment.setPayOn(paymentOn);
                            lastPaid = carPayment.getDate();
                            DueAmount -= money;
                            carPayment.setCarsCleaned((int) snapshot.getChildrenCount() - 2);
                        }else {
                            carPayment.setPayOn("due");
                            carPayment.setCarsCleaned((int) snapshot.getChildrenCount() - 1);
                        }
                        Log.d(TAG, "onDataChange: income = " + money);
                        i++;
                        carsPayment.add(carPayment);
                    }
//                    Log.d(TAG, "onDataChange: " + MoneyOnDate.toString());
//                    setAdapter(Dates, MoneyOnDate,paidOn,carsCleaned);
                    setAdapter(carsPayment);
                }catch (Exception e){
                    Log.d(TAG, "onDataChange: got an exception" + e);
                    Toast.makeText(PaymentActivity.this, "An exception occurred while retrieving data from server : " + e.getStackTrace(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PaymentActivity.this, "request cancelled", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setAdapter(List<Payment> carsPayment){
        try {
//            Log.d(TAG, "setAdapter: " + Date + " // " + price);
            Collections.sort(carsPayment, new Comparator<Payment>() {
                @Override
                public int compare(Payment o1, Payment o2) {
                    if(o1.getDate()==null || o2.getDate()==null)
                    return 0;
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                    try {
                        return sdf.parse(o1.getDate()).compareTo(sdf.parse(o2.getDate()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return 0;
                }
            });
            try {
                presentPayment.setText(getMonth(carsPayment.get(0).getDate()) + "");
            } catch (ParseException e) {
                e.printStackTrace();
            }
            amountDue.setText(DueAmount + "");
            lastPaidOn.setText("last paid on " + lastPaid.substring(0, 2) + " " + getMonth(lastPaid) + " " + lastPaid.substring(6));

            adapter = new RecyclerViewAdapterForProfile(carsPayment, getApplicationContext());
            recyclerView.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);
        }catch (Error | ParseException e){
            Log.d(TAG, "setAdapter: got the error while setting employee profile" + e.getMessage());
            Toast.makeText(this, "Met an Error while retrieving your Profile data", Toast.LENGTH_SHORT).show();
        }
    }

    public String getMonth(String date) throws ParseException {
        Date d = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).parse(date);
        Log.d(TAG, "getMonth: date : " + d);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        String monthName = new SimpleDateFormat("MMM",Locale.ENGLISH).format(cal.getTime());
        Log.d(TAG, "getMonth: present month : " + monthName);
        return monthName.toUpperCase();
    }




}