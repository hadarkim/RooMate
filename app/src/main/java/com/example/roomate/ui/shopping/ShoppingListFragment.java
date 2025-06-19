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

public class ShoppingListFragment extends Fragment {
    private ShoppingViewModel shopVM;
    private ShoppingAdapter adapter;
    private EditText etItem;
    private Button btnAdd;

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

        // 3. הגדר את ה-Adapter
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

        // 4. השג את ה-ViewModel
        shopVM = new ViewModelProvider(this)
                .get(ShoppingViewModel.class);

        // 5. הירשם ל-LiveData לעדכוני UI
        shopVM.getItems().observe(
                getViewLifecycleOwner(),
                list -> adapter.submitList(list)
        );

        // 6. טיפול בלחיצה על "הוסף"
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
