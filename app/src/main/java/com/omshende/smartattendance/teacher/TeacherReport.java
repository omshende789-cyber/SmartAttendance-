package com.omshende.smartattendance.teacher;

public class TeacherReport {

    // =========================================================
    // REPORT INFORMATION
    // =========================================================

    private String lectureId;
    private String subjectName;
    private String className;
    private String roomNo;
    private String date;

    // =========================================================
    // ATTENDANCE DATA
    // =========================================================

    private int totalStudents;
    private int presentStudents;
    private int absentStudents;

    private double attendancePercentage;

    // =========================================================
    // EMPTY CONSTRUCTOR
    // =========================================================

    public TeacherReport() {
        // Required empty constructor
    }

    // =========================================================
    // FULL CONSTRUCTOR
    // =========================================================

    public TeacherReport(
            String lectureId,
            String subjectName,
            String className,
            String roomNo,
            String date,
            int totalStudents,
            int presentStudents,
            int absentStudents,
            double attendancePercentage
    ) {

        this.lectureId = lectureId;
        this.subjectName = subjectName;
        this.className = className;
        this.roomNo = roomNo;
        this.date = date;

        this.totalStudents = totalStudents;
        this.presentStudents = presentStudents;
        this.absentStudents = absentStudents;

        this.attendancePercentage =
                attendancePercentage;
    }

    // =========================================================
    // LECTURE ID
    // =========================================================

    public String getLectureId() {
        return lectureId;
    }

    public void setLectureId(String lectureId) {
        this.lectureId = lectureId;
    }

    // =========================================================
    // SUBJECT NAME
    // =========================================================

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    // =========================================================
    // CLASS NAME
    // =========================================================

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    // =========================================================
    // ROOM NUMBER
    // =========================================================

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    // =========================================================
    // DATE
    // =========================================================

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    // =========================================================
    // TOTAL STUDENTS
    // =========================================================

    public int getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(int totalStudents) {
        this.totalStudents = totalStudents;
    }

    // =========================================================
    // PRESENT STUDENTS
    // =========================================================

    public int getPresentStudents() {
        return presentStudents;
    }

    public void setPresentStudents(int presentStudents) {
        this.presentStudents = presentStudents;
    }

    // =========================================================
    // ABSENT STUDENTS
    // =========================================================

    public int getAbsentStudents() {
        return absentStudents;
    }

    public void setAbsentStudents(int absentStudents) {
        this.absentStudents = absentStudents;
    }

    // =========================================================
    // ATTENDANCE PERCENTAGE
    // =========================================================

    public double getAttendancePercentage() {
        return attendancePercentage;
    }

    public void setAttendancePercentage(
            double attendancePercentage
    ) {

        this.attendancePercentage =
                attendancePercentage;
    }
}