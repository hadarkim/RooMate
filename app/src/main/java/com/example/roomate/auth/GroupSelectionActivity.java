package com.example.roomate.auth;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roomate.R;
import com.example.roomate.model.Group;
import com.example.roomate.ui.group.GroupAdapter;
import com.example.roomate.ui.main.MainActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GroupSelectionActivity extends AppCompatActivity {
    private static final String TAG = "GroupSelectionAct";

    private RecyclerView      rvGroups;
    private EditText          etSearchGroup;
    private Button            btnCreateGroup;
    private DatabaseReference groupsRef;
    private GroupAdapter      adapter;
    private final List<Group> fullGroupList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called");
        setContentView(R.layout.activity_group_selection);



        // 0️⃣ בדיקת Authentication: אם אין משתמש מאומת, נשלח ל-LoginActivity
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "No authenticated user; redirecting to LoginActivity");
            startActivity(new Intent(this, com.example.roomate.auth.LoginActivity.class));
            finish();
            return;
        }

        // 1️⃣ אתחול ה־Views
        rvGroups       = findViewById(R.id.rvGroups);
        etSearchGroup  = findViewById(R.id.etSearchGroup);
        btnCreateGroup = findViewById(R.id.btnCreateGroup);

        // 2️⃣ הגדרת RecyclerView + Adapter
        adapter = new GroupAdapter(this::onGroupClick);
        rvGroups.setLayoutManager(new LinearLayoutManager(this));
        rvGroups.setAdapter(adapter);

        // 3️⃣ קישור ל־Firebase על ענף "groups"
        groupsRef = FirebaseDatabase.getInstance().getReference("groups");

        // 4️⃣ טעינת כל הקבוצות אל fullGroupList
        Log.d(TAG, "Setting up groupsRef listener");
        groupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                fullGroupList.clear();
                for (DataSnapshot child : snap.getChildren()) {
                    Group g = child.getValue(Group.class);
                    if (g != null) {
                        g.setId(child.getKey());
                        fullGroupList.add(g);
                    }
                }
                adapter.submitList(new ArrayList<>(fullGroupList));
                Log.d(TAG, "Loaded groups: " + fullGroupList.size());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError err) {
                Log.e(TAG, "Error loading groups", err.toException());
                Toast.makeText(GroupSelectionActivity.this,
                        "שגיאה בטעינת קבוצות: " + err.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });

        // 5️⃣ סינון בזמן אמת לפי שדה החיפוש
        etSearchGroup.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                filterGroups(s.toString().trim());
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        // 6️⃣ לחצן ליצירת קבוצה חדשה
        btnCreateGroup.setOnClickListener(v -> {
            Log.d(TAG, "btnCreateGroup clicked");
            final EditText etName = new EditText(this);
            new MaterialAlertDialogBuilder(this)
                    .setTitle("צור קבוצה חדשה")
                    .setView(etName)
                    .setPositiveButton("צור", (dialog, which) -> {
                        Log.d(TAG, "Dialog positive button clicked");
                        String name = etName.getText().toString().trim();
                        if (name.isEmpty()) {
                            Toast.makeText(this, "יש להזין שם קבוצה", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Group name empty—aborting create");
                            return;
                        }
                        String newId = UUID.randomUUID().toString().substring(0, 8);
                        Log.d(TAG, "Creating group with ID=" + newId + " name=" + name);
                        groupsRef.child(newId).child("name")
                                .setValue(name)
                                .addOnCompleteListener(task -> {
                                    Log.d(TAG, "DB callback invoked, success=" + task.isSuccessful()
                                            + ", exception=" + task.getException());
                                    if (task.isSuccessful()) {
                                        // אפשר לאחסן גם את השם תחת מפתח נוסף, למשל "displayName", במידת הצורך
                                        View codeView = getLayoutInflater()
                                                .inflate(R.layout.dialog_group_auth, null);
                                        TextInputEditText etDialogCode =
                                                codeView.findViewById(R.id.etDialogCode);
                                        etDialogCode.setText(newId);
                                        etDialogCode.setEnabled(false);
                                        new MaterialAlertDialogBuilder(this)
                                                .setTitle("קוד הקבוצה שלך")
                                                .setView(codeView)
                                                .setPositiveButton("הבנתי", (d, w) -> {
                                                    Log.d(TAG, "User acknowledged new group code");
                                                    selectGroup(new Group(newId, name));
                                                })
                                                .show();
                                    } else {
                                        Exception ex = task.getException();
                                        String msg = ex != null ? ex.getMessage() : "Unknown error";
                                        Toast.makeText(this,
                                                "שגיאה ביצירת קבוצה: " + msg,
                                                Toast.LENGTH_SHORT).show();
                                        Log.e(TAG, "Failed to create group", ex);
                                    }
                                });
                    })
                    .setNegativeButton("ביטול", null)
                    .show();
        });
    }

    // סינון הרשימה לפי השם
    private void filterGroups(String query) {
        List<Group> filtered = new ArrayList<>();
        String lower = query.toLowerCase();
        for (Group g : fullGroupList) {
            if (g.getName() != null && g.getName().toLowerCase().contains(lower)) {
                filtered.add(g);
            }
        }
        adapter.submitList(filtered);
        Log.d(TAG, "filterGroups(\"" + query + "\") → " + filtered.size() + " results");
    }

    // לחיצה על שם קבוצה ברשימה → דיאלוג לאימות ה-ID
    private void onGroupClick(Group group) {
        Log.d(TAG, "Clicked group: " + group.getName() + " id=" + group.getId());
        final EditText etCode = new EditText(this);
        new MaterialAlertDialogBuilder(this)
                .setTitle("קבוצה: " + group.getName())
                .setMessage("הזן את הקוד כדי להצטרף")
                .setView(etCode)
                .setPositiveButton("הצטרף", (dialog, which) -> {
                    String code = etCode.getText().toString().trim();
                    Log.d(TAG, "Attempt join with code=" + code);
                    if (code.equals(group.getId())) {
                        selectGroup(group);
                    } else {
                        Toast.makeText(this, "קוד שגוי", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Join failed—wrong code");
                    }
                })
                .setNegativeButton("ביטול", null)
                .show();
    }

    // שמירת GROUP_ID, הוספת המשתמש כ-member וניווט
    private void selectGroup(@NonNull Group group) {
        Log.d(TAG, "Selecting group id=" + group.getId());
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putString("GROUP_ID", group.getId())
                .apply();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        groupsRef.child(group.getId())
                .child("members")
                .child(uid)
                .setValue(true);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}





