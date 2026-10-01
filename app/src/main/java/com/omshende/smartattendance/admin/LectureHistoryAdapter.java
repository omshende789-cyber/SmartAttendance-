package com.omshende.smartattendance.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.omshende.smartattendance.R;
import com.omshende.smartattendance.model.LectureHistory;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class LectureHistoryAdapter
        extends RecyclerView.Adapter<LectureHistoryAdapter.ViewHolder> {

    private final List<LectureHistory> historyList;

    public LectureHistoryAdapter(
            List<LectureHistory> historyList
    ) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater.from(
                        parent.getContext()
                ).inflate(
                        R.layout.item_lecture_history,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {

        LectureHistory history =
                historyList.get(position);

        holder.tvSubjectName.setText(
                history.getSubjectName()
        );

        holder.tvTeacherName.setText(
                "Teacher: "
                        + history.getTeacherName()
        );

        holder.tvRoomNo.setText(
                "Room: "
                        + history.getRoomNo()
        );

        holder.tvClassName.setText(
                "Class: "
                        + history.getClassName()
        );

        if (history.getLectureDate() != null) {

            SimpleDateFormat dateFormat =
                    new SimpleDateFormat(
                            "dd MMM yyyy, hh:mm a",
                            Locale.getDefault()
                    );

            holder.tvLectureDate.setText(
                    dateFormat.format(
                            history.getLectureDate()
                    )
            );

        } else {

            holder.tvLectureDate.setText(
                    "Date not available"
            );
        }

        if (
                "PRESENT".equalsIgnoreCase(
                        history.getAttendanceStatus()
                )
        ) {

            holder.tvAttendanceStatus.setText(
                    "✓ PRESENT"
            );

            holder.tvAttendanceStatus.setVisibility(
                    View.VISIBLE
            );

        } else {

            holder.tvAttendanceStatus.setText(
                    "✕ ABSENT"
            );

            holder.tvAttendanceStatus.setVisibility(
                    View.VISIBLE
            );
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    // =========================================================
    // VIEW HOLDER
    // =========================================================

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvSubjectName;
        TextView tvTeacherName;
        TextView tvRoomNo;
        TextView tvClassName;
        TextView tvLectureDate;
        TextView tvAttendanceStatus;

        public ViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            tvSubjectName =
                    itemView.findViewById(
                            R.id.tvSubjectName
                    );

            tvTeacherName =
                    itemView.findViewById(
                            R.id.tvTeacherName
                    );

            tvRoomNo =
                    itemView.findViewById(
                            R.id.tvRoomNo
                    );

            tvClassName =
                    itemView.findViewById(
                            R.id.tvClassName
                    );

            tvLectureDate =
                    itemView.findViewById(
                            R.id.tvLectureDate
                    );

            tvAttendanceStatus =
                    itemView.findViewById(
                            R.id.tvAttendanceStatus
                    );
        }
    }
}