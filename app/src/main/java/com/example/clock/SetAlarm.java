package com.example.clock;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class SetAlarm extends AppCompatActivity {
    public NumberPicker hours, minutes;
    public SharedPreferences sharedPreferences;
    AlarmManager alarmManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_set_alarm);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        sharedPreferences = getSharedPreferences("Alarms", MODE_PRIVATE);
        hours = findViewById(R.id.hours);
        minutes = findViewById(R.id.minutes);
        minutes.setMaxValue(59);
        hours.setMaxValue(23);
        Button choose = findViewById(R.id.choose);
        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(() -> {
                    addAlarm();
                    runOnUiThread(() -> startActivity(new Intent(v.getContext(), MainActivity.class)));
                }).start();
            }
        });
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    }
    void addAlarm() {
        int hour = hours.getValue();
        int minute = minutes.getValue();
        String time = String.format("%02d:%02d", hour, minute);
        Set<String> existingAlarmSet = sharedPreferences.getStringSet("Alarms", new HashSet<>());
        Set<String> newAlarmSet = new HashSet<>(existingAlarmSet);
        newAlarmSet.add(time);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("Alarms", newAlarmSet);
        editor.apply();
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }
}