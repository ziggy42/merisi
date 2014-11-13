package com.andreapivetta.changemywall.background;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.andreapivetta.changemywall.Utilities;

import java.util.Calendar;


public class StartupService extends IntentService {
    private SharedPreferences mSharedPreferences;

    public StartupService() {
        super("StartupService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.mSharedPreferences = getApplicationContext().getSharedPreferences(Utilities.MY_PREF, 0);
    }

    @SuppressWarnings("static-access")
    @Override
    protected void onHandleIntent(Intent intent) {
        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent startupIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
                startupIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        int interval = mSharedPreferences.getInt(Utilities.MINUTES, 120) * 60 * 1000;

        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(System.currentTimeMillis());
        time.add(Calendar.MILLISECOND, interval);
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, time.MILLISECOND,
                interval, pendingIntent);
    }

}
