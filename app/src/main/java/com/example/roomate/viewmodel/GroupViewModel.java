// app/src/main/java/com/example/roomate/viewmodel/GroupViewModel.java
package com.example.roomate.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.roomate.model.Group;
import com.example.roomate.repository.GroupRepository;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

/**
 * גשר בין UI ל־GroupRepository, מספק LiveData של Groups
 */
public class GroupViewModel extends AndroidViewModel {
    private final GroupRepository repo;
    private final LiveData<List<Group>> groups;

    public GroupViewModel(@NonNull Application application) {
        super(application);
        repo   = new GroupRepository();   // ← אתחול הריפוזיטורי
        groups = repo.getAllGroups();     // ← שמירת ה-LiveData
        Log.d("GroupViewModel", "Initialized, observing groups"); // ← לוג לאיפוס
    }

    /** מחזיר LiveData של כל הקבוצות */
    public LiveData<List<Group>> getGroups() {
        return groups;
    }

    /**
     * יוצר או מעדכן קבוצה ב־DB
     * @param id מזהה ייחודי
     * @param name שם חדש
     * @param cb מאזין להצלחות/שגיאות
     */
    public void createGroup(
            @NonNull String id,
            @NonNull String name,
            @NonNull DatabaseReference.CompletionListener cb
    ) {
        repo.createOrUpdateGroup(id, name, (error, ref) -> {
            if (error == null) {
                Log.d("GroupViewModel", "Group created/updated: " + id);
            } else {
                Log.e("GroupViewModel", "Failed to create/update group: " + id, error.toException());
            }
            cb.onComplete(error, ref);  // ← ממשיך ל-Activity או Fragment
        });
    }
}
