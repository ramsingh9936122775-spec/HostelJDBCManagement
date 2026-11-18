package com.example.hostelmanagement;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Scanner;
import java.util.Properties;

public class HostelStudentController {
    private static final String URL =
            "jdbc:mysql://localhost:3306/hostel_db";
    private static final String USER = "root";
public void run() {
        try (Scanner scanner = new Scanner(System.in);
             Connection connection =
                     openConnection()) {

            boolean running = true;

            while (running) {
                System.out.println("\n=== KIET Hostel Management ===");
                System.out.println("1. Teen student records insert karo");
                System.out.println("2. Hostel ke students search karo");
                System.out.println("3. Student ki fee update karo");
                System.out.println("4. Student ka room number update karo");
                System.out.println("5. Total fee dekho aur paid record delete karo");
                System.out.println("0. Exit");

                int choice = readInt(scanner, "Option chuno: ");

                try {
                    switch (choice) {
                        case 1 -> insertStudents(connection);
                        case 2 -> searchByHostel(connection, scanner);
                        case 3 -> updateFee(connection, scanner);
                        case 4 -> updateRoom(connection, scanner);
                        case 5 -> showTotalAndDelete(connection, scanner);
                        case 0 -> {
                            running = false;
                            System.out.println("Program band ho gaya.");
                        }
                        default -> System.out.println("0 se 5 ke beech option chuno.");
                    }
                } catch (SQLException e) {
                    System.out.println("Database error: " + e.getMessage());
                }
            }

        } catch (SQLException | IllegalStateException e) {
            System.out.println("Database connection nahi hua: " + e.getMessage());
        }
    }


    private Connection openConnection() throws SQLException {
        Properties settings = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                throw new IllegalStateException(
                        "src/main/resources/db.properties banao; example file dekho.");
            }
            settings.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Database settings read nahi ho sakin.", e);
        }

        String password = settings.getProperty("db.password");
        if (password == null || password.isBlank() || password.equals("change-me")) {
            throw new IllegalStateException("db.properties mein apna MySQL password set karo.");
        }
        return DriverManager.getConnection(URL, USER, password);
    }
    // (a) PreparedStatement se teen records insert; duplicate IDs skip.
    private void insertStudents(Connection connection) throws SQLException {
        List<HostelStudent> students = List.of(
                new HostelStudent(1, "Aditi Verma", "240101001",
                        "Aryabhatta", "A-101", 65000),
                new HostelStudent(2, "Ravi Kumar", "240101002",
                        "Aryabhatta", "A-202", 58000),
                new HostelStudent(3, "Neha Singh", "240101003",
                        "Chanakya", "C-303", 48000)
        );

        String sql = """
                INSERT INTO HostelStudent
                    (student_id, student_name, roll_number,
                     hostel_name, room_number, fee)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (HostelStudent student : students) {
                statement.setInt(1, student.getStudentId());
                statement.setString(2, student.getStudentName());
                statement.setString(3, student.getRollNumber());
                statement.setString(4, student.getHostelName());
                statement.setString(5, student.getRoomNumber());
                statement.setDouble(6, student.getFee());

                try {
                    statement.executeUpdate();
                    System.out.println(student.getStudentName() + " insert ho gaya.");
                } catch (SQLIntegrityConstraintViolationException e) {
                    System.out.println("Student ID " + student.getStudentId()
                            + " pehle se hai; skip kiya.");
                }
            }
        }
    }

    // (b) Fee 50000 se zyada, hostel name ke hisaab se, fee descending.
    private void searchByHostel(Connection connection, Scanner scanner)
            throws SQLException {
        System.out.print("Hostel ka naam daalo: ");
        String hostelName = scanner.nextLine();

        String sql = """
                SELECT student_id, student_name, roll_number,
                       hostel_name, room_number, fee
                FROM HostelStudent
                WHERE hostel_name = ? AND fee > 50000
                ORDER BY fee DESC
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, hostelName);

            try (ResultSet result = statement.executeQuery()) {
                boolean found = false;
                while (result.next()) {
                    found = true;
                    printStudent(result);
                }
                if (!found) {
                    System.out.println("Matching student nahi mila.");
                }
            }
        }
    }

    // (c) Student ki hostel fee update.
    private void updateFee(Connection connection, Scanner scanner)
            throws SQLException {
        int id = readInt(scanner, "Student ID daalo: ");

        if (findStudent(connection, id) == null) {
            System.out.println("Is ID ka student record nahi mila.");
            return;
        }

        double newFee = readDouble(scanner, "Nayi hostel fee daalo: ");
        String sql = "UPDATE HostelStudent SET fee = ? WHERE student_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, newFee);
            statement.setInt(2, id);
            statement.executeUpdate();
        }

        System.out.println("Hostel fee update ho gayi.");
    }

    // (d) Student search, room number update, updated details display.
    private void updateRoom(Connection connection, Scanner scanner)
            throws SQLException {
        int id = readInt(scanner, "Student ID daalo: ");
        HostelStudent student = findStudent(connection, id);

        if (student == null) {
            System.out.println("Is ID ka student record nahi mila.");
            return;
        }

        printStudent(student);
        System.out.print("Naya room number daalo: ");
        String newRoom = scanner.nextLine();

        String sql =
                "UPDATE HostelStudent SET room_number = ? WHERE student_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newRoom);
            statement.setInt(2, id);
            statement.executeUpdate();
        }

        System.out.println("Room update ho gaya. Updated details:");
        printStudent(findStudent(connection, id));
    }

    // (e) SUM(fee), phir fee > 0 hone par hi delete.
    private void showTotalAndDelete(Connection connection, Scanner scanner)
            throws SQLException {
        String totalSql = "SELECT SUM(fee) AS total_fee FROM HostelStudent";

        try (PreparedStatement statement = connection.prepareStatement(totalSql);
             ResultSet result = statement.executeQuery()) {
            if (result.next()) {
                System.out.printf("Total hostel fee collected: ₹%.2f%n",
                        result.getDouble("total_fee"));
            }
        }

        int id = readInt(scanner, "Delete ke liye Student ID daalo: ");
        String deleteSql =
                "DELETE FROM HostelStudent WHERE student_id = ? AND fee > 0";

        try (PreparedStatement statement = connection.prepareStatement(deleteSql)) {
            statement.setInt(1, id);

            if (statement.executeUpdate() > 0) {
                System.out.println("Fee-paid student record delete ho gaya.");
            } else {
                System.out.println(
                        "Delete nahi hua: record nahi mila ya fee zero hai.");
            }
        }
    }

    private HostelStudent findStudent(Connection connection, int id)
            throws SQLException {
        String sql = """
                SELECT student_id, student_name, roll_number,
                       hostel_name, room_number, fee
                FROM HostelStudent
                WHERE student_id = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return new HostelStudent(
                            result.getInt("student_id"),
                            result.getString("student_name"),
                            result.getString("roll_number"),
                            result.getString("hostel_name"),
                            result.getString("room_number"),
                            result.getDouble("fee"));
                }
            }
        }
        return null;
    }

    private int readInt(Scanner scanner, String message) {
        while (true) {
            try {
                System.out.print(message);
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Integer number daalo, jaise 1.");
            }
        }
    }

    private double readDouble(Scanner scanner, String message) {
        while (true) {
            try {
                System.out.print(message);
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Sahi fee amount daalo, jaise 60000.");
            }
        }
    }

    private void printStudent(ResultSet result) throws SQLException {
        printStudent(new HostelStudent(
                result.getInt("student_id"),
                result.getString("student_name"),
                result.getString("roll_number"),
                result.getString("hostel_name"),
                result.getString("room_number"),
                result.getDouble("fee")));
    }

    private void printStudent(HostelStudent student) {
        System.out.printf(
                "ID: %d | Name: %s | Roll: %s | Hostel: %s | Room: %s | Fee: ₹%.2f%n",
                student.getStudentId(),
                student.getStudentName(),
                student.getRollNumber(),
                student.getHostelName(),
                student.getRoomNumber(),
                student.getFee());
    }
}