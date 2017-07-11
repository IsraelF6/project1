package edu.fsu.cs.mobile.project1app;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.content.res.Configuration;
import com.google.android.gms.maps.SupportMapFragment;
import android.content.ContentValues;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import java.util.concurrent.TimeUnit;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
//import com.google.android.gms.maps.MapController;
import com.google.android.gms.maps.MapView;
//import com.google.android.maps.GeoPoint;
import android.location.LocationManager;
import android.content.Context;


public class MainActivity extends AppCompatActivity {

    //90 second timer:
    long timer = 90000;

    public final static String MAPS_FRAGMENT_TAG = "mapsFragment";

    FragmentTransaction trans;

    SensorManager sensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupFragments(getResources().getConfiguration());

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        setupRegisterListner();

    }

    private void setupFragments(Configuration config) {
        MapsFragment mapsFragment = (MapsFragment) getSupportFragmentManager()
                .findFragmentByTag(MAPS_FRAGMENT_TAG);

        if (mapsFragment == null) {
            mapsFragment = new MapsFragment();
            mapsFragment.setArguments(getIntent().getExtras());
        }
        trans=getSupportFragmentManager().beginTransaction();
        trans.replace(R.id.fragment_frame, mapsFragment);
        trans.commit();
    }


    /*
    private void setupFragments(Configuration config) {
        SupportMapFragment mapsFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentByTag(MAPS_FRAGMENT_TAG);

        if (mapsFragment == null) {
            mapsFragment = new SupportMapFragment();
            mapsFragment.setArguments(getIntent().getExtras());
        }
        trans=getSupportFragmentManager().beginTransaction();
        trans.replace(R.id.fragment_frame, mapsFragment);
        trans.commit();
    }

    */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        //Reference: https://stackoverflow.com/questions/17430477/
        // is-it-possible-to-add-a-timer-to-the-actionbar-on-android
        setContentView(R.layout.activity_main);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        final MenuItem distance = menu.findItem(R.id.distance);
        distance.setTitle("Distance: X");

        final MenuItem  steps = menu.findItem(R.id.steps);
        steps.setTitle("Steps: X");

        final MenuItem  counter = menu.findItem(R.id.counter);
        new CountDownTimer(timer, 1000) {

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
        }.start();

        return  true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option1:

                //Whatever we wish to do.

                //DIALOG OPTION:
                //showDialog(DIALOG_ID);

                break;
            case R.id.option2:
                //Whatever we wish to do.

                //EXIT OPTION:
                //finish();

                break;
        }
        return true;
    }

    public void setupRegisterListner() {
        sensorManager.registerListener(new SensorEventListener() {
             @Override
             public void onSensorChanged(SensorEvent event) {
                 float steps = event.values[0];
                 //update number of steps
             }
             @Override
             public void onAccuracyChanged(Sensor sensor, int accuracy) {

             }
        }, sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
                SensorManager.SENSOR_DELAY_UI);
    }

}
