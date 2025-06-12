#!/usr/bin/env bash
#
# init_packages.sh
# סקריפט ליצירת חבילות וקבצי שלד ראשוניים עבור rooMate
#

# שנה את השם לפי חבילת הפרויקט שלך:
BASE_PKG="com/school/roomate"

# היכן לפרויקט:
APP_SRC="app/src/main/java/$BASE_PKG"

# רשימת חבילות לשכפל
declare -a PACKAGES=(
  "auth"
  "ui/main"
  "ui/tasks"
  "ui/shopping"
  "ui/profile"
  "ui/stats"
  "ui/settings"
  "model"
  "repository"
  "util"
  "notification"
)

echo "יצירת מבנה חבילות תחת $APP_SRC"
for pkg in "${PACKAGES[@]}"; do
  dir="$APP_SRC/$pkg"
  mkdir -p "$dir"
  # צור קובץ placeholder לכל חבילה
  touch "$dir/.gitkeep"
  echo "  - $pkg"
done

# צור שלד קבצי Activity ו-Fragment
echo "יצירת שלד קבצי Java:"
cat > "$APP_SRC/auth/LoginActivity.java" <<EOF
package com.school.roomate.auth;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // TODO: init views & FirebaseAuth
    }
}
EOF

cat > "$APP_SRC/ui/main/MainActivity.java" <<EOF
package com.school.roomate.ui.main;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // TODO: setup BottomNavigationView + Navigation Component
    }
}
EOF

# צור שלד Fragment לדוגמא
cat > "$APP_SRC/ui/tasks/TaskListFragment.java" <<EOF
package com.school.roomate.ui.tasks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class TaskListFragment extends Fragment {
    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_list, container, false);
    }
}
EOF

echo "✔️ השלמת יצירת החבילות והקבצים."
