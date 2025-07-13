package com.example.roomate.ui.shopping;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roomate.R;
import com.example.roomate.model.ShoppingItem;
import com.example.roomate.model.User;
import com.example.roomate.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShoppingListFragment extends Fragment {

    private ShoppingViewModel shopVM;
    private ShoppingAdapter adapter;
    private EditText etItem;
    private Button btnAdd;
    private UserRepository userRepo;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        // טען את ה־layout של הפרגמנט
        View v = inflater.inflate(R.layout.fragment_shopping_list, container, false);

        // אתחול ה־Views
        etItem = v.findViewById(R.id.etItem);
        btnAdd = v.findViewById(R.id.btnAdd);
        RecyclerView rv = v.findViewById(R.id.rvShop);

        // שלב 3: איתחול ה־Adapter ו־RecyclerView
        adapter = new ShoppingAdapter(new ShoppingAdapter.Listener() {
            @Override public void onToggle(ShoppingItem item) {
                shopVM.toggleBought(item);
            }
            @Override public void onDelete(ShoppingItem item) {
                shopVM.deleteItem(item);
            }
        });
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        // שלב 4: איתחול ה־ViewModel
        shopVM = new ViewModelProvider(this).get(ShoppingViewModel.class);

        // שלב 5: איתחול ה־UserRepository
        userRepo = new UserRepository();

        // שלב 5א: הגדרת השם של היוצר (המשתמש המחובר) כברירת מחדל
        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        if (me != null) {
            userRepo.getUserById(me.getUid())
                    .observe(getViewLifecycleOwner(), user -> {
                        if (user != null && user.getName() != null) {
                            adapter.setDefaultCreatorName(user.getName());
                        }
                    });
        }

        // שלב 6: התבוננות בנתוני פריטי הקנייה
        shopVM.getItems().observe(getViewLifecycleOwner(), list -> {
            // עדכון הרשימה ב־Adapter
            adapter.submitList(list);

            // איסוף UIDs עבור assignedToUid
            List<String> uids = new ArrayList<>();
            if (list != null) {
                for (ShoppingItem it : list) {
                    String uid = it.getAssignedToUid();
                    if (uid != null && !uid.isEmpty()) {
                        uids.add(uid);
                    }
                }
            }

            // טעינת פרטי המשתמשים ועדכון ה־Adapter
            if (!uids.isEmpty()) {
                userRepo.fetchUsersByIds(uids, usersList -> {
                    Map<String, User> userMap = new HashMap<>();
                    for (User u : usersList) {
                        userMap.put(u.getUid(), u);
                    }
                    adapter.setUserMap(userMap);
                });
            } else {
                // אין assignedToUid: ננקה את מפת המשתמשים
                adapter.setUserMap(Collections.emptyMap());
            }
        });

        // שלב 7: טיפול בלחיצה על כפתור הוספה
        btnAdd.setOnClickListener(view -> {
            String txt = etItem.getText().toString().trim();
            if (!txt.isEmpty()) {
                shopVM.addItem(txt);
                etItem.setText("");
            }
        });

        return v;
    }
}
