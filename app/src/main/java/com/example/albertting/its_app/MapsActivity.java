package com.example.albertting.its_app;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    GoogleMap mMap;
    ArrayList<LatLng> points;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    Location oldLocation;
    private Marker mCurrLocationMarker;

    private final LatLng mDefaultLocation = new LatLng(-122.084, 37.422); //Googleplex
    private boolean mLocationPermissionGranted;
    private boolean mRequestingLocationUpdates;
    private static final long LOCATION_REQUEST_INTERVAL = 2000;
    private static final long LOCATION_REQUEST_FASTEST_INTERVAL = 1000;
    DecimalFormat df;
    Button pullBtn;
    TextView BSSID;
    TextView SSID;
    TextView IPAddress;
    TextView RSSI;
    TextView LinkSpeed;
    TextView NetworkID;
    TextView Coords;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        points = new ArrayList<LatLng>();
        df = new DecimalFormat("0.00");
        mRequestingLocationUpdates = true;

        pullBtn = (Button) findViewById(R.id.pull);

        BSSID = (TextView)findViewById(R.id.BSSID);
        BSSID.setText("BSSID: ");
        SSID = (TextView)findViewById(R.id.SSID);
        SSID.setText("SSID: ");
        IPAddress = (TextView)findViewById(R.id.IPAddress);
        IPAddress.setText("IPAddress: ");
        RSSI = (TextView)findViewById(R.id.RSSI);
        RSSI.setText("RSSI: ");
        LinkSpeed = (TextView)findViewById(R.id.LinkSpeed);
        LinkSpeed.setText("LinkSpeed: ");
        Coords = (TextView)findViewById(R.id.Coords);
        Coords.setText("Lat: " + "     Long: " + "     Alt: ");

        oldLocation= new Location("dummy data");

        buildGoogleApiClient();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        initMap();
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        centerMap();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setSmallestDisplacement(20);
        mLocationRequest.setInterval(LOCATION_REQUEST_INTERVAL);
        mLocationRequest.setFastestInterval(LOCATION_REQUEST_FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    protected void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        oldLocation = location;
        centerMap();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initMap();
                } else {
                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
            // other 'case' lines to check for other permissions this app might request.
            //You can add here other case statements according to your requirement.
        }
    }



    private void initMap() {
        if (mMap == null) {
            return;
        }

        //Request location permission from user so we can pull location
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;

            if (mRequestingLocationUpdates) {
                createLocationRequest();
                startLocationUpdates();
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }

        //Gets the most recent location of the device
        if (mLocationPermissionGranted) {
            oldLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            oldLocation = null;
        }

        // Set the map's camera position to the current location of the device.
        // If it can't be found, defaults to GooglePlex
        if (oldLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(oldLocation.getLatitude(),
                            oldLocation.getLongitude()), 15));
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, 15));
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    //Centers the map on the device
    private void centerMap() {
        if (oldLocation != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(oldLocation.getLatitude(),
                            oldLocation.getLongitude()), 15));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    //sorts between start and end tracking
    public void pull(View view) {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        String sBSSID = wifiInfo.getBSSID();
        String sSSID = wifiInfo.getSSID();
        int ip = wifiInfo.getIpAddress();
        int iRSSI = wifiInfo.getRssi();
        int iLinkSpeed = wifiInfo.getLinkSpeed();
        double lat = oldLocation.getLatitude();
        double lon = oldLocation.getLongitude();
        double alt = oldLocation.getAltitude();

        BSSID.setText("BSSID: " + sBSSID);
        SSID.setText("SSID: " + sSSID);
        IPAddress.setText("IP: " + String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff),
                (ip >> 16 & 0xff), (ip >> 24 & 0xff)));
        RSSI.setText("RSSI: " + iRSSI + " dBm");
        LinkSpeed.setText("LinkSpeed: " + iLinkSpeed + " Mbps");

        Coords.setText("Lat: " + lat + "     Long: " + lon + "     Alt: " + alt + " m");

    }
}