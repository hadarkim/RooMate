
package com.example.roomate.ui.shopping;

import android.app.Application;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.roomate.model.ShoppingItem;
import com.example.roomate.repository.ShoppingItemRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel לניהול פריטי קנייה בהתאם לגישה החדשה:
 * - משתמש ב־ShoppingItemRepository
 * - מושך groupId מ-SharedPreferences (DefaultPrefs, מפתח "GROUP_ID")
 * - יוצר/מעדכן/מוחק פריטים בעזרת CompletionListener
 */
public class ShoppingViewModel extends AndroidViewModel {
    private static final String TAG = "ShoppingViewModel";

    private final ShoppingItemRepository repo;
    private final String groupId;

    public ShoppingViewModel(@NonNull Application application) {
        super(application);
        // 1️⃣ קריאת groupId מ־Default SharedPreferences
        groupId = PreferenceManager.getDefaultSharedPreferences(application.getApplicationContext())
                .getString("GROUP_ID", null);
        if (groupId == null) {
            Log.w(TAG, "No GROUP_ID found in SharedPreferences");
            // אפשר לשלוח אירוע או להנחות ה-UI לטפל (למשל ניווט חזרה)
        } else {
            Log.d(TAG, "Loaded GROUP_ID=" + groupId);
        }
        // 2️⃣ אתחול ה־Repository
        repo = new ShoppingItemRepository();
    }

    /**
     * מחזיר LiveData של כל פריטי הקנייה בקבוצה הנוכחית.
     * אם groupId=null, מחזיר LiveData ריקה עם רשימה ריקה.
     */
    public LiveData<List<ShoppingItem>> getItems() {
        if (groupId == null) {
            Log.w(TAG, "getItems: groupId is null, returning empty LiveData");
            MutableLiveData<List<ShoppingItem>> empty = new MutableLiveData<>();
            empty.setValue(new ArrayList<>());
            return empty;
        }
        return repo.getItemsForGroup(groupId);
    }

    /**
     * יוצר פריט חדש בשם הנתון, ומקצה אותו למשתמש הנוכחי (UID).
     * משתמש ב־CompletionListener פנימי ללוג בלבד.
     *
     * @param name שם הפריט
     */
    public void addItem(String name) {
        if (groupId == null) {
            Log.e(TAG, "addItem: cannot add item because groupId is null");
            return;
        }
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Log.e(TAG, "addItem: user not authenticated");
            return;
        }
        ShoppingItem item = new ShoppingItem();
        item.setName(name);
        item.setAssignedToUid(uid);
        item.setBought(false);

        repo.createItem(groupId, item, (error, ref) -> {
            if (error == null) {
                Log.d(TAG, "addItem succeeded id=" + item.getId());
            } else {
                Log.e(TAG, "addItem failed", error.toException());
            }
        });
    }

    /**
     * משנה את מצב ה-isBought של הפריט.
     *
     * @param item ה־ShoppingItem עם id תקין ושדה isBought הנוכחי
     */
    public void toggleBought(ShoppingItem item) {
        if (groupId == null) {
            Log.e(TAG, "toggleBought: groupId is null");
            return;
        }
        if (item.getId() == null) {
            Log.e(TAG, "toggleBought: item.id is null");
            return;
        }
        // הפיכת הערך
        boolean newBought = !item.isBought();
        item.setBought(newBought);

        // קריאה ל־updateItem ב־Repository
        repo.updateItem(groupId, item, (error, ref) -> {
            if (error == null) {
                Log.d(TAG, "toggleBought succeeded id=" + item.getId());
            } else {
                Log.e(TAG, "toggleBought failed id=" + item.getId(), error.toException());
            }
        });
    }

    /**
     * מוחק פריט לפי ה-ID שלו.
     *
     * @param item ה־ShoppingItem עם id תקין
     */
    public void deleteItem(ShoppingItem item) {
        if (groupId == null) {
            Log.e(TAG, "deleteItem: groupId is null");
            return;
        }
        String itemId = item.getId();
        if (itemId == null) {
            Log.e(TAG, "deleteItem: item.id is null");
            return;
        }
        repo.deleteItem(groupId, itemId, (error, ref) -> {
            if (error == null) {
                Log.d(TAG, "deleteItem succeeded id=" + itemId);
            } else {
                Log.e(TAG, "deleteItem failed id=" + itemId, error.toException());
            }
        });
    }
}
