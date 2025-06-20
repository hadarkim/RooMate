package com.example.roomate.auth;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth; // הוספתי את הייבוא הזה
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.roomate.R;
import com.example.roomate.model.Group;
import com.example.roomate.ui.group.GroupAdapter;
import com.example.roomate.ui.main.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GroupSelectionActivity extends AppCompatActivity {
    private static final String TAG = "GroupSelectionAct";

    private RecyclerView     rvGroups;
    private EditText         etSearchGroup, etNewGroup;
    private Button           btnCreateGroup;

    private GroupAdapter     adapter;
    private final List<Group> fullGroupList = new ArrayList<>();
    private DatabaseReference groupsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_selection);

        // 1️⃣ איתור Views
        rvGroups        = findViewById(R.id.rvGroups);
        etSearchGroup   = findViewById(R.id.etSearchGroup);
        etNewGroup      = findViewById(R.id.etNewGroup);
        btnCreateGroup  = findViewById(R.id.btnCreateGroup);

        // 2️⃣ הגדרת RecyclerView + Adapter
        adapter = new GroupAdapter(new Consumer<Group>() {
            @Override
            public void accept(Group group) {
                selectGroup(group);
            }
        });
        rvGroups.setLayoutManager(new LinearLayoutManager(this));
        rvGroups.setAdapter(adapter);

        // 3️⃣ רפרנס ל־"groups" בבסיס הנתונים
        groupsRef = FirebaseDatabase.getInstance().getReference("groups");

        // 4️⃣ טעינת קבוצות מה-Firebase
        groupsRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                fullGroupList.clear();
                for (DataSnapshot child : snap.getChildren()) {
                    Group g = child.getValue(Group.class);
                    if (g != null) {
                        g.setId(child.getKey());
                        fullGroupList.add(g);
                        Log.d(TAG, "טען קבוצה: " + g);
                    }
                }
                // מציג בהתחלה את כל הרשימה
                adapter.submitList(new ArrayList<>(fullGroupList));
                Log.d(TAG, "הגש רשימה מלאה, גודל: " + fullGroupList.size());
                // אם יש חיפוש פעיל, מסנן מיד
                String q = etSearchGroup.getText().toString().trim();
                if (!q.isEmpty()) filterAndSubmit(q);
            }
            @Override public void onCancelled(@NonNull DatabaseError err) {
                Log.e(TAG, "שגיאה בטעינת קבוצות: " + err.getMessage());
                Toast.makeText(GroupSelectionActivity.this,
                        "שגיאה בטעינת קבוצות: " + err.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });

        // 5️⃣ חיפוש — מסנן בזמן אמת
        etSearchGroup.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            @Override public void onTextChanged(CharSequence s,int st,int b,int c){
                filterAndSubmit(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s){}
        });

        // 6️⃣ יצירת/הצטרפות לקבוצה חדשה
        btnCreateGroup.setOnClickListener(v -> {
            String newId = etNewGroup.getText().toString().trim();
            if (newId.isEmpty()) {
                etNewGroup.setError("יש להזין קוד קבוצה");
                return;
            }
            String defaultName = "קבוצה " + newId;
            DatabaseReference thisRef = groupsRef.child(newId);
            thisRef.child("name").setValue(defaultName)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "צור/הצטרף: " + newId);
                            selectGroup(new Group(newId, defaultName));
                        } else {
                            Log.e(TAG, "שגיאה ביצירה: ", task.getException());
                            Toast.makeText(this,
                                    "שגיאה: " +
                                            (task.getException()!=null ? task.getException().getMessage() : ""),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    // שיטה לסינון לפי טקסט וחזרה ל-Adapter
    private void filterAndSubmit(String query) {
        if (query.isEmpty()) {
            adapter.submitList(new ArrayList<>(fullGroupList));
            Log.d(TAG, "סינון ריק → רשימה מלאה");
        } else {
            List<Group> filtered = new ArrayList<>();
            String low = query.toLowerCase();
            for (Group g: fullGroupList) {
                String name = g.getName()!=null ? g.getName().toLowerCase() : "";
                String id   = g.getId()!=null   ? g.getId().toLowerCase()   : "";
                if (name.contains(low) || id.contains(low)) filtered.add(g);
            }
            adapter.submitList(filtered);
            Log.d(TAG, "סינון \""+query+"\", גודל: "+filtered.size());
        }
    }

    // שמירת GROUP_ID, הוספת המשתמש כ-member ועברה ל-MainActivity
    private void selectGroup(@NonNull Group group) {
        Log.d(TAG, "נבחרה קבוצה: " + group);

        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putString("GROUP_ID", group.getId())
                .apply();
        Log.d(TAG, "שמור GROUP_ID=" + group.getId());

        // הוספת המשתמש כמינבר ב-/groups/{groupId}/members/{uid}
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();  // הוספתי
        groupsRef
                .child(group.getId())
                .child("members")
                .child(uid)
                .setValue(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User added to group members: " + uid);
                    } else {
                        Log.e(TAG, "Failed to add member: " + task.getException());
                    }
                });

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
