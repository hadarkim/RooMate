package com.example.roomate.ui.tasks;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.roomate.R;
import com.example.roomate.model.Task;
import com.example.roomate.viewmodel.TaskViewModel;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class CreateTaskFragment extends Fragment {
    private static final String TAG = "CreateTaskFrag";

    private TaskViewModel viewModel;
    private EditText etTitle, etDueDate, etDescription;
    private Spinner spRoom;
    private Button btnSave;
    private Date dueDate;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1️⃣ אתחול ה-ViewModel
        viewModel = new ViewModelProvider(requireActivity())
                .get(TaskViewModel.class);

        // 2️⃣ איתור כל ה-Views מה-layout קודם לכל Observer
        etTitle       = view.findViewById(R.id.etTitle);
        etDescription = view.findViewById(R.id.etDescription);
        spRoom        = view.findViewById(R.id.spRoom);
        etDueDate     = view.findViewById(R.id.etDueDate);
        btnSave       = view.findViewById(R.id.btnSave);

        // 3️⃣ מאזין לשגיאות מה-ViewModel
        viewModel.getErrorMsg().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(),
                                "שגיאה בשמירה: " + error,
                                Toast.LENGTH_LONG)
                        .show();
                viewModel.clearError();
            }
        });

        // 4️⃣ מאזין למצב טעינה כדי לנעול/לשחרר את כפתור השמירה
        viewModel.getLoadingState().observe(getViewLifecycleOwner(), isLoading -> {
            btnSave.setEnabled(!isLoading);
        });

        // 5️⃣ DatePicker עבור שדה תאריך היעד
        dueDate = null;
        etDueDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(
                    requireContext(),
                    (DatePicker picker, int year, int month, int dayOfMonth) -> {
                        cal.set(year, month, dayOfMonth);
                        dueDate = cal.getTime();
                        etDueDate.setText(DateFormat.getDateInstance()
                                .format(dueDate));
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // 6️⃣ לחצן שמירה: וולידציה, יצירת Task ושליחה ל-ViewModel
        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            if (title.isEmpty()) {
                etTitle.setError("יש להזין כותרת");
                return;
            }
            if (dueDate == null) {
                etDueDate.setError("יש לבחור תאריך");
                return;
            }

            String description = etDescription.getText().toString().trim();
            String room        = spRoom.getSelectedItem().toString();

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Log.e(TAG, "אין משתמש מחובר!");
                Toast.makeText(requireContext(),
                        "אנא התחבר שוב", Toast.LENGTH_SHORT).show();
                return;
            }
            String userId = user.getUid();

            Task task = new Task(
                    UUID.randomUUID().toString(),
                    title,
                    description,
                    room,
                    userId,
                    dueDate,
                    "רגילה",
                    false
            );

            viewModel.addTask(task, () ->
                    NavHostFragment.findNavController(this).popBackStack()
            );
        });
    }
}
