package com.exercise.mergetry2;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ShowActivity extends AppCompatActivity {

    private TextView markerID, typeText;
    private Bundle extras;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference dbRefPlace = firebaseDatabase.getReference("Places");
//    DatabaseReference dbRefUser = firebaseDatabase.getReference("Users");
//    DatabaseReference dbRefPost = firebaseDatabase.getReference("Post");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        extras = getIntent().getExtras();
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_dialog);
        markerID = dialog.findViewById(R.id.posterName);
        typeText = dialog.findViewById(R.id.textActivity);

        markerID.setText(extras.getString("ID"));
    }
}