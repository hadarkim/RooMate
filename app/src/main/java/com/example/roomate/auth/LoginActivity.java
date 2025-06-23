
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

import androidx.appcompat.app.AppCompatActivity;

import com.example.roomate.R;
import com.example.roomate.ui.main.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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

        //  התחברות בפועל
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

                        // החלטה על ניווט לפי PREFS
                        SharedPreferences prefs =
                                PreferenceManager.getDefaultSharedPreferences(this);
                        String groupId = prefs.getString("GROUP_ID", null);

                        Intent intent;
                        if (groupId == null) {
                            intent = new Intent(this, GroupSelectionActivity.class);
                            Log.d(TAG, "No GROUP_ID → navigating to GroupSelectionActivity");
                        } else {
                            intent = new Intent(this, MainActivity.class);
                            Log.d(TAG, "Found GROUP_ID → navigating to MainActivity");
                        }
                        startActivity(intent);
                        finish();
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
}
