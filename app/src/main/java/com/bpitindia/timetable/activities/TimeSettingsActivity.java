package com.bpitindia.timetable.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.bpitindia.timetable.fragments.TimeSettingsFragment;
import com.bpitindia.timetable.utils.PreferenceUtil;
import com.bpitindia.timetable.R;

public class TimeSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PreferenceUtil.getGeneralTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_time);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings, new TimeSettingsFragment())
                .commit();
    }

    @Override
    public void onBackPressed() {
//        startActivity(new Intent(this, SummaryActivity.class));
        PreferenceUtil.setStartActivityShown(this, true);
        super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
