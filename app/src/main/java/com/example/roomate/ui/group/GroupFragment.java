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
 * ומשם מציג את שם הקבוצה ורשימת חברי הקבוצה (RecyclerView).
 * מניח שהמשתמש המחובר תמיד שייך לקבוצה (לא מגיע לכאן אחרת).
 */
public class GroupFragment extends Fragment {
    private static final String TAG = "GroupFragment";

    private TextView     tvCurrentUserName;
    private ImageView    ivUserIcon;       // אייקון סטטי, ללא OnClickListener
    private TextView     tvGroupName;
    private TextView     tvMembersHeader;
    private RecyclerView rvMembers;
    private TextView     tvEmptyMembers;

    private UserAdapter     userAdapter;
    private UserRepository  userRepo;
    private GroupRepository groupRepo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate של ה-layout: res/layout/fragment_group.xml
        return inflater.inflate(R.layout.fragment_group, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. מציאת ה-Views מה-XML
        tvCurrentUserName = view.findViewById(R.id.tvCurrentUserName);
        ivUserIcon        = view.findViewById(R.id.ivUserIcon);        // סטטי, ללא פעולה
        tvGroupName       = view.findViewById(R.id.tvGroupName);
        tvMembersHeader   = view.findViewById(R.id.tvMembersHeader);
        rvMembers         = view.findViewById(R.id.rvMembers);
        tvEmptyMembers    = view.findViewById(R.id.tvEmptyMembers);

        // 2. אתחול Repositories
        userRepo  = new UserRepository();
        groupRepo = new GroupRepository();

        // 3. בדיקת משתמש מחובר
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "No authenticated user; redirecting to LoginActivity");
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
            return;
        }
        String currentUid = currentUser.getUid();

        // 4. טעינת השם של המשתמש המחובר בלבד
        userRepo.getUserById(currentUid).observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                String name = user.getName() != null ? user.getName() : "";
                tvCurrentUserName.setText(name);
            } else {
                tvCurrentUserName.setText("");
            }
        });

        // 5. קבלת ה-GROUP_ID מה-SharedPreferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String groupId = prefs.getString("GROUP_ID", null);
        if (groupId == null) {
            // באופן עקרוני לא אמור לקרות (משתמש שלא שייך לקבוצה לא אמור להגיע לכאן),
            // אך לטיפול מקרה חריג:
            Log.w(TAG, "No GROUP_ID found; redirecting to GroupSelectionActivity");
            Toast.makeText(requireContext(), "לא נמצא קוד קבוצה, בחר/צור קבוצה שוב", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(requireContext(), GroupSelectionActivity.class));
            requireActivity().finish();
            return;
        }

        // 6. טעינת שם הקבוצה להצגה
        groupRepo.getGroupById(groupId).observe(getViewLifecycleOwner(), group -> {
            if (group != null && group.getName() != null) {
                tvGroupName.setText("קבוצה: " + group.getName());
            } else {
                tvGroupName.setText("קבוצה: " + groupId);
            }
        });

        // 7. הכנת RecyclerView להצגת חברים
        userAdapter = new UserAdapter();
        rvMembers.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMembers.setAdapter(userAdapter);

        // 8. טעינת חברי הקבוצה באמצעות המתודה המתוקנת ב-UserRepository
        userRepo.getUsersByGroup(groupId).observe(getViewLifecycleOwner(), users -> {
            if (users != null && !users.isEmpty()) {
                // יש חברים: הצג את הכותרת, הצג את ה-RecyclerView והסתר הודעת "אין חברים"
                tvMembersHeader.setVisibility(View.VISIBLE);
                rvMembers.setVisibility(View.VISIBLE);
                tvEmptyMembers.setVisibility(View.GONE);
                userAdapter.submitList(users);
                Log.d(TAG, "Loaded members: " + users.size());
            } else {
                // אין חברים להצגה: הסתר כותרת ורשימה, הצג הודעת ריק
                tvMembersHeader.setVisibility(View.GONE);
                rvMembers.setVisibility(View.GONE);
                tvEmptyMembers.setVisibility(View.VISIBLE);
                Log.d(TAG, "No members in group");
            }
        });
    }
}
