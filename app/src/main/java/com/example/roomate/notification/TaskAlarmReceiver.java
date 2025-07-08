package com.example.roomate.notification;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.roomate.R;
import com.example.roomate.ui.main.MainActivity;

public class TaskAlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "TaskAlarmReceiver";

    // מזהה הערוץ – חייב להתאים לערוץ שיצרת ב־MainActivity
    public static final String CHANNEL_ID = "task_reminders";
    public static final String EXTRA_TASK_TITLE = "extra_task_title";
    public static final String EXTRA_REQUEST_CODE = "extra_request_code";

    @Override
    public void onReceive(Context context, Intent intent) {
        // 1. קבלת הנתונים מה־Intent
        String taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE);
        int requestCode = intent.getIntExtra(EXTRA_REQUEST_CODE, 0);

        Log.d(TAG, "onReceive: taskTitle=\"" + taskTitle + "\", requestCode=" + requestCode);

        // גיבוי במקרה שחסר כותרת
        if (taskTitle == null || taskTitle.isEmpty()) {
            taskTitle = "יש לך מטלה לממש";
        }

        // 2. יצירת PendingIntent לפתיחת MainActivity בלחיצה על ההתראה
        Intent tapIntent = new Intent(context, MainActivity.class);
        // וודא שהאפליקציה תיפתח כמשימה חדשה ותנקה כל מחסנית ישנה
        tapIntent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        PendingIntent tapPI = PendingIntent.getActivity(
                context,
                requestCode,
                tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 3. בניית ה־Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_task_list)           // אייקון קטן
                .setContentTitle("תזכורת למשימה")                // כותרת קבועה
                .setContentText(taskTitle)                       // תוכן דינמי
                .setContentIntent(tapPI)                         // פעולה בלחיצה
                .setAutoCancel(true)                             // הסרה אחרי לחיצה
                .setDefaults(NotificationCompat.DEFAULT_ALL)     // צלצול, רטט וברירת מחדל
                .setPriority(NotificationCompat.PRIORITY_HIGH);  // עדיפות גבוהה

        // 4. שליחה עם בדיקת הרשאת POST_NOTIFICATIONS (Android 13+)
        NotificationManagerCompat nm = NotificationManagerCompat.from(context);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "POST_NOTIFICATIONS permission not granted; skipping notification");
                    return;
                }
            }
            nm.notify(requestCode, builder.build());
            Log.d(TAG, "Notification posted for taskCode=" + requestCode);
        } catch (Exception e) {
            Log.e(TAG, "Error showing notification", e);
        }
    }
}
