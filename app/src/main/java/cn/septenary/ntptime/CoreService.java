package cn.septenary.ntptime;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.Time;
import android.util.Log;

import org.apache.commons.net.ftp.FTPCmd;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class CoreService extends Service {

    public static final String ACTION = "ACTION_TIME_UPDATE";

    public CoreService() {
        mHandler = new Handler();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        registerReceiver(mTimeBroadcastReceiver, filter);
        mTicker.run();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int cmd = intent.getIntExtra("cmd", 0);
            Log.e("AAAA", "cmd: " + cmd);
            switch (cmd) {
                case 0:
                    break;
                case 1:
                    NTPTime.getInstance().updteTime();
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mTimeBroadcastReceiver);
    }

    private BroadcastReceiver mTimeBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            onTimeChanged();
        }
    };

    private android.os.Handler mHandler;

    private final Runnable mTicker = new Runnable() {
        public void run() {
            onTimeChanged();
            long now = SystemClock.uptimeMillis();
            long next = now + (1000 - now % 1000);
            mHandler.postAtTime(mTicker, next);
        }
    };

    private void onTimeChanged() {
        long time = NTPTime.getInstance().getCurrentTime();
        TimeZone timeZone = TimeZone.getDefault();
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTimeInMillis(time);
        String formateTime = SimpleDateFormat.getDateTimeInstance().format(calendar.getTime());
        Log.e("onTimeChanged", "" + formateTime);
        Intent intent = new Intent(ACTION);
        intent.putExtra("time", formateTime);
        LocalBroadcastManager.getInstance(this.getApplicationContext()).sendBroadcast(intent);
    }
}
