package com.example.fakegpsdetection;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    LocationValidator locationValidator = new LocationValidator(MainActivity.this);
    private final MutableLiveData<Location> myLocation = new MutableLiveData<>();
    private FusedLocationProviderClient fusedLocationProviderClient;

    private TextView mockLocationStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        mockLocationStatus = findViewById(R.id.tvMockLocationStatus);
        Button mockLocationButton = findViewById(R.id.btnCheckMockLocation);

        mockLocationButton.setOnClickListener(view -> checkFakeGps(fusedLocationProviderClient));
    }

    private boolean checkLocationPermission() {
        //check if gps is enabled or not and then request use to enable it
        return ActivityCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void checkFakeGps(FusedLocationProviderClient fusedLocationProviderClient) {
        if (!checkLocationPermission()) {
            Toast.makeText(MainActivity.this, "Permission Location Denied", Toast.LENGTH_SHORT).show();
        } else {
            getDeviceLocation(fusedLocationProviderClient);
        }
    }

    @SuppressLint("MissingPermission")
    private void getDeviceLocation(FusedLocationProviderClient fusedLocationProviderClient) {
        Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
        locationResult.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() == null) {
                    handleNewLocation(fusedLocationProviderClient);
                } else {
                    myLocation.setValue(task.getResult());
                    mockLocationStatus.setText(Objects.requireNonNull(myLocation.getValue()).isFromMockProvider() ? "Fake location" : "Real location");
                    locationValidator.isFakeLocation(myLocation.getValue());
                }
            } else {
                handleNewLocation(fusedLocationProviderClient);
            }
        });
    }

    /** @noinspection CallToPrintStackTrace*/
    @SuppressLint("MissingPermission")
    private void handleNewLocation(FusedLocationProviderClient fusedLocationProviderClient) {
        new Thread(() -> {
            try {
                int priority = Priority.PRIORITY_HIGH_ACCURACY;
                Task<Location> task = fusedLocationProviderClient.getCurrentLocation(
                        priority,
                        new CancellationTokenSource().getToken()
                );
                Location result = Tasks.await(task);
                if (result != null) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        myLocation.setValue(result);
                        mockLocationStatus.setText(Objects.requireNonNull(myLocation.getValue()).isFromMockProvider() ? "Fake location" : "Real location");
                        locationValidator.isFakeLocation(myLocation.getValue());
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}