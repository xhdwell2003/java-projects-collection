import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 工作日历映射工具
 * 将一个日期区间内的所有自然日映射到其对应的上一个工作日
 */
public class WorkingDaysMapper {
    // 日期格式化器
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * 获取日期区间内的自然日到上一工作日的映射
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return Map<LocalDate, LocalDate> 键为自然日，值为上一工作日
     * @throws SQLException SQL异常
     */
    public static Map<LocalDate, LocalDate> getNaturalDayToLastWorkingDayMap(LocalDate startDate, LocalDate endDate) throws SQLException {
        // 结果映射：自然日 -> 上一工作日
        Map<LocalDate, LocalDate> resultMap = new HashMap<>();
        
        // 1. 获取日期区间内的所有工作日
        List<LocalDate> workingDays = getWorkingDays(startDate, endDate);
        
        if (workingDays.isEmpty()) {
            return resultMap;
        }
        
        // 2. 为每个自然日找到对应的上一个工作日
        LocalDate currentDate = startDate;
        LocalDate lastWorkingDay = null;
        
        // 查找开始日期之前的最后一个工作日（如果需要）
        if (!workingDays.contains(startDate)) {
            lastWorkingDay = findLastWorkingDayBefore(startDate);
        }
        
        while (!currentDate.isAfter(endDate)) {
            if (workingDays.contains(currentDate)) {
                // 如果当前日期是工作日，则它自己就是上一个工作日
                lastWorkingDay = currentDate;
            }
            
            // 为当前自然日设置上一个工作日
            if (lastWorkingDay != null) {
                resultMap.put(currentDate, lastWorkingDay);
            }
            
            // 移动到下一天
            currentDate = currentDate.plusDays(1);
        }
        
        return resultMap;
    }
    
    /**
     * 获取日期区间内的所有工作日
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 工作日列表
     * @throws SQLException SQL异常
     */
    private static List<LocalDate> getWorkingDays(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<LocalDate> workingDays = new ArrayList<>();
        
        // 数据库连接参数（需要根据实际环境修改）
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String username = "your_username";
        String password = "your_password";
        
        // SQL查询语句
        String sql = "SELECT RLDATE FROM tgcore.pubrl WHERE RLWORK='Y' AND RLDATE BETWEEN TO_DATE(?, 'YYYY-MM-DD') AND TO_DATE(?, 'YYYY-MM-DD') ORDER BY RLDATE";
        
        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, startDate.format(DATE_FORMATTER));
            pstmt.setString(2, endDate.format(DATE_FORMATTER));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // 从数据库中获取日期并转换为LocalDate
                    Date date = rs.getDate("RLDATE");
                    LocalDate workingDay = date.toLocalDate();
                    workingDays.add(workingDay);
                }
            }
        }
        
        return workingDays;
    }
    
    /**
     * 查找指定日期之前的最后一个工作日
     * 
     * @param date 参考日期
     * @return 最后一个工作日，如果没有找到则返回null
     * @throws SQLException SQL异常
     */
    private static LocalDate findLastWorkingDayBefore(LocalDate date) throws SQLException {
        // 数据库连接参数（需要根据实际环境修改）
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String username = "your_username";
        String password = "your_password";
        
        // SQL查询语句 - 查询指定日期之前的最后一个工作日
        String sql = "SELECT MAX(RLDATE) AS LAST_WORKING_DAY FROM tgcore.pubrl WHERE RLWORK='Y' AND RLDATE < TO_DATE(?, 'YYYY-MM-DD')";
        
        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, date.format(DATE_FORMATTER));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Date lastWorkingDate = rs.getDate("LAST_WORKING_DAY");
                    if (lastWorkingDate != null) {
                        return lastWorkingDate.toLocalDate();
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * 测试示例
     */
    public static void main(String[] args) {
        try {
            // 示例：获取2023年5月1日到2023年5月15日的自然日到上一工作日的映射
            LocalDate startDate = LocalDate.of(2023, 5, 1);
            LocalDate endDate = LocalDate.of(2023, 5, 15);
            Map<LocalDate, LocalDate> result = getNaturalDayToLastWorkingDayMap(startDate, endDate);
            
            // 打印结果
            System.out.println("自然日 -> 上一工作日 映射关系：");
            result.entrySet().stream()
                  .sorted(Map.Entry.comparingByKey())
                  .forEach(entry -> System.out.println(
                      entry.getKey().format(DATE_FORMATTER) + " -> " + 
                      entry.getValue().format(DATE_FORMATTER)));
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}