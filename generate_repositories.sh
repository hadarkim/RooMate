#!/usr/bin/env bash

# הגדר משתנה בסיס לחבילת הקוד
BASE_PKG="com/school/roomate"
REPO_DIR="app/src/main/java/$BASE_PKG/repository"

# יצירת התיקיה (אם אינה קיימת)
mkdir -p "$REPO_DIR"

# TaskRepository.java
cat > "$REPO_DIR/TaskRepository.java" <<EOF
package $BASE_PKG.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.yourschool.roomate.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class TaskRepository {
    private static TaskRepository instance;
    private final CollectionReference tasksRef;

    private TaskRepository() {
        tasksRef = FirebaseFirestore.getInstance().collection("tasks");
    }

    public static TaskRepository getInstance() {
        if (instance == null) {
            synchronized (TaskRepository.class) {
                if (instance == null) {
                    instance = new TaskRepository();
                }
            }
        }
        return instance;
    }

    public LiveData<List<Task>> getActiveTasks() {
        MutableLiveData<List<Task>> liveData = new MutableLiveData<>();
        tasksRef.whereEqualTo("done", false)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) return;
                    List<Task> list = new ArrayList<>();
                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Task t = doc.toObject(Task.class);
                            t.setId(doc.getId());
                            list.add(t);
                        }
                    }
                    liveData.setValue(list);
                });
        return liveData;
    }

    public void add(Task task,
                    OnSuccessListener<Void> ok,
                    OnFailureListener err) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", task.getTitle());
        data.put("room", task.getRoom());
        data.put("assignedToUserId", task.getAssignedToUserId());
        data.put("dueDate", task.getDueDate());
        data.put("priority", task.getPriority());
        data.put("done", false);

        tasksRef.add(data)
                .addOnSuccessListener(docRef -> {
                    task.setId(docRef.getId());
                    ok.onSuccess(null);
                })
                .addOnFailureListener(err);
    }

    public void toggleDone(Task task,
                           OnSuccessListener<Void> ok,
                           OnFailureListener err) {
        boolean newState = !task.isDone();
        Map<String, Object> update = new HashMap<>();
        update.put("done", newState);

        tasksRef.document(task.getId())
                .update(update)
                .addOnSuccessListener(ok)
                .addOnFailureListener(err);
    }

    public void delete(Task task,
                       OnSuccessListener<Void> ok,
                       OnFailureListener err) {
        tasksRef.document(task.getId())
                .delete()
                .addOnSuccessListener(ok)
                .addOnFailureListener(err);
    }
}
EOF

# UserRepository.java
cat > "$REPO_DIR/UserRepository.java" <<EOF
package $BASE_PKG.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yourschool.roomate.model.User;

import java.util.List;

public class UserRepository {
    private static UserRepository instance;
    private final CollectionReference usersRef;

    private UserRepository() {
        usersRef = FirebaseFirestore.getInstance().collection("users");
    }

    public static UserRepository getInstance() {
        if (instance == null) {
            synchronized (UserRepository.class) {
                if (instance == null) {
                    instance = new UserRepository();
                }
            }
        }
        return instance;
    }

    public LiveData<List<User>> getAllUsers() {
        MutableLiveData<List<User>> liveData = new MutableLiveData<>();
        // TODO: הוסף SnapshotListener לשליפת users
        return liveData;
    }

    public void add(User user) {
        // TODO: יישום הוספת משתמש
    }

    // TODO: שאר פעולות CRUD
}
EOF

# ShoppingItemRepository.java
cat > "$REPO_DIR/ShoppingItemRepository.java" <<EOF
package $BASE_PKG.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yourschool.roomate.model.ShoppingItem;

import java.util.List;

public class ShoppingItemRepository {
    private static ShoppingItemRepository instance;
    private final CollectionReference itemsRef;

    private ShoppingItemRepository() {
        itemsRef = FirebaseFirestore.getInstance().collection("shoppingItems");
    }

    public static ShoppingItemRepository getInstance() {
        if (instance == null) {
            synchronized (ShoppingItemRepository.class) {
                if (instance == null) {
                    instance = new ShoppingItemRepository();
                }
            }
        }
        return instance;
    }

    public LiveData<List<ShoppingItem>> getAllItems() {
        MutableLiveData<List<ShoppingItem>> liveData = new MutableLiveData<>();
        // TODO: הוסף SnapshotListener לשליפת הפריטים
        return liveData;
    }

    public void add(ShoppingItem item) {
        // TODO: יישום הוספת פריט
    }

    // TODO: שאר פעולות CRUD
}
EOF

# StatsRepository.java
cat > "$REPO_DIR/StatsRepository.java" <<EOF
package $BASE_PKG.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.yourschool.roomate.model.Stats;

public class StatsRepository {
    private static StatsRepository instance;

    private StatsRepository() { }

    public static StatsRepository getInstance() {
        if (instance == null) {
            synchronized (StatsRepository.class) {
                if (instance == null) {
                    instance = new StatsRepository();
                }
            }
        }
        return instance;
    }

    public LiveData<Stats> getStatsForUser(String userId) {
        MutableLiveData<Stats> liveData = new MutableLiveData<>();
        // TODO: חשב ושלוף סטטיסטיקות מהנתונים ב-Firestore
        return liveData;
    }
}
EOF

echo "✅ Repository files generated in $REPO_DIR"
