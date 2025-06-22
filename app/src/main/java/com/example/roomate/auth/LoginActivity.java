package com.example.roomate.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.roomate.R;
import com.example.roomate.ui.main.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuth.AuthStateListener;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private FirebaseAuth auth;
    private AuthStateListener authListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth       = FirebaseAuth.getInstance();
        etEmail    = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin   = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        // 1️⃣ הגדרת AuthStateListener לניווט בהתאם לקיום GROUP_ID
        authListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // שולף את ה-GROUP_ID מה־Prefs
                SharedPreferences prefs =
                        PreferenceManager.getDefaultSharedPreferences(this);
                String groupId = prefs.getString("GROUP_ID", null);

                // קובע לאן לנווט: אם אין GROUP_ID → GroupSelectionActivity, אחרת → MainActivity
                Intent intent;
                if (groupId == null) {
                    intent = new Intent(this, GroupSelectionActivity.class);
                } else {
                    intent = new Intent(this, MainActivity.class);
                }
                startActivity(intent);
                finish();
            }
        };

        // 2️⃣ התחברות בפועל
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
                    .addOnCompleteListener(this, task -> {
                        if (!task.isSuccessful()) {
                            Toast.makeText(this,
                                    "Auth Failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                        // במקרה של הצלחה, authListener ידאג לנווט
                    });
        });

        // 3️⃣ מעבר למסך הרשמה
        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        auth.removeAuthStateListener(authListener);
    }
}
