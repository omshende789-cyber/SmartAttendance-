package com.omshende.smartattendance.student;

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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

//import com.omshende.smartattendance.EnterOtpActivity;
import com.omshende.smartattendance.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class StudentDashboardFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private TextView tvStudentGreeting;
    private TextView tvStudentClass;

    private TextView tvOverallPercentage;
    private TextView tvAttendanceStatus;
    private TextView tvPresentCount;
    private TextView tvAbsentCount;

    private TextView tvActiveSubject;
    private TextView tvActiveTeacher;
    private TextView tvActiveClass;
    private TextView tvActiveRoom;

    private TextView tvTodayPresent;
    private TextView tvTodayAbsent;
    private TextView tvTodayTotal;

    private MaterialCardView cardActiveLecture;
    private MaterialButton btnEnterActiveOtp;

    private ProgressBar progressStudentDashboard;
    private ProgressBar progressOverallAttendance;

    private String studentId;
    private String studentClassId;

    private DocumentSnapshot activeLecture;

    private int overallPresent = 0;
    private int overallAbsent = 0;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View view = inflater.inflate(
                R.layout.fragment_student_dashboard,
                container,
                false
        );

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews(view);
        setupClicks();

        loadStudentProfile();

        return view;
    }

    private void initializeViews(View view) {

        tvStudentGreeting =
                view.findViewById(R.id.tvStudentGreeting);

        tvStudentClass =
                view.findViewById(R.id.tvStudentClass);

        tvOverallPercentage =
                view.findViewById(R.id.tvOverallPercentage);

        tvAttendanceStatus =
                view.findViewById(R.id.tvAttendanceStatus);

        tvPresentCount =
                view.findViewById(R.id.tvPresentCount);

        tvAbsentCount =
                view.findViewById(R.id.tvAbsentCount);

        tvActiveSubject =
                view.findViewById(R.id.tvActiveSubject);

        tvActiveTeacher =
                view.findViewById(R.id.tvActiveTeacher);

        tvActiveClass =
                view.findViewById(R.id.tvActiveClass);

        tvActiveRoom =
                view.findViewById(R.id.tvActiveRoom);

        tvTodayPresent =
                view.findViewById(R.id.tvTodayPresent);

        tvTodayAbsent =
                view.findViewById(R.id.tvTodayAbsent);

        tvTodayTotal =
                view.findViewById(R.id.tvTodayTotal);

        cardActiveLecture =
                view.findViewById(R.id.cardActiveLecture);

        btnEnterActiveOtp =
                view.findViewById(R.id.btnEnterActiveOtp);

        progressStudentDashboard =
                view.findViewById(R.id.progressStudentDashboard);

        progressOverallAttendance =
                view.findViewById(R.id.progressOverallAttendance);
    }

    private void setupClicks() {

        cardActiveLecture.setOnClickListener(v -> {

            openActiveLecture();
        });

        btnEnterActiveOtp.setOnClickListener(v -> {

            openActiveLecture();
        });
    }

    private void loadStudentProfile() {

        if (auth.getCurrentUser() == null) {

            showError("Student login required");
            return;
        }

        studentId =
                auth.getCurrentUser().getUid();

        showLoading(true);

        db.collection("users")
                .document(studentId)
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {

                        showLoading(false);
                        showError("Student profile not found");
                        return;
                    }

                    String name =
                            getValue(document, "name");

                    String className =
                            getValue(document, "className");

                    String department =
                            getValue(document, "department");

                    String year =
                            getValue(document, "year");

                    String division =
                            getValue(document, "division");

                    String roll =
                            getValue(document, "rollNumber");

                    studentClassId =
                            getValue(document, "classId");

                    setGreeting(name);

                    setStudentClass(
                            className,
                            department,
                            year,
                            division,
                            roll
                    );

                    if (studentClassId.isEmpty()) {

                        showLoading(false);
                        showError("Student class is not configured");
                        return;
                    }

                    loadOverallAttendance();
                    loadTodayAttendance();
                    loadActiveLecture();

                })
                .addOnFailureListener(e -> {

                    showLoading(false);

                    showError(
                            "Unable to load student profile"
                    );
                });
    }

    private void setGreeting(String name) {

        Calendar calendar =
                Calendar.getInstance();

        int hour =
                calendar.get(Calendar.HOUR_OF_DAY);

        String greeting;

        if (hour < 12) {
            greeting = "Good Morning";
        } else if (hour < 17) {
            greeting = "Good Afternoon";
        } else {
            greeting = "Good Evening";
        }

        if (name == null || name.trim().isEmpty()) {
            name = "Student";
        }

        tvStudentGreeting.setText(
                greeting + ", " + name + " 👋"
        );
    }

    private void setStudentClass(
            String className,
            String department,
            String year,
            String division,
            String roll
    ) {

        StringBuilder text =
                new StringBuilder();

        if (!className.isEmpty()) {

            text.append(className);

        } else {

            if (!year.isEmpty()) {
                text.append(year);
            }

            if (!department.isEmpty()) {

                if (text.length() > 0) {
                    text.append(" ");
                }

                text.append(department);
            }
        }

        if (!division.isEmpty()) {

            text.append(" • Division ");
            text.append(division);
        }

        if (!roll.isEmpty()) {

            text.append(" • Roll ");
            text.append(formatRoll(roll));
        }

        if (text.length() == 0) {
            text.append("Student");
        }

        tvStudentClass.setText(text.toString());
    }

    private String formatRoll(String roll) {

        try {

            int number =
                    Integer.parseInt(roll);

            return String.format(
                    Locale.getDefault(),
                    "%02d",
                    number
            );

        } catch (Exception e) {

            return roll;
        }
    }

    private void loadOverallAttendance() {

        db.collection("lectures")
                .whereArrayContains(
                        "targetClassIds",
                        studentClassId
                )
                .whereEqualTo(
                        "status",
                        "CLOSED"
                )
                .get()
                .addOnSuccessListener(lectureSnapshot -> {

                    if (lectureSnapshot.isEmpty()) {

                        overallPresent = 0;
                        overallAbsent = 0;

                        updateOverallAttendance();

                        return;
                    }

                    List<DocumentSnapshot> lectures =
                            lectureSnapshot.getDocuments();

                    loadAttendanceForLectures(
                            lectures,
                            0,
                            new HashSet<>()
                    );

                })
                .addOnFailureListener(e -> {

                    // Backward compatibility with
                    // old lecture documents.

                    loadOldStyleOverallAttendance();
                });
    }

    private void loadOldStyleOverallAttendance() {

        db.collection("lectures")
                .whereEqualTo(
                        "classId",
                        studentClassId
                )
                .whereEqualTo(
                        "status",
                        "CLOSED"
                )
                .get()
                .addOnSuccessListener(snapshot -> {

                    loadAttendanceForLectures(
                            snapshot.getDocuments(),
                            0,
                            new HashSet<>()
                    );
                })
                .addOnFailureListener(e -> {

                    overallPresent = 0;
                    overallAbsent = 0;

                    updateOverallAttendance();
                });
    }

    private void loadAttendanceForLectures(
            List<DocumentSnapshot> lectures,
            int index,
            Set<String> processedLectureIds
    ) {

        if (index >= lectures.size()) {

            overallAbsent =
                    Math.max(
                            0,
                            lectures.size() - overallPresent
                    );

            updateOverallAttendance();

            return;
        }

        DocumentSnapshot lecture =
                lectures.get(index);

        String lectureId =
                lecture.getId();

        if (processedLectureIds.contains(lectureId)) {

            loadAttendanceForLectures(
                    lectures,
                    index + 1,
                    processedLectureIds
            );

            return;
        }

        processedLectureIds.add(lectureId);

        db.collection("attendance")
                .document(
                        lectureId + "_" + studentId
                )
                .get()
                .addOnSuccessListener(attendance -> {

                    if (attendance.exists()
                            && "PRESENT".equalsIgnoreCase(
                            getValue(
                                    attendance,
                                    "status"
                            ))) {

                        overallPresent++;
                    }

                    loadAttendanceForLectures(
                            lectures,
                            index + 1,
                            processedLectureIds
                    );

                })
                .addOnFailureListener(e -> {

                    loadAttendanceForLectures(
                            lectures,
                            index + 1,
                            processedLectureIds
                    );
                });
    }

    private void updateOverallAttendance() {

        int total =
                overallPresent + overallAbsent;

        double percentage = 0;

        if (total > 0) {

            percentage =
                    (overallPresent * 100.0)
                            / total;
        }

        tvOverallPercentage.setText(
                String.format(
                        Locale.getDefault(),
                        "%.1f%%",
                        percentage
                )
        );

        tvPresentCount.setText(
                "Present: " + overallPresent
        );

        tvAbsentCount.setText(
                "Absent: " + overallAbsent
        );

        progressOverallAttendance.setProgress(
                (int) percentage
        );

        updateAttendanceStatus(percentage);
    }

    private void updateAttendanceStatus(
            double percentage
    ) {

        if (percentage >= 75) {

            tvAttendanceStatus.setText("Good");

        } else if (percentage >= 60) {

            tvAttendanceStatus.setText("Average");

        } else {

            tvAttendanceStatus.setText("Low");
        }
    }

    private void loadTodayAttendance() {

        db.collection("lectures")
                .whereArrayContains(
                        "targetClassIds",
                        studentClassId
                )
                .whereEqualTo(
                        "status",
                        "CLOSED"
                )
                .get()
                .addOnSuccessListener(snapshot -> {

                    processTodayLectures(
                            snapshot.getDocuments(),
                            0,
                            0,
                            0
                    );

                })
                .addOnFailureListener(e -> {

                    loadOldStyleTodayAttendance();
                });
    }

    private void loadOldStyleTodayAttendance() {

        db.collection("lectures")
                .whereEqualTo(
                        "classId",
                        studentClassId
                )
                .whereEqualTo(
                        "status",
                        "CLOSED"
                )
                .get()
                .addOnSuccessListener(snapshot -> {

                    processTodayLectures(
                            snapshot.getDocuments(),
                            0,
                            0,
                            0
                    );

                })
                .addOnFailureListener(e -> {

                    tvTodayPresent.setText("0");
                    tvTodayAbsent.setText("0");
                    tvTodayTotal.setText("0");
                });
    }

    private void processTodayLectures(
            List<DocumentSnapshot> lectures,
            int index,
            int todayPresent,
            int todayTotal
    ) {

        if (index >= lectures.size()) {

            int todayAbsent =
                    Math.max(
                            0,
                            todayTotal - todayPresent
                    );

            tvTodayPresent.setText(
                    String.valueOf(todayPresent)
            );

            tvTodayAbsent.setText(
                    String.valueOf(todayAbsent)
            );

            tvTodayTotal.setText(
                    String.valueOf(todayTotal)
            );

            return;
        }

        DocumentSnapshot lecture =
                lectures.get(index);

        if (!isToday(lecture)) {

            processTodayLectures(
                    lectures,
                    index + 1,
                    todayPresent,
                    todayTotal
            );

            return;
        }

        String lectureId =
                lecture.getId();

        db.collection("attendance")
                .document(
                        lectureId + "_" + studentId
                )
                .get()
                .addOnSuccessListener(attendance -> {

                    int present =
                            todayPresent;

                    if (attendance.exists()
                            && "PRESENT".equalsIgnoreCase(
                            getValue(
                                    attendance,
                                    "status"
                            ))) {

                        present++;
                    }

                    processTodayLectures(
                            lectures,
                            index + 1,
                            present,
                            todayTotal + 1
                    );

                })
                .addOnFailureListener(e -> {

                    processTodayLectures(
                            lectures,
                            index + 1,
                            todayPresent,
                            todayTotal + 1
                    );
                });
    }

    private boolean isToday(
            DocumentSnapshot document
    ) {

        Date date =
                getLectureDate(document);

        if (date == null) {
            return false;
        }

        Calendar lectureCalendar =
                Calendar.getInstance();

        lectureCalendar.setTime(date);

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

    private void loadActiveLecture() {

        db.collection("lectures")
                .whereArrayContains(
                        "targetClassIds",
                        studentClassId
                )
                .whereEqualTo(
                        "status",
                        "ACTIVE"
                )
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.isEmpty()) {

                        activeLecture =
                                snapshot.getDocuments().get(0);

                        displayActiveLecture(
                                activeLecture
                        );

                    } else {

                        loadOldStyleActiveLecture();
                    }

                })
                .addOnFailureListener(e -> {

                    loadOldStyleActiveLecture();
                });
    }

    private void loadOldStyleActiveLecture() {

        db.collection("lectures")
                .whereEqualTo(
                        "classId",
                        studentClassId
                )
                .whereEqualTo(
                        "status",
                        "ACTIVE"
                )
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.isEmpty()) {

                        activeLecture =
                                snapshot.getDocuments().get(0);

                        displayActiveLecture(
                                activeLecture
                        );

                    } else {

                        showNoActiveLecture();
                    }

                    showLoading(false);

                })
                .addOnFailureListener(e -> {

                    showNoActiveLecture();
                    showLoading(false);
                });
    }

    private void displayActiveLecture(
            DocumentSnapshot lecture
    ) {

        String subject =
                getValue(
                        lecture,
                        "subjectName"
                );

        String teacher =
                getValue(
                        lecture,
                        "teacherName"
                );

        String className =
                getClassNames(lecture);

        String room =
                getValue(
                        lecture,
                        "roomNo"
                );

        tvActiveSubject.setText(
                subject.isEmpty()
                        ? "Active Lecture"
                        : subject
        );

        tvActiveTeacher.setText(
                teacher.isEmpty()
                        ? "Teacher"
                        : "Teacher: " + teacher
        );

        tvActiveClass.setText(
                className.isEmpty()
                        ? "Class"
                        : className
        );

        tvActiveRoom.setText(
                room.isEmpty()
                        ? "Room --"
                        : "Room " + room
        );

        btnEnterActiveOtp.setEnabled(true);

        showLoading(false);
    }

    private void showNoActiveLecture() {

        activeLecture = null;

        tvActiveSubject.setText(
                "No Active Lecture"
        );

        tvActiveTeacher.setText(
                "No lecture is currently active"
        );

        tvActiveClass.setText(
                "Your active lectures will appear here"
        );

        tvActiveRoom.setText(
                "Room --"
        );

        btnEnterActiveOtp.setEnabled(false);
    }

    private void openActiveLecture() {

        if (activeLecture == null) {

            Toast.makeText(
                    requireContext(),
                    "No active lecture available",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Intent intent =
                new Intent(
                        requireContext(),
                        com.omshende.smartattendance.student.EnterOtpActivity.class
                );

        intent.putExtra(
                "lectureId",
                activeLecture.getId()
        );

        startActivity(intent);
    }

    private String getClassNames(
            DocumentSnapshot document
    ) {

        List<String> names =
                getStringList(
                        document,
                        "targetClassNames"
                );

        if (!names.isEmpty()) {

            return android.text.TextUtils.join(
                    " + ",
                    names
            );
        }

        return getValue(
                document,
                "className"
        );
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

    private Date getLectureDate(
            DocumentSnapshot document
    ) {

        Object createdAt =
                document.get("createdAt");

        if (createdAt instanceof Timestamp) {

            return ((Timestamp) createdAt).toDate();
        }

        Object timestamp =
                document.get("timestamp");

        if (timestamp instanceof Timestamp) {

            return ((Timestamp) timestamp).toDate();
        }

        return null;
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

    private void showLoading(
            boolean show
    ) {

        progressStudentDashboard.setVisibility(
                show
                        ? View.VISIBLE
                        : View.GONE
        );
    }

    private void showError(
            String message
    ) {

        Toast.makeText(
                requireContext(),
                message,
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    public void onResume() {

        super.onResume();

        if (auth != null
                && auth.getCurrentUser() != null
                && studentClassId != null
                && !studentClassId.isEmpty()) {

            loadOverallAttendance();
            loadTodayAttendance();
            loadActiveLecture();
        }
    }
}