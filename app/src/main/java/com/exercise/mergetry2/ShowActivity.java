package com.exercise.mergetry2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ShowActivity extends AppCompatActivity {

    private TextView markerID;
    private Bundle extras;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference dbRefPlace = firebaseDatabase.getReference("Places");
//    DatabaseReference dbRefUser = firebaseDatabase.getReference("Users");
//    DatabaseReference dbRefPost = firebaseDatabase.getReference("Post");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        markerID = findViewById(R.id.markerID);
        extras = getIntent().getExtras();

        markerID.setText(extras.getString("ID"));
    }
}