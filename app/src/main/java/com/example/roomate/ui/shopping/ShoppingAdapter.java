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
        void onToggle(@NonNull ShoppingItem item);
        void onDelete(@NonNull ShoppingItem item);
    }
    private static final String TAG = "ShoppingAdapter";
    private final Listener listener;

    private Map<String, User> userMap = new HashMap<>();
    // שם fallback אם אין assignedToUid
    private String defaultCreatorName = "";
    public ShoppingAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
        setHasStableIds(true);
    }
    @Override
    public long getItemId(int position) {
        return getItem(position).getId().hashCode();
    }
    public void setUserMap(Map<String, User> map) {
        this.userMap = map != null ? map : Collections.emptyMap();
        notifyDataSetChanged();
    }
    public void setDefaultCreatorName(String name) {
        this.defaultCreatorName = name != null ? name : "";
        notifyDataSetChanged();
    }
    @NonNull @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shopping, parent, false);
        return new Holder(v);
    }
    @Override
    public void onBindViewHolder(@NonNull Holder h, int pos) {
        ShoppingItem it = getItem(pos);
        Log.d(TAG, "onBindViewHolder pos=" + pos + " bought=" + it.isBought());
        h.cbBought.setOnCheckedChangeListener(null);
        h.tvName.setText(it.getName());
        h.cbBought.setChecked(it.isBought());
        h.cbBought.setOnCheckedChangeListener((buttonView, isChecked) ->
                listener.onToggle(it)
        );
        String uid = it.getAssignedToUid();
        if (uid != null && userMap.containsKey(uid)) {
            h.tvAssignedName.setText(userMap.get(uid).getName());
        } else {
            h.tvAssignedName.setText(defaultCreatorName);
        }
        h.btnDelete.setOnClickListener(v -> listener.onDelete(it));
    }
    static class Holder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final CheckBox cbBought;
        final View btnDelete;
        final TextView tvAssignedName;
        Holder(@NonNull View itemView) {
            super(itemView);
            tvName         = itemView.findViewById(R.id.tvName);
            cbBought       = itemView.findViewById(R.id.cbBought);
            btnDelete      = itemView.findViewById(R.id.btnDelete);
            tvAssignedName = itemView.findViewById(R.id.tvAssignedName);
        }
    }
    private static final DiffUtil.ItemCallback<ShoppingItem> DIFF =
            new DiffUtil.ItemCallback<ShoppingItem>() {
                @Override public boolean areItemsTheSame(
                        @NonNull ShoppingItem o1, @NonNull ShoppingItem o2) {
                    return o1.getId().equals(o2.getId());
                }
                @Override public boolean areContentsTheSame(
                        @NonNull ShoppingItem o1, @NonNull ShoppingItem o2) {
                    boolean sameName     = o1.getName().equals(o2.getName());
                    boolean sameBought   = o1.isBought() == o2.isBought();
                    boolean sameAssigned =
                            (o1.getAssignedToUid() == null && o2.getAssignedToUid() == null)
                                    || (o1.getAssignedToUid() != null
                                    && o1.getAssignedToUid().equals(o2.getAssignedToUid()));
                    return sameName && sameBought && sameAssigned;
                }
            };
}
