package com.example.roomate.ui.tasks;

import android.os.Bundle;
import android.util.Log;
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

import java.util.List;

/**
 * מציג רשימת מטלות (פתוחות או פקועות) ומנווט למסך יצירת מטלה.
 * מותאם לשימוש ב־TaskAdapter המעודכן שמקבל LifecycleOwner.
 */
public class TaskListFragment extends Fragment {

    private static final String TAG = "TaskListFragment";

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
        // טוען את ה־layout של הפרגמנט
        return inflater.inflate(R.layout.fragment_task_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1️⃣ אינסטנציאציה של ViewModel
        viewModel = new ViewModelProvider(requireActivity())
                .get(TaskViewModel.class);

        // 2️⃣ הגדרת RecyclerView ו-Adapter
        RecyclerView rv = view.findViewById(R.id.rvTasks);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        // callback לסימון מטלה
        TaskAdapter.Listener callback = task -> viewModel.toggleDone(task);

        // יצירת ה־Adapter עם LifecycleOwner = getViewLifecycleOwner()
        adapter = new TaskAdapter(callback, getViewLifecycleOwner());
        rv.setAdapter(adapter);

        // 3️⃣ אתחול ה-TabLayout עם שני טאבים
        tabLayout = view.findViewById(R.id.tabLayout);
        tabLayout.removeAllTabs();
        tabLayout.addTab(tabLayout.newTab().setText("פתוחות"));
        tabLayout.addTab(tabLayout.newTab().setText("פקע תוקף"));

        // 4️⃣ הגדרת Observers מראש
        openObserver = tasks -> {
            Log.d(TAG, "openObserver: got tasks = " + (tasks != null ? tasks.size() : 0));
            adapter.submitList(tasks);
            Log.d(TAG, "openObserver: after submitList()");
        };
        overdueObserver = tasks -> {
            Log.d(TAG, "overdueObserver: got tasks = " + (tasks != null ? tasks.size() : 0));
            adapter.submitList(tasks);
            Log.d(TAG, "overdueObserver: after submitList()");
        };

        // 5️⃣ מאזין לבחירת טאבים
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                Log.d(TAG, "Tab selected pos=" + pos);
                if (pos == 0) {
                    // מטלות פתוחות: נשנה observer
                    // נסיר קודם observer של overdue אם רשם קודם
                    viewModel.getOverdueTasks().removeObserver(overdueObserver);
                    // ואז נוסיף observer של open
                    viewModel.getActiveTasks().observe(getViewLifecycleOwner(), openObserver);
                } else {
                    // מטלות פקעות: נסיר observer של open, נוסיף את overdue
                    viewModel.getActiveTasks().removeObserver(openObserver);
                    viewModel.getOverdueTasks().observe(getViewLifecycleOwner(), overdueObserver);
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) {
                // אפשר לרענן או לגלול לראש; כרגע אין פעולה נוספת
                Log.d(TAG, "Tab reselected pos=" + tab.getPosition());
            }
        });

        // 6️⃣ אתחול ברירת מחדל לטאב הראשון (פתוחות)
        TabLayout.Tab firstTab = tabLayout.getTabAt(0);
        if (firstTab != null) {
            firstTab.select();
        }

        // 7️⃣ FloatingActionButton – ניווט למסך יצירת מטלה
        FloatingActionButton fab = view.findViewById(R.id.fabAddTask);
        fab.setOnClickListener(v ->
                androidx.navigation.fragment.NavHostFragment.findNavController(this)
                        .navigate(R.id.action_tasksFragment_to_createTask)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // נבטל observers כדי למנוע זליגות בבמקרים שה־View נהרס
        if (openObserver != null) {
            viewModel.getActiveTasks().removeObserver(openObserver);
        }
        if (overdueObserver != null) {
            viewModel.getOverdueTasks().removeObserver(overdueObserver);
        }
    }
}
