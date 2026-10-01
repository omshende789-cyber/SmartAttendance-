package com.omshende.smartattendance.teacher;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import com.omshende.smartattendance.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CreateLectureActivity extends AppCompatActivity {

    // ==================================================
    // LOCATION
    // ==================================================

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    // Classroom attendance radius
    private static final double ATTENDANCE_RADIUS_METERS = 100.0;

    private FusedLocationProviderClient fusedLocationClient;

    // ==================================================
    // LECTURE INPUTS
    // ==================================================

    private TextInputEditText etSubject;
    private TextInputEditText etRoomNo;

    // ==================================================
    // TIME INPUTS
    // ==================================================

    /*
     * Start and End Time are only teacher reference.
     *
     * They are NOT saved in Firestore.
     * They are NOT used for attendance validation.
     */

    private TextInputEditText etStartTime;
    private TextInputEditText etEndTime;

    // ==================================================
    // TARGET CLASS CHECKBOXES
    // ==================================================

    private MaterialCheckBox cbClass1;
    private MaterialCheckBox cbClass2;
    private MaterialCheckBox cbClass3;
    private MaterialCheckBox cbClass4;

    // ==================================================
    // BUTTON
    // ==================================================

    private MaterialButton btnStartLecture;

    // ==================================================
    // FIREBASE
    // ==================================================

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // ==================================================
    // TEACHER DATA
    // ==================================================

    private String teacherName = "";
    private String teacherDepartment = "";
    private String teacherDepartmentId = "";

    // ==================================================
    // ON CREATE
    // ==================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_lecture);

        initializeFirebase();
        initializeLocation();
        initializeViews();
        setupListeners();
        loadTeacherData();
    }

    // ==================================================
    // FIREBASE
    // ==================================================

    private void initializeFirebase() {

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    // ==================================================
    // LOCATION INITIALIZATION
    // ==================================================

    private void initializeLocation() {

        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);
    }

    // ==================================================
    // VIEW INITIALIZATION
    // ==================================================

    private void initializeViews() {

        etSubject =
                findViewById(R.id.etSubject);

        etRoomNo =
                findViewById(R.id.etRoomNo);

        etStartTime =
                findViewById(R.id.etStartTime);

        etEndTime =
                findViewById(R.id.etEndTime);

        cbClass1 =
                findViewById(R.id.CbClass1);

        cbClass2 =
                findViewById(R.id.CbClass2);

        cbClass3 =
                findViewById(R.id.CbClass3);

        cbClass4 =
                findViewById(R.id.CbClass4);

        btnStartLecture =
                findViewById(R.id.btnStartLecture);
    }

    // ==================================================
    // LISTENERS
    // ==================================================

    private void setupListeners() {

        etStartTime.setOnClickListener(
                v -> showTimePicker(true)
        );

        etEndTime.setOnClickListener(
                v -> showTimePicker(false)
        );

        btnStartLecture.setOnClickListener(
                v -> createLecture()
        );
    }

    // ==================================================
    // LOAD TEACHER DATA
    // ==================================================

    private void loadTeacherData() {

        if (mAuth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Teacher session not found.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        String uid =
                mAuth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {

                        Toast.makeText(
                                CreateLectureActivity.this,
                                "Unable to load teacher information.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    DocumentSnapshot document =
                            task.getResult();

                    if (document == null
                            || !document.exists()) {

                        Toast.makeText(
                                CreateLectureActivity.this,
                                "Teacher profile not found.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    teacherName =
                            document.getString("name");

                    teacherDepartment =
                            document.getString("department");

                    teacherDepartmentId =
                            document.getString("departmentId");

                    if (TextUtils.isEmpty(teacherName)) {
                        teacherName = "Teacher";
                    }

                    if (TextUtils.isEmpty(teacherDepartment)) {
                        teacherDepartment = "";
                    }

                    if (TextUtils.isEmpty(teacherDepartmentId)) {
                        teacherDepartmentId = "";
                    }

                    setupTargetClasses();
                });
    }

    // ==================================================
    // TARGET CLASSES
    // ==================================================

    private void setupTargetClasses() {

        String department =
                teacherDepartment.trim();

        if (TextUtils.isEmpty(department)) {

            Toast.makeText(
                    this,
                    "Teacher department is not configured.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        cbClass1.setText(
                "1st " + department
        );

        cbClass2.setText(
                "2nd " + department
        );

        cbClass3.setText(
                "3rd " + department
        );

        cbClass4.setText(
                "4th " + department
        );
    }

    // ==================================================
    // TIME PICKER
    // ==================================================

    private void showTimePicker(boolean isStartTime) {

        Calendar calendar =
                Calendar.getInstance();

        int hour =
                calendar.get(Calendar.HOUR_OF_DAY);

        int minute =
                calendar.get(Calendar.MINUTE);

        TimePickerDialog dialog =
                new TimePickerDialog(
                        this,
                        (view,
                         selectedHour,
                         selectedMinute) -> {

                            String formattedTime =
                                    String.format(
                                            Locale.getDefault(),
                                            "%02d:%02d",
                                            selectedHour,
                                            selectedMinute
                                    );

                            if (isStartTime) {

                                etStartTime.setText(
                                        formattedTime
                                );

                            } else {

                                etEndTime.setText(
                                        formattedTime
                                );
                            }
                        },
                        hour,
                        minute,
                        false
                );

        dialog.show();
    }

    // ==================================================
    // CREATE LECTURE
    // ==================================================

    private void createLecture() {

        // --------------------------------------------------
        // Subject
        // --------------------------------------------------

        String subject =
                etSubject.getText() != null
                        ? etSubject.getText()
                        .toString()
                        .trim()
                        : "";

        // --------------------------------------------------
        // Room
        // --------------------------------------------------

        String roomNo =
                etRoomNo.getText() != null
                        ? etRoomNo.getText()
                        .toString()
                        .trim()
                        : "";

        // --------------------------------------------------
        // Time
        // --------------------------------------------------

        /*
         * These are only teacher reference values.
         * They are intentionally NOT saved.
         */

        String startTime =
                etStartTime.getText() != null
                        ? etStartTime.getText()
                        .toString()
                        .trim()
                        : "";

        String endTime =
                etEndTime.getText() != null
                        ? etEndTime.getText()
                        .toString()
                        .trim()
                        : "";

        // --------------------------------------------------
        // Validate subject
        // --------------------------------------------------

        if (TextUtils.isEmpty(subject)) {

            etSubject.setError(
                    "Enter subject name"
            );

            etSubject.requestFocus();
            return;
        }

        // --------------------------------------------------
        // Validate room
        // --------------------------------------------------

        if (TextUtils.isEmpty(roomNo)) {

            etRoomNo.setError(
                    "Enter room number"
            );

            etRoomNo.requestFocus();
            return;
        }

        // --------------------------------------------------
        // Validate target classes
        // --------------------------------------------------

        List<String> targetClassIds =
                getSelectedClassIds();

        List<String> targetClassNames =
                getSelectedClassNames();

        if (targetClassIds.isEmpty()) {

            Toast.makeText(
                    this,
                    "Select at least one target class.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // --------------------------------------------------
        // Check teacher session
        // --------------------------------------------------

        if (mAuth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Teacher session expired.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // --------------------------------------------------
        // IMPORTANT
        // Get classroom location first
        // --------------------------------------------------

        getTeacherLocationAndCreateLecture(
                subject,
                roomNo,
                startTime,
                endTime,
                targetClassIds,
                targetClassNames
        );
    }

    // ==================================================
    // GET TEACHER / CLASSROOM LOCATION
    // ==================================================

    private void getTeacherLocationAndCreateLecture(
            String subject,
            String roomNo,
            String startTime,
            String endTime,
            List<String> targetClassIds,
            List<String> targetClassNames
    ) {

        // --------------------------------------------------
        // Permission check
        // --------------------------------------------------

        if (!hasLocationPermission()) {

            requestLocationPermission();
            return;
        }

        // --------------------------------------------------
        // Location service check
        // --------------------------------------------------

        if (!isLocationEnabled()) {

            Toast.makeText(
                    this,
                    "Please turn ON Location/GPS.",
                    Toast.LENGTH_LONG
            ).show();

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

        setLoading(true);

        // --------------------------------------------------
        // Get current location
        // --------------------------------------------------

        fusedLocationClient
                .getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        null
                )
                .addOnSuccessListener(location -> {

                    if (location == null) {

                        setLoading(false);

                        Toast.makeText(
                                CreateLectureActivity.this,
                                "Unable to get classroom location. Please try again.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    double latitude =
                            location.getLatitude();

                    double longitude =
                            location.getLongitude();

                    // --------------------------------------------------
                    // Create lecture with location
                    // --------------------------------------------------

                    saveLecture(
                            subject,
                            roomNo,
                            startTime,
                            endTime,
                            targetClassIds,
                            targetClassNames,
                            latitude,
                            longitude
                    );
                })
                .addOnFailureListener(e -> {

                    setLoading(false);

                    Toast.makeText(
                            CreateLectureActivity.this,
                            "Unable to get location. Please try again.",
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // ==================================================
    // SAVE LECTURE
    // ==================================================

    private void saveLecture(
            String subject,
            String roomNo,
            String startTime,
            String endTime,
            List<String> targetClassIds,
            List<String> targetClassNames,
            double latitude,
            double longitude
    ) {

        // --------------------------------------------------
        // Generate lecture ID
        // --------------------------------------------------

        String lectureId =
                db.collection("lectures")
                        .document()
                        .getId();

        // --------------------------------------------------
        // Subject ID
        // --------------------------------------------------

        String subjectId =
                generateSubjectId(subject);

        // --------------------------------------------------
        // Lecture year
        // --------------------------------------------------

        String lectureYear =
                getLectureYear(targetClassIds);

        // --------------------------------------------------
        // Display class name
        // --------------------------------------------------

        String classDisplayName =
                buildClassDisplayName(
                        targetClassNames
                );

        // --------------------------------------------------
        // Lecture data
        // --------------------------------------------------

        Map<String, Object> lecture =
                new HashMap<>();

        // Basic
        lecture.put(
                "lectureId",
                lectureId
        );

        lecture.put(
                "teacherId",
                mAuth.getCurrentUser().getUid()
        );

        lecture.put(
                "teacherName",
                teacherName
        );

        // Subject
        lecture.put(
                "subjectId",
                subjectId
        );

        lecture.put(
                "subjectName",
                subject
        );

        // Department
        lecture.put(
                "departmentId",
                teacherDepartmentId
        );

        lecture.put(
                "department",
                teacherDepartment
        );

        // Year
        lecture.put(
                "year",
                lectureYear
        );

        // Target classes
        lecture.put(
                "targetClassIds",
                targetClassIds
        );

        lecture.put(
                "targetClassNames",
                targetClassNames
        );

        // Display class
        lecture.put(
                "className",
                classDisplayName
        );

        // Backward compatibility
        if (!targetClassIds.isEmpty()) {

            lecture.put(
                    "classId",
                    targetClassIds.get(0)
            );
        }

        // Room
        lecture.put(
                "roomNo",
                roomNo
        );

        // --------------------------------------------------
        // CLASSROOM LOCATION
        // --------------------------------------------------

        /*
         * This location represents the teacher's
         * classroom location at lecture creation time.
         */

        lecture.put(
                "latitude",
                latitude
        );

        lecture.put(
                "longitude",
                longitude
        );

        lecture.put(
                "locationRadius",
                ATTENDANCE_RADIUS_METERS
        );

        // --------------------------------------------------
        // Creation timestamp
        // --------------------------------------------------

        lecture.put(
                "createdAt",
                FieldValue.serverTimestamp()
        );

        // --------------------------------------------------
        // Status
        // --------------------------------------------------

        lecture.put(
                "status",
                "ACTIVE"
        );

        // --------------------------------------------------
        // OTP
        // --------------------------------------------------

        lecture.put(
                "otp",
                ""
        );

        lecture.put(
                "otpExpiry",
                null
        );

        // --------------------------------------------------
        // Save
        // --------------------------------------------------

        db.collection("lectures")
                .document(lectureId)
                .set(lecture)
                .addOnCompleteListener(task -> {

                    setLoading(false);

                    if (!task.isSuccessful()) {

                        Toast.makeText(
                                CreateLectureActivity.this,
                                "Failed to create lecture: "
                                        + (
                                        task.getException() != null
                                                ? task.getException()
                                                .getMessage()
                                                : "Unknown error"
                                ),
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    Toast.makeText(
                            CreateLectureActivity.this,
                            "Lecture created successfully!",
                            Toast.LENGTH_SHORT
                    ).show();

                    // --------------------------------------------------
                    // Open Live Lecture
                    // --------------------------------------------------

                    Intent intent =
                            new Intent(
                                    CreateLectureActivity.this,
                                    LiveLectureActivity.class
                            );

                    intent.putExtra(
                            "lectureId",
                            lectureId
                    );

                    startActivity(intent);

                    finish();
                });
    }

    // ==================================================
    // LOCATION PERMISSION
    // ==================================================

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

    // ==================================================
    // REQUEST LOCATION PERMISSION
    // ==================================================

    private void requestLocationPermission() {

        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST
        );
    }

    // ==================================================
    // PERMISSION RESULT
    // ==================================================

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

        if (requestCode == LOCATION_PERMISSION_REQUEST) {

            if (hasLocationPermission()) {

                Toast.makeText(
                        this,
                        "Location permission granted. Tap Start Lecture again.",
                        Toast.LENGTH_SHORT
                ).show();

            } else {

                Toast.makeText(
                        this,
                        "Location permission is required for attendance security.",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    // ==================================================
    // LOCATION SERVICE
    // ==================================================

    private boolean isLocationEnabled() {

        try {

            android.location.LocationManager locationManager =
                    (android.location.LocationManager)
                            getSystemService(
                                    LOCATION_SERVICE
                            );

            if (locationManager == null) {
                return false;
            }

            return locationManager.isProviderEnabled(
                    android.location.LocationManager.GPS_PROVIDER
            )
                    ||
                    locationManager.isProviderEnabled(
                            android.location.LocationManager.NETWORK_PROVIDER
                    );

        } catch (Exception e) {

            return false;
        }
    }

    // ==================================================
    // GET SELECTED CLASS IDS
    // ==================================================

    private List<String> getSelectedClassIds() {

        List<String> selectedIds =
                new ArrayList<>();

        if (cbClass1.isChecked()) {

            selectedIds.add(
                    generateClassId(1)
            );
        }

        if (cbClass2.isChecked()) {

            selectedIds.add(
                    generateClassId(2)
            );
        }

        if (cbClass3.isChecked()) {

            selectedIds.add(
                    generateClassId(3)
            );
        }

        if (cbClass4.isChecked()) {

            selectedIds.add(
                    generateClassId(4)
            );
        }

        return selectedIds;
    }

    // ==================================================
    // GET SELECTED CLASS NAMES
    // ==================================================

    private List<String> getSelectedClassNames() {

        List<String> selectedNames =
                new ArrayList<>();

        if (cbClass1.isChecked()) {

            selectedNames.add(
                    "1st " + teacherDepartment
            );
        }

        if (cbClass2.isChecked()) {

            selectedNames.add(
                    "2nd " + teacherDepartment
            );
        }

        if (cbClass3.isChecked()) {

            selectedNames.add(
                    "3rd " + teacherDepartment
            );
        }

        if (cbClass4.isChecked()) {

            selectedNames.add(
                    "4th " + teacherDepartment
            );
        }

        return selectedNames;
    }

    // ==================================================
    // GENERATE CLASS ID
    // ==================================================

    private String generateClassId(int yearNumber) {

        String departmentId =
                teacherDepartmentId
                        .trim()
                        .toLowerCase(
                                Locale.getDefault()
                        );

        if (TextUtils.isEmpty(departmentId)) {

            departmentId =
                    teacherDepartment
                            .trim()
                            .toLowerCase(
                                    Locale.getDefault()
                            )
                            .replaceAll(
                                    "[^a-z0-9]+",
                                    ""
                            );
        }

        return departmentId
                + "_"
                + yearNumber;
    }

    // ==================================================
    // GENERATE SUBJECT ID
    // ==================================================

    private String generateSubjectId(
            String subject
    ) {

        return subject
                .trim()
                .toLowerCase(
                        Locale.getDefault()
                )
                .replaceAll(
                        "[^a-z0-9]+",
                        "_"
                )
                .replaceAll(
                        "^_+|_+$",
                        ""
                );
    }

    // ==================================================
    // GET LECTURE YEAR
    // ==================================================

    private String getLectureYear(
            List<String> targetClassIds
    ) {

        if (targetClassIds.size() == 1) {

            String classId =
                    targetClassIds.get(0);

            if (classId.endsWith("_1")) {
                return "1st Year";
            }

            if (classId.endsWith("_2")) {
                return "2nd Year";
            }

            if (classId.endsWith("_3")) {
                return "3rd Year";
            }

            if (classId.endsWith("_4")) {
                return "4th Year";
            }
        }

        return "Multiple Years";
    }

    // ==================================================
    // BUILD CLASS DISPLAY NAME
    // ==================================================

    private String buildClassDisplayName(
            List<String> targetClassNames
    ) {

        if (targetClassNames == null
                || targetClassNames.isEmpty()) {

            return "";
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

    // ==================================================
    // LOADING STATE
    // ==================================================

    private void setLoading(boolean loading) {

        btnStartLecture.setEnabled(
                !loading
        );

        if (loading) {

            btnStartLecture.setText(
                    "GETTING LOCATION..."
            );

        } else {

            btnStartLecture.setText(
                    "START LECTURE"
            );
        }
    }
}