#!/usr/bin/env bash
set -e

# 1. הוסף ConstraintLayout ל־build.gradle
sed -i "/implementation 'com.google.android.material/a\    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'" app/build.gradle
echo "✅ הוסף ConstraintLayout dependency"

# 2. עדכן את activity_login.xml
LOGIN_XML="app/src/main/res/layout/activity_login.xml"
if grep -q "ConstraintLayout" "$LOGIN_XML"; then
  echo "✅ activity_login.xml כבר מוגדר נכון כ־ConstraintLayout"
else
  cat << 'EOF' > "$LOGIN_XML"
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <TextView
        android:id="@+id/login_title"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:text="התחברות"
        android:textSize="24sp"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:layout_marginTop="32dp"/>

    <!-- הוסף כאן את שאר השדות עם app:layout_constraint… -->
</androidx.constraintlayout.widget.ConstraintLayout>
EOF
  echo "✅ עדכנתי activity_login.xml לרכיב ConstraintLayout תקין"
fi

echo "🎉 תיקנתי את הבעיה — הרץ שוב Build & Run."
