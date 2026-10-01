package com.omshende.smartattendance.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.omshende.smartattendance.R;
import com.omshende.smartattendance.model.SubjectAttendance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.google.android.material.card.MaterialCardView;

public class StudentDetailsActivity extends AppCompatActivity {

    // =========================================================
    // TOOLBAR
    // =========================================================

    private MaterialToolbar toolbar;

    // =========================================================
    // STUDENT PROFILE
    // =========================================================

    private TextView tvStudentInitial;
    private TextView tvStudentName;
    private TextView tvStudentEmail;

    private TextView tvEnrollment;
    private TextView tvDepartment;
    private TextView tvClass;
    private TextView tvYear;
    private TextView tvDivision;
    private TextView tvRollNumber;

   // private com.google.android.material.card.MaterialCardView
          //  tvViewLectureHistory;
    // =========================================================
    // OVERALL ATTENDANCE
    // =========================================================
    private MaterialCardView tvViewLectureHistory;
    private TextView tvTotalLectures;
    private TextView tvPresent;
    private TextView tvAbsent;
    private TextView tvAttendancePercentage;

    // =========================================================
    // SUBJECT ATTENDANCE
    // =========================================================

    private RecyclerView recyclerSubjectAttendance;
    private TextView tvNoSubjectAttendance;

    private SubjectAttendanceAdapter subjectAttendanceAdapter;

    private final ArrayList<SubjectAttendance>
            subjectAttendanceList =
            new ArrayList<>();

    // =========================================================
    // LOADING
    // =========================================================

    private ProgressBar progressLoading;

    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseFirestore db;

    // =========================================================
    // STUDENT
    // =========================================================

    private String studentId;
    private String studentClassId = "";

    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_student_details
        );

        db =
                FirebaseFirestore.getInstance();

        initializeViews();

        setupToolbar();

        studentId =
                getIntent().getStringExtra(
                        "studentId"
                );

        if (TextUtils.isEmpty(studentId)) {

            Toast.makeText(
                    this,
                    "Student information not found.",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }

        loadStudentDetails();
    }


    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initializeViews() {

        tvViewLectureHistory =
                findViewById(
                        R.id.tvViewLectureHistory
                );

        toolbar =
                findViewById(
                        R.id.toolbar
                );

        tvStudentInitial =
                findViewById(
                        R.id.tvStudentInitial
                );

        tvStudentName =
                findViewById(
                        R.id.tvStudentName
                );

        tvStudentEmail =
                findViewById(
                        R.id.tvStudentEmail
                );

        tvEnrollment =
                findViewById(
                        R.id.tvEnrollment
                );

        tvDepartment =
                findViewById(
                        R.id.tvDepartment
                );

        tvClass =
                findViewById(
                        R.id.tvClass
                );

        tvYear =
                findViewById(
                        R.id.tvYear
                );

        tvDivision =
                findViewById(
                        R.id.tvDivision
                );

        tvRollNumber =
                findViewById(
                        R.id.tvRollNumber
                );

        tvTotalLectures =
                findViewById(
                        R.id.tvTotalLectures
                );

        tvPresent =
                findViewById(
                        R.id.tvPresent
                );

        tvAbsent =
                findViewById(
                        R.id.tvAbsent
                );

        tvAttendancePercentage =
                findViewById(
                        R.id.tvAttendancePercentage
                );

        progressLoading =
                findViewById(
                        R.id.progressLoading
                );

        recyclerSubjectAttendance =
                findViewById(
                        R.id.recyclerSubjectAttendance
                );

        tvNoSubjectAttendance =
                findViewById(
                        R.id.tvNoSubjectAttendance
                );

        // RecyclerView setup
        recyclerSubjectAttendance.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerSubjectAttendance.setNestedScrollingEnabled(
                false
        );

        subjectAttendanceAdapter =
                new SubjectAttendanceAdapter(
                        subjectAttendanceList
                );

        recyclerSubjectAttendance.setAdapter(
                subjectAttendanceAdapter
        );
    }

    // =========================================================
    // TOOLBAR
    // =========================================================

    private void setupToolbar() {

        toolbar.setNavigationOnClickListener(
                v -> finish()
        );
    }

    // =========================================================
    // LOAD STUDENT
    // =========================================================

    private void loadStudentDetails() {

        showLoading(true);

        db.collection("users")
                .document(studentId)
                .get()
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {

                        showLoading(false);

                        Toast.makeText(
                                StudentDetailsActivity.this,
                                "Unable to load student details.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    DocumentSnapshot student =
                            task.getResult();

                    if (
                            student == null
                                    || !student.exists()
                    ) {

                        showLoading(false);

                        Toast.makeText(
                                StudentDetailsActivity.this,
                                "Student not found.",
                                Toast.LENGTH_LONG
                        ).show();

                        finish();

                        return;
                    }

                    // Display profile
                    displayStudent(student);

                    // Get classId
                    studentClassId =
                            getValue(
                                    student,
                                    "classId"
                            );

                    if (
                            TextUtils.isEmpty(
                                    studentClassId
                            )
                    ) {

                        showLoading(false);

                        setAttendanceValues(
                                0,
                                0,
                                0,
                                0
                        );

                        showNoSubjectAttendance();

                        return;
                    }

                    // Load exact attendance
                    loadExactAttendance();
                });
    }


    // =========================================================
    // DISPLAY STUDENT
    // =========================================================

    private void displayStudent(
            DocumentSnapshot student
    ) {

        String name =
                getValue(
                        student,
                        "name"
                );

        if (
                TextUtils.isEmpty(name)
        ) {

            name =
                    "Unknown Student";
        }

        tvStudentName.setText(
                name
        );

        tvStudentInitial.setText(
                getInitial(name)
        );

        String email =
                getValue(
                        student,
                        "email"
                );

        tvStudentEmail.setText(
                displayValue(
                        email,
                        "Email not available"
                )
        );

        String enrollment =
                getValue(
                        student,
                        "enrollmentNo"
                );

        tvEnrollment.setText(
                displayValue(
                        enrollment,
                        "Not available"
                )
        );

        String department =
                getValue(
                        student,
                        "department"
                );

        tvDepartment.setText(
                displayValue(
                        department,
                        "Not available"
                )
        );

        String className =
                getValue(
                        student,
                        "className"
                );

        tvClass.setText(
                displayValue(
                        className,
                        "Not available"
                )
        );

        String year =
                getValue(
                        student,
                        "year"
                );

        tvYear.setText(
                displayValue(
                        year,
                        "Not available"
                )
        );

        String division =
                getValue(
                        student,
                        "division"
                );

        tvDivision.setText(
                displayValue(
                        division,
                        "Not available"
                )
        );

        String rollNumber =
                getValue(
                        student,
                        "rollNumber"
                );

        tvRollNumber.setText(
                displayValue(
                        rollNumber,
                        "Not available"
                )
        );
        tvViewLectureHistory.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    StudentDetailsActivity.this,
                                    LectureHistoryActivity.class
                            );

                    intent.putExtra(
                            "studentId",
                            studentId
                    );

                    startActivity(intent);
                }
        );
    }

    // =========================================================
    // EXACT ATTENDANCE
    // =========================================================

    private void loadExactAttendance() {

        /*
         * Find all lectures which target
         * this student's class.
         */

        db.collection("lectures")
                .whereArrayContains(
                        "targetClassIds",
                        studentClassId
                )
                .get()
                .addOnCompleteListener(
                        lectureTask -> {

                            if (
                                    !lectureTask.isSuccessful()
                            ) {

                                showLoading(false);

                                Toast.makeText(
                                        StudentDetailsActivity.this,
                                        "Unable to load lectures.",
                                        Toast.LENGTH_LONG
                                ).show();

                                setAttendanceValues(
                                        0,
                                        0,
                                        0,
                                        0
                                );

                                showNoSubjectAttendance();

                                return;
                            }

                            /*
                             * lectureId -> subjectName
                             */

                            Map<String, String>
                                    lectureSubjectMap =
                                    new HashMap<>();

                            /*
                             * subjectName -> total lectures
                             */

                            Map<String, Integer>
                                    subjectTotalMap =
                                    new HashMap<>();

                            /*
                             * Only closed lectures
                             * are conducted lectures.
                             */

                            if (
                                    lectureTask.getResult()
                                            != null
                            ) {

                                for (
                                        DocumentSnapshot lecture
                                        : lectureTask
                                        .getResult()
                                        .getDocuments()
                                ) {

                                    String status =
                                            getValue(
                                                    lecture,
                                                    "status"
                                            );

                                    if (
                                            !"CLOSED"
                                                    .equalsIgnoreCase(
                                                            status
                                                    )
                                    ) {

                                        continue;
                                    }

                                    // Get lecture ID
                                    String lectureId =
                                            lecture.getId();

                                    String storedLectureId =
                                            getValue(
                                                    lecture,
                                                    "lectureId"
                                            );

                                    if (
                                            !TextUtils.isEmpty(
                                                    storedLectureId
                                            )
                                    ) {

                                        lectureId =
                                                storedLectureId;
                                    }

                                    if (
                                            TextUtils.isEmpty(
                                                    lectureId
                                            )
                                    ) {

                                        continue;
                                    }

                                    // Get subject name
                                    String subjectName =
                                            getValue(
                                                    lecture,
                                                    "subjectName"
                                            );

                                    if (
                                            TextUtils.isEmpty(
                                                    subjectName
                                            )
                                    ) {

                                        subjectName =
                                                getValue(
                                                        lecture,
                                                        "subjectId"
                                                );
                                    }

                                    if (
                                            TextUtils.isEmpty(
                                                    subjectName
                                            )
                                    ) {

                                        subjectName =
                                                "Unknown Subject";
                                    }

                                    /*
                                     * Save lecture -> subject
                                     */

                                    lectureSubjectMap.put(
                                            lectureId,
                                            subjectName
                                    );

                                    /*
                                     * Increase total lecture
                                     * count for subject.
                                     */

                                    int oldCount =
                                            subjectTotalMap
                                                    .containsKey(
                                                            subjectName
                                                    )
                                                    ? subjectTotalMap
                                                    .get(
                                                            subjectName
                                                    )
                                                    : 0;

                                    subjectTotalMap.put(
                                            subjectName,
                                            oldCount + 1
                                    );
                                }
                            }

                            /*
                             * No lectures
                             */

                            if (
                                    lectureSubjectMap.isEmpty()
                            ) {

                                showLoading(false);

                                setAttendanceValues(
                                        0,
                                        0,
                                        0,
                                        0
                                );

                                showNoSubjectAttendance();

                                return;
                            }

                            /*
                             * Now get this student's
                             * attendance records.
                             */

                            loadStudentAttendance(
                                    lectureSubjectMap,
                                    subjectTotalMap
                            );
                        }
                );
    }

    // =========================================================
    // LOAD ATTENDANCE RECORDS
    // =========================================================

    private void loadStudentAttendance(
            Map<String, String> lectureSubjectMap,
            Map<String, Integer> subjectTotalMap
    ) {

        db.collection("attendance")
                .whereEqualTo(
                        "studentId",
                        studentId
                )
                .get()
                .addOnCompleteListener(
                        attendanceTask -> {

                            showLoading(false);

                            if (
                                    !attendanceTask
                                            .isSuccessful()
                            ) {

                                Toast.makeText(
                                        StudentDetailsActivity.this,
                                        "Unable to load attendance records.",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            /*
                             * subject -> present count
                             */

                            Map<String, Integer>
                                    subjectPresentMap =
                                    new HashMap<>();

                            /*
                             * Overall present
                             */

                            int overallPresent = 0;

                            /*
                             * Prevent duplicate counting
                             */

                            Set<String>
                                    countedLectureIds =
                                    new HashSet<>();

                            if (
                                    attendanceTask.getResult()
                                            != null
                            ) {

                                for (
                                        DocumentSnapshot attendance
                                        : attendanceTask
                                        .getResult()
                                        .getDocuments()
                                ) {

                                    String lectureId =
                                            getValue(
                                                    attendance,
                                                    "lectureId"
                                            );

                                    String status =
                                            getValue(
                                                    attendance,
                                                    "status"
                                            );

                                    /*
                                     * Lecture must belong to
                                     * student's eligible lectures.
                                     */

                                    if (
                                            !lectureSubjectMap
                                                    .containsKey(
                                                            lectureId
                                                    )
                                    ) {

                                        continue;
                                    }

                                    /*
                                     * Only PRESENT counts.
                                     */

                                    if (
                                            !"PRESENT"
                                                    .equalsIgnoreCase(
                                                            status
                                                    )
                                    ) {

                                        continue;
                                    }

                                    /*
                                     * Same lecture should
                                     * never count twice.
                                     */

                                    if (
                                            countedLectureIds
                                                    .contains(
                                                            lectureId
                                                    )
                                    ) {

                                        continue;
                                    }

                                    countedLectureIds.add(
                                            lectureId
                                    );

                                    String subjectName =
                                            lectureSubjectMap
                                                    .get(
                                                            lectureId
                                                    );

                                    if (
                                            TextUtils.isEmpty(
                                                    subjectName
                                            )
                                    ) {

                                        subjectName =
                                                "Unknown Subject";
                                    }

                                    int oldPresent =
                                            subjectPresentMap
                                                    .containsKey(
                                                            subjectName
                                                    )
                                                    ? subjectPresentMap
                                                    .get(
                                                            subjectName
                                                    )
                                                    : 0;

                                    subjectPresentMap.put(
                                            subjectName,
                                            oldPresent + 1
                                    );

                                    overallPresent++;
                                }
                            }

                            /*
                             * Overall total
                             */

                            int overallTotal = 0;

                            for (
                                    Integer total
                                    : subjectTotalMap.values()
                            ) {

                                if (total != null) {

                                    overallTotal += total;
                                }
                            }

                            /*
                             * Overall absent
                             */

                            int overallAbsent =
                                    overallTotal
                                            - overallPresent;

                            if (
                                    overallAbsent < 0
                            ) {

                                overallAbsent = 0;
                            }

                            /*
                             * Overall percentage
                             */

                            double overallPercentage =
                                    0;

                            if (
                                    overallTotal > 0
                            ) {

                                overallPercentage =
                                        (
                                                overallPresent
                                                        * 100.0
                                        )
                                                / overallTotal;
                            }

                            /*
                             * Set overall values
                             */

                            setAttendanceValues(
                                    overallTotal,
                                    overallPresent,
                                    overallAbsent,
                                    overallPercentage
                            );

                            /*
                             * Build subject list
                             */

                            buildSubjectAttendance(
                                    subjectTotalMap,
                                    subjectPresentMap
                            );
                        }
                );
    }

    // =========================================================
    // BUILD SUBJECT ATTENDANCE
    // =========================================================

    private void buildSubjectAttendance(
            Map<String, Integer> subjectTotalMap,
            Map<String, Integer> subjectPresentMap
    ) {

        subjectAttendanceList.clear();

        for (
                Map.Entry<String, Integer> entry
                : subjectTotalMap.entrySet()
        ) {

            String subjectName =
                    entry.getKey();

            int total =
                    entry.getValue() != null
                            ? entry.getValue()
                            : 0;

            int present =
                    subjectPresentMap.containsKey(
                            subjectName
                    )
                            ? subjectPresentMap.get(
                            subjectName
                    )
                            : 0;

            int absent =
                    total - present;

            if (absent < 0) {
                absent = 0;
            }

            double percentage = 0;

            if (total > 0) {

                percentage =
                        (
                                present
                                        * 100.0
                        )
                                / total;
            }

            SubjectAttendance subject =
                    new SubjectAttendance(
                            subjectName,
                            total,
                            present,
                            absent,
                            percentage
                    );

            subjectAttendanceList.add(
                    subject
            );
        }

        /*
         * Sort subjects alphabetically
         * for a clean UI.
         */

        subjectAttendanceList.sort(
                (first, second) ->
                        first.getSubjectName()
                                .compareToIgnoreCase(
                                        second.getSubjectName()
                                )
        );

        subjectAttendanceAdapter.notifyDataSetChanged();

        if (
                subjectAttendanceList.isEmpty()
        ) {

            showNoSubjectAttendance();

        } else {

            recyclerSubjectAttendance
                    .setVisibility(
                            RecyclerView.VISIBLE
                    );

            tvNoSubjectAttendance
                    .setVisibility(
                            TextView.GONE
                    );
        }
    }

    // =========================================================
    // NO SUBJECT ATTENDANCE
    // =========================================================

    private void showNoSubjectAttendance() {

        subjectAttendanceList.clear();

        if (
                subjectAttendanceAdapter != null
        ) {

            subjectAttendanceAdapter
                    .notifyDataSetChanged();
        }

        recyclerSubjectAttendance
                .setVisibility(
                        RecyclerView.GONE
                );

        tvNoSubjectAttendance
                .setVisibility(
                        TextView.VISIBLE
                );
    }

    // =========================================================
    // SET OVERALL ATTENDANCE
    // =========================================================

    private void setAttendanceValues(
            int total,
            int present,
            int absent,
            double percentage
    ) {

        tvTotalLectures.setText(
                String.valueOf(total)
        );

        tvPresent.setText(
                String.valueOf(present)
        );

        tvAbsent.setText(
                String.valueOf(absent)
        );

        tvAttendancePercentage.setText(
                String.format(
                        Locale.getDefault(),
                        "%.1f%%",
                        percentage
                )
        );
    }

    // =========================================================
    // SAFE FIRESTORE VALUE
    // =========================================================

    private String getValue(
            DocumentSnapshot document,
            String field
    ) {

        if (
                document == null
                        || field == null
                        || field.trim().isEmpty()
        ) {

            return "";
        }

        Object value =
                document.get(field);

        if (value == null) {

            return "";
        }

        /*
         * Firestore Number support.
         *
         * This is important because
         * rollNumber may be Number.
         */

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
    // DISPLAY VALUE
    // =========================================================

    private String displayValue(
            String value,
            String fallback
    ) {

        if (
                TextUtils.isEmpty(value)
        ) {

            return fallback;
        }

        return value;
    }

    // =========================================================
    // INITIAL
    // =========================================================

    private String getInitial(
            String name
    ) {

        if (
                TextUtils.isEmpty(name)
        ) {

            return "S";
        }

        String cleanName =
                name.trim();

        if (
                cleanName.isEmpty()
        ) {

            return "S";
        }

        return cleanName
                .substring(0, 1)
                .toUpperCase(
                        Locale.ROOT
                );
    }

    // =========================================================
    // LOADING
    // =========================================================

    private void showLoading(
            boolean loading
    ) {

        if (
                progressLoading == null
        ) {

            return;
        }

        progressLoading.setVisibility(
                loading
                        ? ProgressBar.VISIBLE
                        : ProgressBar.GONE
        );
    }
}