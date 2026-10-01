package com.omshende.smartattendance.student;

import android.os.Bundle;
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

public class StudentProfileFragment extends Fragment {

    private TextView tvProfileInitial;
    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private TextView tvProfileRole;

    private TextView tvEnrollment;
    private TextView tvDepartment;
    private TextView tvYear;
    private TextView tvDivision;
    private TextView tvRollNumber;
    private TextView tvClass;
    private TextView tvRole;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public StudentProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_student_profile,
                container,
                false
        );

        initializeFirebase();
        initializeViews(view);
        loadStudentProfile();

        return view;
    }

    private void initializeFirebase() {

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void initializeViews(View view) {

        tvProfileInitial = view.findViewById(R.id.tvProfileInitial);
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        tvProfileRole = view.findViewById(R.id.tvProfileRole);

        tvEnrollment = view.findViewById(R.id.tvEnrollment);
        tvDepartment = view.findViewById(R.id.tvDepartment);
        tvYear = view.findViewById(R.id.tvYear);
        tvDivision = view.findViewById(R.id.tvDivision);
        tvRollNumber = view.findViewById(R.id.tvRollNumber);
        tvClass = view.findViewById(R.id.tvClass);
        tvRole = view.findViewById(R.id.tvRole);
    }

    private void loadStudentProfile() {

        if (mAuth.getCurrentUser() == null) {

            Toast.makeText(
                    requireContext(),
                    "User session not found.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {

                        Toast.makeText(
                                requireContext(),
                                "Unable to load profile.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    DocumentSnapshot document = task.getResult();

                    if (document == null || !document.exists()) {

                        Toast.makeText(
                                requireContext(),
                                "Student profile not found.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    displayProfile(document);
                });
    }

    private void displayProfile(DocumentSnapshot document) {

        String name = document.getString("name");
        String email = document.getString("email");
        String enrollment = document.getString("enrollmentNo");
        String department = document.getString("department");
        String year = document.getString("year");
        String division = document.getString("division");
        String role = document.getString("role");

        Long rollNumber = document.getLong("rollNumber");

        // Name
        if (name != null && !name.trim().isEmpty()) {

            tvProfileName.setText(name);

            String initial = name
                    .trim()
                    .substring(0, 1)
                    .toUpperCase();

            tvProfileInitial.setText(initial);

        } else {

            tvProfileName.setText("Student");
            tvProfileInitial.setText("S");
        }

        // Email
        if (email != null && !email.trim().isEmpty()) {

            tvProfileEmail.setText(email);

        } else if (mAuth.getCurrentUser() != null) {

            tvProfileEmail.setText(
                    mAuth.getCurrentUser().getEmail()
            );
        }

        // Enrollment
        if (enrollment != null && !enrollment.trim().isEmpty()) {
            tvEnrollment.setText(enrollment);
        }

        // Department
        if (department != null && !department.trim().isEmpty()) {
            tvDepartment.setText(department);
        }

        // Year
        if (year != null && !year.trim().isEmpty()) {
            tvYear.setText(year);
        }

        // Division
        if (division != null && !division.trim().isEmpty()) {
            tvDivision.setText(division);
        }

        // Roll Number
        if (rollNumber != null) {

            tvRollNumber.setText(
                    String.valueOf(rollNumber)
            );
        }

        // Class / Section
        StringBuilder classInfo = new StringBuilder();

        if (department != null && !department.trim().isEmpty()) {
            classInfo.append(department);
        }

        if (year != null && !year.trim().isEmpty()) {

            if (classInfo.length() > 0) {
                classInfo.append(" • ");
            }

            classInfo.append(year);
        }

        if (division != null && !division.trim().isEmpty()) {

            if (classInfo.length() > 0) {
                classInfo.append(" • ");
            }

            classInfo.append("Division ")
                    .append(division);
        }

        if (rollNumber != null) {

            if (classInfo.length() > 0) {
                classInfo.append(" • ");
            }

            classInfo.append("Roll ")
                    .append(String.format(
                            "%02d",
                            rollNumber
                    ));
        }

        if (classInfo.length() > 0) {
            tvClass.setText(classInfo.toString());
        }

        // Role
        if (role != null && !role.trim().isEmpty()) {

            tvRole.setText(capitalize(role));
            tvProfileRole.setText(role.toUpperCase());

        } else {

            tvRole.setText("Student");
            tvProfileRole.setText("STUDENT");
        }
    }

    private String capitalize(String text) {

        if (text == null || text.isEmpty()) {
            return "";
        }

        return text.substring(0, 1).toUpperCase()
                + text.substring(1).toLowerCase();
    }
}