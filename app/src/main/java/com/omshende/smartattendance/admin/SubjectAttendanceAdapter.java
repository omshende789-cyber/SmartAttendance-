package com.omshende.smartattendance.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.omshende.smartattendance.R;
import com.omshende.smartattendance.model.SubjectAttendance;

import java.util.List;
import java.util.Locale;

public class SubjectAttendanceAdapter
        extends RecyclerView.Adapter<SubjectAttendanceAdapter.ViewHolder> {

    private final List<SubjectAttendance> subjectList;

    public SubjectAttendanceAdapter(
            List<SubjectAttendance> subjectList
    ) {
        this.subjectList = subjectList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater.from(
                parent.getContext()
        ).inflate(
                R.layout.item_subject_attendance,
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

        SubjectAttendance subject =
                subjectList.get(position);

        String subjectName =
                subject.getSubjectName();

        if (subjectName == null
                || subjectName.trim().isEmpty()) {

            subjectName = "Unknown Subject";
        }

        holder.tvSubjectName.setText(
                subjectName
        );

        holder.tvSubjectPercentage.setText(
                String.format(
                        Locale.getDefault(),
                        "%.1f%%",
                        subject.getPercentage()
                )
        );

        holder.tvSubjectStats.setText(
                subject.getPresentLectures()
                        + " Present / "
                        + subject.getTotalLectures()
                        + " Lectures"
        );

        int progress =
                (int) Math.round(
                        subject.getPercentage()
                );

        if (progress < 0) {
            progress = 0;
        }

        if (progress > 100) {
            progress = 100;
        }

        holder.progressSubjectAttendance
                .setProgress(progress);

        holder.tvSubjectAbsent.setText(
                subject.getAbsentLectures()
                        + " Absent"
        );
    }

    @Override
    public int getItemCount() {
        return subjectList.size();
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvSubjectName;
        TextView tvSubjectPercentage;
        TextView tvSubjectStats;
        TextView tvSubjectAbsent;

        ProgressBar progressSubjectAttendance;

        public ViewHolder(
                @NonNull View itemView
        ) {
            super(itemView);

            tvSubjectName =
                    itemView.findViewById(
                            R.id.tvSubjectName
                    );

            tvSubjectPercentage =
                    itemView.findViewById(
                            R.id.tvSubjectPercentage
                    );

            tvSubjectStats =
                    itemView.findViewById(
                            R.id.tvSubjectStats
                    );

            tvSubjectAbsent =
                    itemView.findViewById(
                            R.id.tvSubjectAbsent
                    );

            progressSubjectAttendance =
                    itemView.findViewById(
                            R.id.progressSubjectAttendance
                    );
        }
    }
}