package com.omshende.smartattendance.teacher;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import com.omshende.smartattendance.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class LiveLectureActivity extends AppCompatActivity {

    // ==================================================
    // VIEWS
    // ==================================================

    private TextView tvSubjectName;
    private TextView tvClassName;
    private TextView tvRoomNo;
    private TextView tvLectureStatus;

    private TextView tvOtp;
    private TextView tvOtpTimer;

    private TextView tvAttendanceCount;
    private TextView tvPresentStudentLabel;
    private TextView tvNoLiveAttendance;

    private MaterialButton btnCopyOtp;
    private MaterialButton btnShareOtp;
    private MaterialButton btnCloseLecture;

    private RecyclerView recyclerLiveAttendance;

    // ==================================================
    // FIREBASE
    // ==================================================

    private FirebaseFirestore db;

    // ==================================================
    // LECTURE
    // ==================================================

    private String lectureId = "";

    // ==================================================
    // OTP TIMER
    // ==================================================

    private CountDownTimer otpTimer;

    // ==================================================
    // REAL-TIME ATTENDANCE LISTENER
    // ==================================================

    private ListenerRegistration attendanceListener;

    // ==================================================
    // STATE
    // ==================================================

    private boolean lectureClosed = false;

    // ==================================================
    // ATTENDANCE LIST
    // ==================================================

    private final List<LiveAttendance> attendanceList =
            new ArrayList<>();

    private LiveAttendanceAdapter attendanceAdapter;

    // ==================================================
    // ON CREATE
    // ==================================================

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_live_lecture
        );

        initializeFirebase();

        initializeViews();

        setupRecyclerView();

        getLectureId();

        if (TextUtils.isEmpty(lectureId)) {

            Toast.makeText(
                    this,
                    "Lecture ID not found.",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }

        setupListeners();

        loadLecture();

        startRealtimeAttendanceListener();
    }

    // ==================================================
    // FIREBASE INITIALIZATION
    // ==================================================

    private void initializeFirebase() {

        db =
                FirebaseFirestore.getInstance();
    }

    // ==================================================
    // VIEW INITIALIZATION
    // ==================================================

    private void initializeViews() {

        tvSubjectName =
                findViewById(
                        R.id.tvLiveSubjectName
                );

        tvClassName =
                findViewById(
                        R.id.tvLiveClassName
                );

        tvRoomNo =
                findViewById(
                        R.id.tvLiveRoomNo
                );

        tvLectureStatus =
                findViewById(
                        R.id.tvLiveLectureStatus
                );

        tvOtp =
                findViewById(
                        R.id.tvLiveOtp
                );

        tvOtpTimer =
                findViewById(
                        R.id.tvOtpTimer
                );

        tvAttendanceCount =
                findViewById(
                        R.id.tvAttendanceCount
                );

        tvPresentStudentLabel =
                findViewById(
                        R.id.tvPresentStudentLabel
                );

        tvNoLiveAttendance =
                findViewById(
                        R.id.tvNoLiveAttendance
                );

        btnCopyOtp =
                findViewById(
                        R.id.btnCopyOtp
                );

        btnShareOtp =
                findViewById(
                        R.id.btnShareOtp
                );

        btnCloseLecture =
                findViewById(
                        R.id.btnCloseLecture
                );

        recyclerLiveAttendance =
                findViewById(
                        R.id.recyclerLiveAttendance
                );
    }

    // ==================================================
    // RECYCLER VIEW
    // ==================================================

    private void setupRecyclerView() {

        attendanceAdapter =
                new LiveAttendanceAdapter(
                        attendanceList
                );

        recyclerLiveAttendance.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerLiveAttendance.setAdapter(
                attendanceAdapter
        );

        recyclerLiveAttendance.setNestedScrollingEnabled(
                false
        );
    }

    // ==================================================
    // LISTENERS
    // ==================================================

    private void setupListeners() {

        btnCloseLecture.setOnClickListener(
                v -> closeLecture()
        );

        btnCopyOtp.setOnClickListener(
                v -> copyOtp()
        );

        btnShareOtp.setOnClickListener(
                v -> shareOtp()
        );
    }

    // ==================================================
    // GET LECTURE ID
    // ==================================================

    private void getLectureId() {

        lectureId =
                getIntent().getStringExtra(
                        "lectureId"
                );

        if (lectureId == null) {

            lectureId = "";
        }
    }

    // ==================================================
    // LOAD LECTURE
    // ==================================================

    private void loadLecture() {

        db.collection("lectures")
                .document(lectureId)
                .get()
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {

                        Toast.makeText(
                                LiveLectureActivity.this,
                                "Unable to load lecture.",
                                Toast.LENGTH_LONG
                        ).show();

                        finish();

                        return;
                    }

                    DocumentSnapshot document =
                            task.getResult();

                    if (document == null
                            || !document.exists()) {

                        Toast.makeText(
                                LiveLectureActivity.this,
                                "Lecture not found.",
                                Toast.LENGTH_LONG
                        ).show();

                        finish();

                        return;
                    }

                    displayLecture(document);

                    String status =
                            document.getString("status");

                    if (!"ACTIVE".equalsIgnoreCase(status)) {

                        lectureClosed = true;

                        showLectureClosedState();

                        return;
                    }

                    // ==============================================
                    // LOAD EXISTING OTP
                    // ==============================================

                    String existingOtp =
                            document.getString("otp");

                    Timestamp otpExpiry =
                            document.getTimestamp("otpExpiry");

                    if (!TextUtils.isEmpty(existingOtp)
                            && otpExpiry != null
                            && otpExpiry.toDate()
                            .getTime()
                            > System.currentTimeMillis()) {

                        showOtp(
                                existingOtp,
                                otpExpiry
                                        .toDate()
                                        .getTime()
                        );

                    } else {

                        generateOtp();
                    }
                });
    }

    // ==================================================
    // DISPLAY LECTURE
    // ==================================================

    private void displayLecture(
            DocumentSnapshot document
    ) {

        String subjectName =
                document.getString(
                        "subjectName"
                );

        String roomNo =
                document.getString(
                        "roomNo"
                );

        String status =
                document.getString(
                        "status"
                );

        // ==================================================
        // SUBJECT
        // ==================================================

        tvSubjectName.setText(
                TextUtils.isEmpty(subjectName)
                        ? "Unknown Subject"
                        : subjectName
        );

        // ==================================================
        // TARGET CLASSES
        // ==================================================

        List<String> targetClassNames =
                (List<String>)
                        document.get(
                                "targetClassNames"
                        );

        if (targetClassNames != null
                && !targetClassNames.isEmpty()) {

            tvClassName.setText(
                    buildTargetClassText(
                            targetClassNames
                    )
            );

        } else {

            String oldClassName =
                    document.getString(
                            "className"
                    );

            tvClassName.setText(
                    TextUtils.isEmpty(oldClassName)
                            ? "Target classes not available"
                            : oldClassName
            );
        }

        // ==================================================
        // ROOM
        // ==================================================

        tvRoomNo.setText(
                TextUtils.isEmpty(roomNo)
                        ? "Room: Not available"
                        : "Room: " + roomNo
        );

        // ==================================================
        // STATUS
        // ==================================================

        if ("ACTIVE".equalsIgnoreCase(status)) {

            tvLectureStatus.setText(
                    "LECTURE ACTIVE"
            );

            btnCloseLecture.setEnabled(true);

        } else {

            tvLectureStatus.setText(
                    "LECTURE CLOSED"
            );

            btnCloseLecture.setEnabled(false);

            lectureClosed = true;
        }
    }

    // ==================================================
    // BUILD TARGET CLASS TEXT
    // ==================================================

    private String buildTargetClassText(
            List<String> targetClassNames
    ) {

        if (targetClassNames == null
                || targetClassNames.isEmpty()) {

            return "Target classes not available";
        }

        StringBuilder builder =
                new StringBuilder();

        builder.append("Classes: ");

        for (int i = 0;
             i < targetClassNames.size();
             i++) {

            if (i > 0) {

                builder.append(", ");
            }

            builder.append(
                    targetClassNames.get(i)
            );
        }

        return builder.toString();
    }

    // ==================================================
    // GENERATE OTP
    // ==================================================

    private void generateOtp() {

        if (lectureClosed) {

            return;
        }

        // ==================================================
        // GENERATE RANDOM OTP
        // ==================================================

        String otp =
                generateSixDigitOtp();

        // ==================================================
        // OTP EXPIRY = 60 SECONDS
        // ==================================================

        long expiryMillis =
                System.currentTimeMillis()
                        + (60 * 1000);

        Timestamp expiryTimestamp =
                new Timestamp(
                        new Date(
                                expiryMillis
                        )
                );

        // ==================================================
        // FIRESTORE UPDATE
        // ==================================================

        Map<String, Object> update =
                new HashMap<>();

        update.put(
                "otp",
                otp
        );

        update.put(
                "otpExpiry",
                expiryTimestamp
        );

        db.collection("lectures")
                .document(lectureId)
                .update(update)
                .addOnCompleteListener(
                        task -> {

                            if (!task.isSuccessful()) {

                                Toast.makeText(
                                        LiveLectureActivity.this,
                                        "Unable to generate OTP.",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            showOtp(
                                    otp,
                                    expiryMillis
                            );
                        }
                );
    }

    // ==================================================
    // GENERATE SIX DIGIT OTP
    // ==================================================

    private String generateSixDigitOtp() {

        Random random =
                new Random();

        int number =
                100000
                        + random.nextInt(
                        900000
                );

        return String.valueOf(number);
    }

    // ==================================================
    // SHOW OTP
    // ==================================================

    private void showOtp(
            String otp,
            long expiryMillis
    ) {

        tvOtp.setText(otp);

        startOtpTimer(
                expiryMillis
        );
    }

    // ==================================================
    // START OTP TIMER
    // ==================================================

    private void startOtpTimer(
            long expiryMillis
    ) {

        if (otpTimer != null) {

            otpTimer.cancel();
        }

        long remaining =
                expiryMillis
                        - System.currentTimeMillis();

        if (remaining <= 0) {

            tvOtp.setText(
                    "------"
            );

            tvOtpTimer.setText(
                    "OTP EXPIRED"
            );

            if (!lectureClosed) {

                generateOtp();
            }

            return;
        }

        otpTimer =
                new CountDownTimer(
                        remaining,
                        1000
                ) {

                    @Override
                    public void onTick(
                            long millisUntilFinished
                    ) {

                        long seconds =
                                millisUntilFinished
                                        / 1000;

                        tvOtpTimer.setText(
                                "Expires in "
                                        + seconds
                                        + " seconds"
                        );
                    }

                    @Override
                    public void onFinish() {

                        tvOtp.setText(
                                "------"
                        );

                        tvOtpTimer.setText(
                                "OTP EXPIRED"
                        );

                        if (!lectureClosed) {

                            generateOtp();
                        }
                    }
                };

        otpTimer.start();
    }

    // ==================================================
    // COPY OTP
    // ==================================================

    private void copyOtp() {

        String otp =
                tvOtp.getText()
                        .toString()
                        .trim();

        if (otp.isEmpty()
                || otp.equals("------")) {

            Toast.makeText(
                    this,
                    "OTP is not available.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        ClipboardManager clipboard =
                (ClipboardManager)
                        getSystemService(
                                Context.CLIPBOARD_SERVICE
                        );

        if (clipboard != null) {

            ClipData clip =
                    ClipData.newPlainText(
                            "Attendance OTP",
                            otp
                    );

            clipboard.setPrimaryClip(clip);

            Toast.makeText(
                    this,
                    "OTP copied.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // ==================================================
    // SHARE OTP
    // ==================================================

    private void shareOtp() {

        String otp =
                tvOtp.getText()
                        .toString()
                        .trim();

        if (otp.isEmpty()
                || otp.equals("------")) {

            Toast.makeText(
                    this,
                    "OTP is not available.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String subject =
                tvSubjectName.getText()
                        .toString();

        String room =
                tvRoomNo.getText()
                        .toString();

        String message =
                "Smart Attendance\n\n"
                        + "Subject: "
                        + subject
                        + "\n"
                        + room
                        + "\n\n"
                        + "Attendance OTP: "
                        + otp
                        + "\n\n"
                        + "OTP is valid for a limited time.";

        Intent shareIntent =
                new Intent(
                        Intent.ACTION_SEND
                );

        shareIntent.setType(
                "text/plain"
        );

        shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                message
        );

        startActivity(
                Intent.createChooser(
                        shareIntent,
                        "Share Attendance OTP"
                )
        );
    }

    // ==================================================
    // REAL-TIME ATTENDANCE LISTENER
    // ==================================================

    private void startRealtimeAttendanceListener() {

        attendanceListener =
                db.collection("attendance")

                        .whereEqualTo(
                                "lectureId",
                                lectureId
                        )

                        .whereEqualTo(
                                "status",
                                "PRESENT"
                        )

                        .addSnapshotListener(
                                (snapshots, error) -> {

                                    if (error != null) {

                                        return;
                                    }

                                    if (snapshots == null) {

                                        updateAttendanceUI(
                                                new ArrayList<>()
                                        );

                                        return;
                                    }

                                    List<LiveAttendance>
                                            newList =
                                            new ArrayList<>();

                                    for (
                                            DocumentSnapshot attendanceDoc
                                            : snapshots.getDocuments()
                                    ) {

                                        String studentId =
                                                attendanceDoc
                                                        .getString(
                                                                "studentId"
                                                        );

                                        if (TextUtils.isEmpty(
                                                studentId
                                        )) {

                                            continue;
                                        }

                                        Date timestamp = null;

                                        Timestamp firebaseTimestamp =
                                                attendanceDoc
                                                        .getTimestamp(
                                                                "timestamp"
                                                        );

                                        if (firebaseTimestamp != null) {

                                            timestamp =
                                                    firebaseTimestamp
                                                            .toDate();
                                        }

                                        LiveAttendance item =
                                                new LiveAttendance();

                                        item.setStudentId(
                                                studentId
                                        );

                                        item.setTimestamp(
                                                timestamp
                                        );

                                        newList.add(item);

                                        loadStudentDetails(
                                                item
                                        );
                                    }

                                    // If no students
                                    // immediately show empty.
                                    if (newList.isEmpty()) {

                                        updateAttendanceUI(
                                                newList
                                        );
                                    }
                                }
                        );
    }

    // ==================================================
    // LOAD STUDENT DETAILS
    // ==================================================

    private void loadStudentDetails(
            LiveAttendance attendance
    ) {

        String studentId =
                attendance.getStudentId();

        db.collection("users")
                .document(studentId)
                .get()
                .addOnSuccessListener(
                        document -> {

                            if (document != null
                                    && document.exists()) {

                                attendance.setStudentName(
                                        document.getString(
                                                "name"
                                        )
                                );

                                attendance.setDivision(
                                        document.getString(
                                                "division"
                                        )
                                );

                                attendance.setEnrollmentNo(
                                        document.getString(
                                                "enrollmentNo"
                                        )
                                );

                                attendance.setRollNumber(
                                        getSafeString(
                                                document,
                                                "rollNumber"
                                        )
                                );
                            }

                            updateAttendanceListItem(
                                    attendance
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            attendance.setStudentName(
                                    "Student"
                            );

                            updateAttendanceListItem(
                                    attendance
                            );
                        }
                );
    }

    // ==================================================
    // SAFE FIRESTORE VALUE
    // ==================================================

    private String getSafeString(
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

    // ==================================================
    // UPDATE ATTENDANCE LIST
    // ==================================================

    private void updateAttendanceListItem(
            LiveAttendance attendance
    ) {

        boolean found = false;

        for (int i = 0;
             i < attendanceList.size();
             i++) {

            LiveAttendance existing =
                    attendanceList.get(i);

            if (existing.getStudentId()
                    .equals(
                            attendance.getStudentId()
                    )) {

                attendanceList.set(
                        i,
                        attendance
                );

                found = true;

                break;
            }
        }

        if (!found) {

            attendanceList.add(
                    attendance
            );
        }

        sortAttendanceList();

        updateAttendanceUI(
                attendanceList
        );
    }

    // ==================================================
    // SORT ATTENDANCE
    // ==================================================

    private void sortAttendanceList() {

        attendanceList.sort(
                (a, b) -> {

                    String nameA =
                            a.getStudentName();

                    String nameB =
                            b.getStudentName();

                    if (nameA == null) {
                        nameA = "";
                    }

                    if (nameB == null) {
                        nameB = "";
                    }

                    return nameA.compareToIgnoreCase(
                            nameB
                    );
                }
        );
    }

    // ==================================================
    // UPDATE ATTENDANCE UI
    // ==================================================

    private void updateAttendanceUI(
            List<LiveAttendance> list
    ) {

        int count =
                list.size();

        tvAttendanceCount.setText(
                String.valueOf(count)
        );

        tvPresentStudentLabel.setText(
                count + " "
                        + (count == 1
                        ? "student"
                        : "students")
        );

        if (count == 0) {

            tvNoLiveAttendance.setVisibility(
                    android.view.View.VISIBLE
            );

            recyclerLiveAttendance.setVisibility(
                    android.view.View.GONE
            );

        } else {

            tvNoLiveAttendance.setVisibility(
                    android.view.View.GONE
            );

            recyclerLiveAttendance.setVisibility(
                    android.view.View.VISIBLE
            );
        }

        attendanceAdapter.notifyDataSetChanged();
    }

    // ==================================================
    // SHOW CLOSED STATE
    // ==================================================

    private void showLectureClosedState() {

        if (otpTimer != null) {

            otpTimer.cancel();
        }

        tvOtp.setText(
                "------"
        );

        tvOtpTimer.setText(
                "LECTURE CLOSED"
        );

        tvLectureStatus.setText(
                "LECTURE CLOSED"
        );

        btnCloseLecture.setEnabled(
                false
        );

        btnCopyOtp.setEnabled(
                false
        );

        btnShareOtp.setEnabled(
                false
        );
    }

    // ==================================================
    // CLOSE LECTURE
    // ==================================================

    private void closeLecture() {

        if (lectureClosed) {

            return;
        }

        btnCloseLecture.setEnabled(
                false
        );

        btnCloseLecture.setText(
                "CLOSING..."
        );

        Map<String, Object> update =
                new HashMap<>();

        update.put(
                "status",
                "CLOSED"
        );

        // Invalidate OTP

        update.put(
                "otp",
                ""
        );

        update.put(
                "otpExpiry",
                null
        );

        db.collection("lectures")
                .document(lectureId)
                .update(update)
                .addOnCompleteListener(
                        task -> {

                            if (!task.isSuccessful()) {

                                btnCloseLecture.setEnabled(
                                        true
                                );

                                btnCloseLecture.setText(
                                        "CLOSE LECTURE"
                                );

                                Toast.makeText(
                                        LiveLectureActivity.this,
                                        "Unable to close lecture.",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            lectureClosed =
                                    true;

                            if (otpTimer != null) {

                                otpTimer.cancel();
                            }

                            tvOtp.setText(
                                    "------"
                            );

                            tvOtpTimer.setText(
                                    "LECTURE CLOSED"
                            );

                            tvLectureStatus.setText(
                                    "LECTURE CLOSED"
                            );

                            btnCloseLecture.setText(
                                    "LECTURE CLOSED"
                            );

                            btnCloseLecture.setEnabled(
                                    false
                            );

                            btnCopyOtp.setEnabled(
                                    false
                            );

                            btnShareOtp.setEnabled(
                                    false
                            );

                            Toast.makeText(
                                    LiveLectureActivity.this,
                                    "Lecture closed successfully.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                );
    }

    // ==================================================
    // ON DESTROY
    // ==================================================

    @Override
    protected void onDestroy() {

        if (otpTimer != null) {

            otpTimer.cancel();
        }

        if (attendanceListener != null) {

            attendanceListener.remove();

            attendanceListener = null;
        }

        super.onDestroy();
    }
}