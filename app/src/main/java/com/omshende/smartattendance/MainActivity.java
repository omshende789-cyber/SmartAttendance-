package com.omshende.smartattendance;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private Button btnLogin;
    private TextView tvRegister;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initializeFirebase();
        initializeViews();
        setupListeners();
    }

    private void initializeFirebase() {

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void initializeViews() {

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
    }

    private void setupListeners() {

        btnLogin.setOnClickListener(v -> loginUser());

        tvRegister.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    RegisterActivity.class
            );

            startActivity(intent);
        });
    }

    private void loginUser() {

        String email = etEmail.getText() != null
                ? etEmail.getText().toString().trim()
                : "";

        String password = etPassword.getText() != null
                ? etPassword.getText().toString()
                : "";


        // Email validation
        if (TextUtils.isEmpty(email)) {

            etEmail.setError("Enter your email");
            etEmail.requestFocus();

            return;
        }


        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();

            return;
        }


        // Password validation
        if (TextUtils.isEmpty(password)) {

            etPassword.setError("Enter your password");
            etPassword.requestFocus();

            return;
        }


        // Disable login button
        btnLogin.setEnabled(false);
        btnLogin.setText("LOGGING IN...");


        // Firebase Authentication
        mAuth.signInWithEmailAndPassword(
                        email,
                        password
                )
                .addOnCompleteListener(this, task -> {

                    if (!task.isSuccessful()) {

                        enableLoginButton();

                        Toast.makeText(
                                MainActivity.this,
                                getFirebaseError(
                                        task.getException()
                                ),
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }


                    if (mAuth.getCurrentUser() == null) {

                        enableLoginButton();

                        Toast.makeText(
                                MainActivity.this,
                                "Unable to get user information.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }


                    String uid =
                            mAuth.getCurrentUser().getUid();


                    loadUserRole(uid);
                });
    }


    // --------------------------------------------------
    // LOAD USER ROLE
    // --------------------------------------------------

    private void loadUserRole(String uid) {

        db.collection("users")
                .document(uid)
                .get()
                .addOnCompleteListener(task -> {

                    enableLoginButton();


                    if (!task.isSuccessful()) {

                        Toast.makeText(
                                MainActivity.this,
                                "Unable to load user profile.",
                                Toast.LENGTH_LONG
                        ).show();

                        mAuth.signOut();

                        return;
                    }


                    if (task.getResult() == null
                            || !task.getResult().exists()) {

                        Toast.makeText(
                                MainActivity.this,
                                "User profile not found.",
                                Toast.LENGTH_LONG
                        ).show();

                        mAuth.signOut();

                        return;
                    }


                    String role =
                            task.getResult()
                                    .getString("role");


                    if (TextUtils.isEmpty(role)) {

                        Toast.makeText(
                                MainActivity.this,
                                "User role is not configured.",
                                Toast.LENGTH_LONG
                        ).show();

                        mAuth.signOut();

                        return;
                    }


                    openDashboard(role);
                });
    }


    // --------------------------------------------------
    // OPEN DASHBOARD
    // --------------------------------------------------

    private void openDashboard(String role) {

        Intent intent;


        switch (role.toLowerCase()) {

            case "student":

                intent = new Intent(
                        MainActivity.this,
                        StudentActivity.class
                );

                break;


            case "teacher":

                intent = new Intent(
                        MainActivity.this,
                        TeacherActivity.class
                );

                break;


            case "admin":

                intent = new Intent(
                        MainActivity.this,
                        AdminActivity.class
                );

                break;


            default:

                Toast.makeText(
                        MainActivity.this,
                        "Invalid user role.",
                        Toast.LENGTH_LONG
                ).show();

                mAuth.signOut();

                return;
        }


        startActivity(intent);

        finish();
    }


    // --------------------------------------------------
    // ENABLE LOGIN BUTTON
    // --------------------------------------------------

    private void enableLoginButton() {

        btnLogin.setEnabled(true);
        btnLogin.setText("LOGIN");
    }


    // --------------------------------------------------
    // FIREBASE ERROR
    // --------------------------------------------------

    private String getFirebaseError(Exception exception) {

        if (exception == null) {

            return "Login failed. Please try again.";
        }


        String message =
                exception.getMessage();


        if (message == null) {

            return "Login failed. Please try again.";
        }


        if (message.contains("invalid-credential")
                || message.contains("password is invalid")) {

            return "Incorrect email or password.";
        }


        if (message.contains("user-not-found")) {

            return "No account found with this email.";
        }


        if (message.contains("invalid-email")) {

            return "Invalid email address.";
        }


        if (message.contains("user-disabled")) {

            return "This account has been disabled.";
        }


        if (message.contains("too-many-requests")) {

            return "Too many login attempts. Please try again later.";
        }


        if (message.contains("network")) {

            return "Network error. Check your internet connection.";
        }


        return "Login failed. Please try again.";
    }
}