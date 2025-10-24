package opps;

import java.sql.*;

public class DatabasePrinter {

    public static void printAllTables() {
        String[] tables = {"Room", "Customer", "Booking", "Payment"};

        try (Connection conn = DatabaseConnection.getConnection()) {
            for (String table : tables) {
                System.out.println("\n========== TABLE: " + table + " ==========");

                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM " + table);

                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();

                // Print column headers
                for (int i = 1; i <= colCount; i++) {
                    System.out.print(meta.getColumnName(i) + "\t|\t");
                }
                System.out.println("\n" + "-".repeat(50));

                // Print each row
                while (rs.next()) {
                    for (int i = 1; i <= colCount; i++) {
                        System.out.print(rs.getString(i) + "\t|\t");
                    }
                    System.out.println();
                }

                System.out.println("=====================================\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error printing tables: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        printAllTables();
    }
}
