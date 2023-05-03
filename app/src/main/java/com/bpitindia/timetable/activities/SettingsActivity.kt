package com.bpitindia.timetable.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.bpitindia.timetable.fragments.SettingsFragment
import com.bpitindia.timetable.utils.PreferenceUtil
import com.bpitindia.timetable.R
import java.util.*

class SettingsActivity : AppCompatActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    @JvmField
    var loadedFragments = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(PreferenceUtil.getGeneralTheme(this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager.beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {
        // Instantiate the new Fragment
        val args = pref.extras
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
            classLoader,
            pref.fragment
        )
        fragment.arguments = args
        fragment.setTargetFragment(caller, 0)
        // Replace the existing Fragment with the new Fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.settings, fragment)
            .addToBackStack(null)
            .commit()
        try {
            Objects.requireNonNull(supportActionBar)!!.title = pref.title
        } catch (ignore: Exception) {
        }
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (loadedFragments == 0) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            loadedFragments--
            try {
                Objects.requireNonNull(supportActionBar)?.setTitle(R.string.settings)
            } catch (ignore: Exception) {
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        const val KEY_SEVEN_DAYS_SETTING = "sevendays"
        const val KEY_SCHOOL_WEBSITE_SETTING = "schoolwebsite"
        const val KEY_START_WEEK_ON_SUNDAY = "start_sunday"
    }
}