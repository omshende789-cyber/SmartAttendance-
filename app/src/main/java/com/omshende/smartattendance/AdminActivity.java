package com.omshende.smartattendance;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.omshende.smartattendance.admin.AdminDashboardFragment;
import com.omshende.smartattendance.admin.AdminProfileFragment;
import com.omshende.smartattendance.admin.ClassesFragment;
import com.omshende.smartattendance.admin.StudentsFragment;
import com.omshende.smartattendance.admin.TeachersFragment;

public class AdminActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_admin);

        initializeViews();

        setupBottomNavigation();

        if (savedInstanceState == null) {

            loadFragment(
                    new AdminDashboardFragment()
            );
        }
    }

    // ==================================================
    // INITIALIZE VIEWS
    // ==================================================

    private void initializeViews() {

        bottomNavigationView =
                findViewById(
                        R.id.adminBottomNavigationView
                );
    }

    // ==================================================
    // BOTTOM NAVIGATION
    // ==================================================

    private void setupBottomNavigation() {

        bottomNavigationView.setOnItemSelectedListener(
                item -> {

                    Fragment selectedFragment = null;

                    int itemId =
                            item.getItemId();

                    // ==========================================
                    // DASHBOARD
                    // ==========================================

                    if (itemId ==
                            R.id.admin_nav_home) {

                        selectedFragment =
                                new AdminDashboardFragment();
                    }

                    // ==========================================
                    // STUDENTS
                    // ==========================================

                    else if (itemId ==
                            R.id.admin_nav_students) {

                        selectedFragment =
                                new StudentsFragment();
                    }

                    // ==========================================
                    // TEACHERS
                    // ==========================================

                    else if (itemId ==
                            R.id.admin_nav_teachers) {

                        selectedFragment =
                                new TeachersFragment();
                    }

                    // ==========================================
                    // CLASSES
                    // ==========================================

                    else if (itemId ==
                            R.id.admin_nav_classes) {

                        selectedFragment =
                                new ClassesFragment();
                    }

                    // ==========================================
                    // PROFILE
                    // ==========================================

                    else if (itemId ==
                            R.id.admin_nav_profile) {

                        selectedFragment =
                                new AdminProfileFragment();
                    }

                    // ==========================================
                    // LOAD FRAGMENT
                    // ==========================================

                    if (selectedFragment != null) {

                        loadFragment(
                                selectedFragment
                        );

                        return true;
                    }

                    return false;
                }
        );

        // ==============================================
        // DEFAULT TAB
        // ==============================================

        bottomNavigationView.setSelectedItemId(
                R.id.admin_nav_home
        );
    }

    // ==================================================
    // LOAD FRAGMENT
    // ==================================================

    private void loadFragment(
            Fragment fragment
    ) {

        getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.adminFragmentContainer,
                        fragment
                )
                .commit();
    }
}