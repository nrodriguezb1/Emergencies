package dte.masteriot.mdp.emergencies;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;


public class MapsActivity<lineOptions> extends FragmentActivity implements OnMapReadyCallback {

    // Variables
    private GoogleMap mMap;
    // Change View Variables
    private RadioButton mapita, hybrid, satellite;
    // Camera Cord. after click
    double Camlatitude,Camlongitude;
    String coordenadas;
    // Creation of Polyline
    PolylineOptions PlyOpt = null;
    Marker CurrentPosM;

    //LocationServices Vars : Current Location
    private FusedLocationProviderClient mFusedLocationClient;

    double wayLatitude = 0.0, wayLongitude = 0.0;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private boolean isContinue = false;
    private boolean isGPS = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mapita = (RadioButton)findViewById(R.id.mapita);
        hybrid = (RadioButton)findViewById(R.id.hybrid);
        satellite = (RadioButton)findViewById(R.id.satellite);
//Get Values of location after click
        coordenadas =  getIntent().getStringExtra("coordinates");

//Get Current Location Init
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10 * 1000); // 10 seconds
        locationRequest.setFastestInterval(5 * 1000); // 5 seconds

// Ask User to turn On GPS
        new GpsUtils(this).turnGPSOn(new GpsUtils.onGpsListener() {
            @Override
            public void gpsStatus(boolean isGPSEnable) {
                // turn on GPS
                isGPS = isGPSEnable;
            }
        });

// C Location
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        wayLatitude = location.getLatitude();
                        wayLongitude = location.getLongitude();

                        if (mFusedLocationClient != null) {
                            mFusedLocationClient.removeLocationUpdates(locationCallback);
                        }
                    }
                }
            }
        };

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (!isGPS) {
            Toast.makeText(this, "Please turn on GPS", Toast.LENGTH_SHORT).show();
            return;
        }


    }

// Change View

    public void modoMapa(View view){
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (!isGPS) {
            Toast.makeText(this, "Please turn on GPS", Toast.LENGTH_SHORT).show();
            return;
        }
        getLocation();
    }
    public void modoSatellite(View view){
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        if (!isGPS) {
            Toast.makeText(this, "Please turn on GPS", Toast.LENGTH_SHORT).show();
            return;
        }
        getLocation();
    }
    public void modoHybrid(View view){
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        if (!isGPS) {
            Toast.makeText(this, "Please turn on GPS", Toast.LENGTH_SHORT).show();
            return;
        }
        getLocation();
    }

    private void getLocation() {

        // Get Camera Location
        Camlongitude = Double.parseDouble(coordenadas.substring(1, 15));
        Camlatitude = Double.parseDouble(coordenadas.substring(19, 32));
        Log.d("URLCNX", String.format(Locale.US, "%s - %s", Camlatitude, Camlongitude));
        // Get Current Location
        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        mFusedLocationClient.getLastLocation().addOnSuccessListener(MapsActivity.this, location ->
        {
            if (location != null)
            {
                wayLatitude = location.getLatitude();
                wayLongitude = location.getLongitude();
                LatLng CurrentPos = new LatLng(wayLatitude,wayLongitude);
                LatLng CamPos = new LatLng(Camlatitude, Camlongitude);
                Log.d("URLCNX", String.format(Locale.US, "%s - %s", wayLatitude, wayLongitude));
                //txtLocation.setText(String.format(Locale.US, "%s - %s", wayLatitude, wayLongitude));
                CurrentPosM = mMap.addMarker(new MarkerOptions()
                        .position(CurrentPos)
                        .title("Current Position"));

//Markers Creation
                LatLng coordenadas_camara = new LatLng(Camlatitude, Camlongitude);
                mMap.addMarker(new MarkerOptions().position(coordenadas_camara).title("Camera Position"));


//Camera Centering using both Markers :
                LatLngBounds latLngBounds = new LatLngBounds.Builder()
                        .include(CurrentPos)
                        .include(CamPos)
                        .build();

                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 190));

// Build Trajectory using AsynckTask Call
                DownloadWebPageTask task = new DownloadWebPageTask();
                task.execute(String.format(Locale.US, "%s",wayLatitude),String.format(Locale.US, "%s",wayLongitude),String.format(Locale.US, "%s",Camlatitude),String.format(Locale.US, "%s",Camlongitude));

            }
        }
        );
    }

        @Override
        public void onMapReady (GoogleMap googleMap){
            mMap = googleMap;
            getLocation();
            mapita.setChecked(true);
        }
// Permission Check
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1000: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (isContinue) {
                        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    } else {
                        mFusedLocationClient.getLastLocation().addOnSuccessListener(MapsActivity.this, location -> {
                            if (location != null) {
                                wayLatitude = location.getLatitude();
                                wayLongitude = location.getLongitude();
                                Log.d("URLCNX", String.format(Locale.US, "%s - %s", wayLatitude, wayLongitude));

                            } else {
                                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                            }
                        });
                    }
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppConstants.GPS_REQUEST) {
                isGPS = true; // flag maintain before get location
            }
        }
    }

// Path using YOURS Location API

    private class DownloadWebPageTask extends AsyncTask<String, Void, String> {
        private String contentType = "";

        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            String strUrl = "http://www.yournavigation.org/api/1.0/gosmore.php?format=kml&flat="+urls[0]+"&flon="+urls[1]+"&tlat="+urls[2]+"&tlon="+urls[3]+"&v=motorcar&fast=1&layer=mapnik&instructions=0";
            Log.d("URLCNX", strUrl.toString());
            HttpURLConnection urlConnection = null;

            try {
                URL url = new URL( strUrl );
                urlConnection = (HttpURLConnection) url.openConnection();
                contentType = urlConnection.getContentType();
                InputStream is = urlConnection.getInputStream();

                if ( contentType.toString()
                        .contains("text/xml; charset=utf-8") )
                {
                    InputStreamReader reader = new InputStreamReader( is );
                    BufferedReader in = new BufferedReader( reader );
                    String line = in.readLine();
                    while ( line != null ) {
                        response += line + "\n";
                        line = in.readLine();
                    }
                }
                else {
                    response = contentType + " not processed";
                    Log.d("Down", "Not Processed");
                }

                urlConnection.disconnect();

            } catch (Exception e) {
                response = e.toString();

            }

            return response;
        }

// Result Processing
        @Override
        protected void onPostExecute(String result) {
            String[] arrayString = ((result.split("<coordinates>"))[1].split("</coordinates>"))[0].split("\n");
            Log.d("Struct", result.toString());
            // Initial Poly
            PlyOpt = new PolylineOptions();
            PolylineOptions lp = new PolylineOptions();

            for(int x = 0; x <arrayString.length-1; x++){
                String [] sLatLong = arrayString[x].split(",");
                LatLng position = new LatLng(Double.parseDouble(sLatLong[1]), Double.parseDouble(sLatLong[0]));
                lp.add(position);
                lp.width(5);
                lp.color(Color.BLUE);
                PlyOpt.add(position);
                PlyOpt.width(5);
                Log.d("Lp Cord",position.toString());
            }
            mMap.addPolyline(lp);

        }
    }

}

// End of Code !!






