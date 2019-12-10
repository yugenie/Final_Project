package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class GameActivity extends FragmentActivity {

    private GoogleMap gameMap;
    public int gameTime;
    public int zombieNumber;
    public static LatLngBounds bounds = NewGameActivity.bounds;
    private Game game;

    private static final String TAG = "GameActivity";
    private boolean hasLocationPermission;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    public Location currentLocation;

    private List<Marker> zombies = new ArrayList<>();

    CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(theMap -> {
            gameMap = theMap;
            setUpMap();
            centerMap(gameMap);
            setUpGame();
        });

        gameTime = getIntent().getIntExtra("gameTime", 0);
        zombieNumber = getIntent().getIntExtra("zombieNumber", 0);

        // Android only allows location access to apps that asked for it and had the request approved by the user
        // See if we need to make a request
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // If permission isn't already granted, start a request
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            Log.i(TAG, "Asked for location permission");
            // The result will be delivered to the onRequestPermissionsResult function
        } else {
            Log.i(TAG, "Already had location permission");
            // If we have the location permission, start the location listener service
            hasLocationPermission = true;
            startLocationWatching();
        }

        Button cureButton = findViewById(R.id.cureZombie);
        cureButton.setOnClickListener(unused -> {
            updateLocation();
            int before = game.zombieMarkers.size();
            game.cureZombie(currentLocation);
            int after = game.zombieMarkers.size();
            if (game.playerState == 0) {
                gameOver();
            } else if (game.spray == 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Oops");
                builder.setMessage("Do not have the spray to cure.");
                builder.setNegativeButton("Back", null);
                builder.create().show();
            }  else if (before == after) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Oops");
                builder.setMessage("Not close enough to cure.");
                builder.setNegativeButton("Back", null);
                builder.create().show();
            }
        });

        long gameTimeInSecond = (long) gameTime * 60 * 1000;
        TextView timer = findViewById(R.id.timer);
        countDownTimer = new CountDownTimer(gameTimeInSecond, 1000) {
            @Override
            public void onTick(long l) {
                int min = (int) l / 60000;
                int sec = (int) (l - min * 60000) / 1000;
                System.out.println("timer working");
                timer.setText("Time remaining: " + Integer.toString(min) + ": " + Integer.toString(sec));
            }

            @Override
            public void onFinish() {
                gameOver();
            }
        };
        countDownTimer.start();

        Button endGame = findViewById(R.id.endGame);
        endGame.setOnClickListener(unused -> {
            endGame();
        });
    }

    /**
     * Sets up the Google map.
     */
    @SuppressWarnings("MissingPermission")
    private void setUpMap() {
        Log.i(TAG, "Entered setUpMap");
        if (hasLocationPermission) {
            // Can only enable the blue My Location dot if the location permission is granted
            gameMap.setMyLocationEnabled(true);
            Log.i(TAG, "setUpMap enabled My Location");
        }

        // Disable some extra UI that gets in the way
        gameMap.getUiSettings().setIndoorLevelPickerEnabled(false);
        gameMap.getUiSettings().setMapToolbarEnabled(false);

        // This function is no longer responsible for rendering game-specific elements
        // That's taken care of by the Game subclasses
    }

    private void centerMap(final GoogleMap map) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    System.out.println(location.getLatitude());
                    System.out.println(location.getLongitude());
                    gameMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                }
            }
        });
    }

    /**
     * Called by the Android system when a permissions request receives a response from the user.
     * @param requestCode the ID of the request (always 0 in our case)
     * @param permissions the affected permissions' names
     * @param grantResults whether each permission was granted (corresponds to the permissions array)
     */
    @Override
    @SuppressLint("MissingPermission")
    public void onRequestPermissionsResult(final int requestCode, final @NonNull String[] permissions,
                                           final @NonNull int[] grantResults) {
        Log.i(TAG, "Permission request result received");
        // The "super" call is required so that the notification will be delivered to fragments
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check whether the request was approved by the user
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted by the user");
            // Got the location permission for the first time
            hasLocationPermission = true;
            // Enable the My Location blue dot on the map
            if (gameMap != null) {
                Log.i(TAG, "onRequestPermissionsResult enabled My Location");
                gameMap.setMyLocationEnabled(true);
            }
            // Start the location listener service
            startLocationWatching();
        }
    }

    /**
     * Starts watching for location changes if possible under the current permissions.
     */
    @SuppressWarnings("MissingPermission")
    private void startLocationWatching() {
        Log.i(TAG, "Starting location watching");
        // Make sure the location permission has been granted
        if (!hasLocationPermission) {
            Log.w(TAG, "startLocationWatching: Missing permission");
            return;
        }
        // Make sure the My Location blue dot on the map is enabled
        if (gameMap != null) {
            gameMap.setMyLocationEnabled(true);
            Log.i(TAG, "startLocationWatching enabled My Location");
        }

        // Keep the screen on even if not touched in a while
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        updateLocation();
    }

    private void updateLocation() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    currentLocation = location;
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    gameMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    game.getLocation(currentLocation);
                    if (game.zombieMarkers.size() == 0) {
                        gameWin();
                    }
                }
            };
        };
        locationRequest = new LocationRequest().setInterval(600000); // one minute interval
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            }
        });
    }

    private void setUpGame() {
        game = new Game(gameMap, bounds, gameTime, zombieNumber);
    }

    private void gameOver() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Game Over.");
        if (game.playerState == 0) {
            builder.setMessage("You are infected.");
        } else {
            builder.setMessage("Time out.");
        }
        builder.setNegativeButton("Back", (unused1, unused2) -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
        builder.create().show();
    }

    private void endGame() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to end the game? This cannot be undone.");
        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton("End Game", (unused1, unused2) -> {
            countDownTimer.cancel();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
        builder.create().show();
    }

    private void gameWin() {
        countDownTimer.cancel();
        int id = getResources().getIdentifier("vic", "raw", getPackageName());
        MediaPlayer song = MediaPlayer.create(getApplicationContext(), id);
        song.start();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You Win.");
        builder.setNegativeButton("Back", (unused1, unused2) -> {
            song.stop();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
        builder.create().show();
    }
}
