package com.bpitindia.timetable.fragments

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.bpitindia.timetable.profiles.Profile
import com.bpitindia.timetable.profiles.ProfileManagement
import com.bpitindia.timetable.utils.DbHelper
import com.bpitindia.timetable.R

class ProfileActivityFragment : Fragment() {
    private var adapter: ProfileListAdapter? = null
    private var preferredProfilePos = ProfileManagement.getPreferredProfilePosition()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
        adapter = ProfileListAdapter(requireContext(), 0)
        (root.findViewById<View>(R.id.profile_list) as ListView).adapter = adapter
        requireActivity().findViewById<View>(R.id.fab)
            .setOnClickListener { v: View? -> openAddDialog() }
        return root
    }

    private inner class ProfileListAdapter internal constructor(con: Context, resource: Int) :
        ArrayAdapter<Array<String?>?>(con, resource) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.list_profiles_entry, null)
            }
            return if (position < ProfileManagement.getSize()) generateProfileView(
                convertView!!,
                position
            ) else generateInfoView(
                convertView!!
            )
        }

        override fun getCount(): Int {
            return ProfileManagement.getSize() + 1
        }

        private fun generateProfileView(base: View, position: Int): View {
            val p = ProfileManagement.getProfile(position)
            val name = base.findViewById<TextView>(R.id.profilelist_name)
            name.textSize = 20f
            name.text = p.name
            val edit = base.findViewById<ImageButton>(R.id.profilelist_edit)
            edit.visibility = View.VISIBLE
            edit.setOnClickListener { v: View? -> openEditDialog(position) }
            val delete = base.findViewById<ImageButton>(R.id.profilelist_delete)
            delete.visibility = View.VISIBLE
            delete.setOnClickListener { v: View? -> openDeleteDialog(position) }
            val star = base.findViewById<ImageButton>(R.id.profilelist_preferred)
            star.visibility = View.VISIBLE
            if (position == preferredProfilePos) {
                star.setImageResource(R.drawable.ic_star_black_24dp)
            } else {
                star.setImageResource(R.drawable.ic_star_border_black_24dp)
            }
            star.setOnClickListener { v: View? -> setPreferredProfile(position) }
            return base
        }

        private fun generateInfoView(base: View): View {
            base.findViewById<View>(R.id.profilelist_edit).visibility = View.GONE
            base.findViewById<View>(R.id.profilelist_delete).visibility =
                View.GONE
            base.findViewById<View>(R.id.profilelist_preferred).visibility = View.GONE
            val name = base.findViewById<TextView>(R.id.profilelist_name)
            name.text = getString(R.string.preferred_profile_explanation)
            name.textSize = 12f
            return base
        }
    }

    private fun openAddDialog() {
        val builder = MaterialDialog.Builder(requireActivity())
        builder.title(getString(R.string.profiles_add))

        // Set up the input
        val input = EditText(requireContext())
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.hint = getString(R.string.name)
        builder.customView(input, true)

        // Set up the buttons
        builder.onPositive { dialog: MaterialDialog?, which: DialogAction? ->
            //Add Profile
            val inputText = input.text.toString()
            if (!inputText.trim { it <= ' ' }.isEmpty()) ProfileManagement.addProfile(
                Profile(
                    inputText
                )
            )

            adapter!!.notifyDataSetChanged()
        }
        builder.onNegative { dialog: MaterialDialog, which: DialogAction? -> dialog.dismiss() }
        builder.positiveText(R.string.add)
        builder.negativeText(R.string.cancel)
        builder.show()
    }

    private fun openEditDialog(position: Int) {
        val builder = MaterialDialog.Builder(requireContext())
        builder.title(getString(R.string.profiles_edit))

        // Set up the input
        val base = LinearLayout(requireContext())
        base.orientation = LinearLayout.VERTICAL
        val name = EditText(requireContext())
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        name.inputType = InputType.TYPE_CLASS_TEXT
        name.setText(ProfileManagement.getProfile(position).name)
        name.setHint(R.string.name)
        base.addView(name)
        builder.customView(base, true)

        // Set up the buttons
        builder.positiveText(getString(R.string.ok))
        builder.negativeText(getString(R.string.cancel))
        builder.onPositive { dialog: MaterialDialog?, which: DialogAction? ->
            val profile = ProfileManagement.getProfile(position)
            val nameText = name.text.toString()
            //Do not enter empty text
            ProfileManagement.editProfile(position, Profile(if (nameText.trim { it <= ' ' }
                    .isEmpty()) profile.name else nameText))
            adapter!!.notifyDataSetChanged()
        }
        builder.onNegative { dialog: MaterialDialog, which: DialogAction? -> dialog.dismiss() }
        builder.show()
    }

    private fun openDeleteDialog(position: Int) {
        val p = ProfileManagement.getProfile(position)
        MaterialDialog.Builder(requireContext())
            .title(getString(R.string.profiles_delete_submit_heading))
            .content(getString(R.string.profiles_delete_message, p.name))
            .positiveText(getString(R.string.yes))
            .onPositive { dialog: MaterialDialog?, which: DialogAction? ->
                ProfileManagement.removeProfile(position)
                val dbHelper = DbHelper(context)
                dbHelper.deleteAll()
                adapter!!.notifyDataSetChanged()
            }
            .onNegative { dialog: MaterialDialog, which: DialogAction? -> dialog.dismiss() }
            .negativeText(getString(R.string.no))
            .show()
    }

    private fun setPreferredProfile(position: Int) {
        ProfileManagement.setPreferredProfilePosition(position)
        preferredProfilePos = ProfileManagement.getPreferredProfilePosition()
        adapter!!.notifyDataSetChanged()
    }
}