
package com.example.roomate.ui.group;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roomate.R;
import com.example.roomate.model.Group;

import java.util.function.Consumer;

public class GroupAdapter extends ListAdapter<Group, GroupAdapter.GroupHolder> {
    private static final String TAG = "GroupAdapter";
    private final Consumer<Group> onGroupClick;

    public GroupAdapter(Consumer<Group> onGroupClick) {
        super(DIFF_CALLBACK);
        this.onGroupClick = onGroupClick;
    }

    private static final DiffUtil.ItemCallback<Group> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Group>() {
                @Override
                public boolean areItemsTheSame(@NonNull Group oldItem, @NonNull Group newItem) {
                    boolean same = oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
                    Log.d(TAG, "areItemsTheSame? " + same);
                    return same;
                }
                @Override
                public boolean areContentsTheSame(@NonNull Group oldItem, @NonNull Group newItem) {
                    boolean sameContent = oldItem.equals(newItem);
                    Log.d(TAG, "areContentsTheSame? " + sameContent);
                    return sameContent;
                }
            };

    @NonNull
    @Override
    public GroupHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group, parent, false);
        return new GroupHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupHolder holder, int position) {
        Group group = getItem(position);
        Log.d(TAG, "onBindViewHolder: position=" + position + ", group=" + group);

        String displayName = group.getName() != null ? group.getName() : group.getId();
        holder.tvName.setText(displayName);
        Log.d(TAG, "Bound group name: " + displayName);

        holder.itemView.setOnClickListener(v -> {
            Log.d(TAG, "Clicked group: " + group);
            onGroupClick.accept(group);
        });
    }

    static class GroupHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView ivIcon;
        public GroupHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvGroupName);
            // אם יש לך ImageView להצגה:
            // ivIcon = itemView.findViewById(R.id.ivGroupIcon);
        }
    }
}
