package com.example.cmotoemployee.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cmotoemployee.EmployeeActivities.StartCarCleaningActivity;
import com.example.cmotoemployee.Model.Payment;
import com.example.cmotoemployee.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecyclerViewAdapterForProfile extends RecyclerView.Adapter<RecyclerViewAdapterForProfile.ViewHolder> {

    private static final String TAG = "RecyclerViewAdapterForP";

    private List<Payment> carsPayment;
    private Context context;
    FirebaseAuth auth = FirebaseAuth.getInstance();

    public RecyclerViewAdapterForProfile(List<Payment> carsPayment, Context context1) {
        this.carsPayment = carsPayment;
        this.context = context1;
    }

    @NonNull
    @Override
    public RecyclerViewAdapterForProfile.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.payment_item,parent,false);
        return new RecyclerViewAdapterForProfile.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerViewAdapterForProfile.ViewHolder holder, final int position) {

        try {
            holder.dates.setText(carsPayment.get(position).getDate().substring(0,2)+ " " + getMonth(carsPayment.get(position).getDate()) + "" + carsPayment.get(position).getDate().substring(6));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            holder.day.setText(getDay(carsPayment.get(position).getDate()) + "");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.price.setText("Rs. " + carsPayment.get(position).getPrice() + "");

        if(carsPayment.get(position).getPayOn()!=null && !carsPayment.get(position).getPayOn().equals("due")){
            try {
                holder.paidOn.setText("Pay received on " + carsPayment.get(position).getPayOn().substring(0,2) + " " + getMonth(carsPayment.get(position).getPayOn()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "onBindViewHolder: paid on :" + carsPayment.get(position).getPayOn());
        }else{
            holder.paidOn.setText("Payment is due");
        }
        holder.carsCleaned.setText(carsPayment.get(position).getCarsCleaned() + " Cars");

    }

    @Override
    public int getItemCount() {
        return carsPayment.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView dates;
        private TextView price;
        private TextView paidOn;
        private TextView carsCleaned;
        private TextView day;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            price = itemView.findViewById(R.id.amount);
            dates = itemView.findViewById(R.id.date);
            paidOn = itemView.findViewById(R.id.paidOn);
            day = itemView.findViewById(R.id.day);
            carsCleaned = itemView.findViewById(R.id.carsCleaned);
        }

    }



    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public String getDay(String date) throws ParseException {
        Date d = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).parse(date);
        Log.d(TAG, "getDay: date : " + d);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        String dayName = new SimpleDateFormat("EEE",Locale.ENGLISH).format(cal.getTime());
        Log.d(TAG, "getDay: present day : " + dayName);
        return dayName.toUpperCase();
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
