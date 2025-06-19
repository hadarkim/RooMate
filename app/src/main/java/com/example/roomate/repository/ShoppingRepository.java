package com.example.roomate.repository;

import android.util.Log;  // ← הוסף לוגינג
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.roomate.model.ShoppingItem;
import com.google.firebase.database.DataSnapshot;       // ← שינוי: שימוש ב–Realtime DB במקום Firestore
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ShoppingRepository {
    // ← שינוי: הפניה ל–Realtime Database במקום Firestore
    private final DatabaseReference shopRef;
    private final MutableLiveData<List<ShoppingItem>> itemsLive = new MutableLiveData<>();

    /**
     * קונסטרקטור חדש המקבל groupID,
     * כדי לבצע קריאה ל־/groups/{groupID}/shoppingItems
     */
    public ShoppingRepository(String groupID) {
        // ← שינוי מ–Firestore.collection("shoppingItems") ל–Realtime path:
        shopRef = FirebaseDatabase
                .getInstance()
                .getReference("groups")
                .child(groupID)
                .child("shoppingItems");

        // ← שינוי: השתמש ב–addValueEventListener במקום snapshotListener
        shopRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snap) {
                List<ShoppingItem> list = new ArrayList<>();
                for (DataSnapshot c : snap.getChildren()) {
                    ShoppingItem it = c.getValue(ShoppingItem.class);
                    if (it != null) {
                        it.setId(c.getKey());           // ← הוספת setId כדי לשמור את המפתח
                        list.add(it);
                    }
                }
                itemsLive.postValue(list);            // ← שימוש ב–postValue כדי לעדכן את LiveData
                Log.d("ShopRepo", "items=" + list.size());  // ← הוספת לוג כדי לוודא שהנתונים מגיעים
            }

            @Override
            public void onCancelled(DatabaseError err) {
                Log.e("ShopRepo", "onCancelled", err.toException());  // ← הוספת טיפול בשגיאה ולוג
            }
        });
    }

    /** מחזיר את ה־LiveData של כל הפריטים */
    public LiveData<List<ShoppingItem>> getItems() {
        return itemsLive;
    }

    /**
     * הוספת פריט חדש
     * ← שינוי: שימוש ב–setValue על child במקום Firestore.document(id).set(...)
     */
    public void addItem(ShoppingItem it) {
        shopRef.child(it.getId())
                .setValue(it)
                .addOnSuccessListener(v -> Log.d("ShopRepo", "added " + it.getName()))    // ← לוג הצלחה
                .addOnFailureListener(e -> Log.e("ShopRepo", "add failed", e));          // ← לוג שגיאה
    }

    /**
     * הפיכת סטטוס קנייה
     * ← שינוי: עדכון השדה "bought" ב–Realtime DB
     */
    public void toggleBought(ShoppingItem it) {
        shopRef.child(it.getId())
                .child("bought")
                .setValue(!it.isBought());  // הישתנות לערך ההפוך
    }

    /**
     * מחיקת פריט
     * ← שינוי: שימוש ב–removeValue() במקום Firestore.document(id).delete()
     */
    public void deleteItem(String id) {
        shopRef.child(id).removeValue();
    }
}
