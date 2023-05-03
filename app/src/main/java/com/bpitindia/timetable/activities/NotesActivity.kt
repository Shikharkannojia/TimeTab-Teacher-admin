package com.bpitindia.timetable.activities

import android.content.Intent
import android.os.Bundle
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AbsListView.MultiChoiceModeListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.bpitindia.timetable.adapters.NotesAdapter
import com.bpitindia.timetable.model.Note
import com.bpitindia.timetable.profiles.ProfileManagement
import com.bpitindia.timetable.utils.AlertDialogsHelper
import com.bpitindia.timetable.utils.DbHelper
import com.bpitindia.timetable.utils.PreferenceUtil
import com.bpitindia.timetable.R
import java.util.*

class NotesActivity : AppCompatActivity() {
    private val context: AppCompatActivity = this
    private var listView: ListView? = null
    private var db: DbHelper? = null
    private var adapter: NotesAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(PreferenceUtil.getGeneralTheme(this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)
        db = if (ACTION_SHOW.equals(intent.action, ignoreCase = true)) {
            DbHelper(this, ProfileManagement.loadPreferredProfilePosition())
        } else {
            DbHelper(this)
        }
        initAll()
    }

    private fun initAll() {
        setupAdapter()
        setupListViewMultiSelect()
        setupCustomDialog()
    }

    private fun setupAdapter() {
        listView = findViewById(R.id.notelist)
        adapter = NotesAdapter(
            db,
            this@NotesActivity,
            listView,
            R.layout.listview_notes_adapter,
            db!!.note
        )
        listView!!.setAdapter(adapter)
        listView!!.setOnItemClickListener(OnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
            val intent = Intent(context, NoteInfoActivity::class.java)
            intent.putExtra(KEY_NOTE, adapter!!.noteList[position])
            startActivity(intent)
        })
    }

    private fun setupListViewMultiSelect() {
        listView!!.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL
        listView!!.setMultiChoiceModeListener(object : MultiChoiceModeListener {
            override fun onItemCheckedStateChanged(
                mode: ActionMode,
                position: Int,
                id: Long,
                checked: Boolean
            ) {
                val checkedCount = listView!!.checkedItemCount
                mode.title = checkedCount.toString() + " " + resources.getString(R.string.selected)
                if (checkedCount == 0) mode.finish()
            }

            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                val menuInflater = mode.menuInflater
                menuInflater.inflate(R.menu.toolbar_action_mode, menu)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                if (item.itemId == R.id.action_delete) {
                    val removelist = ArrayList<Note>()
                    val checkedItems = listView!!.checkedItemPositions
                    for (i in 0 until checkedItems.size()) {
                        val key = checkedItems.keyAt(i)
                        if (checkedItems[key]) {
                            db!!.deleteNoteById(
                                Objects.requireNonNull(
                                    adapter!!.getItem(key)
                                )!!
                            )
                            removelist.add(adapter!!.noteList[key])
                        }
                    }
                    adapter!!.noteList.removeAll(removelist)
                    db!!.updateNote(adapter!!.note)
                    adapter!!.notifyDataSetChanged()
                    mode.finish()
                    return true
                }
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode) {}
        })
    }

    private fun setupCustomDialog() {
        val alertLayout = layoutInflater.inflate(R.layout.dialog_add_note, null)
        AlertDialogsHelper.getAddNoteDialog(db, this@NotesActivity, alertLayout, adapter!!)
    }

    override fun onResume() {
        super.onResume()
        adapter!!.clear()
        adapter!!.addAll(db!!.note)
        adapter!!.notifyDataSetChanged()
    }

    companion object {
        const val ACTION_SHOW = "showNotes"
        const val KEY_NOTE = "note"
    }
}