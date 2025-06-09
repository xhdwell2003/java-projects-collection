import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * METRICSYS字符串提取工具类
 */
public class MetricsExtractor {
    
    /**
     * 提取字符串中所有以METRICSYS开头的子字符串
     * @param input 输入字符串
     * @return 包含所有METRICSYS开头子字符串的List
     */
    public static List<String> extractMetricSysStrings(String input) {
        List<String> result = new ArrayList<>();
        if (input == null || input.isEmpty()) {
            return result;
        }
        
        // 正则表达式匹配METRICSYS开头后跟数字的字符串
        Pattern pattern = Pattern.compile("METRICSYS\\d+");
        Matcher matcher = pattern.matcher(input);
        
        // 查找所有匹配项并添加到结果列表
        while (matcher.find()) {
            result.add(matcher.group());
        }
        
        return result;
    }
    
    /**
     * 测试方法
     */
    public static void main(String[] args) {
        String s = "METRICSYS1199307357  out  METRICSYS1199307358";
        List<String> extracted = extractMetricSysStrings(s);
        
        System.out.println("提取的METRICSYS字符串:");
        for (String str : extracted) {
            System.out.println(str);
        }
    }
}