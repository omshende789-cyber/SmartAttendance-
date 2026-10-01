package com.omshende.smartattendance.model;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class Lecture {

    // ==================================================
    // BASIC LECTURE INFORMATION
    // ==================================================

    private String lectureId;

    private String subjectId;
    private String subjectName;

    private String teacherId;
    private String teacherName;

    // ==================================================
    // TEACHER / DEPARTMENT INFORMATION
    // ==================================================

    private String departmentId;
    private String department;

    // ==================================================
    // LECTURE YEAR INFORMATION
    // ==================================================

    /*
     * This field is kept for compatibility and display.
     *
     * For a lecture targeting multiple years,
     * this can contain:
     *
     * "Multiple Years"
     *
     * For a single year:
     *
     * "2nd Year"
     */
    private String year;

    // ==================================================
    // TARGET CLASS INFORMATION
    // ==================================================

    /*
     * IMPORTANT:
     *
     * targetClassIds is now the MAIN lecture targeting field.
     *
     * Examples:
     *
     * ["it_2"]
     *
     * OR
     *
     * ["it_1", "it_2", "it_3", "it_4"]
     *
     * Student's classId is checked against this list.
     */
    private List<String> targetClassIds;

    /*
     * Human-readable class names.
     *
     * Examples:
     *
     * ["2nd IT"]
     *
     * OR
     *
     * ["1st IT", "2nd IT", "3rd IT", "4th IT"]
     *
     * This is useful for displaying selected classes
     * in Teacher and Student screens.
     */
    private List<String> targetClassNames;

    /*
     * className is kept for backward compatibility
     * and simple display.
     *
     * For a single target:
     *
     * "2nd IT"
     *
     * For multiple targets:
     *
     * "1st IT, 2nd IT, 3rd IT, 4th IT"
     */
    private String className;

    /*
     * OLD single classId field.
     *
     * We keep it temporarily for compatibility with
     * old Firestore documents and existing code.
     *
     * NEW CODE should use targetClassIds.
     */
    private String classId;

    // ==================================================
    // ROOM INFORMATION
    // ==================================================

    private String roomNo;

    // ==================================================
    // OTP INFORMATION
    // ==================================================

    private String otp;

    /*
     * OTP expiry is NOT lecture timing.
     *
     * It only determines how long the current OTP
     * remains valid.
     */
    private Timestamp otpExpiry;

    // ==================================================
    // LECTURE STATUS
    // ==================================================

    /*
     * ACTIVE
     *     Students can see the lecture and
     *     attempt attendance.
     *
     * CLOSED
     *     Students can no longer mark attendance.
     */
    private String status;

    // ==================================================
    // CONSTRUCTOR
    // ==================================================

    public Lecture() {
        /*
         * Required empty constructor for Firestore.
         */
    }

    // ==================================================
    // LECTURE ID
    // ==================================================

    public String getLectureId() {
        return lectureId;
    }

    public void setLectureId(String lectureId) {
        this.lectureId = lectureId;
    }

    // ==================================================
    // SUBJECT ID
    // ==================================================

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    // ==================================================
    // SUBJECT NAME
    // ==================================================

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    // ==================================================
    // TEACHER ID
    // ==================================================

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    // ==================================================
    // TEACHER NAME
    // ==================================================

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    // ==================================================
    // DEPARTMENT ID
    // ==================================================

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    // ==================================================
    // DEPARTMENT
    // ==================================================

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    // ==================================================
    // YEAR
    // ==================================================

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    // ==================================================
    // TARGET CLASS IDS
    // ==================================================

    public List<String> getTargetClassIds() {

        if (targetClassIds == null) {
            targetClassIds = new ArrayList<>();
        }

        return targetClassIds;
    }

    public void setTargetClassIds(
            List<String> targetClassIds
    ) {

        this.targetClassIds = targetClassIds;
    }

    // ==================================================
    // TARGET CLASS NAMES
    // ==================================================

    public List<String> getTargetClassNames() {

        if (targetClassNames == null) {
            targetClassNames = new ArrayList<>();
        }

        return targetClassNames;
    }

    public void setTargetClassNames(
            List<String> targetClassNames
    ) {

        this.targetClassNames = targetClassNames;
    }

    // ==================================================
    // OLD CLASS ID
    // ==================================================

    /*
     * Kept only for compatibility with old data.
     *
     * New lecture creation will use targetClassIds.
     */
    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    // ==================================================
    // CLASS NAME
    // ==================================================

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    // ==================================================
    // ROOM NUMBER
    // ==================================================

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    // ==================================================
    // OTP
    // ==================================================

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    // ==================================================
    // OTP EXPIRY
    // ==================================================

    public Timestamp getOtpExpiry() {
        return otpExpiry;
    }

    public void setOtpExpiry(Timestamp otpExpiry) {
        this.otpExpiry = otpExpiry;
    }

    // ==================================================
    // STATUS
    // ==================================================

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}