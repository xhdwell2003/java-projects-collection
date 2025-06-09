import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringReplacer {
    
    /**
     * 替换字符串中的 #{CPCODE} 为格式化后的代码列表，并将前面的 = 改为 in
     * @param inputString 输入的原始字符串
     * @param cpcodes 代码列表
     * @return 返回一个包含替换后字符串和 in 前面数据项的数组，索引0为替换后字符串，索引1为提取的数据项
     */
    public static String[] replaceAndExtract(String inputString, List<String> cpcodes) {
        // 格式化 cpcodes 列表为 ('code1','code2','code3') 的形式
        StringBuilder formattedCodes = new StringBuilder("(");
        for (int i = 0; i < cpcodes.size(); i++) {
            formattedCodes.append("'").append(cpcodes.get(i)).append("'");
            if (i < cpcodes.size() - 1) {
                formattedCodes.append(",");
            }
        }
        formattedCodes.append(")");
        
        // 创建正则表达式来匹配 xxx = #{CPCODE} 模式
        Pattern pattern = Pattern.compile("(\\w+)\\s*=\\s*#\\{CPCODE\\}");
        Matcher matcher = pattern.matcher(inputString);
        
        String dataItem = "";
        if (matcher.find()) {
            // 提取 = 前面的数据项
            dataItem = matcher.group(1);
        }
        
        // 替换 = #{CPCODE} 为 in (formattedCodes)
        String replacedString = inputString.replaceAll("=\\s*#\\{CPCODE\\}", " in " + formattedCodes);
        
        return new String[] {replacedString, dataItem};
    }
    
    public static void main(String[] args) {
        // 示例
        List<String> cpcodes = List.of("17882", "1231231", "123123");
        
        String s1 = "#{ZLDATE} between SDSTRD and SDENDD  and SDSTAT='A'  and SDCPDM = #{CPCODE}";
        String s2 = "PRDCODE =#{CPCODE} AND #{ZLDATE} BETWEEN BUSDATE AND ENDDATE AND STATUS = 'A'";
        String s3 = "PRDCODE= #{CPCODE} AND BIZDATE = #{ZLDATE}";
        
        String[] result1 = replaceAndExtract(s1, cpcodes);
        String[] result2 = replaceAndExtract(s2, cpcodes);
        String[] result3 = replaceAndExtract(s3, cpcodes);
        
        System.out.println("原始字符串: " + s1);
        System.out.println("替换后: " + result1[0]);
        System.out.println("数据项: " + result1[1]);
        System.out.println();
        
        System.out.println("原始字符串: " + s2);
        System.out.println("替换后: " + result2[0]);
        System.out.println("数据项: " + result2[1]);
        System.out.println();
        
        System.out.println("原始字符串: " + s3);
        System.out.println("替换后: " + result3[0]);
        System.out.println("数据项: " + result3[1]);
    }
}