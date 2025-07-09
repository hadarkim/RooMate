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

/**
 * אחראי על גישה ל־Firebase Realtime Database תחת /groups/{groupId}/tasks
 */
public class TaskRepository {
    private static final String TAG = "TaskRepository";

    private final String groupId;
    private final DatabaseReference tasksRef;
    private final DatabaseReference groupMembersRef;

    public TaskRepository(@NonNull String groupID) {
        this.groupId = groupID;
        this.tasksRef = FirebaseDatabase
                .getInstance()
                .getReference("groups")
                .child(groupID)
                .child("tasks");
        this.groupMembersRef = FirebaseDatabase
                .getInstance()
                .getReference("groups")
                .child(groupID)
                .child("members");
    }

    /**
     * מטלות פתוחות ממוינות לפי dueDateMillis.
     * כעת **ללא** סינון על done, כדי שגם מטלות שבוצעו יופיעו.
     */
    public LiveData<List<Task>> getOpenTasksSortedByDate() {
        MutableLiveData<List<Task>> liveOpenTasks = new MutableLiveData<>();
        tasksRef
                .orderByChild("dueDateMillis")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        List<Task> list = new ArrayList<>();
                        for (DataSnapshot child : snap.getChildren()) {
                            Task t = child.getValue(Task.class);
                            if (t != null) {
                                t.setId(child.getKey());
                                list.add(t);
                            }
                        }
                        Log.d(TAG, "open tasks size=" + list.size());
                        liveOpenTasks.postValue(list);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError err) {
                        Log.e(TAG, "open-tasks listener cancelled", err.toException());
                    }
                });
        return liveOpenTasks;
    }

    /**
     * מטלות שפג תאריך יעד <= now
     */
    public LiveData<List<Task>> getTasksDueUpToNow() {
        MutableLiveData<List<Task>> liveOverdueTasks = new MutableLiveData<>();
        long now = System.currentTimeMillis();
        tasksRef
                .orderByChild("dueDateMillis")
                .endAt(now)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        List<Task> list = new ArrayList<>();
                        for (DataSnapshot child : snap.getChildren()) {
                            Task t = child.getValue(Task.class);
                            if (t != null) {
                                t.setId(child.getKey());
                                list.add(t);
                            }
                        }
                        Log.d(TAG, "overdue tasks size=" + list.size());
                        liveOverdueTasks.postValue(list);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError err) {
                        Log.e(TAG, "overdue-tasks listener cancelled", err.toException());
                    }
                });
        return liveOverdueTasks;
    }

    /**
     * הוספת מטלה חדשה.
     * ה־Task המועבר מכיל assignedToUid (לרוב ה-UID של היוצר, או UID אחר בהתאם למדיניות).
     * לפני השמירה, בודקים שה־assignedToUid הוא חבר בקבוצה.
     *
     * @param t          אובייקט Task עם השדות הנדרשים (כולל assignedToUid)
     * @param onSuccess  ריצה במקרה הצלחה
     * @param onError    Consumer עם Exception במקרה כשלון
     */
    public void addTask(
            @NonNull Task t,
            @NonNull Runnable onSuccess,
            @NonNull Consumer<Exception> onError
    ) {
        String uid = t.getAssignedToUid();
        // בודקים שהיוצר / ה־assignedToUid הוא חבר
        groupMembersRef.child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        if (snap.exists()) {
                            // בונים ID אם חסר
                            if (t.getId() == null) {
                                String newId = tasksRef.push().getKey();
                                t.setId(newId);
                            }
                            String taskId = t.getId();
                            Log.d(TAG, "addTask: creating task id=" + taskId);
                            tasksRef.child(taskId)
                                    .setValue(t)
                                    .addOnSuccessListener(v -> {
                                        onSuccess.run();
                                    })
                                    .addOnFailureListener(e -> {
                                        onError.accept(e);
                                    });
                        } else {
                            String msg = "addTask: user " + uid + " not a member";
                            Log.e(TAG, msg);
                            onError.accept(new Exception(msg));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "addTask: failed checking membership", error.toException());
                        onError.accept(error.toException());
                    }
                });
    }

    /**
     * עדכון מטלה קיימת (למשל עריכת שדות).
     * כעת מאפשר לכל חבר בקבוצה לעדכן (לא רק assignedToUid).
     *
     * @param t          ה־Task עם השדות החדשים (id חייב להיות קיים)
     * @param updaterUid UID של המשתמש שמבצע את העדכון
     * @param cb         CompletionListener לקבלת הצלחה/כישלון
     */
    public void updateTask(@NonNull Task t,
                           @NonNull String updaterUid,
                           @NonNull DatabaseReference.CompletionListener cb) {
        String taskId = t.getId();
        if (taskId == null) {
            Log.e(TAG, "updateTask: id is null");
            cb.onComplete(null, null);
            return;
        }
        // טוענים קודם כדי לוודא שהמטלה קיימת
        tasksRef.child(taskId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                Task existing = snap.getValue(Task.class);
                if (existing == null) {
                    Log.e(TAG, "updateTask: task not found id=" + taskId);
                    cb.onComplete(null, null);
                    return;
                }
                // שינוי מדיניות: לבדוק אם updaterUid הוא חבר בקבוצה
                groupMembersRef.child(updaterUid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot memberSnap) {
                        if (!memberSnap.exists()) {
                            Log.e(TAG, "updateTask: user " + updaterUid + " not a group member");
                            cb.onComplete(null, null);
                            return;
                        }
                        // מותר לעדכן
                        Log.d(TAG, "updateTask: user " + updaterUid + " is member, updating task " + taskId);
                        tasksRef.child(taskId)
                                .setValue(t, cb);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "updateTask: failed checking membership", error.toException());
                        cb.onComplete(error, null);
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "updateTask: cancelled fetching task", error.toException());
                cb.onComplete(error, null);
            }
        });
    }

    /**
     * סימון/ביטול סימון מטלה כבוצעה.
     * מאפשר לכל חבר בקבוצה לסמן.
     *
     * @param taskId     מזהה המטלה
     * @param newDone    הערך החדש לשדה done
     * @param updaterUid UID של המשתמש שמבצע את הסימון
     * @param cb         CompletionListener לקבלת הצלחה/כישלון
     */
    public void toggleTaskDone(@NonNull String taskId,
                               boolean newDone,
                               @NonNull String updaterUid,
                               @NonNull DatabaseReference.CompletionListener cb) {
        tasksRef.child(taskId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                Task existing = snap.getValue(Task.class);
                if (existing == null) {
                    Log.e(TAG, "toggleTaskDone: task not found id=" + taskId);
                    cb.onComplete(null, null);
                    return;
                }
                // בדיקת חברות בקבוצה
                groupMembersRef.child(updaterUid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot memberSnap) {
                        if (!memberSnap.exists()) {
                            Log.e(TAG, "toggleTaskDone: user " + updaterUid + " not a group member");
                            cb.onComplete(null, null);
                            return;
                        }
                        // מותר לכל חבר לסמן כבוצע
                        Log.d(TAG, "toggleTaskDone: user " + updaterUid + " is member, setting done=" + newDone);
                        tasksRef.child(taskId).child("done")
                                .setValue(newDone, cb);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "toggleTaskDone: failed checking membership", error.toException());
                        cb.onComplete(error, null);
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "toggleTaskDone: cancelled fetching task", error.toException());
                cb.onComplete(error, null);
            }
        });
    }

    /**
     * מחיקת מטלה (אם נדרש). ניצול בדיקת חברות אם רק חבר יכול למחוק.
     * @param taskId       מזהה המטלה למחיקה
     * @param requesterUid UID של המנסה למחוק
     * @param cb           CompletionListener
     */
    public void deleteTask(@NonNull String taskId,
                           @NonNull String requesterUid,
                           @NonNull DatabaseReference.CompletionListener cb) {
        tasksRef.child(taskId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                Task existing = snap.getValue(Task.class);
                if (existing == null) {
                    Log.e(TAG, "deleteTask: task not found id=" + taskId);
                    cb.onComplete(null, null);
                    return;
                }
                groupMembersRef.child(requesterUid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot memberSnap) {
                        if (!memberSnap.exists()) {
                            Log.e(TAG, "deleteTask: user " + requesterUid + " not a group member");
                            cb.onComplete(null, null);
                            return;
                        }
                        Log.d(TAG, "deleteTask: deleting task id=" + taskId);
                        tasksRef.child(taskId).removeValue(cb);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "deleteTask: failed checking membership", error.toException());
                        cb.onComplete(error, null);
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "deleteTask: cancelled fetching task", error.toException());
                cb.onComplete(error, null);
            }
        });
    }
}
