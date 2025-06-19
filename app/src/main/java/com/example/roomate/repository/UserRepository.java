package com.example.roomate.repository;


import android.util.Log;
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

public class UserRepository {
    // מצביע לשורש groups
    private final DatabaseReference groupsRef =
            FirebaseDatabase.getInstance()
                    .getReference("groups");

    /**
     * מחזיר LiveData של רשימת משתמשים (List<User>)
     * המתעדכן בכל פעם שיש שינוי ב-/groups/{groupID}/members
     */
    public LiveData<List<User>> getUsersByGroup(String groupID) {
        MutableLiveData<List<User>> liveUsers = new MutableLiveData<>();

        DatabaseReference membersRef = groupsRef
                .child(groupID)
                .child("members");

        // מאזין לשינויים במתודת addValueEventListener
        membersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<User> users = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    // המרה לאובייקט User
                    User u = child.getValue(User.class);
                    if (u != null) {
                        // חשוב: קובע את ה-ID לפי המפתח בשורש
                        u.setId(child.getKey());
                        users.add(u);
                    }
                }
                // ← הוספת Log.d כדי לוודא שקיבלנו את מספר המשתמשים הנכון
                Log.d("UserRepo", "Fetched users: " + users.size());

                // שולח את הרשימה ל-LiveData (UI יתעדכן אוטומטית)
                liveUsers.postValue(users);
            }

            @Override
            public void onCancelled(DatabaseError error) {// יכולות לטפל בשגיאה (לוג, LiveData נפרד וכו')
                // ← הוספת Log.e לטיפול ודיווח על שגיאות
                Log.e("UserRepo", "onCancelled fetching users", error.toException());

            }
        });

        return liveUsers;
    }
}
