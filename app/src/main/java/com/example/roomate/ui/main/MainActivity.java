package com.example.roomate.ui.main;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.roomate.R;

public class mainActivity extends AppCompatActivity {

    // משתנה שיחזיק הפניה ל־NavController (שולט בניווט בין Fragments)
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // מטעינים את ה־layout המתאים
        setContentView(R.layout.activity_main);

        // 1. מציאת NavHostFragment מתוך ה־FragmentManager
        //    NavHostFragment הוא ה-“מיכל” (container) שמכיל בפועל את ה־Fragments.
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);

        // 2. קבלת ה־NavController מתוך ה־NavHostFragment
        //    NavController הוא האובייקט שמטפל בניווט (navigation) בין Fragments.
        navController = navHostFragment.getNavController();

        // 3. מציאת ה־BottomNavigationView מתוך ה־layout
        // משתנה שיחזיק הפניה לרכיב ה־BottomNavigationView
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // 4. חיבור ה־BottomNavigationView ל־NavController
        //    פעולה זו גורמת לכך שכאשר המשתמש ילחץ על אחד הפריטים בתפריט התחתון,
        //    התוכנה תנווט אוטומטית ל־Fragment המתאים שהוגדר ב־nav_graph.xml
        NavigationUI.setupWithNavController(bottomNav, navController);

        // 5. (אופציונלי) במידה ורוצים לטפל באירוע לחיצה חוזרת על אותו פריט,
        //    ניתן לבצע כאן הגדרת מאזין. לדוגמה:
        bottomNav.setOnItemReselectedListener(item -> {
            // אם המשתמש לחץ שוב על אותו פריט תחתון,
            // אפשר לגלול לראש ה־RecyclerView, לרענן תוכן וכו'.
            // בשלב זה אפשר להשאיר ריק או לוגיקה מיוחדת.
        });
    }

    // 6. אופציה: מתן תמיכה ל-"Up button" (לחצן החזור בתפריט העליון).
    //    אם נרצה שהחץ העליון יפעל כמו החזור ב־NavController:
    @Override
    public boolean onSupportNavigateUp() {
        // navController.navigateUp() ינסה לנווט חזרה בס택 ה־Fragments,
        // ואם אינו מצליח – נקרא למתודה של האב (super).
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}



