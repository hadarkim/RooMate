package com.example.roomate.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.roomate.R;
import com.example.roomate.auth.GroupSelectionActivity;
import com.example.roomate.auth.LoginActivity;

public class MainActivity extends AppCompatActivity {

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 0️⃣  בדיקת התחברות: אם לא מחובר → למסך התחברות
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // 1️⃣ בדיקת GROUP_ID: אם לא נבחרת קבוצה עדיין → למסך בחירת קבוצה
        String groupId = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString("GROUP_ID", null);
        if (groupId == null) {
            startActivity(new Intent(this, GroupSelectionActivity.class));
            finish();
            return;
        }

        // 2️⃣ טוענים את ה־layout רק אחרי שהמשתמש מחובר ויש GROUP_ID
        setContentView(R.layout.activity_main);

        // 3️⃣ עד סוף כמו קודם: הגדרת הניווט
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        NavigationUI.setupWithNavController(bottomNav, navController);
//        NavigationUI.setupActionBarWithNavController(this, navController);

        bottomNav.setOnItemReselectedListener(item -> {
            // יכול לשמור על scroll-to-top או רענון
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
