// com/example/roomate/repository/GroupRepository.java

package com.example.roomate.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.roomate.model.Group;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * אחראי על גישה ל־Firebase Realtime Database תחת הענף "groups"
 */
public class GroupRepository {
    private static final String TAG = "GroupRepository";
    private final DatabaseReference groupsRef =
            FirebaseDatabase.getInstance().getReference("groups");

    /**
     * מחזיר LiveData של כל הקבוצות ב־DB
     */
    public LiveData<List<Group>> getAllGroups() {
        MutableLiveData<List<Group>> live = new MutableLiveData<>();
        groupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                List<Group> list = new ArrayList<>();
                for (DataSnapshot child : snap.getChildren()) {
                    Group g = child.getValue(Group.class);
                    if (g != null) {
                        g.setId(child.getKey());
                        list.add(g);
                    }
                }
                Log.d(TAG, "Loaded groups size=" + list.size());
                live.postValue(list);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError err) {
                Log.e(TAG, "Failed to load groups", err.toException());
            }
        });
        return live;
    }

    /**
     * יוצר קבוצה חדשה באופן אטומי: שם + חבר יוצר
     *
     * @param id         מזהה ייחודי של הקבוצה
     * @param name       השם שיוצג ללקוחות
     * @param creatorUid ה-UID של היוצר; יתווסף למפת members כ- UID→true
     * @return Task<Void> של פעולת הכתיבה
     */
    public Task<Void> createGroup(
            @NonNull String id,
            @NonNull String name,
            @NonNull String creatorUid
    ) {
        Map<String, Object> groupData = new HashMap<>();
        groupData.put("name", name);

        Map<String, Boolean> members = new HashMap<>();
        members.put(creatorUid, true);
        groupData.put("members", members);

        Log.d(TAG, "Creating group atomically: id=" + id + " name=" + name);
        return groupsRef.child(id).setValue(groupData);
    }

    /**
     * מצטרף לקבוצה: מוסיף את ה-UID למפת members
     *
     * @param groupId מזהה הקבוצה
     * @param uid     ה-UID של המשתמש להצטרפות
     * @return Task<Void> של פעולת הכתיבה
     */
    public Task<Void> joinGroup(
            @NonNull String groupId,
            @NonNull String uid
    ) {
        Log.d(TAG, "Joining group: id=" + groupId + " uid=" + uid);
        return groupsRef.child(groupId).child("members").child(uid).setValue(true);
    }

    /**
     * משנה רק את שם הקבוצה (בלי לשנות את members)
     *
     * @param groupId מזהה הקבוצה
     * @param newName השם החדש להצגה
     * @return Task<Void> של פעולת העדכון
     */
    public Task<Void> updateGroupName(
            @NonNull String groupId,
            @NonNull String newName
    ) {
        Log.d(TAG, "Updating group name: id=" + groupId + " newName=" + newName);
        return groupsRef.child(groupId).child("name").setValue(newName);
    }

    /**
     * מחזיר LiveData של Group בודד לפי groupId.
     * ברגע שהנתונים נטענים מ־DB, LiveData יתעדכן.
     *
     * @param groupId מזהה הקבוצה
     * @return LiveData<Group> עם השדה id ו-name ומפת members (אם תרצה להשתמש בה)
     */
    public LiveData<Group> getGroupById(String groupId) {
        MutableLiveData<Group> live = new MutableLiveData<>();
        groupsRef.child(groupId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        Group g = snap.getValue(Group.class);
                        if (g != null) {
                            g.setId(groupId);
                            // members יתמפה אוטומטית אם יש בשדה "members" במבנה הנכון
                            live.postValue(g);
                        } else {
                            Log.w(TAG, "getGroupById: Group null for id=" + groupId);
                            live.postValue(null);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "getGroupById cancelled for id=" + groupId, error.toException());
                        live.postValue(null);
                    }
                });
        return live;
    }
}
