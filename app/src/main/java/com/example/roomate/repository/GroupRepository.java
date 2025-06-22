package com.example.roomate.repository;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.roomate.model.Group;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * אחראי על גישה ל־Firebase Realtime Database תחת הענף "groups"
 */
public class GroupRepository {
    // מצביע לשורש /groups
    private final DatabaseReference groupsRef = FirebaseDatabase
            .getInstance()
            .getReference("groups"); // ← נקודת התחלה לכל הפעולות

    /**
     * מחזיר LiveData של כל הקבוצות ב־DB
     */
    public LiveData<List<Group>> getAllGroups() {
        MutableLiveData<List<Group>> live = new MutableLiveData<>();
        groupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Group> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Group g = child.getValue(Group.class);
                    if (g != null) {
                        g.setId(child.getKey()); // ← שומר את ה-ID ממפתח העץ
                        list.add(g);
                    }
                }
                Log.d("GroupRepo", "Loaded groups size=" + list.size()); // ← לוג לבדיקה
                live.postValue(list); // ← מעדכן את ה-LiveData
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("GroupRepo", "Failed to load groups", error.toException()); // ← טיפול בשגיאה
            }
        });
        return live;
    }

    /**
     * יוצר או מעדכן קבוצה ב־DB
     * @param id מזהה ייחודי של הקבוצה
     * @param name השם שיופיע ללקוחות
     * @param cb CompletionListener לקבלת הצלחה/כשלון
     */
    public void createOrUpdateGroup(
            @NonNull String id,
            @NonNull String name,
            @NonNull DatabaseReference.CompletionListener cb
    ) {
        groupsRef
                .child(id)              // ← לענף הקבוצה הספציפית
                .child("name")          // ← נשמור את השדה "name"
                .setValue(name, cb);    // ← מוסיף CompletionListener
    }
}
