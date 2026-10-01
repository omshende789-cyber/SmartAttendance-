package com.omshende.smartattendance.student;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import com.omshende.smartattendance.R;
import com.omshende.smartattendance.model.Lecture;

import java.util.ArrayList;
import java.util.List;

public class ActiveLectureFragment extends Fragment {

    // --------------------------------------------------
    // Views
    // --------------------------------------------------

    private RecyclerView recyclerActiveLectures;
    private ProgressBar progressLoading;
    private LinearLayout layoutEmpty;

    // --------------------------------------------------
    // Firebase
    // --------------------------------------------------

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Firestore realtime listener
    private ListenerRegistration lectureListener;

    // --------------------------------------------------
    // Data
    // --------------------------------------------------

    private final List<Lecture> lectureList = new ArrayList<>();

    private ActiveLectureAdapter adapter;

    // --------------------------------------------------
    // Lifecycle
    // --------------------------------------------------

    public ActiveLectureFragment() {
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
                R.layout.fragment_active_lecture,
                container,
                false
        );

        initializeFirebase();
        initializeViews(view);
        setupRecyclerView();

        loadStudentLectures();

        return view;
    }

    // --------------------------------------------------
    // Firebase Initialization
    // --------------------------------------------------

    private void initializeFirebase() {

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    // --------------------------------------------------
    // View Initialization
    // --------------------------------------------------

    private void initializeViews(@NonNull View view) {

        recyclerActiveLectures =
                view.findViewById(R.id.recyclerActiveLectures);

        progressLoading =
                view.findViewById(R.id.progressLoading);

        layoutEmpty =
                view.findViewById(R.id.layoutEmpty);
    }

    // --------------------------------------------------
    // RecyclerView
    // --------------------------------------------------

    private void setupRecyclerView() {

        adapter = new ActiveLectureAdapter(
                lectureList,
                lecture -> {

                    if (!isAdded()) {
                        return;
                    }

                    String lectureId = lecture.getLectureId();

                    if (TextUtils.isEmpty(lectureId)) {
                        Toast.makeText(
                                requireContext(),
                                "Lecture ID not found.",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    Intent intent = new Intent(
                            requireContext(),
                            EnterOtpActivity.class
                    );

                    intent.putExtra(
                            "lectureId",
                            lectureId
                    );

                    startActivity(intent);
                }
        );

        recyclerActiveLectures.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        recyclerActiveLectures.setAdapter(adapter);
    }

    // --------------------------------------------------
    // Load Student Profile
    // --------------------------------------------------

    private void loadStudentLectures() {

        if (!isAdded()) {
            return;
        }

        if (mAuth.getCurrentUser() == null) {

            showLoading(false);
            showEmptyState();

            Toast.makeText(
                    requireContext(),
                    "Student session not found.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        String studentId =
                mAuth.getCurrentUser().getUid();

        showLoading(true);

        db.collection("users")
                .document(studentId)
                .get()
                .addOnSuccessListener(student -> {

                    if (!isAdded()) {
                        return;
                    }

                    if (student == null || !student.exists()) {

                        showLoading(false);
                        showEmptyState();

                        Toast.makeText(
                                requireContext(),
                                "Student profile not found.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    String studentClassId =
                            student.getString("classId");

                    if (TextUtils.isEmpty(studentClassId)) {

                        showLoading(false);
                        showEmptyState();

                        Toast.makeText(
                                requireContext(),
                                "Student class is not assigned.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    studentClassId = studentClassId.trim();

                    if (studentClassId.isEmpty()) {

                        showLoading(false);
                        showEmptyState();

                        Toast.makeText(
                                requireContext(),
                                "Student class is not assigned.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    loadActiveLectures(studentClassId);
                })
                .addOnFailureListener(e -> {

                    if (!isAdded()) {
                        return;
                    }

                    showLoading(false);
                    showEmptyState();

                    Toast.makeText(
                            requireContext(),
                            "Unable to load student information.",
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // --------------------------------------------------
    // Load Active Lectures
    // --------------------------------------------------

    private void loadActiveLectures(String studentClassId) {

        // Remove previous listener if already active
        removeLectureListener();

        showLoading(true);

        /*
         * Student class:
         *
         * classId = it_2
         *
         * Lecture:
         *
         * targetClassIds = [
         *     "it_1",
         *     "it_2",
         *     "it_3",
         *     "it_4"
         * ]
         *
         * Student can see the lecture because
         * targetClassIds contains it_2.
         *
         * Division is NOT checked here.
         *
         * All divisions belonging to it_2
         * are automatically eligible.
         *
         * Lecture must also have:
         *
         * status = ACTIVE
         *
         * No startTime check.
         * No endTime check.
         * No current-time check.
         */

        lectureListener = db.collection("lectures")
                .whereArrayContains(
                        "targetClassIds",
                        studentClassId
                )
                .whereEqualTo(
                        "status",
                        "ACTIVE"
                )
                .addSnapshotListener((snapshot, error) -> {

                    if (!isAdded()) {
                        return;
                    }

                    showLoading(false);

                    // ------------------------------------------
                    // Firestore Error
                    // ------------------------------------------

                    if (error != null) {

                        lectureList.clear();

                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }

                        showEmptyState();

                        Toast.makeText(
                                requireContext(),
                                "Unable to load active lectures.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    // ------------------------------------------
                    // Clear Previous Data
                    // ------------------------------------------

                    lectureList.clear();

                    // ------------------------------------------
                    // Read Lectures
                    // ------------------------------------------

                    if (snapshot != null) {

                        for (DocumentSnapshot document
                                : snapshot.getDocuments()) {

                            // Check status again for safety
                            String status =
                                    document.getString("status");

                            if (!"ACTIVE".equalsIgnoreCase(status)) {
                                continue;
                            }

                            // Convert Firestore document
                            Lecture lecture =
                                    document.toObject(Lecture.class);

                            if (lecture == null) {
                                continue;
                            }

                            // Always use Firestore document ID
                            lecture.setLectureId(
                                    document.getId()
                            );

                            lectureList.add(lecture);
                        }
                    }

                    // ------------------------------------------
                    // Update RecyclerView
                    // ------------------------------------------

                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }

                    // ------------------------------------------
                    // Show Empty / Data State
                    // ------------------------------------------

                    if (lectureList.isEmpty()) {

                        showEmptyState();

                    } else {

                        layoutEmpty.setVisibility(
                                View.GONE
                        );

                        recyclerActiveLectures.setVisibility(
                                View.VISIBLE
                        );
                    }
                });
    }

    // --------------------------------------------------
    // Loading State
    // --------------------------------------------------

    private void showLoading(boolean loading) {

        if (!isAdded()) {
            return;
        }

        if (loading) {

            progressLoading.setVisibility(
                    View.VISIBLE
            );

            recyclerActiveLectures.setVisibility(
                    View.GONE
            );

            layoutEmpty.setVisibility(
                    View.GONE
            );

        } else {

            progressLoading.setVisibility(
                    View.GONE
            );
        }
    }

    // --------------------------------------------------
    // Empty State
    // --------------------------------------------------

    private void showEmptyState() {

        if (!isAdded()) {
            return;
        }

        layoutEmpty.setVisibility(
                View.VISIBLE
        );

        recyclerActiveLectures.setVisibility(
                View.GONE
        );
    }

    // --------------------------------------------------
    // Remove Firestore Listener
    // --------------------------------------------------

    private void removeLectureListener() {

        if (lectureListener != null) {

            lectureListener.remove();
            lectureListener = null;
        }
    }

    // --------------------------------------------------
    // Refresh
    // --------------------------------------------------

    @Override
    public void onResume() {

        super.onResume();

        if (mAuth != null
                && mAuth.getCurrentUser() != null) {

            loadStudentLectures();
        }
    }

    // --------------------------------------------------
    // Destroy View
    // --------------------------------------------------

    @Override
    public void onDestroyView() {

        removeLectureListener();

        if (recyclerActiveLectures != null) {
            recyclerActiveLectures.setAdapter(null);
        }

        super.onDestroyView();
    }
}