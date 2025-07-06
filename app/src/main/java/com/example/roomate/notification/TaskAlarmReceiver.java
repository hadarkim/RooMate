package com.example.roomate.notification;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.roomate.R;
import com.example.roomate.ui.main.MainActivity;

public class TaskAlarmReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "task_channel";
    public static final String EXTRA_TASK_TITLE = "extra_task_title";
    public static final String EXTRA_REQUEST_CODE = "extra_request_code";

    @Override
    public void onReceive(Context context, Intent intent) {
        String taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE);
        int requestCode = intent.getIntExtra(EXTRA_REQUEST_CODE, 0);

        // 1. יוצרים את ה־PendingIntent לניווט למסך הראשי
        Intent tapIntent = new Intent(context, MainActivity.class);
        PendingIntent tapPI = PendingIntent.getActivity(
                context,
                requestCode,
                tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 2. בונים את ההודעה
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_task_list)
                        .setContentTitle("מטלה מתקרבת")
                        .setContentText(taskTitle + " – הגיע זמן לבצע!")
                        .setContentIntent(tapPI)
                        .setAutoCancel(true)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)  // צלצול, רטט וכו׳
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        // 3. שולחים את ההודעה
        NotificationManagerCompat nm = NotificationManagerCompat.from(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // אם אין הרשאה, פשוט לא נשלח – במקומך כבר ביקשת אותה ב־SettingsFragment
            if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        nm.notify(requestCode, builder.build());
    }
}

