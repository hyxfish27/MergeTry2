package com.exercise.mergetry2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;

import com.exercise.mergetry2.Controller.DAUPlace;
import com.exercise.mergetry2.Model.Place;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener {

    private static final String TAG = "MapsActivity";

    private final HashMap<String, Marker> markers = new HashMap<>();

    private GoogleMap mMap;
    View mapView;
    private Geocoder geocoder;

    private final int ACCESS_LOCATION_REQUEST_CODE = 10001;
    FusedLocationProviderClient fusedLocationProviderClient;

    DAUPlace dauPlace = new DAUPlace();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference dbRefPlace = firebaseDatabase.getReference("Place");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        assert mapFragment != null;
        mapView = mapFragment.getView();
        mapFragment.getMapAsync(this);
        geocoder = new Geocoder(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        GoogleMapOptions googleMapOptions = new GoogleMapOptions();
        googleMapOptions.mapType(GoogleMap.MAP_TYPE_NORMAL)
                .zoomControlsEnabled(false)
                .tiltGesturesEnabled(false)
                .scrollGesturesEnabled(true)
                .rotateGesturesEnabled(true)
                .zoomGesturesEnabled(true)
                .compassEnabled(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onMapReady(@NotNull GoogleMap googleMap) {

        ImageButton myLocationBtn = findViewById(R.id.myLocationBtn);
        myLocationBtn.setOnClickListener(v -> zoomToUserLocation());

        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);

        // Monitoring the DB Activity
        dbRefPlace.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NotNull DataSnapshot dataSnapshot, String s) {
                Place place = dataSnapshot.getValue(Place.class);
                // stash the key in the title, for recall later
                if (place != null) {
                    int activity_type = place.getType();
                    int width = 300;
                    int height = 300;
                    BitmapDrawable bitmapDrawable;

                    switch (activity_type) {
                        case 1:
                            bitmapDrawable = (BitmapDrawable) ResourcesCompat.getDrawable(getResources(),R.drawable.icon_charity_3dmarker,null);
                            break;
                        case 2:
                            bitmapDrawable = (BitmapDrawable)ResourcesCompat.getDrawable(getResources(),R.drawable.icon_course_3dmarker,null);
                            break;
                        case 3:
                            bitmapDrawable = (BitmapDrawable)ResourcesCompat.getDrawable(getResources(),R.drawable.icon_discount_3dmarker,null);
                            break;
                        case 4:
                            bitmapDrawable = (BitmapDrawable)ResourcesCompat.getDrawable(getResources(),R.drawable.icon_leisure_3dmarker,null);
                            break;
                        default:
                            bitmapDrawable = (BitmapDrawable)ResourcesCompat.getDrawable(getResources(),R.drawable.icon_sport_3dmarker,null);
                    }
                    assert bitmapDrawable != null;
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    Bitmap bitmapMarker = Bitmap.createScaledBitmap(bitmap, width, height, false);

                    Marker myMarker = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(place.getPlatitude(), place.getPlongitude()))
                            .icon(BitmapDescriptorFactory.fromBitmap(bitmapMarker)));

                    myMarker.setTag(dataSnapshot.getKey());
                    dbRefPlace.child(dataSnapshot.getKey()).child("markerID").setValue(dataSnapshot.getKey());

                    // cache the marker locally
                    markers.put(dataSnapshot.getKey(), myMarker);
                }
            }

            @Override
            public void onChildChanged(@NotNull DataSnapshot dataSnapshot, String s) {
                Place place = dataSnapshot.getValue(Place.class);

                // Move markers on the map if changed on Firebase
                Marker changedMarker = markers.get(dataSnapshot.getKey());
                assert changedMarker != null;
                assert place != null;
                changedMarker.setPosition(new LatLng(place.getPlatitude(), place.getPlongitude()));
            }

            @Override
            public void onChildRemoved(@NotNull DataSnapshot dataSnapshot) {
                //When markers are removed from
                if (markers.containsKey(dataSnapshot.getKey())) {
                    Marker deadMarker = markers.get(dataSnapshot.getKey());
                    if (deadMarker != null) {
                        Log.d(TAG, "onChildRemoved: ");
                        deadMarker.remove();
                        markers.remove(dataSnapshot.getKey());
                    } else {
                        Log.d(TAG, "deadMarker is null");
                    }
                } else {
                    Log.d(TAG, "onChildRemoved: ERROR");
                }
                Log.d(TAG, "onChildRemoved: Done");
            }

            @Override
            public void onChildMoved(@NotNull DataSnapshot dataSnapshot, String s) {
                // This won't happen to our simple list, but log just in case
                Log.v(TAG, "moved !" + dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                // Ignore cancelations (but log just in case)
                Log.v(TAG, "canceled!" + databaseError.getMessage());
            }
        });

        // Enable and Zoom to User Location if permission is accessed
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            enableUserLocation();
            zoomToUserLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    ACCESS_LOCATION_REQUEST_CODE);
        }
    }

    @Override
    public void onMapLongClick(@NonNull @NotNull LatLng latLng) {
        int type = (int)(Math.random() * 5 + 1);
        Place place = new Place(latLng.latitude, latLng.longitude, type);
        dauPlace.add(place).addOnSuccessListener(unused -> {
            Log.d(TAG, "onSuccess: Add place successfully!!");
            Toast.makeText(MapsActivity.this,"Successfully",Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> Log.d(TAG, "onFailure: Failed to add place..."));
    }

    @Override
    public boolean onMarkerClick(@NonNull @NotNull Marker marker) {
//        dauPlace = new DAUPlace();
//        dauPlace.remove(marker.getTitle())
//                .addOnSuccessListener(unused -> Toast.makeText(MapsActivity.this,"Successfully deleted",Toast.LENGTH_SHORT).show())
//                .addOnFailureListener(e -> Toast.makeText(MapsActivity.this,"Failed delete place...",Toast.LENGTH_SHORT).show());
        Intent intent = new Intent(MapsActivity.this, ShowActivity.class);
        intent.putExtra("ID",marker.getTag().toString());
        startActivity(intent);
        return false;
    }


    //  Return the permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACCESS_LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation();
                zoomToUserLocation();
            }  //We can show a dialog that permission is not granted

        }
    }

    // Check Permission Activity
    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    // Zoom to user's place
    private void zoomToUserLocation() {
        //Move camera to user's location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(location -> {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)
                    .tilt(70)
                    .zoom(16)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        });
        locationTask.addOnFailureListener(e -> Log.d(TAG, "onFailure: zoomToUserLocation Failed"));

    }
}