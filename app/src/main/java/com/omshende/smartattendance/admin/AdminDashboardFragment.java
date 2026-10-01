package com.omshende.smartattendance.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import com.omshende.smartattendance.R;

public class AdminDashboardFragment extends Fragment {

    // ==================================================
    // VIEWS
    // ==================================================

    private TextView tvAdminGreeting;
    private TextView tvAdminName;

    private TextView tvTotalStudents;
    private TextView tvTotalTeachers;
    private TextView tvTotalClasses;
    private TextView tvActiveLectures;

    private TextView tvStudentLabel;
    private TextView tvTeacherLabel;
    private TextView tvClassLabel;
    private TextView tvLectureLabel;

    private ProgressBar progressLoading;

    private MaterialCardView cardManageStudents;
    private MaterialCardView cardManageTeachers;
    private MaterialCardView cardManageClasses;
    private MaterialCardView cardRefresh;

    // ==================================================
    // FIREBASE
    // ==================================================

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // ==================================================
    // LIFECYCLE
    // ==================================================

    public AdminDashboardFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View view = inflater.inflate(
                R.layout.fragment_admin_dashboard,
                container,
                false
        );

        initializeFirebase();

        initializeViews(view);

        setupListeners();

        loadAdminProfile();

        loadDashboardStats();

        return view;
    }

    // ==================================================
    // FIREBASE
    // ==================================================

    private void initializeFirebase() {

        mAuth =
                FirebaseAuth.getInstance();

        db =
                FirebaseFirestore.getInstance();
    }

    // ==================================================
    // INITIALIZE VIEWS
    // ==================================================

    private void initializeViews(View view) {

        tvAdminGreeting =
                view.findViewById(
                        R.id.tvAdminGreeting
                );

        tvAdminName =
                view.findViewById(
                        R.id.tvAdminName
                );

        tvTotalStudents =
                view.findViewById(
                        R.id.tvTotalStudents
                );

        tvTotalTeachers =
                view.findViewById(
                        R.id.tvTotalTeachers
                );

        tvTotalClasses =
                view.findViewById(
                        R.id.tvTotalClasses
                );

        tvActiveLectures =
                view.findViewById(
                        R.id.tvActiveLectures
                );

        tvStudentLabel =
                view.findViewById(
                        R.id.tvStudentLabel
                );

        tvTeacherLabel =
                view.findViewById(
                        R.id.tvTeacherLabel
                );

        tvClassLabel =
                view.findViewById(
                        R.id.tvClassLabel
                );

        tvLectureLabel =
                view.findViewById(
                        R.id.tvLectureLabel
                );

        progressLoading =
                view.findViewById(
                        R.id.progressLoading
                );

        cardManageStudents =
                view.findViewById(
                        R.id.cardManageStudents
                );

        cardManageTeachers =
                view.findViewById(
                        R.id.cardManageTeachers
                );

        cardManageClasses =
                view.findViewById(
                        R.id.cardManageClasses
                );

        cardRefresh =
                view.findViewById(
                        R.id.cardRefresh
                );
    }

    // ==================================================
    // LISTENERS
    // ==================================================

    private void setupListeners() {

        // ==================================================
        // MANAGE STUDENTS
        // ==================================================

        cardManageStudents.setOnClickListener(
                v -> {

                    Toast.makeText(
                            requireContext(),
                            "Students management will open here.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );

        // ==================================================
        // MANAGE TEACHERS
        // ==================================================

        cardManageTeachers.setOnClickListener(
                v -> {

                    Toast.makeText(
                            requireContext(),
                            "Teachers management will open here.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );

        // ==================================================
        // MANAGE CLASSES
        // ==================================================

        cardManageClasses.setOnClickListener(
                v -> {

                    Toast.makeText(
                            requireContext(),
                            "Classes management will open here.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );

        // ==================================================
        // REFRESH
        // ==================================================

        cardRefresh.setOnClickListener(
                v -> {

                    loadDashboardStats();

                    Toast.makeText(
                            requireContext(),
                            "Dashboard refreshed.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );
    }

    // ==================================================
    // LOAD ADMIN PROFILE
    // ==================================================

    private void loadAdminProfile() {

        if (mAuth.getCurrentUser() == null) {

            tvAdminGreeting.setText(
                    "Welcome back"
            );

            tvAdminName.setText(
                    "Administrator"
            );

            return;
        }

        String uid =
                mAuth.getCurrentUser()
                        .getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnCompleteListener(
                        task -> {

                            if (!task.isSuccessful()) {

                                tvAdminGreeting.setText(
                                        "Welcome back"
                                );

                                tvAdminName.setText(
                                        "Administrator"
                                );

                                return;
                            }

                            if (task.getResult() == null
                                    || !task.getResult()
                                    .exists()) {

                                tvAdminGreeting.setText(
                                        "Welcome back"
                                );

                                tvAdminName.setText(
                                        "Administrator"
                                );

                                return;
                            }

                            String name =
                                    task.getResult()
                                            .getString(
                                                    "name"
                                            );

                            if (TextUtils.isEmpty(
                                    name
                            )) {

                                name =
                                        "Administrator";
                            }

                            tvAdminName.setText(
                                    name
                            );

                            tvAdminGreeting.setText(
                                    getGreeting()
                            );
                        }
                );
    }

    // ==================================================
    // GREETING
    // ==================================================

    private String getGreeting() {

        java.util.Calendar calendar =
                java.util.Calendar.getInstance();

        int hour =
                calendar.get(
                        java.util.Calendar.HOUR_OF_DAY
                );

        if (hour < 12) {

            return "Good morning";
        }

        if (hour < 17) {

            return "Good afternoon";
        }

        return "Good evening";
    }

    // ==================================================
    // LOAD DASHBOARD STATISTICS
    // ==================================================

    private void loadDashboardStats() {

        showLoading(true);

        loadStudentsCount();

        loadTeachersCount();

        loadClassesCount();

        loadActiveLecturesCount();
    }

    // ==================================================
    // STUDENTS COUNT
    // ==================================================

    private void loadStudentsCount() {

        db.collection("users")
                .whereEqualTo(
                        "role",
                        "student"
                )
                .get()
                .addOnCompleteListener(
                        task -> {

                            if (!task.isSuccessful()) {

                                tvTotalStudents.setText(
                                        "—"
                                );

                                return;
                            }

                            int count =
                                    task.getResult() != null
                                            ? task.getResult()
                                            .size()
                                            : 0;

                            tvTotalStudents.setText(
                                    String.valueOf(
                                            count
                                    )
                            );

                            hideLoading();
                        }
                );
    }

    // ==================================================
    // TEACHERS COUNT
    // ==================================================

    private void loadTeachersCount() {

        db.collection("users")
                .whereEqualTo(
                        "role",
                        "teacher"
                )
                .get()
                .addOnCompleteListener(
                        task -> {

                            if (!task.isSuccessful()) {

                                tvTotalTeachers.setText(
                                        "—"
                                );

                                return;
                            }

                            int count =
                                    task.getResult() != null
                                            ? task.getResult()
                                            .size()
                                            : 0;

                            tvTotalTeachers.setText(
                                    String.valueOf(
                                            count
                                    )
                            );

                            hideLoading();
                        }
                );
    }

    // ==================================================
    // CLASSES COUNT
    // ==================================================

    private void loadClassesCount() {

        db.collection("classes")
                .whereEqualTo(
                        "active",
                        true
                )
                .get()
                .addOnCompleteListener(
                        task -> {

                            if (!task.isSuccessful()) {

                                tvTotalClasses.setText(
                                        "—"
                                );

                                return;
                            }

                            int count =
                                    task.getResult() != null
                                            ? task.getResult()
                                            .size()
                                            : 0;

                            tvTotalClasses.setText(
                                    String.valueOf(
                                            count
                                    )
                            );

                            hideLoading();
                        }
                );
    }

    // ==================================================
    // ACTIVE LECTURES COUNT
    // ==================================================

    private void loadActiveLecturesCount() {

        db.collection("lectures")
                .whereEqualTo(
                        "status",
                        "ACTIVE"
                )
                .get()
                .addOnCompleteListener(
                        task -> {

                            if (!task.isSuccessful()) {

                                tvActiveLectures.setText(
                                        "—"
                                );

                                return;
                            }

                            int count =
                                    task.getResult() != null
                                            ? task.getResult()
                                            .size()
                                            : 0;

                            tvActiveLectures.setText(
                                    String.valueOf(
                                            count
                                    )
                            );

                            hideLoading();
                        }
                );
    }

    // ==================================================
    // LOADING
    // ==================================================

    private void showLoading(
            boolean loading
    ) {

        if (progressLoading == null) {
            return;
        }

        progressLoading.setVisibility(
                loading
                        ? View.VISIBLE
                        : View.GONE
        );
    }

    private void hideLoading() {

        /*
         * Each Firestore query calls this method.
         * Hiding the loader here keeps the UI responsive
         * while individual cards update.
         */

        if (progressLoading != null) {

            progressLoading.setVisibility(
                    View.GONE
            );
        }
    }

    // ==================================================
    // REFRESH ON RESUME
    // ==================================================

    @Override
    public void onResume() {

        super.onResume();

        if (db != null) {

            loadDashboardStats();
        }
    }
}