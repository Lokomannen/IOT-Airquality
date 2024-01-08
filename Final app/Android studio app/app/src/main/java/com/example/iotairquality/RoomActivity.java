package com.example.iotairquality;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class RoomActivity extends ActivitySyncer {

    private Room room;
    private String fileName;
    private LinearLayout deviceLayout;

    private ArrayList<View> selectedDevices;
    private ArrayList<View> selectedDeletionDevices;

    private ConnectionManager connectionManager;
    private DeviceManager deviceManager;

    private RoomManager roomManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        int id  = intent.getIntExtra("id", -1);
        if(name == null || id == -1){
            return;
        }

        createSpinner(R.id.spnWeek2, R.array.weeks_array);
        createSpinner(R.id.spnDay2, R.array.days_array);
        createSpinner(R.id.spnTime2, R.array.time_array);

        roomManager = RoomManager.getInstance();

        room = roomManager.findRoom(id);
        if (room == null){
            room = new Room(id, name, Room.Quality.UNKNOWN, Room.Status.INACTIVE);
        }
        fileName = room.getFileName();
        TextView txtRoomName = (TextView) findViewById(R.id.txtRoomName);
        txtRoomName.setText(room.getRoomName());
        deviceLayout = (LinearLayout) findViewById(R.id.layDevices);
        selectedDevices = new ArrayList<>();
        selectedDeletionDevices = new ArrayList<>();
        connectionManager = ConnectionManager.getInstance(this);
        deviceManager = DeviceManager.getInstance();

        setButtonClickables();
        createExistingDevicePreviews();

        connectionManager.syncData();

    }

    /*private void saveRoom(Room newRoom){
        File directory = new File(getFilesDir(), "Rooms");
        if (!directory.exists()) {
            directory.mkdir(); // Create the directory if it doesn't exist
        }
        //String fileName = newRoom.getRoomName() + "_" + newRoom.getRoomId() +".ser";
        File file = new File(directory, fileName);

        try (FileOutputStream fos = new FileOutputStream(file);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            oos.writeObject(newRoom);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
    /*private Room loadRoom(int roomId, String roomName) {
        File file = new File(getFilesDir() + "/Rooms", roomName + "_" + roomId + ".ser");

        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {

            return (Room) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }*/

    private void setButtonClickables(){
        Button btnAddDevice = (Button) findViewById(R.id.btnChangeDevice);
        btnAddDevice.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showAvailableDevices();
            }
        });

        Button btnAccept = (Button) findViewById(R.id.btnAccept);
        btnAccept.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                acceptDevices();
            }
        });

        Button btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                cancelDevices();
            }
        });

        Button btnRoomAdvanceTime = (Button) findViewById(R.id.btnRoomAdvanceTime);
        btnRoomAdvanceTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                connectionManager.advanceTime();
            }
        });

        Button btnDeleteRoom = (Button) findViewById(R.id.btnDeleteRoom);
        btnDeleteRoom.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                deleteRoom();
            }
        });

        Button btnSetTime = (Button) findViewById(R.id.btnSetTime1);
        btnSetTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setTime();
            }
        });
    }


    private void deleteRoom(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("command", "delete");
        intent.putExtra("roomId", room.getRoomId());
        startActivity(intent);

        roomManager.removeRoom(room);
    }

    private void setChangeRoomClickables(){
        LinearLayout layDevices = (LinearLayout)  findViewById(R.id.layDevices);
        for (int i = 1; i < layDevices.getChildCount(); i++) {
            View itemView = layDevices.getChildAt(i);
            itemView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    selectDeletionDevice(itemView);
                }
            });
        }
    }

    private void showAvailableDevices(){
        ConstraintLayout grpStandard = (ConstraintLayout) findViewById(R.id.grpStandard);
        ConstraintLayout grpAdd = (ConstraintLayout) findViewById(R.id.grpAdd);
        LinearLayout layAvailableDevices = (LinearLayout)  findViewById(R.id.layAvailableDevices);
        grpStandard.setVisibility(View.GONE);
        grpAdd.setVisibility(View.VISIBLE);
        layAvailableDevices.setVisibility(View.VISIBLE);

        setChangeRoomClickables();

        connectionManager.getDevicesAndValues();
    }

    private void hideDevices(){
        ConstraintLayout grpStandard = (ConstraintLayout) findViewById(R.id.grpStandard);
        ConstraintLayout grpAdd = (ConstraintLayout) findViewById(R.id.grpAdd);
        LinearLayout layAvailableDevices = (LinearLayout)  findViewById(R.id.layAvailableDevices);
        grpStandard.setVisibility(View.VISIBLE);
        grpAdd.setVisibility(View.GONE);
        layAvailableDevices.setVisibility(View.GONE);
        layAvailableDevices.removeViews(1,layAvailableDevices.getChildCount()-1);

        createExistingDevicePreviews();
    }

    private void createExistingDevicePreviews(){
        deviceLayout.removeViews(1, deviceLayout.getChildCount() - 1);

        Device[] deviceArray = room.getDevices();

        for (int i = 0; i < deviceArray.length; i++) {
            createDevicePreview(deviceArray[i]);
        }
    }

    private void createAvailableDevicePreview(Device device, String type){
        String[] displayTextArray = device.getDisplayedText();
        View itemView = createItemListPreview(displayTextArray[0], displayTextArray[1], displayTextArray[2]);
        itemView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                selectDevice(itemView);
            }
        });
        TextView typeField = itemView.findViewById(R.id.txtType);
        typeField.setText(type);
        LinearLayout availableDeviceLayout = (LinearLayout) findViewById(R.id.layAvailableDevices);
        availableDeviceLayout.addView(itemView);
    }

    private void createDevicePreview(Device device){
        String[] displayTextArray = device.getDisplayedText();
        View itemView = createItemListPreview(displayTextArray[0], displayTextArray[1], displayTextArray[2]);
        if (device.getType() == Device.Type.ACTUATOR){
            itemView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    toggleActuator((Actuator) device, itemView);
                }
            });
        }
        deviceLayout.addView(itemView);
    }

    private View createItemListPreview(String leftText, String middleText, String rightText){
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.item_list_layout, deviceLayout, false);
        // Set layout parameters for the child view
        LinearLayout.LayoutParams childParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        itemView.setLayoutParams(childParams);
        TextView txtLeft = (TextView) itemView.findViewById(R.id.txtLeft);
        TextView txtMiddle = (TextView) itemView.findViewById(R.id.txtMiddle);
        TextView txtRight = (TextView) itemView.findViewById(R.id.txtRight);

        txtLeft.setText(leftText);
        txtMiddle.setText(middleText);
        txtRight.setText(rightText);

        return itemView;
    }

    private void selectDevice(View itemView){
        Drawable selectedLayout;
        if(selectedDevices.contains(itemView)){
            selectedLayout = getDrawable(R.drawable.round_corners);
            selectedDevices.remove(itemView);
        }else {
            selectedLayout = getDrawable(R.drawable.round_corners_selected);
            selectedDevices.add(itemView);
        }
        itemView.setBackground(selectedLayout);
    }

    private void selectDeletionDevice(View itemView){
        Drawable selectedLayout;
        if(selectedDeletionDevices.contains(itemView)){
            selectedLayout = getDrawable(R.drawable.round_corners);
            selectedDeletionDevices.remove(itemView);
        }else {
            selectedLayout = getDrawable(R.drawable.round_corners_deletion);
            selectedDeletionDevices.add(itemView);
        }
        itemView.setBackground(selectedLayout);
    }

    private void acceptDevices(){
        for (int i = 0; i < selectedDevices.size(); i++) {
            View deviceView = selectedDevices.get(i);
            TextView nameField = deviceView.findViewById(R.id.txtLeft);
            String name = nameField.getText().toString();
            TextView typeField = deviceView.findViewById(R.id.txtType);
            String typeString = typeField.getText().toString();
            Device foundDevice = deviceManager.findDevice(name);
            if(foundDevice == null){
                TextView txtRight = deviceView.findViewById(R.id.txtRight);
                String txtRightString = txtRight.getText().toString();
                if (typeString.equals("actuator")){
                    foundDevice = new Actuator(name);
                    if (txtRightString.equals("Enabled")){
                        ((Actuator) foundDevice).setStatus(Room.Status.ACTIVE);
                    }else if (txtRightString.equals("Disabled")){
                        ((Actuator) foundDevice).setStatus(Room.Status.INACTIVE);
                    }else {
                        ((Actuator) foundDevice).setStatus(Room.Status.UNKNOWN);
                    }
                }else{
                    TextView txtMiddle = deviceView.findViewById(R.id.txtMiddle);
                    String txtMiddleString = txtMiddle.getText().toString();
                    String humidity = "";
                    String carbon = "";
                    for (int j = 0; j < txtRightString.length(); j++) {
                        if (txtRightString.charAt(j) == '%'){
                            humidity = txtRightString.substring(0,j + 1);
                            carbon = txtRightString.substring(j + 1, txtRightString.length()) ;
                        }
                    }
                    foundDevice = new Sensor(name);
                    ((Sensor) foundDevice).setTemperature(txtMiddleString);
                    ((Sensor) foundDevice).setHumidity(humidity);
                    ((Sensor) foundDevice).setCarbon(carbon);
                }
                deviceManager.addDevice(foundDevice);
            }

            room.addDevice(foundDevice);
            connectionManager.addDevice(room, foundDevice);
        }
        selectedDevices.clear();

        for (int i = 0; i < selectedDeletionDevices.size(); i++) {
            View deviceView = selectedDeletionDevices.get(i);
            TextView nameField = deviceView.findViewById(R.id.txtLeft);
            String name = nameField.getText().toString();
            Device foundDevice = deviceManager.findDevice(name);
            if(foundDevice == null){
                continue;
            }

            room.removeDevice(foundDevice);
            deviceManager.removeDevice(foundDevice);
            connectionManager.removeDevice(room, foundDevice);
        }
        selectedDeletionDevices.clear();
        hideDevices();
    }

    private void cancelDevices(){
        selectedDevices.clear();
        selectedDeletionDevices.clear();
        hideDevices();
    }

    private void toggleActuator(Actuator actuator){
        actuator.setEnabled(!actuator.isEnabled());
    }

    private void toggleActuator(Actuator actuator, View itemView){
        toggleActuator(actuator);

        TextView txtRight = (TextView) itemView.findViewById(R.id.txtRight);
        String displayedText = "Disabled";
        if (actuator.isEnabled()){
            displayedText = "Enabled";
        }
        txtRight.setText(displayedText);
    }

    public void handleSync(int tempMax, int tempMin, int humMax, int humMin, int time, int day, String week, ArrayList<Room> roomList) {

        Spinner spnWeek = findViewById(R.id.spnWeek2);
        Spinner spnDay = findViewById(R.id.spnDay2);
        Spinner spnTime = findViewById(R.id.spnTime2);

        int weekNumber = Character.getNumericValue( week.charAt(week.length() - 1) );
        spnWeek.setSelection(weekNumber - 1);
        spnDay.setSelection(day);
        spnTime.setSelection(time);

        room = roomManager.findRoom(room.getRoomId());

        createExistingDevicePreviews();
    }

    public void handleGetSensorsAndValues(ArrayList<Sensor> sensorList, ArrayList<Actuator> actuatorList){
        for(Sensor sensor : sensorList){
            if (!room.hasDevice(sensor)){
                createAvailableDevicePreview(sensor, "sensor");
            }
        }
        for(Actuator actuator : actuatorList){
            if (!room.hasDevice(actuator)){
                createAvailableDevicePreview(actuator, "actuator");
            }
        }
    }

    private void setTime(){
        Spinner spnWeek = findViewById(R.id.spnWeek2);
        Spinner spnDay = findViewById(R.id.spnDay2);
        Spinner spnTime = findViewById(R.id.spnTime2);
        String week = "week_" + (spnWeek.getSelectedItemPosition() + 1 );
        int day = spnDay.getSelectedItemPosition();
        int time = spnTime.getSelectedItemPosition();

        connectionManager.setTime(week, day, time);
    }

    private void createSpinner(int spinnerId, int stringResource){
        Spinner spinner = (Spinner) findViewById(spinnerId);
// Create an ArrayAdapter using the string array and a default spinner layout.
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                stringResource,
                android.R.layout.simple_spinner_item
        );
// Specify the layout to use when the list of choices appears.
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner.
        spinner.setAdapter(adapter);
    }
}