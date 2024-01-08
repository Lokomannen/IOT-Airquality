package com.example.iotairquality;

import android.view.View;

import java.io.Serializable;

public abstract class Device implements Serializable {

    private int connectedRooms = 0;

    public enum Type {
        SENSOR, ACTUATOR
    }
    private String name;

    public Device(String name) {
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public int getConnectedRooms() {
        return connectedRooms;
    }

    abstract public Type getType();

    public void increaseConnectedRooms(){
        connectedRooms += 1;
    }

    public void decreaseConnectedRooms(){
        connectedRooms -= 1;
    }

    abstract public String[] getDisplayedText();
}
