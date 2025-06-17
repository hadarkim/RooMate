package com.example.roomate.viewmodel;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.roomate.model.Task;
import com.example.roomate.notification.ReminderScheduler;
import com.example.roomate.repository.TaskRepository;

import java.util.List;
import java.util.function.Consumer;

/**
 * AndroidViewModel עבור ניהול המטלות ב-rooMate.
 * כעת תומך בשני מסלולים:
 *  • open tasks (מסך ראשי)
 *  • overdue tasks (לתזכורות אוטומטיות)
 */
public class TaskViewModel extends AndroidViewModel {

    private final TaskRepository repo = TaskRepository.getInstance();

    // ② LiveData למטלות פתוחות (open), ממוינות לפי dueDateMillis
    private final LiveData<List<Task>> activeTasks;

    // ③ LiveData למטלות שתאריך היעד שלהן כבר עבר
    private final LiveData<List<Task>> overdueTasks;

    //  observer לשמירה על ביטול בת-cleared()
    private final Observer<List<Task>> overdueObserver;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMsg   = new MutableLiveData<>();
    public LiveData<String> getErrorMsg() { return errorMsg; }

    public TaskViewModel(@NonNull Application application) {
        super(application);

        // את השניים שהגדרנו ב-Repository
        activeTasks  = repo.getOpenTasksSortedByDate();
        overdueTasks = repo.getTasksDueUpToNow();

        // ① מאזין ל-overdueTasks ומזמין תזכורת לכל מטלה שפגה
        Context ctx = getApplication();
        overdueObserver = tasks -> {
            for (Task t : tasks) {
                ReminderScheduler.scheduleReminder(ctx, t);
            }
        };
        overdueTasks.observeForever(overdueObserver);
    }

    // --- גеттерים ל-UI ---

    /** מטלות פתוחות (open) */
    public LiveData<List<Task>> getActiveTasks() {
        return activeTasks;
    }

    /** מטלות שפג תוקפן (overdue), לצורך תזכורת */
    public LiveData<List<Task>> getOverdueTasks() {
        return overdueTasks;
    }

    public LiveData<Boolean> getLoadingState() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMsg;
    }

    // --- פעולות על המטלות ---

    @Override
    public void onCleared() {
        super.onCleared();
        // להסרת ה-observer ולמניעת דליפת זיכרון
        overdueTasks.removeObserver(overdueObserver);
    }

    /**
     * סימון מטלה כ-בוצע או פתוחה מחדש.
     * מבטל תזכורת אם סימנו כבוצע.
     */
    public void toggleDone(@NonNull Task task) {
        isLoading.setValue(true);
        try {
            boolean nowDone = !task.isDone();
            task.setDone(nowDone);
            repo.updateTask(task);

            // ② אם סימנו כבוצע, בטל תזכורת
            if (nowDone) {
                ReminderScheduler.cancelReminder(getApplication(), task);
            }
        } catch (Exception e) {
            errorMsg.setValue("שגיאה בעדכון מטלה: " + e.getMessage());
        } finally {
            isLoading.setValue(false);
        }
    }


    /**
     * הוספת מטלה חדשה ל-repo, עם טיפול גם לכישלון.
     */
    public void addTask(@NonNull Task task,
                        @NonNull Runnable onSuccess) {
        isLoading.setValue(true);
        repo.addTask(
                task,
                // onSuccess listener
                () -> {
                    isLoading.postValue(false);
                    onSuccess.run();
                },
                // added: onError listener שמעדכן את errorMsg ב-LiveData
                exception -> {
                    isLoading.postValue(false);
                    errorMsg.postValue("שגיאה בהוספת מטלה: " + exception.getMessage());
                }
        );
    }
    // ◆ **המתודה החדשה** ◆
    // קוראים לה כדי "לאפס" (לנקות) את הודעת השגיאה לאחר שהצגנו אותה למשתמש
    // errorMsg הוא MutableLiveData<String> שמייצג את הטעות הנוכחית (או null אם אין שגיאה).
    //
    //ב־addTask, כשמתרחשת שגיאה, אנחנו מפרסמים אותה דרך errorMsg.postValue(...).
    //
    //המתודה clearError() מאפסת את errorMsg ל־null, כך שעירור ה־LiveData יתאפס.
    public void clearError() {
        errorMsg.setValue(null);
    }
}
