package com.example.driver;



public class UserActivity  {
    private double Capacity;
    private double Latitude;
    private double Longitude;

    public UserActivity() {
    }

    public UserActivity(double capacity, double latitude, double longitude) {
        this.Capacity = capacity;
        this.Latitude = latitude;
        this.Longitude = longitude;
    }

    public double getCapacity() {
        return Capacity;
    }

    public void setCapacity(double capacity) {
        this.Capacity = capacity;
    }

    public double getLatitude() {
        return Latitude;
    }

    public void setLatitude(double latitude) {
        this.Latitude = latitude;
    }

    public double getLongitude() {
        return Longitude;
    }

    public void setLongitude(double longitude) {
        this.Longitude = longitude;
    }
}

