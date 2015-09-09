package cn.septenary.ntptime;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class MainActivity extends AppCompatActivity {

    private TextView mDigitalClock;

    private AnalogClock mAnalogClock;

    private TextView mDateView;

    private String mTimeZone = "GMT+8";

    private String mTimeZoneSummary = "(GMT+8:00) 北京";

    private final android.os.Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService(new Intent(this, CoreService.class));
        mDigitalClock = (TextView) findViewById(R.id.digital_clock);
        mAnalogClock = (AnalogClock) findViewById(R.id.analog_clock);
        mDateView = (TextView) findViewById(R.id.date);
        mAnalogClock.setAutoTick(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String tz = prefs.getString(SettingsActivity.KEY_HOME_TZ, mTimeZone);
        if (!TextUtils.equals(mTimeZone, tz)) {
            mTimeZone = tz;
            ActionBar bar = getSupportActionBar();
            if (bar != null) {
                mTimeZoneSummary = prefs.getString(SettingsActivity.KEY_HOME_TZ_SUMMARY, mTimeZoneSummary);
                bar.setTitle(mTimeZoneSummary);
            }
            onTimeChanged();
            NTPTime.getInstance().updteNTPTime();
        }
        mTicker.run();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mTicker);
    }

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
        Date date = new Date(time);
        mAnalogClock.onTimeChanged(time);

        DateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
        timeFormat.setTimeZone(TimeZone.getTimeZone(mTimeZone));
        mDigitalClock.setText(timeFormat.format(date));

        DateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日", Locale.ENGLISH);
        dateFormat.setTimeZone(TimeZone.getTimeZone(mTimeZone));
        mDateView.setText(dateFormat.format(date));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
