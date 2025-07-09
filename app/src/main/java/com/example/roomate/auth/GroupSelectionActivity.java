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
import androidx.appcompat.widget.Toolbar;


public class GroupSelectionActivity extends AppCompatActivity {
    private static final String TAG = "GroupSelectionAct";

    // רכיבי UI
    private RecyclerView rvGroups;
    private EditText etSearchGroup;
    private Button btnCreateGroup;
    private Button btnLogoutGroupSel;

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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("בחירת קבוצה");

        // 1. בדיקת Authentication
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // אם אין משתמש מחובר, נווט ל-Login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // 2. אתחול רכיבי UI


        rvGroups       = findViewById(R.id.rvGroups);
        etSearchGroup  = findViewById(R.id.etSearchGroup);
        btnCreateGroup = findViewById(R.id.btnCreateGroup);
        btnLogoutGroupSel = findViewById(R.id.btnLogoutGroupSel);

        // 3. חיבור ה-logout
        btnLogoutGroupSel.setOnClickListener(v -> {
            // א. הסרת GROUP_ID מה-SharedPreferences
            PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .remove("GROUP_ID")
                    .apply();
            // ב. התנתקות מ-Firebase
            FirebaseAuth.getInstance().signOut();
            // ג. ניווט ל-LoginActivity וסגירת Activity הנוכחי
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // 4. אתחול ה-Adapter לרשימת הקבוצות
        adapter = new GroupAdapter(this::onGroupClick);
        rvGroups.setLayoutManager(new LinearLayoutManager(this));
        rvGroups.setAdapter(adapter);

        // 5. אתחול ViewModel וצפייה ב-LiveData
        viewModel = new ViewModelProvider(this).get(GroupViewModel.class);
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

        // 6. סינון בזמן אמת לפי שדה החיפוש
        etSearchGroup.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                filterGroups(s.toString().trim());
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        // 7. יצירת קבוצה חדשה דרך ה-ViewModel
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
                        String newId = UUID.randomUUID().toString().substring(0, 8);
                        Log.d(TAG, "ViewModel: createGroup id=" + newId + " name=" + name);

                        Task<Void> task = viewModel.createGroup(
                                newId, name, currentUser.getUid());
                        task.addOnCompleteListener(t -> {
                            if (t.isSuccessful()) {
                                showCodeAndJoin(newId, name);
                            } else {
                                String msg = t.getException() != null
                                        ? t.getException().getMessage()
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

    // סינון הרשימה לפי query
    private void filterGroups(String query) {
        List<Group> filtered = new ArrayList<>();
        String lower = query.toLowerCase();
        for (Group g : fullGroupList) {
            if (g.getName() != null &&
                    g.getName().toLowerCase().contains(lower)) {
                filtered.add(g);
            }
        }
        adapter.submitList(filtered);
    }

    // לחיצה על קבוצה להצטרפות
    private void onGroupClick(Group group) {
        final EditText etCode = new EditText(this);
        new MaterialAlertDialogBuilder(this)
                .setTitle("קבוצה: " + group.getName())
                .setMessage("הזן את הקוד כדי להצטרף")
                .setView(etCode)
                .setPositiveButton("הצטרף", (dialog, which) -> {
                    if (etCode.getText().toString().trim().equals(group.getId())) {
                        joinGroup(group);
                    } else {
                        Toast.makeText(this, "קוד שגוי", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("ביטול", null)
                .show();
    }

    // הצגה של הקוד והצטרפות בפועל
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

    // הצטרפות לקבוצה ושמירה ב־DB + SharedPreferences
    private void joinGroup(@NonNull Group group) {
        String groupId = group.getId();
        String uid     = currentUser.getUid();

        // 1. שמירת GROUP_ID
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putString("GROUP_ID", groupId)
                .apply();

        // 2. עדכון /groups/{groupId}/members/{uid}
        Task<Void> taskJoin = viewModel.joinGroup(groupId, uid);
        taskJoin.addOnCompleteListener(t -> {
            if (t.isSuccessful()) {
                // 3. עדכון ב־/users/{uid}/groupId
                FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(uid)
                        .child("groupId")
                        .setValue(groupId)
                        .addOnCompleteListener(task2 -> {
                            // סיום → כניסה ל-MainActivity
                            startActivity(new Intent(GroupSelectionActivity.this,
                                    MainActivity.class));
                            finish();
                        });
            } else {
                // שגיאה: הסרת GROUP_ID
                PreferenceManager.getDefaultSharedPreferences(this)
                        .edit()
                        .remove("GROUP_ID")
                        .apply();
            }
        });
    }
}
