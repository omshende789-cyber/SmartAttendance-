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

public class TeacherWeeklyReportActivity extends AppCompatActivity {

    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // =========================================================
    // UI
    // =========================================================

    private RecyclerView recyclerWeeklyReports;
    private ProgressBar progressWeeklyReports;
    private TextView tvNoWeeklyReports;
    private TextView tvWeeklyTitle;

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
                R.layout.activity_teacher_weekly_report
        );

        // =====================================================
        // FIREBASE
        // =====================================================

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // =====================================================
        // VIEWS
        // =====================================================

        recyclerWeeklyReports =
                findViewById(
                        R.id.recyclerWeeklyReports
                );

        progressWeeklyReports =
                findViewById(
                        R.id.progressWeeklyReports
                );

        tvNoWeeklyReports =
                findViewById(
                        R.id.tvNoWeeklyReports
                );

        tvWeeklyTitle =
                findViewById(
                        R.id.tvWeeklyTitle
                );

        // =====================================================
        // RECYCLER VIEW
        // =====================================================

        recyclerWeeklyReports.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adapter =
                new TeacherReportsAdapter(
                        reportList
                );

        recyclerWeeklyReports.setAdapter(
                adapter
        );

        // =====================================================
        // LOAD REPORT
        // =====================================================

        loadWeeklyReports();
    }

    // =========================================================
    // LOAD WEEKLY REPORT
    // =========================================================

    private void loadWeeklyReports() {

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

                            List<DocumentSnapshot> weeklyLectures =
                                    new ArrayList<>();

                            for (
                                    DocumentSnapshot lecture
                                    : snapshots.getDocuments()
                            ) {

                                if (isThisWeek(lecture)) {

                                    weeklyLectures.add(
                                            lecture
                                    );
                                }
                            }

                            // -------------------------------------------------
                            // NO LECTURES
                            // -------------------------------------------------

                            if (
                                    weeklyLectures.isEmpty()
                            ) {

                                showNoReports(
                                        "No closed lectures found this week."
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
                                    weeklyLectures,
                                    0
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            showLoading(false);

                            Toast.makeText(
                                    this,
                                    "Unable to load weekly reports: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();

                            showNoReports(
                                    "Unable to load weekly reports."
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

                tvNoWeeklyReports.setVisibility(
                        View.GONE
                );

                recyclerWeeklyReports.setVisibility(
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
        // TARGET CLASSES
        // -----------------------------------------------------

        List<String> targetClassIds =
                getStringList(
                        lecture.get("targetClassIds")
                );

        // -----------------------------------------------------
        // OLD LECTURE SUPPORT
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
        // GET STUDENTS
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
                            // GET ATTENDANCE
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
                                                        totalStudents
                                                                > 0
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
    // CHECK THIS WEEK
    // =========================================================

    private boolean isThisWeek(
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

        /*
         * Set Monday as first day of week.
         */

        today.setFirstDayOfWeek(
                Calendar.MONDAY
        );

        lectureCalendar.setFirstDayOfWeek(
                Calendar.MONDAY
        );

        /*
         * Calculate start of current week.
         */

        Calendar weekStart =
                (Calendar) today.clone();

        weekStart.set(
                Calendar.DAY_OF_WEEK,
                Calendar.MONDAY
        );

        weekStart.set(
                Calendar.HOUR_OF_DAY,
                0
        );

        weekStart.set(
                Calendar.MINUTE,
                0
        );

        weekStart.set(
                Calendar.SECOND,
                0
        );

        weekStart.set(
                Calendar.MILLISECOND,
                0
        );

        /*
         * Calculate end of today.
         */

        Calendar tomorrow =
                (Calendar) today.clone();

        tomorrow.add(
                Calendar.DAY_OF_YEAR,
                1
        );

        tomorrow.set(
                Calendar.HOUR_OF_DAY,
                0
        );

        tomorrow.set(
                Calendar.MINUTE,
                0
        );

        tomorrow.set(
                Calendar.SECOND,
                0
        );

        tomorrow.set(
                Calendar.MILLISECOND,
                0
        );

        return !lectureDate.before(
                weekStart.getTime()
        )
                &&
                lectureDate.before(
                        tomorrow.getTime()
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
                        String.valueOf(item).trim();

                if (name.isEmpty()) {
                    continue;
                }

                if (builder.length() > 0) {

                    builder.append(
                            " + "
                    );
                }

                builder.append(name);
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

                    result.add(text);
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

            progressWeeklyReports.setVisibility(
                    View.VISIBLE
            );

            recyclerWeeklyReports.setVisibility(
                    View.GONE
            );

            tvNoWeeklyReports.setVisibility(
                    View.GONE
            );

        } else {

            progressWeeklyReports.setVisibility(
                    View.GONE
            );
        }
    }

    // =========================================================
    // NO REPORTS
    // =========================================================

    private void showNoReports(
            String message
    ) {

        showLoading(false);

        tvNoWeeklyReports.setText(
                message
        );

        tvNoWeeklyReports.setVisibility(
                View.VISIBLE
        );

        recyclerWeeklyReports.setVisibility(
                View.GONE
        );
    }
}