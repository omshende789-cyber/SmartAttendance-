package com.omshende.smartattendance.teacher;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.omshende.smartattendance.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TeacherLectureAttendanceActivity
        extends AppCompatActivity {

    // =====================================================
    // Firebase
    // =====================================================

    private FirebaseFirestore db;


    // =====================================================
    // Lecture
    // =====================================================

    private String lectureId;


    // =====================================================
    // RecyclerView
    // =====================================================

    private RecyclerView recyclerAttendance;
    private ProgressBar progressAttendance;

    private TeacherLectureAttendanceAdapter adapter;


    // =====================================================
    // Lecture Information
    // =====================================================

    private TextView tvSubject;
    private TextView tvClass;
    private TextView tvRoom;
    private TextView tvDate;


    // =====================================================
    // Attendance Counts
    // =====================================================

    private TextView tvPresentCount;
    private TextView tvAbsentCount;
    private TextView tvTotalCount;
    private TextView tvNoAttendance;


    // =====================================================
    // Search
    // =====================================================

    private TextInputEditText etSearchAttendance;


    // =====================================================
    // Filters
    // =====================================================

    private MaterialButton btnFilterAll;
    private MaterialButton btnFilterPresent;
    private MaterialButton btnFilterAbsent;

    private String currentFilter = "ALL";


    // =====================================================
    // Activity Data
    // =====================================================

    private final List<TeacherAttendance> attendanceList =
            new ArrayList<>();


    // =====================================================
    // Counts
    // =====================================================

    private int presentCount = 0;
    private int absentCount = 0;
    private int totalCount = 0;


    // =====================================================
    // ON CREATE
    // =====================================================

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_teacher_lecture_attendance
        );


        // Firebase
        db =
                FirebaseFirestore.getInstance();


        // =================================================
        // Get Lecture ID
        // =================================================

        lectureId =
                getIntent().getStringExtra(
                        "lectureId"
                );


        if (
                lectureId == null
                        || lectureId.trim().isEmpty()
        ) {

            Toast.makeText(
                    this,
                    "Lecture ID not found",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }


        // =================================================
        // Initialize
        // =================================================

        initViews();

        setupRecyclerView();

        setupSearchAndFilters();

        loadLecture();
    }


    // =====================================================
    // Initialize Views
    // =====================================================

    private void initViews() {

        // -------------------------------------------------
        // Toolbar
        // -------------------------------------------------

        Toolbar toolbar =
                findViewById(
                        R.id.toolbarTeacherAttendance
                );


        setSupportActionBar(
                toolbar
        );


        if (
                getSupportActionBar()
                        != null
        ) {

            getSupportActionBar()
                    .setDisplayHomeAsUpEnabled(
                            true
                    );

            getSupportActionBar()
                    .setTitle(
                            "Lecture Attendance"
                    );
        }


        toolbar.setNavigationOnClickListener(
                v -> finish()
        );


        // -------------------------------------------------
        // Lecture Info
        // -------------------------------------------------

        tvSubject =
                findViewById(
                        R.id.tvAttendanceSubject
                );

        tvClass =
                findViewById(
                        R.id.tvAttendanceClass
                );

        tvRoom =
                findViewById(
                        R.id.tvAttendanceRoom
                );

        tvDate =
                findViewById(
                        R.id.tvAttendanceDate
                );


        // -------------------------------------------------
        // Counts
        // -------------------------------------------------

        tvPresentCount =
                findViewById(
                        R.id.tvPresentCount
                );

        tvAbsentCount =
                findViewById(
                        R.id.tvAbsentCount
                );

        tvTotalCount =
                findViewById(
                        R.id.tvTotalCount
                );


        tvNoAttendance =
                findViewById(
                        R.id.tvNoAttendance
                );


        // -------------------------------------------------
        // RecyclerView
        // -------------------------------------------------

        recyclerAttendance =
                findViewById(
                        R.id.recyclerTeacherAttendance
                );

        progressAttendance =
                findViewById(
                        R.id.progressTeacherAttendance
                );


        // -------------------------------------------------
        // Search
        // -------------------------------------------------

        etSearchAttendance =
                findViewById(
                        R.id.etSearchAttendance
                );


        // -------------------------------------------------
        // Filters
        // -------------------------------------------------

        btnFilterAll =
                findViewById(
                        R.id.btnFilterAll
                );

        btnFilterPresent =
                findViewById(
                        R.id.btnFilterPresent
                );

        btnFilterAbsent =
                findViewById(
                        R.id.btnFilterAbsent
                );
    }


    // =====================================================
    // RecyclerView Setup
    // =====================================================

    private void setupRecyclerView() {

        recyclerAttendance.setLayoutManager(
                new LinearLayoutManager(
                        this
                )
        );


        recyclerAttendance.setHasFixedSize(
                false
        );


        adapter =
                new TeacherLectureAttendanceAdapter(
                        attendanceList
                );


        recyclerAttendance.setAdapter(
                adapter
        );
    }


    // =====================================================
    // Search + Filters
    // =====================================================

    private void setupSearchAndFilters() {

        // -------------------------------------------------
        // Search
        // -------------------------------------------------

        etSearchAttendance.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }


                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {

                        applyFilter();
                    }


                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
        );


        // -------------------------------------------------
        // ALL
        // -------------------------------------------------

        btnFilterAll.setOnClickListener(
                v -> {

                    currentFilter =
                            "ALL";

                    applyFilter();
                }
        );


        // -------------------------------------------------
        // PRESENT
        // -------------------------------------------------

        btnFilterPresent.setOnClickListener(
                v -> {

                    currentFilter =
                            "PRESENT";

                    applyFilter();
                }
        );


        // -------------------------------------------------
        // ABSENT
        // -------------------------------------------------

        btnFilterAbsent.setOnClickListener(
                v -> {

                    currentFilter =
                            "ABSENT";

                    applyFilter();
                }
        );
    }


    // =====================================================
    // Apply Filter
    // =====================================================

    private void applyFilter() {

        if (adapter == null) {
            return;
        }


        String query = "";


        if (
                etSearchAttendance
                        != null
        ) {

            query =
                    etSearchAttendance
                            .getText()
                            .toString();
        }


        adapter.filter(
                query,
                currentFilter
        );


        // -------------------------------------------------
        // Empty result check
        // -------------------------------------------------

        int visibleCount =
                adapter.getItemCount();


        if (visibleCount == 0) {

            recyclerAttendance.setVisibility(
                    View.GONE
            );

            tvNoAttendance.setVisibility(
                    View.VISIBLE
            );


            if (
                    currentFilter
                            .equals("PRESENT")
            ) {

                tvNoAttendance.setText(
                        "No present students found"
                );

            } else if (
                    currentFilter
                            .equals("ABSENT")
            ) {

                tvNoAttendance.setText(
                        "No absent students found"
                );

            } else {

                String search =
                        query.trim();


                if (!search.isEmpty()) {

                    tvNoAttendance.setText(
                            "No student found for \""
                                    + search
                                    + "\""
                    );

                } else {

                    tvNoAttendance.setText(
                            "No students found"
                    );
                }
            }

        } else {

            recyclerAttendance.setVisibility(
                    View.VISIBLE
            );

            tvNoAttendance.setVisibility(
                    View.GONE
            );
        }
    }


    // =====================================================
    // Load Lecture
    // =====================================================

    private void loadLecture() {

        showLoading(true);


        db.collection("lectures")
                .document(lectureId)
                .get()
                .addOnSuccessListener(
                        document -> {

                            if (
                                    !document.exists()
                            ) {

                                showLoading(false);


                                Toast.makeText(
                                        this,
                                        "Lecture not found",
                                        Toast.LENGTH_LONG
                                ).show();


                                finish();

                                return;
                            }


                            // ---------------------------------
                            // Subject
                            // ---------------------------------

                            String subject =
                                    getValue(
                                            document,
                                            "subjectName"
                                    );


                            // ---------------------------------
                            // Class
                            // ---------------------------------

                            String className =
                                    getClassNames(
                                            document
                                    );


                            // ---------------------------------
                            // Room
                            // ---------------------------------

                            String room =
                                    getValue(
                                            document,
                                            "roomNo"
                                    );


                            tvSubject.setText(
                                    safe(subject)
                            );


                            tvClass.setText(
                                    safe(className)
                            );


                            tvRoom.setText(
                                    "Room: "
                                            + safe(room)
                            );


                            tvDate.setText(
                                    getLectureDate(
                                            document
                                    )
                            );


                            // ---------------------------------
                            // Load students
                            // ---------------------------------

                            loadStudents(
                                    document
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            showLoading(false);


                            Toast.makeText(
                                    this,
                                    "Failed to load lecture: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }


    // =====================================================
    // Load Eligible Students
    // =====================================================

    private void loadStudents(
            DocumentSnapshot lecture
    ) {

        List<String> targetClassIds =
                getTargetClassIds(
                        lecture
                );


        // -------------------------------------------------
        // No class found
        // -------------------------------------------------

        if (
                targetClassIds.isEmpty()
        ) {

            showLoading(false);


            recyclerAttendance.setVisibility(
                    View.GONE
            );


            tvNoAttendance.setVisibility(
                    View.VISIBLE
            );


            tvNoAttendance.setText(
                    "No target class found"
            );


            return;
        }


        Set<String> targetClasses =
                new HashSet<>(
                        targetClassIds
                );


        // -------------------------------------------------
        // Get all students
        // -------------------------------------------------

        db.collection("users")
                .whereEqualTo(
                        "role",
                        "student"
                )
                .get()
                .addOnSuccessListener(
                        querySnapshot -> {

                            List<DocumentSnapshot>
                                    students =
                                    new ArrayList<>();


                            // ---------------------------------
                            // Filter by classId
                            // ---------------------------------

                            for (
                                    DocumentSnapshot student
                                    : querySnapshot
                                    .getDocuments()
                            ) {

                                String classId =
                                        getValue(
                                                student,
                                                "classId"
                                        );


                                if (
                                        targetClasses
                                                .contains(
                                                        classId
                                                )
                                ) {

                                    students.add(
                                            student
                                    );
                                }
                            }


                            // ---------------------------------
                            // Load attendance
                            // ---------------------------------

                            loadAttendance(
                                    students
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            showLoading(false);


                            Toast.makeText(
                                    this,
                                    "Failed to load students: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }


    // =====================================================
    // Load Attendance
    // =====================================================

    private void loadAttendance(
            List<DocumentSnapshot> students
    ) {

        db.collection("attendance")
                .whereEqualTo(
                        "lectureId",
                        lectureId
                )
                .get()
                .addOnSuccessListener(
                        querySnapshot -> {

                            Set<String>
                                    presentStudents =
                                    new HashSet<>();


                            // ---------------------------------
                            // Find Present Students
                            // ---------------------------------

                            for (
                                    DocumentSnapshot attendance
                                    : querySnapshot
                                    .getDocuments()
                            ) {

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
                                ) {

                                    String studentId =
                                            getValue(
                                                    attendance,
                                                    "studentId"
                                            );


                                    if (
                                            !studentId
                                                    .isEmpty()
                                    ) {

                                        presentStudents.add(
                                                studentId
                                        );
                                    }
                                }
                            }


                            // ---------------------------------
                            // Build final list
                            // ---------------------------------

                            buildAttendanceList(
                                    students,
                                    presentStudents
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            showLoading(false);


                            Toast.makeText(
                                    this,
                                    "Failed to load attendance: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }


    // =====================================================
    // Build Attendance List
    // =====================================================

    private void buildAttendanceList(
            List<DocumentSnapshot> students,
            Set<String> presentStudents
    ) {

        // -------------------------------------------------
        // Temporary list
        // -------------------------------------------------

        List<TeacherAttendance>
                newAttendanceList =
                new ArrayList<>();


        presentCount = 0;
        absentCount = 0;


        // -------------------------------------------------
        // Build Present / Absent
        // -------------------------------------------------

        for (
                DocumentSnapshot student
                : students
        ) {

            String studentId =
                    student.getId();


            String name =
                    getValue(
                            student,
                            "name"
                    );


            String enrollment =
                    getValue(
                            student,
                            "enrollmentNo"
                    );


            String division =
                    getValue(
                            student,
                            "division"
                    );


            int rollNumber =
                    getIntValue(
                            student,
                            "rollNumber"
                    );


            String status;


            if (
                    presentStudents
                            .contains(
                                    studentId
                            )
            ) {

                status =
                        "PRESENT";

                presentCount++;

            } else {

                status =
                        "ABSENT";

                absentCount++;
            }


            newAttendanceList.add(
                    new TeacherAttendance(
                            studentId,
                            name,
                            enrollment,
                            division,
                            rollNumber,
                            status
                    )
            );
        }


        // -------------------------------------------------
        // Sort
        // -------------------------------------------------

        newAttendanceList.sort(
                Comparator.comparingInt(
                        TeacherAttendance
                                ::getRollNumber
                )
        );


        // -------------------------------------------------
        // Total
        // -------------------------------------------------

        totalCount =
                newAttendanceList.size();


        // -------------------------------------------------
        // Update Activity list
        // -------------------------------------------------

        attendanceList.clear();

        attendanceList.addAll(
                newAttendanceList
        );


        // -------------------------------------------------
        // Update Adapter
        // -------------------------------------------------

        adapter.setData(
                newAttendanceList
        );


        // -------------------------------------------------
        // Counts
        // -------------------------------------------------

        updateCounts();


        showLoading(false);


        // -------------------------------------------------
        // Apply current filter
        // -------------------------------------------------

        applyFilter();
    }


    // =====================================================
    // Update Counts
    // =====================================================

    private void updateCounts() {

        tvPresentCount.setText(
                "Present\n"
                        + presentCount
        );


        tvAbsentCount.setText(
                "Absent\n"
                        + absentCount
        );


        tvTotalCount.setText(
                "Total\n"
                        + totalCount
        );
    }


    // =====================================================
    // Get Target Class IDs
    // =====================================================

    private List<String> getTargetClassIds(
            DocumentSnapshot lecture
    ) {

        List<String> result =
                new ArrayList<>();


        Object value =
                lecture.get(
                        "targetClassIds"
                );


        // -------------------------------------------------
        // New lecture format
        // -------------------------------------------------

        if (
                value instanceof List
        ) {

            List<?> list =
                    (List<?>) value;


            for (
                    Object item
                    : list
            ) {

                if (item != null) {

                    result.add(
                            String.valueOf(
                                    item
                            )
                    );
                }
            }
        }


        // -------------------------------------------------
        // Old lecture format
        // -------------------------------------------------

        if (
                result.isEmpty()
        ) {

            String oldClassId =
                    getValue(
                            lecture,
                            "classId"
                    );


            if (
                    !oldClassId.isEmpty()
            ) {

                result.add(
                        oldClassId
                );
            }
        }


        return result;
    }


    // =====================================================
    // Get Class Names
    // =====================================================

    private String getClassNames(
            DocumentSnapshot lecture
    ) {

        Object value =
                lecture.get(
                        "targetClassNames"
                );


        if (
                value instanceof List
        ) {

            List<?> list =
                    (List<?>) value;


            if (
                    !list.isEmpty()
            ) {

                StringBuilder builder =
                        new StringBuilder();


                for (
                        int i = 0;
                        i < list.size();
                        i++
                ) {

                    if (i > 0) {

                        builder.append(
                                " + "
                        );
                    }


                    if (
                            list.get(i)
                                    != null
                    ) {

                        builder.append(
                                String.valueOf(
                                        list.get(i)
                                )
                        );
                    }
                }


                String result =
                        builder.toString()
                                .trim();


                if (
                        !result.isEmpty()
                ) {

                    return result;
                }
            }
        }


        return getValue(
                lecture,
                "className"
        );
    }


    // =====================================================
    // Lecture Date
    // =====================================================

    private String getLectureDate(
            DocumentSnapshot lecture
    ) {

        Object value =
                lecture.get(
                        "createdAt"
                );


        if (
                value instanceof Timestamp
        ) {

            Date date =
                    ((Timestamp) value)
                            .toDate();


            SimpleDateFormat format =
                    new SimpleDateFormat(
                            "dd MMM yyyy, hh:mm a",
                            Locale.getDefault()
                    );


            return format.format(
                    date
            );
        }


        return "Date not available";
    }


    // =====================================================
    // Firestore Value
    // =====================================================

    private String getValue(
            DocumentSnapshot document,
            String field
    ) {

        Object value =
                document.get(
                        field
                );


        if (value == null) {

            return "";
        }


        if (
                value instanceof Number
        ) {

            Number number =
                    (Number) value;


            double d =
                    number.doubleValue();


            long l =
                    number.longValue();


            if (d == l) {

                return String.valueOf(
                        l
                );
            }


            return String.valueOf(
                    d
            );
        }


        return String.valueOf(
                value
        );
    }


    // =====================================================
    // Integer Value
    // =====================================================

    private int getIntValue(
            DocumentSnapshot document,
            String field
    ) {

        Object value =
                document.get(
                        field
                );


        if (
                value instanceof Number
        ) {

            return ((Number) value)
                    .intValue();
        }


        try {

            return Integer.parseInt(
                    String.valueOf(
                            value
                    )
            );

        } catch (Exception e) {

            return 0;
        }
    }


    // =====================================================
    // Safe Display
    // =====================================================

    private String safe(
            String value
    ) {

        if (
                value == null
                        || value.trim().isEmpty()
        ) {

            return "-";
        }


        return value.trim();
    }


    // =====================================================
    // Loading
    // =====================================================

    private void showLoading(
            boolean loading
    ) {

        if (
                progressAttendance == null
        ) {

            return;
        }


        progressAttendance.setVisibility(
                loading
                        ? View.VISIBLE
                        : View.GONE
        );
    }
}