package com.example.roomate.ui.settings;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.roomate.R;
import com.example.roomate.auth.GroupSelectionActivity;
import com.example.roomate.auth.LoginActivity;
import com.example.roomate.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Fragment להגדרות המשתמש, כולל תזכורות מדויקות והודעות.
 */
public class SettingsFragment extends Fragment {
    private static final String TAG = "SettingsFragment";

    private ImageView ivProfileAvatar;
    private TextView  tvSettingsNameValue;
    private TextView  tvSettingsEmailValue;
    private Button    btnLeaveGroup;
    private Button    btnLogout;
    private Button    btnExactAlarms;
    private Button    btnNotifications;

    private UserRepository           userRepo;
    private ActivityResultLauncher<String> notifPermissionLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- מציאת ה־Views ---
        ivProfileAvatar      = view.findViewById(R.id.ivProfileAvatar);
        tvSettingsNameValue  = view.findViewById(R.id.tvSettingsNameValue);
        tvSettingsEmailValue = view.findViewById(R.id.tvSettingsEmailValue);
        btnLeaveGroup        = view.findViewById(R.id.btnLeaveGroup);
        btnLogout            = view.findViewById(R.id.btnLogout);
        btnExactAlarms       = view.findViewById(R.id.btnRequestExactAlarms);
        btnNotifications     = view.findViewById(R.id.btnRequestNotifications);

        userRepo = new UserRepository();

        // --- launcher לבקשת POST_NOTIFICATIONS ---
        notifPermissionLauncher = registerForActivityResult(
                new RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Toast.makeText(getContext(),
                                "הרשאת התראות אושרה", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(),
                                "הרשאת התראות נדחתה", Toast.LENGTH_SHORT).show();
                    }
                    updateButtons();
                }
        );

        // --- טעינת פרטי המשתמש המחובר ---
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "No authenticated user, redirect to LoginActivity");
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
            return;
        }
        String uid = currentUser.getUid();

        userRepo.getUserById(uid).observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                tvSettingsNameValue.setText(user.getName());
                tvSettingsEmailValue.setText(user.getEmail());
                String avatarUrl = user.getAvatarUrl();
                Glide.with(requireContext())
                        .load(avatarUrl != null && !avatarUrl.isEmpty() ? avatarUrl : R.drawable.ic_profile)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .circleCrop()
                        .into(ivProfileAvatar);
            }
        });

        // --- לחצני Exact Alarms ו־Notifications ---
        btnExactAlarms.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AlarmManager am = requireContext()
                        .getSystemService(AlarmManager.class);
                boolean allowed = am != null && am.canScheduleExactAlarms();
                if (!allowed) {
                    // פנייה לבקשה מערכתית
                    startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
                } else {
                    // פנייה לדף האפליקציה כדי לבטל
                    Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", requireContext().getPackageName(), null));
                    startActivity(i);
                }
            } else {
                Toast.makeText(getContext(),
                        "לא נדרש בגרסת Android זו", Toast.LENGTH_SHORT).show();
            }
        });

        btnNotifications.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                boolean granted = ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED;
                if (!granted) {
                    notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                } else {
                    // פתיחת מסך הגדרות ההתראות של האפליקציה
                    Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().getPackageName());
                    startActivity(intent);
                }
            } else {
                Toast.makeText(getContext(),
                        "הרשאת התראות ניתנת כברירת מחדל בגרסה זו",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // --- עזיבת קבוצה והתנתקות (כמו שהיה אצלך) ---
        btnLeaveGroup.setOnClickListener(v -> { /* ... */ });
        btnLogout.setOnClickListener(v -> { /* ... */ });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateButtons();
    }

    /** מעדכן את הטקסט וה־enabled-state של הכפתורים לפי ההרשאות הנוכחיות */
    private void updateButtons() {
        // Exact Alarms
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager am = requireContext().getSystemService(AlarmManager.class);
            boolean allowed = am != null && am.canScheduleExactAlarms();
            btnExactAlarms.setText(
                    allowed ? "Disable Exact Alarms" : "Enable Exact Alarms"
            );
            btnExactAlarms.setEnabled(true);
        } else {
            btnExactAlarms.setText("Exact Alarms not required");
            btnExactAlarms.setEnabled(false);
        }

        // Notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean granted = ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
            btnNotifications.setText(
                    granted ? "Disable Notifications" : "Enable Notifications"
            );
            btnNotifications.setEnabled(true);
        } else {
            btnNotifications.setText("Notifications allowed");
            btnNotifications.setEnabled(false);
        }
    }
}
