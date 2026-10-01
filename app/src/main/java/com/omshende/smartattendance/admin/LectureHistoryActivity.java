package com.omshende.smartattendance.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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
import com.omshende.smartattendance.model.LectureHistory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LectureHistoryActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;

    private TextView tvStudentName;
    private TextView tvHistoryCount;
    private TextView tvNoHistory;

    private RecyclerView recyclerLectureHistory;

    private ProgressBar progressLoading;

    private FirebaseFirestore db;

    private String studentId;
    private String studentClassId = "";

    private final ArrayList<LectureHistory> historyList =
            new ArrayList<>();

    private LectureHistoryAdapter adapter;

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_lecture_history
        );

        db = FirebaseFirestore.getInstance();

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

        loadStudentAndHistory();
    }

    // =========================================================
    // INITIALIZE
    // =========================================================

    private void initializeViews() {

        toolbar =
                findViewById(
                        R.id.toolbar
                );

        tvStudentName =
                findViewById(
                        R.id.tvStudentName
                );

        tvHistoryCount =
                findViewById(
                        R.id.tvHistoryCount
                );

        tvNoHistory =
                findViewById(
                        R.id.tvNoHistory
                );

        recyclerLectureHistory =
                findViewById(
                        R.id.recyclerLectureHistory
                );

        progressLoading =
                findViewById(
                        R.id.progressLoading
                );

        recyclerLectureHistory.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerLectureHistory.setNestedScrollingEnabled(
                false
        );

        adapter =
                new LectureHistoryAdapter(
                        historyList
                );

        recyclerLectureHistory.setAdapter(
                adapter
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

    private void loadStudentAndHistory() {

        showLoading(true);

        db.collection("users")
                .document(studentId)
                .get()
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {

                        showLoading(false);

                        Toast.makeText(
                                LectureHistoryActivity.this,
                                "Unable to load student.",
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
                                LectureHistoryActivity.this,
                                "Student not found.",
                                Toast.LENGTH_LONG
                        ).show();

                        finish();

                        return;
                    }

                    String name =
                            getValue(
                                    student,
                                    "name"
                            );

                    if (TextUtils.isEmpty(name)) {
                        name = "Student";
                    }

                    tvStudentName.setText(name);

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

                        showNoHistory();

                        return;
                    }

                    loadLectureHistory();
                });
    }

    // =========================================================
    // LOAD LECTURES
    // =========================================================

    private void loadLectureHistory() {

        db.collection("lectures")
                .whereArrayContains(
                        "targetClassIds",
                        studentClassId
                )
                .get()
                .addOnCompleteListener(
                        lectureTask -> {

                            if (
                                    !lectureTask
                                            .isSuccessful()
                            ) {

                                showLoading(false);

                                Toast.makeText(
                                        LectureHistoryActivity.this,
                                        "Unable to load lecture history.",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            /*
                             * lectureId -> Lecture document
                             */

                            Map<String, DocumentSnapshot>
                                    lectureMap =
                                    new HashMap<>();

                            if (
                                    lectureTask
                                            .getResult()
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

                                    /*
                                     * Only CLOSED lectures
                                     * are completed lectures.
                                     */

                                    if (
                                            !"CLOSED"
                                                    .equalsIgnoreCase(
                                                            status
                                                    )
                                    ) {

                                        continue;
                                    }

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
                                            !TextUtils.isEmpty(
                                                    lectureId
                                            )
                                    ) {

                                        lectureMap.put(
                                                lectureId,
                                                lecture
                                        );
                                    }
                                }
                            }

                            if (lectureMap.isEmpty()) {

                                showLoading(false);

                                showNoHistory();

                                return;
                            }

                            loadAttendanceRecords(
                                    lectureMap
                            );
                        }
                );
    }

    // =========================================================
    // LOAD ATTENDANCE
    // =========================================================

    private void loadAttendanceRecords(
            Map<String, DocumentSnapshot> lectureMap
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
                                        LectureHistoryActivity.this,
                                        "Unable to load attendance records.",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            /*
                             * Store present lecture IDs.
                             */

                            Set<String> presentLectureIds =
                                    new HashSet<>();

                            if (
                                    attendanceTask
                                            .getResult()
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

                                    if (
                                            "PRESENT"
                                                    .equalsIgnoreCase(
                                                            status
                                                    )
                                                    &&
                                                    lectureMap.containsKey(
                                                            lectureId
                                                    )
                                    ) {

                                        presentLectureIds.add(
                                                lectureId
                                        );
                                    }
                                }
                            }

                            buildHistory(
                                    lectureMap,
                                    presentLectureIds
                            );
                        }
                );
    }

    // =========================================================
    // BUILD HISTORY
    // =========================================================

    private void buildHistory(
            Map<String, DocumentSnapshot> lectureMap,
            Set<String> presentLectureIds
    ) {

        historyList.clear();

        for (
                Map.Entry<String, DocumentSnapshot> entry
                : lectureMap.entrySet()
        ) {

            String lectureId =
                    entry.getKey();

            DocumentSnapshot lecture =
                    entry.getValue();

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
                        "Unknown Subject";
            }

            String teacherName =
                    getValue(
                            lecture,
                            "teacherName"
                    );

            if (
                    TextUtils.isEmpty(
                            teacherName
                    )
            ) {

                teacherName =
                        "Teacher not available";
            }

            String roomNo =
                    getValue(
                            lecture,
                            "roomNo"
                    );

            if (
                    TextUtils.isEmpty(
                            roomNo
                    )
            ) {

                roomNo =
                        "Room not available";
            }

            String className =
                    getValue(
                            lecture,
                            "className"
                    );

            if (
                    TextUtils.isEmpty(
                            className
                    )
            ) {

                className =
                        "Class not available";
            }

            Date lectureDate =
                    getLectureDate(lecture);

            boolean present =
                    presentLectureIds.contains(
                            lectureId
                    );

            String attendanceStatus =
                    present
                            ? "PRESENT"
                            : "ABSENT";

            LectureHistory history =
                    new LectureHistory(
                            lectureId,
                            subjectName,
                            teacherName,
                            roomNo,
                            className,
                            lectureDate,
                            attendanceStatus
                    );

            historyList.add(history);
        }

        /*
         * Latest lecture first.
         */

        Collections.sort(
                historyList,
                new Comparator<LectureHistory>() {

                    @Override
                    public int compare(
                            LectureHistory first,
                            LectureHistory second
                    ) {

                        Date firstDate =
                                first.getLectureDate();

                        Date secondDate =
                                second.getLectureDate();

                        if (
                                firstDate == null
                                        && secondDate == null
                        ) {

                            return 0;
                        }

                        if (firstDate == null) {
                            return 1;
                        }

                        if (secondDate == null) {
                            return -1;
                        }

                        return secondDate.compareTo(
                                firstDate
                        );
                    }
                }
        );

        adapter.notifyDataSetChanged();

        if (historyList.isEmpty()) {

            showNoHistory();

        } else {

            recyclerLectureHistory
                    .setVisibility(
                            RecyclerView.VISIBLE
                    );

            tvNoHistory.setVisibility(
                    View.GONE
            );

            tvHistoryCount.setText(
                    historyList.size()
                            + " Lectures"
            );
        }
    }

    // =========================================================
    // GET LECTURE DATE
    // =========================================================

    private Date getLectureDate(
            DocumentSnapshot lecture
    ) {

        /*
         * First try common timestamp fields.
         */

        Object timestamp =
                lecture.get("timestamp");

        if (timestamp instanceof com.google.firebase.Timestamp) {

            return (
                    (
                            com.google.firebase.Timestamp
                            ) timestamp
            ).toDate();
        }

        timestamp =
                lecture.get("createdAt");

        if (timestamp instanceof com.google.firebase.Timestamp) {

            return (
                    (
                            com.google.firebase.Timestamp
                            ) timestamp
            ).toDate();
        }

        timestamp =
                lecture.get("date");

        if (timestamp instanceof com.google.firebase.Timestamp) {

            return (
                    (
                            com.google.firebase.Timestamp
                            ) timestamp
            ).toDate();
        }

        return null;
    }

    // =========================================================
    // NO HISTORY
    // =========================================================

    private void showNoHistory() {

        historyList.clear();

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        recyclerLectureHistory.setVisibility(
                RecyclerView.GONE
        );

        tvNoHistory.setVisibility(
                View.VISIBLE
        );

        tvHistoryCount.setText(
                "0 Lectures"
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

        progressLoading.setVisibility(
                loading
                        ? View.VISIBLE
                        : View.GONE
        );
    }
}