package com.omshende.smartattendance.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.omshende.smartattendance.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StudentsFragment extends Fragment {

    private RecyclerView recyclerStudents;
    private ProgressBar progressLoading;
    private TextView tvEmptyStudents;
    private TextView tvStudentCount;
    private TextInputEditText etSearchStudents;

    private FirebaseFirestore db;

    private final List<DocumentSnapshot> allStudents =
            new ArrayList<>();

    private final List<DocumentSnapshot> filteredStudents =
            new ArrayList<>();

    private StudentAdapter adapter;

    public StudentsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View view = inflater.inflate(
                R.layout.fragment_students,
                container,
                false
        );

        db = FirebaseFirestore.getInstance();

        initializeViews(view);
        setupRecyclerView();
        setupSearch();
        loadStudents();

        return view;
    }

    private void initializeViews(View view) {

        recyclerStudents =
                view.findViewById(R.id.recyclerStudents);

        progressLoading =
                view.findViewById(R.id.progressLoading);

        tvEmptyStudents =
                view.findViewById(R.id.tvEmptyStudents);

        tvStudentCount =
                view.findViewById(R.id.tvStudentCount);

        etSearchStudents =
                view.findViewById(R.id.etSearchStudents);
    }

    private void setupRecyclerView() {

        adapter = new StudentAdapter(
                filteredStudents,
                student -> {

                    if (student == null) {
                        return;
                    }

                    String studentId =
                            student.getId();

                    if (studentId == null ||
                            studentId.trim().isEmpty()) {

                        Toast.makeText(
                                requireContext(),
                                "Student ID not found.",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    Intent intent = new Intent(
                            requireContext(),
                            StudentDetailsActivity.class
                    );

                    intent.putExtra(
                            "studentId",
                            studentId
                    );

                    startActivity(intent);
                }
        );

        recyclerStudents.setLayoutManager(
                new LinearLayoutManager(
                        requireContext()
                )
        );

        recyclerStudents.setAdapter(adapter);
    }

    private void setupSearch() {

        etSearchStudents.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {

                        filterStudents(
                                s == null
                                        ? ""
                                        : s.toString()
                        );
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
        );
    }

    private void loadStudents() {

        if (!isAdded()) {
            return;
        }

        showLoading(true);

        db.collection("users")
                .whereEqualTo(
                        "role",
                        "student"
                )
                .get()
                .addOnCompleteListener(task -> {

                    if (!isAdded()) {
                        return;
                    }

                    showLoading(false);

                    if (!task.isSuccessful()) {

                        allStudents.clear();
                        filteredStudents.clear();

                        adapter.notifyDataSetChanged();

                        updateCount();
                        showEmptyState(true);

                        Toast.makeText(
                                requireContext(),
                                "Unable to load students.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    allStudents.clear();

                    if (task.getResult() != null) {

                        allStudents.addAll(
                                task.getResult()
                                        .getDocuments()
                        );
                    }

                    String search = "";

                    if (etSearchStudents != null &&
                            etSearchStudents.getText() != null) {

                        search =
                                etSearchStudents
                                        .getText()
                                        .toString();
                    }

                    filterStudents(search);
                });
    }

    private void filterStudents(
            String searchText
    ) {

        filteredStudents.clear();

        String query =
                searchText == null
                        ? ""
                        : searchText
                        .trim()
                        .toLowerCase(Locale.ROOT);

        for (DocumentSnapshot student :
                allStudents) {

            if (student == null) {
                continue;
            }

            String name =
                    getValue(student, "name");

            String email =
                    getValue(student, "email");

            String enrollment =
                    getValue(student, "enrollmentNo");

            String department =
                    getValue(student, "department");

            String className =
                    getValue(student, "className");

            String year =
                    getValue(student, "year");

            String division =
                    getValue(student, "division");

            String rollNumber =
                    getValue(student, "rollNumber");

            String searchableText =
                    (
                            name + " "
                                    + email + " "
                                    + enrollment + " "
                                    + department + " "
                                    + className + " "
                                    + year + " "
                                    + division + " "
                                    + rollNumber
                    ).toLowerCase(Locale.ROOT);

            if (query.isEmpty() ||
                    searchableText.contains(query)) {

                filteredStudents.add(student);
            }
        }

        adapter.notifyDataSetChanged();

        updateCount();

        showEmptyState(
                filteredStudents.isEmpty()
        );
    }

    private String getValue(
            DocumentSnapshot document,
            String field
    ) {

        if (document == null ||
                field == null) {

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

    private void updateCount() {

        int count =
                filteredStudents.size();

        tvStudentCount.setText(
                count +
                        " student" +
                        (count == 1 ? "" : "s")
        );
    }

    private void showLoading(
            boolean loading
    ) {

        progressLoading.setVisibility(
                loading
                        ? View.VISIBLE
                        : View.GONE
        );

        if (loading) {

            recyclerStudents.setVisibility(
                    View.GONE
            );

            tvEmptyStudents.setVisibility(
                    View.GONE
            );
        }
    }

    private void showEmptyState(
            boolean show
    ) {

        if (show) {

            recyclerStudents.setVisibility(
                    View.GONE
            );

            tvEmptyStudents.setVisibility(
                    View.VISIBLE
            );

        } else {

            recyclerStudents.setVisibility(
                    View.VISIBLE
            );

            tvEmptyStudents.setVisibility(
                    View.GONE
            );
        }
    }

    @Override
    public void onResume() {

        super.onResume();

        if (db != null && isAdded()) {
            loadStudents();
        }
    }
}