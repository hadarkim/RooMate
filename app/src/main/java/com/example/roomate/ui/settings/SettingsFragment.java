package com.example.roomate.ui.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.roomate.R;
import com.example.roomate.auth.GroupSelectionActivity;
import com.example.roomate.auth.LoginActivity;
import com.example.roomate.model.User;
import com.example.roomate.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Fragment עבור מסך ההגדרות: מציג תמונת פרופיל (אם קיימת), שם, אימייל,
 * ולחצנים לעזיבת קבוצה ולהתנתקות.
 */
public class SettingsFragment extends Fragment {
    private static final String TAG = "SettingsFragment";

    private ImageView ivProfileAvatar;
    private TextView tvSettingsNameValue;
    private TextView tvSettingsEmailValue;
    private Button btnLeaveGroup;
    private Button btnLogout;
    // אופציונלי: Button btnChangeName;

    private UserRepository userRepo;

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

        // מציאת ה-Views
        ivProfileAvatar     = view.findViewById(R.id.ivProfileAvatar);
        tvSettingsNameValue = view.findViewById(R.id.tvSettingsNameValue);
        tvSettingsEmailValue= view.findViewById(R.id.tvSettingsEmailValue);
        btnLeaveGroup       = view.findViewById(R.id.btnLeaveGroup);
        btnLogout           = view.findViewById(R.id.btnLogout);
        // btnChangeName     = view.findViewById(R.id.btnChangeName); // אם מוסיפים שינוי שם

        userRepo = new UserRepository();

        // בדיקת משתמש מחובר
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // אם אין משתמש מחובר, נווט ל-Login
            Log.d(TAG, "No authenticated user, redirecting to LoginActivity");
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
            return;
        }
        String uid = currentUser.getUid();

        // טעינת פרטי המשתמש (שם, אימייל, avatarUrl) לצורך הצגה
        userRepo.getUserById(uid).observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                // שם
                String name = user.getName() != null ? user.getName() : "";
                tvSettingsNameValue.setText(name);
                // אימייל
                String email = user.getEmail() != null ? user.getEmail() : "";
                tvSettingsEmailValue.setText(email);
                // avatar (אם רוצים): נטען עם Glide אם קיים URL, אחרת נציג ברירת מחדל או נסיר ה-ImageView
                String avatarUrl = user.getAvatarUrl();
                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    ivProfileAvatar.setVisibility(View.VISIBLE);
                    Glide.with(requireContext())
                            .load(avatarUrl)
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .circleCrop()
                            .into(ivProfileAvatar);
                } else {
                    // אם אין URL, אפשר להציג תמונת ברירת מחדל או להסתיר
                    ivProfileAvatar.setVisibility(View.VISIBLE);
                    ivProfileAvatar.setImageResource(R.drawable.ic_profile);
                }
            } else {
                // במקרה שלא נמצא משתמש, נסתיר או נציג ריק
                tvSettingsNameValue.setText("");
                tvSettingsEmailValue.setText("");
                ivProfileAvatar.setVisibility(View.VISIBLE);
                ivProfileAvatar.setImageResource(R.drawable.ic_profile);
            }
        });

        // הגדרת לחצן עזיבת קבוצה
        btnLeaveGroup.setOnClickListener(v -> {
            // ודא שיש GROUP_ID ב-SharedPreferences
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
            String groupId = prefs.getString("GROUP_ID", null);
            if (groupId == null) {
                Toast.makeText(requireContext(), "אין קבוצה לעזוב.", Toast.LENGTH_SHORT).show();
                return;
            }
            // שאלה למשתמש: האם באמת רוצה לעזוב?
            new AlertDialog.Builder(requireContext())
                    .setTitle("עזיבת קבוצה")
                    .setMessage("האם אתה בטוח שברצונך לעזוב את הקבוצה?")
                    .setPositiveButton("כן", (dialog, which) -> {
                        // מחק את ה-UID מתוך /groups/{groupId}/members
                        FirebaseDatabase.getInstance()
                                .getReference("groups")
                                .child(groupId)
                                .child("members")
                                .child(uid)
                                .removeValue()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "Member removed from group in /groups");
                                        // מחק את GROUP_ID מה-SharedPreferences
                                        prefs.edit().remove("GROUP_ID").apply();
                                        // מחק גם את השדה /users/{uid}/groupId כדי שבכניסה הבאה יידרש לבחור קבוצה
                                        FirebaseDatabase.getInstance()
                                                .getReference("users")
                                                .child(uid)
                                                .child("groupId")
                                                .removeValue()
                                                .addOnCompleteListener(task2 -> {
                                                    if (task2.isSuccessful()) {
                                                        Log.d(TAG, "Removed groupId from user node");
                                                    } else {
                                                        Log.w(TAG, "Failed remove groupId from user node: "
                                                                + task2.getException());
                                                    }
                                                    // לאחר העזיבה, נווט ל-GroupSelectionActivity
                                                    Toast.makeText(requireContext(), "עזבת את הקבוצה.", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(requireContext(), GroupSelectionActivity.class);
                                                    startActivity(intent);
                                                    requireActivity().finish();
                                                });
                                    } else {
                                        String msg = task.getException() != null ? task.getException().getMessage() : "שגיאה";
                                        Log.e(TAG, "Failed remove member from group: " + msg, task.getException());
                                        Toast.makeText(requireContext(), "לא ניתן לעזוב: " + msg, Toast.LENGTH_LONG).show();
                                    }
                                });
                    })
                    .setNegativeButton("ביטול", null)
                    .show();
        });

        // הגדרת לחצן התנתקות (Logout)
        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("התנתקות")
                    .setMessage("האם אתה בטוח שברצונך להתנתק?")
                    .setPositiveButton("כן", (dialog, which) -> {
                        // מחיקת GROUP_ID ממקומי
                        SharedPreferences prefs2 = PreferenceManager.getDefaultSharedPreferences(requireContext());
                        prefs2.edit().remove("GROUP_ID").apply();
                        // התנתקות מ-FirebaseAuth
                        FirebaseAuth.getInstance().signOut();
                        // נווט ל-LoginActivity וסגור Activity/Fragment
                        Intent intent = new Intent(requireContext(), LoginActivity.class);
                        startActivity(intent);
                        requireActivity().finish();
                    })
                    .setNegativeButton("ביטול", null)
                    .show();
        });

        // לדוגמה: אם מוסיפים כפתור שינוי שם:
        /*
        btnChangeName.setOnClickListener(v -> {
            EditText et = new EditText(requireContext());
            et.setInputType(InputType.TYPE_CLASS_TEXT);
            new AlertDialog.Builder(requireContext())
                    .setTitle("שנה שם")
                    .setView(et)
                    .setPositiveButton("שמור", (dlg, which) -> {
                        String newName = et.getText().toString().trim();
                        if (newName.isEmpty()) {
                            Toast.makeText(requireContext(), "השם לא יכול להיות ריק", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // עדכון /users/{uid}/name
                        FirebaseDatabase.getInstance()
                                .getReference("users")
                                .child(uid)
                                .child("name")
                                .setValue(newName)
                                .addOnCompleteListener(t -> {
                                    if (t.isSuccessful()) {
                                        Toast.makeText(requireContext(), "השם עודכן", Toast.LENGTH_SHORT).show();
                                    } else {
                                        String msg = t.getException() != null ? t.getException().getMessage() : "שגיאה";
                                        Toast.makeText(requireContext(), "לא ניתן לעדכן: " + msg, Toast.LENGTH_LONG).show();
                                    }
                                });
                    })
                    .setNegativeButton("ביטול", null)
                    .show();
        });
        */
    }
}
