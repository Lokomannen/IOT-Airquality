package com.example.iotairquality;

import java.util.ArrayList;

public class RoomManager {
    private static RoomManager instance;
    private static ArrayList<Room> rooms;

    private int nextRoomId;

    public RoomManager(){

    }

    public static RoomManager getInstance() {
        if (instance == null) {
            instance = new RoomManager();
        }
        return instance;
    }

    public int getNewRoomId(){
        int oldId = nextRoomId;
        nextRoomId += 1;
        return oldId;
    }

    public void addRoom(Room room){
        rooms.add(room);
    }

    public void removeRoom(Room room){
        room.removeAllDevices();
        rooms.remove(room);
    }

    public Room findRoom(int id){
        for(Room room : rooms){
            if(id == room.getRoomId()){
                return room;
            }
        }
        return null;
    }

    public void setList(ArrayList<Room> roomList){
        rooms = roomList;
    }

    public void setNextRoomId(int nextRoomId){
        this.nextRoomId = nextRoomId;
    }
}
