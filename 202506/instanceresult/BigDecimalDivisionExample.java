import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * BigDecimal除法运算示例
 * 展示了在Java中如何正确使用BigDecimal进行精确的除法运算
 */
public class BigDecimalDivisionExample {
    
    /**
     * 基本的BigDecimal除法示例
     */
    public static void basicDivisionExample() {
        System.out.println("=== 基本BigDecimal除法示例 ===");
        
        BigDecimal dividend = new BigDecimal("10.5");  // 被除数
        BigDecimal divisor = new BigDecimal("3");      // 除数
        
        // 方法1: 指定精度和舍入模式
        BigDecimal result1 = dividend.divide(divisor, 4, RoundingMode.HALF_UP);
        System.out.println("10.5 ÷ 3 = " + result1); // 输出: 3.5000
        
        // 方法2: 使用stripTrailingZeros()去除尾部零
        BigDecimal result2 = dividend.divide(divisor, 10, RoundingMode.HALF_UP).stripTrailingZeros();
        System.out.println("10.5 ÷ 3 (去除尾部零) = " + result2); // 输出: 3.5
        
        // 方法3: 使用toPlainString()避免科学计数法
        BigDecimal result3 = dividend.divide(divisor, 10, RoundingMode.HALF_UP);
        System.out.println("使用toPlainString(): " + result3.toPlainString());
    }
    
    /**
     * 处理除零情况的安全除法
     */
    public static BigDecimal safeDivide(BigDecimal dividend, BigDecimal divisor, int scale, RoundingMode roundingMode) {
        if (divisor == null || divisor.compareTo(BigDecimal.ZERO) == 0) {
            return null; // 或抛出异常，或返回特定值
        }
        return dividend.divide(divisor, scale, roundingMode);
    }
    
    /**
     * 不同舍入模式的对比
     */
    public static void roundingModeComparison() {
        System.out.println("\n=== 舍入模式对比 ===");
        
        BigDecimal dividend = new BigDecimal("10");
        BigDecimal divisor = new BigDecimal("3");
        
        // 不同的舍入模式
        RoundingMode[] modes = {
            RoundingMode.UP,           // 向上舍入
            RoundingMode.DOWN,         // 向下舍入
            RoundingMode.CEILING,      // 向正无穷舍入
            RoundingMode.FLOOR,        // 向负无穷舍入
            RoundingMode.HALF_UP,      // 四舍五入
            RoundingMode.HALF_DOWN,    // 五舍六入
            RoundingMode.HALF_EVEN     // 银行家舍入法
        };
        
        for (RoundingMode mode : modes) {
            BigDecimal result = dividend.divide(divisor, 4, mode);
            System.out.printf("%-15s: %s%n", mode.name(), result);
        }
    }
    
    /**
     * 高精度计算示例
     */
    public static void highPrecisionExample() {
        System.out.println("\n=== 高精度计算示例 ===");
        
        // 对比double和BigDecimal的精度差异
        double d1 = 1.0;
        double d2 = 3.0;
        double doubleResult = d1 / d2;
        System.out.println("double计算: 1.0 / 3.0 = " + doubleResult);
        
        BigDecimal bd1 = new BigDecimal("1.0");
        BigDecimal bd2 = new BigDecimal("3.0");
        BigDecimal bigDecimalResult = bd1.divide(bd2, 20, RoundingMode.HALF_UP);
        System.out.println("BigDecimal计算: 1.0 / 3.0 = " + bigDecimalResult);
    }
    
    /**
     * 实际应用中的除法计算（类似你的代码中的使用场景）
     */
    public static String calculateRatio(String numeratorStr, String denominatorStr) {
        try {
            if (numeratorStr == null || numeratorStr.trim().isEmpty() ||
                denominatorStr == null || denominatorStr.trim().isEmpty()) {
                return "";
            }
            
            BigDecimal numerator = new BigDecimal(numeratorStr.trim());
            BigDecimal denominator = new BigDecimal(denominatorStr.trim());
            
            // 检查分母是否为零
            if (denominator.compareTo(BigDecimal.ZERO) == 0) {
                return ""; // 或者返回"无穷大"、"除零错误"等
            }
            
            // 进行除法运算，保留10位小数，使用四舍五入
            BigDecimal ratio = numerator.divide(denominator, 10, RoundingMode.HALF_UP);
            
            // 去除尾部零并返回
            return ratio.stripTrailingZeros().toPlainString();
            
        } catch (NumberFormatException | ArithmeticException e) {
            System.err.println("计算错误: " + e.getMessage());
            return "";
        }
    }
    
    /**
     * 批量计算比率的示例
     */
    public static void batchCalculationExample() {
        System.out.println("\n=== 批量计算示例 ===");
        
        String[][] testData = {
            {"100", "3"},        // 正常计算
            {"22", "7"},         // 需要舍入
            {"0", "5"},          // 分子为零
            {"10", "0"},         // 分母为零
            {"", "5"},           // 空值
            {"abc", "5"},        // 非数字
            {"15.75", "2.5"}     // 小数计算
        };
        
        for (String[] data : testData) {
            String result = calculateRatio(data[0], data[1]);
            System.out.printf("%s ÷ %s = %s%n", 
                data[0].isEmpty() ? "空值" : data[0], 
                data[1].isEmpty() ? "空值" : data[1], 
                result.isEmpty() ? "计算失败" : result);
        }
    }
    
    public static void main(String[] args) {
        basicDivisionExample();
        roundingModeComparison();
        highPrecisionExample();
        batchCalculationExample();
    }
}
