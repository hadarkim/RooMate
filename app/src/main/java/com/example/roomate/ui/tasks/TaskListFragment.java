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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roomate.R;
import com.example.roomate.model.Task;
import com.example.roomate.viewmodel.TaskViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class TaskListFragment extends Fragment {

    private TaskAdapter adapter;
    private TaskViewModel viewModel;
    private TabLayout tabLayout;

    // Observers לטאבים
    private Observer<List<Task>> openObserver;
    private Observer<List<Task>> overdueObserver;

    public TaskListFragment() { /* בנאי ריק */ }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. ViewModel
        viewModel = new ViewModelProvider(requireActivity())
                .get(TaskViewModel.class);

        // 2. RecyclerView + Adapter
        RecyclerView rv = view.findViewById(R.id.rvTasks);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TaskAdapter(
                // 1. listener לסימון done/undone
                task -> viewModel.toggleDone(task),
                // 2. listener למחיקה
                task -> viewModel.deleteTask(task),
                // 3. ה־LifecycleOwner
                getViewLifecycleOwner()
        );

        rv.setAdapter(adapter);

        // 3. TabLayout: "פתוחות" ו-"פקע תוקף"
        tabLayout = view.findViewById(R.id.tabLayout);
        tabLayout.removeAllTabs();
        tabLayout.addTab(tabLayout.newTab().setText("עתידיות"));
        tabLayout.addTab(tabLayout.newTab().setText("פקע תוקף"));

        // 4. Configure observers

// ‏openObserver: מציג כל מטלה שטרם פגה לה מועד היעד, בין אם בוצעה ובין אם לא
        openObserver = tasks -> {
            long now = System.currentTimeMillis();
            List<Task> filtered = new ArrayList<>();
            if (tasks != null) {
                for (Task t : tasks) {
                    if (t.getDueDateMillis() > now) {
                        filtered.add(t);
                    }
                }
            }
            adapter.submitList(filtered);
        };

// ‏overdueObserver: מציג כל מטלה שתאריך היעד שלה כבר עבר, בין אם בוצעה ובין אם לא
        overdueObserver = tasks -> {
            long now = System.currentTimeMillis();
            List<Task> filtered = new ArrayList<>();
            if (tasks != null) {
                for (Task t : tasks) {
                    if (t.getDueDateMillis() <= now) {
                        filtered.add(t);
                    }
                }
            }
            adapter.submitList(filtered);
        };


        // 5. רישום מיידי של openObserver (פתוחות)
        viewModel.getActiveTasks()
                .observe(getViewLifecycleOwner(), openObserver);

        // 6. מאזין לשינוי טאבים
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    // טאב "פתוחות"
                    viewModel.getOverdueTasks().removeObserver(overdueObserver);
                    viewModel.getActiveTasks().removeObserver(openObserver);
                    viewModel.getActiveTasks()
                            .observe(getViewLifecycleOwner(), openObserver);

                } else {
                    // טאב "פקע תוקף"
                    viewModel.getActiveTasks().removeObserver(openObserver);
                    viewModel.getOverdueTasks().removeObserver(overdueObserver);
                    viewModel.getOverdueTasks()
                            .observe(getViewLifecycleOwner(), overdueObserver);
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });

        // 7. בחר כברירת מחדל את הטאב הראשון ("פתוחות")
        TabLayout.Tab first = tabLayout.getTabAt(0);
        if (first != null) first.select();

        // 8. FloatingActionButton → מסך יצירת מטלה
        FloatingActionButton fab = view.findViewById(R.id.fabAddTask);
        fab.setOnClickListener(v ->
                androidx.navigation.fragment.NavHostFragment
                        .findNavController(this)
                        .navigate(R.id.action_tasksFragment_to_createTask)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // הסרה של שני ה־Observers למניעת זליגות
        viewModel.getActiveTasks().removeObserver(openObserver);
        viewModel.getOverdueTasks().removeObserver(overdueObserver);
    }
}
