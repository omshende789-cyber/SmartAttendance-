package com.omshende.smartattendance.admin;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.omshende.smartattendance.R;

import java.util.List;
import java.util.Locale;

public class StudentAdapter
        extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private final List<DocumentSnapshot> studentList;

    private final OnStudentClickListener listener;

    public interface OnStudentClickListener {

        void onStudentClick(
                DocumentSnapshot student
        );
    }

    public StudentAdapter(
            List<DocumentSnapshot> studentList,
            OnStudentClickListener listener
    ) {

        this.studentList = studentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(
                                R.layout.item_admin_student,
                                parent,
                                false
                        );

        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull StudentViewHolder holder,
            int position
    ) {

        if (studentList == null ||
                position < 0 ||
                position >= studentList.size()) {

            return;
        }

        DocumentSnapshot student =
                studentList.get(position);

        if (student == null) {
            return;
        }

        String name =
                getValue(student, "name");

        String email =
                getValue(student, "email");

        String enrollment =
                getValue(student, "enrollmentNo");

        String department =
                getValue(student, "department");

        String year =
                getValue(student, "year");

        String division =
                getValue(student, "division");

        String rollNumber =
                getValue(student, "rollNumber");

        String className =
                getValue(student, "className");

        // NAME
        holder.tvStudentName.setText(
                TextUtils.isEmpty(name)
                        ? "Unknown Student"
                        : name
        );

        // EMAIL
        holder.tvStudentEmail.setText(
                TextUtils.isEmpty(email)
                        ? "Email not available"
                        : email
        );

        // ENROLLMENT
        holder.tvEnrollment.setText(
                TextUtils.isEmpty(enrollment)
                        ? "Enrollment not available"
                        : "Enrollment: " + enrollment
        );

        // CLASS
        StringBuilder classInfo =
                new StringBuilder();

        if (!TextUtils.isEmpty(className)) {

            classInfo.append(
                    className.trim()
            );

        } else {

            if (!TextUtils.isEmpty(year)) {

                classInfo.append(
                        year.trim()
                );
            }

            if (!TextUtils.isEmpty(department)) {

                if (classInfo.length() > 0) {
                    classInfo.append(" • ");
                }

                classInfo.append(
                        department.trim()
                );
            }
        }

        // DIVISION
        if (!TextUtils.isEmpty(division)) {

            if (classInfo.length() > 0) {
                classInfo.append(" • ");
            }

            classInfo.append(
                    "Division "
            );

            classInfo.append(
                    division.trim()
            );
        }

        // ROLL
        if (!TextUtils.isEmpty(rollNumber)) {

            if (classInfo.length() > 0) {
                classInfo.append(" • ");
            }

            classInfo.append(
                    "Roll "
            );

            classInfo.append(
                    rollNumber.trim()
            );
        }

        holder.tvStudentClass.setText(
                classInfo.length() > 0
                        ? classInfo.toString()
                        : "Class information not available"
        );

        // INITIAL
        holder.tvStudentInitial.setText(
                getInitial(name)
        );

        // CLICK
        holder.itemView.setOnClickListener(
                v -> {

                    if (listener != null) {

                        listener.onStudentClick(
                                student
                        );
                    }
                }
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

    private String getInitial(
            String name
    ) {

        if (TextUtils.isEmpty(name)) {
            return "S";
        }

        String cleanName =
                name.trim();

        if (cleanName.isEmpty()) {
            return "S";
        }

        return cleanName
                .substring(0, 1)
                .toUpperCase(
                        Locale.ROOT
                );
    }

    @Override
    public int getItemCount() {

        return studentList == null
                ? 0
                : studentList.size();
    }

    public static class StudentViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvStudentInitial;
        TextView tvStudentName;
        TextView tvStudentEmail;
        TextView tvEnrollment;
        TextView tvStudentClass;

        public StudentViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            tvStudentInitial =
                    itemView.findViewById(
                            R.id.tvStudentInitial
                    );

            tvStudentName =
                    itemView.findViewById(
                            R.id.tvStudentName
                    );

            tvStudentEmail =
                    itemView.findViewById(
                            R.id.tvStudentEmail
                    );

            tvEnrollment =
                    itemView.findViewById(
                            R.id.tvEnrollment
                    );

            tvStudentClass =
                    itemView.findViewById(
                            R.id.tvStudentClass
                    );
        }
    }
}