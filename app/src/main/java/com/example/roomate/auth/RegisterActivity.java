
package com.example.roomate.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.roomate.R;
import com.example.roomate.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import androidx.appcompat.widget.Toolbar;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    private EditText etName, etEmail, etPassword;
    private Button   btnRegister;

    private FirebaseAuth    auth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("יצירת חשבון");



        // אתחול Firebase
        auth     = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance()
                .getReference("users"); // ענף הפרופילים

        // אתחול ה־Views
        etName     = findViewById(R.id.etName);
        etEmail    = findViewById(R.id.etEmailReg);
        etPassword = findViewById(R.id.etPasswordReg);
        btnRegister= findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            String name  = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String pass  = etPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || pass.length() < 6) {
                Toast.makeText(this,
                        "Enter valid name, email & password>=6",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            //  יצירת משתמש ב־FirebaseAuth
            auth.createUserWithEmailAndPassword(email, pass)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser fbUser = auth.getCurrentUser();
                        if (fbUser == null) {
                            Toast.makeText(this,
                                    "Error: user is null after registration",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        String uid = fbUser.getUid();
                        Log.d(TAG, "Registered new user, uid=" + uid);

                        // בניית אובייקט User מלא
                        User newUser = new User(
                                uid,
                                name,
                                email,
                                "",    // avatarUrl ריק כברירת מחדל
                                null   // groupId עדיין לא קיים
                        );

                        // שמירת הפרופיל ב־Realtime Database
                        usersRef.child(uid)
                                .setValue(newUser)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "User profile saved under /users/" + uid);
                                    Toast.makeText(this,
                                            "Registered successfully!",
                                            Toast.LENGTH_SHORT).show();
                                    // מעבר למסך בחירת קבוצה
                                    startActivity(new Intent(
                                            this, GroupSelectionActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to save user profile", e);
                                    Toast.makeText(this,
                                            "DB Error: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Auth failed", e);
                        Toast.makeText(this,
                                "Auth Failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        });
    }
}
