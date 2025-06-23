package com.example.roomate.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.roomate.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * אחראי על טעינת פרופילי משתמשים מתוך /users
 * ועל מיפוי חברי קבוצה לפי רשימת UID בלבד
 */
public class UserRepository {
    private static final String TAG = "UserRepository";

    // מצביע לשורש /groups
    private final DatabaseReference groupsRef =
            FirebaseDatabase.getInstance()
                    .getReference("groups");

    // מצביע לשורש /users
    private final DatabaseReference usersRef =
            FirebaseDatabase.getInstance()
                    .getReference("users");

    /**
     * מחזיר LiveData של רשימת User עבור קבוצה נתונה.
     * טוען קודם את רשימת ה-UIDs מ־/groups/{groupID}/members,
     * ואז עבור כל UID טוען את ה-User המלא מ־/users/{uid}.
     *
     * @param groupID מזהה הקבוצה
     */
    public LiveData<List<User>> getUsersByGroup(String groupID) {
        MutableLiveData<List<User>> liveUsers = new MutableLiveData<>();

        DatabaseReference membersRef = groupsRef
                .child(groupID)
                .child("members");

        membersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Boolean> membersMap =
                        (Map<String, Boolean>) snapshot.getValue();

                if (membersMap == null || membersMap.isEmpty()) {
                    Log.d(TAG, "getUsersByGroup: no members in group " + groupID);
                    liveUsers.postValue(new ArrayList<>());
                    return;
                }

                List<User> users = new ArrayList<>();
                // נספור כמה UID-ים יש כדי לדעת מתי סיימנו
                int total = membersMap.size();

                for (String uid : membersMap.keySet()) {
                    usersRef.child(uid)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot userSnap) {
                                    User u = userSnap.getValue(User.class);
                                    if (u != null) {
                                        u.setId(uid);
                                        users.add(u);
                                    }
                                    if (users.size() == total) {
                                        Log.d(TAG, "getUsersByGroup: fetched " + users.size() + " users");
                                        liveUsers.postValue(users);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e(TAG, "getUsersByGroup: failed to fetch user " + uid,
                                            error.toException());
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "getUsersByGroup: members listener cancelled", error.toException());
            }
        });

        return liveUsers;
    }

    /**
     * מחזיר LiveData של User יחיד לפי UID.
     *
     * @param uid מזהה המשתמש
     */
    public LiveData<User> getUserById(String uid) {
        MutableLiveData<User> live = new MutableLiveData<>();

        usersRef.child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        User u = snap.getValue(User.class);
                        if (u != null) {
                            u.setId(uid);
                            live.postValue(u);
                        } else {
                            Log.w(TAG, "getUserById: user null for uid=" + uid);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError err) {
                        Log.e(TAG, "getUserById: cancelled for uid=" + uid,
                                err.toException());
                    }
                });

        return live;
    }

    /**
     * טוען מספר משתמשים בו־זמנית.
     *
     * @param uids רשימת הגדרי המשתמשים
     * @param cb   callback עם התוצאה
     */
    public void fetchUsersByIds(List<String> uids, UserFetchCallback cb) {
        List<User> users = new ArrayList<>();
        int total = uids.size();

        for (String uid : uids) {
            usersRef.child(uid)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snap) {
                            User u = snap.getValue(User.class);
                            if (u != null) {
                                u.setId(uid);
                                users.add(u);
                            }
                            if (users.size() == total) {
                                cb.onUsersFetched(users);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError err) {
                            Log.e(TAG, "fetchUsersByIds: failed for uid=" + uid,
                                    err.toException());
                        }
                    });
        }
    }

    /** callback לטעינת מספר משתמשים */
    public interface UserFetchCallback {
        void onUsersFetched(List<User> users);
    }
}
