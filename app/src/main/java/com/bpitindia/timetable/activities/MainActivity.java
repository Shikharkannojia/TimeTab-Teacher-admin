package com.bpitindia.timetable.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ajts.androidmads.library.ExcelToSQLite;
import com.ajts.androidmads.library.SQLiteToExcel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.pd.chocobar.ChocoBar;
import com.bpitindia.timetable.adapters.FragmentsTabAdapter;
import com.bpitindia.timetable.fragments.WeekdayFragment;
import com.bpitindia.timetable.model.Week;
import com.bpitindia.timetable.profiles.ProfileManagement;
import com.bpitindia.timetable.receivers.DoNotDisturbReceiversKt;
import com.bpitindia.timetable.receivers.MidnightReceiver;
import com.bpitindia.timetable.utils.AlertDialogsHelper;
import com.bpitindia.timetable.utils.DbHelper;
import com.bpitindia.timetable.utils.NotificationUtil;
import com.bpitindia.timetable.utils.PreferenceUtil;
import com.bpitindia.timetable.utils.ShortcutUtils;
import com.bpitindia.timetable.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import info.isuru.sheriff.enums.SheriffPermission;
import info.isuru.sheriff.helper.Sheriff;
import info.isuru.sheriff.interfaces.PermissionListener;
import saschpe.android.customtabs.CustomTabsHelper;
import saschpe.android.customtabs.WebViewFallback;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FragmentsTabAdapter adapter;
    private ViewPager viewPager;
    private ProgressBar progressBar;
    private DbHelper db;

    private static final int showNextDayAfterSpecificHour = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PreferenceUtil.getGeneralThemeNoActionBar(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ProfileManagement.initProfiles(this);
        progressBar=findViewById(R.id.progressBar);

        if (Build.VERSION.SDK_INT >= 25) {
            ShortcutUtils.Companion.createShortcuts(this);
        }
        if (!PreferenceUtil.hasStartActivityBeenShown(this)) {
            new MaterialDialog.Builder(this)
                    .content(R.string.first_start_setup)
                    .positiveText(R.string.ok)
                    .onPositive((v, w) -> startActivity(new Intent(this, TimeSettingsActivity.class)))
                    .show();
        }


        initAll();

        FloatingActionButton doneButton = findViewById(R.id.update);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Upload Timetable");
                builder.setMessage("Do you want to upload the new time table to all the students");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        putOnFirebase();
                        Toast.makeText(getApplicationContext(),"Time Table Uploaded To All The Students",Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Perform action when Cancel is pressed
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    private void getFirebase() {
        db = new DbHelper(this);
        loadDay("Monday");
        loadDay("Tuesday");
        loadDay("Wednesday");
        loadDay("Thursday");
        loadDay("Friday");
    }

    public void loadDay(String day){
        ArrayList<Week> list = new ArrayList<>();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("App");
        db = new DbHelper(this);
        db.deleteAll();

        DatabaseReference myRef1 = database.getReference("App").child("TimeTable").child(day);
        myRef1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Get the final size of the list
                int size1 = (int) snapshot.getChildrenCount();
                // Do something with the final size
                // ...
                for (int i = 1; i <= size1; i++) {
                    String str = "P" + i;

                    myRef.child("TimeTable").child(day).child(str).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                        @Override
                        public void onSuccess(DataSnapshot dataSnapshot) {
                            String name = Objects.requireNonNull(dataSnapshot.child("Subject").getValue()).toString();
                            String room = Objects.requireNonNull(dataSnapshot.child("Room").getValue()).toString();
                            String teacher = Objects.requireNonNull(dataSnapshot.child("Teacher").getValue()).toString();
                            String time = Objects.requireNonNull(dataSnapshot.child("Time").getValue()).toString();
                            int color = Integer.parseInt(Objects.requireNonNull(dataSnapshot.child("Color").getValue()).toString());
                            String time1 = "";
                            String time2 = "";
                            ///progressBar.setProgress(50);
                            if (time != null && !time.equals("null")) {
                                time1 = time.substring(0, 5);
                                time2 = time.substring(8, time.length());
                            }

                            Week w1 = new Week(name, teacher, room, time1, time2, color);
                            w1.setFragment(day);
                            db.insertWeek(w1);
                            list.add(w1);
                            //progressBar.setProgress(75);
                            // progressBar.setVisibility(View.GONE);
                            initAll();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle any errors that may occur
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors that may occur
            }
        });
    }




    public void putOnFirebase(){
        db = new DbHelper(this);
        ArrayList<Week> monday = db.getWeek("Monday");
        ArrayList<Week> tuesday = db.getWeek("Tuesday");
        ArrayList<Week> wednesday = db.getWeek("Wednesday");
        ArrayList<Week> thursday = db.getWeek("Thursday");
        ArrayList<Week> friday = db.getWeek("Friday");

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("App");
        myRef.removeValue();


        for(int i = 0; i < monday.size(); i++){
            String str = "P"+(i+1);
            DatabaseReference m = myRef.child("TimeTable").child("Monday").child(str);
            String teacher = monday.get(i).getTeacher();
            String subject = monday.get(i).getSubject();
            String room = monday.get(i).getRoom();
            String time = monday.get(i).getFromTime() + " - " + monday.get(i).getToTime();

            m.child("Teacher").setValue(teacher);
            m.child("Subject").setValue(subject);
            m.child("Room").setValue(room);
            m.child("Time").setValue(time);
            m.child("Color").setValue(monday.get(i).getColor());
        }

        for(int i = 0; i < tuesday.size(); i++){
            String str = "P"+(i+1);
            DatabaseReference m = myRef.child("TimeTable").child("Tuesday").child(str);
            String teacher = tuesday.get(i).getTeacher();
            String subject = tuesday.get(i).getSubject();
            String room = tuesday.get(i).getRoom();
            String time = tuesday.get(i).getFromTime() + " - " + monday.get(i).getToTime();

            m.child("Teacher").setValue(teacher);
            m.child("Subject").setValue(subject);
            m.child("Room").setValue(room);
            m.child("Time").setValue(time);
            m.child("Color").setValue(tuesday.get(i).getColor());
        }

        for(int i = 0; i < wednesday.size(); i++){
            String str = "P"+(i+1);
            DatabaseReference m = myRef.child("TimeTable").child("Wednesday").child(str);
            String teacher = wednesday.get(i).getTeacher();
            String subject = wednesday.get(i).getSubject();
            String room = wednesday.get(i).getRoom();
            String time = wednesday.get(i).getFromTime() + " - " + wednesday.get(i).getToTime();

            m.child("Teacher").setValue(teacher);
            m.child("Subject").setValue(subject);
            m.child("Room").setValue(room);
            m.child("Time").setValue(time);
            m.child("Color").setValue(wednesday.get(i).getColor());
        }

        for(int i = 0; i < thursday.size(); i++){
            String str = "P"+(i+1);
            DatabaseReference m = myRef.child("TimeTable").child("Thursday").child(str);
            String teacher = thursday.get(i).getTeacher();
            String subject = thursday.get(i).getSubject();
            String room = thursday.get(i).getRoom();
            String time = thursday.get(i).getFromTime() + " - " + thursday.get(i).getToTime();

            m.child("Teacher").setValue(teacher);
            m.child("Subject").setValue(subject);
            m.child("Room").setValue(room);
            m.child("Time").setValue(time);
            m.child("Color").setValue(thursday.get(i).getColor());
        }

        for(int i = 0; i < friday.size(); i++){
            String str = "P"+(i+1);
            DatabaseReference m = myRef.child("TimeTable").child("Friday").child(str);
            String teacher = friday.get(i).getTeacher();
            String subject = friday.get(i).getSubject();
            String room = friday.get(i).getRoom();
            String time = friday.get(i).getFromTime() + " - " + friday.get(i).getToTime();

            m.child("Teacher").setValue(teacher);
            m.child("Subject").setValue(subject);
            m.child("Room").setValue(room);
            m.child("Time").setValue(time);
            m.child("Color").setValue(friday.get(i).getColor());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        progressBar.setVisibility(View.VISIBLE);
        DoNotDisturbReceiversKt.setDoNotDisturbReceivers(this, false);
        getFirebase();
      //  initAll();
    }

    private void initAll() {

        NotificationUtil.sendNotificationCurrentLesson(this, false);
        PreferenceUtil.setDoNotDisturb(this, PreferenceUtil.doNotDisturbDontAskAgain(this));
        PreferenceUtil.setOneTimeAlarm(this, MidnightReceiver.class, MidnightReceiver.Companion.getHour(), MidnightReceiver.Companion.getMinutes(), 0, MidnightReceiver.Companion.getMidnightRecieverID());
        initSpinner();

        setupWeeksTV();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerview = navigationView.getHeaderView(0);
        headerview.findViewById(R.id.nav_header_main_settings).setOnClickListener((View v) -> startActivity(new Intent(this, SettingsActivity.class)));
        TextView title = headerview.findViewById(R.id.nav_header_main_title);
        title.setText(R.string.app_name);

        TextView desc = headerview.findViewById(R.id.nav_header_main_desc);
        desc.setText(R.string.nav_drawer_description);

        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        setupFragments();
        setupCustomDialog();
        progressBar.setVisibility(View.GONE);
    }

    private boolean dontfire = true;

    private void initSpinner() {
        //Set Profiles
        Spinner parentSpinner = findViewById(R.id.profile_spinner);

        if (ProfileManagement.isMoreThanOneProfile()) {
            parentSpinner.setVisibility(View.VISIBLE);
            dontfire = true;
            List<String> list = ProfileManagement.getProfileListNames();
            list.add(getString(R.string.profiles_edit));
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            parentSpinner.setAdapter(dataAdapter);
            parentSpinner.setSelection(ProfileManagement.getSelectedProfilePosition());
            parentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
                    if (dontfire) {
                        dontfire = false;
                        return;
                    }

                    String item = parent.getItemAtPosition(position).toString();
                    if (item.equals(getString(R.string.profiles_edit))) {
                        Intent intent = new Intent(getBaseContext(), ProfileActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        //Change profile position
                        ProfileManagement.setSelectedProfile(position);
                        startActivity(new Intent(getBaseContext(), MainActivity.class));
                        finish();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        } else {
            parentSpinner.setVisibility(View.GONE);
        }
    }

    private void setupWeeksTV() {
        TextView weekView = findViewById(R.id.main_week_tV);
        if (PreferenceUtil.isTwoWeeksEnabled(this)) {
           // odd even timetable visibility
            weekView.setVisibility(View.GONE);
            if (PreferenceUtil.isEvenWeek(this, Calendar.getInstance()))
                weekView.setText(R.string.even_week);
            else
                weekView.setText(R.string.odd_week);
        } else
            weekView.setVisibility(View.GONE);
    }

    private void setupFragments() {
        adapter = new FragmentsTabAdapter(getSupportFragmentManager());
        viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        WeekdayFragment mondayFragment = new WeekdayFragment(WeekdayFragment.KEY_MONDAY_FRAGMENT);
        WeekdayFragment tuesdayFragment = new WeekdayFragment(WeekdayFragment.KEY_TUESDAY_FRAGMENT);
        WeekdayFragment wednesdayFragment = new WeekdayFragment(WeekdayFragment.KEY_WEDNESDAY_FRAGMENT);
        WeekdayFragment thursdayFragment = new WeekdayFragment(WeekdayFragment.KEY_THURSDAY_FRAGMENT);
        WeekdayFragment fridayFragment = new WeekdayFragment(WeekdayFragment.KEY_FRIDAY_FRAGMENT);
/*        WeekdayFragment saturdayFragment = new WeekdayFragment(WeekdayFragment.KEY_SATURDAY_FRAGMENT);
        WeekdayFragment sundayFragment = new WeekdayFragment(WeekdayFragment.KEY_SUNDAY_FRAGMENT);*/

        boolean startOnSunday = PreferenceUtil.isWeekStartOnSunday(this);
        boolean showWeekend = PreferenceUtil.isSevenDays(this);

        if (!startOnSunday) {
            adapter.addFragment(mondayFragment, getResources().getString(R.string.monday));
            adapter.addFragment(tuesdayFragment, getResources().getString(R.string.tuesday));
            adapter.addFragment(wednesdayFragment, getResources().getString(R.string.wednesday));
            adapter.addFragment(thursdayFragment, getResources().getString(R.string.thursday));
            adapter.addFragment(fridayFragment, getResources().getString(R.string.friday));

            if (showWeekend) {
              //  adapter.addFragment(saturdayFragment, getResources().getString(R.string.saturday));
              //  adapter.addFragment(sundayFragment, getResources().getString(R.string.sunday));
            }
        } else {
           // adapter.addFragment(sundayFragment, getResources().getString(R.string.sunday));
            adapter.addFragment(mondayFragment, getResources().getString(R.string.monday));
            adapter.addFragment(tuesdayFragment, getResources().getString(R.string.tuesday));
            adapter.addFragment(wednesdayFragment, getResources().getString(R.string.wednesday));
            adapter.addFragment(thursdayFragment, getResources().getString(R.string.thursday));

            if (showWeekend) {
                adapter.addFragment(fridayFragment, getResources().getString(R.string.friday));
               // adapter.addFragment(saturdayFragment, getResources().getString(R.string.saturday));
            }
        }


        viewPager.setAdapter(adapter);

        int day = getFragmentChoosingDay();
        if (startOnSunday) {
            viewPager.setCurrentItem(day - 1, true);
        } else {
            viewPager.setCurrentItem(day == 1 ? 6 : day - 2, true);
        }

        tabLayout.setupWithViewPager(viewPager);
    }

    private int getFragmentChoosingDay() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        //If its after 20 o'clock, show the next day
        if (hour >= showNextDayAfterSpecificHour) {
            day++;
        }

        if (day > 7) { //Calender.Saturday
            day = day - 7; //1 = Calendar.Sunday, 2 = Calendar.Monday etc.
        }

        boolean startOnSunday = PreferenceUtil.isWeekStartOnSunday(this);
        boolean showWeekend = PreferenceUtil.isSevenDays(this);

        //If Saturday/Sunday are hidden, switch to Monday
        if ((!startOnSunday && !showWeekend) && (day == Calendar.SATURDAY || day == Calendar.SUNDAY)) {
            day = Calendar.MONDAY;
        } else if ((startOnSunday && !showWeekend) && (day == Calendar.FRIDAY || day == Calendar.SATURDAY)) {
            day = Calendar.SUNDAY;
        }

        return day;
    }

    private void setupCustomDialog() {
        final View alertLayout = getLayoutInflater().inflate(R.layout.dialog_add_subject, null);
        AlertDialogsHelper.getAddSubjectDialog(new DbHelper(this), MainActivity.this, alertLayout, adapter, viewPager);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        ProfileManagement.resetSelectedProfile();
        finishAffinity();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent settings = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settings);
            finish();
        } else if (item.getItemId() == R.id.action_backup) {
            backup();
        } else if (item.getItemId() == R.id.action_restore) {
            restore();
        } else if (item.getItemId() == R.id.action_remove_all) {
            deleteAll();
        }
//        else if (item.getItemId() == R.id.action_about_libs) {
//            new LibsBuilder()
//                    .withActivityTitle(getString(R.string.about_libs_title))
//                    .withAboutIconShown(true)
//                    .withFields(R.string.class.getFields())
//                    .withLicenseShown(true)
//                    .withAboutDescription(getString(R.string.nav_drawer_description))
//                    .withAboutAppName(getString(R.string.app_name))
//                    .start(this);
//        } else if (item.getItemId() == R.id.action_profiles) {
//            Intent intent = new Intent(getBaseContext(), ProfileActivity.class);
//            startActivity(intent);
//            finish();
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
//        if (itemId == R.id.exams) {
//            Intent exams = new Intent(MainActivity.this, ExamsActivity.class);
//            startActivity(exams);
//        } else if (itemId == R.id.homework) {
//            Intent homework = new Intent(MainActivity.this, HomeworkActivity.class);
//            startActivity(homework);
//        } else if (itemId == R.id.notes) {
//            Intent note = new Intent(MainActivity.this, NotesActivity.class);
//            startActivity(note);
//        }
        if (itemId == R.id.settings) {
            Intent settings = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settings);
            finish();
        } else if (itemId == R.id.schoolwebsitemenu) {
            String schoolWebsite = "https://www.bpitindia.com/";
            /* PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsActivity.KEY_SCHOOL_WEBSITE_SETTING, null);*/

            if (!TextUtils.isEmpty(schoolWebsite)) {
                openUrlInChromeCustomTab(schoolWebsite);
            } else {
                ChocoBar.builder().setActivity(this)
                        .setText(getString(R.string.please_set_school_website_url))
                        .setDuration(ChocoBar.LENGTH_LONG)
                        .red()
                        .show();
            }
        } else if (itemId == R.id.teachers) {
            Intent teacher = new Intent(MainActivity.this, TeachersActivity.class);
            startActivity(teacher);
        } else if (itemId == R.id.summary) {
            Intent teacher = new Intent(MainActivity.this, SummaryActivity.class);
            startActivity(teacher);
        } else if (itemId == R.id.buymeacoffee) {
            openUrlInChromeCustomTab("https://www.buymeacoffee.com/asdoi");
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private static final String backup_filename = "Timetable_Backup.xls";

    @SuppressWarnings("deprecation")
    public void backup() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            requestPermission(this::backup, SheriffPermission.STORAGE);
            return;
        }

        String path = Environment.getExternalStoragePublicDirectory(Build.VERSION.SDK_INT >= 19 ? Environment.DIRECTORY_DOCUMENTS : Environment.DIRECTORY_DOWNLOADS).toString();
//        SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyyMMdd");
//        String filename = timeStampFormat.format(new Date());

        AppCompatActivity activity = this;

        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        SQLiteToExcel sqliteToExcel = new SQLiteToExcel(this, DbHelper.getDBName(ProfileManagement.getSelectedProfilePosition()), path);
        sqliteToExcel.exportAllTables(backup_filename, new SQLiteToExcel.ExportListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onCompleted(String filePath) {
                runOnUiThread(() -> ChocoBar.builder().setActivity(activity)
                        .setText(getString(R.string.backup_successful, Build.VERSION.SDK_INT >= 19 ? getString(R.string.Documents) : getString(R.string.Downloads)))
                        .setDuration(ChocoBar.LENGTH_LONG)
                        .setIcon(R.drawable.ic_baseline_save_24)
                        .green()
                        .show());
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> ChocoBar.builder().setActivity(activity)
                        .setText(getString(R.string.backup_failed) + ": " + e.toString())
                        .setDuration(ChocoBar.LENGTH_LONG)
                        .red()
                        .show());
            }
        });
    }

    @SuppressWarnings("deprecation")
    public void restore() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            requestPermission(this::restore, SheriffPermission.STORAGE);
            return;
        }

        String path = Environment.getExternalStoragePublicDirectory(Build.VERSION.SDK_INT >= 19 ? Environment.DIRECTORY_DOCUMENTS : Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + backup_filename;
        File file = new File(path);
        if (!file.exists()) {
            ChocoBar.builder().setActivity(this)
                    .setText(getString(R.string.no_backup_found_in_downloads, Build.VERSION.SDK_INT >= 19 ? getString(R.string.Documents) : getString(R.string.Downloads)))
                    .setDuration(ChocoBar.LENGTH_LONG)
                    .red()
                    .show();
            return;
        }

        AppCompatActivity activity = this;
        DbHelper dbHelper = new DbHelper(this);
        dbHelper.deleteAll();

        ExcelToSQLite excelToSQLite = new ExcelToSQLite(getApplicationContext(), DbHelper.getDBName(ProfileManagement.getSelectedProfilePosition()), false);
        excelToSQLite.importFromFile(path, new ExcelToSQLite.ImportListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onCompleted(String filePath) {
                runOnUiThread(() -> ChocoBar.builder().setActivity(activity)
                        .setText(getString(R.string.import_successful))
                        .setDuration(ChocoBar.LENGTH_LONG)
                        .setIcon(R.drawable.ic_baseline_settings_backup_restore_24)
                        .green()
                        .show());
                initAll();
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> ChocoBar.builder().setActivity(activity)
                        .setText(getString(R.string.import_failed) + ": " + e.toString())
                        .setDuration(ChocoBar.LENGTH_LONG)
                        .red()
                        .show());
            }
        });
    }

    public void deleteAll() {
        new MaterialDialog.Builder(this)
                .title(getString(R.string.delete_everything))
                .content(getString(R.string.delete_everything_desc))
                .positiveText(getString(R.string.yes))
                .onPositive((dialog, which) -> {
                    try {
                        DbHelper dbHelper = new DbHelper(this);
                        dbHelper.deleteAll();
                        ChocoBar.builder().setActivity(this)
                                .setText(getString(R.string.successfully_deleted_everything))
                                .setDuration(ChocoBar.LENGTH_LONG)
                                .setIcon(R.drawable.ic_delete_forever_black_24dp)
                                .green()
                                .show();
                        initAll();
                    } catch (Exception e) {
                        ChocoBar.builder().setActivity(this)
                                .setText(getString(R.string.an_error_occurred))
                                .setDuration(ChocoBar.LENGTH_LONG)
                                .red()
                                .show();
                    }
                })
                .onNegative((dialog, which) -> dialog.dismiss())
                .negativeText(getString(R.string.no))
                .onNeutral((dialog, which) -> {
                    backup();
                    dialog.dismiss();
                })
                .neutralText(R.string.backup)
                .show();
    }

    private void openUrlInChromeCustomTab(String url) {
        Context context = this;
        try {
            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                    .addDefaultShareMenuItem()
                    .setToolbarColor(PreferenceUtil.getPrimaryColor(this))
                    .setShowTitle(false)
                    .setUrlBarHidingEnabled(false)
                    .build();

            // This is optional but recommended
            CustomTabsHelper.Companion.addKeepAliveExtra(context, customTabsIntent.intent);

            // This is where the magic happens...
            CustomTabsHelper.Companion.openCustomTab(context,
                    customTabsIntent,
                    Uri.parse(url),
                    new WebViewFallback());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Permissions
    private Sheriff sheriffPermission;
    private static final int REQUEST_MULTIPLE_PERMISSION = 101;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        sheriffPermission.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    protected void requestPermission(Runnable runAfter, SheriffPermission... permissions) {
        PermissionListener pl = new MyPermissionListener(runAfter);

        sheriffPermission = Sheriff.Builder()
                .with(this)
                .requestCode(REQUEST_MULTIPLE_PERMISSION)
                .setPermissionResultCallback(pl)
                .askFor(permissions)
                .rationalMessage(getString(R.string.permission_request_message))
                .build();

        sheriffPermission.requestPermissions();
    }

    private class MyPermissionListener implements PermissionListener {
        final Runnable runAfter;

        MyPermissionListener(Runnable r) {
            runAfter = r;
        }

        @Override
        public void onPermissionsGranted(int requestCode, ArrayList<String> acceptedPermissionList) {
            if (runAfter == null)
                return;
            try {
                runAfter.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onPermissionsDenied(int requestCode, ArrayList<String> deniedPermissionList) {
            // setup the alert builder
            MaterialDialog.Builder builder = new MaterialDialog.Builder(MainActivity.this);
            builder.title(getString(R.string.permission_required));
            builder.content(getString(R.string.permission_required_description));

            // add the buttons
            builder.onPositive((dialog, which) -> {
                openAppPermissionSettings();
                dialog.dismiss();
            });
            builder.positiveText(getString(R.string.permission_ok_button));

            builder.negativeText(getString(R.string.permission_cancel_button));
            builder.onNegative((dialog, which) -> dialog.dismiss());

            // create and show the alert dialog
            MaterialDialog dialog = builder.build();
            dialog.show();
        }
    }

    private void openAppPermissionSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }
}
