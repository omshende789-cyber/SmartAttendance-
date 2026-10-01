package com.omshende.smartattendance.teacher;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.omshende.smartattendance.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TeacherMonthlyReportActivity extends AppCompatActivity {

    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // =========================================================
    // UI
    // =========================================================

    private RecyclerView recyclerMonthlyReports;
    private ProgressBar progressMonthlyReports;
    private TextView tvNoMonthlyReports;
    private TextView tvMonthlyTitle;

    // =========================================================
    // ADAPTER
    // =========================================================

    private TeacherReportsAdapter adapter;

    // =========================================================
    // REPORT LIST
    // =========================================================

    private final List<TeacherReport> reportList =
            new ArrayList<>();

    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_teacher_monthly_report
        );

        // =====================================================
        // FIREBASE
        // =====================================================

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // =====================================================
        // VIEWS
        // =====================================================

        recyclerMonthlyReports =
                findViewById(
                        R.id.recyclerMonthlyReports
                );

        progressMonthlyReports =
                findViewById(
                        R.id.progressMonthlyReports
                );

        tvNoMonthlyReports =
                findViewById(
                        R.id.tvNoMonthlyReports
                );

        tvMonthlyTitle =
                findViewById(
                        R.id.tvMonthlyTitle
                );

        // =====================================================
        // RECYCLER VIEW
        // =====================================================

        recyclerMonthlyReports.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adapter =
                new TeacherReportsAdapter(
                        reportList
                );

        recyclerMonthlyReports.setAdapter(
                adapter
        );

        // =====================================================
        // SET MONTH TITLE
        // =====================================================

        tvMonthlyTitle.setText(
                new SimpleDateFormat(
                        "MMMM yyyy",
                        Locale.getDefault()
                ).format(
                        new Date()
                ) + " Attendance"
        );

        // =====================================================
        // LOAD REPORT
        // =====================================================

        loadMonthlyReports();
    }

    // =========================================================
    // LOAD MONTHLY REPORTS
    // =========================================================

    private void loadMonthlyReports() {

        if (auth.getCurrentUser() == null) {

            showNoReports(
                    "Teacher session not found."
            );

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
                .whereEqualTo(
                        "status",
                        "CLOSED"
                )
                .get()
                .addOnSuccessListener(
                        snapshots -> {

                            List<DocumentSnapshot> monthlyLectures =
                                    new ArrayList<>();

                            for (
                                    DocumentSnapshot lecture
                                    : snapshots.getDocuments()
                            ) {

                                if (isThisMonth(lecture)) {

                                    monthlyLectures.add(
                                            lecture
                                    );
                                }
                            }

                            // -------------------------------------------------
                            // NO LECTURES
                            // -------------------------------------------------

                            if (
                                    monthlyLectures.isEmpty()
                            ) {

                                showNoReports(
                                        "No closed lectures found this month."
                                );

                                return;
                            }

                            // -------------------------------------------------
                            // CLEAR OLD DATA
                            // -------------------------------------------------

                            reportList.clear();

                            // -------------------------------------------------
                            // PROCESS
                            // -------------------------------------------------

                            processLectures(
                                    monthlyLectures,
                                    0
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            showLoading(false);

                            Toast.makeText(
                                    this,
                                    "Unable to load monthly reports: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();

                            showNoReports(
                                    "Unable to load monthly reports."
                            );
                        }
                );
    }

    // =========================================================
    // PROCESS LECTURES
    // =========================================================

    private void processLectures(
            List<DocumentSnapshot> lectures,
            int index
    ) {

        // -----------------------------------------------------
        // ALL LECTURES PROCESSED
        // -----------------------------------------------------

        if (index >= lectures.size()) {

            // Newest first
            reportList.sort(
                    (a, b) ->
                            b.getDate().compareTo(
                                    a.getDate()
                            )
            );

            adapter.setData(
                    reportList
            );

            showLoading(false);

            if (reportList.isEmpty()) {

                showNoReports(
                        "No attendance data available."
                );

            } else {

                tvNoMonthlyReports.setVisibility(
                        View.GONE
                );

                recyclerMonthlyReports.setVisibility(
                        View.VISIBLE
                );
            }

            return;
        }

        // -----------------------------------------------------
        // CURRENT LECTURE
        // -----------------------------------------------------

        DocumentSnapshot lecture =
                lectures.get(index);

        String lectureId =
                lecture.getId();

        // -----------------------------------------------------
        // TARGET CLASS IDS
        // -----------------------------------------------------

        List<String> targetClassIds =
                getStringList(
                        lecture.get("targetClassIds")
                );

        // -----------------------------------------------------
        // BACKWARD COMPATIBILITY
        // -----------------------------------------------------

        if (targetClassIds.isEmpty()) {

            String classId =
                    getValue(
                            lecture,
                            "classId"
                    );

            if (!classId.isEmpty()) {

                targetClassIds.add(
                        classId
                );
            }
        }

        // =====================================================
        // GET ALL STUDENTS
        // =====================================================

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

                            // -------------------------------------------------
                            // FIND ELIGIBLE STUDENTS
                            // -------------------------------------------------

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
                                        targetClassIds.contains(
                                                studentClassId
                                        )
                                ) {

                                    eligibleStudents.add(
                                            student.getId()
                                    );
                                }
                            }

                            int totalStudents =
                                    eligibleStudents.size();

                            // =================================================
                            // LOAD ATTENDANCE
                            // =================================================

                            db.collection("attendance")
                                    .whereEqualTo(
                                            "lectureId",
                                            lectureId
                                    )
                                    .get()
                                    .addOnSuccessListener(
                                            attendanceSnapshots -> {

                                                int present = 0;

                                                Set<String>
                                                        countedStudents =
                                                        new HashSet<>();

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

                                                    // -------------------------------------------------
                                                    // COUNT PRESENT
                                                    // -------------------------------------------------

                                                    if (
                                                            "PRESENT"
                                                                    .equalsIgnoreCase(
                                                                            status
                                                                    )
                                                                    && eligibleStudents
                                                                    .contains(
                                                                            studentId
                                                                    )
                                                                    && !countedStudents
                                                                    .contains(
                                                                            studentId
                                                                    )
                                                    ) {

                                                        countedStudents.add(
                                                                studentId
                                                        );

                                                        present++;
                                                    }
                                                }

                                                // -------------------------------------------------
                                                // ABSENT
                                                // -------------------------------------------------

                                                int absent =
                                                        totalStudents
                                                                - present;

                                                // -------------------------------------------------
                                                // PERCENTAGE
                                                // -------------------------------------------------

                                                double percentage =
                                                        0;

                                                if (
                                                        totalStudents > 0
                                                ) {

                                                    percentage =
                                                            present
                                                                    * 100.0
                                                                    / totalStudents;
                                                }

                                                // =================================================
                                                // CREATE REPORT
                                                // =================================================

                                                TeacherReport report =
                                                        new TeacherReport();

                                                report.setLectureId(
                                                        lectureId
                                                );

                                                report.setSubjectName(
                                                        getValue(
                                                                lecture,
                                                                "subjectName"
                                                        )
                                                );

                                                report.setClassName(
                                                        getClassNames(
                                                                lecture
                                                        )
                                                );

                                                report.setRoomNo(
                                                        getValue(
                                                                lecture,
                                                                "roomNo"
                                                        )
                                                );

                                                report.setDate(
                                                        getDate(
                                                                lecture
                                                        )
                                                );

                                                report.setTotalStudents(
                                                        totalStudents
                                                );

                                                report.setPresentStudents(
                                                        present
                                                );

                                                report.setAbsentStudents(
                                                        absent
                                                );

                                                report.setAttendancePercentage(
                                                        percentage
                                                );

                                                reportList.add(
                                                        report
                                                );

                                                // -------------------------------------------------
                                                // NEXT LECTURE
                                                // -------------------------------------------------

                                                processLectures(
                                                        lectures,
                                                        index + 1
                                                );
                                            }
                                    )
                                    .addOnFailureListener(
                                            e ->
                                                    processLectures(
                                                            lectures,
                                                            index + 1
                                                    )
                                    );
                        }
                )
                .addOnFailureListener(
                        e ->
                                processLectures(
                                        lectures,
                                        index + 1
                                )
                );
    }

    // =========================================================
    // CHECK THIS MONTH
    // =========================================================

    private boolean isThisMonth(
            DocumentSnapshot lecture
    ) {

        Object value =
                lecture.get("createdAt");

        if (!(value instanceof Timestamp)) {

            return false;
        }

        Date lectureDate =
                ((Timestamp) value).toDate();

        Calendar lectureCalendar =
                Calendar.getInstance();

        lectureCalendar.setTime(
                lectureDate
        );

        Calendar today =
                Calendar.getInstance();

        return lectureCalendar.get(
                Calendar.YEAR
        ) == today.get(
                Calendar.YEAR
        )
                &&
                lectureCalendar.get(
                        Calendar.MONTH
                ) == today.get(
                        Calendar.MONTH
                );
    }

    // =========================================================
    // CLASS NAMES
    // =========================================================

    private String getClassNames(
            DocumentSnapshot lecture
    ) {

        Object value =
                lecture.get("targetClassNames");

        if (value instanceof List) {

            List<?> list =
                    (List<?>) value;

            StringBuilder builder =
                    new StringBuilder();

            for (Object item : list) {

                if (item == null) {
                    continue;
                }

                String name =
                        String.valueOf(
                                item
                        ).trim();

                if (name.isEmpty()) {
                    continue;
                }

                if (builder.length() > 0) {

                    builder.append(
                            " + "
                    );
                }

                builder.append(
                        name
                );
            }

            if (builder.length() > 0) {

                return builder.toString();
            }
        }

        return getValue(
                lecture,
                "className"
        );
    }

    // =========================================================
    // DATE
    // =========================================================

    private String getDate(
            DocumentSnapshot lecture
    ) {

        Object value =
                lecture.get("createdAt");

        if (value instanceof Timestamp) {

            Date date =
                    ((Timestamp) value).toDate();

            return new SimpleDateFormat(
                    "dd MMM yyyy, hh:mm a",
                    Locale.getDefault()
            ).format(date);
        }

        return "Date not available";
    }

    // =========================================================
    // STRING LIST
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
                        String.valueOf(
                                item
                        ).trim();

                if (!text.isEmpty()) {

                    result.add(
                            text
                    );
                }
            }
        }

        return result;
    }

    // =========================================================
    // FIRESTORE VALUE
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
    // LOADING
    // =========================================================

    private void showLoading(
            boolean loading
    ) {

        if (loading) {

            progressMonthlyReports.setVisibility(
                    View.VISIBLE
            );

            recyclerMonthlyReports.setVisibility(
                    View.GONE
            );

            tvNoMonthlyReports.setVisibility(
                    View.GONE
            );

        } else {

            progressMonthlyReports.setVisibility(
                    View.GONE
            );
        }
    }

    // =========================================================
    // NO REPORT
    // =========================================================

    private void showNoReports(
            String message
    ) {

        showLoading(false);

        tvNoMonthlyReports.setText(
                message
        );

        tvNoMonthlyReports.setVisibility(
                View.VISIBLE
        );

        recyclerMonthlyReports.setVisibility(
                View.GONE
        );
    }
}