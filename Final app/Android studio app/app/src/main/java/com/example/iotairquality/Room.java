package com.example.iotairquality;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Room implements Serializable {
    public enum Status{ // 1 , 2 , 3
        ACTIVE, INACTIVE, UNKNOWN;
    }

    public enum Quality{ // 1 , 2 , 3, 4
        GOOD, OKAY, BAD, UNKNOWN;
    }
    private int roomId;
    private Status status;
    private Quality quality;
    private String roomName;
    private ArrayList<Sensor> sensorList;
    private ArrayList<Actuator> actuatorList;

    public Room(int id, String name, Quality quality, Status status){
        roomId = id;
        roomName = name;
        sensorList = new ArrayList<>();
        actuatorList = new ArrayList<>();
        this.quality = quality;
        this.status = status;
    }

    public int getRoomId(){
        return roomId;
    }

    public String getRoomName(){
        return roomName;
    }

    public String getFileName(){
        return getRoomName() + "_" + getRoomId() +".ser";
    }

    public String getStatus() {
        switch (status) {
            case ACTIVE:
                return capitalizeFirstLetter(Status.ACTIVE.name());
            case INACTIVE:
                return capitalizeFirstLetter(Status.INACTIVE.name());
            default:
                return capitalizeFirstLetter(Status.UNKNOWN.name());
        }
    }

    public String getQuality() {
        switch (quality) {
            case GOOD:
                return capitalizeFirstLetter(Quality.GOOD.name());
            case BAD:
                return capitalizeFirstLetter(Quality.BAD.name());
            default:
                return capitalizeFirstLetter(Quality.UNKNOWN.name());
        }
    }

    public static Quality getQualityFromNumber(int number){
        if(number == 1){
            return Quality.GOOD;
        } else if (number == 2) {
            return Quality.OKAY;
        }else if (number == 3) {
            return Quality.BAD;
        }else{
            return Quality.UNKNOWN;
        }
    }

    public static Status getStatusFromNumber(int number){
        if(number == 1){
            return Status.ACTIVE;
        } else if (number == 2) {
            return Status.INACTIVE;
        }else{
            return Status.UNKNOWN;
        }
    }

    public Device[] getDevices(){
        Device[] deviceArray = new Device[sensorList.size() + actuatorList.size()];
        sensorList.toArray();

        System.arraycopy(sensorList.toArray(), 0 ,deviceArray, 0, sensorList.size());
        System.arraycopy(actuatorList.toArray(), 0 ,deviceArray, sensorList.size(), actuatorList.size());
        return deviceArray;
    }

    private String capitalizeFirstLetter(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    public void addDevice(Device device){
        if(device.getType() == Device.Type.ACTUATOR){
            actuatorList.add((Actuator) device);
        }else{
            sensorList.add((Sensor) device);
        }
        device.increaseConnectedRooms();
    }

    public void removeDevice(Device device){
        if(device.getType() == Device.Type.ACTUATOR){
            actuatorList.remove((Actuator) device);
        }else{
            sensorList.remove((Sensor) device);
        }
        device.decreaseConnectedRooms();
    }

    public void removeAllDevices(){
        for(Device device : sensorList){
            device.decreaseConnectedRooms();
        }
        for(Device device : actuatorList){
            device.decreaseConnectedRooms();
        }
        sensorList.clear();
        actuatorList.clear();
    }

    public boolean hasDevice(Device device){
        String deviceName = device.getName();
        for(Sensor sensor : sensorList){
            if (deviceName.equals(sensor.getName())){
                return true;
            }
        }
        for(Actuator actuator : actuatorList){
            if (deviceName.equals(actuator.getName())){
                return true;
            }
        }
        return false;
    }
}
