package com.example.iotairquality;

import android.view.View;

public class Sensor extends Device{
    private String temperature, humidity, carbon;

    public Sensor(String name){
        super(name);
    }

    public Sensor(String name, int tempValue, int humValue, int carValue){
        super(name);
        setTemperature(tempValue);
        setHumidity(humValue);
        setCarbon(carValue);
    }
    public String[] getDisplayedText(){
        String[] textArray = {super.getName(), temperature, humidity + " " + carbon};
        return textArray;
    }

    public Type getType(){
        return Type.SENSOR;
    }
    public void setTemperature(int tempValue){
        temperature = tempValue + "C";
    }

    public void setHumidity(int humValue){
        humidity = humValue + "%";
    }

    public void setCarbon(int carValue){
        carbon = carValue + "ppm";
    }
    public void setTemperature(String tempString){
        temperature = tempString;
    }

    public void setHumidity(String humString){
        humidity = humString;
    }

    public void setCarbon(String carString){
        carbon = carString;
    }
}
