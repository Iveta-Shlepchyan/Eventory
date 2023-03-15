package com.example.eventory.models;

import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;
import java.util.ArrayList;

public class SerializableGeoPoint implements Serializable {
    private double latitude;
    private double longitude;
    private String address;
    private ArrayList<CardModel> events;
    private String category;

    public SerializableGeoPoint(GeoPoint geoPoint) {
        if (geoPoint != null) {
            this.latitude = geoPoint.getLatitude();
            this.longitude = geoPoint.getLongitude();
        }
    }

    public SerializableGeoPoint(GeoPoint geoPoint, String address, String category) {
        if (geoPoint != null) {
            this.latitude = geoPoint.getLatitude();
            this.longitude = geoPoint.getLongitude();
            this.address = address;
            this.category = category;
        }
    }

    public String getCategory() {
        return category;
    }

    public String getAddress(){
        return address;
    }

    public GeoPoint getGeoPoint() {
        return new GeoPoint(latitude, longitude);
    }

    public ArrayList<CardModel> getEvents() {
        return events;
    }

    public void setEvents(ArrayList<CardModel> events) {
        this.events = events;
    }
}
