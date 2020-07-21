package com.example.cmotoemployee.AdminActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cmotoemployee.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PaymentActivity extends AppCompatActivity {
    private static final String TAG = "PaymentActivity";

    public static int NUMBER_OF_DAYS_LEFT = 26;

    private EditText CarNumber;
    private EditText CarAddress;
    private EditText amount;
    private AppCompatButton Pay;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private DatabaseReference reference;

    // vars
    long RemainingDays = 0;
    String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        CarNumber = findViewById(R.id.carNumber);
        CarAddress = findViewById(R.id.carAddress);
        amount = findViewById(R.id.amount);
        Pay = findViewById(R.id.pay);
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        reference = database.getReference();

        date = new SimpleDateFormat("dd-MM-yyyy").format(Calendar.getInstance().getTime());

        Pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String workingAddress = CarAddress.getText().toString();

                reference.child("Car Status").child(CarNumber.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            if(verifyCarAddress(workingAddress) == true){

                            }
                            
                        }else{
                            Toast.makeText(PaymentActivity.this, "Car Number Not Found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });
    }

    private void doPayment() {
        final String workingAddress = CarAddress.getText().toString();
        reference.child("Car Status").child(CarNumber.getText().toString()).child("Days_Left").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    Log.d(TAG, "onDataChange: payment is already active and extended");
                    RemainingDays = (long)dataSnapshot.getValue();
                    reference.child("Expired/"+workingAddress).child(CarNumber.getText().toString()).removeValue();
                    reference.child("Car Status").child(CarNumber.getText().toString()).child("Payment History").child(date).setValue(amount.getText().toString());
                    reference.child("Car Status").child(CarNumber.getText().toString()).child("Days_Left").setValue(NUMBER_OF_DAYS_LEFT + RemainingDays);
                    reference.child("Car Status").child(CarNumber.getText().toString()).child("Payment").setValue("Active");
                }else{
                    Log.d(TAG, "onDataChange: payment was expired and now extended");
                    reference.child("Expired/"+workingAddress).child(CarNumber.getText().toString()).removeValue();
                    reference.child("Car Status").child(CarNumber.getText().toString()).child("Payment History").child(date).setValue(amount.getText().toString());
                    reference.child("Car Status").child(CarNumber.getText().toString()).child("Days_Left").setValue(NUMBER_OF_DAYS_LEFT);
                    reference.child("Car Status").child(CarNumber.getText().toString()).child("Payment").setValue("Active");
                }
                Toast.makeText(PaymentActivity.this, "Payment Complete", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private boolean verifyCarAddress(final String workingAddress) {
        final boolean[] send = new boolean[1];

        reference.child("cars/clusters/"+workingAddress).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    Toast.makeText(PaymentActivity.this, "Check The Area name and Society Name Again", Toast.LENGTH_SHORT).show();
                    send[0] = false;
                }else{
                    send[0] = true;
                    Toast.makeText(PaymentActivity.this, "Verification Done...", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onDataChange: address verification done : "+ send[0]);
                    Log.d(TAG, "onDataChange: got the access to payment");
                    doPayment();
                    startActivity(new Intent(PaymentActivity.this,AdminHomeActivity.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return send[0];

    }
}