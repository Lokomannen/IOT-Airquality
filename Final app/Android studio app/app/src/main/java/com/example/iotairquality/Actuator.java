package com.example.iotairquality;

import android.view.View;

public class Actuator extends Device{

    private Room.Status status;
    private boolean enabled;
    public Actuator(String name){
        super(name);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setStatus(Room.Status status){
        this.status = status;
    }

    public String[] getDisplayedText(){
        String status = "Disabled";
        if (this.status == Room.Status.UNKNOWN){
            status = "Unknown";
        }else if (isEnabled()){
            status = "Enabled";
        }
        String[] textArray = {super.getName(),"Actuator", status};
        return textArray;
    }

    public Type getType(){
        return Type.ACTUATOR;
    }
}
