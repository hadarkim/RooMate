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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * אחראי על גישה למשתמשים מתוך Firebase Realtime Database.
 * כולל:
 *  - getUsersByGroup: מושך את כל ה-UIDs מתוך /groups/{groupID}/members, ואז מפרש כל /users/{uid} ל-User.
 *  - getUserById: מושך פרטי משתמש יחיד מתוך /users/{uid}.
 *  - fetchUsersByIds: מושך פרטי משתמשים עבור רשימת UIDs נתונה ומשגר callback כשסיום.
 */
public class UserRepository {
    private static final String TAG = "UserRepository";

    private final DatabaseReference groupsRef =
            FirebaseDatabase.getInstance().getReference("groups");
    private final DatabaseReference usersRef =
            FirebaseDatabase.getInstance().getReference("users");

    /**
     * מחזיר LiveData של רשימת משתמשים (List<User>) שהם חברי הקבוצה:
     *  1. קורא ל-/groups/{groupID}/members כדי לקבל רשימת UIDs.
     *  2. עבור כל UID, קורא ל-/users/{uid} לקבל את פרטי ה-User.
     *  3. מרכיב List<User> מסודר לפי סדר ה-UIDs שהתקבלו, ומפרסם ל-LiveData כשכל הנתונים נטענו.
     *
     * @param groupID מזהה הקבוצה
     * @return LiveData<List<User>> שמשתנה כאשר משתנה מבנה החברים או פרטי המשתמשים משתנים
     */
    public LiveData<List<User>> getUsersByGroup(String groupID) {
        MutableLiveData<List<User>> liveUsers = new MutableLiveData<>();

        DatabaseReference membersRef = groupsRef
                .child(groupID)
                .child("members");

        membersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // יתכן שאין כלל members
                if (!snapshot.exists() || !snapshot.hasChildren()) {
                    liveUsers.postValue(new ArrayList<>()); // רשימה ריקה
                    return;
                }
                // בונים רשימת UIDs
                List<String> uidList = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String uid = child.getKey();
                    if (uid != null) {
                        uidList.add(uid);
                    }
                }
                if (uidList.isEmpty()) {
                    liveUsers.postValue(new ArrayList<>());
                    return;
                }
                // כדי לוודא קריאה לכל ה-UIDs, משתמשים ב-AtomicInteger לספירת השלמות
                AtomicInteger completedCount = new AtomicInteger(0);
                int total = uidList.size();
                // מפת עזר כדי לאסוף את ה-User objects שקוראים
                Map<String, User> usersMap = new HashMap<>();

                for (String uid : uidList) {
                    usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnap) {
                            User u = userSnap.getValue(User.class);
                            if (u != null) {
                                u.setId(uid);
                                usersMap.put(uid, u);
                            } else {
                                Log.w(TAG, "User data null for uid=" + uid);
                            }
                            // סימון השלמה ואיפוס ברגע שכולם חזרו
                            if (completedCount.incrementAndGet() == total) {
                                // מסדרים את הרשימה לפי סדר ה-uidList
                                List<User> userList = new ArrayList<>();
                                for (String id : uidList) {
                                    User usr = usersMap.get(id);
                                    if (usr != null) {
                                        userList.add(usr);
                                    }
                                }
                                liveUsers.postValue(userList);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Failed fetch user " + uid + ": " + error.getMessage());
                            // גם במקרה של שגיאה, נסמן השלמה בלי להוסיף למפה
                            if (completedCount.incrementAndGet() == total) {
                                List<User> userList = new ArrayList<>();
                                for (String id : uidList) {
                                    User usr = usersMap.get(id);
                                    if (usr != null) {
                                        userList.add(usr);
                                    }
                                }
                                liveUsers.postValue(userList);
                            }
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled fetching members for group " + groupID + ": " + error.getMessage());
                liveUsers.postValue(new ArrayList<>()); // או null לפי העדפה
            }
        });

        return liveUsers;
    }

    /**
     * מושך פרטי משתמש בודד לפי UID מתוך /users/{uid}.
     *
     * @param uid מזהה המשתמש
     * @return LiveData<User> שמתעדכן עם הפרטים (או null אם אין)
     */
    public LiveData<User> getUserById(String uid) {
        MutableLiveData<User> liveUser = new MutableLiveData<>();
        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User u = snapshot.getValue(User.class);
                if (u != null) {
                    u.setId(uid);
                }
                liveUser.postValue(u);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled fetch userById " + uid + ": " + error.getMessage());
                liveUser.postValue(null);
            }
        });
        return liveUser;
    }

    /**
     * מושך בתצורה אסינכרונית את פרטי המשתמשים עבור רשימת UIDs נתונה.
     * משגר callback.accept(List<User>) ברגע שכל הפניות ל-/users/{uid} סיימו.
     *
     * @param uids     רשימת מזהי משתמשים
     * @param callback Consumer<List<User>> שמקבל את התוצאה. במקרה של רשימה ריקה או שגיאה, מקבל רשימה ריקה או חלקית.
     */
    public void fetchUsersByIds(List<String> uids, Consumer<List<User>> callback) {
        if (uids == null || uids.isEmpty()) {
            // במקרה של רשימה ריקה, מחזירים רשימה ריקה מיד
            callback.accept(new ArrayList<>());
            return;
        }
        AtomicInteger completedCount = new AtomicInteger(0);
        int total = uids.size();
        Map<String, User> usersMap = new HashMap<>();

        for (String uid : uids) {
            usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User u = snapshot.getValue(User.class);
                    if (u != null) {
                        u.setId(uid);
                        usersMap.put(uid, u);
                    } else {
                        Log.w(TAG, "User data null for uid=" + uid);
                    }
                    if (completedCount.incrementAndGet() == total) {
                        List<User> result = new ArrayList<>();
                        for (String id : uids) {
                            User usr = usersMap.get(id);
                            if (usr != null) {
                                result.add(usr);
                            }
                        }
                        callback.accept(result);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed fetch user " + uid + ": " + error.getMessage());
                    // גם במקרה של שגיאה, נסמן השלמה בלי להוסיף למפה
                    if (completedCount.incrementAndGet() == total) {
                        List<User> result = new ArrayList<>();
                        for (String id : uids) {
                            User usr = usersMap.get(id);
                            if (usr != null) {
                                result.add(usr);
                            }
                        }
                        callback.accept(result);
                    }
                }
            });
        }
    }
}
