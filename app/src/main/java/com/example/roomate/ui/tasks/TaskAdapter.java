package com.example.roomate.ui.tasks;

import android.util.Log;  // הוספת ייבוא ל-Log
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
import com.example.roomate.model.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * מציג שורות מטלה ב-RecyclerView ומעביר callback בעת סימון "בוצע".
 */
public class TaskAdapter extends ListAdapter<Task, TaskAdapter.Holder> {

    public interface Listener { void onToggle(Task task); }
    private static final String TAG = "TaskAdapter"; // תג ללוגים
    private final Listener listener;

    public TaskAdapter(Listener l) {
        super(DIFF);
        listener = l;
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
        // לוגינג: מדפיס מיקום ורשימת השדות החשובה
        Log.d(TAG, "onBindViewHolder pos=" + position
                + " title=" + t.getTitle()
                + " room=" + t.getRoom()
                + " done=" + t.isDone());
        h.bind(t);
    }

    class Holder extends RecyclerView.ViewHolder {
        private final CheckBox cbDone;
        private final TextView tvTitle, tvRoom, tvDue;
        private final SimpleDateFormat dateFmt =
                new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        Holder(View itemView) {
            super(itemView);
            cbDone  = itemView.findViewById(R.id.cbDone);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvRoom  = itemView.findViewById(R.id.tvRoom);
            tvDue   = itemView.findViewById(R.id.tvDue);
        }

        void bind(Task task) {
            tvTitle.setText(task.getTitle());
            tvRoom.setText(task.getRoom());

            Date due = task.getDueDate();
            if (due != null) {
                tvDue.setText(dateFmt.format(due));
            } else {
                tvDue.setText("");
            }

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
