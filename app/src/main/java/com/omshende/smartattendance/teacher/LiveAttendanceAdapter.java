package com.omshende.smartattendance.teacher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.omshende.smartattendance.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class LiveAttendanceAdapter
        extends RecyclerView.Adapter<LiveAttendanceAdapter.AttendanceViewHolder> {

    private final List<LiveAttendance> attendanceList;

    public LiveAttendanceAdapter(
            List<LiveAttendance> attendanceList
    ) {
        this.attendanceList = attendanceList;
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(
                                R.layout.item_live_attendance,
                                parent,
                                false
                        );

        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull AttendanceViewHolder holder,
            int position
    ) {

        LiveAttendance attendance =
                attendanceList.get(position);

        holder.tvStudentName.setText(
                safe(attendance.getStudentName(), "Unknown Student")
        );

        String roll =
                safe(attendance.getRollNumber(), "");

        String division =
                safe(attendance.getDivision(), "");

        if (!roll.isEmpty() && !division.isEmpty()) {

            holder.tvStudentRoll.setText(
                    division + "-" + formatRoll(roll)
            );

        } else if (!roll.isEmpty()) {

            holder.tvStudentRoll.setText(
                    "Roll No. " + roll
            );

        } else {

            holder.tvStudentRoll.setText(
                    "Roll number not available"
            );
        }

        String enrollment =
                safe(attendance.getEnrollmentNo(), "");

        if (!enrollment.isEmpty()) {

            holder.tvEnrollment.setText(
                    "Enrollment: " + enrollment
            );

        } else {

            holder.tvEnrollment.setText(
                    "Enrollment not available"
            );
        }

        holder.tvAttendanceStatus.setText(
                "PRESENT"
        );

        if (attendance.getTimestamp() != null) {

            SimpleDateFormat format =
                    new SimpleDateFormat(
                            "hh:mm:ss a",
                            Locale.getDefault()
                    );

            holder.tvAttendanceTime.setText(
                    "Marked at " +
                            format.format(
                                    attendance.getTimestamp()
                            )
            );

        } else {

            holder.tvAttendanceTime.setText(
                    "Marked just now"
            );
        }
    }

    private String formatRoll(String value) {

        try {

            int number =
                    Integer.parseInt(value);

            return String.format(
                    Locale.getDefault(),
                    "%02d",
                    number
            );

        } catch (Exception e) {

            return value;
        }
    }

    private String safe(
            String value,
            String fallback
    ) {

        if (value == null ||
                value.trim().isEmpty()) {

            return fallback;
        }

        return value.trim();
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    static class AttendanceViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvStudentName;
        TextView tvStudentRoll;
        TextView tvEnrollment;
        TextView tvAttendanceStatus;
        TextView tvAttendanceTime;

        public AttendanceViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            tvStudentName =
                    itemView.findViewById(
                            R.id.tvLiveStudentName
                    );

            tvStudentRoll =
                    itemView.findViewById(
                            R.id.tvLiveStudentRoll
                    );

            tvEnrollment =
                    itemView.findViewById(
                            R.id.tvLiveStudentEnrollment
                    );

            tvAttendanceStatus =
                    itemView.findViewById(
                            R.id.tvLiveAttendanceStatus
                    );

            tvAttendanceTime =
                    itemView.findViewById(
                            R.id.tvLiveAttendanceTime
                    );
        }
    }
}