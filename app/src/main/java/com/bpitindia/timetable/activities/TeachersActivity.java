package com.bpitindia.timetable.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.bpitindia.timetable.adapters.TeachersAdapter;
import com.bpitindia.timetable.model.Teacher;
import com.bpitindia.timetable.utils.AlertDialogsHelper;
import com.bpitindia.timetable.utils.DbHelper;
import com.bpitindia.timetable.utils.PreferenceUtil;
import com.bpitindia.timetable.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class TeachersActivity extends AppCompatActivity {

    private final Context context = this;
    private ListView listView;
    private DbHelper db;
    private TeachersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PreferenceUtil.getGeneralTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teachers);
        FloatingActionButton doneButton = findViewById(R.id.updateteacher);
        doneButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(TeachersActivity.this);
            builder.setTitle("Upload Changes to Teacher's List");
            builder.setMessage("Do you want to upload the new changes to all the students?");
            builder.setPositiveButton("Upload", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int id) {
                    pushtofirebase();
                    Toast.makeText(getApplicationContext(),"List of Teachers Updated",Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // Perform action when Cancel is pressed
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        });


        getfromfirebase();
        initAll();
    }

    private void initAll() {
        setupAdapter();
        setupListViewMultiSelect();
        setupCustomDialog();
    }
    private void pushtofirebase() {
        // Get the list of teachers from the database.
        List<Teacher> teachers = db.getTeacher();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("App");

// Create a Firebase reference to the teachers node.
       // Firebase teachersRef = FirebaseDatabase.getInstance().getReference("teachers");

// Iterate over the list of teachers and add them to Firebase.
      /*  for (Teacher teacher : teachers) {
            m.push().setValue(teacher);
        }*/

        db = new DbHelper(this);


        for(int i = 0; i < teachers.size(); i++){
            String str = "T"+(i+1);
            DatabaseReference m = myRef.child("TimeTable").child("Teacher").child(str);
           // DatabaseReference m = myRef.child("TimeTable").child("Student").child(branch).child(semester).child(day).child(str);
            String name = teachers.get(i).getName();
            String post = teachers.get(i).getPost();
            String phonenumber = teachers.get(i).getPhonenumber();
            String email = teachers.get(i).getEmail();

            m.child("name").setValue(name);
            m.child("post").setValue(post);
            m.child("phonenumber").setValue(phonenumber);
            m.child("email").setValue(email);
            m.child("Color").setValue(teachers.get(i).getColor());
        }




    }
         private void getfromfirebase() {
             // Create a Firebase reference to the teachers node.
             FirebaseDatabase database = FirebaseDatabase.getInstance();
             DatabaseReference myRef = database.getReference("App");
             DatabaseReference m = myRef.child("TimeTable").child("Teacher");
             db = new DbHelper(this);
// Listen for changes to the teachers node.
             m.addValueEventListener(new ValueEventListener() {

                 @Override
                 public void onDataChange(@NonNull DataSnapshot snapshot) {

                     if (snapshot.exists()) {
                         int s = (int) snapshot.getChildrenCount();
                         for (int i = 1; i <= s; i++) {
                             String str = "T" + i;

                             m.child(str).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                                 @Override
                                 public void onSuccess(@NonNull DataSnapshot dataSnapshot) {
                                    // String color = Objects.requireNonNull(dataSnapshot.child("Subject").getValue()).toString();



                                     int color = Integer.parseInt(Objects.requireNonNull(dataSnapshot.child("Color").getValue()).toString());
                                     String email = Objects.requireNonNull(dataSnapshot.child("email").getValue()).toString();
                                     String name = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                                     String phonenumber = Objects.requireNonNull(dataSnapshot.child("phonenumber").getValue()).toString();
                                     String post = Objects.requireNonNull(dataSnapshot.child("post").getValue()).toString();

                                     ///progressBar.setProgress(50);
                                     //String name, String post, String phonenumber, String email, int color

                                     Teacher t1 = new Teacher(name,post,phonenumber,email,color);
                                     db.insertTeacher(t1);
                                     initAll();
                                     //progressBar.setVisibility(View.GONE);
                                 }
                             }).addOnFailureListener(new OnFailureListener() {
                                 @Override
                                 public void onFailure(@NonNull Exception e) {
                                     // Handle any errors that may occur
                                     initAll();
                                 }
                             });
                         }
                     } else {
                         initAll();
                     }





                     /* // Get the list of teachers from Firebase.
                     List<Teacher> teachers = new ArrayList<>();
                     for (DataSnapshot teacherSnapshot : dataSnapshot.getChildren()) {
                         teachers.add(teacherSnapshot.getValue(Teacher.class));
                     }

                     // Do something with the list of teachers.
                     db.insertTeacher(teachers);*/





                 }

                 @Override
                 public void onCancelled(DatabaseError databaseError) {
                     // Handle the error.
                 }
             });
         }

    private void setupAdapter() {
        db = new DbHelper(context);
        listView = findViewById(R.id.teacherlist);
        adapter = new TeachersAdapter(db, TeachersActivity.this, listView, R.layout.listview_teachers_adapter, db.getTeacher());
        listView.setAdapter(adapter);
    }

    private void setupListViewMultiSelect() {
        final CoordinatorLayout coordinatorLayout = findViewById(R.id.coordinatorTeachers);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(@NonNull ActionMode mode, int position, long id, boolean checked) {
                final int checkedCount = listView.getCheckedItemCount();
                mode.setTitle(checkedCount + " " + getResources().getString(R.string.selected));
                if (checkedCount == 0) mode.finish();
            }

            @Override
            public boolean onCreateActionMode(@NonNull ActionMode mode, Menu menu) {
                MenuInflater menuInflater = mode.getMenuInflater();
                menuInflater.inflate(R.menu.toolbar_action_mode, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(@NonNull final ActionMode mode, @NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        ArrayList<Teacher> removelist = new ArrayList<>();
                        SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
                        for (int i = 0; i < checkedItems.size(); i++) {
                            int key = checkedItems.keyAt(i);
                            if (checkedItems.get(key)) {
                                db.deleteTeacherById(Objects.requireNonNull(adapter.getItem(key)));
                                removelist.add(adapter.getTeacherList().get(key));
                            }
                        }
                        adapter.getTeacherList().removeAll(removelist);
                        db.updateTeacher(adapter.getTeacher());
                        adapter.notifyDataSetChanged();
                        mode.finish();
                        return true;

                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }
        });
    }

    private void setupCustomDialog() {
        final View alertLayout = getLayoutInflater().inflate(R.layout.dialog_add_teacher, null);
        AlertDialogsHelper.getAddTeacherDialog(db, TeachersActivity.this, alertLayout, adapter);
    }
}
