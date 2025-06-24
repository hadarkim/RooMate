package com.example.roomate.ui.shopping;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roomate.R;
import com.example.roomate.model.ShoppingItem;
import com.example.roomate.model.User;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ShoppingAdapter
        extends ListAdapter<ShoppingItem, ShoppingAdapter.Holder> {

    public interface Listener {
        void onToggle(ShoppingItem item);
        void onDelete(ShoppingItem item);
    }

    private static final String TAG = "ShoppingAdapter";
    private final Listener listener;

    // מפת UID → User (שם בלבד). תעודכן מבחוץ דרך setUserMap(...)
    private Map<String, User> userMap = new HashMap<>();

    public ShoppingAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    /**
     * עדכון המפה של משתמשים (מתקבל מ־Activity/Fragment ע"י קריאה ל־UserRepository.fetchUsersByIds).
     * לאחר קריאה יש לקרוא notifyDataSetChanged() או submitList מחדש.
     */
    public void setUserMap(Map<String, User> map) {
        if (map == null) {
            this.userMap = Collections.emptyMap();
        } else {
            this.userMap = map;
        }
        // מרעננים כי שינוי ההקצאה משפיע על התצוגה
        notifyDataSetChanged();
    }

    static final DiffUtil.ItemCallback<ShoppingItem> DIFF =
            new DiffUtil.ItemCallback<ShoppingItem>() {
                @Override public boolean areItemsTheSame(
                        @NonNull ShoppingItem o1, @NonNull ShoppingItem o2) {
                    return o1.getId() != null && o1.getId().equals(o2.getId());
                }
                @Override public boolean areContentsTheSame(
                        @NonNull ShoppingItem o1, @NonNull ShoppingItem o2) {
                    // משווים name, isBought, assignedToUId
                    boolean sameName = o1.getName() != null
                            ? o1.getName().equals(o2.getName())
                            : o2.getName() == null;
                    boolean sameBought = o1.isBought() == o2.isBought();
                    boolean sameAssigned = o1.getAssignedToUid() != null
                            ? o1.getAssignedToUid().equals(o2.getAssignedToUid())
                            : o2.getAssignedToUid() == null;
                    boolean same = sameName && sameBought && sameAssigned;
                    Log.d(TAG, "areContentsTheSame? id=" + o1.getId()
                            + " sameName=" + sameName
                            + " sameBought=" + sameBought
                            + " sameAssigned=" + sameAssigned
                            + " -> " + same);
                    return same;
                }
            };

    @NonNull @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shopping, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int pos) {
        ShoppingItem it = getItem(pos);
        Log.d(TAG, "onBindViewHolder pos=" + pos
                + " id=" + it.getId()
                + " name=" + it.getName()
                + " bought=" + it.isBought()
                + " assignedTo=" + it.getAssignedToUid());

        // הצגת שם הפריט וסטטוס
        h.tvName.setText(it.getName());
        h.cbBought.setChecked(it.isBought());

        // הצגת מי מוקצה (שם בלבד, ללא תמונה):
        String assignedUid = it.getAssignedToUid();
        if (assignedUid != null && userMap.containsKey(assignedUid)) {
            User u = userMap.get(assignedUid);
            h.tvAssignedName.setText(u.getName());
        } else {
            h.tvAssignedName.setText("לא מוקצה");
        }

        // מאזיני לחיצה על CheckBox ו-delete
        h.cbBought.setOnClickListener(v -> listener.onToggle(it));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(it));
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView tvName;
        CheckBox cbBought;
        View btnDelete;
        TextView tvAssignedName;

        Holder(@NonNull View itemView) {
            super(itemView);
            tvName    = itemView.findViewById(R.id.tvName);
            cbBought  = itemView.findViewById(R.id.cbBought);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            // ודא שב-layout item_shopping.xml יש TextView עם id tvAssignedName
            tvAssignedName = itemView.findViewById(R.id.tvAssignedName);
        }
    }
}
