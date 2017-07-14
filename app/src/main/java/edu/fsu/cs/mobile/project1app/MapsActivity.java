package edu.fsu.cs.mobile.project1app;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    SensorManager sensorManager;

    private LatLng oldLatLng, newLatLng;
    private static String STEPS_STR = "Steps: ";
    float numSteps = 0;

    private GoogleMap mMap;
    LocationManager locationManager;
    //90 second timer:
    long timer = 90000;
    Button Start_Pause, Reset, Lap, Show_Hide;
    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L ;
    Handler handler;
    int Seconds, Minutes, MilliSeconds, start=0, show=1;
    Runnable runnable;
    MenuItem  counter, distance, steps;
    private Menu menu;
    TextView watch;
    ListView listView, mDrawerList;
    DrawerLayout mDrawerLayout;
    String[] ListElements = new String[] {  };
    String[] DrawerList = new String[] {"Current Run", "Past Runs", "Settings"};

    List<String> ListElementsArrayList;
    List<String> DrawerArrayList;
    ArrayAdapter<String> adapter, drawer_adapter ;
    ActionBarDrawerToggle mDrawerToggle;


    ZoomControls zoomControls;

    private boolean mLocationPermissionGranted = false;
    public static int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Stop Watch Variables
        Start_Pause = (Button) findViewById(R.id.Start_Pause_button);
        Reset = (Button) findViewById(R.id.Reset_button);
        Lap = (Button) findViewById(R.id.Lap_button);
        watch = (TextView) findViewById(R.id.counter);
        Show_Hide = (Button) findViewById(R.id.Show_button);
        watch.setText("0:00:000");

        // Lap listView
        listView = (ListView) findViewById(R.id.listview1);
        ListElementsArrayList = new ArrayList<String>(Arrays.asList(ListElements));
        adapter = new ArrayAdapter<String>(MapsActivity.this,
                android.R.layout.simple_list_item_1,
                ListElementsArrayList);
        listView.setAdapter(adapter);

        // Drawer Variables
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        DrawerArrayList = new ArrayList<String>(Arrays.asList(DrawerList));
        drawer_adapter = new ArrayAdapter<String>(MapsActivity.this, android.R.layout.simple_list_item_1, DrawerArrayList);
        mDrawerList.setAdapter(drawer_adapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // I was not able to get the default map zoom controls (using mMap.getUiSettings().setZoomContolsEnabled(true);)
        // but found on StackOverflow (https://stackoverflow.com/questions/920741/always-show-zoom-controls-on-a-mapview)
        // that you can make your own so that's what I did here and it seems to work
        zoomControls = (ZoomControls) findViewById(R.id.map_zoom_controls);
        zoomControls.setOnZoomInClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.moveCamera(CameraUpdateFactory.zoomIn());
            }
        });
        zoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.moveCamera(CameraUpdateFactory.zoomOut());
            }
        });

    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        steps.setVisible(!drawerOpen);
        distance.setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        //Reference: https://stackoverflow.com/questions/17430477/
        // is-it-possible-to-add-a-timer-to-the-actionbar-on-android
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        handler = new Handler();

        distance = menu.findItem(R.id.distance);
        distance.setTitle("Distance: X");

        steps = menu.findItem(R.id.steps);
        steps.setTitle(STEPS_STR + (int) numSteps);

        this.menu = menu;

        setupRegisterListener();

        //counter = menu.findItem(R.id.counter);
        //counter.setTitle("00:00:00");

        /*new CountDownTimer(timer, 1000) {

            public void onTick(long millisUntilFinished) {
                long millis = millisUntilFinished;
                String  hms =  (TimeUnit.MILLISECONDS.toHours(millis))+":"+
                        (TimeUnit.MILLISECONDS.toMinutes(millis) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)))+":"+
                        (TimeUnit.MILLISECONDS.toSeconds(millis) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));

                counter.setTitle("Time left: " + hms);
                timer = millis;

            }

            public void onFinish() {
                counter.setTitle("done!");
            }
        }.start();*/

        runnable = new Runnable() {

            public void run() {

                MillisecondTime = SystemClock.uptimeMillis() - StartTime;

                UpdateTime = TimeBuff + MillisecondTime;

                Seconds = (int) (UpdateTime / 1000);

                Minutes = Seconds / 60;

                Seconds = Seconds % 60;

                MilliSeconds = (int) (UpdateTime % 1000);

               /* counter.setTitle("" + Minutes + ":"
                        + String.format("%02d", Seconds) + ":"
                        + String.format("%03d", MilliSeconds));*/

                watch.setText("" + Minutes + ":"
                        + String.format("%02d", Seconds) + ":"
                        + String.format("%03d", MilliSeconds));

                handler.postDelayed(this, 0);
            }

        };
        return  true;

    }

    public void onClick_Show(View v){
        switch(show){
            case 0:
                Show_Hide.setText("Hide Laps");
                listView.setVisibility(View.VISIBLE);
                show = 1;
                break;
            case 1:
                Show_Hide.setText("Show Laps");
                listView.setVisibility(View.INVISIBLE);
                show = 0;
                break;
        }
    }

    public void onClick_Start(View v){
        switch(start) {
            case 0:
                Start_Pause.setText("Pause");
                StartTime = SystemClock.uptimeMillis();
                handler.postDelayed(runnable, 0);
                Reset.setEnabled(false);
                Reset.setText("");
                Lap.setEnabled(false);
                Lap.setText("");
                start = 1;
                break;
            case 1:
                Start_Pause.setText("Start");
                TimeBuff += MillisecondTime;
                handler.removeCallbacks(runnable);
                Reset.setEnabled(true);
                Reset.setText("Reset");
                Lap.setEnabled(true);
                Lap.setText("Save Lap");
                start = 0;
                break;
        }
    }

    public void onClick_Reset(View v){
        MillisecondTime = 0L ;
        StartTime = 0L ;
        TimeBuff = 0L ;
        UpdateTime = 0L ;
        Seconds = 0 ;
        Minutes = 0 ;
        MilliSeconds = 0 ;

        //counter.setTitle("00:00:00");
        watch.setText("0:00:000");
        ListElementsArrayList.clear();
        adapter.notifyDataSetChanged();
    }

    public void onClick_Lap(View v){
        ListElementsArrayList.add(watch.getText().toString());
        adapter.notifyDataSetChanged();
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

        // Enables touch gestures so that they can control the map
        // Useful to enable pinch and zoom in/out as well as one handed zoom in feature
        mMap.getUiSettings().setZoomGesturesEnabled(true);


        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        // Check to see if proper permissions are in place to get current location, if they are,
        // enable setMyLocation
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        mMap.setMyLocationEnabled(true);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();

        String provider = locationManager.getBestProvider(criteria, true);

        Location mLocation = locationManager.getLastKnownLocation(provider);

        if (mLocation == null) {
            locationManager.requestLocationUpdates(provider, 1000, 0, this);
        }


        double latitude = mLocation.getLatitude();
        double longitude = mLocation.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);

        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        oldLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);


        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
    }


    @Override
    public void onLocationChanged(Location location) {
        newLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        PolylineOptions line = new PolylineOptions()
                .add(oldLatLng, newLatLng)
                .width(5).color(Color.RED);
        mMap.addPolyline(line);
        oldLatLng = newLatLng;

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void setupRegisterListener() {
        sensorManager.registerListener(new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            //numSteps = event.values[0];
            numSteps++;
            //update number of steps
            if (menu == null) {
                return;
            }
            steps = menu.findItem(R.id.steps);
            steps.setTitle(STEPS_STR + (int) numSteps);
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
     }, sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR),
                SensorManager.SENSOR_DELAY_UI);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position){
        switch (position){
            case 0:
                Toast.makeText(this, "This is Position 0 in Drawer", Toast.LENGTH_SHORT).show();
                break;
            case 1:
                Toast.makeText(this, "This is Position 1 in Drawer", Toast.LENGTH_SHORT).show();
                break;
            case 2:
                Toast.makeText(this, "This is Position 2 in Drawer", Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this, "This is the Default in Drawer", Toast.LENGTH_SHORT).show();
        }
    }
}
