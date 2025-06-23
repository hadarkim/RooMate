// com/example/roomate/viewmodel/GroupViewModel.java

package com.example.roomate.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.roomate.model.Group;
import com.example.roomate.repository.GroupRepository;
import com.google.android.gms.tasks.Task;

import java.util.List;

public class GroupViewModel extends AndroidViewModel {
    private static final String TAG = "GroupViewModel";

    private final GroupRepository repo;
    private final LiveData<List<Group>> groups;

    public GroupViewModel(@NonNull Application application) {
        super(application);
        repo   = new GroupRepository();
        groups = repo.getAllGroups();
        Log.d(TAG, "Initialized, observing groups");
    }

    /** מחזיר LiveData של כל הקבוצות */
    public LiveData<List<Group>> getGroups() {
        return groups;
    }

    /**
     * יוצר קבוצה חדשה אטומית (name + members)
     * @return Task<Void> שיכול להאזין ל־onComplete במסך
     */
    public Task<Void> createGroup(
            @NonNull String id,
            @NonNull String name,
            @NonNull String creatorUid
    ) {
        Log.d(TAG, "ViewModel: createGroup id=" + id);
        return repo.createGroup(id, name, creatorUid);
    }

    /**
     * מצטרף לקבוצה קיימת
     * @return Task<Void> שיכול להאזין ל־onComplete במסך
     */
    public Task<Void> joinGroup(
            @NonNull String groupId,
            @NonNull String uid
    ) {
        Log.d(TAG, "ViewModel: joinGroup id=" + groupId);
        return repo.joinGroup(groupId, uid);
    }
}
