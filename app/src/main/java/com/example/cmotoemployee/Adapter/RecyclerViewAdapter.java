package com.example.cmotoemployee.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.droidnet.DroidListener;
import com.droidnet.DroidNet;
import com.example.cmotoemployee.EmployeeActivities.HomeActivity;
import com.example.cmotoemployee.EmployeeActivities.StartCarCleaningActivity;
import com.example.cmotoemployee.R;
import com.example.cmotoemployee.RoundedTransformation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "RecyclerViewAdapter";

    private List<String> cars;
    private List<String> carsPhoto;
    private List<String> carsModel;
    private Context context;
    private String Area;
    FirebaseAuth auth = FirebaseAuth.getInstance();


    //internet connection listeners
    private boolean Connected = true;

    //vars
    private long lastClicked = 0;
    private long toBeWait = 2000;

    public RecyclerViewAdapter(List<String> cars, Context context1, String area,List<String> carsModel, List<String> carsPhoto) {

        this.cars = cars;
        this.context = context1;
        this.Area = area;
        this.carsModel = carsModel;
        this.carsPhoto = carsPhoto;
    }


    @NonNull
    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.car_item,parent,false);
        return new RecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerViewAdapter.ViewHolder holder, final int position) {
       holder.CarNumber.setText(cars.get(position));
       holder.carName.setText(carsModel.get(position));
       Picasso.get().load(carsPhoto.get(position)).transform(new RoundedTransformation(60, 0)).into(holder.CarPhoto);
       FirebaseDatabase.getInstance().getReference("Employee").child(auth.getUid()).child("working on").addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               if(dataSnapshot.getValue() != null && dataSnapshot.getValue().toString().equals(cars.get(position))){
                   holder.CarNumber.setTextColor(ContextCompat.getColor(context,R.color.colorAccent));
                   holder.carName.setTextColor(ContextCompat.getColor(context,R.color.colorAccent));
                   final int[] timerValue = {0};
                   FirebaseDatabase.getInstance().getReference("Car Status").child(cars.get(position)).addListenerForSingleValueEvent(new ValueEventListener() {
                       @Override
                       public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                           if (dataSnapshot.child("category").getValue().toString().toLowerCase().equals("hatchback") || dataSnapshot.child("category").getValue().toString().toLowerCase().equals("compactsedan")) {
                               timerValue[0] = 1;
                           } else if (dataSnapshot.child("category").getValue().toString().toLowerCase().equals("sedan") || dataSnapshot.child("category").getValue().toString().toLowerCase().equals("luv")) {
                               timerValue[0] = 1;
                           } else if (dataSnapshot.child("category").getValue().toString().toLowerCase().equals("suv")) {
                               timerValue[0] = 1;
                           }
                           Double timeStamp = Double.valueOf(dataSnapshot.child("timeStamp").getValue().toString());
                           Double currentTimeStamp = (double) System.currentTimeMillis();
                           Log.d(TAG, "onDataChange: difference is " + (timerValue[0] - (currentTimeStamp - timeStamp) / 60000));
                           new CountDownTimer((long) (60000 * timerValue[0] - (currentTimeStamp - timeStamp)), 1000) {

                               @Override
                               public void onTick(long millisUntilFinished) {
                                   holder.timer.setText(Math.round(millisUntilFinished / 60000) + " : " + Math.round(millisUntilFinished / 1000) % 60);
                               }

                               @Override
                               public void onFinish() {
                                   holder.timer.setText("Time Finished");
                               }

                           }.start();
                       }

                       @Override
                       public void onCancelled(@NonNull DatabaseError databaseError) {

                       }
                   });
               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });



       final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Employee").child(auth.getUid()).child("working on");
       holder.CarNumber.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if(!Connected){
                   Toast.makeText(context, "No INTERNET connection", Toast.LENGTH_SHORT).show();
                   return;
               }
               if(SystemClock.elapsedRealtime() - lastClicked < toBeWait){
                   return;
               }
               lastClicked = SystemClock.elapsedRealtime();
               reference.addListenerForSingleValueEvent(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            if (dataSnapshot.getValue().toString().equals(cars.get(position)) || dataSnapshot.getValue().toString().equals("")) {

                                Toast.makeText(context, "number = " + holder.CarNumber.getText().toString(), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(context, StartCarCleaningActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra(context.getString(R.string.carNumber), holder.CarNumber.getText().toString());
                                intent.putExtra(context.getString(R.string.Area), Area);
                                context.startActivity(intent);
                            } else {

                                Toast.makeText(context, "Please complete your previous task first", Toast.LENGTH_SHORT).show();
                            }
                        }
                        catch (Exception e){
                            Log.d(TAG, "onDataChange: got the error while retrieving data = " + e.getMessage());
                            Toast.makeText(context, "Problem exist in your account.Got error while retrieving data from the server", Toast.LENGTH_SHORT).show();
                        }

                   }

                   @Override
                   public void onCancelled(@NonNull DatabaseError databaseError) {
                       Toast.makeText(context, "got an error", Toast.LENGTH_SHORT).show();
                   }
               });

           }
       });


        holder.carName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Connected){
                    Toast.makeText(context, "No INTERNET connection", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(SystemClock.elapsedRealtime() - lastClicked < toBeWait){
                    return;
                }
                lastClicked = SystemClock.elapsedRealtime();
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            if (dataSnapshot.getValue().toString().equals(cars.get(position)) || dataSnapshot.getValue().toString().equals("")) {

                                Toast.makeText(context, "number = " + holder.CarNumber.getText().toString(), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(context, StartCarCleaningActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra(context.getString(R.string.carNumber), holder.CarNumber.getText().toString());
                                intent.putExtra(context.getString(R.string.Area), Area);
                                context.startActivity(intent);
                            } else {

                                Toast.makeText(context, "Please complete your previous task first", Toast.LENGTH_SHORT).show();
                            }
                        }
                        catch (Exception e){
                            Log.d(TAG, "onDataChange: got the error while retrieving data = " + e.getMessage());
                            Toast.makeText(context, "Problem exist in your account.Got error while retrieving data from the server", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(context, "got an error", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        holder.timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Connected){
                    Toast.makeText(context, "No INTERNET connection", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(SystemClock.elapsedRealtime() - lastClicked < toBeWait){
                    return;
                }
                lastClicked = SystemClock.elapsedRealtime();
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            if (dataSnapshot.getValue().toString().equals(cars.get(position)) || dataSnapshot.getValue().toString().equals("")) {

                                Toast.makeText(context, "number = " + holder.CarNumber.getText().toString(), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(context, StartCarCleaningActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra(context.getString(R.string.carNumber), holder.CarNumber.getText().toString());
                                intent.putExtra(context.getString(R.string.Area), Area);
                                context.startActivity(intent);
                            } else {

                                Toast.makeText(context, "Please complete your previous task first", Toast.LENGTH_SHORT).show();
                            }
                        }
                        catch (Exception e){
                            Log.d(TAG, "onDataChange: got the error while retrieving data = " + e.getMessage());
                            Toast.makeText(context, "Problem exist in your account.Got error while retrieving data from the server", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(context, "got an error", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        holder.CarPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Connected){
                    Toast.makeText(context, "No INTERNET connection", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(SystemClock.elapsedRealtime() - lastClicked < toBeWait){
                    return;
                }
                lastClicked = SystemClock.elapsedRealtime();
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            if (dataSnapshot.getValue().toString().equals(cars.get(position)) || dataSnapshot.getValue().toString().equals("")) {

                                Toast.makeText(context, "number = " + holder.CarNumber.getText().toString(), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(context, StartCarCleaningActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra(context.getString(R.string.carNumber), holder.CarNumber.getText().toString());
                                intent.putExtra(context.getString(R.string.Area), Area);
                                context.startActivity(intent);
                            } else {

                                Toast.makeText(context, "Please complete your previous task first", Toast.LENGTH_SHORT).show();
                            }
                        }
                        catch (Exception e){
                            Log.d(TAG, "onDataChange: got the error while retrieving data = " + e.getMessage());
                            Toast.makeText(context, "Problem exist in your account.Got error while retrieving data from the server", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(context, "got an error", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }

    @Override
    public int getItemCount() {
        return cars.size();
    }




    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView CarNumber;
        private TextView carName;
        private ImageView CarPhoto;
        private TextView timer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            CarNumber = itemView.findViewById(R.id.carNumber);
            carName = itemView.findViewById(R.id.carName);
            CarPhoto = itemView.findViewById(R.id.carPhoto);
            timer = itemView.findViewById(R.id.timer);
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


}
