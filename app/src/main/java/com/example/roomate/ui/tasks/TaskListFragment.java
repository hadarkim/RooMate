package com.example.roomate.ui.tasks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roomate.R;
import com.example.roomate.viewmodel.TaskViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * מציג רשימת מטלות ומנווט למסך יצירת מטלה.
 */
public class TasksFragment extends Fragment {

    private TaskAdapter adapter;
    private TaskViewModel viewModel;

    public TasksFragment() {
        // בנאי ריק נחוץ לפרגמנט
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        // 1️⃣ אינסטנציאציה של ViewModel
        viewModel = new ViewModelProvider(requireActivity())
                .get(TaskViewModel.class);

        // 2️⃣ הגדרת RecyclerView ו-Adapter (עכשיו ש-viewModel כבר מאותחל)
        RecyclerView rv = view.findViewById(R.id.rvTasks);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TaskAdapter(task -> viewModel.toggleDone(task));
        rv.setAdapter(adapter);

        // 3️⃣ הקשבה ל-LiveData מתוך ה-ViewModel
        viewModel.getActiveTasks()
                .observe(getViewLifecycleOwner(), adapter::submitList);

        // 4️⃣ FloatingActionButton – ניווט למסך יצירת מטלה
        FloatingActionButton fab = view.findViewById(R.id.fabAddTask);
        fab.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_tasksFragment_to_createTaskFragment)
        );

        return view;
    }
}
