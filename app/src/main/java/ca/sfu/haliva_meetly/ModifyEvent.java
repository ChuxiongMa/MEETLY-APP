package ca.sfu.haliva_meetly;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class ModifyEvent extends ActionBarActivity {
    private GoogleMap map;
    private double eventLatitude;
    private double eventLongitude;
    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat formatTime = new SimpleDateFormat("kk:mm");
    private int buttonTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_event);

        displayEventName();
        displayEventStartTime();
        displayEventDate();
        displayEventEndTime();
        displayEventLocation();

        onClickDate();
        onClickStartTime();
        onClickEndTime();

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setMyLocationEnabled(true);

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
    }

    private void onClickStartTime() {
        EditText startTime = (EditText) findViewById(R.id.modStartTimeText);
        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(ModifyEvent.this, startTimePick,
                        calendar.get(Calendar.HOUR),
                        calendar.get(Calendar.MINUTE), true).show();
            }
        });
    }

    private void onClickEndTime() {
        EditText endTime = (EditText) findViewById(R.id.modEndTimeText);
        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(ModifyEvent.this, endTimePick,
                        calendar.get(Calendar.HOUR),
                        calendar.get(Calendar.MINUTE), true).show();
            }
        });
    }

    private void onClickDate() {
        EditText dateText = (EditText) findViewById(R.id.modDateText);
        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(ModifyEvent.this, datePick,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    TimePickerDialog.OnTimeSetListener startTimePick = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            calendar.set(Calendar.HOUR, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            EditText startTime = (EditText) findViewById(R.id.modStartTimeText);
            startTime.setText(formatTime.format(calendar.getTime()));
        }
    };

    TimePickerDialog.OnTimeSetListener endTimePick = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            calendar.set(Calendar.HOUR, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            EditText endTime = (EditText) findViewById(R.id.modEndTimeText);
            endTime.setText(formatTime.format(calendar.getTime()));
        }
    };

    DatePickerDialog.OnDateSetListener datePick = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            EditText dateText = (EditText) findViewById(R.id.modDateText);
            dateText.setText(formatDate.format(calendar.getTime()));
        }
    };

    public void modifyEvent(View v){
        new Thread(new Runnable(){
            @Override
            public void run() {
                modifyEventThread();
            }
        }).start();
    }


    public void modifyEventThread() {
        //Get the old list of events from a file located in the phone
        List<Events> listOfEvents = new ArrayList<>();
        String filePath = this.getFilesDir().getPath() + "/t.ser";
        try {
            listOfEvents = Events.read(filePath);
        } catch (IOException e) {
            System.out.println("Failed to read");
            e.printStackTrace();
        }

        if(listOfEvents.get(buttonTag).getStatus().equals("Unshared")){
            EditText eventTitle = (EditText) findViewById(R.id.modEventTitle);
            String title = eventTitle.getText().toString();
            listOfEvents.get(buttonTag).setEventTitle(title);
            EditText dateText = (EditText) findViewById(R.id.modDateText);
            String dateOfEvent = dateText.getText().toString();
            listOfEvents.get(buttonTag).setDate(dateOfEvent);
            EditText startTime = (EditText) findViewById(R.id.modStartTimeText);
            String startTime1 = startTime.getText().toString();
            listOfEvents.get(buttonTag).setStartTime(startTime1);
            EditText endTime = (EditText) findViewById(R.id.modEndTimeText);
            String endTime1 = endTime.getText().toString();
            listOfEvents.get(buttonTag).setEndTime(endTime1);
            listOfEvents.get(buttonTag).setLatitude(eventLatitude);
            listOfEvents.get(buttonTag).setLongitude(eventLongitude);
            listOfEvents.get(buttonTag).setDuration();
            listOfEvents.get(buttonTag).setViewedChanged();
        }
        else {
            EditText eventTitle = (EditText) findViewById(R.id.modEventTitle);
            String title = eventTitle.getText().toString();
            EditText dateText = (EditText) findViewById(R.id.modDateText);
            String dateOfEvent = dateText.getText().toString();
            EditText startTime = (EditText) findViewById(R.id.modStartTimeText);
            String startTime1 = startTime.getText().toString();
            EditText endTime = (EditText) findViewById(R.id.modEndTimeText);
            String endTime1 = endTime.getText().toString();


            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = sp.edit();
            int tokenNumber = sp.getInt("tokenNumber_key", 0);

            Calendar startTimeCal = Calendar.getInstance();
            Calendar endTimeCal = Calendar.getInstance();
            try {
                SimpleDateFormat stc = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy");
                startTimeCal.setTime(stc.parse(listOfEvents.get(buttonTag).getStartTime()));

                SimpleDateFormat etc = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy");
                endTimeCal.setTime(etc.parse(listOfEvents.get(buttonTag).getEndTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            MeetlyServerImpl server = new MeetlyServerImpl();

            try {
                server.modifyEvent(listOfEvents.get(buttonTag).getUniqueID(), tokenNumber, title, startTimeCal, endTimeCal, eventLatitude, eventLongitude);
            } catch (MeetlyServer.FailedPublicationException e) {
                e.printStackTrace();
            }
            listOfEvents.remove(buttonTag);
        }
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

    private void displayEventName() {
        List<Events> listOfEvents = new ArrayList<>();
        String filePath = this.getFilesDir().getPath() + "/t.ser";
        try {
            listOfEvents = Events.read(filePath);
        } catch (IOException e) {
            System.out.println("Failed to read");
            e.printStackTrace();
        }
        Intent intent = getIntent();
        buttonTag = intent.getIntExtra("Button Tag", 0);
        String text = listOfEvents.get(buttonTag).getEventTitle();
        System.out.print("EVENT: " + text);
        EditText eventTitle = (EditText) findViewById(R.id.modEventTitle);
        eventTitle.setText(text);
    }

    private void displayEventDate() {
        List<Events> listOfEvents = new ArrayList<>();
        String filePath = this.getFilesDir().getPath() + "/t.ser";
        try {
            listOfEvents = Events.read(filePath);
        } catch (IOException e) {
            System.out.println("Failed to read");
            e.printStackTrace();
        }
        Intent intent = getIntent();
        buttonTag = intent.getIntExtra("Button Tag", 0);
        String text = listOfEvents.get(buttonTag).getDate();
        EditText dateText = (EditText) findViewById(R.id.modDateText);
        dateText.setText(text);
    }

    private void displayEventStartTime() {
        List<Events> listOfEvents = new ArrayList<>();
        String filePath = this.getFilesDir().getPath() + "/t.ser";
        try {
            listOfEvents = Events.read(filePath);
        } catch (IOException e) {
            System.out.println("Failed to read");
            e.printStackTrace();
        }
        Intent intent = getIntent();
        buttonTag = intent.getIntExtra("Button Tag", 0);
        String text = listOfEvents.get(buttonTag).getStartTime();
        EditText startTime = (EditText) findViewById(R.id.modStartTimeText);
        startTime.setText(text);
    }

    private void displayEventEndTime() {
        List<Events> listOfEvents = new ArrayList<>();
        String filePath = this.getFilesDir().getPath() + "/t.ser";
        try {
            listOfEvents = Events.read(filePath);
        } catch (IOException e) {
            System.out.println("Failed to read");
            e.printStackTrace();
        }
        Intent intent = getIntent();
        buttonTag = intent.getIntExtra("Button Tag", 0);
        String text = listOfEvents.get(buttonTag).getEndTime();
        EditText endTime = (EditText) findViewById(R.id.modEndTimeText);
        endTime.setText(text);
    }

    private void displayEventLocation() {
        List<Events> listOfEvents = new ArrayList<>();
        String filePath = this.getFilesDir().getPath() + "/t.ser";
        try {
            listOfEvents = Events.read(filePath);
        } catch (IOException e) {
            System.out.println("Failed to read");
            e.printStackTrace();
        }
        Intent intent = getIntent();
        buttonTag = intent.getIntExtra("Button Tag", 0);
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        LatLng eventLocation = new LatLng(listOfEvents.get(buttonTag).getLatitude(),
                listOfEvents.get(buttonTag).getLongitude());
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.addMarker(new MarkerOptions().position(eventLocation).title("Event location"));
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(eventLocation, 13);
        map.animateCamera(update);

        eventLatitude = eventLocation.latitude;
        eventLongitude = eventLocation.longitude;
    }

}
