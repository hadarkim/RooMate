package com.example.roomate.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMsg   = new MutableLiveData<>();

    public TaskViewModel(@NonNull Application application) {
        super(application);

        // קבלת ה-GROUP_ID מה־SharedPreferences
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(application);
        String groupID = prefs.getString("GROUP_ID", "");
        repo = new TaskRepository(groupID);

        // LiveData למטלות פתוחות ולמטלות שעתידן פגה
        activeTasks  = repo.getOpenTasksSortedByDate();
        overdueTasks = repo.getTasksDueUpToNow();
    }

    /** LiveData של מטלות פתוחות (not done, dueDate in future) */
    public LiveData<List<Task>> getActiveTasks() {
        return activeTasks;
    }

    /** LiveData של מטלות פגות־מועד (dueDate <= now) */
    public LiveData<List<Task>> getOverdueTasks() {
        return overdueTasks;
    }

    public LiveData<Boolean> getLoadingState() {
        return isLoading;
    }

    public LiveData<String> getErrorMsg() {
        return errorMsg;
    }

    /**
     * סימון מטלה כבוצעה/לא בוצעה.
     * אם היא מסומנת כ״בוצעה״, מבטלים את התזכורת שלה.
     */
    public void toggleDone(@NonNull Task task) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "toggleDone: no authenticated user");
            errorMsg.setValue("אנא התחבר שוב");
            return;
        }

        String currentUid = user.getUid();
        boolean nowDone = !task.isDone();
        isLoading.setValue(true);

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
                                // ביטול התזכורת במידה והמטלה בוצעה
                                ReminderScheduler.cancelReminder(
                                        getApplication(), task);
                            }
                            Log.d(TAG, "toggleDone success for task " + task.getId());
                        }
                    }
                }
        );
    }

    /**
     * הוספת מטלה חדשה.
     * אין כאן תזמון — ה־Fragment שקורא ל־addTask יתזמן הודעה על יצירתה.
     */
    public void addTask(@NonNull Task task,
                        @NonNull Runnable onSuccess) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "addTask: no authenticated user");
            errorMsg.setValue("אנא התחבר שוב");
            return;
        }

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

    /** איפוס הודעות שגיאה */
    public void clearError() {
        errorMsg.setValue(null);
    }
}
