package com.example.roomate.notification;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.roomate.model.Task;

import java.util.Calendar;

/** כלי עזר לקביעת Alarm ותזכורות משימה. */
public class ReminderScheduler {

    // קביעת תזכורת "ידנית" (נשמרת ב-AlarmManager)
    @SuppressLint("ScheduleExactAlarm")
    public static void scheduleTaskReminder(Context ctx,
                                            String taskId,
                                            String title,
                                            Calendar when) {

        Intent intent = new Intent(ctx, TaskAlarmReceiver.class);
        intent.putExtra(TaskAlarmReceiver.EXTRA_TASK_TITLE, title);

        PendingIntent pi = PendingIntent.getBroadcast(
                ctx,
                taskId.hashCode(),   // מזהה ייחודי לכל מטלה
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    when.getTimeInMillis(),
                    pi
            );
        } else {
            am.setExact(
                    AlarmManager.RTC_WAKEUP,
                    when.getTimeInMillis(),
                    pi
            );
        }
    }

    // ביטול תזכורת לפי מזהה המטלה
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
        }
    }

    // —— מתודות נוספות לנוחות —— //

    /**
     * קבע תזכורת אוטומטית מה-TODO שלך,
     * לפי שדה dueDateMillis ב-Task.
     */
    public static void scheduleReminder(Context ctx, Task task) {
        long millis = task.getDueDateMillis();
        if (millis <= 0) return;                  // אין תאריך תקף
        Calendar when = Calendar.getInstance();
        when.setTimeInMillis(millis);

        // קורא לפונקציה המרכזית
        scheduleTaskReminder(ctx,
                task.getId(),
                task.getTitle(),
                when);
    }

    /**
     * בטל תזכורת עבור Task (למשל אחרי סימון done=true).
     */
    public static void cancelReminder(Context ctx, Task task) {
        cancelTaskReminder(ctx, task.getId());
    }
}
