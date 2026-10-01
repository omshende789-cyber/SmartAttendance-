package com.omshende.smartattendance;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.omshende.smartattendance.teacher.LecturesFragment;
import com.omshende.smartattendance.teacher.MyClassesFragment;
import com.omshende.smartattendance.teacher.ReportsFragment;
import com.omshende.smartattendance.teacher.TeacherDashboardFragment;
import com.omshende.smartattendance.teacher.TeacherProfileFragment;

public class TeacherActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        initializeViews();
        setupBottomNavigation();

        if (savedInstanceState == null) {
            loadFragment(new TeacherDashboardFragment());
        }
    }

    private void initializeViews() {
        bottomNavigationView = findViewById(R.id.teacherBottomNavigationView);
    }

    private void setupBottomNavigation() {

        bottomNavigationView.setOnItemSelectedListener(item -> {

            Fragment selectedFragment = null;

            int itemId = item.getItemId();

            if (itemId == R.id.teacher_nav_home) {

                selectedFragment = new TeacherDashboardFragment();

            } else if (itemId == R.id.teacher_nav_classes) {

                selectedFragment = new MyClassesFragment();

            } else if (itemId == R.id.teacher_nav_lectures) {

                selectedFragment = new LecturesFragment();

            } else if (itemId == R.id.teacher_nav_reports) {

                selectedFragment = new ReportsFragment();

            } else if (itemId == R.id.teacher_nav_profile) {

                selectedFragment = new TeacherProfileFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }

            return false;
        });

        bottomNavigationView.setSelectedItemId(R.id.teacher_nav_home);
    }

    private void loadFragment(Fragment fragment) {

        getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.teacherFragmentContainer,
                        fragment
                )
                .commit();
    }
}