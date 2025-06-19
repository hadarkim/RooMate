package com.example.roomate.ui.stats;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * ViewModel שמושך בזמן אמת (Realtime Database) את מספר המטלות שבוצעו ופתוחות
 * ומשדר אותן כ‐LiveData<Map<String,Integer>> שבה המפתחות:
 *   "done" -> מספר מטלות בוצעו
 *   "todo" -> מספר מטלות פתוחות
 */
public class StatsViewModel extends ViewModel {
    // LiveData לצפייה בסטטיסטיקות
    private final MutableLiveData<Map<String, Integer>> chartData = new MutableLiveData<>();

    // reference אל צומת המטלות ב־Realtime Database
    private final DatabaseReference tasksRef =
            FirebaseDatabase.getInstance()
                    .getReference("tasks");

    // השמית ה־ValueEventListener כדי שנוכל להסיר אותו אם נרצה
    private ValueEventListener tasksListener;

    /**
     * מחזיר LiveData של הסטטיסטיקות; מתחיל להאזין לנתונים
     */
    public LiveData<Map<String, Integer>> getChartData() {
        if (tasksListener == null) {
            loadDataFromFirebase();
        }
        return chartData;
    }

    /**
     * יוצר ומוסיף ValueEventListener על צומת "tasks"
     * ומעדכן את ה-MutableLiveData בספירת המטלות
     */
    private void loadDataFromFirebase() {
        tasksListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int doneCount = 0;
                int todoCount = 0;

                for (DataSnapshot taskSnap : snapshot.getChildren()) {
                    Boolean done = taskSnap.child("done").getValue(Boolean.class);
                    if (done != null && done) {
                        doneCount++;
                    } else {
                        todoCount++;
                    }
                }

                // הכנת המיפוי ושליחתו ל-LiveData
                Map<String, Integer> data = new HashMap<>();
                data.put("done", doneCount);
                data.put("todo", todoCount);
                chartData.setValue(data);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // במידה ויש שגיאה, ניתן לרשום לוג:
                // Log.e("StatsViewModel", "Firebase load cancelled", error.toException());
            }
        };

        // הוספת המאזין לצומת
        tasksRef.addValueEventListener(tasksListener);
    }

    /**
     * נקרא כאשר ה-ViewModel מושמד; מסיר את המאזין כדי למנוע memory leak
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        if (tasksListener != null) {
            tasksRef.removeEventListener(tasksListener);
        }
    }
}
