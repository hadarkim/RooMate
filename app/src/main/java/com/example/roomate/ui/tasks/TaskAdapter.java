package com.example.roomate.ui.tasks;

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
        h.bind(t);
    }

    class Holder extends RecyclerView.ViewHolder {
        private final CheckBox cbDone;
        private final TextView tvTitle, tvRoom, tvDue;
        // פורמט שתאריך מוצג בשורה
        private final SimpleDateFormat dateFmt =
                new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        Holder(View itemView) {
            super(itemView);
            cbDone = itemView.findViewById(R.id.cbDone);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvRoom  = itemView.findViewById(R.id.tvRoom);
            tvDue   = itemView.findViewById(R.id.tvDue);
        }

        void bind(Task task) {
            // כותרת ואזור
            tvTitle.setText(task.getTitle());
            tvRoom.setText(task.getRoom());

            // המרת Date ל-String (או ריק אם null)
            Date due = task.getDueDate();
            if (due != null) {
                tvDue.setText(dateFmt.format(due));
            } else {
                tvDue.setText("");
            }

            // סימון והאזנה ל-checkbox
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
