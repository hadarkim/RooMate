package com.example.roomate.ui.settings;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.example.roomate.R;
import com.example.roomate.auth.GroupSelectionActivity;
import com.example.roomate.auth.LoginActivity;
import com.example.roomate.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class SettingsFragment extends Fragment {
    private static final String TAG = "SettingsFragment";

    private ImageView ivProfileAvatar;
    private TextView tvSettingsNameValue;
    private TextView tvSettingsEmailValue;
    private Switch btnExactAlarms;
    private Switch btnNotifications;
    private TextView btnLeaveGroup;
    private TextView btnLogout;

    private ActivityResultLauncher<String> notifPermissionLauncher;
    private UserRepository userRepo;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        ivProfileAvatar     = view.findViewById(R.id.ivProfileAvatar);
        tvSettingsNameValue = view.findViewById(R.id.tvSettingsNameValue);
        tvSettingsEmailValue= view.findViewById(R.id.tvSettingsEmailValue);
        btnExactAlarms      = view.findViewById(R.id.btnRequestExactAlarms);
        btnNotifications    = view.findViewById(R.id.btnRequestNotifications);
        btnLeaveGroup       = view.findViewById(R.id.btnLeaveGroup);
        btnLogout           = view.findViewById(R.id.btnLogout);

        userRepo = new UserRepository();

        notifPermissionLauncher = registerForActivityResult(
                new RequestPermission(),
                isGranted -> {
                    Toast.makeText(getContext(),
                            isGranted ? "הרשאת התראות אושרה" : "הרשאת התראות נדחתה",
                            Toast.LENGTH_SHORT).show();
                    updateButtons();
                }
        );

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "No authenticated user, redirect to LoginActivity");
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
            return;
        }
        String uid = currentUser.getUid();

        userRepo.getUserById(uid).observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;
            tvSettingsNameValue.setText(user.getName());
            tvSettingsEmailValue.setText(user.getEmail());
            String avatarUrl = user.getAvatarUrl();
            if (ivProfileAvatar != null) {
                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    Glide.with(this)
                            .load(avatarUrl)
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .circleCrop()
                            .into(ivProfileAvatar);
                } else {
                    ivProfileAvatar.setImageResource(R.drawable.ic_profile);
                }
            }
        });

        btnExactAlarms.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AlarmManager am = requireContext().getSystemService(AlarmManager.class);
                if (am != null && !am.canScheduleExactAlarms()) {
                    startActivity(
                            new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
                } else {
                    Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package",
                                    requireContext().getPackageName(), null));
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
                    Intent intent = new Intent(
                            Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE,
                                    requireContext().getPackageName());
                    startActivity(intent);
                }
            } else {
                Toast.makeText(getContext(),
                        "הרשאת התראות ניתנת כברירת מחדל בגרסה זו",
                        Toast.LENGTH_SHORT).show();
            }
        });

        btnLeaveGroup.setOnClickListener(v -> {
            SharedPreferences prefs =
                    PreferenceManager.getDefaultSharedPreferences(requireContext());
            String groupId = prefs.getString("GROUP_ID", null);
            if (groupId == null) {
                Toast.makeText(requireContext(),
                        "אין קבוצה לעזוב.", Toast.LENGTH_SHORT).show();
                return;
            }
            new AlertDialog.Builder(requireContext())
                    .setTitle("עזיבת קבוצה")
                    .setMessage("האם אתה בטוח שברצונך לעזוב את הקבוצה?")
                    .setPositiveButton("כן", (dialog, which) -> {
                        // 1. הסרת ה-UID מתוך /groups/{groupId}/members
                        FirebaseDatabase.getInstance()
                                .getReference("groups")
                                .child(groupId)
                                .child("members")
                                .child(uid)
                                .removeValue()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "Member removed from group");
                                        // 2. הסרת GROUP_ID מ־SharedPreferences
                                        prefs.edit().remove("GROUP_ID").apply();
                                        // 3. הסרת field groupId מ-/users/{uid}/groupId
                                        FirebaseDatabase.getInstance()
                                                .getReference("users")
                                                .child(uid)
                                                .child("groupId")
                                                .removeValue()
                                                .addOnCompleteListener(task2 -> {
                                                    if (task2.isSuccessful()) {
                                                        Log.d(TAG, "Removed groupId from user node");
                                                    } else {
                                                        Log.w(TAG, "Failed remove groupId: "
                                                                + task2.getException());
                                                    }
                                                    // 4. לחיצה: חזרה ל-GroupSelectionActivity
                                                    Toast.makeText(requireContext(),
                                                            "עזבת את הקבוצה.",
                                                            Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(
                                                            requireContext(),
                                                            GroupSelectionActivity.class));
                                                    requireActivity().finish();
                                                });
                                    } else {
                                        String msg = task.getException() != null
                                                ? task.getException().getMessage()
                                                : "שגיאה";
                                        Log.e(TAG, "Failed remove member: " + msg,
                                                task.getException());
                                        Toast.makeText(requireContext(),
                                                "לא ניתן לעזוב: " + msg,
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                    })
                    .setNegativeButton("ביטול", null)
                    .show();
        });

        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("התנתקות")
                    .setMessage("האם אתה בטוח שברצונך להתנתק?")
                    .setPositiveButton("כן", (dialog, which) -> {
                        PreferenceManager.getDefaultSharedPreferences(requireContext())
                                .edit().remove("GROUP_ID").apply();
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(requireContext(), LoginActivity.class));
                        requireActivity().finish();
                    })
                    .setNegativeButton("ביטול", null)
                    .show();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateButtons();
    }

    private void updateButtons() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager am = requireContext().getSystemService(AlarmManager.class);
            btnExactAlarms.setChecked(am != null && am.canScheduleExactAlarms());
        } else {
            btnExactAlarms.setEnabled(false);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean granted = ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
            btnNotifications.setChecked(granted);
        } else {
            btnNotifications.setEnabled(false);
        }
    }
}
