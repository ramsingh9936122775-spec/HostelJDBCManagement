package com.example.hostelmanagement;

public class HostelStudent {
    private int studentId;
    private String studentName;
    private String rollNumber;
    private String hostelName;
    private String roomNumber;
    private double fee;

    public HostelStudent() {
    }

    public HostelStudent(int studentId, String studentName, String rollNumber,
                         String hostelName, String roomNumber, double fee) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.rollNumber = rollNumber;
        this.hostelName = hostelName;
        this.roomNumber = roomNumber;
        this.fee = fee;
    }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    public String getHostelName() { return hostelName; }
    public void setHostelName(String hostelName) { this.hostelName = hostelName; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public double getFee() { return fee; }
    public void setFee(double fee) { this.fee = fee; }
}
