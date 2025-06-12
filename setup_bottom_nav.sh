#!/usr/bin/env bash
# setup_bottom_nav.sh
# מוסיף ארבעת Vector Assets לתיקיית res/drawable
# ומוסיף מחרוזות ל-res/values/strings.xml

# 1. בדיקה: האם אנחנו בתיקיית Android Studio (יש src/main/res)?
if [ ! -d "app/src/main/res" ]; then
  echo "Error: תוודא שאתה מריץ את הסקריפט מתיקיית השורש של הפרויקט (עם app/src/main/res)"
  exit 1
fi

# 2. יצירת תיקיית drawable במידת הצורך
mkdir -p app/src/main/res/drawable

# 3. פונקציה לרישום Vector Asset
add_vector_asset () {
  local name=$1
  local pathData=$2
  cat > app/src/main/res/drawable/ic_${name}.xml <<EOF
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="?attr/colorOnSurface"
        android:pathData="${pathData}" />
</vector>
EOF
  echo "✔️  הוסף ic_${name}.xml"
}

echo "🛠️ מתחיל להוסיף Vector Assets..."

# 4. הוספת ה-pathData האופייניים (מתוך Material Icons)
add_vector_asset tasks "M19,3H5c-1.1,0-2,0.9-2,2v14c0,1.1,0.9,2,2,2h14c1.1,0,2-0.9,2-2V5C21,3.9,20.1,3,19,3z M19,19H5V5h14V19z M7,7h10v2H7z M7,11h10v2H7z M7,15h7v2H7z"
add_vector_asset shopping_cart "M7,18c-1.1,0-1.99,0.9-1.99,2S5.9,22,7,22s2-0.9,2-2S8.1,18,7,18z M1,2v2h2l3.6,7.59l-1.35,2.44C4.52,14.37,5,15.28,5.94,15.5c0.92,0.22,1.85-0.15,2.36-0.81L18,6H6.21l-0.94-2H1z M17,18c-1.1,0-1.99,0.9-1.99,2S15.9,22,17,22s2-0.9,2-2S18.1,18,17,18z M6.16,12l1.25-2.25h9.2l-3.42,6.18c-0.27,0.49-0.78,0.82-1.35,0.82H7.33L6.16,12z"
add_vector_asset stats "M3,17h3v-7H3V17z M10,17h3v-4h-3V17z M17,17h3V3h-3V17z"
add_vector_asset profile "M12,12c2.21,0,4-1.79,4-4s-1.79-4-4-4s-4,1.79-4,4S9.79,12,12,12z M12,14c-4.42,0-8,1.79-8,4v2h16v-2C20,15.79,16.42,14,12,14z"

echo "✅ כל ה-Vector Assets נוספו."

# 5. הוספת המחרוזות ל-strings.xml
STRINGS_FILE="app/src/main/res/values/strings.xml"
if ! grep -q "title_tasks" "$STRINGS_FILE"; then
  echo "📝 מוסיף מחרוזות ל-$STRINGS_FILE..."
  # הוספת המחרוזות בסוף קובץ values/strings.xml, לפני </resources>
  sed -i.bak '/<\/resources>/i \
    <string name="title_tasks">מטלות</string>\n\
    <string name="title_shopping">קניות</string>\n\
    <string name="title_stats">סטטיסטיקה</string>\n\
    <string name="title_profile">פרופיל</string>\n' "$STRINGS_FILE"
  echo "✅ מחרוזות נוספו."
else
  echo "ℹ️ מחרוזות כבר קיימות ב-$STRINGS_FILE, דילגתי."
fi

echo "🎉 הפעל gradle sync ו-Run כדי לוודא שהכל נטען בהצלחה."
