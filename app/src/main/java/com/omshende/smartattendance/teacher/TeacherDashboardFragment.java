package com.omshende.smartattendance.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.omshende.smartattendance.R;

public class TeacherDashboardFragment extends Fragment {

    private TextView tvTeacherGreeting;
    private TextView tvTeacherInitial;
    private TextView tvTeacherName;
    private TextView tvTeacherFacultyId;
    private TextView tvTeacherDepartment;

    private View cardStartLecture;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public TeacherDashboardFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_teacher_dashboard,
                container,
                false
        );

        initializeFirebase();
        initializeViews(view);
        setupListeners();
        loadTeacherData();

        return view;
    }

    private void initializeFirebase() {

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void initializeViews(View view) {

        tvTeacherGreeting =
                view.findViewById(R.id.tvTeacherGreeting);

        tvTeacherInitial =
                view.findViewById(R.id.tvTeacherInitial);

        tvTeacherName =
                view.findViewById(R.id.tvTeacherName);

        tvTeacherFacultyId =
                view.findViewById(R.id.tvTeacherFacultyId);

        tvTeacherDepartment =
                view.findViewById(R.id.tvTeacherDepartment);

        cardStartLecture =
                view.findViewById(R.id.cardStartLecture);
    }

    private void setupListeners() {

        cardStartLecture.setOnClickListener(v -> {

            Intent intent = new Intent(
                    requireContext(),
                    CreateLectureActivity.class
            );

            startActivity(intent);
        });
    }

    private void loadTeacherData() {

        if (mAuth.getCurrentUser() == null) {

            Toast.makeText(
                    requireContext(),
                    "Teacher session not found.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        String uid =
                mAuth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {

                        Toast.makeText(
                                requireContext(),
                                "Unable to load teacher information.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    DocumentSnapshot document =
                            task.getResult();

                    if (document == null
                            || !document.exists()) {

                        Toast.makeText(
                                requireContext(),
                                "Teacher profile not found.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    displayTeacherData(document);
                });
    }

    private void displayTeacherData(
            DocumentSnapshot document
    ) {

        String name =
                document.getString("name");

        String facultyId =
                document.getString("facultyId");

        String department =
                document.getString("department");

        if (TextUtils.isEmpty(name)) {
            name = "Teacher";
        }

        tvTeacherName.setText(name);

        tvTeacherGreeting.setText(
                "Welcome back, " + name + " 👋"
        );

        if (!TextUtils.isEmpty(facultyId)) {

            tvTeacherFacultyId.setText(
                    "Faculty ID: " + facultyId
            );

        } else {

            tvTeacherFacultyId.setText(
                    "Faculty ID: Not available"
            );
        }

        if (!TextUtils.isEmpty(department)) {

            tvTeacherDepartment.setText(
                    "Department: " + department
            );

        } else {

            tvTeacherDepartment.setText(
                    "Department: Not available"
            );
        }

        String cleanName = name.trim();

        if (!cleanName.isEmpty()) {

            String initial =
                    cleanName.substring(0, 1)
                            .toUpperCase();

            tvTeacherInitial.setText(initial);
        } else {

            tvTeacherInitial.setText("T");
        }
    }
}