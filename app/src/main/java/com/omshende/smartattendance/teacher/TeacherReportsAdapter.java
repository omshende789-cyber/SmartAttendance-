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

public class TeacherReportsAdapter
        extends RecyclerView.Adapter<TeacherReportsAdapter.ViewHolder> {

    private final List<TeacherReport> reportList =
            new ArrayList<>();

    public TeacherReportsAdapter(
            List<TeacherReport> initialList
    ) {

        if (initialList != null) {

            reportList.addAll(
                    initialList
            );
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(
                                R.layout.item_teacher_report,
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

        TeacherReport report =
                reportList.get(position);

        holder.tvSubject.setText(
                safe(report.getSubjectName())
        );

        holder.tvClass.setText(
                safe(report.getClassName())
        );

        holder.tvRoom.setText(
                "Room: "
                        + safe(report.getRoomNo())
        );

        holder.tvDate.setText(
                safe(report.getDate())
        );

        holder.tvPresent.setText(
                "Present: "
                        + report.getPresentStudents()
        );

        holder.tvAbsent.setText(
                "Absent: "
                        + report.getAbsentStudents()
        );

        holder.tvTotal.setText(
                "Total: "
                        + report.getTotalStudents()
        );

        holder.tvPercentage.setText(
                String.format(
                        Locale.getDefault(),
                        "%.1f%%",
                        report.getAttendancePercentage()
                )
        );
    }

    @Override
    public int getItemCount() {

        return reportList.size();
    }

    public void setData(
            List<TeacherReport> newList
    ) {

        reportList.clear();

        if (newList != null) {

            reportList.addAll(
                    newList
            );
        }

        notifyDataSetChanged();
    }

    private String safe(
            String value
    ) {

        if (
                value == null
                        || value.trim().isEmpty()
        ) {

            return "-";
        }

        return value.trim();
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvSubject;
        TextView tvClass;
        TextView tvRoom;
        TextView tvDate;
        TextView tvPresent;
        TextView tvAbsent;
        TextView tvTotal;
        TextView tvPercentage;

        public ViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            tvSubject =
                    itemView.findViewById(
                            R.id.tvReportSubject
                    );

            tvClass =
                    itemView.findViewById(
                            R.id.tvReportClass
                    );

            tvRoom =
                    itemView.findViewById(
                            R.id.tvReportRoom
                    );

            tvDate =
                    itemView.findViewById(
                            R.id.tvReportDate
                    );

            tvPresent =
                    itemView.findViewById(
                            R.id.tvReportPresent
                    );

            tvAbsent =
                    itemView.findViewById(
                            R.id.tvReportAbsent
                    );

            tvTotal =
                    itemView.findViewById(
                            R.id.tvReportTotal
                    );

            tvPercentage =
                    itemView.findViewById(
                            R.id.tvReportPercentage
                    );
        }
    }
}