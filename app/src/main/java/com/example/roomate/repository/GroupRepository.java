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

public class GroupRepository {
    private static final String TAG = "GroupRepository";
    private final DatabaseReference groupsRef =
            FirebaseDatabase.getInstance().getReference("groups");

    public LiveData<List<Group>> getAllGroups() {
        MutableLiveData<List<Group>> live = new MutableLiveData<>();
        groupsRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
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
            @Override public void onCancelled(@NonNull DatabaseError err) {
                Log.e(TAG, "Failed to load groups", err.toException());
            }
        });
        return live;
    }

    public Task<Void> createGroup(
            @NonNull String id,
            @NonNull String name,
            @NonNull String creatorUid
    ) {
        Map<String,Object> groupData = new HashMap<>();
        groupData.put("name", name);
        Map<String,Boolean> members = new HashMap<>();
        members.put(creatorUid, true);
        groupData.put("members", members);

        Log.d(TAG, "Creating group atomically: id=" + id + " name=" + name);
        return groupsRef.child(id).setValue(groupData);
    }

    public Task<Void> joinGroup(
            @NonNull String groupId,
            @NonNull String uid
    ) {
        Log.d(TAG, "Joining group: id=" + groupId + " uid=" + uid);
        return groupsRef.child(groupId).child("members").child(uid).setValue(true);
    }
}
