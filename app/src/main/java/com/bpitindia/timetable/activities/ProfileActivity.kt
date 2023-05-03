
package com.bpitindia.timetable.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bpitindia.timetable.fragments.ProfileActivityFragment
import com.bpitindia.timetable.profiles.ProfileManagement
import com.bpitindia.timetable.utils.PreferenceUtil
import com.bpitindia.timetable.R

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(PreferenceUtil.getGeneralTheme(this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction().replace(R.id.fragment, ProfileActivityFragment())
            .commit()
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onBackPressed() {
        ProfileManagement.resetSelectedProfile()
        ProfileManagement.checkPreferredProfile()
        ProfileManagement.save(this, true)
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}