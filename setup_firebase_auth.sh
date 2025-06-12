#!/usr/bin/env bash
set -e

### 1. הגדרות ראשוניות (ערוך לפי שם ה-package שלך) ###
PACKAGE_NAME="com.example.roomate"
PACKAGE_PATH="app/src/main/java/$(echo $PACKAGE_NAME | tr '.' '/')"
AUTH_PATH="$PACKAGE_PATH/auth"
LAYOUT_PATH="app/src/main/res/layout"
BUILD_GRADLE_APP="app/build.gradle"

echo ">>> מוודא קיום תיקיות…"
mkdir -p "$AUTH_PATH"
mkdir -p "$LAYOUT_PATH"


### 3. יוצר layout/login ###
echo ">>> יוצר activity_login.xml…"
cat > "$LAYOUT_PATH/activity_login.xml" << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="24dp">

    <EditText
        android:id="@+id/etEmail"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:hint="Email"
        android:inputType="textEmailAddress"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"/>

    <EditText
        android:id="@+id/etPassword"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:hint="Password"
        android:inputType="textPassword"
        app:layout_constraintTop_toBottomOf="@id/etEmail"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:layout_marginTop="16dp"/>

    <Button
        android:id="@+id/btnLogin"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:text="Login"
        app:layout_constraintTop_toBottomOf="@id/etPassword"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:layout_marginTop="24dp"/>

    <TextView
        android:id="@+id/tvRegister"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Register new account"
        android:textColor="?attr/colorPrimary"
        app:layout_constraintTop_toBottomOf="@id/btnLogin"
        app:layout_constraintStart_toStartOf="parent"
        android:layout_marginTop="16dp"/>
</androidx.constraintlayout.widget.ConstraintLayout>
EOF

### 4. יוצר layout/register ###
echo ">>> יוצר activity_register.xml…"
cat > "$LAYOUT_PATH/activity_register.xml" << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="24dp">

    <EditText
        android:id="@+id/etName"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:hint="Full Name"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"/>

    <EditText
        android:id="@+id/etEmailReg"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:hint="Email"
        android:inputType="textEmailAddress"
        app:layout_constraintTop_toBottomOf="@id/etName"
        android:layout_marginTop="16dp"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"/>

    <EditText
        android:id="@+id/etPasswordReg"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:hint="Password"
        android:inputType="textPassword"
        app:layout_constraintTop_toBottomOf="@id/etEmailReg"
        android:layout_marginTop="16dp"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"/>

    <Button
        android:id="@+id/btnRegister"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:text="Register"
        app:layout_constraintTop_toBottomOf="@id/etPasswordReg"
        android:layout_marginTop="24dp"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"/>
</androidx.constraintlayout.widget.ConstraintLayout>
EOF

### 5. יוצר LoginActivity.java ###
echo ">>> יוצר LoginActivity.java…"
cat > "$AUTH_PATH/LoginActivity.java" << EOF
package $PACKAGE_NAME.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuth.AuthStateListener;
import com.google.firebase.auth.FirebaseUser;
import $PACKAGE_NAME.ui.main.MainActivity;
import $PACKAGE_NAME.R;

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

        auth = FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        authListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        };

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass  = etPassword.getText().toString().trim();
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Enter email & password", Toast.LENGTH_SHORT).show();
                return;
            }
            auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    if (!task.isSuccessful())
                        Toast.makeText(this, "Auth Failed: " + task.getException().getMessage(),
                                       Toast.LENGTH_LONG).show();
                });
        });

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
EOF

### 6. יוצר RegisterActivity.java ###
echo ">>> יוצר RegisterActivity.java…"
cat > "$AUTH_PATH/RegisterActivity.java" << EOF
package $PACKAGE_NAME.auth;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import $PACKAGE_NAME.R;

public class RegisterActivity extends AppCompatActivity {
    private EditText etName, etEmail, etPassword;
    private Button btnRegister;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        etName     = findViewById(R.id.etName);
        etEmail    = findViewById(R.id.etEmailReg);
        etPassword = findViewById(R.id.etPasswordReg);
        btnRegister= findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email= etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();
            if (name.isEmpty() || email.isEmpty() || pass.length()<6) {
                Toast.makeText(this, "Enter valid name, email & password>=6", Toast.LENGTH_SHORT).show();
                return;
            }
            auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String uid = auth.getCurrentUser().getUid();
                        Map<String,Object> userMap = new HashMap<>();
                        userMap.put("id", uid);
                        userMap.put("name", name);
                        userMap.put("email", email);
                        db.collection("users").document(uid)
                          .set(userMap)
                          .addOnSuccessListener(aVoid -> {
                              Toast.makeText(this, "Registered!", Toast.LENGTH_SHORT).show();
                              finish();
                          })
                          .addOnFailureListener(e ->
                              Toast.makeText(this, "Firestore Error: " + e.getMessage(),
                                             Toast.LENGTH_LONG).show()
                          );
                    } else {
                        Toast.makeText(this, "Auth Failed: " + task.getException().getMessage(),
                                       Toast.LENGTH_LONG).show();
                    }
                });
        });
}
}
EOF

echo ">>> סיום! כל הקבצים נוצרו ו־build.gradle עודכן."
echo "כעת: סנכרן (Sync) את הפרויקט ב־Android Studio והריץ."
