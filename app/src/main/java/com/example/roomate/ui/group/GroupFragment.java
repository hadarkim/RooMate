package com.example.roomate.ui.group;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roomate.R;
import com.example.roomate.auth.GroupSelectionActivity;
import com.example.roomate.auth.LoginActivity;
import com.example.roomate.repository.GroupRepository;
import com.example.roomate.repository.UserRepository;
import com.example.roomate.ui.member.UserAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

/**
 * Fragment שמציג בראש את השם של המשתמש המחובר לצד אייקון סטטי,
 * ובהמשך את שם הקבוצה, את ה-ID שלה, ורשימת חברי הקבוצה.
 */
public class GroupFragment extends Fragment {
    private static final String TAG = "GroupFragment";

    private TextView     tvCurrentUserName;
    private ImageView    ivUserIcon;
    private TextView     tvGroupName;
    private TextView     tvGroupId;
    private TextView     tvMembersHeader;
    private RecyclerView rvMembers;
    private TextView     tvEmptyMembers;

    private UserAdapter     userAdapter;
    private UserRepository  userRepo;
    private GroupRepository groupRepo;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        // טוען את ה-layout עבור Fragment זה
        return inflater.inflate(R.layout.fragment_group, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        // 1. מציאת כל ה-Views מתוך ה-XML
        tvCurrentUserName = view.findViewById(R.id.tvCurrentUserName);
        ivUserIcon        = view.findViewById(R.id.ivUserIcon);
        tvGroupName       = view.findViewById(R.id.tvGroupName);
        tvGroupId         = view.findViewById(R.id.tvGroupId);
        tvMembersHeader   = view.findViewById(R.id.tvMembersHeader);
        rvMembers         = view.findViewById(R.id.rvMembers);
        tvEmptyMembers    = view.findViewById(R.id.tvEmptyMembers);

        // 2. אתחול ה-Repositories
        userRepo  = new UserRepository();
        groupRepo = new GroupRepository();

        // 3. בדיקת משתמש מחובר; אם אין — ניתוב ל-Login
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "No authenticated user; redirecting to LoginActivity");
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
            return;
        }
        String currentUid = currentUser.getUid();

        // 4. טעינת ושמירת שם המשתמש המחובר בלבד
        userRepo.getUserById(currentUid)
                .observe(getViewLifecycleOwner(), user -> {
                    if (user != null) {
                        tvCurrentUserName.setText(user.getName());
                    } else {
                        tvCurrentUserName.setText("");
                    }
                });

        // 5. קריאת GROUP_ID מ-SharedPreferences
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(requireContext());
        String groupId = prefs.getString("GROUP_ID", null);
        Log.d(TAG, ">>> GroupFragment loaded GROUP_ID = " + groupId);
        if (groupId == null) {
            Log.w(TAG, "No GROUP_ID found; redirecting to GroupSelectionActivity");
            Toast.makeText(requireContext(),
                    "לא נמצא קוד קבוצה, בחר/צור קבוצה שוב",
                    Toast.LENGTH_SHORT).show();
            startActivity(new Intent(requireContext(), GroupSelectionActivity.class));
            requireActivity().finish();
            return;
        }

        // 6. טעינת שם הקבוצה והצגת ה-ID שלה
        groupRepo.getGroupById(groupId)
                .observe(getViewLifecycleOwner(), group -> {
                    if (group != null) {
                        // הצגת השם (או ה-ID בתור שמירת ברירת מחדל)
                        String name = group.getName() != null
                                ? group.getName()
                                : groupId;
                        tvGroupName.setText("קבוצה: " + name);

                        // הצגת ה-ID
                        tvGroupId.setText("Group ID: " + group.getId());
                    } else {
                        // מקרה קצה
                        tvGroupName.setText("קבוצה: " + groupId);
                        tvGroupId.setText("ID: " + groupId);
                    }
                });

        // 7. הכנת ה-RecyclerView להצגת חברי הקבוצה
        userAdapter = new UserAdapter();
        rvMembers.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMembers.setAdapter(userAdapter);

        // 8. טעינת חברי הקבוצה ע"י UserRepository
        userRepo.getUsersByGroup(groupId)
                .observe(getViewLifecycleOwner(), users -> {
                    if (users != null && !users.isEmpty()) {
                        tvMembersHeader.setVisibility(View.VISIBLE);
                        rvMembers.setVisibility(View.VISIBLE);
                        tvEmptyMembers.setVisibility(View.GONE);
                        userAdapter.submitList(users);
                        Log.d(TAG, "Loaded members: " + users.size());
                    } else {
                        tvMembersHeader.setVisibility(View.GONE);
                        rvMembers.setVisibility(View.GONE);
                        tvEmptyMembers.setVisibility(View.VISIBLE);
                        Log.d(TAG, "No members in group");
                    }
                });
    }
}
