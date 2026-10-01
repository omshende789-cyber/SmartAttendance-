package com.omshende.smartattendance.model;

public class SubjectAttendance {

    private String subjectName;
    private int totalLectures;
    private int presentLectures;
    private int absentLectures;
    private double percentage;

    public SubjectAttendance() {
    }

    public SubjectAttendance(
            String subjectName,
            int totalLectures,
            int presentLectures,
            int absentLectures,
            double percentage
    ) {
        this.subjectName = subjectName;
        this.totalLectures = totalLectures;
        this.presentLectures = presentLectures;
        this.absentLectures = absentLectures;
        this.percentage = percentage;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public int getTotalLectures() {
        return totalLectures;
    }

    public void setTotalLectures(int totalLectures) {
        this.totalLectures = totalLectures;
    }

    public int getPresentLectures() {
        return presentLectures;
    }

    public void setPresentLectures(int presentLectures) {
        this.presentLectures = presentLectures;
    }

    public int getAbsentLectures() {
        return absentLectures;
    }

    public void setAbsentLectures(int absentLectures) {
        this.absentLectures = absentLectures;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}