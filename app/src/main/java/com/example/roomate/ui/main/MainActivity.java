package com.example.roomate.ui.main;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.roomate.R;

public class MainActivity extends AppCompatActivity {

    // משתנה שיחזיק הפניה ל־NavController (שולט בניווט בין Fragments)
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 1. מטעינים את ה־layout המתאים
        setContentView(R.layout.activity_main);

        // 2. מציאת NavHostFragment מתוך ה־FragmentManager
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);

        // 3. קבלת ה־NavController מתוך ה־NavHostFragment
        navController = navHostFragment.getNavController();

        // 4. מציאת ה־BottomNavigationView מתוך ה־layout
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // 5. חיבור ה־BottomNavigationView ל־NavController
        NavigationUI.setupWithNavController(bottomNav, navController);

        // 6. OPTIONAL: חיבור ה־ActionBar (Up button) ל־NavController
        NavigationUI.setupActionBarWithNavController(this, navController);

        // 7. OPTIONAL: טיפול בלחיצה חוזרת על אותו פריט בתפריט התחתון
        bottomNav.setOnItemReselectedListener(item -> {
            // למשל: לגלול לרשימת המטלות למעלה, לרענן תוכן וכו'.
        });
    }

    // 8. תמיכה ב-Up button בתפריט העליון
    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
