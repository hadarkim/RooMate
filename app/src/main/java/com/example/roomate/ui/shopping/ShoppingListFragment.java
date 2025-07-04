package com.example.roomate.ui.shopping;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roomate.R;
import com.example.roomate.model.ShoppingItem;
import com.example.roomate.model.User;
import com.example.roomate.repository.UserRepository;

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
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        // 1. טען את ה-XML
        View v = inflater.inflate(
                R.layout.fragment_shopping_list,
                container,
                false
        );

        // 2. קבל הפניות ל-Views
        etItem = v.findViewById(R.id.etItem);
        btnAdd = v.findViewById(R.id.btnAdd);
        RecyclerView rv = v.findViewById(R.id.rvShop);

        // 3. אתחול ה-Adapter
        adapter = new ShoppingAdapter(new ShoppingAdapter.Listener() {
            @Override
            public void onToggle(ShoppingItem item) {
                shopVM.toggleBought(item);
            }
            @Override
            public void onDelete(ShoppingItem item) {
                shopVM.deleteItem(item);
            }
        });
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        // 4. אתחול ה-ViewModel
        shopVM = new ViewModelProvider(this)
                .get(ShoppingViewModel.class);

        // 5. אתחול ה-UserRepository
        userRepo = new UserRepository();

        // 6. הירשם ל-LiveData של פריטי הקנייה
        shopVM.getItems().observe(
                getViewLifecycleOwner(),
                list -> {
                    // קודם כל עדכון הרשימה ב-Adapter
                    adapter.submitList(list);

                    // איסוף כל assignedToUId כדי לטעון פרטי משתמש
                    List<String> uids = new ArrayList<>();
                    if (list != null) {
                        for (ShoppingItem it : list) {
                            String uid = it.getAssignedToUid();
                            if (uid != null && !uid.isEmpty()) {
                                uids.add(uid);
                            }
                        }
                    }
                    if (!uids.isEmpty()) {
                        // קריאה ל-UserRepository לקבלת User מלא לכל UID
                        userRepo.fetchUsersByIds(uids, usersList -> {
                            // בנה מפה של UID→User
                            Map<String, User> userMap = new HashMap<>();
                            for (User u : usersList) {
                                userMap.put(u.getId(), u);
                            }
                            // עדכן את ה-Adapter עם המפה
                            adapter.setUserMap(userMap);
                        });
                    } else {
                        // אין assignedToUId ברשימה: ננקה מפת משתמשים
                        adapter.setUserMap(Collections.emptyMap());
                    }
                }
        );

        // 7. טיפול בלחיצה על "הוסף"
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
