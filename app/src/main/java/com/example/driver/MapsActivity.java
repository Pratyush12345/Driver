package com.example.driver;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.support.v7.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.opencensus.trace.export.SpanExporter;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private FirebaseDatabase database;
    private DatabaseReference mRef, mdriver,mNextRef,mgetkey;
    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private Marker currentLoactionMarker,destinationMarker,redMarker,dupMarker;
    public static final int REQUEST_LOCATION_CODE_ = 99;
    double startlat, startlon, currentlan, currentlon,Coverlat=1.1,Coverlon=1.1;
    double endlat, endlon, capacity,CheckCapacity;
    private UserActivity user,dupuser;
    private String Zone,Ward,Subward, subchild="Dustbin1";
    int counter;
    double smallest ;
    private FirebaseUser currentuser;
    private String uid;
    int flag=0,onetime=0,empty=0,count=0,dustbin60=0,covered=0;
    private TextView addresscom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Zone=getIntent().getStringExtra("Zone");
        Ward=getIntent().getStringExtra("Ward");
        Subward=getIntent().getStringExtra("Subward");
        addresscom=(TextView)findViewById(R.id.textView3);

        currentuser = FirebaseAuth.getInstance().getCurrentUser();

        uid = currentuser.getUid();
        mdriver= FirebaseDatabase.getInstance().getReference("Surat").child(Zone).child(Ward).child(Subward).child("Driver");
        mdriver.child("covereddustbins").setValue(0);
        mdriver.child("online").setValue(1);

        user = new UserActivity();
        dupuser = new UserActivity();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onBackPressed() {
        mdriver.child("online").setValue(0);
        super.onBackPressed();
    }

    @Override
    public void onPanelClosed(int featureId, Menu menu) {
        mdriver.child("online").setValue(0);
        super.onPanelClosed(featureId, menu);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case REQUEST_LOCATION_CODE_:
                if(grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    //permission granted
                    if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    {
                        if(client==null)
                        {
                            buildGoogleApiClient();
                        }
                    }mMap.setMyLocationEnabled(true);
                }
                else //permission denied
                {
                    Toast.makeText(this,"Permission denied", Toast.LENGTH_LONG).show();
                }
                //return;

        }
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

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

    }

    protected synchronized void buildGoogleApiClient()
    {
        client =new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        client.connect();
    }




    @Override
    public void onLocationChanged (Location location){

        lastLocation = location;

        /*if (currentLoactionMarker == null) {
            currentLoactionMarker.remove();
        }*/
        currentlan = location.getLatitude();
        currentlon = location.getLongitude();
        mdriver.child("Currentlan").setValue(currentlan);
        mdriver.child("Currentlon").setValue(currentlon);
        if(empty==0) {
            mdriver.child("Coverlan").setValue(Coverlat);
            mdriver.child("Coverlon").setValue(Coverlon);
            empty=1;
            count=0;
        }
        if(flag==0) {
            startlat = currentlan;
            startlon = currentlon;

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(startlat, startlon));
            markerOptions.title("Current location");
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.truck));
            currentLoactionMarker = mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(startlat, startlon)));
            mMap.animateCamera(CameraUpdateFactory.zoomBy(10));


            mdriver.child("Startlan").setValue(startlat);
            mdriver.child("Startlon").setValue(startlon);
            flag=1;
        }
        //mMap.clear();
        optimal();

        if (client == null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
        }
    }//on location changed


    public void optimal()
    {
        database = FirebaseDatabase.getInstance();
        mRef = database.getReference("Surat").child(Zone).child(Ward).child(Subward).child("Dustbins");
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                smallest=999999; dustbin60=0;
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    user = ds.getValue(UserActivity.class);
                    capacity=user.getCapacity();
                    if(capacity>60)
                    {
                        dustbin60++;
                        endlat = user.getLatitude();
                        endlon = user.getLongitude();
                        MarkerOptions markerOptions1 = new MarkerOptions();
                        markerOptions1.position(new LatLng(endlat, endlon));
                        markerOptions1.title("Dustbin");
                        float results[] = new float[10];
                        Location.distanceBetween(startlat, startlon, endlat, endlon, results);
                        markerOptions1.snippet("Distance = " + results[0]+" Capcity = "+capacity);
                        markerOptions1.icon(BitmapDescriptorFactory.fromResource(R.drawable.blackmarker));
                        redMarker=mMap.addMarker(markerOptions1);
                        if (results[0] < smallest) {
                            smallest=results[0];
                            subchild=ds.getKey();
                            dupMarker=redMarker;
                        }//if
                    }//if capacity
                }//for
                if(dupMarker!=null)
                    dupMarker.remove();
                database.getReference("Surat").child(Zone).child(Ward).child(Subward).child("Dustbins").child(subchild).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(onetime==0) {
                            endlat = (Double) dataSnapshot.child("Latitude").getValue();
                            endlon = (Double) dataSnapshot.child("Longitude").getValue();

                            if(destinationMarker!=null){
                                destinationMarker.remove();
                            }

                            MarkerOptions markerOptions2 = new MarkerOptions();
                            markerOptions2.position(new LatLng(endlat,endlon));
                            markerOptions2.title("Destination");
                            markerOptions2.icon(BitmapDescriptorFactory.fromResource(R.drawable.redmarker));

                            destinationMarker = mMap.addMarker(markerOptions2);

                            mdriver.child("Endlan").setValue(endlat);
                            mdriver.child("Endlon").setValue(endlon);
                            String city="";
                             Geocoder geocoder=new Geocoder(MapsActivity.this, Locale.getDefault());
                            try {
                            List<Address> addresses=geocoder.getFromLocation(endlat,endlon,1);
                            String address=addresses.get(0).getAddressLine(0);
                            city=addresses.get(0).getLocality();
                            Log.d("Mylo","Complete address:"+ addresses.toString());
                            Log.d("Mylo","address:"+ address);
                                addresscom.setText("Destination Address:\n"+address+"\n"+"Distance= "+(int)smallest+"m");



                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            LatLng end=new LatLng(endlat,endlon);
                            LatLng start=new LatLng(startlat,startlon);
                            PolylineOptions poption=new PolylineOptions().add(start).add(end).width(8).color(Color.BLUE).geodesic(true);
                            mMap.addPolyline(poption);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start,13));
                            mdriver.child("above60").setValue(dustbin60);
                            onetime = 1;
                        }
                        /*Object dataTransfer[]=new Object[3];
                        String url= getDirectionUrl();
                        GetDirectionsData getDirectionsData=new GetDirectionsData();
                        dataTransfer[0]=mMap;
                        dataTransfer[1]=url;
                        dataTransfer[2]=new LatLng(endlat,endlon);
                        getDirectionsData.execute(dataTransfer);*/

                        CheckCapacity = (Long) dataSnapshot.child("Capacity").getValue();

                        if(CheckCapacity>60)
                        {

                        }
                        else
                        {
                            //mRef.child("Capacity").setValue(0);
                            mMap.clear();
                            //currentLoactionMarker.remove();
                            //redMarker.remove();
                            if(count==0)
                            {
                                Coverlat=startlat;
                                Coverlon=startlon;
                                mdriver.child("count").setValue(count);
                                empty=0;
                                if(dustbin60!=0) {
                                    covered++;
                                    mdriver.child("covereddustbins").setValue(covered);
                                }

                            }
                            count++;

                            startlat=endlat;
                            startlon=endlon;
                            mdriver.child("Startlan").setValue(startlat);
                            mdriver.child("Startlon").setValue(startlon);
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(new LatLng(startlat,startlon));
                            markerOptions.title("Current location");
                            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.truck));
                            currentLoactionMarker = mMap.addMarker(markerOptions);
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(startlat,startlon)));
                            flag=1;
                            onetime=0;


                            // mMap.animateCamera(CameraUpdateFactory.zoomBy(10));
                            //optimal();
                            //Intent intent = new Intent(MapsActivity.this,MapsActivity.class);
                            //startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }//on data change


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });//add value event listener


            /*if(flag==1)
            {
                flag=0;
                App.refresh(this);
            }*/

    }//optimal



   /* public void refresh(int milliseconds)
    {

        final Handler handler=new Handler();
        final Runnable runnable=new Runnable() {
            @Override
            public void run()  {
                optimal();
            }
        };
        handler.postDelayed(runnable,milliseconds);
    }*/

    /*private String getDirectionUrl()
    {
        StringBuilder googleDirectionsUrl =new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        googleDirectionsUrl.append("origin="+startlat+","+startlon);
        googleDirectionsUrl.append("&destination="+endlat+","+endlon);
        googleDirectionsUrl.append("&key="+"AIzaSyCbXNt5isxGgBSi_N5Zu0YrLf9GFN7csww");
        return googleDirectionsUrl.toString();
    }*/
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }

    }
    public boolean checkLocationPermission(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION )!= PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE_);
            }
            else
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE_);
            }
            return false;

        }
        return true;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

}
