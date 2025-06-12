package com.example.roomate.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.roomate.model.Task;
import com.example.roomate.repository.TaskRepository;

import java.util.List;

/**
 * ViewModel עבור ניהול המטלות ב-rooMate.
 * מפשט את הקשר בין ה־UI (Fragments) לבין ה־TaskRepository (Firebase Realtime DB).
 */
public class TaskViewModel extends ViewModel {

    // ① הסינגלטון של ה-Repository שלנו
    private final TaskRepository repo = TaskRepository.getInstance();

    // ② LiveData לרשימת המטלות שטרם בוצעו (done == false)
    private final LiveData<List<Task>> activeTasks = repo.getActiveTasks();

    // ③ MutableLiveData לשמירה על מצב טעינה
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    // ④ MutableLiveData להצגת הודעות שגיאה
    private final MutableLiveData<String> errorMsg = new MutableLiveData<>();

    // --- גеттерים לטובת ה-UI ---

    /** רשימת המטלות הפעילות (לא בוצעו) */
    public LiveData<List<Task>> getActiveTasks() {
        return activeTasks;
    }

    /** מצב טעינה – true אם מתבצעת בקשה ל-Firebase */
    public LiveData<Boolean> getLoadingState() {
        return isLoading;
    }

    /** הודעת שגיאה אחרונה, אם התרחשה */
    public LiveData<String> getErrorMessage() {
        return errorMsg;
    }

    // --- פעולות על המטלות ---

    /**
     * הופך את מצב "בוצע" של המטלה ומעדכן ב-Firebase.
     * במידה ומתעוררת שגיאה (Exception), תדחוף הודעה ל-errorMsg.
     */
    public void toggleDone(@NonNull Task task) {
        isLoading.setValue(true);
        try {
            task.setDone(!task.isDone());
            repo.updateTask(task);
            isLoading.setValue(false);
        } catch (Exception e) {
            isLoading.setValue(false);
            errorMsg.setValue("שגיאה בעדכון מטלה: " + e.getMessage());
        }
    }

    /**
     * מוסיף מטלה חדשה ל-Firebase.
     * onSuccess יופעל אחרי שהמטלה נשמרה.
     */
    public void addTask(@NonNull Task task, @NonNull Runnable onSuccess) {
        isLoading.setValue(true);
        repo.addTask(task, () -> {
            // הצלחה
            isLoading.postValue(false);
            onSuccess.run();
        });
        // אם תרצה טיפול בשגיאה ב-addTask, הוסף callback מתאים ב-TaskRepository
    }

    /**
     * במידה ותרצה בעתיד לסנן מטלות לפי משתמש ספציפי:
     * return repo.getTasksByUser(userId);
     */
}
