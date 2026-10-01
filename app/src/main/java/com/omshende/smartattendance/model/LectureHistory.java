package com.omshende.smartattendance.model;

import java.util.Date;

public class LectureHistory {

    private String lectureId;
    private String subjectName;
    private String teacherName;
    private String roomNo;
    private String className;
    private Date lectureDate;
    private String attendanceStatus;

    public LectureHistory() {
    }

    public LectureHistory(
            String lectureId,
            String subjectName,
            String teacherName,
            String roomNo,
            String className,
            Date lectureDate,
            String attendanceStatus
    ) {
        this.lectureId = lectureId;
        this.subjectName = subjectName;
        this.teacherName = teacherName;
        this.roomNo = roomNo;
        this.className = className;
        this.lectureDate = lectureDate;
        this.attendanceStatus = attendanceStatus;
    }

    public String getLectureId() {
        return lectureId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public String getClassName() {
        return className;
    }

    public Date getLectureDate() {
        return lectureDate;
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
    }
}