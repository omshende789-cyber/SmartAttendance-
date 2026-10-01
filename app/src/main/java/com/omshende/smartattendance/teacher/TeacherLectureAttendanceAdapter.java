package com.omshende.smartattendance.teacher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.omshende.smartattendance.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TeacherLectureAttendanceAdapter
        extends RecyclerView.Adapter<TeacherLectureAttendanceAdapter.ViewHolder> {

    // =========================================================
    // DISPLAYED LIST
    // =========================================================

    private final List<TeacherAttendance> attendanceList =
            new ArrayList<>();

    // =========================================================
    // COMPLETE ORIGINAL LIST
    // =========================================================

    private final List<TeacherAttendance> fullList =
            new ArrayList<>();

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public TeacherLectureAttendanceAdapter(
            List<TeacherAttendance> initialList
    ) {

        if (initialList != null) {

            fullList.addAll(initialList);
            attendanceList.addAll(initialList);
        }
    }

    // =========================================================
    // CREATE VIEW HOLDER
    // =========================================================

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(
                        R.layout.item_teacher_lecture_attendance,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    // =========================================================
    // BIND VIEW HOLDER
    // =========================================================

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {

        TeacherAttendance student =
                attendanceList.get(position);

        // -----------------------------------------------------
        // SERIAL NUMBER
        // -----------------------------------------------------

        holder.tvSerial.setText(
                String.valueOf(position + 1)
        );

        // -----------------------------------------------------
        // STUDENT NAME
        // -----------------------------------------------------

        holder.tvStudentName.setText(
                safe(
                        student.getStudentName()
                )
        );

        // -----------------------------------------------------
        // ENROLLMENT NUMBER
        // -----------------------------------------------------

        holder.tvEnrollment.setText(
                "Enrollment: "
                        + safe(
                        student.getEnrollmentNo()
                )
        );

        // -----------------------------------------------------
        // DIVISION
        // -----------------------------------------------------

        holder.tvDivision.setText(
                safe(
                        student.getDivision()
                )
        );

        // -----------------------------------------------------
        // ROLL NUMBER
        // -----------------------------------------------------

        holder.tvRoll.setText(
                String.format(
                        Locale.getDefault(),
                        "%02d",
                        student.getRollNumber()
                )
        );

        // -----------------------------------------------------
        // ATTENDANCE STATUS
        // -----------------------------------------------------

        holder.tvStatus.setText(
                safe(
                        student.getStatus()
                )
        );
    }

    // =========================================================
    // ITEM COUNT
    // =========================================================

    @Override
    public int getItemCount() {

        return attendanceList.size();
    }

    // =========================================================
    // SET / REFRESH COMPLETE DATA
    // =========================================================

    public void setData(
            List<TeacherAttendance> newList
    ) {

        // Clear currently displayed data
        attendanceList.clear();

        // Clear original data
        fullList.clear();

        // Add fresh data
        if (newList != null) {

            fullList.addAll(
                    newList
            );

            attendanceList.addAll(
                    newList
            );
        }

        notifyDataSetChanged();
    }

    // =========================================================
    // FILTER + SEARCH
    // =========================================================

    public void filter(
            String query,
            String selectedStatus
    ) {

        // -----------------------------------------------------
        // SEARCH TEXT
        // -----------------------------------------------------

        String search = "";

        if (query != null) {

            search = query
                    .trim()
                    .toLowerCase(
                            Locale.getDefault()
                    );
        }

        // -----------------------------------------------------
        // STATUS
        // -----------------------------------------------------

        String status =
                selectedStatus == null
                        ? "ALL"
                        : selectedStatus.trim();

        // -----------------------------------------------------
        // NEW FILTERED LIST
        // -----------------------------------------------------

        List<TeacherAttendance> filteredList =
                new ArrayList<>();

        // =====================================================
        // IMPORTANT:
        // ALWAYS SEARCH fullList
        // NOT attendanceList
        // =====================================================

        for (
                TeacherAttendance student
                : fullList
        ) {

            // -------------------------------------------------
            // NAME
            // -------------------------------------------------

            String name =
                    safe(
                            student.getStudentName()
                    ).toLowerCase(
                            Locale.getDefault()
                    );

            // -------------------------------------------------
            // ENROLLMENT
            // -------------------------------------------------

            String enrollment =
                    safe(
                            student.getEnrollmentNo()
                    ).toLowerCase(
                            Locale.getDefault()
                    );

            // -------------------------------------------------
            // DIVISION
            // -------------------------------------------------

            String division =
                    safe(
                            student.getDivision()
                    ).toLowerCase(
                            Locale.getDefault()
                    );

            // -------------------------------------------------
            // ROLL NUMBER
            // -------------------------------------------------

            String roll =
                    String.valueOf(
                            student.getRollNumber()
                    );

            // -------------------------------------------------
            // SEARCH MATCH
            // -------------------------------------------------

            boolean searchMatch =
                    search.isEmpty()
                            || name.contains(search)
                            || enrollment.contains(search)
                            || division.contains(search)
                            || roll.contains(search);

            // -------------------------------------------------
            // STATUS MATCH
            // -------------------------------------------------

            String studentStatus =
                    safe(
                            student.getStatus()
                    );

            boolean statusMatch =
                    status.equalsIgnoreCase("ALL")
                            || studentStatus.equalsIgnoreCase(
                            status
                    );

            // -------------------------------------------------
            // FINAL MATCH
            // -------------------------------------------------

            if (
                    searchMatch
                            && statusMatch
            ) {

                filteredList.add(
                        student
                );
            }
        }

        // =====================================================
        // UPDATE DISPLAYED LIST
        // =====================================================

        attendanceList.clear();

        attendanceList.addAll(
                filteredList
        );

        notifyDataSetChanged();
    }

    // =========================================================
    // SHOW ALL STUDENTS
    // =========================================================

    public void showAll() {

        attendanceList.clear();

        attendanceList.addAll(
                fullList
        );

        notifyDataSetChanged();
    }

    // =========================================================
    // GET COMPLETE LIST COUNT
    // =========================================================

    public int getFullListCount() {

        return fullList.size();
    }

    // =========================================================
    // GET DISPLAYED LIST COUNT
    // =========================================================

    public int getDisplayedListCount() {

        return attendanceList.size();
    }

    // =========================================================
    // SAFE STRING
    // =========================================================

    private String safe(
            String value
    ) {

        if (
                value == null
                        || value.trim().isEmpty()
        ) {

            return "";
        }

        return value.trim();
    }

    // =========================================================
    // VIEW HOLDER
    // =========================================================

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvSerial;
        TextView tvStudentName;
        TextView tvEnrollment;
        TextView tvDivision;
        TextView tvRoll;
        TextView tvStatus;

        public ViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            // -------------------------------------------------
            // SERIAL
            // -------------------------------------------------

            tvSerial =
                    itemView.findViewById(
                            R.id.tvAttendanceSerial
                    );

            // -------------------------------------------------
            // STUDENT NAME
            // -------------------------------------------------

            tvStudentName =
                    itemView.findViewById(
                            R.id.tvAttendanceStudentName
                    );

            // -------------------------------------------------
            // ENROLLMENT
            // -------------------------------------------------

            tvEnrollment =
                    itemView.findViewById(
                            R.id.tvAttendanceEnrollment
                    );

            // -------------------------------------------------
            // DIVISION
            // -------------------------------------------------

            tvDivision =
                    itemView.findViewById(
                            R.id.tvAttendanceDivision
                    );

            // -------------------------------------------------
            // ROLL NUMBER
            // -------------------------------------------------

            tvRoll =
                    itemView.findViewById(
                            R.id.tvAttendanceRoll
                    );

            // ------------------------------------------------- 
            // STATUS
            // -------------------------------------------------

            tvStatus =
                    itemView.findViewById(
                            R.id.tvAttendanceStatus
                    );
        }
    }
}