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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.omshende.smartattendance.R;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LecturesFragment extends Fragment {

    private RecyclerView recyclerLectures;
    private ProgressBar progressLectures;
    private TextView tvNoLectures;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private final List<DocumentSnapshot> lectureList =
            new ArrayList<>();

    private TeacherLecturesAdapter adapter;

    public LecturesFragment() {
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
                R.layout.fragment_lectures,
                container,
                false
        );

        // -------------------------------------------------
        // Firebase
        // -------------------------------------------------
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // -------------------------------------------------
        // Views
        // -------------------------------------------------
        recyclerLectures =
                view.findViewById(
                        R.id.recyclerLectures
                );

        progressLectures =
                view.findViewById(
                        R.id.progressLectures
                );

        tvNoLectures =
                view.findViewById(
                        R.id.tvNoLectures
                );

        // -------------------------------------------------
        // RecyclerView
        // -------------------------------------------------
        recyclerLectures.setLayoutManager(
                new LinearLayoutManager(
                        requireContext()
                )
        );

        recyclerLectures.setHasFixedSize(false);

        // -------------------------------------------------
        // Adapter
        // -------------------------------------------------
        adapter = new TeacherLecturesAdapter(
                lectureList,
                lecture -> {

                    String lectureId =
                            lecture.getId();

                    if (
                            lectureId == null
                                    || lectureId.trim().isEmpty()
                    ) {

                        Toast.makeText(
                                requireContext(),
                                "Lecture ID not found",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    Intent intent =
                            new Intent(
                                    requireContext(),
                                    TeacherLectureAttendanceActivity.class
                            );

                    intent.putExtra(
                            "lectureId",
                            lectureId
                    );

                    startActivity(intent);
                }
        );

        recyclerLectures.setAdapter(
                adapter
        );

        // -------------------------------------------------
        // Load lectures
        // -------------------------------------------------
        loadTeacherLectures();

        return view;
    }

    // =====================================================
    // Load Teacher Lectures
    // =====================================================

    private void loadTeacherLectures() {

        if (auth.getCurrentUser() == null) {

            showEmpty();

            return;
        }

        String teacherId =
                auth.getCurrentUser().getUid();

        showLoading(true);

        db.collection("lectures")
                .whereEqualTo(
                        "teacherId",
                        teacherId
                )
                .get()
                .addOnSuccessListener(
                        querySnapshot -> {

                            lectureList.clear();

                            for (
                                    DocumentSnapshot document
                                    : querySnapshot.getDocuments()
                            ) {

                                lectureList.add(
                                        document
                                );
                            }

                            // ---------------------------------
                            // Sort latest lecture first
                            // ---------------------------------
                            lectureList.sort(
                                    new Comparator<DocumentSnapshot>() {

                                        @Override
                                        public int compare(
                                                DocumentSnapshot d1,
                                                DocumentSnapshot d2
                                        ) {

                                            Object t1 =
                                                    d1.get("createdAt");

                                            Object t2 =
                                                    d2.get("createdAt");

                                            if (
                                                    t1 instanceof
                                                            com.google.firebase.Timestamp
                                                            &&
                                                            t2 instanceof
                                                                    com.google.firebase.Timestamp
                                            ) {

                                                com.google.firebase.Timestamp
                                                        ts1 =
                                                        (com.google.firebase.Timestamp)
                                                                t1;

                                                com.google.firebase.Timestamp
                                                        ts2 =
                                                        (com.google.firebase.Timestamp)
                                                                t2;

                                                return ts2
                                                        .toDate()
                                                        .compareTo(
                                                                ts1.toDate()
                                                        );
                                            }

                                            return 0;
                                        }
                                    }
                            );

                            adapter.notifyDataSetChanged();

                            showLoading(false);

                            if (lectureList.isEmpty()) {

                                showEmpty();

                            } else {

                                tvNoLectures.setVisibility(
                                        View.GONE
                                );

                                recyclerLectures.setVisibility(
                                        View.VISIBLE
                                );

                                // ---------------------------------
                                // Load attendance counts
                                // ---------------------------------
                                loadAttendanceCounts();
                            }
                        }
                )
                .addOnFailureListener(
                        e -> {

                            showLoading(false);

                            Toast.makeText(
                                    requireContext(),
                                    "Failed to load lectures: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();

                            showEmpty();
                        }
                );
    }

    // =====================================================
    // Load Present / Total Count
    // =====================================================

    private void loadAttendanceCounts() {

        // -------------------------------------------------
        // One query for each lecture
        // This is acceptable for the college prototype.
        // -------------------------------------------------

        for (
                DocumentSnapshot lecture
                : lectureList
        ) {

            String lectureId =
                    lecture.getId();

            getTotalStudents(
                    lecture,
                    total -> {

                        getPresentStudents(
                                lectureId,
                                present -> {

                                    adapter.setAttendanceCount(
                                            lectureId,
                                            present,
                                            total
                                    );

                                }
                        );

                    }
            );
        }
    }

    // =====================================================
    // Get Total Eligible Students
    // =====================================================

    private void getTotalStudents(
            DocumentSnapshot lecture,
            OnCountResult callback
    ) {

        Object targetValue =
                lecture.get(
                        "targetClassIds"
                );

        List<String> targetClassIds =
                new ArrayList<>();

        if (targetValue instanceof List) {

            List<?> list =
                    (List<?>) targetValue;

            for (Object item : list) {

                if (item != null) {

                    targetClassIds.add(
                            String.valueOf(item)
                    );
                }
            }
        }

        // ---------------------------------------------
        // Backward compatibility
        // ---------------------------------------------

        if (targetClassIds.isEmpty()) {

            String oldClassId =
                    getValue(
                            lecture,
                            "classId"
                    );

            if (!oldClassId.isEmpty()) {

                targetClassIds.add(
                        oldClassId
                );
            }
        }

        if (targetClassIds.isEmpty()) {

            callback.onResult(0);

            return;
        }

        // ---------------------------------------------
        // Get all students
        // ---------------------------------------------

        db.collection("users")
                .whereEqualTo(
                        "role",
                        "student"
                )
                .get()
                .addOnSuccessListener(
                        querySnapshot -> {

                            int total = 0;

                            for (
                                    DocumentSnapshot student
                                    : querySnapshot.getDocuments()
                            ) {

                                String studentClassId =
                                        getValue(
                                                student,
                                                "classId"
                                        );

                                if (
                                        targetClassIds.contains(
                                                studentClassId
                                        )
                                ) {

                                    total++;
                                }
                            }

                            callback.onResult(
                                    total
                            );
                        }
                )
                .addOnFailureListener(
                        e -> callback.onResult(0)
                );
    }

    // =====================================================
    // Get Present Students
    // =====================================================

    private void getPresentStudents(
            String lectureId,
            OnCountResult callback
    ) {

        db.collection("attendance")
                .whereEqualTo(
                        "lectureId",
                        lectureId
                )
                .get()
                .addOnSuccessListener(
                        querySnapshot -> {

                            int present = 0;

                            for (
                                    DocumentSnapshot attendance
                                    : querySnapshot.getDocuments()
                            ) {

                                String status =
                                        getValue(
                                                attendance,
                                                "status"
                                        );

                                if (
                                        "PRESENT".equalsIgnoreCase(
                                                status
                                        )
                                ) {

                                    present++;
                                }
                            }

                            callback.onResult(
                                    present
                            );
                        }
                )
                .addOnFailureListener(
                        e -> callback.onResult(0)
                );
    }

    // =====================================================
    // Safe Firestore Value
    // =====================================================

    private String getValue(
            DocumentSnapshot document,
            String field
    ) {

        Object value =
                document.get(field);

        if (value == null) {
            return "";
        }

        if (value instanceof Number) {

            Number number =
                    (Number) value;

            double d =
                    number.doubleValue();

            long l =
                    number.longValue();

            if (d == l) {

                return String.valueOf(l);
            }

            return String.valueOf(d);
        }

        return String.valueOf(value);
    }

    // =====================================================
    // Loading UI
    // =====================================================

    private void showLoading(
            boolean loading
    ) {

        if (progressLectures != null) {

            progressLectures.setVisibility(
                    loading
                            ? View.VISIBLE
                            : View.GONE
            );
        }

        if (loading) {

            recyclerLectures.setVisibility(
                    View.GONE
            );

            tvNoLectures.setVisibility(
                    View.GONE
            );
        }
    }

    // =====================================================
    // Empty UI
    // =====================================================

    private void showEmpty() {

        if (progressLectures != null) {

            progressLectures.setVisibility(
                    View.GONE
            );
        }

        if (recyclerLectures != null) {

            recyclerLectures.setVisibility(
                    View.GONE
            );
        }

        if (tvNoLectures != null) {

            tvNoLectures.setVisibility(
                    View.VISIBLE
            );

            tvNoLectures.setText(
                    "No lectures found"
            );
        }
    }

    // =====================================================
    // Callback
    // =====================================================

    private interface OnCountResult {

        void onResult(int count);
    }
}