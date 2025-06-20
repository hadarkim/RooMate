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

    /**
     * @param onGroupClick callback שיקבל את ה-Group שנבחר (למשל, שמירת GROUP_ID וניווט)
     */
    public GroupAdapter(Consumer<Group> onGroupClick) {
        super(DIFF_CALLBACK);
        this.onGroupClick = onGroupClick;
    }

    private static final DiffUtil.ItemCallback<Group> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Group>() {
                @Override
                public boolean areItemsTheSame(@NonNull Group oldItem, @NonNull Group newItem) {
                    // בודקים אם זה אותו group לפי id
                    String oldId = oldItem.getId();
                    String newId = newItem.getId();
                    boolean same = oldId != null && oldId.equals(newId);
                    Log.d(TAG, "areItemsTheSame? oldId=" + oldId + " newId=" + newId + " -> " + same);
                    return same;
                }

                @Override
                public boolean areContentsTheSame(@NonNull Group oldItem, @NonNull Group newItem) {
                    // השוואה לפי equals שהגדרנו במודל (בדיקת name וכו')
                    boolean sameContent = oldItem.equals(newItem);
                    Log.d(TAG, "areContentsTheSame? old=" + oldItem + " new=" + newItem + " -> " + sameContent);
                    return sameContent;
                }
            };

    @NonNull
    @Override
    public GroupHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // כאן משנים ל-inflate של layout מותאם
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group, parent, false); // שימוש ב-item_group.xml שהגדרת
        return new GroupHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupHolder holder, int position) {
        Group group = getItem(position);
        Log.d(TAG, "onBindViewHolder: position=" + position + ", group=" + group);

        // שימוש ב-TextView מתוך item_group.xml
        String displayName = group.getName() != null ? group.getName() : group.getId();
        holder.tvName.setText(displayName);
        Log.d(TAG, "Bound group name: " + displayName);

        // אם יש לך ImageView ב-item_group.xml להצגה, אפשר לטעון כאן:
        // holder.ivIcon.setImageResource(R.drawable.ic_group);

        holder.itemView.setOnClickListener(v -> {
            Log.d(TAG, "Clicked group: " + group);
            onGroupClick.accept(group);
        });
    }

    static class GroupHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView ivIcon; // במידה ויש לך ImageView ב-item_group.xml

        public GroupHolder(@NonNull View itemView) {
            super(itemView);
            // ודא שה-id של ה-TextView ב-item_group.xml הוא tvGroupName
            tvName = itemView.findViewById(R.id.tvGroupName);
            // אם הוספת ImageView ב-item_group.xml, בטל את ההערה ושנה את ה-id במקרה הצורך:
            // ivIcon = itemView.findViewById(R.id.ivGroupIcon);
        }
    }
}
