package com.example.roomate.notification;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.roomate.model.Task;

import java.util.Calendar;

/** כלי עזר לקביעת Alarm ותזכורות משימה. */
public class ReminderScheduler {
    private static final String TAG = "ReminderScheduler";

    @SuppressLint("ScheduleExactAlarm")
    public static void scheduleTaskReminder(Context ctx,
                                            String taskId,
                                            String title,
                                            Calendar when) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);

        // 1. API 31+ דורש הרשאת Exact Alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (am == null) {
                Log.e(TAG, "AlarmManager null, cannot schedule");
                return;
            }
            if (!am.canScheduleExactAlarms()) {
                Log.w(TAG, "Exact alarms not allowed; skip scheduling for task " + taskId);
                return;
            }
        }

        try {
            // 2. הרכבת ה־PendingIntent
            Intent intent = new Intent(ctx, TaskAlarmReceiver.class);
            intent.putExtra(TaskAlarmReceiver.EXTRA_TASK_TITLE, title);
            PendingIntent pi = PendingIntent.getBroadcast(
                    ctx,
                    taskId.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // 3. קביעת ה־Alarm
            long triggerAt = when.getTimeInMillis();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAt,
                        pi
                );
            } else {
                am.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerAt,
                        pi
                );
            }
            Log.d(TAG, "Scheduled exact alarm for task " + taskId + " at " + triggerAt);

        } catch (SecurityException e) {
            Log.e(TAG, "Missing exact alarm permission for task " + taskId, e);
        }
    }

    public static void cancelTaskReminder(Context ctx, String taskId) {
        Intent intent = new Intent(ctx, TaskAlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(
                ctx,
                taskId.hashCode(),
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (pi != null) {
            AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
            am.cancel(pi);
            Log.d(TAG, "Cancelled alarm for task " + taskId);
        }
    }

    // נוחות: על סמך dueDateMillis
    public static void scheduleReminder(Context ctx, Task task) {
        long millis = task.getDueDateMillis();
        if (millis <= 0) return;
        Calendar when = Calendar.getInstance();
        when.setTimeInMillis(millis);
        scheduleTaskReminder(ctx, task.getId(), task.getTitle(), when);
    }

    public static void cancelReminder(Context ctx, Task task) {
        cancelTaskReminder(ctx, task.getId());
    }
}
