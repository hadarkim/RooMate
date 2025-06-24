package com.example.roomate.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.roomate.R;
import com.example.roomate.ui.main.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;


public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private EditText etEmail, etPassword;
    private Button   btnLogin;
    private TextView tvRegister;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth       = FirebaseAuth.getInstance();
        etEmail    = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin   = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        // התחברות בפועל
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass  = etPassword.getText().toString().trim();
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this,
                        "Enter email & password",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            auth.signInWithEmailAndPassword(email, pass)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user == null) {
                            Toast.makeText(this,
                                    "Login error: user is null",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        Log.d(TAG, "Login successful, uid=" + user.getUid());
                        // לאחר הצלחת ההתחברות, נקרא לניווט
                        handlePostLoginNavigation(user.getUid());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Auth failed", e);
                        Toast.makeText(this,
                                "Auth Failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        });

        // מעבר למסך הרשמה
        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        // במידה ויש כבר משתמש מאומת, נוודא ניווט אוטומטי
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "User already signed in, checking groupId");
            handlePostLoginNavigation(currentUser.getUid());
        }
    }

    /**
     * לאחר התחברות או אם המשתמש כבר מחובר, בודקים אם יש GROUP_ID בשמירה מקומית.
     * אם אין, קוראים ל-/users/{uid}/groupId; לפי התוצאה שומרים SharedPreferences ואז ניווט.
     *
     * @param uid ה־UID של המשתמש המחובר
     */
    private void handlePostLoginNavigation(@NonNull String uid) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String savedGroupId = prefs.getString("GROUP_ID", null);
        if (savedGroupId != null) {
            Log.d(TAG, "Found GROUP_ID in SharedPreferences: " + savedGroupId + " → navigating to MainActivity");
            navigateToMain();
        } else {
            // SharedPreferences ריקה → בדיקה ב־Realtime Database
            DatabaseReference userGroupRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(uid)
                    .child("groupId");
            userGroupRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String groupId = snapshot.getValue(String.class);
                    if (groupId != null) {
                        Log.d(TAG, "Found groupId in Database: " + groupId + " → saving to SharedPreferences and navigating to MainActivity");
                        prefs.edit().putString("GROUP_ID", groupId).apply();
                        navigateToMain();
                    } else {
                        Log.d(TAG, "No groupId in Database → navigating to GroupSelectionActivity");
                        navigateToGroupSelection();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w(TAG, "Failed reading groupId from Database: " + error.getMessage());
                    // במקרה של שגיאה ב־DB, נניח שאין קבוצה, נפנה למסך בחירת קבוצה
                    navigateToGroupSelection();
                }
            });
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToGroupSelection() {
        Intent intent = new Intent(LoginActivity.this, GroupSelectionActivity.class);
        startActivity(intent);
        finish();
    }
}
