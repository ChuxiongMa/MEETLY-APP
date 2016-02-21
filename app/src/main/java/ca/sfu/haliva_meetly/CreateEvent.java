package ca.sfu.haliva_meetly;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class CreateEvent extends ActionBarActivity {
    private GoogleMap map;
    private double eventLatitude;
    private double eventLongitude;
    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        onClickDate();
        onClickStartTime();
        onClickEndTime();

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setMyLocationEnabled(true);

        // Gets current location. If app is ran on virtual device, you must pass
        // gps coordinates in emulator control
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                map.addMarker(new MarkerOptions()
                        .position(currentLocation)
                        .title("You are here!")
                        .draggable(true));
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(currentLocation, 13);
                map.animateCamera(update);
                eventLatitude = location.getLatitude();
                eventLongitude = location.getLongitude();

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

        };

        // To drag the marker in order to determine location for event
        map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
            }

            @Override
            public void onMarkerDrag(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng ll = marker.getPosition();
                marker.setTitle("Event Location!");
                eventLatitude = ll.latitude;
                eventLongitude = ll.longitude;
            }
        });

        //Add a new marker for a new location
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) {
                map.clear();
                map.addMarker(new MarkerOptions()
                        .position(point)
                        .title("New event will be here")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                LatLng currentLocation = new LatLng(point.latitude, point.longitude);
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(currentLocation, 13);
                map.animateCamera(update);
                eventLatitude = point.latitude;
                eventLongitude = point.longitude;
            }
        });

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
    }

    private void onClickStartTime() {
        EditText startTime = (EditText) findViewById(R.id.startTimeText);
        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(CreateEvent.this, startTimePick,
                        calendar.get(Calendar.HOUR),
                        calendar.get(Calendar.MINUTE), true).show();
            }
        });
    }

    private void onClickEndTime() {
        EditText endTime = (EditText) findViewById(R.id.endTimeText);
        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(CreateEvent.this, endTimePick,
                        calendar.get(Calendar.HOUR),
                        calendar.get(Calendar.MINUTE), true).show();
            }
        });
    }

    private void onClickDate() {
        EditText eventDate = (EditText) findViewById(R.id.dateText);
        eventDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(CreateEvent.this, datePick,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    TimePickerDialog.OnTimeSetListener startTimePick = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            EditText startTime = (EditText) findViewById(R.id.startTimeText);
            calendar.set(Calendar.HOUR, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            startTime.setText(formatTime.format(calendar.getTime()));
        }
    };

    TimePickerDialog.OnTimeSetListener endTimePick = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            EditText endTime = (EditText) findViewById(R.id.endTimeText);
            calendar.set(Calendar.HOUR, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            endTime.setText(formatTime.format(calendar.getTime()));
        }
    };

    DatePickerDialog.OnDateSetListener datePick = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            EditText eventDate = (EditText) findViewById(R.id.dateText);
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            eventDate.setText(formatDate.format(calendar.getTime()));
        }
    };


    public void storeEvent(View v) {
        EditText t = (EditText) findViewById(R.id.eventTitleText);
        String title = t.getText().toString();
        EditText d = (EditText) findViewById(R.id.dateText);
        String dateOfEvent = d.getText().toString();
        EditText startTime = (EditText) findViewById(R.id.startTimeText);
        String startTime1 = startTime.getText().toString();
        EditText endTime = (EditText) findViewById(R.id.endTimeText);
        String endTime1 = endTime.getText().toString();

        //Get the old list of events from a file located in the phone
        List<Events> listOfEvents = new ArrayList<>();
        String filePath = this.getFilesDir().getPath() + "/t.ser";
        try {
            listOfEvents = Events.read(filePath);
        } catch (IOException e) {
            System.out.println("Failed to read");
            e.printStackTrace();
        }

        Events newEvent = new Events(title, dateOfEvent, startTime1, endTime1, eventLatitude, eventLongitude);
        newEvent.setViewedNew();
        listOfEvents.add(newEvent);
        Collections.sort(listOfEvents);
        try {
            File yourFile = new File(filePath);
            if (!yourFile.exists()) {
                yourFile.createNewFile();
            }
            Events.save(filePath, listOfEvents);
            System.out.println("Succeeded to write");

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to write");
        }

        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
