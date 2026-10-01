package com.omshende.smartattendance.teacher;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import com.omshende.smartattendance.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TeacherAllReportsActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private RecyclerView recyclerAllReports;
    private ProgressBar progressAllReports;
    private TextView tvNoAllReports;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private TeacherReportsAdapter adapter;

    private final List<TeacherReport> reportList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_all_reports);

        initializeFirebase();
        initializeViews();
        setupToolbar();
        setupRecyclerView();

        loadAllReports();
    }

    private void initializeFirebase() {

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void initializeViews() {

        toolbar = findViewById(R.id.toolbarAllReports);
        recyclerAllReports = findViewById(R.id.recyclerAllReports);
        progressAllReports = findViewById(R.id.progressAllReports);
        tvNoAllReports = findViewById(R.id.tvNoAllReports);
    }

    private void setupToolbar() {

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {

        recyclerAllReports.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adapter = new TeacherReportsAdapter(reportList);

        recyclerAllReports.setAdapter(adapter);
    }

    private void loadAllReports() {

        if (auth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Teacher login required",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        String teacherId = auth.getCurrentUser().getUid();

        showLoading(true);

        db.collection("lectures")
                .whereEqualTo("teacherId", teacherId)
                .whereEqualTo("status", "CLOSED")
                .get()
                .addOnSuccessListener(lectureSnapshot -> {

                    if (lectureSnapshot.isEmpty()) {

                        reportList.clear();
                        adapter.setData(reportList);

                        showLoading(false);
                        showNoReports(true);

                        return;
                    }

                    reportList.clear();

                    List<DocumentSnapshot> lectures =
                            new ArrayList<>(lectureSnapshot.getDocuments());

                    // Latest lectures first
                    Collections.sort(
                            lectures,
                            (d1, d2) -> {

                                Date date1 = getLectureDateObject(d1);
                                Date date2 = getLectureDateObject(d2);

                                if (date1 == null && date2 == null) {
                                    return 0;
                                }

                                if (date1 == null) {
                                    return 1;
                                }

                                if (date2 == null) {
                                    return -1;
                                }

                                return date2.compareTo(date1);
                            }
                    );

                    processLectures(
                            lectures,
                            0
                    );

                })
                .addOnFailureListener(e -> {

                    showLoading(false);

                    Toast.makeText(
                            this,
                            "Failed to load reports",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void processLectures(
            List<DocumentSnapshot> lectures,
            int index
    ) {

        if (index >= lectures.size()) {

            adapter.setData(reportList);

            showLoading(false);

            showNoReports(reportList.isEmpty());

            return;
        }

        DocumentSnapshot lecture = lectures.get(index);

        String lectureId = lecture.getId();

        List<String> targetClassIds =
                getStringList(lecture, "targetClassIds");

        // Backward compatibility
        if (targetClassIds.isEmpty()) {

            String oldClassId =
                    getValue(lecture, "classId");

            if (!oldClassId.isEmpty()) {
                targetClassIds.add(oldClassId);
            }
        }

        if (targetClassIds.isEmpty()) {

            processLectures(
                    lectures,
                    index + 1
            );

            return;
        }

        Set<String> targetClassSet =
                new HashSet<>(targetClassIds);

        db.collection("users")
                .whereEqualTo("role", "student")
                .get()
                .addOnSuccessListener(studentSnapshot -> {

                    List<String> eligibleStudentIds =
                            new ArrayList<>();

                    for (DocumentSnapshot student :
                            studentSnapshot.getDocuments()) {

                        String studentClassId =
                                getValue(student, "classId");

                        if (targetClassSet.contains(studentClassId)) {

                            eligibleStudentIds.add(
                                    student.getId()
                            );
                        }
                    }

                    int totalStudents =
                            eligibleStudentIds.size();

                    loadAttendance(
                            lecture,
                            eligibleStudentIds,
                            totalStudents,
                            lectures,
                            index
                    );

                })
                .addOnFailureListener(e -> {

                    processLectures(
                            lectures,
                            index + 1
                    );
                });
    }

    private void loadAttendance(
            DocumentSnapshot lecture,
            List<String> eligibleStudentIds,
            int totalStudents,
            List<DocumentSnapshot> lectures,
            int index
    ) {

        String lectureId = lecture.getId();

        db.collection("attendance")
                .whereEqualTo("lectureId", lectureId)
                .get()
                .addOnSuccessListener(attendanceSnapshot -> {

                    Set<String> eligibleSet =
                            new HashSet<>(eligibleStudentIds);

                    Set<String> presentStudents =
                            new HashSet<>();

                    for (DocumentSnapshot attendance :
                            attendanceSnapshot.getDocuments()) {

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

                        if (eligibleSet.contains(studentId)
                                && "PRESENT".equalsIgnoreCase(status)) {

                            presentStudents.add(studentId);
                        }
                    }

                    int presentStudentsCount =
                            presentStudents.size();

                    int absentStudentsCount =
                            Math.max(
                                    0,
                                    totalStudents
                                            - presentStudentsCount
                            );

                    double percentage = 0;

                    if (totalStudents > 0) {

                        percentage =
                                (presentStudentsCount * 100.0)
                                        / totalStudents;
                    }

                    String subjectName =
                            getValue(
                                    lecture,
                                    "subjectName"
                            );

                    String className =
                            getClassNames(lecture);

                    String roomNo =
                            getValue(
                                    lecture,
                                    "roomNo"
                            );

                    String date =
                            getLectureDate(lecture);

                    TeacherReport report =
                            new TeacherReport(
                                    lectureId,
                                    subjectName,
                                    className,
                                    roomNo,
                                    date,
                                    totalStudents,
                                    presentStudentsCount,
                                    absentStudentsCount,
                                    percentage
                            );

                    reportList.add(report);

                    processLectures(
                            lectures,
                            index + 1
                    );

                })
                .addOnFailureListener(e -> {

                    processLectures(
                            lectures,
                            index + 1
                    );
                });
    }

    private String getClassNames(
            DocumentSnapshot document
    ) {

        List<String> classNames =
                getStringList(
                        document,
                        "targetClassNames"
                );

        if (!classNames.isEmpty()) {

            return android.text.TextUtils.join(
                    " + ",
                    classNames
            );
        }

        String className =
                getValue(
                        document,
                        "className"
                );

        if (!className.isEmpty()) {
            return className;
        }

        return "Class not available";
    }

    private List<String> getStringList(
            DocumentSnapshot document,
            String field
    ) {

        List<String> result =
                new ArrayList<>();

        Object value =
                document.get(field);

        if (value instanceof List<?>) {

            for (Object item :
                    (List<?>) value) {

                if (item != null) {
                    result.add(
                            String.valueOf(item)
                    );
                }
            }
        }

        return result;
    }

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

    private String getLectureDate(
            DocumentSnapshot document
    ) {

        Date date =
                getLectureDateObject(document);

        if (date == null) {
            return "Date not available";
        }

        SimpleDateFormat format =
                new SimpleDateFormat(
                        "dd MMM yyyy, hh:mm a",
                        Locale.getDefault()
                );

        return format.format(date);
    }

    private Date getLectureDateObject(
            DocumentSnapshot document
    ) {

        Object createdAt =
                document.get("createdAt");

        if (createdAt instanceof com.google.firebase.Timestamp) {

            return ((com.google.firebase.Timestamp) createdAt)
                    .toDate();
        }

        Object timestamp =
                document.get("timestamp");

        if (timestamp instanceof com.google.firebase.Timestamp) {

            return ((com.google.firebase.Timestamp) timestamp)
                    .toDate();
        }

        return null;
    }

    private void showLoading(boolean show) {

        progressAllReports.setVisibility(
                show
                        ? View.VISIBLE
                        : View.GONE
        );

        recyclerAllReports.setVisibility(
                show
                        ? View.GONE
                        : View.VISIBLE
        );
    }

    private void showNoReports(boolean show) {

        tvNoAllReports.setVisibility(
                show
                        ? View.VISIBLE
                        : View.GONE
        );

        recyclerAllReports.setVisibility(
                show
                        ? View.GONE
                        : View.VISIBLE
        );
    }
}