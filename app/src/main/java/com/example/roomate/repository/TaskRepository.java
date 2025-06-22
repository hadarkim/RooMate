package com.example.roomate.repository;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.roomate.model.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TaskRepository {
    // ← **שונה**: עכשיו מצביע לתת־אוסף של מטלות בתוך קבוצה
    private final DatabaseReference tasksRef;

    /**
     * **שונה**: קבל now גם את groupID,
     * כדי שכל השאילתות יתייחסו ל-/groups/{groupID}/tasks
     */
    public TaskRepository(@NonNull String groupID) {
        tasksRef = FirebaseDatabase
                .getInstance()
                .getReference("groups")
                .child(groupID)
                .child("tasks");
    }

    /**
     * 1) מטלות לא בוצעו (done==false)
     *    ממוינות לפי dueDateMillis
     */
    public LiveData<List<Task>> getOpenTasksSortedByDate() {
        MutableLiveData<List<Task>> liveOpenTasks = new MutableLiveData<>();

        tasksRef
                .orderByChild("dueDateMillis")   // מיון לפי שדה חדש ב־Task
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        List<Task> list = new ArrayList<>();
                        for (DataSnapshot child : snap.getChildren()) {
                            Task t = child.getValue(Task.class);
                            if (t != null && !t.isDone()) {
                                t.setId(child.getKey());    // ← חשוב: לשמור את ה־ID ממפתח העץ
                                list.add(t);
                            }
                        }
                        // ← **הוספת לוג** כדי לוודא שהנתונים התקבלו
                        Log.d("TaskRepo", "open tasks size=" + list.size());
                        liveOpenTasks.postValue(list);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError err) {
                        // ← **הוספת לוג שגיאה**
                        Log.e("TaskRepo", "open-tasks listener cancelled", err.toException());
                    }
                });

        return liveOpenTasks;
    }

    /**
     * 2) מטלות ש־dueDateMillis שלהן עבר (<= now)
     */
    public LiveData<List<Task>> getTasksDueUpToNow() {
        MutableLiveData<List<Task>> liveOverdueTasks = new MutableLiveData<>();
        long now = System.currentTimeMillis();

        tasksRef
                .orderByChild("dueDateMillis")   // מיון לפי תאריך יעד
                .endAt(now)                      // עד המיליסקנד הנוכחי
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        List<Task> list = new ArrayList<>();
                        for (DataSnapshot child : snap.getChildren()) {
                            Task t = child.getValue(Task.class);
                            if (t != null) {
                                t.setId(child.getKey());  // ← לשמור את המפתח
                                list.add(t);
                            }
                        }
                        // ← **הוספת לוג** על מספר המטלות שפגו תוקף
                        Log.d("TaskRepo", "overdue tasks size=" + list.size());
                        liveOverdueTasks.postValue(list);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError err) {
                        // ← **הוספת לוג שגיאה**
                        Log.e("TaskRepo", "overdue-tasks listener cancelled", err.toException());
                    }
                });

        return liveOverdueTasks;
    }

    /**
     * 3) הוספת מטלה חדשה עם מאזיני הצלחה/כישלון
     */
    public void addTask(
            @NonNull Task t,
            @NonNull Runnable onSuccess,
            @NonNull Consumer<Exception> onError
    ) {
        tasksRef
                .child(t.getId())   // ← הנתיב כבר כולל groupID/tasks
                .setValue(t)
                .addOnSuccessListener(v -> {
                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    if (onError != null) onError.accept(e);
                });
    }

    /** 4) עדכון מטלה קיימת (למשל סימון כבוצע) */
    public void updateTask(@NonNull Task t) {
        tasksRef
                .child(t.getId())
                .setValue(t);
    }

    /** 5) מחיקת מטלה לפי מזהה */
    public void deleteTask(@NonNull String taskId) {
        tasksRef
                .child(taskId)
                .removeValue();
    }
}
