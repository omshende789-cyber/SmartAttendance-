package com.omshende.smartattendance.teacher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.omshende.smartattendance.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TeacherLecturesAdapter
        extends RecyclerView.Adapter<TeacherLecturesAdapter.ViewHolder> {

    // =========================================================
    // LECTURE CLICK LISTENER
    // =========================================================

    public interface OnLectureClickListener {
        void onLectureClick(DocumentSnapshot lecture);
    }

    // =========================================================
    // VARIABLES
    // =========================================================

    private final List<DocumentSnapshot> lectures;
    private final OnLectureClickListener listener;

    /*
     * Stores attendance count for each lecture.
     *
     * Key   = lectureId
     * Value = "Attendance: Present / Total"
     */
    private final Map<String, String> attendanceCounts =
            new HashMap<>();

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public TeacherLecturesAdapter(
            List<DocumentSnapshot> lectures,
            OnLectureClickListener listener
    ) {

        this.lectures = lectures;
        this.listener = listener;
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

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_teacher_lecture,
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

        DocumentSnapshot lecture =
                lectures.get(position);

        // -----------------------------------------------------
        // BASIC LECTURE DATA
        // -----------------------------------------------------

        String subject =
                getValue(
                        lecture,
                        "subjectName"
                );

        String className =
                getClassNames(lecture);

        String room =
                getValue(
                        lecture,
                        "roomNo"
                );

        String status =
                getValue(
                        lecture,
                        "status"
                );

        // -----------------------------------------------------
        // SET SUBJECT
        // -----------------------------------------------------

        holder.tvSubject.setText(
                safe(subject)
        );

        // -----------------------------------------------------
        // SET CLASS
        // -----------------------------------------------------

        holder.tvClass.setText(
                safe(className)
        );

        // -----------------------------------------------------
        // SET ROOM
        // -----------------------------------------------------

        holder.tvRoom.setText(
                "Room: " + safe(room)
        );

        // -----------------------------------------------------
        // SET STATUS
        // -----------------------------------------------------

        holder.tvStatus.setText(
                safe(status)
        );

        // -----------------------------------------------------
        // SET DATE
        // -----------------------------------------------------

        holder.tvDate.setText(
                getLectureDate(lecture)
        );

        // -----------------------------------------------------
        // SET ATTENDANCE
        // -----------------------------------------------------

        String lectureId =
                lecture.getId();

        String attendanceText =
                attendanceCounts.get(lectureId);

        if (attendanceText == null) {

            holder.tvAttendance.setText(
                    "Attendance: Loading..."
            );

        } else {

            holder.tvAttendance.setText(
                    attendanceText
            );
        }

        // -----------------------------------------------------
        // CARD CLICK
        // -----------------------------------------------------

        holder.card.setOnClickListener(v -> {

            if (listener != null) {

                listener.onLectureClick(
                        lecture
                );
            }
        });
    }

    // =========================================================
    // GET CLASS NAMES
    // =========================================================

    private String getClassNames(
            DocumentSnapshot lecture
    ) {

        /*
         * New lecture structure:
         *
         * targetClassNames:
         * [
         *   "1st IT",
         *   "2nd IT",
         *   "3rd IT"
         * ]
         */

        Object value =
                lecture.get("targetClassNames");

        if (value instanceof List) {

            List<?> list =
                    (List<?>) value;

            if (!list.isEmpty()) {

                StringBuilder builder =
                        new StringBuilder();

                for (
                        int i = 0;
                        i < list.size();
                        i++
                ) {

                    Object item =
                            list.get(i);

                    if (item == null) {
                        continue;
                    }

                    String className =
                            String.valueOf(item).trim();

                    if (className.isEmpty()) {
                        continue;
                    }

                    if (builder.length() > 0) {

                        builder.append(" + ");
                    }

                    builder.append(
                            className
                    );
                }

                if (builder.length() > 0) {

                    return builder.toString();
                }
            }
        }

        // -----------------------------------------------------
        // BACKWARD COMPATIBILITY
        // -----------------------------------------------------

        String className =
                getValue(
                        lecture,
                        "className"
                );

        return className;
    }

    // =========================================================
    // GET LECTURE DATE
    // =========================================================

    private String getLectureDate(
            DocumentSnapshot lecture
    ) {

        Object value =
                lecture.get("createdAt");

        if (value instanceof Timestamp) {

            Timestamp timestamp =
                    (Timestamp) value;

            Date date =
                    timestamp.toDate();

            SimpleDateFormat format =
                    new SimpleDateFormat(
                            "dd MMM yyyy, hh:mm a",
                            Locale.getDefault()
                    );

            return format.format(date);
        }

        return "Date not available";
    }

    // =========================================================
    // SAFE FIRESTORE VALUE
    // =========================================================

    private String getValue(
            DocumentSnapshot document,
            String field
    ) {

        Object value =
                document.get(field);

        if (value == null) {

            return "";
        }

        // -----------------------------------------------------
        // NUMBER HANDLING
        // -----------------------------------------------------

        if (value instanceof Number) {

            Number number =
                    (Number) value;

            double doubleValue =
                    number.doubleValue();

            long longValue =
                    number.longValue();

            if (doubleValue == longValue) {

                return String.valueOf(
                        longValue
                );
            }

            return String.valueOf(
                    doubleValue
            );
        }

        return String.valueOf(value);
    }

    // =========================================================
    // SAFE TEXT
    // =========================================================

    private String safe(String value) {

        if (
                value == null
                        || value.trim().isEmpty()
        ) {

            return "-";
        }

        return value.trim();
    }

    // =========================================================
    // ITEM COUNT
    // =========================================================

    @Override
    public int getItemCount() {

        return lectures.size();
    }

    // =========================================================
    // SET ATTENDANCE COUNT
    // =========================================================

    public void setAttendanceCount(
            String lectureId,
            int present,
            int total
    ) {

        if (lectureId == null) {

            return;
        }

        /*
         * Store the count.
         *
         * Example:
         * Attendance: 25 / 40
         */

        String attendanceText =
                "Attendance: "
                        + present
                        + " / "
                        + total;

        attendanceCounts.put(
                lectureId,
                attendanceText
        );

        // -----------------------------------------------------
        // REFRESH ONLY THAT LECTURE
        // -----------------------------------------------------

        for (
                int i = 0;
                i < lectures.size();
                i++
        ) {

            DocumentSnapshot lecture =
                    lectures.get(i);

            if (
                    lecture.getId()
                            .equals(lectureId)
            ) {

                notifyItemChanged(i);

                break;
            }
        }
    }

    // =========================================================
    // CLEAR ATTENDANCE COUNTS
    // =========================================================

    public void clearAttendanceCounts() {

        attendanceCounts.clear();

        notifyDataSetChanged();
    }

    // =========================================================
    // VIEW HOLDER
    // =========================================================

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        MaterialCardView card;

        TextView tvSubject;
        TextView tvClass;
        TextView tvRoom;
        TextView tvDate;
        TextView tvStatus;
        TextView tvAttendance;

        public ViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            // -------------------------------------------------
            // MAIN CARD
            // -------------------------------------------------

            card =
                    itemView.findViewById(
                            R.id.lectureItemContainer
                    );

            // -------------------------------------------------
            // SUBJECT
            // -------------------------------------------------

            tvSubject =
                    itemView.findViewById(
                            R.id.tvLectureSubject
                    );

            // -------------------------------------------------
            // CLASS
            // -------------------------------------------------

            tvClass =
                    itemView.findViewById(
                            R.id.tvLectureClass
                    );

            // -------------------------------------------------
            // ROOM
            // -------------------------------------------------

            tvRoom =
                    itemView.findViewById(
                            R.id.tvLectureRoom
                    );

            // -------------------------------------------------
            // DATE
            // -------------------------------------------------

            tvDate =
                    itemView.findViewById(
                            R.id.tvLectureDate
                    );

            // -------------------------------------------------
            // STATUS
            // -------------------------------------------------

            tvStatus =
                    itemView.findViewById(
                            R.id.tvLectureStatus
                    );

            // -------------------------------------------------
            // ATTENDANCE
            // -------------------------------------------------

            tvAttendance =
                    itemView.findViewById(
                            R.id.tvLectureAttendance
                    );
        }
    }
}