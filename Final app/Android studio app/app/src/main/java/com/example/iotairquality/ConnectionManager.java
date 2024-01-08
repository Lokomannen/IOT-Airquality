package com.example.iotairquality;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

public class ConnectionManager {

    private static ConnectionManager instance;
    private static DeviceManager deviceManager;
    private RoomManager roomManager;
    private int taskId = 0;
    private Queue<Integer> taskQueue = new LinkedList<>();
    private boolean connectionOccupied = false;

    private ActivitySyncer activeActivity;
    public ConnectionManager(){
        roomManager = RoomManager.getInstance();
        deviceManager = DeviceManager.getInstance();
    }

    public static ConnectionManager getInstance(ActivitySyncer activeActivity) {
        if (instance == null) {
            instance = new ConnectionManager();
        }
        instance.activeActivity = activeActivity;
        return instance;
    }

    private int getNewTaskId(){
        taskId += 1;
        return taskId;
    }

    public void syncData(){
        String command = "python code/syncData.py";
        runAsyncCommand(command, this::handleSync);
    }

    private void handleSync(ArrayList<String> result){
        deviceManager.clearList();
        if (result == null || result.size() == 0){
            return;
        }
        int tempMax = Integer.parseInt(result.get(0));
        int tempMin = Integer.parseInt(result.get(1));
        int humMax = Integer.parseInt(result.get(2));
        int humMin = Integer.parseInt(result.get(3));

        int time = Integer.parseInt(result.get(4));
        int day = Integer.parseInt(result.get(5));
        String week = result.get(6);
        int nextRoomId = Integer.parseInt(result.get(7));
        
        ArrayList<Room> roomList = new ArrayList<>();
        int i = 8;
        while (i < result.size()){
            String roomNameId = result.get(i);
            String roomName = "";
            int roomId = -1;
            for (int j = roomNameId.length() - 1; j >= 0; j--) {
                if (roomNameId.charAt(j) == '_'){
                    roomName = roomNameId.substring(0,j);
                    roomId = Integer.parseInt( roomNameId.substring(j + 1, roomNameId.length()) );
                }
            }
            i += 1;

            Room.Quality roomQuality = Room.getQualityFromNumber( Integer.parseInt(result.get(i)) );
            i += 1;
            Room.Status roomStatus = Room.getStatusFromNumber( Integer.parseInt(result.get(i)) );
            i += 1;

            Room room = new Room(roomId, roomName, roomQuality, roomStatus);
            roomList.add(room);
            //sensors
            String nextString = result.get(i);
            while (!nextString.equals("actuators")){
                String sensorName = result.get(i);
                i += 1;
                int temp = Integer.parseInt(result.get(i));
                i += 1;
                int humidity = Integer.parseInt(result.get(i));
                i += 1;
                int carbon = Integer.parseInt(result.get(i));
                i += 1;
                Sensor sensor = new Sensor(sensorName, temp, humidity, carbon);
                room.addDevice(sensor);
                deviceManager.addDevice(sensor);
                nextString = result.get(i);
            }
            i += 1;
            nextString = result.get(i);
            while (!nextString.equals("end")){
                String actuatorName = result.get(i);
                i += 1;
                int statusNumber = Integer.parseInt(result.get(i));
                Room.Status status = Room.getStatusFromNumber(statusNumber);
                i += 1;
                Actuator actuator = new Actuator(actuatorName);
                actuator.setStatus(status);
                room.addDevice(actuator);
                deviceManager.addDevice(actuator);
                nextString = result.get(i);
            }
            i += 1;
        }

        roomManager.setList(roomList);
        roomManager.setNextRoomId(nextRoomId);
        activeActivity.handleSync(tempMax,tempMin, humMax, humMin, time, day, week, roomList);
    }

    public void getDevicesAndValues(){
        String command = "python code/listDevicesAndValues.py";
        runAsyncCommand(command, this::handleDevicesAndValues);
    }

    private void handleDevicesAndValues(ArrayList<String> result){
        int i = 0;
        ArrayList<Sensor> sensorList = new ArrayList<>();
        while (i < result.size() - 1){
            String sensorName = result.get(i);
            i += 1;
            if (sensorName.equals("end")){
                break;
            }
            int temp = Integer.parseInt(result.get(i));
            i += 1;
            int humidity = Integer.parseInt(result.get(i));
            i += 1;
            int carbon = Integer.parseInt(result.get(i));
            i += 1;
            Sensor sensor = new Sensor(sensorName, temp, humidity, carbon);
            sensorList.add(sensor);
        }
        ArrayList<Actuator> actuatorList = new ArrayList<>();
        while (i < result.size() - 1) {
            String actuatorName = result.get(i);
            i += 1;
            int statusNumber = Integer.parseInt(result.get(i));
            Room.Status status = Room.getStatusFromNumber(statusNumber);
            i += 1;
            Actuator actuator = new Actuator(actuatorName);
            actuator.setStatus(status);
            actuatorList.add(actuator);
        }
        activeActivity.handleGetSensorsAndValues(sensorList, actuatorList);
    }

    public void advanceTime(){
        String command = "python code/advanceTimeAndAutomate.py";
        runAsyncCommand(command, null);
        syncData();
    }

    public void createNewRoom(String name, int roomId){
        String command = "python code/changeRoom.py create " + roomId + " " + name;
        runAsyncCommand(command, null);
    }

    public void addDevice(Room room,Device foundDevice){
        String command = "python code/changeRoom.py ";
        if (foundDevice.getType() == Device.Type.ACTUATOR){
            command += "connectActuator " + room.getRoomId() + " " + foundDevice.getName();
        }else{
            command += "connectSensor " + room.getRoomId() + " " + foundDevice.getName();
        }
        runAsyncCommand(command, null);
    }

    public void removeDevice(Room room,Device foundDevice){
        String command = "python code/changeRoom.py ";
        if (foundDevice.getType() == Device.Type.ACTUATOR){
            command += "removeActuator " + room.getRoomId() + " " + foundDevice.getName();
        }else{
            command += "removeSensor " + room.getRoomId() + " " + foundDevice.getName();
        }
        runAsyncCommand(command, null);
    }

    public void deleteRoom(int id){
        String command = "python code/changeRoom.py delete " + id ;
        runAsyncCommand(command, null);
    }

    public void setRanges(int tempMax, int tempMin, int humMax, int humMin){
        String command = "python code/setValue.py ranges " + tempMax + " " + tempMin + " " + humMax + " " + humMin;
        runAsyncCommand(command, null);
    }

    public void setTime(String week,int day,int time){
        String command = "python code/setValue.py date " + time + " " + day + " " + week;
        runAsyncCommand(command, null);
    }

    private ArrayList<String> runCommand (String command) {
        String hostname = "192.168.2.106";
        String username = "pi";
        String password = "IoTlove";
        try
        {
            Connection conn = new Connection(hostname); //init connection
            conn.connect(); //start connection to the hostname
            boolean isAuthenticated = conn.authenticateWithPassword(username,
                    password);
            if (isAuthenticated == false)
                throw new IOException("Authentication failed.");
            Session sess = conn.openSession();

            sess.execCommand(command);
            InputStream stdout = new StreamGobbler(sess.getStdout());
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));

//reads text
            ArrayList<String> responseList = new ArrayList<>();
            while (true){
                String line = br.readLine(); // read line
                if (line == null)
                    break;
                System.out.println("line" + line);
                responseList.add(line);
            }
            /* Show exit status, if available (otherwise "null") */
            System.out.println("ExitCode: " + sess.getExitStatus());
            sess.close(); // Close this session
            conn.close();
            return responseList;
        }
        catch (IOException e) {
            e.printStackTrace(System.err);
            //System.exit(2);
            return null;
            }
    }

    private void runAsyncCommand(String command, Consumer<ArrayList<String>> resultFunction) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        int localTaskId = getNewTaskId();
        taskQueue.add(localTaskId);

        executor.execute(() -> {
            while (true){
                if (connectionOccupied == false && taskQueue.peek() == localTaskId){
                    connectionOccupied = true;
                    taskQueue.poll();
                    break;
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            ArrayList<String> result = runCommand(command);


            handler.post(() -> {
                connectionOccupied = false;
                if (resultFunction == null){
                    return;
                }
                resultFunction.accept(result);
            });
        });
    }
}
