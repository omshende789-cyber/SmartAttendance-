package com.omshende.smartattendance;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    // ==================================================
    // VIEWS
    // ==================================================

    private MaterialButton btnStudent;
    private MaterialButton btnTeacher;

    private AutoCompleteTextView actDepartment;
    private AutoCompleteTextView actYear;
    private AutoCompleteTextView actDivision;

    private TextInputEditText etName;
    private TextInputEditText etEmail;
    private TextInputEditText etCollegeId;
    private TextInputEditText etRollNumber;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;

    private View studentFields;

    private MaterialButton btnRegister;

    // ==================================================
    // FIREBASE
    // ==================================================

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // ==================================================
    // ROLE
    // ==================================================

    private String selectedRole = "student";

    // ==================================================
    // DROPDOWN DATA
    // ==================================================

    private final String[] departments = {
            "IT",
            "CE",
            "EE",
            "Civil",
            "Mechanical",
            "ENTC",
            "AIDS",
            "AI/ML"
    };

    private final String[] years = {
            "1st Year",
            "2nd Year",
            "3rd Year",
            "4th Year"
    };

    private final String[] divisions = {
            "A",
            "B",
            "C",
            "D"
    };

    // ==================================================
    // ON CREATE
    // ==================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        initializeViews();
        initializeFirebase();
        setupDropdowns();
        setupRoleSelection();
        setupListeners();

        selectStudent();
    }

    // ==================================================
    // INITIALIZE VIEWS
    // ==================================================

    private void initializeViews() {

        btnStudent = findViewById(R.id.btnStudent);
        btnTeacher = findViewById(R.id.btnTeacher);

        actDepartment = findViewById(R.id.actDepartment);
        actYear = findViewById(R.id.actYear);
        actDivision = findViewById(R.id.actDivision);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etCollegeId = findViewById(R.id.etCollegeId);
        etRollNumber = findViewById(R.id.etRollNumber);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        studentFields = findViewById(R.id.studentFields);

        btnRegister = findViewById(R.id.btnRegister);
    }

    // ==================================================
    // FIREBASE
    // ==================================================

    private void initializeFirebase() {

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    // ==================================================
    // DROPDOWNS
    // ==================================================

    private void setupDropdowns() {

        ArrayAdapter<String> departmentAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        departments
                );

        actDepartment.setAdapter(departmentAdapter);

        ArrayAdapter<String> yearAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        years
                );

        actYear.setAdapter(yearAdapter);

        ArrayAdapter<String> divisionAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        divisions
                );

        actDivision.setAdapter(divisionAdapter);
    }

    // ==================================================
    // ROLE SELECTION
    // ==================================================

    private void setupRoleSelection() {

        btnStudent.setOnClickListener(
                v -> selectStudent()
        );

        btnTeacher.setOnClickListener(
                v -> selectTeacher()
        );
    }

    // ==================================================
    // SELECT STUDENT
    // ==================================================

    private void selectStudent() {

        selectedRole = "student";

        studentFields.setVisibility(View.VISIBLE);

        btnStudent.setAlpha(1.0f);
        btnTeacher.setAlpha(0.65f);

        btnStudent.setText("🎓  Student");
        btnTeacher.setText("👨‍🏫  Teacher");

        etCollegeId.setHint("Enrollment Number");
    }

    // ==================================================
    // SELECT TEACHER
    // ==================================================

    private void selectTeacher() {

        selectedRole = "teacher";

        studentFields.setVisibility(View.GONE);

        btnStudent.setAlpha(0.65f);
        btnTeacher.setAlpha(1.0f);

        btnStudent.setText("🎓  Student");
        btnTeacher.setText("👨‍🏫  Teacher");

        etCollegeId.setHint("Faculty ID");

        actYear.setText("", false);
        actDivision.setText("", false);
        etRollNumber.setText("");
    }

    // ==================================================
    // LISTENERS
    // ==================================================

    private void setupListeners() {

        btnRegister.setOnClickListener(
                v -> registerUser()
        );

        actDepartment.setOnClickListener(
                v -> actDepartment.showDropDown()
        );

        actYear.setOnClickListener(
                v -> actYear.showDropDown()
        );

        actDivision.setOnClickListener(
                v -> actDivision.showDropDown()
        );
    }

    // ==================================================
    // REGISTER USER
    // ==================================================

    private void registerUser() {

        // --------------------------------------------------
        // GET VALUES
        // --------------------------------------------------

        final String name =
                getText(etName);

        final String email =
                getText(etEmail).toLowerCase();

        final String collegeId =
                getText(etCollegeId);

        final String department =
                actDepartment.getText()
                        .toString()
                        .trim();

        final String year =
                actYear.getText()
                        .toString()
                        .trim();

        final String division =
                actDivision.getText()
                        .toString()
                        .trim();

        final String rollText =
                getText(etRollNumber);

        final String password =
                getText(etPassword);

        final String confirmPassword =
                getText(etConfirmPassword);

        // ==================================================
        // NAME VALIDATION
        // ==================================================

        if (TextUtils.isEmpty(name)) {

            etName.setError("Enter your full name");
            etName.requestFocus();
            return;
        }

        // ==================================================
        // EMAIL VALIDATION
        // ==================================================

        if (TextUtils.isEmpty(email)) {

            etEmail.setError("Enter your email");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }

        // ==================================================
        // COLLEGE / FACULTY ID
        // ==================================================

        if (TextUtils.isEmpty(collegeId)) {

            etCollegeId.setError(
                    selectedRole.equals("student")
                            ? "Enter enrollment number"
                            : "Enter faculty ID"
            );

            etCollegeId.requestFocus();
            return;
        }

        // ==================================================
        // DEPARTMENT
        // ==================================================

        if (TextUtils.isEmpty(department)) {

            actDepartment.setError(
                    "Select your department"
            );

            actDepartment.requestFocus();
            return;
        }

        // ==================================================
        // DEPARTMENT ID
        // ==================================================

        final String departmentId =
                getDepartmentId(department);

        // ==================================================
        // STUDENT DATA
        // ==================================================

        int tempRollNumber = 0;

        String tempClassId = "";
        String tempClassName = "";

        if (selectedRole.equals("student")) {

            // --------------------------------------------------
            // YEAR
            // --------------------------------------------------

            if (TextUtils.isEmpty(year)) {

                actYear.setError("Select year");
                actYear.requestFocus();
                return;
            }

            // --------------------------------------------------
            // DIVISION
            // --------------------------------------------------

            if (TextUtils.isEmpty(division)) {

                actDivision.setError(
                        "Select division"
                );

                actDivision.requestFocus();
                return;
            }

            // --------------------------------------------------
            // ROLL NUMBER
            // --------------------------------------------------

            if (TextUtils.isEmpty(rollText)) {

                etRollNumber.setError(
                        "Enter roll number"
                );

                etRollNumber.requestFocus();
                return;
            }

            try {

                tempRollNumber =
                        Integer.parseInt(rollText);

                if (tempRollNumber <= 0) {

                    etRollNumber.setError(
                            "Enter a valid roll number"
                    );

                    etRollNumber.requestFocus();
                    return;
                }

            } catch (NumberFormatException e) {

                etRollNumber.setError(
                        "Enter a valid roll number"
                );

                etRollNumber.requestFocus();
                return;
            }

            // --------------------------------------------------
            // CLASS ID
            // --------------------------------------------------

            tempClassId =
                    generateClassId(
                            departmentId,
                            year
                    );

            // --------------------------------------------------
            // CLASS NAME
            // --------------------------------------------------

            tempClassName =
                    generateClassName(
                            department,
                            year
                    );
        }

        // ==================================================
        // FINAL VALUES
        // ==================================================
        //
        // These variables are FINAL because they are used
        // inside Firebase lambda callbacks.
        //
        // ==================================================

        final int finalRollNumber =
                tempRollNumber;

        final String finalClassId =
                tempClassId;

        final String finalClassName =
                tempClassName;

        // ==================================================
        // PASSWORD VALIDATION
        // ==================================================

        if (TextUtils.isEmpty(password)) {

            etPassword.setError(
                    "Enter password"
            );

            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {

            etPassword.setError(
                    "Password must be at least 6 characters"
            );

            etPassword.requestFocus();
            return;
        }

        // ==================================================
        // CONFIRM PASSWORD
        // ==================================================

        if (TextUtils.isEmpty(confirmPassword)) {

            etConfirmPassword.setError(
                    "Confirm your password"
            );

            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {

            etConfirmPassword.setError(
                    "Passwords do not match"
            );

            etConfirmPassword.requestFocus();
            return;
        }

        // ==================================================
        // START REGISTRATION
        // ==================================================

        setRegisterButtonLoading(true);

        mAuth.createUserWithEmailAndPassword(
                        email,
                        password
                )
                .addOnCompleteListener(
                        this,
                        task -> {

                            // ==================================================
                            // AUTH FAILED
                            // ==================================================

                            if (!task.isSuccessful()) {

                                setRegisterButtonLoading(false);

                                Toast.makeText(
                                        RegisterActivity.this,
                                        getFirebaseError(
                                                task.getException()
                                        ),
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            // ==================================================
                            // GET FIREBASE USER
                            // ==================================================

                            FirebaseUser firebaseUser =
                                    mAuth.getCurrentUser();

                            if (firebaseUser == null) {

                                setRegisterButtonLoading(false);

                                Toast.makeText(
                                        RegisterActivity.this,
                                        "Unable to create account.",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            final String uid =
                                    firebaseUser.getUid();

                            // ==================================================
                            // CREATE USER DATA
                            // ==================================================

                            Map<String, Object> userData =
                                    new HashMap<>();

                            // --------------------------------------------------
                            // COMMON DATA
                            // --------------------------------------------------

                            userData.put(
                                    "uid",
                                    uid
                            );

                            userData.put(
                                    "name",
                                    name
                            );

                            userData.put(
                                    "email",
                                    email
                            );

                            userData.put(
                                    "role",
                                    selectedRole
                            );

                            userData.put(
                                    "department",
                                    department
                            );

                            userData.put(
                                    "departmentId",
                                    departmentId
                            );

                            // ==================================================
                            // STUDENT DATA
                            // ==================================================

                            if (selectedRole.equals("student")) {

                                userData.put(
                                        "enrollmentNo",
                                        collegeId
                                );

                                userData.put(
                                        "year",
                                        year
                                );

                                userData.put(
                                        "division",
                                        division
                                );

                                userData.put(
                                        "rollNumber",
                                        finalRollNumber
                                );

                                // IMPORTANT:
                                // classId is the actual lecture target.

                                userData.put(
                                        "classId",
                                        finalClassId
                                );

                                userData.put(
                                        "className",
                                        finalClassName
                                );
                            }

                            // ==================================================
                            // TEACHER DATA
                            // ==================================================

                            else {

                                userData.put(
                                        "facultyId",
                                        collegeId
                                );
                            }

                            // ==================================================
                            // SAVE USER PROFILE
                            // ==================================================

                            db.collection("users")
                                    .document(uid)
                                    .set(userData)
                                    .addOnCompleteListener(
                                            firestoreTask -> {

                                                setRegisterButtonLoading(
                                                        false
                                                );

                                                // ==================================================
                                                // FIRESTORE SUCCESS
                                                // ==================================================

                                                if (firestoreTask.isSuccessful()) {

                                                    Toast.makeText(
                                                            RegisterActivity.this,
                                                            "Account created successfully!",
                                                            Toast.LENGTH_LONG
                                                    ).show();

                                                    // Sign out
                                                    mAuth.signOut();

                                                    // Go to login
                                                    Intent intent =
                                                            new Intent(
                                                                    RegisterActivity.this,
                                                                    MainActivity.class
                                                            );

                                                    intent.addFlags(
                                                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                                                                    | Intent.FLAG_ACTIVITY_NEW_TASK
                                                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                    );

                                                    startActivity(intent);

                                                    finish();

                                                } else {

                                                    // ==================================================
                                                    // FIRESTORE FAILED
                                                    // ==================================================

                                                    Toast.makeText(
                                                            RegisterActivity.this,
                                                            "Account created, but profile could not be saved.",
                                                            Toast.LENGTH_LONG
                                                    ).show();
                                                }
                                            }
                                    );
                        }
                );
    }

    // ==================================================
    // GENERATE CLASS ID
    // ==================================================

    private String generateClassId(
            String departmentId,
            String year
    ) {

        String yearNumber;

        switch (year) {

            case "1st Year":
                yearNumber = "1";
                break;

            case "2nd Year":
                yearNumber = "2";
                break;

            case "3rd Year":
                yearNumber = "3";
                break;

            case "4th Year":
                yearNumber = "4";
                break;

            default:
                yearNumber = "0";
                break;
        }

        return departmentId + "_" + yearNumber;
    }

    // ==================================================
    // GENERATE CLASS NAME
    // ==================================================

    private String generateClassName(
            String department,
            String year
    ) {

        String yearName;

        switch (year) {

            case "1st Year":
                yearName = "1st";
                break;

            case "2nd Year":
                yearName = "2nd";
                break;

            case "3rd Year":
                yearName = "3rd";
                break;

            case "4th Year":
                yearName = "4th";
                break;

            default:
                yearName = year;
                break;
        }

        return yearName + " " + department;
    }

    // ==================================================
    // GET TEXT
    // ==================================================

    private String getText(
            TextInputEditText editText
    ) {

        if (editText.getText() == null) {
            return "";
        }

        return editText.getText()
                .toString()
                .trim();
    }

    // ==================================================
    // DEPARTMENT ID
    // ==================================================

    private String getDepartmentId(
            String department
    ) {

        switch (department) {

            case "IT":
                return "it";

            case "CE":
                return "ce";

            case "EE":
                return "ee";

            case "Civil":
                return "civil";

            case "Mechanical":
                return "mechanical";

            case "ENTC":
                return "entc";

            case "AIDS":
                return "aids";

            case "AI/ML":
                return "ai_ml";

            default:
                return department
                        .toLowerCase()
                        .replace("/", "_")
                        .replace(" ", "_");
        }
    }

    // ==================================================
    // REGISTER BUTTON LOADING
    // ==================================================

    private void setRegisterButtonLoading(
            boolean loading
    ) {

        if (loading) {

            btnRegister.setEnabled(false);

            btnRegister.setText(
                    "Creating Account..."
            );

        } else {

            btnRegister.setEnabled(true);

            btnRegister.setText(
                    "CREATE ACCOUNT"
            );
        }
    }

    // ==================================================
    // FIREBASE ERROR
    // ==================================================

    private String getFirebaseError(
            Exception exception
    ) {

        if (exception == null) {

            return "Registration failed. Please try again.";
        }

        String message =
                exception.getMessage();

        if (message == null) {

            return "Registration failed. Please try again.";
        }

        if (message.contains(
                "email-already-in-use"
        )) {

            return "This email is already registered.";
        }

        if (message.contains(
                "invalid-email"
        )) {

            return "Invalid email address.";
        }

        if (message.contains(
                "weak-password"
        )) {

            return "Password is too weak.";
        }

        if (message.contains(
                "network"
        )) {

            return "Network error. Check your internet connection.";
        }

        return "Registration failed. Please try again.";
    }
}