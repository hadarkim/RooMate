package com.example.roomate.ui.tasks;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.roomate.R;
import com.example.roomate.model.Task;
import com.example.roomate.viewmodel.TaskViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * פרגמנט ליצירת מטלה חדשה.
 */
public class CreateTaskFragment extends Fragment {

    private TaskViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // מנפיחים את ה-layout של הפרגמנט
        return inflater.inflate(R.layout.fragment_create_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1️⃣ אתחול ה-ViewModel
        viewModel = new ViewModelProvider(requireActivity())
                .get(TaskViewModel.class);

        // 2️⃣ איתור ה-Views מה-layout
        EditText etTitle   = view.findViewById(R.id.etTitle);
        Spinner  spRoom    = view.findViewById(R.id.spRoom);
        EditText etDueDate = view.findViewById(R.id.etDueDate);
        Button   btnSave   = view.findViewById(R.id.btnSave);
        EditText etDescription = view.findViewById(R.id.etDescription);

        // 3️⃣ משתנה אחסון לתאריך שנבחר
        final Date[] dueDate = {null};

        // 4️⃣ DatePicker עבור שדה תאריך היעד
        etDueDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(
                    requireContext(),
                    (picker, year, month, dayOfMonth) -> {
                        cal.set(year, month, dayOfMonth);
                        Date date = cal.getTime();
                        dueDate[0] = date;
                        etDueDate.setText(DateFormat.getDateInstance().format(date));
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // 5️⃣ לחצן שמירה: בונה אובייקט Task ושולח ל-ViewModel
        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            if (title.isEmpty()) {
                etTitle.setError("יש להזין כותרת");
                return;
            }
            if (dueDate[0] == null) {
                etDueDate.setError("יש לבחור תאריך");
                return;
            }

            String desc = etDescription.getText().toString().trim();
            String room   = spRoom.getSelectedItem().toString();
            String userId = FirebaseAuth.getInstance()
                    .getCurrentUser()
                    .getUid();

            Task task = new Task(
                    UUID.randomUUID().toString(),
                    title,
                    desc,
                    room,
                    userId,
                    dueDate[0],
                    "רגילה",
                    false
            );

            // 6️⃣ הוספת המטלה והחזרה למסך הקודם
            viewModel.addTask(task, () ->
                    NavHostFragment.findNavController(this).popBackStack()
            );
        });
    }
}
