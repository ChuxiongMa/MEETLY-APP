package ca.sfu.haliva_meetly;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Events implements Comparable<Object>, Serializable {
    private String eventTitle;
    private String date;
    private String startTime;
    private String endTime;
    private String duration;
    private double latitude;
    private double longitude;
    private String status;
    private int uniqueID;
    private String viewed;

    public Events(String title, String date, String start, String end, double lat, double lng) {
        this.eventTitle = title;
        this.date = date;
        this.startTime = start;
        this.endTime = end;
        this.latitude = lat;
        this.longitude = lng;
        this.status = "Unshared";
        setDuration();
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getDate() {
        return date;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getDuration() {
        return duration;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getStatus() {
        return status;
    }

    public String getViewed() {
        return viewed;
    }

    public int getUniqueID(){
        return uniqueID;
    }

    public void setDuration() {
        String[] start = startTime.split(":");
        String[] end = endTime.split(":");
        int hour = Integer.parseInt(end[0]) - Integer.parseInt(start[0]);
        int minutes = Integer.parseInt(end[1]) - Integer.parseInt(start[1]);
        duration = hour + "hrs. & " + minutes + "mins.";
    }

    public void setEventTitle(String title) {
        this.eventTitle = title;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setLatitude(double lat) {
        this.latitude = lat;
    }

    public void setLongitude(double lng) {
        this.longitude = lng;
    }

    public void setStatusShared() {
        this.status = "Shared";
    }

    public void setStatusSpontaneous() {
        this.status = "Spontaneous";
    }

    public void setUniqueID(int uniqueID) {
        this.uniqueID = uniqueID;
    }

    public void setViewedNew() {
        viewed = "New Event";
    }

    public void setViewedChanged() {
        viewed = "Changed Event";
    }

    public void setViewed(){
        viewed = "";
    }

    // REFERENCE:
    // http://stackoverflow.com/questions/13864018/sort-date-time-objects-latest-first
    @Override
    public int compareTo(Object another) {
        DateFormat formatter;
        Date date1 = null;
        Date date2 = null;
        Events other = (Events) another;

        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            date1 = formatter.parse(this.date + " " + this.startTime);
            date2 = formatter.parse(other.date + " " + other.startTime);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (NullPointerException n) {
            System.out.println("Exception thrown " + n.getMessage() + " date1 is " + date1 + " date2 is " + date2);
        }
        return date1.compareTo(date2);
    }

    public static void save(String fileName, List<Events> listOfEvents) throws IOException {
        FileOutputStream fout = new FileOutputStream(fileName);
        ObjectOutputStream oos = new ObjectOutputStream(fout);
        oos.writeObject(listOfEvents);
        fout.close();
    }

    public static List<Events> read(String fileName) throws IOException {
        FileInputStream fin = new FileInputStream(fileName);
        ObjectInputStream ois = new ObjectInputStream(fin);
        List<Events> listOfEvents = new ArrayList<>();
        try {
            listOfEvents = (ArrayList<Events>) ois.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        fin.close();
        return listOfEvents;
    }
}
