package com.example.roomate.ui.tasks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roomate.R;
import com.example.roomate.model.Task;
import com.example.roomate.viewmodel.TaskViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

/**
 * מציג רשימת מטלות (פתוחות או פקע־תוקף) ומנווט למסך יצירת מטלה.
 */
public class TaskListFragment extends Fragment {

    private TaskAdapter adapter;
    private TaskViewModel viewModel;
    private TabLayout tabLayout;
    private Observer<List<Task>> openObserver;
    private Observer<List<Task>> overdueObserver;

    public TaskListFragment() {
        // בנאי ריק נחוץ לפרגמנט
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_task_list, container, false);

        // 1️⃣ אינסטנציאציה של ViewModel
        viewModel = new ViewModelProvider(requireActivity())
                .get(TaskViewModel.class);

        // 2️⃣ הגדרת RecyclerView ו-Adapter
        RecyclerView rv = view.findViewById(R.id.rvTasks);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TaskAdapter(task -> viewModel.toggleDone(task));
        rv.setAdapter(adapter);

        // 3️⃣ אתחול ה-TabLayout עם שני טאבים
        tabLayout = view.findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("פתוחות"));
        tabLayout.addTab(tabLayout.newTab().setText("פקע תוקף"));

        // 4️⃣ הגדרת Observers מראש
        openObserver    = tasks -> adapter.submitList(tasks);
        overdueObserver = tasks -> adapter.submitList(tasks);

        // 5️⃣ מאזין לבחירת טאבים
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    // מטלות פתוחות
                    viewModel.getActiveTasks()
                            .removeObserver(overdueObserver);
                    viewModel.getActiveTasks()
                            .observe(getViewLifecycleOwner(), openObserver);
                } else {
                    // מטלות שפקע להן המועד
                    viewModel.getOverdueTasks()
                            .removeObserver(openObserver);
                    viewModel.getOverdueTasks()
                            .observe(getViewLifecycleOwner(), overdueObserver);
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });

        // 6️⃣ אתחול ברירת מחדל לטאב הראשון (פתוחות)
        tabLayout.getTabAt(0).select();

        // 7️⃣ FloatingActionButton – ניווט למסך יצירת מטלה
        FloatingActionButton fab = view.findViewById(R.id.fabAddTask);
        fab.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_tasksFragment_to_createTask)
        );

        return view;
    }
}
