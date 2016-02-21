package ca.sfu.haliva_meetly;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class ViewEventDetails extends ActionBarActivity {
    private GoogleMap map;
    private int buttonTag;
    private int tokenNumber;
    private String username;
    private String password;
    private boolean loggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event_details);

        displayEventViewed();
        displayEventName();
        displayEventTime();
        displayEventDate();
        displayEventDuration();
        displayEventLocation();
        displayEventStatus();
        createEventStatusButton();
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoginInfo();
    }

    private void displayEventViewed(){
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
        String text = listOfEvents.get(buttonTag).getViewed();
        TextView titleText = (TextView) findViewById(R.id.eventDetailsMainTitle);
        titleText.setText(text);
        System.out.println(getResources().getString(R.string.event_details_main_new));
        System.out.println(listOfEvents.get(buttonTag).getViewed());
        if(listOfEvents.get(buttonTag).getViewed().equals(getResources().getString(R.string.event_details_main_new))
                || listOfEvents.get(buttonTag).getViewed().equals(getResources().getString(R.string.event_details_main_changed))){
            System.out.println("here");
            listOfEvents.get(buttonTag).setViewed();
        }
        if(listOfEvents.get(buttonTag).getViewed().equals("New Event")
                || listOfEvents.get(buttonTag).getViewed().equals("Changed Event")){
            System.out.println("here2");
            listOfEvents.get(buttonTag).setViewed();
        }
        try {
            File yourFile = new File(filePath);
            if(!yourFile.exists()) {
                yourFile.createNewFile();
            }
            Events.save(filePath, listOfEvents);

        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    private void displayEventName() {
        List<Events> listOfEvents = new ArrayList<>();
        String filePath = this.getFilesDir().getPath() + "/t.ser";
        try {
            listOfEvents = Events.read(filePath);
        }
        catch(IOException e){
            e.printStackTrace();
        }
        Intent intent = getIntent();
        buttonTag = intent.getIntExtra("Button Tag", 0);
        String text = listOfEvents.get(buttonTag).getEventTitle();
        TextView textView = (TextView) findViewById(R.id.eventDetailsName);
        textView.setText(text);
    }

    private void displayEventDate() {
        List<Events> listOfEvents = new ArrayList<>();
        String filePath = this.getFilesDir().getPath() + "/t.ser";
        try {
            listOfEvents = Events.read(filePath);
        }
        catch(IOException e){
            e.printStackTrace();
        }
        Intent intent = getIntent();
        buttonTag = intent.getIntExtra("Button Tag", 0);
        String text = "Date: " + listOfEvents.get(buttonTag).getDate();
        TextView textView = (TextView) findViewById(R.id.eventDetailsDate);
        textView.setText(text);
    }

    private void displayEventTime() {
        List<Events> listOfEvents = new ArrayList<>();
        String filePath = this.getFilesDir().getPath() + "/t.ser";
        try {
            listOfEvents = Events.read(filePath);
        }
        catch(IOException e){
            e.printStackTrace();
        }
        Intent intent = getIntent();
        buttonTag = intent.getIntExtra("Button Tag", 0);
        String text = "Start Time: " + listOfEvents.get(buttonTag).getStartTime();
        TextView textView = (TextView) findViewById(R.id.eventDetailsTime);
        textView.setText(text);
    }

    private void displayEventDuration() {
        List<Events> listOfEvents = new ArrayList<>();
        String filePath = this.getFilesDir().getPath() + "/t.ser";
        try {
            listOfEvents = Events.read(filePath);
        }
        catch(IOException e){
            e.printStackTrace();
        }
        Intent intent = getIntent();
        buttonTag = intent.getIntExtra("Button Tag", 0);
        String text = "Duration: " + listOfEvents.get(buttonTag).getDuration();
        TextView textView = (TextView) findViewById(R.id.eventDetailsDuration);
        textView.setText(text);
    }

    private void displayEventLocation() {
        List<Events> listOfEvents = new ArrayList<>();
        String filePath = this.getFilesDir().getPath() + "/t.ser";
        try {
            listOfEvents = Events.read(filePath);
        }
        catch(IOException e){
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
    }

    private void displayEventStatus() {
        List<Events> listOfEvents = new ArrayList<>();
        String filePath = this.getFilesDir().getPath() + "/t.ser";
        try {
            listOfEvents = Events.read(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent intent = getIntent();
        buttonTag = intent.getIntExtra("Button Tag", 0);
        String text = "Status: " + listOfEvents.get(buttonTag).getStatus();
        TextView textView = (TextView) findViewById(R.id.eventDetailsStatus);
        textView.setText(text);
    }

    public void createEventStatusButton() {
        List<Events> listOfEvents = new ArrayList<>();
        String filePath = this.getFilesDir().getPath() + "/t.ser";
        try {
            listOfEvents = Events.read(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent intent = getIntent();
        buttonTag = intent.getIntExtra("Button Tag", 0);
        Button button = (Button) findViewById(R.id.changeStatusButton);
        button.setEnabled(true);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]{"Planned Event (Available to everyone. Account required)", "Spontaneous Event (Nearby only)"};
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewEventDetails.this);
                builder.setTitle("How would you like to share?");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                if (!loggedIn) {
                                    Toast toast = Toast.makeText(getApplicationContext(), "Not logged in!", Toast.LENGTH_SHORT);
                                    toast.show();
                                } else {
                                    new Thread(new Runnable(){
                                        @Override
                                        public void run() {
                                            updateListShared();
                                        }
                                    }).start();
                                }
                                break;
                            case 1:
                                updateListSpontaneous();
                                break;
                        }
                    }
                });
                builder.show();
            }

        });
        if (!listOfEvents.get(buttonTag).getStatus().equals("Unshared")){
            button.setEnabled(false);
        }
    }

    public void updateListShared(){
        //Get the old list of events from a file located in the phone
        List<Events> listOfEvents = new ArrayList<>();
        String filePath = this.getFilesDir().getPath() + "/t.ser";
        try {
            listOfEvents = Events.read(filePath);
        }
        catch(IOException e){
            e.printStackTrace();
        }

        Calendar startTimeCal = Calendar.getInstance();
        Calendar endTimeCal = Calendar.getInstance();
        try {
            SimpleDateFormat stc = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy");
            startTimeCal.setTime(stc.parse(listOfEvents.get(buttonTag).getStartTime()));

            SimpleDateFormat etc = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy");
            endTimeCal.setTime(etc.parse(listOfEvents.get(buttonTag).getEndTime()));
        } catch(ParseException e){
            e.printStackTrace();
        }
        MeetlyServerImpl server = new MeetlyServerImpl();

        try {
            int uniqueID = server.publishEvent(username, tokenNumber, listOfEvents.get(buttonTag).getEventTitle(),
                    startTimeCal, endTimeCal,
                    listOfEvents.get(buttonTag).getLatitude(), listOfEvents.get(buttonTag).getLongitude());
            listOfEvents.get(buttonTag).setStatusShared();
            listOfEvents.get(buttonTag).setUniqueID(uniqueID);
        } catch(MeetlyServer.FailedPublicationException e){
            e.printStackTrace();
        }

        Collections.sort(listOfEvents);
        try {
            File yourFile = new File(filePath);
            if(!yourFile.exists()) {
                yourFile.createNewFile();
            }
            Events.save(filePath, listOfEvents);

        }
        catch(IOException e){
            e.printStackTrace();
        }
        finish();
    }

    public void updateListSpontaneous(){
        //Get the old list of events from a file located in the phone
        List<Events> listOfEvents = new ArrayList<>();
        String filePath = this.getFilesDir().getPath() + "/t.ser";
        try {
            listOfEvents = Events.read(filePath);
        }
        catch(IOException e){
            e.printStackTrace();
        }
//        int uniqueId = publishEvent(username, userToken, listOfEvents.get(buttonTag).getEventTitle(), Calendar startTime, Calendar endTime, listOfEvents.get(buttonTag).getLatitude(), listOfEvents.get(buttonTag).getLongitude());
        listOfEvents.get(buttonTag).setStatusSpontaneous();

        Collections.sort(listOfEvents);
        try {
            File yourFile = new File(filePath);
            if(!yourFile.exists()) {
                yourFile.createNewFile();
            }
            Events.save(filePath, listOfEvents);

        }
        catch(IOException e){
            e.printStackTrace();
        }
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_event_details, menu);
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

    public void modifyEventDetails(View v) {
        Intent intent = new Intent(getApplicationContext(), ModifyEvent.class);
        intent.putExtra("Button Tag", buttonTag);
        startActivity(intent);
        finish();
    }

    public void getLoginInfo(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        tokenNumber = sp.getInt("tokenNumber_key", 0);
        username = sp.getString("username_key", null);
        password = sp.getString("password_key", null);
        loggedIn = sp.getBoolean("loggedIn_key", false);
    }
}
