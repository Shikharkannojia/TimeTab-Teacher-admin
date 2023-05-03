package com.bpitindia.timetable.activities

import android.os.Bundle
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.AbsListView.MultiChoiceModeListener
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.bpitindia.timetable.adapters.ExamsAdapter
import com.bpitindia.timetable.model.Exam
import com.bpitindia.timetable.utils.AlertDialogsHelper
import com.bpitindia.timetable.utils.DbHelper
import com.bpitindia.timetable.utils.PreferenceUtil
import com.bpitindia.timetable.R
import java.util.*

class ExamsActivity : AppCompatActivity() {
    private val context: AppCompatActivity = this
    private var listView: ListView? = null
    private var adapter: ExamsAdapter? = null
    private var db: DbHelper? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(PreferenceUtil.getGeneralTheme(this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exams)
        initAll()
    }

    private fun initAll() {
        setupAdapter()
        setupListViewMultiSelect()
        setupCustomDialog()
    }

    private fun setupAdapter() {
        db = DbHelper(context)
        listView = findViewById(R.id.examslist)
        adapter = ExamsAdapter(
            db,
            this@ExamsActivity,
            listView,
            R.layout.listview_exams_adapter,
            db!!.exam
        )
        listView!!.setAdapter(adapter)
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
                    val removelist = ArrayList<Exam>()
                    val checkedItems = listView!!.checkedItemPositions
                    for (i in 0 until checkedItems.size()) {
                        val key = checkedItems.keyAt(i)
                        if (checkedItems[key]) {
                            db!!.deleteExamById(Objects.requireNonNull(adapter!!.getItem(key))!!)
                            removelist.add(adapter!!.examList[key])
                        }
                    }
                    adapter!!.examList.removeAll(removelist)
                    db!!.updateExam(adapter!!.exam)
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
        val alertLayout = layoutInflater.inflate(R.layout.dialog_add_exam, null)
        AlertDialogsHelper.getAddExamDialog(db, this@ExamsActivity, alertLayout, adapter!!)
    }
}