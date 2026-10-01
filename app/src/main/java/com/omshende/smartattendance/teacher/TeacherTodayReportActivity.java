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

public class TeacherTodayReportActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private RecyclerView recyclerTodayReports;
    private ProgressBar progressTodayReports;
    private TextView tvNoTodayReports;
    private TextView tvTodayTitle;

    private TeacherReportsAdapter adapter;

    private final List<TeacherReport> reportList =
            new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_teacher_today_report
        );

        // =====================================================
        // FIREBASE
        // =====================================================

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // =====================================================
        // VIEWS
        // =====================================================

        recyclerTodayReports =
                findViewById(
                        R.id.recyclerTodayReports
                );

        progressTodayReports =
                findViewById(
                        R.id.progressTodayReports
                );

        tvNoTodayReports =
                findViewById(
                        R.id.tvNoTodayReports
                );

        tvTodayTitle =
                findViewById(
                        R.id.tvTodayTitle
                );

        // =====================================================
        // RECYCLER VIEW
        // =====================================================

        recyclerTodayReports.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adapter =
                new TeacherReportsAdapter(
                        reportList
                );

        recyclerTodayReports.setAdapter(
                adapter
        );

        // =====================================================
        // LOAD REPORT
        // =====================================================

        loadTodayReports();
    }

    // =========================================================
    // LOAD TODAY REPORTS
    // =========================================================

    private void loadTodayReports() {

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

                            List<DocumentSnapshot> todayLectures =
                                    new ArrayList<>();

                            for (
                                    DocumentSnapshot lecture
                                    : snapshots.getDocuments()
                            ) {

                                if (isToday(lecture)) {

                                    todayLectures.add(
                                            lecture
                                    );
                                }
                            }

                            if (todayLectures.isEmpty()) {

                                showNoReports(
                                        "No closed lectures found for today."
                                );

                                return;
                            }

                            processLectures(
                                    todayLectures,
                                    0
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            showLoading(false);

                            Toast.makeText(
                                    this,
                                    "Unable to load reports: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();

                            showNoReports(
                                    "Unable to load today's reports."
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

        if (index >= lectures.size()) {

            // Sort newest first
            reportList.sort(
                    (a, b) ->
                            b.getDate()
                                    .compareTo(
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

                tvNoTodayReports.setVisibility(
                        View.GONE
                );

                recyclerTodayReports.setVisibility(
                        View.VISIBLE
                );
            }

            return;
        }

        DocumentSnapshot lecture =
                lectures.get(index);

        String lectureId =
                lecture.getId();

        List<String> targetClassIds =
                getStringList(
                        lecture.get("targetClassIds")
                );

        // Backward compatibility
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
        // GET ELIGIBLE STUDENTS
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
                                        targetClassIds
                                                .contains(
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
                            // ATTENDANCE
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

                                                    /*
                                                     * Count only:
                                                     *
                                                     * PRESENT
                                                     * + eligible student
                                                     * + once
                                                     */

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

                                                int absent =
                                                        totalStudents
                                                                - present;

                                                double percentage = 0;

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

                                                // Next lecture
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
    // CHECK TODAY
    // =========================================================

    private boolean isToday(
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
                        Calendar.DAY_OF_YEAR
                ) == today.get(
                        Calendar.DAY_OF_YEAR
                );
    }

    // =========================================================
    // GET CLASS NAMES
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

                    builder.append(" + ");
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
    // GET DATE
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
    // LOADING
    // =========================================================

    private void showLoading(
            boolean loading
    ) {

        if (loading) {

            progressTodayReports.setVisibility(
                    View.VISIBLE
            );

            recyclerTodayReports.setVisibility(
                    View.GONE
            );

            tvNoTodayReports.setVisibility(
                    View.GONE
            );

        } else {

            progressTodayReports.setVisibility(
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

        tvNoTodayReports.setText(
                message
        );

        tvNoTodayReports.setVisibility(
                View.VISIBLE
        );

        recyclerTodayReports.setVisibility(
                View.GONE
        );
    }
}