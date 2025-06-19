package com.example.roomate.ui.shopping;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.roomate.model.ShoppingItem;
import com.example.roomate.repository.ShoppingRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.UUID;

public class ShoppingViewModel extends AndroidViewModel {
    private final ShoppingRepository repo;

    public ShoppingViewModel(@NonNull Application application) {
        super(application);
        // 1. קבלת ה-groupID מ-SharedPreferences
        Context ctx = application.getApplicationContext();
        String groupID = ctx.getSharedPreferences("rooMatePrefs", Context.MODE_PRIVATE)
                .getString("CURRENT_GROUP_ID", "defaultGroup");
        // 2. אתחול ה-Repository עם groupID
        repo = new ShoppingRepository(groupID);
    }

    /** מחזיר את רשימת פריטי הקנייה */
    public LiveData<List<ShoppingItem>> getItems() {
        return repo.getItems();
    }

    /** יוצר פריט חדש ושולח ל-Repository */
    public void addItem(String name) {
        String uid = FirebaseAuth.getInstance().getUid();
        String id  = UUID.randomUUID().toString();
        ShoppingItem item = new ShoppingItem(id, name, uid);
        repo.addItem(item);
    }

    /** משנה את מצב ה-isBought של הפריט */
    public void toggleBought(ShoppingItem item) {
        repo.toggleBought(item);
    }

    /** מוחק פריט לפי ה-ID שלו */
    public void deleteItem(ShoppingItem item) {
        repo.deleteItem(item.getId());
    }
}
