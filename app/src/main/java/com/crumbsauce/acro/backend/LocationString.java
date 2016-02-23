package com.crumbsauce.acro.backend;

import android.location.Location;

public class LocationString {
    public String location;

    public static LocationString fromLocation(Location loc) {
        if (loc != null) {
            String locationString = String.format("%s, %s",
                    String.valueOf(loc.getLatitude()),
                    String.valueOf(loc.getLongitude()));
            return new LocationString(locationString);
        } else {
            return null;
        }
    }

    public LocationString(String loc) {
        location = loc;
    }
}
