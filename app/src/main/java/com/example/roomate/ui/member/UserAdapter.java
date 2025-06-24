// com/example/roomate/ui/member/UserAdapter.java

package com.example.roomate.ui.member;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.roomate.R;
import com.example.roomate.model.User;

/**
 * Adapter להצגת רשימת משתמשים (User) ב-RecyclerView.
 * מציג את השם, האימייל ותמונת אבטר אם קיימת (avatarUrl), אחרת drawable ברירת מחדל.
 * תומך באפקט ripple על לחיצה כאשר layout מוגדר כ-clickable עם foreground.
 * בנוסף, ניתן להעביר OnUserClickListener כדי לקבל callback על לחיצה על פריט.
 */
public class UserAdapter extends ListAdapter<User, UserAdapter.UserHolder> {

    /**
     * Listener ללחיצה על פריט משתמש.
     */
    public interface OnUserClickListener {
        void onUserClick(@NonNull User user);
    }

    private final OnUserClickListener onUserClickListener;

    /**
     * Constructor ללא לחיצה: אם לא צריך תגובה ללחיצה, השתמש בו.
     */
    public UserAdapter() {
        super(DIFF_CALLBACK);
        this.onUserClickListener = null;
    }

    /**
     * Constructor עם לחיצה: מספק callback כאשר לוחצים על משתמש ברשימה.
     * @param listener האובייקט שיקבל onUserClick עם המשתמש שנבחר
     */
    public UserAdapter(@NonNull OnUserClickListener listener) {
        super(DIFF_CALLBACK);
        this.onUserClickListener = listener;
    }

    private static final DiffUtil.ItemCallback<User> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<User>() {
                @Override
                public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
                    // זהה אם זהו אותו משתמש לפי UID
                    String oldId = oldItem.getId();
                    String newId = newItem.getId();
                    return oldId != null && oldId.equals(newId);
                }

                @Override
                public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
                    // השוואה לפי שדות רלוונטיים: שם, אימייל ותמונת פרופיל (avatarUrl)
                    String oldName = oldItem.getName();
                    String newName = newItem.getName();
                    String oldEmail = oldItem.getEmail();
                    String newEmail = newItem.getEmail();
                    String oldAvatar = oldItem.getAvatarUrl();
                    String newAvatar = newItem.getAvatarUrl();

                    boolean nameSame = (oldName == null && newName == null)
                            || (oldName != null && oldName.equals(newName));
                    boolean emailSame = (oldEmail == null && newEmail == null)
                            || (oldEmail != null && oldEmail.equals(newEmail));
                    boolean avatarSame = (oldAvatar == null && newAvatar == null)
                            || (oldAvatar != null && oldAvatar.equals(newAvatar));

                    return nameSame && emailSame && avatarSame;
                }
            };

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate של layout לפריט. נניח שהגדרת res/layout/item_member.xml
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member, parent, false);
        return new UserHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserHolder holder, int position) {
        User user = getItem(position);

        // הצגת השם
        String name = user.getName();
        holder.tvName.setText(name != null ? name : "");

        // הצגת האימייל
        String email = user.getEmail();
        holder.tvEmail.setText(email != null ? email : "");

        // טעינת ה-avatar באמצעות Glide אם יש URL, אחרת ברירת מחדל
        String avatarUrl = user.getAvatarUrl();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_user)
                    .error(R.drawable.ic_user)
                    .circleCrop()  // אם רוצים תמונה עגולה
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_user);
        }

        // הגדרת לחיצה על הפריט אם קיים listener
        if (onUserClickListener != null) {
            holder.itemView.setOnClickListener(v -> {
                // Trigger ripple effect and callback
                onUserClickListener.onUserClick(user);
            });
        } else {
            // לוודא שאם אין listener, אין OnClickListener ישן שימנע ripple
            holder.itemView.setOnClickListener(null);
        }
    }

    static class UserHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvEmail;
        ImageView ivAvatar;

        public UserHolder(@NonNull View itemView) {
            super(itemView);
            // IDs תואמים ל-item_member.xml:
            tvName = itemView.findViewById(R.id.tvMemberName);
            tvEmail = itemView.findViewById(R.id.tvMemberEmail);
            ivAvatar = itemView.findViewById(R.id.ivMemberAvatar);
        }
    }
}
