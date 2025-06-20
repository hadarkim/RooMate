package com.example.roomate.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.roomate.R;
import com.example.roomate.auth.GroupSelectionActivity;

public class MainActivity extends AppCompatActivity {

    // משתנה שיחזיק הפניה ל־NavController (שולט בניווט בין Fragments)
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ▪ 1️⃣ בדיקה אם יש GROUP_ID תקין ב־SharedPreferences
        String groupId = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString("GROUP_ID", null);
        if (groupId == null) {
            // המשתמש לא בחר קבוצה עדיין → העבר למסך GroupSelectionActivity
            startActivity(new Intent(this, GroupSelectionActivity.class));
            finish();     // סוגר את MainActivity כדי שלא ייפתח חזרה
            return;       // מונע המשך הרצת הקוד
        }

        // ▪ 2️⃣ מטעינים את ה־layout רק אחרי שאנו בטוחים שיש GROUP_ID תקין
        setContentView(R.layout.activity_main);

        // 3. מציאת NavHostFragment מתוך ה־FragmentManager
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);

        // 4. קבלת ה־NavController מתוך ה־NavHostFragment
        navController = navHostFragment.getNavController();

        // 5. מציאת ה־BottomNavigationView מתוך ה־layout
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // 6. חיבור ה־BottomNavigationView ל־NavController
        NavigationUI.setupWithNavController(bottomNav, navController);

        // 7. OPTIONAL: חיבור ה־ActionBar (Up button) ל־NavController
        NavigationUI.setupActionBarWithNavController(this, navController);

        // 8. OPTIONAL: טיפול בלחיצה חוזרת על אותו פריט בתפריט התחתון
        bottomNav.setOnItemReselectedListener(item -> {
            // למשל: לגלול לרשימת המטלות למעלה, לרענן תוכן וכו'.
        });
    }

    // 9. תמיכה ב-Up button בתפריט העליון
    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
