package com.bpitindia.timetable.activities

import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bpitindia.timetable.model.Note
import com.bpitindia.timetable.utils.DbHelper
import com.bpitindia.timetable.utils.PreferenceUtil
import com.bpitindia.timetable.R
import java.util.*

class NoteInfoActivity : AppCompatActivity() {
    private var db: DbHelper? = null
    private var note: Note? = null
    private var text: EditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(PreferenceUtil.getGeneralTheme(this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_info)
        setupIntent()
    }

    private fun setupIntent() {
        db = DbHelper(this@NoteInfoActivity)
        note = intent.getSerializableExtra(NotesActivity.KEY_NOTE) as Note
        text = findViewById(R.id.edittextNote)
        if (Objects.requireNonNull(note)!!.text != null) {
            text!!.setText(note!!.text)
        }
    }

    override fun onBackPressed() {
        Objects.requireNonNull(note)!!.text = text!!.text.toString()
        db!!.updateNote(note!!)
        Toast.makeText(
            this@NoteInfoActivity,
            resources.getString(R.string.saved),
            Toast.LENGTH_SHORT
        ).show()
        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                Objects.requireNonNull(note)!!.text =
                    text!!.text.toString()
                db!!.updateNote(note!!)
                Toast.makeText(
                    this@NoteInfoActivity,
                    resources.getString(R.string.saved),
                    Toast.LENGTH_SHORT
                ).show()
                super.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}