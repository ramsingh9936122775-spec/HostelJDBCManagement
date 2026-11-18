# Hostel JDBC Management

Menu-driven Java JDBC and MySQL project for the KIET Hostel Management System exercise.

## Requirements

- JDK 25
- MySQL Server and MySQL Workbench
- Maven (the IntelliJ project includes the Maven wrapper)

## Database setup

Run this in MySQL Workbench:

```sql
CREATE DATABASE IF NOT EXISTS hostel_db;
USE hostel_db;
CREATE TABLE IF NOT EXISTS HostelStudent (
    student_id INT PRIMARY KEY,
    student_name VARCHAR(100),
    roll_number VARCHAR(20),
    hostel_name VARCHAR(50),
    room_number VARCHAR(20),
    fee DOUBLE
);
```

Copy `src/main/resources/db.properties.example` to `src/main/resources/db.properties` and set `db.password` to your local MySQL password. The real password file is ignored by Git and is not part of this repository.

## Run

Open the Maven project in IntelliJ IDEA and run `com.example.hostelmanagement.Main`.

Menu options insert three sample students, search by hostel and fee, update fee, update room, total the fees, and conditionally delete a student.