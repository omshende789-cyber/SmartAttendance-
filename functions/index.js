const {onCall, HttpsError} = require("firebase-functions/v2/https");
const {initializeApp} = require("firebase-admin/app");
const {
  getFirestore,
  FieldValue,
} = require("firebase-admin/firestore");

initializeApp();

const db = getFirestore();

exports.markAttendance = onCall(async (request) => {
  // 1. Check login
  if (!request.auth) {
    throw new HttpsError(
        "unauthenticated",
        "You must be logged in.",
    );
  }

  const studentId = request.auth.uid;

  // 2. Get input
  const lectureId = request.data && request.data.lectureId;
  const enteredOtp = request.data && request.data.otp;
  if (!lectureId || !enteredOtp) {
    throw new HttpsError(
        "invalid-argument",
        "Lecture ID and OTP are required.",
    );
  }

  // 3. Validate OTP format
  const otpString = String(enteredOtp);

  if (!/^\d{6}$/.test(otpString)) {
    throw new HttpsError(
        "invalid-argument",
        "OTP must contain exactly 6 digits.",
    );
  }

  // 4. Get student
  const studentRef = db
      .collection("users")
      .doc(studentId);

  const studentSnapshot = await studentRef.get();

  if (!studentSnapshot.exists) {
    throw new HttpsError(
        "not-found",
        "Student profile not found.",
    );
  }

  const student = studentSnapshot.data();

  // 5. Check student role
  if (student.role !== "student") {
    throw new HttpsError(
        "permission-denied",
        "Only students can mark attendance.",
    );
  }

  // 6. Get student class
  const studentClassId = student.classId;

  if (!studentClassId) {
    throw new HttpsError(
        "failed-precondition",
        "Student class is not configured.",
    );
  }

  // 7. Get lecture
  const lectureRef = db
      .collection("lectures")
      .doc(lectureId);

  const lectureSnapshot = await lectureRef.get();

  if (!lectureSnapshot.exists) {
    throw new HttpsError(
        "not-found",
        "Lecture not found.",
    );
  }

  const lecture = lectureSnapshot.data();

  // 8. Check lecture status
  if (lecture.status !== "ACTIVE") {
    throw new HttpsError(
        "failed-precondition",
        "This lecture is closed.",
    );
  }

  // 9. Get target classes
  const targetClassIds = Array.isArray(
      lecture.targetClassIds,
  ) ?
    lecture.targetClassIds :
    [];

  // 10. Check student eligibility
  if (!targetClassIds.includes(studentClassId)) {
    throw new HttpsError(
        "permission-denied",
        "You are not eligible for this lecture.",
    );
  }

  // 11. Get lecture OTP
  const lectureOtp = String(
      lecture.otp || "",
  );

  if (!lectureOtp) {
    throw new HttpsError(
        "failed-precondition",
        "OTP is not available.",
    );
  }

  // 12. Check OTP
  if (lectureOtp !== otpString) {
    throw new HttpsError(
        "permission-denied",
        "Incorrect OTP.",
    );
  }

  // 13. Check OTP expiry
  if (!lecture.otpExpiry) {
    throw new HttpsError(
        "failed-precondition",
        "OTP expiry is not configured.",
    );
  }

  const expiryDate = lecture.otpExpiry.toDate();
  const currentTime = new Date();

  if (currentTime > expiryDate) {
    throw new HttpsError(
        "deadline-exceeded",
        "OTP has expired.",
    );
  }

  // 14. Unique attendance ID
  const attendanceId =
    `${lectureId}_${studentId}`;

  const attendanceRef = db
      .collection("attendance")
      .doc(attendanceId);

  // 15. Duplicate check
  const attendanceSnapshot =
    await attendanceRef.get();

  if (attendanceSnapshot.exists) {
    throw new HttpsError(
        "already-exists",
        "Attendance is already marked.",
    );
  }

  // 16. Create attendance
  await attendanceRef.set({
    lectureId: lectureId,
    studentId: studentId,
    status: "PRESENT",
    markedBy: "OTP",
    timestamp: FieldValue.serverTimestamp(),
  });

  // 17. Success
  return {
    success: true,
    message: "Attendance marked successfully.",
  };
});
