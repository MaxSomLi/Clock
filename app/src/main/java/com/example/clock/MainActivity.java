package com.example.clock;
import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    AlarmManager alarmManager;
    long mtime;
    int index, s;
    String swapString;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("Alarms", MODE_PRIVATE);
        Button setAlarmButton = findViewById(R.id.setAlarmButton);
        ListView alarmList = findViewById(R.id.alarmList);
        Set<String> set = sharedPreferences.getStringSet("Alarms", new HashSet<>());
        ArrayList<String> dataList = new ArrayList<>(set);
        s = dataList.size();
        for (int j = 0; j < s; j++) {
            mtime = 0;
            index = 0;
            for (int i = 0; i < s - j; i++) {
                LocalTime targetTime = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    targetTime = LocalTime.parse(dataList.get(i));
                }
                LocalTime currentTime = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    currentTime = LocalTime.now();
                }
                long minutesNow = 0, minutesThen = 0;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    minutesNow = currentTime.getHour() * 60L + currentTime.getMinute();
                    minutesThen = targetTime.getHour() * 60L + targetTime.getMinute();
                }
                long diff = minutesThen - minutesNow;
                if (diff < 0) {
                    diff += 1440;
                }
                if (diff > mtime) {
                    mtime = diff;
                    index = i;
                }
            }
            swapString = dataList.get(index);
            dataList.set(index, dataList.get(s - j - 1));
            dataList.set(s - j - 1, swapString);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, dataList);
        alarmList.setAdapter(adapter);
        setAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), SetAlarm.class));
            }
        });
        alarmList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setTitle("Delete Alarm");
                alertDialogBuilder.setMessage("Are you sure you want to delete this alarm?");
                alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String alarmToDelete = adapter.getItem(position);
                        Set<String> existingAlarmSet = sharedPreferences.getStringSet("Alarms", new HashSet<>());
                        Set<String> newAlarmSet = new HashSet<>(existingAlarmSet);
                        newAlarmSet.remove(alarmToDelete);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putStringSet("Alarms", newAlarmSet);
                        editor.apply();
                        Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                        alarmManager.cancel(pendingIntent);
                        recreate();
                    }
                });
                alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });
    }

}

