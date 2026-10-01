package com.omshende.smartattendance.teacher;

public class TeacherAttendance {

    private String studentId;
    private String studentName;
    private String enrollmentNo;
    private String division;
    private int rollNumber;
    private String status;

    public TeacherAttendance() {
    }

    public TeacherAttendance(
            String studentId,
            String studentName,
            String enrollmentNo,
            String division,
            int rollNumber,
            String status
    ) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.enrollmentNo = enrollmentNo;
        this.division = division;
        this.rollNumber = rollNumber;
        this.status = status;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getEnrollmentNo() {
        return enrollmentNo;
    }

    public void setEnrollmentNo(String enrollmentNo) {
        this.enrollmentNo = enrollmentNo;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public int getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(int rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}