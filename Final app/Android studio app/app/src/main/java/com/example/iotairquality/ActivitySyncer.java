package com.example.iotairquality;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public abstract class ActivitySyncer extends AppCompatActivity {

    abstract public void handleGetSensorsAndValues(ArrayList<Sensor> sensors, ArrayList<Actuator> actuatorList);
    abstract public void handleSync(int tempMax, int tempMin, int humMax, int humMin, int time, int day, String week, ArrayList<Room> roomList);
}
