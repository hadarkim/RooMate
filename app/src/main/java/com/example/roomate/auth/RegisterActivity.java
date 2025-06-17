package com.example.roomate.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.roomate.R;
import com.example.roomate.ui.main.MainActivity;           // הנחה: MainActivity היא מסך הבית
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private EditText etName, etEmail, etPassword;
    private Button btnRegister;
    private FirebaseAuth auth;
    private DatabaseReference db;   // <-- שינינו ל-DatabaseReference

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        // כאן אנחנו מקבלים את השורש "users" ב-Realtime DB
        db   = FirebaseDatabase
                .getInstance()
                .getReference("users");

        etName     = findViewById(R.id.etName);
        etEmail    = findViewById(R.id.etEmailReg);
        etPassword = findViewById(R.id.etPasswordReg);
        btnRegister= findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email= etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || pass.length() < 6) {
                Toast.makeText(this,
                        "Enter valid name, email & password>=6",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // 1) צור משתמש ב-Auth
            auth.createUserWithEmailAndPassword(email, pass)
                    .addOnSuccessListener(authResult -> {
                        // 2) קיבל UID
                        String uid = auth.getCurrentUser().getUid();

                        // 3) בנה Map עם נתוני המשתמש
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("id",    uid);
                        userMap.put("name",  name);
                        userMap.put("email", email);

                        // 4) שמור ב-Realtime Database תחת /users/{uid}
                        db.child(uid)
                                .setValue(userMap)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this,
                                            "Registered successfully!",
                                            Toast.LENGTH_SHORT).show();
                                    // מעבר למסך הראשי
                                    startActivity(
                                            new Intent(this, MainActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    // e כאן הוא Exception תקין
                                    Toast.makeText(this,
                                            "DB Error: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        // טיפול בכישלון ברישום ב-Auth
                        Toast.makeText(this,
                                "Auth Failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        });
    }
}
