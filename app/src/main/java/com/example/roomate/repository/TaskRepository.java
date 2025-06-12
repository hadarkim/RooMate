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

/**
 * Singleton Repository עבור מטלות, באמצעות
 * Firebase Realtime Database.
 */
public class TaskRepository {

    // ① Instance בודד (Singleton)
    private static TaskRepository INSTANCE;

    // ② Reference לשורש ה-"tasks" ב־Realtime DB
    private final DatabaseReference tasksRef;

    // ③ LiveData פנימי המשקף את רשימת המטלות
    private final MutableLiveData<List<Task>> liveTasks = new MutableLiveData<>();

    /** ctor פרטי כדי לממש Singleton */
    private TaskRepository() {
        // קבלת Reference
        tasksRef = FirebaseDatabase
                .getInstance()
                .getReference("tasks");

        // מאזין לשינויים: רק מטלות עם done==false
        tasksRef
                .orderByChild("done")
                .equalTo(false)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Task> list = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Task t = child.getValue(Task.class);
                            if (t != null) {
                                list.add(t);
                            }
                        }
                        // דוחף את הרשימה אל ה-LiveData
                        liveTasks.setValue(list);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // טיפול בשגיאה: אפשר לוג או retry
                        error.toException().printStackTrace();
                    }
                });
    }

    /** מחזיר את ה־Instance היחיד */
    public static TaskRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TaskRepository();
        }
        return INSTANCE;
    }

    /** ④ חשיפת LiveData לצפייה מטה in ViewModel */
    public LiveData<List<Task>> getActiveTasks() {
        return liveTasks;
    }

    /** ⑤ הוספת מטלה חדשה */
    public void addTask(Task t, Runnable onSuccess) {
        tasksRef
                .child(t.getId())          // מזהה ייחודי לכל מטלה
                .setValue(t)               // שומר את אובייקט ה-Task
                .addOnSuccessListener(v -> {
                    if (onSuccess != null) onSuccess.run();
                });
    }

    /** ⑥ עדכון מטלה קיימת (לדוגמה: שינוי שדה done) */
    public void updateTask(Task t) {
        tasksRef
                .child(t.getId())
                .setValue(t);
    }
}
