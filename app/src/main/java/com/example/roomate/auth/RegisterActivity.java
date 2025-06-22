package com.example.roomate.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.roomate.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    // שדות ה-UI
    private EditText etName, etEmail, etPassword;
    private Button btnRegister;
    // מופעי FirebaseAuth ו-Realtime Database
    private FirebaseAuth auth;
    private DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);  // טוען את layout של הרישום

        // אתחול מופעי Firebase
        auth = FirebaseAuth.getInstance();
        db   = FirebaseDatabase.getInstance()
                .getReference("users");            // מצביע לענף "users" ב-DB

        // מציאת ה-Views ב-XML לפי ה-IDs
        etName      = findViewById(R.id.etName);
        etEmail     = findViewById(R.id.etEmailReg);
        etPassword  = findViewById(R.id.etPasswordReg);
        btnRegister = findViewById(R.id.btnRegister);

        // הגדרת לחצן ההרשמה
        btnRegister.setOnClickListener(v -> {
            // 1) קריאה לערכי הקלט והסרת רווחים מיותרים
            String name  = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String pass  = etPassword.getText().toString().trim();

            // 2) בדיקת וולידציה: שם לא ריק, אימייל לא ריק, סיסמה לפחות 6 תווים
            if (name.isEmpty() || email.isEmpty() || pass.length() < 6) {
                Toast.makeText(this,
                        "Enter valid name, email & password>=6",
                        Toast.LENGTH_SHORT).show();
                return;  // יציאה אם הקלט לא תקין
            }

            // 3) יצירת משתמש ב-FirebaseAuth
            auth.createUserWithEmailAndPassword(email, pass)
                    .addOnSuccessListener(authResult -> {
                        // 4) onSuccess: קבלת ה־UID של המשתמש שנוצר
                        String uid = auth.getCurrentUser().getUid();

                        // 5) בניית Map עם פרטי המשתמש לשמירה ב-DB
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("id",    uid);
                        userMap.put("name",  name);
                        userMap.put("email", email);

                        // 6) שמירה ב-Realtime Database תחת /users/{uid}
                        db.child(uid)
                                .setValue(userMap)
                                .addOnSuccessListener(aVoid -> {
                                    // 7) DB success: הודעה למשתמש
                                    Toast.makeText(this,
                                            "Registered successfully!",
                                            Toast.LENGTH_SHORT).show();

                                    // 8) ניווט ל-GroupSelectionActivity כדי לבחור/ליצור קבוצה
                                    startActivity(new Intent(
                                            this, GroupSelectionActivity.class));
                                    finish();  // סוגר את RegisterActivity
                                })
                                .addOnFailureListener(e -> {
                                    // 9) DB failure: טיפול בשגיאה בכתיבה
                                    Toast.makeText(this,
                                            "DB Error: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        // 10) Auth failure: טיפול בשגיאה ביצירת המשתמש
                        Toast.makeText(this,
                                "Auth Failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        });
    }
}
