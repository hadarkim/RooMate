package com.example.roomate.ui.shopping;

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

public class ShoppingAdapter
        extends ListAdapter<ShoppingItem, ShoppingAdapter.Holder> {

    public interface Listener {
        void onToggle(ShoppingItem item);
        void onDelete(ShoppingItem item);
    }

    private final Listener listener;

    public ShoppingAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    static final DiffUtil.ItemCallback<ShoppingItem> DIFF =
            new DiffUtil.ItemCallback<ShoppingItem>() {
                @Override public boolean areItemsTheSame(
                        @NonNull ShoppingItem o1, @NonNull ShoppingItem o2) {
                    return o1.getId().equals(o2.getId());
                }
                @Override public boolean areContentsTheSame(
                        @NonNull ShoppingItem o1, @NonNull ShoppingItem o2) {
                    return o1.isBought() == o2.isBought()
                            && o1.getName().equals(o2.getName());
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
        h.tvName.setText(it.getName());
        h.cbBought.setChecked(it.isBought());
        h.cbBought.setOnClickListener(v -> listener.onToggle(it));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(it));
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView tvName;
        CheckBox cbBought;
        View btnDelete;
        Holder(@NonNull View itemView) {
            super(itemView);
            tvName    = itemView.findViewById(R.id.tvName);
            cbBought  = itemView.findViewById(R.id.cbBought);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
