package com.omshende.smartattendance.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.omshende.smartattendance.MainActivity;
import com.omshende.smartattendance.R;

import java.util.List;

public class TeacherProfileFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private TextView tvProfileInitial;
    private TextView tvFacultyId;
    private TextView tvDepartment;
    private TextView tvAssignedClasses;
    private TextView tvAssignedSubjects;

    private ProgressBar progressProfile;

    private MaterialCardView cardChangePassword;
    private MaterialCardView cardLogout;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View view = inflater.inflate(
                R.layout.fragment_teacher_profile,
                container,
                false
        );

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews(view);
        setupActions();
        loadTeacherProfile();

        return view;
    }

    private void initializeViews(View view) {

        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        tvProfileInitial = view.findViewById(R.id.tvProfileInitial);

        tvFacultyId = view.findViewById(R.id.tvFacultyId);
        tvDepartment = view.findViewById(R.id.tvDepartment);
        tvAssignedClasses = view.findViewById(R.id.tvAssignedClasses);
        tvAssignedSubjects = view.findViewById(R.id.tvAssignedSubjects);

        progressProfile = view.findViewById(R.id.progressProfile);

        cardChangePassword =
                view.findViewById(R.id.cardChangePassword);

        cardLogout =
                view.findViewById(R.id.cardLogout);
    }

    private void setupActions() {

        cardChangePassword.setOnClickListener(v -> {

            sendPasswordResetEmail();
        });

        cardLogout.setOnClickListener(v -> {

            logoutTeacher();
        });
    }

    private void loadTeacherProfile() {

        if (auth.getCurrentUser() == null) {
            return;
        }

        String teacherId =
                auth.getCurrentUser().getUid();

        progressProfile.setVisibility(View.VISIBLE);

        db.collection("users")
                .document(teacherId)
                .get()
                .addOnSuccessListener(document -> {

                    progressProfile.setVisibility(View.GONE);

                    if (!document.exists()) {

                        Toast.makeText(
                                requireContext(),
                                "Profile not found",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    displayProfile(document);

                })
                .addOnFailureListener(e -> {

                    progressProfile.setVisibility(View.GONE);

                    Toast.makeText(
                            requireContext(),
                            "Unable to load profile",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void displayProfile(
            DocumentSnapshot document
    ) {

        String name =
                getValue(document, "name");

        String email =
                getValue(document, "email");

        String facultyId =
                getValue(document, "facultyId");

        String department =
                getValue(document, "department");

        String assignedClasses =
                getValue(document, "assignedClasses");

        String assignedSubjects =
                getValue(document, "assignedSubjects");

        tvProfileName.setText(
                name.isEmpty()
                        ? "Teacher"
                        : name
        );

        tvProfileEmail.setText(
                email.isEmpty()
                        ? "No email"
                        : email
        );

        tvFacultyId.setText(
                facultyId.isEmpty()
                        ? "Not available"
                        : facultyId
        );

        tvDepartment.setText(
                department.isEmpty()
                        ? "Not available"
                        : department
        );

        tvAssignedClasses.setText(
                formatArrayOrText(
                        document,
                        "assignedClasses",
                        assignedClasses
                )
        );

        tvAssignedSubjects.setText(
                formatArrayOrText(
                        document,
                        "assignedSubjects",
                        assignedSubjects
                )
        );

        String initial = "T";

        if (!name.isEmpty()) {
            initial = name.substring(0, 1).toUpperCase();
        }

        tvProfileInitial.setText(initial);
    }

    private String formatArrayOrText(
            DocumentSnapshot document,
            String field,
            String fallback
    ) {

        Object value = document.get(field);

        if (value instanceof List<?>) {

            List<?> list = (List<?>) value;

            if (!list.isEmpty()) {

                StringBuilder builder =
                        new StringBuilder();

                for (int i = 0; i < list.size(); i++) {

                    if (i > 0) {
                        builder.append(" • ");
                    }

                    builder.append(
                            String.valueOf(list.get(i))
                    );
                }

                return builder.toString();
            }
        }

        return fallback.isEmpty()
                ? "Not assigned"
                : fallback;
    }

    private String getValue(
            DocumentSnapshot document,
            String field
    ) {

        Object value = document.get(field);

        if (value == null) {
            return "";
        }

        return String.valueOf(value);
    }

    private void sendPasswordResetEmail() {

        if (auth.getCurrentUser() == null) {
            return;
        }

        String email =
                auth.getCurrentUser().getEmail();

        if (email == null || email.trim().isEmpty()) {

            Toast.makeText(
                    requireContext(),
                    "Email not available",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            requireContext(),
                            "Password reset email sent",
                            Toast.LENGTH_LONG
                    ).show();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            requireContext(),
                            "Unable to send reset email",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void logoutTeacher() {

        auth.signOut();

        Intent intent =
                new Intent(
                        requireActivity(),
                        MainActivity.class
                );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
    }
}