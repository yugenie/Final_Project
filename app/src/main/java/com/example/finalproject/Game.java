package com.example.finalproject;

import android.location.Location;
import android.location.LocationManager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game {

    private GoogleMap gameMap;
    private LatLngBounds bounds;
    private int gameTime;
    private int zombieNumber;
    public int spray = 0;
    private int range = 15;
    private int safeRange = 10;
    public int playerState = 1;

    private LatLng sprayLatlng;
    private Marker sprayMarker;

    private Location location;
    private int remainingZombie;

    public List<Marker> zombieMarkers = new ArrayList<>();
    //public MarkerOptions spray;

    public Game(final GoogleMap setMap, final LatLngBounds setBounds, final int setTime, final int setZombieNumber) {
        gameMap = setMap;
        bounds = setBounds;
        gameTime = setTime;
        zombieNumber = setZombieNumber;
        remainingZombie = setZombieNumber;

        setSpray(bounds);
        setZombies(zombieNumber, bounds);
    }

    private void setZombies(int zm, LatLngBounds b) {
        LatLng southwest = b.southwest;
        LatLng northeast = b.northeast;
        double south = southwest.latitude;
        double west = southwest.longitude;
        double north = northeast.latitude;
        double east = northeast.longitude;
        Random r = new Random();
        for (int i = 0; i < zm; i++) {
            double random1 = r.nextDouble();
            double latitude = random1 * (north - south) + south;
            double random2 = r.nextDouble();
            double longitude = random2 * (east - west) + west;
            LatLng latLng = new LatLng(latitude, longitude);
            MarkerOptions options = new MarkerOptions().position(latLng);
            Marker marker = gameMap.addMarker(options);
            zombieMarkers.add(marker);
        }
    }

    private void setSpray(LatLngBounds b) {
        LatLng southwest = b.southwest;
        LatLng northeast = b.northeast;
        double south = southwest.latitude;
        double west = southwest.longitude;
        double north = northeast.latitude;
        double east = northeast.longitude;
        Random r = new Random();
        double random1 = r.nextDouble();
        double latitude = random1 * (north - south) + south;
        double random2 = r.nextDouble();
        double longitude = random2 * (east - west) + west;
        sprayLatlng = new LatLng(latitude, longitude);
        MarkerOptions options = new MarkerOptions().position(sprayLatlng);
        sprayMarker = gameMap.addMarker(options);
        BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
        sprayMarker.setIcon(icon);
    }

    public void getLocation(Location l) {
        location = l;
        System.out.println("Get it.");
        getSpray(l, sprayLatlng);
    }

    private void getSpray(Location l, LatLng s) {
        Location sl = new Location(LocationManager.GPS_PROVIDER);
        sl.setLatitude(s.latitude);
        sl.setLongitude(s.longitude);
        double distance = l.distanceTo(sl);
        if (distance <= range) {
            spray = zombieNumber;
            sprayMarker.remove();
        }
    }

    public void cureZombie(Location l) {
        for (Marker marker: zombieMarkers) {
            LatLng zombie = marker.getPosition();
            Location zombieLocation = new Location(LocationManager.GPS_PROVIDER);
            zombieLocation.setLatitude(zombie.latitude);
            zombieLocation.setLongitude(zombie.longitude);
            double distance = l.distanceTo(zombieLocation);
            if (distance <= safeRange) {
                playerState = 0;
            }
            if (distance <= range && spray != 0) {
                marker.remove();
                spray--;
                remainingZombie--;
                zombieMarkers.remove(marker);
            }
        }
    }
}
