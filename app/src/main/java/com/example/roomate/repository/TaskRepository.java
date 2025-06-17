package com.example.roomate.repository;

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

// יבוא ה־Consumer לטיפול בשגיאות
import java.util.function.Consumer;

/**
 * Singleton Repository עבור מטלות ב־Firebase Realtime Database.
 * כעת תומך בשני “מסלולים”:
 *  • openTasks: מטלות לא בוצעו, ממוין לפי dueDateMillis
 *  • overdueTasks: מטלות שתאריך היעד שלהן כבר עבר
 */
public class TaskRepository {

    private static TaskRepository INSTANCE;
    private final DatabaseReference tasksRef;

    // LiveData למסלול “מטלות פתוחות” (open)
    private final MutableLiveData<List<Task>> liveOpenTasks = new MutableLiveData<>();

    // LiveData למסלול “מטלות פג תוקף” (overdue)
    private final MutableLiveData<List<Task>> liveOverdueTasks = new MutableLiveData<>();

    private TaskRepository() {
        tasksRef = FirebaseDatabase
                .getInstance()
                .getReference("tasks");
    }

    /** מחזיר את ה־Singleton instance */
    public static synchronized TaskRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TaskRepository();
        }
        return INSTANCE;
    }

    /**
     * מטלות שלא בוצעו (done==false), ממוינות לפי תאריך יעד (dueDateMillis).
     * תשמש למסך הראשי (TaskListFragment).
     */
    public LiveData<List<Task>> getOpenTasksSortedByDate() {
        tasksRef
                .orderByChild("dueDateMillis")       // מיון לפי dueDateMillis
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        List<Task> list = new ArrayList<>();
                        for (DataSnapshot child : snap.getChildren()) {
                            Task t = child.getValue(Task.class);
                            // סינון: רק מטלות שעדיין לא בוצעו
                            if (t != null && !t.isDone()) {
                                list.add(t);
                            }
                        }
                        liveOpenTasks.setValue(list);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError err) {
                        err.toException().printStackTrace();
                    }
                });
        return liveOpenTasks;
    }

    /**
     * מטלות שתאריך היעד שלהן כבר עבר (dueDateMillis <= now).
     * תשמש כדי לתזמן התראות, לא לצפייה במסך.
     */
    public LiveData<List<Task>> getTasksDueUpToNow() {
        long now = System.currentTimeMillis();
        tasksRef
                .orderByChild("dueDateMillis")       // מיון לפי dueDateMillis
                .endAt(now)                          // רק עד ה־millis הנוכחי
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        List<Task> list = new ArrayList<>();
                        for (DataSnapshot child : snap.getChildren()) {
                            Task t = child.getValue(Task.class);
                            // לא עושים סינון נוסף על done, אפשר להוסיף אם רוצים
                            if (t != null) {
                                list.add(t);
                            }
                        }
                        liveOverdueTasks.setValue(list);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError err) {
                        err.toException().printStackTrace();
                    }
                });
        return liveOverdueTasks;
    }

    /**
     * להוספת מטלה חדשה עם שני מאזינים:
     *  • onSuccess: רץ כשהשמירה הצליחה
     *  • onError:   רץ כשהשמירה נכשלת, מקבל את ה-Exception
     */
    public void addTask(
            @NonNull Task t,
            @NonNull Runnable onSuccess,
            @NonNull Consumer<Exception> onError
    ) {
        tasksRef
                .child(t.getId())
                .setValue(t)
                .addOnSuccessListener(v -> {
                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    if (onError != null) onError.accept(e);
                });
    }

    /**
     * Overload לשמירה על תאימות-אחורית:
     * אם לא מעבירים onError, נדפיס stacktrace ברירת-מחדל.
     */
    public void addTask(
            @NonNull Task t,
            @NonNull Runnable onSuccess
    ) {
        addTask(t,
                onSuccess,
                e -> e.printStackTrace()  // ברירת-מחדל לטיפול בשגיאה
        );
    }


    /** לעדכון מטלה קיימת (למשל סימון כבוצע) */
    public void updateTask(Task t) {
        tasksRef
                .child(t.getId())
                .setValue(t);
    }

    /** למחיקת מטלה לפי מזהה */
    public void deleteTask(String taskId) {
        tasksRef
                .child(taskId)
                .removeValue();
    }
}
