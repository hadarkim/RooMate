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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roomate.R;
import com.example.roomate.model.Group;
import com.example.roomate.ui.group.GroupAdapter;
import com.example.roomate.ui.main.MainActivity;
import com.example.roomate.viewmodel.GroupViewModel;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Activity להצגת רשימת קבוצות קיימות, יצירה והצטרפות לקבוצה.
 * לאחר יצירה או הצטרפות, נשמר /groups/{groupId}/members/{uid}=true,
 * וגם /users/{uid}/groupId={groupId}, ולבסוף SharedPreferences["GROUP_ID"]={groupId},
 * ואז ניווט ל-MainActivity.
 */
public class GroupSelectionActivity extends AppCompatActivity {
    private static final String TAG = "GroupSelectionAct";

    // רכיבי UI
    private RecyclerView rvGroups;
    private EditText etSearchGroup;
    private Button btnCreateGroup;

    // Adapter ו־List לשמירת כל הקבוצות
    private GroupAdapter adapter;
    private final List<Group> fullGroupList = new ArrayList<>();

    // ViewModel
    private GroupViewModel viewModel;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_selection);

        // בדיקת Authentication
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // אם אין משתמש מחובר, נווט ל-Login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // אתחול רכיבי UI
        rvGroups = findViewById(R.id.rvGroups);
        etSearchGroup = findViewById(R.id.etSearchGroup);
        btnCreateGroup = findViewById(R.id.btnCreateGroup);

        // אתחול ה-Adapter
        adapter = new GroupAdapter(this::onGroupClick);
        rvGroups.setLayoutManager(new LinearLayoutManager(this));
        rvGroups.setAdapter(adapter);

        // אתחול ViewModel
        viewModel = new ViewModelProvider(this).get(GroupViewModel.class);

        // לצפות ב-LiveData של כל הקבוצות
        viewModel.getGroups().observe(this, new Observer<List<Group>>() {
            @Override
            public void onChanged(List<Group> groups) {
                fullGroupList.clear();
                if (groups != null) {
                    fullGroupList.addAll(groups);
                    adapter.submitList(new ArrayList<>(groups));
                    Log.d(TAG, "Loaded groups: " + groups.size());
                } else {
                    adapter.submitList(new ArrayList<>());
                    Log.d(TAG, "Loaded groups: null or empty");
                }
            }
        });

        // סינון בזמן אמת לפי שדה החיפוש
        etSearchGroup.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                filterGroups(s.toString().trim());
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        // יצירת קבוצה חדשה דרך ה-ViewModel
        btnCreateGroup.setOnClickListener(v -> {
            final EditText etName = new EditText(this);
            new MaterialAlertDialogBuilder(this)
                    .setTitle("צור קבוצה חדשה")
                    .setView(etName)
                    .setPositiveButton("צור", (dialog, which) -> {
                        String name = etName.getText().toString().trim();
                        if (name.isEmpty()) {
                            Toast.makeText(this, "יש להזין שם קבוצה", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // ייצור מזהה ייחודי קצר
                        String newId = UUID.randomUUID().toString().substring(0, 8);
                        Log.d(TAG, "ViewModel: createGroup id=" + newId + " name=" + name);

                        // קריאה ליבוא ה-ViewModel.createGroup
                        Task<Void> task = viewModel.createGroup(newId, name, currentUser.getUid());
                        task.addOnCompleteListener(t -> {
                            if (t.isSuccessful()) {
                                Log.d(TAG, "Group created successfully via ViewModel");
                                // לאחר יצירת הקבוצה, מציג את הקוד ומצטרף אוטומטית
                                showCodeAndJoin(newId, name);
                            } else {
                                String msg = t.getException() != null  ?
                                        t.getException().getMessage()
                                        : "Unknown error";
                                Toast.makeText(this,
                                        "שגיאה ביצירת קבוצה: " + msg,
                                        Toast.LENGTH_LONG).show();
                                Log.e(TAG, "Error creating group", t.getException());
                            }
                        });
                    })
                    .setNegativeButton("ביטול", null)
                    .show();
        });
    }

    // סינון הרשימה לפי מחרוזת
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

    // לחיצה על קבוצה להצטרפות
    private void onGroupClick(Group group) {
        final EditText etCode = new EditText(this);
        new MaterialAlertDialogBuilder(this)
                .setTitle("קבוצה: " + group.getName())
                .setMessage("הזן את הקוד כדי להצטרף")
                .setView(etCode)
                .setPositiveButton("הצטרף", (dialog, which) -> {
                    String code = etCode.getText().toString().trim();
                    if (code.equals(group.getId())) {
                        joinGroup(group);
                    } else {
                        Toast.makeText(this, "קוד שגוי", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("ביטול", null)
                .show();
    }

    // הצגת הקוד למשתמש ואחר כך קריאה ל-joinGroup
    private void showCodeAndJoin(String id, String name) {
        View codeView = getLayoutInflater()
                .inflate(R.layout.dialog_group_auth, null);
        TextInputEditText etDialogCode =
                codeView.findViewById(R.id.etDialogCode);
        etDialogCode.setText(id);
        etDialogCode.setEnabled(false);

        new MaterialAlertDialogBuilder(this)
                .setTitle("קוד הקבוצה שלך")
                .setView(codeView)
                .setPositiveButton("הבנתי", (d, w) ->
                        joinGroup(new Group(id, name))
                )
                .show();
    }

    // הצטרפות לקבוצה: שמירה ב-SharedPreferences, ב-/groups/{id}/members, וב-/users/{uid}/groupId, ואז ניווט ל-MainActivity
    private void joinGroup(@NonNull Group group) {
        String groupId = group.getId();
        String uid = currentUser.getUid();

        // 1. שמירת ב-SharedPreferences
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putString("GROUP_ID", groupId)
                .apply();

        // 2. כתיבה ל-/groups/{groupId}/members/{uid}=true
        Task<Void> taskJoin = viewModel.joinGroup(groupId, uid);
        taskJoin.addOnCompleteListener(t -> {
            if (t.isSuccessful()) {
                Log.d(TAG, "Joined group successfully via ViewModel");

                // 3. שמירת field groupId ב-/users/{uid}/groupId
                FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(uid)
                        .child("groupId")
                        .setValue(groupId)
                        .addOnCompleteListener(task2 -> {
                            if (task2.isSuccessful()) {
                                Log.d(TAG, "Saved groupId under user node");
                            } else {
                                Log.w(TAG, "Failed saving groupId under user node: "
                                        + task2.getException());
                            }
                            // 4. ניווט ל-MainActivity בסוף
                            startActivity(new Intent(GroupSelectionActivity.this, MainActivity.class));
                            finish();
                        });
            } else {
                String msg = t.getException() != null
                        ? t.getException().getMessage()
                        : "Unknown error";
                Toast.makeText(this,
                        "שגיאה בהצטרפות: " + msg,
                        Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error joining group", t.getException());

                // במידה וכשלנו בהצטרפות, מומלץ להסיר SharedPreferences כדי לא להשאיר ערך שגוי
                PreferenceManager.getDefaultSharedPreferences(this)
                        .edit()
                        .remove("GROUP_ID")
                        .apply();
            }
        });
    }
}
