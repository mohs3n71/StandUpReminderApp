package com.pouryazdan.mohsen.standupreminder;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import net.sqlcipher.database.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final int interval = 1000 * 60 * 60; // 1 Second * 60 * 60 = 1 hour
    private final int snooze_interval = 1000 * 60 * 5; // 5 mins snooze
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        public void run() {

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("snooze", true);

            Intent cIntent = new Intent(getApplicationContext(), CancelBroadcast.class);
            cIntent.putExtra("notificationId", 1);

            PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            PendingIntent cpIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, cIntent, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this);
            builder.setContentText("Its's more than 1 hour you didn't stand !");
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setContentTitle(getString(R.string.app_name));
            builder.addAction(R.mipmap.ic_launcher, "Snooze", pIntent);
            builder.addAction(R.mipmap.ic_launcher, "Cancel", cpIntent);
            NotificationManagerCompat.from(MainActivity.this).notify(0, builder.build());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SQLiteDatabase.loadLibs(this);
        DbHandler db = new DbHandler(this);
        db.getWritableDatabase("P@ssw0rd");

        Intent mainIntent = getIntent();
        boolean snooze = mainIntent.getBooleanExtra("snooze", false);
        if (snooze) {
            handler.removeCallbacks(runnable);
            handler.postAtTime(runnable, System.currentTimeMillis() + snooze_interval);
            handler.postDelayed(runnable, snooze_interval);
        }

        Button btn = (Button) findViewById(R.id.stand_now);
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Calendar c = Calendar.getInstance();

                SimpleDateFormat hour = new SimpleDateFormat("HH", Locale.UK);
                String hh = hour.format(c.getTime());
                Integer hour_int = Integer.parseInt(hh);

                if (hour_int >= 0 && hour_int < 24) {
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
                    String formattedDate = df.format(c.getTime());

                    DbHandler db = new DbHandler(MainActivity.this);
                    StandData stand = db.findByDate(formattedDate);
                    if (stand != null) {
                        Log.e("found", "update " + stand.get_id() + " " + stand.get_count());
                        stand.set_count(stand.get_count() + 1);
                        db.updateStandData(stand);

                    } else {
                        stand = new StandData(formattedDate, 1);
                        db.addStandDate(stand);
                    }

                    handler.removeCallbacks(runnable);
                    handler.postAtTime(runnable, System.currentTimeMillis() + interval);
                    handler.postDelayed(runnable, interval);
                }
            }
        });


        final Button chart = (Button) findViewById(R.id.showChart);
        chart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chartIntent = new Intent(MainActivity.this, Chart.class);
                startActivity(chartIntent);
            }
        });

    }
}
