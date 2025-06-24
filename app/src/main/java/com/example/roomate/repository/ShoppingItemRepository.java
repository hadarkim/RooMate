// com/example/roomate/repository/ShoppingItemRepository.java
package com.example.roomate.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.roomate.model.ShoppingItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * אחראי על גישה ל־shoppingItems ב־Realtime Database תחת /groups/{groupId}/shoppingItems
 */
public class ShoppingItemRepository {
    private static final String TAG = "ShoppingItemRepo";

    // מצביע לשורש /groups
    private final DatabaseReference groupsRef =
            FirebaseDatabase.getInstance()
                    .getReference("groups");

    /**
     * מחזיר LiveData של רשימת ShoppingItem עבור קבוצה נתונה.
     * מאזין לשינויים ב-/groups/{groupId}/shoppingItems.
     *
     * @param groupId מזהה הקבוצה
     */
    public LiveData<List<ShoppingItem>> getItemsForGroup(String groupId) {
        MutableLiveData<List<ShoppingItem>> live = new MutableLiveData<>();

        DatabaseReference itemsRef = groupsRef
                .child(groupId)
                .child("shoppingItems");

        itemsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ShoppingItem> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    ShoppingItem item = child.getValue(ShoppingItem.class);
                    if (item != null) {
                        // ודא שהתבצע setId בתוך ה-model
                        item.setId(child.getKey());
                        list.add(item);
                    }
                }
                Log.d(TAG, "Loaded shopping items for group " + groupId + ", size=" + list.size());
                live.postValue(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load shopping items for group " + groupId, error.toException());
            }
        });

        return live;
    }

    /**
     * יוצר פריט חדש באופן אטומי תחת הקבוצה.
     * אם item.getId() ריק או null, ייווצר id אוטומטי.
     *
     * @param groupId מזהה הקבוצה
     * @param item    אובייקט ShoppingItem; יש להגדיר שם (name) ושדה assignedToUId (יכול להיות null) ו־isBought (לרוב false)
     * @param cb      CompletionListener לקבלת הצלחה/כישלון
     */
    public void createItem(
            @NonNull String groupId,
            @NonNull ShoppingItem item,
            @NonNull DatabaseReference.CompletionListener cb
    ) {
        // וידוא id
        String itemId = item.getId();
        if (itemId == null || itemId.isEmpty()) {
            itemId = UUID.randomUUID().toString().substring(0, 8);
        }
        item.setId(itemId);

        DatabaseReference ref = groupsRef
                .child(groupId)
                .child("shoppingItems")
                .child(itemId);

        // אם רוצים לכתוב את כל האובייקט כולל id:
        // Log.d(TAG, "Creating item with full object: id=" + itemId);
        // ref.setValue(item, cb);

        // אחרת, כתיבה של שדות בלבד (ללא שדה id בתוך ה־node):
        Map<String, Object> data = new HashMap<>();
        data.put("name", item.getName());
        data.put("assignedToUId", item.getAssignedToUid());
        data.put("isBought", item.isBought());
        Log.d(TAG, "Creating shopping item id=" + itemId + " in group " + groupId);
        ref.setValue(data, cb);
    }

    /**
     * מעדכן פריט קיים.
     * מעדכן רק את השדות: name, assignedToUId, isBought.
     *
     * @param groupId מזהה הקבוצה
     * @param item    אובייקט ShoppingItem עם id תקין
     * @param cb      CompletionListener לקבלת הצלחה/כישלון
     */
    public void updateItem(
            @NonNull String groupId,
            @NonNull ShoppingItem item,
            @NonNull DatabaseReference.CompletionListener cb
    ) {
        String itemId = item.getId();
        if (itemId == null || itemId.isEmpty()) {
            Log.e(TAG, "updateItem: item.id is null or empty");
            // אפשר לקרוא cb.onComplete עם שגיאה מדומה, או פשוט להפסיק כאן
            return;
        }

        DatabaseReference ref = groupsRef
                .child(groupId)
                .child("shoppingItems")
                .child(itemId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", item.getName());
        updates.put("assignedToUId", item.getAssignedToUid());
        updates.put("isBought", item.isBought());
        Log.d(TAG, "Updating shopping item id=" + itemId + " in group " + groupId);
        ref.updateChildren(updates, cb);
    }

    /**
     * הפיכת סטטוס הקנייה (toggle): משתמש ב־updateItem פנימי.
     *
     * @param groupId מזהה הקבוצה
     * @param item    ShoppingItem עם id תקין; ה־isBought שלו הוא הסטטוס הנוכחי
     * @param cb      CompletionListener לקבלת הצלחה/כישלון
     */
    public void toggleBought(
            @NonNull String groupId,
            @NonNull ShoppingItem item,
            @NonNull DatabaseReference.CompletionListener cb
    ) {
        // הפיכת הסטטוס:
        boolean newBought = !item.isBought();
        item.setBought(newBought);
        Log.d(TAG, "Toggling isBought for item id=" + item.getId() + " to " + newBought);
        updateItem(groupId, item, cb);
    }

    /**
     * מוחק פריט לפי id.
     *
     * @param groupId מזהה הקבוצה
     * @param itemId  המזהה של הפריט
     * @param cb      CompletionListener לקבלת הצלחה/כישלון
     */
    public void deleteItem(
            @NonNull String groupId,
            @NonNull String itemId,
            @NonNull DatabaseReference.CompletionListener cb
    ) {
        DatabaseReference ref = groupsRef
                .child(groupId)
                .child("shoppingItems")
                .child(itemId);
        Log.d(TAG, "Deleting shopping item id=" + itemId + " from group " + groupId);
        ref.removeValue(cb);
    }
}
