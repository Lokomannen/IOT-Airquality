package com.example.iotairquality;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActivitySyncer {

    private LinearLayout roomsLayout;

    private RoomManager roomManager;
    private ConnectionManager connectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        connectionManager = ConnectionManager.getInstance(this);

        roomsLayout = (LinearLayout) findViewById(R.id.layRooms);

        Intent intent = getIntent();
        String command = intent.getStringExtra("command");
        if (command != null){
            if (command.equals("delete")){
                int roomId = intent.getIntExtra("roomId", -1);
                if (roomId != -1){
                    roomsLayout.removeAllViews();
                    connectionManager.deleteRoom(roomId);
                }
            }
        }

        connectionManager.syncData();

        roomManager = RoomManager.getInstance();

        //loadAllRooms();

        createSpinner(R.id.spnWeek, R.array.weeks_array);
        createSpinner(R.id.spnDay, R.array.days_array);
        createSpinner(R.id.spnTime, R.array.time_array);

        setButtonClickables();
    }

    private void setButtonClickables(){
        Button btnNewRoom = (Button) findViewById(R.id.btnNewRoom);
        btnNewRoom.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                promptNewRoom(v);
            }
        });

        Button btnAdvanceTime = (Button) findViewById(R.id.btnAdvanceTime);
        btnAdvanceTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                connectionManager.advanceTime();
            }
        });

        Button btnRanges = (Button) findViewById(R.id.btnRanges);
        btnRanges.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setRanges();
            }
        });

        Button btnSetTime = (Button) findViewById(R.id.btnSetTime);
        btnSetTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setTime();
            }
        });
    }

    private void setTime(){
        Spinner spnWeek = findViewById(R.id.spnWeek);
        Spinner spnDay = findViewById(R.id.spnDay);
        Spinner spnTime = findViewById(R.id.spnTime);
        String week = "week_" + (spnWeek.getSelectedItemPosition() + 1 );
        int day = spnDay.getSelectedItemPosition();
        int time = spnTime.getSelectedItemPosition();

        connectionManager.setTime(week, day, time);
    }

    private void setRanges(){
        TextInputEditText txtTempMax = findViewById(R.id.txtTempMax);
        TextInputEditText txtTempMin = findViewById(R.id.txtTempMin);

        TextInputEditText txtHumMax = findViewById(R.id.txtHumMax);
        TextInputEditText txtHumMin = findViewById(R.id.txtHumMin);

        try {
            int tempMax = formatRange(txtTempMax);
            int tempMin = formatRange(txtTempMin);
            int humMax = formatRange(txtHumMax);
            int humMin = formatRange(txtHumMin);
            connectionManager.setRanges(tempMax, tempMin, humMax, humMin);
        }catch (Exception e){
            txtTempMax.setText("must be number");
            txtTempMin.setText("must be number");
            txtHumMax.setText("must be number");
            txtHumMin.setText("must be number");
        }
    }

    private int formatRange(TextInputEditText textInput) throws Exception {
        try {
            String text = textInput.getText().toString();
            int number = Integer.parseInt(text);
            return number;
        }catch (Exception e){
            throw new Exception("must be a integer");
        }
    }

    private void createNewRoom(String name){
        int roomId = roomManager.getNewRoomId();
        Room newRoom = new Room(roomId, name, Room.Quality.UNKNOWN, Room.Status.INACTIVE);

        roomManager.addRoom(newRoom);

        connectionManager.createNewRoom(name, roomId);

        createRoomPreview(newRoom);

        //saveRoomToFile(newRoom);
    }

    private void createRoomPreview(Room room){
        View roomView = createItemListPreview(room.getRoomName(), room.getQuality(), room.getStatus());
        roomView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                goToRoom(room.getRoomId(), room.getRoomName());
            }
        });
        roomsLayout.addView(roomView);
    }

    private View createItemListPreview(String leftText, String middleText, String rightText){
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.item_list_layout, roomsLayout, false);
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

    /*private void saveRoomToFile(Room newRoom){
        File directory = new File(getFilesDir(), "Rooms");
        if (!directory.exists()) {
            directory.mkdir(); // Create the directory if it doesn't exist
        }
        String fileName = newRoom.getRoomName() + "_" + newRoom.getRoomId() +".ser";
        File file = new File(directory, fileName);

        try (FileOutputStream fos = new FileOutputStream(file);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            oos.writeObject(newRoom);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    /*private void loadAllRooms() {
        List<Room> roomList = new ArrayList<>();

        File directory = new File(getFilesDir() + "/Rooms");
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".ser")) {
                    Room room = loadRoom(file);
                    if (room != null) {
                        roomList.add(room);
                        createRoomPreview(room);
                    }
                }
            }
        }
    }*/


    /*private Room loadRoom(File file) {
        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {

            return (Room) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }*/

    private void promptNewRoom(View view){
        View view1 = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_popup, null);
        TextInputEditText editText = view1.findViewById(R.id.editText);
        AlertDialog alertDialog = new MaterialAlertDialogBuilder(MainActivity.this)
                .setTitle("What will the room be called?")
                .setView(view1)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        createNewRoom(editText.getText().toString());
                        dialogInterface.dismiss();
                    }
            }).setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).create();
        alertDialog.show();
    }

    private void goToRoom(int id, String name){
        Intent intent = new Intent(MainActivity.this,RoomActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("name", name);
        startActivity(intent);
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

    public void handleSync(int tempMax, int tempMin, int humMax, int humMin, int time, int day, String week, ArrayList<Room> roomList){
        TextInputEditText txtTempMax = findViewById(R.id.txtTempMax);
        TextInputEditText txtTempMin = findViewById(R.id.txtTempMin);

        TextInputEditText txtHumMax = findViewById(R.id.txtHumMax);
        TextInputEditText txtHumMin = findViewById(R.id.txtHumMin);

        txtTempMax.setText("" + tempMax);
        txtTempMin.setText("" + tempMin);
        txtHumMax.setText("" + humMax);
        txtHumMin.setText("" + humMin);

        Spinner spnWeek = findViewById(R.id.spnWeek);
        Spinner spnDay = findViewById(R.id.spnDay);
        Spinner spnTime = findViewById(R.id.spnTime);

        int weekNumber = Character.getNumericValue( week.charAt(week.length() - 1) );
        spnWeek.setSelection(weekNumber - 1);
        spnDay.setSelection(day);
        spnTime.setSelection(time);


        roomsLayout.removeAllViews();
        for(Room room : roomList){
            createRoomPreview(room);
        }
    }

    public void handleGetSensorsAndValues(ArrayList<Sensor> sensorList, ArrayList<Actuator> actuatorList){ // user has left room activity before data arrived

    }
}