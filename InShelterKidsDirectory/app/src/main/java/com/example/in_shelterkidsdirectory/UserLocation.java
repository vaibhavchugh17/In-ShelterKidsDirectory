//UserLovcation
package com.example.dlpbgj;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.common.internal.safeparcel.SafeParcelable.NULL;

public class UserLocation extends FragmentActivity implements OnMapReadyCallback {
    LocationManager LocM;
    LocationListener LocL;
    LatLng UserLongLat;
    Button select;
    private GoogleMap mMap;
    private Geocoder geocoder;
    int LOCATIONRC = 10001;
    FusedLocationProviderClient FPC;
    String returnAddress;
    Marker marker;
    String flag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_location);
        flag = (String) getIntent().getSerializableExtra("Flag");
        select = findViewById(R.id.getLocation);
        if (!flag.equals("None")){
            select.setText("Go Back");
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        geocoder = new Geocoder(this);
        FPC = LocationServices.getFusedLocationProviderClient(this);

        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flag.equals("None")) {
                    Toast toast = Toast.makeText(view.getContext(), "The selected location is " + returnAddress, Toast.LENGTH_SHORT);
                    toast.show();
                    Intent intent = new Intent();
                    intent.putExtra("Location", returnAddress);
                    setResult(-1, intent);
                }
                finish();
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (flag.equals("None")){
                    try {
                        List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                        if (addressList.size() > 0) {
                            Address tempAddress = addressList.get(0);
                            String tempAddressString = tempAddress.getAddressLine(0);
                            returnAddress = tempAddressString;
                            marker.setPosition(latLng);
                            marker.setTitle(tempAddressString);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
            }

            @Override
            public void onMarkerDrag(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                if (flag.equals("None")){
                    LatLng markerPosition = marker.getPosition();
                    try {
                        List<Address> addressList = geocoder.getFromLocation(markerPosition.latitude, markerPosition.longitude, 1);
                        if (addressList.size() > 0) {
                            Address tempAddress = addressList.get(0);
                            String tempAddressString = tempAddress.getAddressLine(0);
                            returnAddress = tempAddressString;
                            marker.setTitle(tempAddressString);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLastLocation();
        } else {
            askLocPermision();
        }
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Task<Location> LocationTask = FPC.getLastLocation();
        LocationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (!flag.equals("None")){
                    try {
                        List<Address> geoAddress = geocoder.getFromLocationName(flag, 1);
                        if (geoAddress.size() > 0) {
                            Address address = geoAddress.get(0);
                            LatLng newLoc = new LatLng(address.getLatitude(), address.getLongitude());
                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(new LatLng(address.getLatitude(), address.getLongitude()))
                                    .title(flag)
                                    .draggable(false);
                            mMap.addMarker(markerOptions);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLoc, 16));
                        }
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }
                }
                else{
                    if(location != null){
                        marker = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(location.getLatitude(),location.getLongitude()))
                                .title("Current Location")
                                .draggable(true));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()), 16));
                        try {
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            if (addresses.size() > 0) {
                                Address tempAddress = addresses.get(0);
                                String tempAddressString = tempAddress.getAddressLine(0);
                                returnAddress = tempAddressString;
                            }
                        } catch (IOException e) {
                            Log.d("UserLocation", e.getMessage());
                        }

                    }
                }

            }
        });
        LocationTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }
    private void askLocPermision(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATIONRC );
            }else{
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATIONRC );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == LOCATIONRC){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getLastLocation();
            }
        }
    }
}