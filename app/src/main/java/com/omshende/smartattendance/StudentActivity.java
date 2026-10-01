package com.omshende.smartattendance;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.omshende.smartattendance.student.ActiveLectureFragment;
import com.omshende.smartattendance.student.AttendanceFragment;
import com.omshende.smartattendance.student.HistoryFragment;
import com.omshende.smartattendance.student.StudentDashboardFragment;
import com.omshende.smartattendance.student.StudentProfileFragment;

public class StudentActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_student);

        initializeViews();
        setupBottomNavigation();

        // Open Dashboard by default
        if (savedInstanceState == null) {
            loadFragment(new StudentDashboardFragment());
        }
    }

    private void initializeViews() {

        bottomNavigationView =
                findViewById(R.id.bottomNavigationView);
    }

    private void setupBottomNavigation() {

        bottomNavigationView.setOnItemSelectedListener(item -> {

            Fragment selectedFragment = null;

            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {

                selectedFragment =
                        new StudentDashboardFragment();

            } else if (itemId == R.id.nav_lectures) {

                selectedFragment =
                        new ActiveLectureFragment();

            } else if (itemId == R.id.nav_attendance) {

                selectedFragment =
                        new AttendanceFragment();

            } else if (itemId == R.id.nav_history) {

                selectedFragment =
                        new HistoryFragment();

            } else if (itemId == R.id.nav_profile) {

                selectedFragment =
                        new StudentProfileFragment();
            }

            if (selectedFragment != null) {

                loadFragment(selectedFragment);

                return true;
            }

            return false;
        });

        // Select Home initially
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }

    private void loadFragment(Fragment fragment) {

        getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.studentFragmentContainer,
                        fragment
                )
                .commit();
    }
}