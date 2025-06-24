package com.example.roomate.ui.tasks;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roomate.R;
import com.example.roomate.model.Task;
import com.example.roomate.repository.UserRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * מציג שורות מטלה ב-RecyclerView.
 * מעדכן גם את שם המשתמש שהמטלה מוקצת לו.
 */
public class TaskAdapter extends ListAdapter<Task, TaskAdapter.Holder> {

    public interface Listener { void onToggle(Task task); }
    private static final String TAG = "TaskAdapter";
    private final Listener listener;
    private final UserRepository userRepo;
    private final LifecycleOwner lifecycleOwner;

    /**
     * @param l     callback לסימון done/undone
     * @param owner LifecycleOwner (Activity/Fragment) לצורך observe LiveData
     */
    public TaskAdapter(Listener l, LifecycleOwner owner) {
        super(DIFF);
        listener = l;
        lifecycleOwner = owner;
        userRepo = new UserRepository();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new Holder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Task t = getItem(position);
        Log.d(TAG, "onBindViewHolder pos=" + position
                + " title=" + t.getTitle()
                + " room=" + t.getRoom()
                + " done=" + t.isDone()
                + " assignedToUid=" + t.getAssignedToUid());
        h.bind(t);
    }

    class Holder extends RecyclerView.ViewHolder {
        private final CheckBox cbDone;
        private final TextView tvTitle, tvRoom, tvDue, tvAssignedToName;
        private final SimpleDateFormat dateFmt =
                new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());


        Holder(View itemView) {
            super(itemView);
            cbDone  = itemView.findViewById(R.id.cbDone);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvRoom  = itemView.findViewById(R.id.tvRoom);
            tvDue   = itemView.findViewById(R.id.tvDue);
            tvAssignedToName = itemView.findViewById(R.id.tvAssignedToName);

        }

        void bind(Task task) {
            // הגדרת שדות המטלה
            tvTitle.setText(task.getTitle());
            tvRoom.setText(task.getRoom());
            Date due = task.getDueDate();
            tvDue.setText(due != null ? dateFmt.format(due) : "");

            // טעינת שם המשתמש שהמטלה מוקצת לו
            String assignedUid = task.getAssignedToUid();
            if (assignedUid != null && !assignedUid.isEmpty()) {
                tvAssignedToName.setText("נטען...");
                userRepo.getUserById(assignedUid).observe(lifecycleOwner, user -> {
                    if (user != null) {
                        tvAssignedToName.setText(user.getName());

                    } else {
                        tvAssignedToName.setText("משתמש לא נמצא");
                    }
                });
            } else {
                tvAssignedToName.setText("");
            }

            // טיפול בסימון done
            cbDone.setOnCheckedChangeListener(null);
            cbDone.setChecked(task.isDone());
            cbDone.setOnCheckedChangeListener((btn, isChecked) ->
                    listener.onToggle(task));
        }
    }

    private static final DiffUtil.ItemCallback<Task> DIFF =
            new DiffUtil.ItemCallback<Task>() {
                @Override
                public boolean areItemsTheSame(@NonNull Task a, @NonNull Task b) {
                    return a.getId().equals(b.getId());
                }
                @Override
                public boolean areContentsTheSame(@NonNull Task a, @NonNull Task b) {
                    return a.equals(b);
                }
            };
}
