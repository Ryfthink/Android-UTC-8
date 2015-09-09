/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.septenary.ntptime;

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Septenary on 15-9-7.
 * Settings for the TimeZone
 */
public class SettingsActivity extends BaseSettingActivity implements Preference.OnPreferenceChangeListener {

    public static final String KEY_HOME_TZ = "home_time_zone";

    public static final String KEY_HOME_TZ_SUMMARY = "home_time_zone_summary";

    private static CharSequence[][] mTimezones;
    private static Locale mLocale;
    private long mTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        }

        // We don't want to reconstruct the timezone list every single time
        // onResume() is called so we do it once in onCreate
        ListPreference listPref;
        listPref = (ListPreference) findPreference(KEY_HOME_TZ);
        if (mTimezones == null || isLocaleChanged()) {
            mTime = System.currentTimeMillis();
            mTimezones = getAllTimezones();
        }

        listPref.setEntryValues(mTimezones[0]);
        listPref.setEntries(mTimezones[1]);
        listPref.setSummary(listPref.getEntry());
        listPref.setOnPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object newValue) {
        if (KEY_HOME_TZ.equals(pref.getKey())) {
            final ListPreference listPref = (ListPreference) pref;
            final int idx = listPref.findIndexOfValue((String) newValue);
            CharSequence summary = listPref.getEntries()[idx];
            listPref.setSummary(summary);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().putString(KEY_HOME_TZ_SUMMARY, summary.toString()).apply();
        }
        return true;
    }

    private void refresh() {
        ListPreference listPref = (ListPreference) findPreference(KEY_HOME_TZ);
        if (mTimezones == null || isLocaleChanged()) {
            mTime = System.currentTimeMillis();
            mTimezones = getAllTimezones();
        }
        listPref.setEnabled(true);
        listPref.setEntryValues(mTimezones[0]);
        listPref.setEntries(mTimezones[1]);
        listPref.setSummary(listPref.getEntry());
    }

    private class TimeZoneRow implements Comparable<TimeZoneRow> {
        private static final boolean SHOW_DAYLIGHT_SAVINGS_INDICATOR = false;

        public final String mId;
        public final String mDisplayName;
        public final int mOffset;

        public TimeZoneRow(String id, String name) {
            mId = id;
            TimeZone tz = TimeZone.getTimeZone(id);
            boolean useDaylightTime = tz.useDaylightTime();
            mOffset = tz.getOffset(mTime);
            mDisplayName = buildGmtDisplayName(id, name, useDaylightTime);
        }

        @Override
        public int compareTo(TimeZoneRow another) {
            return mOffset - another.mOffset;
        }

        public String buildGmtDisplayName(String id, String displayName, boolean useDaylightTime) {
            int p = Math.abs(mOffset);
            StringBuilder name = new StringBuilder("(GMT");
            name.append(mOffset < 0 ? '-' : '+');

            name.append(p / DateUtils.HOUR_IN_MILLIS);
            name.append(':');

            int min = p / 60000;
            min %= 60;

            if (min < 10) {
                name.append('0');
            }
            name.append(min);
            name.append(") ");
            name.append(displayName);
            if (useDaylightTime && SHOW_DAYLIGHT_SAVINGS_INDICATOR) {
                name.append(" \u2600"); // Sun symbol
            }
            return name.toString();
        }
    }


    /**
     * Returns an array of ids/time zones. This returns a double indexed array
     * of ids and time zones for Calendar. It is an inefficient method and
     * shouldn't be called often, but can be used for one time generation of
     * this list.
     *
     * @return double array of tz ids and tz names
     */
    public CharSequence[][] getAllTimezones() {
        Resources resources = this.getResources();
        String[] ids = resources.getStringArray(R.array.timezone_values);
        String[] labels = resources.getStringArray(R.array.timezone_labels);
        int minLength = ids.length;
        if (ids.length != labels.length) {
            minLength = Math.min(minLength, labels.length);
            Log.e("AAAA", "Timezone ids and labels have different length!");
        }
        List<TimeZoneRow> timezones = new ArrayList<TimeZoneRow>();
        for (int i = 0; i < minLength; i++) {
            timezones.add(new TimeZoneRow(ids[i], labels[i]));
        }
        Collections.sort(timezones);

        CharSequence[][] timeZones = new CharSequence[2][timezones.size()];
        int i = 0;
        for (TimeZoneRow row : timezones) {
            timeZones[0][i] = row.mId;
            timeZones[1][i++] = row.mDisplayName;
        }
        return timeZones;
    }

    private boolean isLocaleChanged() {
        Resources resource = getResources();
        if (resource != null) {
            Configuration config = resource.getConfiguration();
            if (config != null) {
                if (mLocale == config.locale) {
                    return false;
                }
                mLocale = config.locale;
            }
        }
        return true;
    }
}
