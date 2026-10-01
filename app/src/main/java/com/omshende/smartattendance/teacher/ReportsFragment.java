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

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.omshende.smartattendance.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReportsFragment extends Fragment {

    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // =========================================================
    // UI
    // =========================================================

    private TextView tvTotalLectures;
    private TextView tvTotalPresent;
    private TextView tvTotalAbsent;
    private TextView tvOverallPercentage;

    private ProgressBar progressOverallAttendance;

    private View cardTodayReport;
    private View cardWeekReport;
    private View cardMonthReport;
    private View cardSubjectReport;
    private View cardClassReport;
    private View btnViewAllReports;

    // =========================================================
    // REPORT DATA
    // =========================================================

    private int totalLectures = 0;
    private int totalPresent = 0;
    private int totalAbsent = 0;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public ReportsFragment() {
        // Required empty constructor
    }

    // =========================================================
    // ON CREATE VIEW
    // =========================================================

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View view = inflater.inflate(
                R.layout.fragment_reports,
                container,
                false
        );

        // Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // -----------------------------------------------------
        // FIND VIEWS
        // -----------------------------------------------------

        tvTotalLectures =
                view.findViewById(
                        R.id.tvTotalLectures
                );

        tvTotalPresent =
                view.findViewById(
                        R.id.tvTotalPresent
                );

        tvTotalAbsent =
                view.findViewById(
                        R.id.tvTotalAbsent
                );

        tvOverallPercentage =
                view.findViewById(
                        R.id.tvOverallPercentage
                );

        progressOverallAttendance =
                view.findViewById(
                        R.id.progressOverallAttendance
                );

        // -----------------------------------------------------
        // REPORT CARDS
        // -----------------------------------------------------

        cardTodayReport =
                view.findViewById(
                        R.id.cardTodayReport
                );

        cardWeekReport =
                view.findViewById(
                        R.id.cardWeekReport
                );

        cardMonthReport =
                view.findViewById(
                        R.id.cardMonthReport
                );

        cardSubjectReport =
                view.findViewById(
                        R.id.cardSubjectReport
                );

        cardClassReport =
                view.findViewById(
                        R.id.cardClassReport
                );

        btnViewAllReports =
                view.findViewById(
                        R.id.btnViewAllReports
                );

        // -----------------------------------------------------
        // DEFAULT UI
        // -----------------------------------------------------

        setDefaultValues();

        // -----------------------------------------------------
        // CLICK LISTENERS
        // -----------------------------------------------------

        setupClickListeners();

        return view;
    }

    // =========================================================
    // ON START
    // =========================================================

    @Override
    public void onStart() {
        super.onStart();

        loadOverallReport();
    }

    // =========================================================
    // LOAD OVERALL REPORT
    // =========================================================

    private void loadOverallReport() {

        if (!isAdded()) {
            return;
        }

        if (auth.getCurrentUser() == null) {

            showError(
                    "Teacher session not found"
            );

            return;
        }

        String teacherId =
                auth.getCurrentUser().getUid();

        // Reset
        totalLectures = 0;
        totalPresent = 0;
        totalAbsent = 0;

        /*
         * We use only CLOSED lectures for reports.
         *
         * ACTIVE lecture is still running, so it should
         * not be included in final attendance reports.
         */

        db.collection("lectures")
                .whereEqualTo(
                        "teacherId",
                        teacherId
                )
                .whereEqualTo(
                        "status",
                        "CLOSED"
                )
                .get()
                .addOnSuccessListener(
                        lectureSnapshots -> {

                            if (!isAdded()) {
                                return;
                            }

                            if (
                                    lectureSnapshots == null
                                            || lectureSnapshots.isEmpty()
                            ) {

                                updateReportUI();

                                return;
                            }

                            totalLectures =
                                    lectureSnapshots.size();

                            /*
                             * We process each lecture separately.
                             */

                            List<DocumentSnapshot> lectures =
                                    lectureSnapshots.getDocuments();

                            processLectures(
                                    lectures,
                                    0,
                                    0,
                                    0
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }

                            showError(
                                    "Unable to load reports: "
                                            + e.getMessage()
                            );
                        }
                );
    }

    // =========================================================
    // PROCESS LECTURES
    // =========================================================

    private void processLectures(
            List<DocumentSnapshot> lectures,
            int lectureIndex,
            int presentTotal,
            int absentTotal
    ) {

        if (!isAdded()) {
            return;
        }

        // -----------------------------------------------------
        // ALL LECTURES PROCESSED
        // -----------------------------------------------------

        if (
                lectureIndex >= lectures.size()
        ) {

            totalPresent =
                    presentTotal;

            totalAbsent =
                    absentTotal;

            updateReportUI();

            return;
        }

        DocumentSnapshot lecture =
                lectures.get(lectureIndex);

        String lectureId =
                lecture.getId();

        // -----------------------------------------------------
        // TARGET CLASS IDS
        // -----------------------------------------------------

        List<String> targetClassIds =
                getStringList(
                        lecture.get("targetClassIds")
                );

        /*
         * Old lecture compatibility:
         *
         * If targetClassIds does not exist,
         * try classId.
         */

        if (targetClassIds.isEmpty()) {

            String oldClassId =
                    getValue(
                            lecture,
                            "classId"
                    );

            if (
                    oldClassId != null
                            && !oldClassId.trim().isEmpty()
            ) {

                targetClassIds.add(
                        oldClassId.trim()
                );
            }
        }

        // -----------------------------------------------------
        // LOAD ELIGIBLE STUDENTS
        // -----------------------------------------------------

        loadEligibleStudents(
                targetClassIds,
                eligibleStudents -> {

                    int totalStudents =
                            eligibleStudents.size();

                    if (totalStudents == 0) {

                        // No eligible students
                        processLectures(
                                lectures,
                                lectureIndex + 1,
                                presentTotal,
                                absentTotal
                        );

                        return;
                    }

                    // -------------------------------------------------
                    // LOAD ATTENDANCE FOR THIS LECTURE
                    // -------------------------------------------------

                    db.collection("attendance")
                            .whereEqualTo(
                                    "lectureId",
                                    lectureId
                            )
                            .get()
                            .addOnSuccessListener(
                                    attendanceSnapshots -> {

                                        int present =
                                                0;

                                        /*
                                         * Count only PRESENT records.
                                         */

                                        for (
                                                DocumentSnapshot attendance
                                                : attendanceSnapshots
                                                .getDocuments()
                                        ) {

                                            String studentId =
                                                    getValue(
                                                            attendance,
                                                            "studentId"
                                                    );

                                            String status =
                                                    getValue(
                                                            attendance,
                                                            "status"
                                                    );

                                            if (
                                                    "PRESENT"
                                                            .equalsIgnoreCase(
                                                                    status
                                                            )
                                                            && eligibleStudents
                                                            .contains(
                                                                    studentId
                                                            )
                                            ) {

                                                present++;
                                            }
                                        }

                                        int absent =
                                                totalStudents
                                                        - present;

                                        processLectures(
                                                lectures,
                                                lectureIndex + 1,
                                                presentTotal
                                                        + present,
                                                absentTotal
                                                        + absent
                                        );
                                    }
                            )
                            .addOnFailureListener(
                                    e -> {

                                        /*
                                         * If attendance query fails,
                                         * move to next lecture instead
                                         * of crashing the Reports screen.
                                         */

                                        processLectures(
                                                lectures,
                                                lectureIndex + 1,
                                                presentTotal,
                                                absentTotal
                                        );
                                    });
                }
        );
    }

    // =========================================================
    // LOAD ELIGIBLE STUDENTS
    // =========================================================

    private void loadEligibleStudents(
            List<String> targetClassIds,
            EligibleStudentsCallback callback
    ) {

        if (targetClassIds == null) {
            callback.onResult(
                    new HashSet<>()
            );
            return;
        }

        if (targetClassIds.isEmpty()) {

            callback.onResult(
                    new HashSet<>()
            );

            return;
        }

        db.collection("users")
                .whereEqualTo(
                        "role",
                        "student"
                )
                .get()
                .addOnSuccessListener(
                        studentSnapshots -> {

                            Set<String> eligibleStudents =
                                    new HashSet<>();

                            for (
                                    DocumentSnapshot student
                                    : studentSnapshots.getDocuments()
                            ) {

                                String studentClassId =
                                        getValue(
                                                student,
                                                "classId"
                                        );

                                if (
                                        studentClassId != null
                                                && targetClassIds
                                                .contains(
                                                        studentClassId
                                                )
                                ) {

                                    eligibleStudents.add(
                                            student.getId()
                                    );
                                }
                            }

                            callback.onResult(
                                    eligibleStudents
                            );
                        }
                )
                .addOnFailureListener(
                        e -> callback.onResult(
                                new HashSet<>()
                        )
                );
    }

    // =========================================================
    // CALLBACK
    // =========================================================

    private interface EligibleStudentsCallback {

        void onResult(
                Set<String> studentIds
        );
    }

    // =========================================================
    // UPDATE UI
    // =========================================================

    private void updateReportUI() {

        if (!isAdded()) {
            return;
        }

        // -----------------------------------------------------
        // TOTAL LECTURES
        // -----------------------------------------------------

        tvTotalLectures.setText(
                String.valueOf(
                        totalLectures
                )
        );

        // -----------------------------------------------------
        // PRESENT
        // -----------------------------------------------------

        tvTotalPresent.setText(
                String.valueOf(
                        totalPresent
                )
        );

        // -----------------------------------------------------
        // ABSENT
        // -----------------------------------------------------

        tvTotalAbsent.setText(
                String.valueOf(
                        totalAbsent
                )
        );

        // -----------------------------------------------------
        // PERCENTAGE
        // -----------------------------------------------------

        int totalAttendance =
                totalPresent + totalAbsent;

        double percentage = 0;

        if (totalAttendance > 0) {

            percentage =
                    (
                            totalPresent * 100.0
                    )
                            / totalAttendance;
        }

        tvOverallPercentage.setText(
                String.format(
                        java.util.Locale.getDefault(),
                        "%.1f%%",
                        percentage
                )
        );

        // -----------------------------------------------------
        // PROGRESS BAR
        // -----------------------------------------------------

        progressOverallAttendance.setMax(
                100
        );

        progressOverallAttendance.setProgress(
                (int) Math.round(
                        percentage
                )
        );
    }

    // =========================================================
    // DEFAULT VALUES
    // =========================================================

    private void setDefaultValues() {

        tvTotalLectures.setText("0");
        tvTotalPresent.setText("0");
        tvTotalAbsent.setText("0");
        tvOverallPercentage.setText("0%");

        progressOverallAttendance.setMax(100);
        progressOverallAttendance.setProgress(0);
    }

    // =========================================================
    // CLICK LISTENERS
    // =========================================================

    private void setupClickListeners() {

        cardTodayReport.setOnClickListener(v -> {

            Intent intent = new Intent(
                    requireContext(),
                    TeacherTodayReportActivity.class
            );

            startActivity(intent);
        });

        cardWeekReport.setOnClickListener(v -> {

            Intent intent = new Intent(
                    requireContext(),
                    TeacherWeeklyReportActivity.class
            );

            startActivity(intent);
        });

        cardMonthReport.setOnClickListener(v -> {

            Intent intent = new Intent(
                    requireContext(),
                    TeacherMonthlyReportActivity.class
            );

            startActivity(intent);
        });

        cardSubjectReport.setOnClickListener(
                v -> {

                    Toast.makeText(
                            requireContext(),
                            "Subject-wise report will be added next.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );

        cardClassReport.setOnClickListener(
                v -> {

                    Toast.makeText(
                            requireContext(),
                            "Class-wise report will be added next.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );

        btnViewAllReports.setOnClickListener(v -> {

            Intent intent = new Intent(
                    requireContext(),
                    TeacherAllReportsActivity.class
            );

            startActivity(intent);
        });
    }

    // =========================================================
    // GET STRING LIST
    // =========================================================

    private List<String> getStringList(
            Object value
    ) {

        List<String> result =
                new ArrayList<>();

        if (!(value instanceof List)) {
            return result;
        }

        List<?> list =
                (List<?>) value;

        for (Object item : list) {

            if (item != null) {

                String text =
                        String.valueOf(item).trim();

                if (!text.isEmpty()) {

                    result.add(text);
                }
            }
        }

        return result;
    }

    // =========================================================
    // SAFE FIRESTORE VALUE
    // =========================================================

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

    // =========================================================
    // ERROR
    // =========================================================

    private void showError(
            String message
    ) {

        if (!isAdded()) {
            return;
        }

        Toast.makeText(
                requireContext(),
                message,
                Toast.LENGTH_LONG
        ).show();
    }
}