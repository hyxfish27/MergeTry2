package com.exercise.mergetry2.Controller;

import com.exercise.mergetry2.Model.Place;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.HashMap;

public class DAUPlace {

    private DatabaseReference databaseReference;

    public DAUPlace () {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("Place");
    }

    public Task<Void> add(Place place) {
        return databaseReference.push().setValue(place);
    }

    public Task<Void> update(String key, HashMap<String, Object> hashMap) {
        return databaseReference.child(key).updateChildren(hashMap);
    }

    public Task<Void> remove(String key){
        return databaseReference.child(key).removeValue();
    }

    public Query get() {
        return databaseReference;
    }
}
