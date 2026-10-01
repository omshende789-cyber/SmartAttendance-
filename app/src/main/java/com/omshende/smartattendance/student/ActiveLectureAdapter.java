package com.omshende.smartattendance.student;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.omshende.smartattendance.R;
import com.omshende.smartattendance.model.Lecture;

import java.util.List;

public class ActiveLectureAdapter
        extends RecyclerView.Adapter<ActiveLectureAdapter.LectureViewHolder> {

    // ==================================================
    // LECTURE LIST
    // ==================================================

    private final List<Lecture> lectureList;

    // ==================================================
    // CLICK LISTENER
    // ==================================================

    private final OnLectureClickListener listener;

    // ==================================================
    // INTERFACE
    // ==================================================

    public interface OnLectureClickListener {

        void onLectureClick(Lecture lecture);
    }

    // ==================================================
    // CONSTRUCTOR
    // ==================================================

    public ActiveLectureAdapter(
            List<Lecture> lectureList,
            OnLectureClickListener listener
    ) {

        this.lectureList = lectureList;
        this.listener = listener;
    }

    // ==================================================
    // CREATE VIEW HOLDER
    // ==================================================

    @NonNull
    @Override
    public LectureViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(
                        R.layout.item_active_lecture,
                        parent,
                        false
                );

        return new LectureViewHolder(view);
    }

    // ==================================================
    // BIND DATA
    // ==================================================

    @Override
    public void onBindViewHolder(
            @NonNull LectureViewHolder holder,
            int position
    ) {

        Lecture lecture =
                lectureList.get(position);

        // ==================================================
        // SUBJECT NAME
        // ==================================================

        String subjectName =
                lecture.getSubjectName();

        if (subjectName != null
                && !subjectName.trim().isEmpty()) {

            holder.tvSubjectName.setText(
                    subjectName
            );

        } else {

            holder.tvSubjectName.setText(
                    "Unknown Subject"
            );
        }

        // ==================================================
        // TEACHER NAME
        // ==================================================

        String teacherName =
                lecture.getTeacherName();

        if (teacherName != null
                && !teacherName.trim().isEmpty()) {

            holder.tvTeacherName.setText(
                    "Prof. " + teacherName
            );

        } else {

            holder.tvTeacherName.setText(
                    "Teacher: Not available"
            );
        }

        // ==================================================
        // ACTIVE STATUS
        // ==================================================

        String status =
                lecture.getStatus();

        if (status != null
                && status.equalsIgnoreCase("ACTIVE")) {

            holder.tvActiveStatus.setText(
                    "ACTIVE"
            );

        } else {

            holder.tvActiveStatus.setText(
                    "CLOSED"
            );
        }

        // ==================================================
        // CLASS INFORMATION
        // ==================================================

        String department =
                lecture.getDepartment();

        String year =
                lecture.getYear();

        String className =
                lecture.getClassName();

        StringBuilder classInfo =
                new StringBuilder();

        // --------------------------------------------------
        // Prefer className
        // Example: 2nd IT
        // --------------------------------------------------

        if (className != null
                && !className.trim().isEmpty()) {

            classInfo.append(
                    className.trim()
            );

        } else {

            // --------------------------------------------------
            // Fallback: Department + Year
            // --------------------------------------------------

            if (department != null
                    && !department.trim().isEmpty()) {

                classInfo.append(
                        department.trim()
                );
            }

            if (year != null
                    && !year.trim().isEmpty()) {

                if (classInfo.length() > 0) {
                    classInfo.append(" • ");
                }

                classInfo.append(
                        year.trim()
                );
            }
        }

        if (classInfo.length() > 0) {

            holder.tvClassInfo.setText(
                    "Class: " + classInfo
            );

        } else {

            holder.tvClassInfo.setText(
                    "Class: Not available"
            );
        }

        // ==================================================
        // ROOM INFORMATION
        // ==================================================

        String roomNo =
                lecture.getRoomNo();

        if (roomNo != null
                && !roomNo.trim().isEmpty()) {

            holder.tvRoomInfo.setText(
                    "Room: " + roomNo
            );

        } else {

            holder.tvRoomInfo.setText(
                    "Room: Not available"
            );
        }

        // ==================================================
        // ENTER OTP BUTTON
        // ==================================================

        holder.btnEnterOtp.setOnClickListener(
                v -> {

                    if (listener != null) {

                        listener.onLectureClick(
                                lecture
                        );
                    }
                }
        );

        // ==================================================
        // CARD CLICK
        // ==================================================

        holder.itemView.setOnClickListener(
                v -> {

                    if (listener != null) {

                        listener.onLectureClick(
                                lecture
                        );
                    }
                }
        );
    }

    // ==================================================
    // ITEM COUNT
    // ==================================================

    @Override
    public int getItemCount() {

        if (lectureList == null) {
            return 0;
        }

        return lectureList.size();
    }

    // ==================================================
    // VIEW HOLDER
    // ==================================================

    public static class LectureViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvSubjectName;
        TextView tvTeacherName;
        TextView tvActiveStatus;
        TextView tvClassInfo;
        TextView tvRoomInfo;

        MaterialButton btnEnterOtp;

        // ==================================================
        // CONSTRUCTOR
        // ==================================================

        public LectureViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            // --------------------------------------------------
            // EXACT IDs FROM item_active_lecture.xml
            // --------------------------------------------------

            tvSubjectName =
                    itemView.findViewById(
                            R.id.tvSubjectName
                    );

            tvTeacherName =
                    itemView.findViewById(
                            R.id.tvTeacherName
                    );

            tvActiveStatus =
                    itemView.findViewById(
                            R.id.tvActiveStatus
                    );

            tvClassInfo =
                    itemView.findViewById(
                            R.id.tvClassInfo
                    );

            tvRoomInfo =
                    itemView.findViewById(
                            R.id.tvRoomInfo
                    );

            btnEnterOtp =
                    itemView.findViewById(
                            R.id.btnEnterOtp
                    );
        }
    }
}