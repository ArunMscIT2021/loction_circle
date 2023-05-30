package com.example.loctioncircle;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LocationHelper.LocationListener {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private double TARGET_LATITUDE; // Target latitude
    private double TARGET_LONGITUDE; // Target longitude
    private static final float MAX_DISTANCE = 10; // Maximum distance in meters
    private final static int ALL_PERMISSIONS_RESULT = 101;

    private FusedLocationProviderClient fusedLocationClient;


    Button btngetlocation, btnupdate, btnDeveloper;
    LocationTrack locationTrack;

    private LocationHelper locationHelper;
    private ArrayList permissionsToRequest;
    private ArrayList permissionsRejected = new ArrayList();
    private ArrayList permissions = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btngetlocation = findViewById(R.id.btngetlocation);
        btnupdate = findViewById(R.id.btnupdate);
        btnDeveloper = findViewById(R.id.btnDeveloper);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnDeveloper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDebug();
            }
        });


        btnupdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLocationUpdates();

            }
        });
        btngetlocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLastKnownLocation();
            }
        });
        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);
        permissionsToRequest = findUnAskedPermissions(permissions);
        //get the permissions we have asked for before but are not granted..
        //we will store this in a global list to access later.


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            if (permissionsToRequest.size() > 0)
                requestPermissions((String[]) permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }


    }

    private void startLocationUpdates() {
        locationHelper = new LocationHelper(this, this);
        locationHelper.startLocationUpdates();
    }

    private ArrayList findUnAskedPermissions(ArrayList wanted) {
        ArrayList result = new ArrayList();

        for (Object perm : wanted) {
            if (!hasPermission((String) perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private void gpslocation() {

        locationTrack = new LocationTrack(this);


        if (locationTrack.canGetLocation()) {

            TARGET_LONGITUDE = locationTrack.getLongitude();
            TARGET_LATITUDE = locationTrack.getLatitude();

            Toast.makeText(getApplicationContext(), "Longitude:" + Double.toString(TARGET_LONGITUDE) + "\nLatitude:" + Double.toString(TARGET_LATITUDE), Toast.LENGTH_SHORT).show();
        } else {

            locationTrack.showSettingsAlert();
        }
    }

    private void getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            TARGET_LONGITUDE = longitude;
            TARGET_LATITUDE = latitude;
//
            Toast.makeText(getApplicationContext(), "Longitude:" + Double.toString(TARGET_LONGITUDE) + "\nLatitude:" + Double.toString(TARGET_LATITUDE), Toast.LENGTH_SHORT).show();
//
                            // Use the latitude and longitude as needed
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle the failure case
                    }
                });
    }

    public void checkDebug(){
        int developerOptions = Settings.Secure.getInt(getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0);
        if (developerOptions == 1) {
            Toast.makeText(this, "Developer option on", Toast.LENGTH_SHORT).show();
            // Developer Options are enabled
            // Perform desired actions here
        } else {
            Toast.makeText(this, "Developer option off", Toast.LENGTH_SHORT).show();

            // Developer Options are disabled
            // Perform desired actions here
        }

    }

    @Override
    public void onLocationReceived(Location location) {
        // Calculate the distance between the user's location and the target location
        float distance = location.distanceTo(getTargetLocation());

        if (distance <= MAX_DISTANCE) {
            btnupdate.setEnabled(true);
            // User is within the 500-meter radius
            Toast.makeText(this, "User is within "+MAX_DISTANCE+" meters", Toast.LENGTH_SHORT).show();
        } else {
            btnupdate.setEnabled(false);
            // User is outside the 500-meter radius
            Toast.makeText(this, "User is outside the "+MAX_DISTANCE+"-meter radius", Toast.LENGTH_SHORT).show();
        }
    }

    private Location getTargetLocation() {
        Location targetLocation = new Location("");
        targetLocation.setLatitude(TARGET_LATITUDE);
        targetLocation.setLongitude(TARGET_LONGITUDE);
        return targetLocation;
    }
    public boolean isDeviceRooted() {
        String[] knownRootPaths = {
                "/system/bin/su",
                "/system/xbin/su",
                "/sbin/su",
                "/system/su",
                "/system/app/Superuser.apk",
                "/data/local/xbin/su",
                "/data/local/bin/su",
                "/data/local/su",
                "/system/sbin/su",
                "/usr/bin/su",
                "/system/sd/xbin/su",
                "/system/bin/failsafe/su",
                "/data/local/",
                "/su/bin/su"
        };

        for (String path : knownRootPaths) {
            if (new File(path).exists()) {
                return true;
            }
        }

        return false;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationHelper != null) {
            locationHelper.stopLocationUpdates();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start location updates
                startLocationUpdates();
            } else {
                // Permission denied
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}