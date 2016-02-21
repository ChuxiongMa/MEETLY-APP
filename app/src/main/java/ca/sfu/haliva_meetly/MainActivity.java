package ca.sfu.haliva_meetly;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MainActivity extends ActionBarActivity {
    private static final int NUM_COLS = 1;
    public Button buttons[][];
    public int buttonTag = 0;
    private int tokenNumber;
    private String username;
    private boolean loggedIn;
    private List<Events> listOfEvents = new ArrayList<>();
    private int lastTick;
    private boolean changed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        changed = true;
        lastTick = 1;
    }

    @Override
    public void onResume() {
        super.onResume();
        setContentView(R.layout.activity_main);
        displayUsername();
        findCurrentLocation();
        checkIfLoggedIn();
        setupLoginButton();
        updateList();
    }

    public void onClickPullEvents(View v){
        updateList();
    }

    private void updateList(){
        lastTick++;
        Thread t = new Thread(){
            @Override
            public void run() {
                updateListOfEvents();
            }
        };
        t.start();
        try {
            t.join();
            updateButtons();
        }
        catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    private void updateButtons(){
        if(changed) {
            buttons = new Button[listOfEvents.size()][1];
            populateButtons(listOfEvents.size());
            changed = false;
        }
    }

    private void updateListOfEvents(){
        String filePath = this.getFilesDir().getPath() + "/t.ser";
        try {
            listOfEvents = Events.read(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        MeetlyServerImpl server = new MeetlyServerImpl();
        try {
            List<Events> newEvents = new ArrayList();
            List<Events> deleteEvents = new ArrayList();
            for (MeetlyServer.MeetlyEvent e : server.fetchEventsAfter(lastTick)) {
                if(listOfEvents.isEmpty()){
                    SimpleDateFormat formatTime = new SimpleDateFormat("kk:mm");
                    SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");

                    String startTime = formatTime.format(e.startTime.getTime());
                    String endTime = formatTime.format(e.endTime.getTime());
                    String date = formatDate.format(e.startTime.getTime());

                    Events eventFromServer = new Events(e.title, date, startTime, endTime, e.latitude, e.longitude);
                    eventFromServer.setUniqueID(e.eventID);
                    eventFromServer.setStatusShared();
                    eventFromServer.setViewedNew();
                    newEvents.add(eventFromServer);
                } else {
                    boolean anotherCheck = false;
                    SimpleDateFormat formatTime = new SimpleDateFormat("kk:mm");
                    SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");

                    String startTime = formatTime.format(e.startTime.getTime());
                    String endTime = formatTime.format(e.endTime.getTime());
                    String date = formatDate.format(e.startTime.getTime());
                    for (Events f : listOfEvents) {

                        if (e.title.equals(f.getEventTitle()) &&
                                startTime.equals(f.getStartTime()) &&
                                endTime.equals(f.getEndTime()) &&
                                date.equals(f.getDate()) &&
                                e.latitude == f.getLatitude() &&
                                e.longitude == f.getLongitude() &&
                                e.eventID == f.getUniqueID()) {
                            anotherCheck = true;
                        } else if (e.eventID == f.getUniqueID()) {
                            deleteEvents.add(f);
                            Events eventFromServer = new Events(e.title, date, startTime, endTime, e.latitude, e.longitude);
                            eventFromServer.setUniqueID(e.eventID);
                            eventFromServer.setStatusShared();
                            eventFromServer.setViewedChanged();
                            newEvents.add(eventFromServer);
                        }
                    }
                    if(!anotherCheck){
                            Events eventFromServer = new Events(e.title, date, startTime, endTime, e.latitude, e.longitude);
                            eventFromServer.setUniqueID(e.eventID);
                            eventFromServer.setStatusShared();
                            eventFromServer.setViewedNew();
                            newEvents.add(eventFromServer);
                    }
                }
            }
            for(Events event : newEvents){
                listOfEvents.add(event);
            }
            for(Events event : deleteEvents){
                listOfEvents.remove(event);
            }
            newEvents.clear();
            deleteEvents.clear();
            Collections.sort(listOfEvents);
        }
        catch(MeetlyServer.FailedFetchException e){
            e.printStackTrace();
        }

        try {
            File yourFile = new File(filePath);
            if (!yourFile.exists()) {
                yourFile.createNewFile();
            }
            Events.save(filePath, listOfEvents);

        } catch (IOException e) {
            e.printStackTrace();
        }
        changed = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void CreateEventButton(View v) {
        startActivity(new Intent(MainActivity.this, CreateEvent.class));
    }

    public void UserLoginButton(View v) {
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
    }

    public void displayUsername() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        username = sp.getString("username_key", null);
        String text = username;
        TextView textView = (TextView) findViewById(R.id.usernameDisplay);
        textView.setText(text);
    }

    public void findCurrentLocation() {
        SharedPreferences prefs = getSharedPreferences("Current Address", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("address", "Unknown");
        editor.putString("city", "Unknown");
        editor.commit();
        currentLocationUpdate();

        // Finds the current location
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Geocoder geocoder;
                try {
                    List<Address> addresses;
                    geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                    addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                    SharedPreferences prefs = getSharedPreferences("Current Address", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("address", addresses.get(0).getAddressLine(0));
                    editor.putString("city", addresses.get(0).getAddressLine(1));
                    editor.commit();
                    currentLocationUpdate();
                } catch (IOException e) {
                    SharedPreferences prefs = getSharedPreferences("Current Address", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("address", "Unknown");
                    editor.putString("city", "Unknown");
                    editor.commit();
                    currentLocationUpdate();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
                SharedPreferences prefs = getSharedPreferences("Current Address", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("address", "Unknown");
                editor.putString("city", "Unknown");
                editor.commit();
                currentLocationUpdate();
            }
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
    }

    // Updates the textviews for the current location
    public void currentLocationUpdate() {
        TextView address = (TextView) findViewById(R.id.currentAddressTxt);
        TextView city = (TextView) findViewById(R.id.currentCityTxt);

        SharedPreferences prefs = getSharedPreferences("Current Address", MODE_PRIVATE);
        String addressString = prefs.getString("address", "Unknown");
        address.setText(addressString);
        String cityString = prefs.getString("city", "Unknown");
        city.setText(cityString);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void populateButtons(final int numOfEvents) {
        TableLayout table = (TableLayout) findViewById(R.id.events_table);
        for (int row = 0; row < numOfEvents; row++) {
            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.MATCH_PARENT,
                    1.0f));
            table.addView(tableRow);

            for (int col = 0; col < NUM_COLS; col++) {
                final Button button = new Button(this);
                button.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT,
                        1.0f));

                button.setText(listOfEvents.get(row).getViewed() + " " + "Title: " + listOfEvents.get(row).getEventTitle() + " Date: " + listOfEvents.get(row).getDate()
                        + " Duration: " + listOfEvents.get(row).getDuration());

                button.setPadding(0, 0, 0, 0);

                button.setTag(row);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        lockButtonSize(numOfEvents);
                        buttonTag = (int) button.getTag();
                        Intent intent = new Intent(getApplicationContext(), ViewEventDetails.class);
                        intent.putExtra("Button Tag", buttonTag);
                        startActivity(intent);
                    }
                });
                tableRow.addView(button);
                buttons[row][col] = button;
            }
        }
    }

    private void lockButtonSize(int numOfEvents) {
        for (int row = 0; row < numOfEvents; row++) {
            for (int col = 0; col < NUM_COLS; col++) {
                Button button = buttons[row][col];

                int width = button.getWidth();
                button.setMinWidth(width);
                button.setMaxWidth(width);

                int height = button.getHeight();
                button.setMinHeight(height);
                button.setMaxHeight(height);
            }
        }
    }

    private void checkIfLoggedIn(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sp.edit();
        tokenNumber = sp.getInt("tokenNumber_key", 0);
        username = sp.getString("username_key", null);
        loggedIn = sp.getBoolean("loggedIn_key", false);
        if(loggedIn) {
            String text = username;
            TextView textView = (TextView) findViewById(R.id.usernameDisplay);
            textView.setText(text);
        } else {
            String text = "Guest";
            TextView textView = (TextView) findViewById(R.id.usernameDisplay);
            textView.setText(text);
            editor.putBoolean("loggedIn_key", false);
            editor.commit();
        }
    }

    private void setupLoginButton(){
        Button button = (Button) findViewById(R.id.userLoginBtn);
        if(!loggedIn){
            button.setText(getResources().getString(R.string.user_login_button));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent newPage = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(newPage);
                    finish();
                }
            });
        }
        else {
            button.setText(getResources().getString(R.string.user_logout_button));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putInt("tokenNumber_key", tokenNumber);
                    editor.putString("username_key", username);
                    editor.putString("password_key", "");
                    editor.putBoolean("loggedIn_key", false);
                    editor.commit();
                    checkIfLoggedIn();
                    setupLoginButton();
                }
            });
        }
    }
}
