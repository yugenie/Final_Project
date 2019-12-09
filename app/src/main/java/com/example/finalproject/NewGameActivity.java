package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnSuccessListener;

public class NewGameActivity extends AppCompatActivity {

    public GoogleMap gameMap;
    public static int gameTime;
    public static int zombieNumber;
    public static LatLngBounds bounds;

    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(newMap -> {
            gameMap = newMap;
            centerMap(gameMap);
        });

        Button createGame = findViewById(R.id.createGame);
        createGame.setOnClickListener(unused -> createGameClicked());
    }

    private void centerMap(final GoogleMap map) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                }
            }
        });
    }

    private void createGameClicked() {
        EditText setTime = findViewById(R.id.setTime);
        EditText setZombieNumber = findViewById(R.id.setZombieNumber);
        String getTime = setTime.getText().toString();
        String getZombieNumber = setZombieNumber.getText().toString();
        if (!getTime.isEmpty() && !getZombieNumber.isEmpty()) {
            gameTime = Integer.parseInt(getTime);
            zombieNumber = Integer.parseInt(getZombieNumber);

            bounds = gameMap.getProjection().getVisibleRegion().latLngBounds;

            Intent intent = new Intent(this, GameActivity.class);
            startActivity(intent);
            finish();
        }
    }
}