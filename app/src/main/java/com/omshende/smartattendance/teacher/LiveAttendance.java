package com.omshende.smartattendance.teacher;

import java.util.Date;

public class LiveAttendance {

    private String studentId;
    private String studentName;
    private String rollNumber;
    private String division;
    private String enrollmentNo;
    private Date timestamp;

    public LiveAttendance() {
    }

    public LiveAttendance(
            String studentId,
            String studentName,
            String rollNumber,
            String division,
            String enrollmentNo,
            Date timestamp
    ) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.rollNumber = rollNumber;
        this.division = division;
        this.enrollmentNo = enrollmentNo;
        this.timestamp = timestamp;
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

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getEnrollmentNo() {
        return enrollmentNo;
    }

    public void setEnrollmentNo(String enrollmentNo) {
        this.enrollmentNo = enrollmentNo;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}