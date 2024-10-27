package com.example.fakegpsdetection;

import android.content.Context;
import android.location.Location;
import android.provider.Settings;
import android.widget.Toast;

public class LocationValidator {

    private final Context context;

    public LocationValidator(Context context) {
        this.context = context;
    }

    public void isFakeLocation(
            Location location
//            Location previousLocation
    ) {
        if (location == null) return;

        // Check if location is from mock provider
        if (isMockLocation(location)) {
            Toast.makeText(context, "Mock location detected!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if Developer Mode is enabled
        if (isDeveloperModeEnabled(context)) {
            Toast.makeText(context, "Developer mode is enabled. Higher chance of fake location.", Toast.LENGTH_SHORT).show();
        }

        // Check for suspicious location changes (optional, based on previous location)
        /*
        if (isLocationSuspicious(location, previousLocation)) {
            Toast.makeText(context, "Location changes seem suspicious!", Toast.LENGTH_SHORT).show();
            return true;
        }
         */

    }

    private boolean isMockLocation(Location location) {
        return location != null && location.isFromMockProvider();
    }

    private boolean isDeveloperModeEnabled(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(),
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) != 0;
    }

    /*
    private boolean isLocationSuspicious(Location location, Location previousLocation) {
        if (previousLocation == null) return false;

        float speed = location.getSpeed();
        float distance = location.distanceTo(previousLocation);
        float timeDiff = (location.getTime() - previousLocation.getTime()) / 1000.0f;

        return speed > 83 || (distance > 1000 && timeDiff < 5); // Example conditions
    }
     */
}