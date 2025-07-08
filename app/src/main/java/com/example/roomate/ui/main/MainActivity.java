package com.example.roomate.ui.main;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.roomate.R;
import com.example.roomate.auth.GroupSelectionActivity;
import com.example.roomate.auth.LoginActivity;
import com.example.roomate.model.Task;
import com.example.roomate.notification.ReminderScheduler;
import com.example.roomate.viewmodel.TaskViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private NavController navController;
    private TaskViewModel viewModel;
    private final List<Task> prevTasks = new ArrayList<>();
    private boolean isFirstLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // יצירת Notification Channel (API 26+)
        createNotificationChannel();

        // 0️⃣ בדיקת Authentication
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // 1️⃣ בדיקת GROUP_ID
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String groupId = prefs.getString("GROUP_ID", null);
        if (groupId == null) {
            startActivity(new Intent(this, GroupSelectionActivity.class));
            finish();
            return;
        }

        // 2️⃣ טועני layout, Toolbar ו-NavController
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        NavigationUI.setupWithNavController(bottomNav, navController);
        NavigationUI.setupActionBarWithNavController(this, navController);
        bottomNav.setOnItemReselectedListener(item -> { /* no-op */ });

        // 3️⃣ הוספת Observer לכל החיים על activeTasks
        viewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        viewModel.getActiveTasks().observe(this, tasks -> {
            long now = System.currentTimeMillis();
            AlarmManager am = getSystemService(AlarmManager.class);
            boolean canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S
                    || (am != null && am.canScheduleExactAlarms());

            // 3a. התראות על מטלות חדשות
            if (!isFirstLoad && tasks != null) {
                for (Task t : tasks) {
                    boolean isNew = prevTasks.stream()
                            .noneMatch(old -> old.getId().equals(t.getId()));
                    if (isNew) {
                        sendImmediateNewTaskNotification(t);
                    }
                }
            }

            // 3b. קביעת תזכורות 12h ו-due, וביטול אם פג/בוצע
            if (tasks != null) {
                for (Task t : tasks) {
                    String id12  = t.getId() + "-12h";
                    String idDue = t.getId() + "-due";
                    if (!t.isDone() && t.getDueDateMillis() > now && canExact) {
                        Calendar c12 = Calendar.getInstance();
                        c12.setTimeInMillis(t.getDueDateMillis());
                        c12.add(Calendar.HOUR_OF_DAY, -12);
                        if (c12.getTimeInMillis() > now) {
                            ReminderScheduler.scheduleTaskReminder(
                                    this, id12,
                                    t.getTitle() + " בעוד 12 שעות", c12);
                        }
                        Calendar cDue = Calendar.getInstance();
                        cDue.setTimeInMillis(t.getDueDateMillis());
                        ReminderScheduler.scheduleTaskReminder(
                                this, idDue,
                                t.getTitle(), cDue);
                    } else {
                        ReminderScheduler.cancelTaskReminder(this, id12);
                        ReminderScheduler.cancelTaskReminder(this, idDue);
                    }
                }
            }

            // 3c. עדכון prevTasks ו-isFirstLoad
            prevTasks.clear();
            if (tasks != null) prevTasks.addAll(tasks);
            isFirstLoad = false;
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    /** יצירת Notification Channel עבור תזכורות מטלות */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "task_reminders",
                    "Task Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for task deadline reminders");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    /** התראת Push מיידית עבור מטלה שנוספה */
    private void sendImmediateNewTaskNotification(Task t) {
        // בדיקת הרשאת POST_NOTIFICATIONS (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Notification permission not granted; skip new-task notif");
                return;
            }
        }
        try {
            NotificationCompat.Builder b = new NotificationCompat.Builder(this, "task_reminders")
                    .setSmallIcon(R.drawable.ic_task_list)
                    .setContentTitle("מטלה חדשה")
                    .setContentText(t.getTitle())
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(
                            PendingIntent.getActivity(
                                    this,
                                    t.getId().hashCode(),
                                    new Intent(this, MainActivity.class),
                                    PendingIntent.FLAG_IMMUTABLE
                            )
                    );

            NotificationManagerCompat.from(this)
                    .notify(t.getId().hashCode(), b.build());
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to post new-task notif for " + t.getId(), e);
        }
    }
}
