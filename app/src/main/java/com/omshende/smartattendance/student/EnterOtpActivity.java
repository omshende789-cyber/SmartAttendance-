package com.omshende.smartattendance.student;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import com.omshende.smartattendance.R;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EnterOtpActivity extends AppCompatActivity {

    // =========================================================
    // CONSTANTS
    // =========================================================

    private static final int LOCATION_PERMISSION_REQUEST = 2001;

    // Classroom attendance radius
    private static final double DEFAULT_ATTENDANCE_RADIUS = 10.0;

    // Maximum acceptable location accuracy.
    // We allow slightly more than classroom radius because GPS
    // can fluctuate indoors.
    private static final float MAX_ACCEPTABLE_ACCURACY = 100.0f;


    // =========================================================
    // VIEWS
    // =========================================================

    private TextView tvSubjectName;
    private TextView tvTeacherName;
    private TextView tvClassName;
    private TextView tvRoomNo;

    private TextInputEditText etOtp;

    private MaterialButton btnMarkAttendance;


    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    // =========================================================
    // LOCATION
    // =========================================================

    private FusedLocationProviderClient fusedLocationClient;


    // =========================================================
    // LECTURE
    // =========================================================

    private String lectureId = "";


    // =========================================================
    // PENDING LOCATION VERIFICATION
    // =========================================================

    private String pendingStudentId = "";
    private DocumentSnapshot pendingLecture = null;


    // =========================================================
    // ACTIVITY CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_enter_otp);

        initializeFirebase();
        initializeLocation();
        initializeViews();
        setupOtpInput();
        getLectureId();
        setupListeners();

        if (TextUtils.isEmpty(lectureId)) {

            showToast(
                    "Lecture information not found.",
                    Toast.LENGTH_LONG
            );

            finish();
            return;
        }

        loadLecture();
    }


    // =========================================================
    // FIREBASE INITIALIZATION
    // =========================================================

    private void initializeFirebase() {

        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();
    }


    // =========================================================
    // LOCATION INITIALIZATION
    // =========================================================

    private void initializeLocation() {

        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);
    }


    // =========================================================
    // VIEW INITIALIZATION
    // =========================================================

    private void initializeViews() {

        tvSubjectName =
                findViewById(R.id.tvOtpSubjectName);

        tvTeacherName =
                findViewById(R.id.tvOtpTeacherName);

        tvClassName =
                findViewById(R.id.tvOtpClassName);

        tvRoomNo =
                findViewById(R.id.tvOtpRoomNo);

        etOtp =
                findViewById(R.id.etOtp);

        btnMarkAttendance =
                findViewById(R.id.btnMarkAttendance);
    }


    // =========================================================
    // OTP INPUT
    // =========================================================

    private void setupOtpInput() {

        etOtp.setKeyListener(
                DigitsKeyListener.getInstance("0123456789")
        );

        etOtp.setFilters(
                new InputFilter[]{
                        new InputFilter.LengthFilter(6)
                }
        );

        etOtp.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER
        );
    }


    // =========================================================
    // GET LECTURE ID
    // =========================================================

    private void getLectureId() {

        lectureId =
                getIntent().getStringExtra("lectureId");

        if (lectureId == null) {
            lectureId = "";
        }

        lectureId = lectureId.trim();
    }


    // =========================================================
    // BUTTON LISTENER
    // =========================================================

    private void setupListeners() {

        btnMarkAttendance.setOnClickListener(
                v -> verifyOtpAndMarkAttendance()
        );
    }


    // =========================================================
    // LOAD LECTURE
    // =========================================================

    private void loadLecture() {

        setLoading(true);

        db.collection("lectures")
                .document(lectureId)
                .get()
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {

                        setLoading(false);

                        showToast(
                                "Unable to load lecture.\n"
                                        + getFirestoreError(task.getException()),
                                Toast.LENGTH_LONG
                        );

                        return;
                    }

                    DocumentSnapshot document =
                            task.getResult();

                    if (document == null
                            || !document.exists()) {

                        setLoading(false);

                        showToast(
                                "Lecture not found.",
                                Toast.LENGTH_LONG
                        );

                        finish();
                        return;
                    }

                    displayLecture(document);

                    setLoading(false);
                });
    }


    // =========================================================
    // DISPLAY LECTURE
    // =========================================================

    private void displayLecture(
            DocumentSnapshot document
    ) {

        String subject =
                document.getString("subjectName");

        String teacher =
                document.getString("teacherName");

        String className =
                document.getString("className");

        String roomNo =
                document.getString("roomNo");


        // -----------------------------------------------------
        // SUBJECT
        // -----------------------------------------------------

        tvSubjectName.setText(
                TextUtils.isEmpty(subject)
                        ? "Unknown Subject"
                        : subject
        );


        // -----------------------------------------------------
        // TEACHER
        // -----------------------------------------------------

        tvTeacherName.setText(
                TextUtils.isEmpty(teacher)
                        ? "Teacher not available"
                        : teacher.startsWith("Prof.")
                        ? teacher
                        : "Prof. " + teacher
        );


        // -----------------------------------------------------
        // CLASS
        // -----------------------------------------------------

        if (!TextUtils.isEmpty(className)) {

            tvClassName.setText(className);

        } else {

            @SuppressWarnings("unchecked")
            List<String> targetClassNames =
                    (List<String>) document.get(
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

                tvClassName.setText(
                        "Target classes not available"
                );
            }
        }


        // -----------------------------------------------------
        // ROOM
        // -----------------------------------------------------

        tvRoomNo.setText(
                TextUtils.isEmpty(roomNo)
                        ? "Room not available"
                        : "Room " + roomNo
        );
    }


    // =========================================================
    // BUILD TARGET CLASS TEXT
    // =========================================================

    private String buildTargetClassText(
            List<String> targetClassNames
    ) {

        if (targetClassNames == null
                || targetClassNames.isEmpty()) {

            return "Target classes not available";
        }

        StringBuilder builder =
                new StringBuilder();

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


    // =========================================================
    // MAIN OTP VERIFICATION
    // =========================================================

    private void verifyOtpAndMarkAttendance() {

        String enteredOtp =
                etOtp.getText() != null
                        ? etOtp.getText()
                        .toString()
                        .trim()
                        : "";


        // -----------------------------------------------------
        // EMPTY OTP
        // -----------------------------------------------------

        if (TextUtils.isEmpty(enteredOtp)) {

            etOtp.setError("Enter OTP");

            etOtp.requestFocus();

            return;
        }


        // -----------------------------------------------------
        // OTP LENGTH
        // -----------------------------------------------------

        if (enteredOtp.length() != 6) {

            etOtp.setError(
                    "OTP must be 6 digits"
            );

            etOtp.requestFocus();

            return;
        }


        // -----------------------------------------------------
        // OTP NUMERIC
        // -----------------------------------------------------

        if (!enteredOtp.matches("\\d{6}")) {

            etOtp.setError(
                    "OTP must contain only numbers"
            );

            etOtp.requestFocus();

            return;
        }


        // -----------------------------------------------------
        // STUDENT SESSION
        // -----------------------------------------------------

        if (mAuth.getCurrentUser() == null) {

            showToast(
                    "Student session expired. Please login again.",
                    Toast.LENGTH_LONG
            );

            finish();

            return;
        }


        setLoading(true);


        String studentId =
                mAuth.getCurrentUser().getUid();


        // -----------------------------------------------------
        // LOAD STUDENT PROFILE
        // -----------------------------------------------------

        db.collection("users")
                .document(studentId)
                .get()
                .addOnCompleteListener(
                        userTask -> {

                            if (!userTask.isSuccessful()) {

                                setLoading(false);

                                showToast(
                                        "Unable to load student profile.\n"
                                                + getFirestoreError(
                                                userTask.getException()
                                        ),
                                        Toast.LENGTH_LONG
                                );

                                return;
                            }


                            DocumentSnapshot student =
                                    userTask.getResult();


                            if (student == null
                                    || !student.exists()) {

                                setLoading(false);

                                showToast(
                                        "Student profile not found.",
                                        Toast.LENGTH_LONG
                                );

                                return;
                            }


                            String role =
                                    getSafeString(
                                            student,
                                            "role"
                                    );


                            // -------------------------------------------------
                            // ROLE CHECK
                            // -------------------------------------------------

                            if (!TextUtils.isEmpty(role)
                                    && !"student".equalsIgnoreCase(
                                    role.trim()
                            )) {

                                setLoading(false);

                                showToast(
                                        "Only student accounts can mark attendance.",
                                        Toast.LENGTH_LONG
                                );

                                return;
                            }


                            // -------------------------------------------------
                            // CLASS ID
                            // -------------------------------------------------

                            String studentClassId =
                                    getSafeString(
                                            student,
                                            "classId"
                                    );


                            if (TextUtils.isEmpty(
                                    studentClassId
                            )) {

                                setLoading(false);

                                showToast(
                                        "Student class is not assigned.",
                                        Toast.LENGTH_LONG
                                );

                                return;
                            }


                            checkLecture(
                                    studentId,
                                    studentClassId.trim(),
                                    enteredOtp
                            );
                        }
                );
    }


    // =========================================================
    // CHECK LECTURE
    // =========================================================

    private void checkLecture(
            String studentId,
            String studentClassId,
            String enteredOtp
    ) {

        db.collection("lectures")
                .document(lectureId)
                .get()
                .addOnCompleteListener(
                        task -> {

                            if (!task.isSuccessful()) {

                                setLoading(false);

                                showToast(
                                        "Unable to verify lecture.\n"
                                                + getFirestoreError(
                                                task.getException()
                                        ),
                                        Toast.LENGTH_LONG
                                );

                                return;
                            }


                            DocumentSnapshot lecture =
                                    task.getResult();


                            if (lecture == null
                                    || !lecture.exists()) {

                                setLoading(false);

                                showToast(
                                        "Lecture not found.",
                                        Toast.LENGTH_LONG
                                );

                                return;
                            }


                            // =================================================
                            // LECTURE DATA
                            // =================================================

                            String status =
                                    lecture.getString("status");

                            String correctOtp =
                                    lecture.getString("otp");

                            Timestamp otpExpiry =
                                    lecture.getTimestamp("otpExpiry");


                            @SuppressWarnings("unchecked")
                            List<String> targetClassIds =
                                    (List<String>) lecture.get(
                                            "targetClassIds"
                                    );


                            // =================================================
                            // CHECK 1 — ACTIVE
                            // =================================================

                            if (!"ACTIVE".equalsIgnoreCase(
                                    status
                            )) {

                                setLoading(false);

                                showToast(
                                        "This lecture is not active.",
                                        Toast.LENGTH_LONG
                                );

                                return;
                            }


                            // =================================================
                            // CHECK 2 — TARGET CLASS
                            // =================================================

                            if (targetClassIds == null
                                    || targetClassIds.isEmpty()) {

                                setLoading(false);

                                showToast(
                                        "No target class is configured for this lecture.",
                                        Toast.LENGTH_LONG
                                );

                                return;
                            }


                            boolean classAllowed = false;


                            for (String targetClassId :
                                    targetClassIds) {

                                if (targetClassId != null
                                        && studentClassId.equalsIgnoreCase(
                                        targetClassId.trim()
                                )) {

                                    classAllowed = true;

                                    break;
                                }
                            }


                            if (!classAllowed) {

                                setLoading(false);

                                showToast(
                                        "You are not eligible for this lecture.",
                                        Toast.LENGTH_LONG
                                );

                                return;
                            }


                            // =================================================
                            // CHECK 3 — OTP EXISTS
                            // =================================================

                            if (TextUtils.isEmpty(
                                    correctOtp
                            )) {

                                setLoading(false);

                                showToast(
                                        "OTP is not available. Ask teacher to generate OTP.",
                                        Toast.LENGTH_LONG
                                );

                                return;
                            }


                            // =================================================
                            // CHECK 4 — OTP CORRECT
                            // =================================================

                            if (!enteredOtp.equals(
                                    correctOtp.trim()
                            )) {

                                setLoading(false);

                                etOtp.setError(
                                        "Incorrect OTP"
                                );

                                etOtp.requestFocus();

                                return;
                            }


                            // =================================================
                            // CHECK 5 — OTP EXPIRY
                            // =================================================

                            if (otpExpiry == null) {

                                setLoading(false);

                                showToast(
                                        "OTP expiry information is missing.",
                                        Toast.LENGTH_LONG
                                );

                                return;
                            }


                            long currentTime =
                                    System.currentTimeMillis();


                            long expiryTime =
                                    otpExpiry
                                            .toDate()
                                            .getTime();


                            if (currentTime >= expiryTime) {

                                setLoading(false);

                                showToast(
                                        "OTP has expired. Ask teacher for a new OTP.",
                                        Toast.LENGTH_LONG
                                );

                                return;
                            }


                            // =================================================
                            // CHECK 6 — GPS
                            // =================================================

                            pendingStudentId =
                                    studentId;

                            pendingLecture =
                                    lecture;


                            verifyStudentLocation(
                                    studentId,
                                    lecture
                            );
                        }
                );
    }


    // =========================================================
    // LOCATION VERIFICATION
    // =========================================================

    @SuppressLint("MissingPermission")
    private void verifyStudentLocation(
            String studentId,
            DocumentSnapshot lecture
    ) {

        // -----------------------------------------------------
        // PERMISSION
        // -----------------------------------------------------

        if (!hasLocationPermission()) {

            pendingStudentId =
                    studentId;

            pendingLecture =
                    lecture;

            setLoading(false);

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST
            );

            showToast(
                    "Location permission is required for attendance.",
                    Toast.LENGTH_LONG
            );

            return;
        }


        // -----------------------------------------------------
        // GPS / LOCATION ENABLED
        // -----------------------------------------------------

        if (!isLocationEnabled()) {

            setLoading(false);

            showToast(
                    "Please turn ON Location/GPS and try again.",
                    Toast.LENGTH_LONG
            );

            try {

                Intent intent =
                        new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS
                        );

                startActivity(intent);

            } catch (Exception ignored) {
            }

            return;
        }


        // -----------------------------------------------------
        // GET TEACHER CLASSROOM LOCATION
        // -----------------------------------------------------

        Double lectureLatitude =
                getDoubleValue(
                        lecture.get("latitude")
                );


        Double lectureLongitude =
                getDoubleValue(
                        lecture.get("longitude")
                );


        Double locationRadius =
                getDoubleValue(
                        lecture.get("locationRadius")
                );


        // -----------------------------------------------------
        // LOCATION NOT CONFIGURED
        // -----------------------------------------------------

        if (lectureLatitude == null
                || lectureLongitude == null) {

            setLoading(false);

            showToast(
                    "Classroom location is not configured for this lecture.\n"
                            + "Create a new lecture with GPS enabled.",
                    Toast.LENGTH_LONG
            );

            return;
        }


        // -----------------------------------------------------
        // DEFAULT RADIUS
        // -----------------------------------------------------

        if (locationRadius == null
                || locationRadius <= 0) {

            locationRadius =
                    DEFAULT_ATTENDANCE_RADIUS;
        }


        final double allowedRadius =
                locationRadius;


        // -----------------------------------------------------
        // GET CURRENT LOCATION
        // -----------------------------------------------------

        fusedLocationClient
                .getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        null
                )
                .addOnSuccessListener(
                        location -> {

                            if (location != null) {

                                processStudentLocation(
                                        studentId,
                                        lecture,
                                        location,
                                        lectureLatitude,
                                        lectureLongitude,
                                        allowedRadius
                                );

                                return;
                            }


                            // ------------------------------------------------
                            // CURRENT LOCATION NULL
                            // Try last known location as fallback
                            // ------------------------------------------------

                            getLastKnownLocation(
                                    studentId,
                                    lecture,
                                    lectureLatitude,
                                    lectureLongitude,
                                    allowedRadius
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            // Try last location if current location fails.
                            getLastKnownLocation(
                                    studentId,
                                    lecture,
                                    lectureLatitude,
                                    lectureLongitude,
                                    allowedRadius
                            );
                        }
                );
    }


    // =========================================================
    // LAST KNOWN LOCATION FALLBACK
    // =========================================================

    @SuppressLint("MissingPermission")
    private void getLastKnownLocation(
            String studentId,
            DocumentSnapshot lecture,
            double lectureLatitude,
            double lectureLongitude,
            double allowedRadius
    ) {

        fusedLocationClient
                .getLastLocation()
                .addOnSuccessListener(
                        lastLocation -> {

                            if (lastLocation == null) {

                                setLoading(false);

                                showToast(
                                        "Unable to detect your location.\n"
                                                + "Please turn ON GPS, move outdoors for a few seconds, and try again.",
                                        Toast.LENGTH_LONG
                                );

                                return;
                            }


                            // ------------------------------------------------
                            // Check location age
                            // ------------------------------------------------

                            long locationAge =
                                    System.currentTimeMillis()
                                            - lastLocation.getTime();


                            // More than 2 minutes old = reject.
                            if (locationAge > 120000) {

                                setLoading(false);

                                showToast(
                                        "Your location is outdated.\n"
                                                + "Please wait a few seconds and try again.",
                                        Toast.LENGTH_LONG
                                );

                                return;
                            }


                            processStudentLocation(
                                    studentId,
                                    lecture,
                                    lastLocation,
                                    lectureLatitude,
                                    lectureLongitude,
                                    allowedRadius
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            setLoading(false);

                            showToast(
                                    "Unable to get your location.\n"
                                            + getLocationError(e),
                                    Toast.LENGTH_LONG
                            );
                        }
                );
    }


    // =========================================================
    // PROCESS STUDENT LOCATION
    // =========================================================

    private void processStudentLocation(
            String studentId,
            DocumentSnapshot lecture,
            Location studentLocation,
            double lectureLatitude,
            double lectureLongitude,
            double allowedRadius
    ) {

        if (studentLocation == null) {

            setLoading(false);

            showToast(
                    "Unable to detect your location. Please try again.",
                    Toast.LENGTH_LONG
            );

            return;
        }


        // =====================================================
        // LOCATION ACCURACY
        // =====================================================

        float accuracy =
                studentLocation.hasAccuracy()
                        ? studentLocation.getAccuracy()
                        : Float.MAX_VALUE;


        if (accuracy > MAX_ACCEPTABLE_ACCURACY) {

            setLoading(false);

            showToast(
                    "GPS accuracy is too low.\n"
                            + "Current accuracy: "
                            + formatDistance(accuracy)
                            + "\n\nPlease turn ON High Accuracy Location and try again.",
                    Toast.LENGTH_LONG
            );

            return;
        }


        // =====================================================
        // CALCULATE DISTANCE
        // =====================================================

        double distance =
                calculateDistance(
                        studentLocation.getLatitude(),
                        studentLocation.getLongitude(),
                        lectureLatitude,
                        lectureLongitude
                );


        // =====================================================
        // OUTSIDE CLASSROOM
        // =====================================================

        if (distance > allowedRadius) {

            setLoading(false);

            showToast(
                    "Attendance rejected.\n\n"
                            + "Distance from classroom: "
                            + formatDistance(distance)
                            + "\n"
                            + "Allowed radius: "
                            + formatDistance(allowedRadius),
                    Toast.LENGTH_LONG
            );

            return;
        }


        // =====================================================
        // GPS VERIFIED
        // =====================================================

        markAttendance(
                lecture,
                studentId,
                distance,
                accuracy
        );
    }


    // =========================================================
    // CALCULATE DISTANCE
    // =========================================================

    private double calculateDistance(
            double studentLatitude,
            double studentLongitude,
            double lectureLatitude,
            double lectureLongitude
    ) {

        float[] results =
                new float[1];


        Location.distanceBetween(
                studentLatitude,
                studentLongitude,
                lectureLatitude,
                lectureLongitude,
                results
        );


        return results[0];
    }


    // =========================================================
    // FORMAT DISTANCE
    // =========================================================

    private String formatDistance(
            double distance
    ) {

        if (distance < 1000) {

            return String.format(
                    Locale.getDefault(),
                    "%.0f meters",
                    distance
            );
        }


        return String.format(
                Locale.getDefault(),
                "%.2f km",
                distance / 1000.0
        );
    }


    // =========================================================
    // LOCATION PERMISSION
    // =========================================================

    private boolean hasLocationPermission() {

        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

                ||

                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;
    }


    // =========================================================
    // LOCATION ENABLED
    // =========================================================

    private boolean isLocationEnabled() {

        try {

            LocationManager manager =
                    (LocationManager)
                            getSystemService(
                                    LOCATION_SERVICE
                            );


            if (manager == null) {

                return false;
            }


            return manager.isProviderEnabled(
                    LocationManager.GPS_PROVIDER
            )
                    ||

                    manager.isProviderEnabled(
                            LocationManager.NETWORK_PROVIDER
                    );

        } catch (Exception e) {

            return false;
        }
    }


    // =========================================================
    // MARK ATTENDANCE
    // =========================================================

    private void markAttendance(
            DocumentSnapshot lecture,
            String studentId,
            double distance,
            float accuracy
    ) {

        String attendanceId =
                lectureId
                        + "_"
                        + studentId;


        // -----------------------------------------------------
        // Attendance Map
        // -----------------------------------------------------

        Map<String, Object> attendance =
                new HashMap<>();


        attendance.put(
                "lectureId",
                lectureId
        );


        attendance.put(
                "studentId",
                studentId
        );


        attendance.put(
                "status",
                "PRESENT"
        );


        attendance.put(
                "markedBy",
                "OTP + GPS"
        );


        // -----------------------------------------------------
        // GPS Audit Information
        // -----------------------------------------------------

        attendance.put(
                "distanceMeters",
                distance
        );


        attendance.put(
                "locationAccuracyMeters",
                accuracy
        );


        attendance.put(
                "timestamp",
                FieldValue.serverTimestamp()
        );


        // -----------------------------------------------------
        // DIRECT CREATE
        //
        // We DO NOT read attendance first.
        // Firestore Rules + fixed document ID prevent duplicate.
        // -----------------------------------------------------

        db.collection("attendance")
                .document(attendanceId)
                .set(attendance)
                .addOnCompleteListener(
                        task -> {

                            setLoading(false);


                            if (!task.isSuccessful()) {

                                String error =
                                        getFirestoreError(
                                                task.getException()
                                        );


                                showToast(
                                        "Failed to mark attendance.\n\n"
                                                + error,
                                        Toast.LENGTH_LONG
                                );

                                return;
                            }


                            showToast(
                                    "Attendance marked successfully! ✓",
                                    Toast.LENGTH_LONG
                            );


                            // Clear pending state
                            pendingStudentId = "";
                            pendingLecture = null;


                            finish();
                        }
                );
    }


    // =========================================================
    // PERMISSION RESULT
    // =========================================================

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );


        if (requestCode
                != LOCATION_PERMISSION_REQUEST) {

            return;
        }


        if (hasLocationPermission()) {

            showToast(
                    "Location permission granted. Verifying attendance...",
                    Toast.LENGTH_SHORT
            );


            // -------------------------------------------------
            // Automatically continue verification
            // -------------------------------------------------

            if (!TextUtils.isEmpty(pendingStudentId)
                    && pendingLecture != null) {

                setLoading(true);

                verifyStudentLocation(
                        pendingStudentId,
                        pendingLecture
                );

            } else {

                setLoading(false);
            }

        } else {

            setLoading(false);

            showToast(
                    "Location permission is required for attendance.",
                    Toast.LENGTH_LONG
            );
        }
    }


    // =========================================================
    // SAFE STRING
    // =========================================================

    private String getSafeString(
            DocumentSnapshot document,
            String field
    ) {

        Object value =
                document.get(field);


        if (value == null) {

            return "";
        }


        return String.valueOf(value);
    }


    // =========================================================
    // SAFE DOUBLE
    // =========================================================

    private Double getDoubleValue(
            Object value
    ) {

        if (value instanceof Number) {

            return ((Number) value).doubleValue();
        }


        if (value instanceof String) {

            try {

                return Double.parseDouble(
                        ((String) value).trim()
                );

            } catch (NumberFormatException ignored) {
            }
        }


        return null;
    }


    // =========================================================
    // FIRESTORE ERROR
    // =========================================================

    private String getFirestoreError(
            Exception exception
    ) {

        if (exception == null) {

            return "Unknown Firebase error.";
        }


        String message =
                exception.getMessage();


        if (TextUtils.isEmpty(message)) {

            return "Unknown Firebase error.";
        }


        return message;
    }


    // =========================================================
    // LOCATION ERROR
    // =========================================================

    private String getLocationError(
            Exception exception
    ) {

        if (exception == null) {

            return "Please check GPS and try again.";
        }


        String message =
                exception.getMessage();


        if (TextUtils.isEmpty(message)) {

            return "Please check GPS and try again.";
        }


        return message;
    }


    // =========================================================
    // TOAST HELPER
    // =========================================================

    private void showToast(
            String message,
            int duration
    ) {

        Toast.makeText(
                this,
                message,
                duration
        ).show();
    }


    // =========================================================
    // LOADING
    // =========================================================

    private void setLoading(
            boolean loading
    ) {

        if (btnMarkAttendance != null) {

            btnMarkAttendance.setEnabled(
                    !loading
            );


            if (loading) {

                btnMarkAttendance.setText(
                        "VERIFYING..."
                );

            } else {

                btnMarkAttendance.setText(
                        "MARK ATTENDANCE"
                );
            }
        }


        if (etOtp != null) {

            etOtp.setEnabled(
                    !loading
            );
        }
    }
}