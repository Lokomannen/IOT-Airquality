package com.example.iotairquality;

import java.util.ArrayList;

public class DeviceManager {
    private static DeviceManager instance;
    private ArrayList<Sensor> sensorList = new ArrayList<>();

    private ArrayList<Actuator> actuatorList = new ArrayList<>();

    public DeviceManager(){

    }

    public static DeviceManager getInstance() {
        if (instance == null) {
            instance = new DeviceManager();
        }
        return instance;
    }

    public Device findDevice(String name){
        for(Actuator actuator : actuatorList){
            if(actuator.getName().equals(name)){
                return actuator;
            }
        }
        for(Sensor sensor : sensorList){
            if(sensor.getName().equals(name)){
                return sensor;
            }
        }
        return null;
    }

    public void addDevice(Device device){
        if (findDevice(device.getName()) != null){
            return;
        }
        if(device.getType() == Device.Type.ACTUATOR){
            actuatorList.add((Actuator) device);
        }else{
            sensorList.add((Sensor) device);
        }
    }
    public void removeDevice(Device device){
        if(device.getConnectedRooms() == 0){
            if (sensorList.contains(device)){
                sensorList.remove(device);
                return;
            }
            actuatorList.remove(device);
        }
    }

    public void clearList(){
        sensorList.clear();
        actuatorList.clear();
    }
}
