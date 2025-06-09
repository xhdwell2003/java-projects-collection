import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for working with work calendar data from the tgcore.pubrl table.
 * Provides functionality to get previous working days for natural days within a date range.
 */
public class WorkCalendarService {
    
    // Database connection properties - replace with your actual DB settings
    private static final String DB_URL = "jdbc:mysql://localhost:3306/tgcore";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password";
    
    // Date format used in the database (adjust if different)
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    /**
     * Gets a map of natural days to their previous working days within a specified date range.
     * 
     * @param startDate Start date of the range (inclusive) in yyyyMMdd format
     * @param endDate End date of the range (inclusive) in yyyyMMdd format
     * @return Map where key is each natural day and value is its previous working day
     * @throws SQLException If a database error occurs
     */
    public Map<LocalDate, LocalDate> getNaturalDayToPreviousWorkDayMap(String startDate, String endDate) throws SQLException {
        // Parse input dates
        LocalDate start = LocalDate.parse(startDate, DATE_FORMATTER);
        LocalDate end = LocalDate.parse(endDate, DATE_FORMATTER);
        
        // Get all working days in the range
        List<LocalDate> workDays = getWorkDays(startDate, endDate);
        
        // Create the result map
        Map<LocalDate, LocalDate> naturalDayToPrevWorkDay = new HashMap<>();
        
        // For each natural day in the range, find its previous working day
        LocalDate currentDay = start;
        LocalDate prevWorkDay = findPreviousWorkDay(currentDay, workDays);
        
        while (!currentDay.isAfter(end)) {
            // If current day is a working day, update the previous working day
            if (workDays.contains(currentDay)) {
                prevWorkDay = currentDay;
            }
            
            // Map the current day to its previous working day
            naturalDayToPrevWorkDay.put(currentDay, prevWorkDay);
            
            // Move to the next day
            currentDay = currentDay.plusDays(1);
        }
        
        return naturalDayToPrevWorkDay;
    }
    
    /**
     * Retrieves all working days from the database within the specified date range.
     * 
     * @param startDate Start date in yyyyMMdd format
     * @param endDate End date in yyyyMMdd format
     * @return List of working days
     * @throws SQLException If a database error occurs
     */
    private List<LocalDate> getWorkDays(String startDate, String endDate) throws SQLException {
        List<LocalDate> workDays = new ArrayList<>();
        
        try (Connection conn = getConnection()) {
            String sql = "SELECT RLDATE FROM tgcore.pubrl WHERE rlwork='Y' AND RLDATE BETWEEN ? AND ? ORDER BY RLDATE";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, startDate);
                stmt.setString(2, endDate);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String dateStr = rs.getString("RLDATE");
                        LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
                        workDays.add(date);
                    }
                }
            }
        }
        
        return workDays;
    }
    
    /**
     * Finds the previous working day before or on the specified date.
     * 
     * @param date The date to find the previous working day for
     * @param workDays List of all working days
     * @return The previous working day, or null if none found
     */
    private LocalDate findPreviousWorkDay(LocalDate date, List<LocalDate> workDays) {
        // Find the most recent working day that is before or equal to the given date
        return workDays.stream()
                .filter(workDay -> !workDay.isAfter(date))
                .max(LocalDate::compareTo)
                .orElse(null);
    }
    
    /**
     * Creates a database connection.
     * 
     * @return Database connection
     * @throws SQLException If connection fails
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
    
    /**
     * Main method for testing the functionality.
     */
    public static void main(String[] args) {
        try {
            WorkCalendarService service = new WorkCalendarService();
            
            // Example usage: Get mapping for a date range
            String startDate = "20250301"; // March 1, 2025
            String endDate = "20250331";   // March 31, 2025
            
            Map<LocalDate, LocalDate> result = service.getNaturalDayToPreviousWorkDayMap(startDate, endDate);
            
            // Print the results
            System.out.println("Natural Day -> Previous Work Day Mapping:");
            result.forEach((naturalDay, prevWorkDay) -> {
                System.out.println(naturalDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + 
                                  " -> " + 
                                  (prevWorkDay != null ? prevWorkDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "None"));
            });
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
