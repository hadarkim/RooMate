package com.example.roomate.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.roomate.model.Task;
import com.example.roomate.notification.ReminderScheduler;
import com.example.roomate.repository.TaskRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class TaskViewModel extends AndroidViewModel {
    private static final String TAG = "TaskViewModel";

    private final TaskRepository repo;
    private final LiveData<List<Task>> activeTasks;
    private final LiveData<List<Task>> overdueTasks;
    private final Observer<List<Task>> overdueObserver;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMsg   = new MutableLiveData<>();
    public LiveData<String> getErrorMsg() { return errorMsg; }

    public TaskViewModel(@NonNull Application application) {
        super(application);

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(application);
        String groupID = prefs.getString("GROUP_ID", null);
        if (groupID == null) {
            Log.w(TAG, "TaskViewModel: GROUP_ID is null");
            groupID = "";
        }
        repo = new TaskRepository(groupID);

        activeTasks  = repo.getOpenTasksSortedByDate();
        overdueTasks = repo.getTasksDueUpToNow();

        Context ctx = getApplication();
        overdueObserver = tasks -> {
            for (Task t : tasks) {
              //  ReminderScheduler.scheduleReminder(ctx, t);
            }
        };
        overdueTasks.observeForever(overdueObserver);
    }

    @Override
    public void onCleared() {
        super.onCleared();
        overdueTasks.removeObserver(overdueObserver);
    }

    public LiveData<List<Task>> getActiveTasks() {
        return activeTasks;
    }
    public LiveData<List<Task>> getOverdueTasks() { return overdueTasks; }
    public LiveData<Boolean> getLoadingState() { return isLoading; }

    /**
     * סימון מטלה כבוצע/לא בוצע: קורא ל-toggleTaskDone עם currentUid
     */
    public void toggleDone(@NonNull Task task) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "toggleDone: אין משתמש מחובר!");
            errorMsg.setValue("אנא התחבר שוב");
            return;
        }
        String currentUid = user.getUid();
        isLoading.setValue(true);
        boolean nowDone = !task.isDone();
        repo.toggleTaskDone(
                task.getId(),
                nowDone,
                currentUid,
                new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError error, DatabaseReference ref) {
                        isLoading.postValue(false);
                        if (error != null) {
                            Log.e(TAG, "toggleDone failed for task " + task.getId()
                                    + ", error=" + error.getMessage());
                            errorMsg.postValue("אין הרשאה או שגיאה בסימון מטלה");
                        } else {
                            task.setDone(nowDone);
                            if (nowDone) {
                              //  ReminderScheduler.cancelReminder(getApplication(), task);
                            }
                            Log.d(TAG, "toggleDone success for task " + task.getId());
                        }
                    }
                }
        );
    }

    /**
     * הוספת מטלה חדשה: משתמש את addTask הקיים
     */
    public void addTask(@NonNull Task task,
                        @NonNull Runnable onSuccess) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "addTask: אין משתמש מחובר!");
            errorMsg.setValue("אנא התחבר שוב");
            return;
        }
        // ניתן לוודא task.setAssignedToUid(user.getUid()) לפני הקריאה, אם זה המדיניות
        isLoading.setValue(true);
        repo.addTask(
                task,
                () -> {
                    isLoading.postValue(false);
                    Log.d(TAG, "addTask success for " + task.getId());
                    onSuccess.run();
                },
                exception -> {
                    isLoading.postValue(false);
                    Log.e(TAG, "addTask failed: " + exception.getMessage());
                    errorMsg.postValue("שגיאה בהוספת מטלה: " + exception.getMessage());
                }
        );
    }

    public void clearError() {
        errorMsg.setValue(null);
    }
}
