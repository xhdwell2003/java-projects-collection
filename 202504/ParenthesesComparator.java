import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * 括号比较器，用于比对字符串中的括号结构差异并提供相应提示
 */
public class ParenthesesComparator {

    public static void main(String[] args) {
        // 测试用例1：冗余括号
        String s1 = " ( (  filterMWMAz  and  (filterFLJta  or  (filterdLBHI  or  filterlqEqW )) ) )";
        String rs1 = " (   filterMWMAz  and  (filterFLJta  or  (filterdLBHI  or  filterlqEqW )) ) ";
        
        // 测试用例2：缺少括号
        String s2 = " (  filterMWMAz  and  filterFLJta  or  (filterdLBHI  or  filterlqEqW ))";
        String rs2 = " (   filterMWMAz  and  (filterFLJta  or  (filterdLBHI  or  filterlqEqW )) ) ";
        
        // 测试用例3：相同括号数量但位置不同
        String s3 = " ( (  filterMWMAz  and  filterFLJta  or  (filterdLBHI  or  filterlqEqW )) )";
        String rs3 = " (   filterMWMAz  and  (filterFLJta  or  (filterdLBHI  or  filterlqEqW )) ) ";
        
        System.out.println("===== 测试用例1 =====");
        compareAndPrompt(s1, rs1);
        
        System.out.println("\n===== 测试用例2 =====");
        compareAndPrompt(s2, rs2);
        
        System.out.println("\n===== 测试用例3 =====");
        compareAndPrompt(s3, rs3);
    }
    
    /**
     * 比较两个字符串的括号结构并给出相应提示
     *
     * @param original 原始字符串
     * @param result 结果字符串
     */
    public static void compareAndPrompt(String original, String result) {
        System.out.println("原始字符串: " + original);
        System.out.println("结果字符串: " + result);
        
        // 提取括号信息
        List<BracketPair> originalBrackets = extractBracketPairs(original);
        List<BracketPair> resultBrackets = extractBracketPairs(result);
        
        // 输出括号对信息（调试用）
        System.out.println("原始字符串括号对数量: " + originalBrackets.size());
        System.out.println("结果字符串括号对数量: " + resultBrackets.size());
        
        // 标准化字符串内容用于结构分析
        String normalizedOriginal = normalizeContent(original);
        String normalizedResult = normalizeContent(result);
        
        // 分析表达式核心内容（去除所有括号和空格后）是否相同
        String originalCore = normalizedOriginal.replaceAll("[\\(\\)\\s]", "").trim();
        String resultCore = normalizedResult.replaceAll("[\\(\\)\\s]", "").trim();
        
        if (!originalCore.equals(resultCore)) {
            System.out.println("警告: 两个表达式的核心内容不同，无法进行有效比较");
            return;
        }
        
        // 构建括号结构映射
        Map<String, List<BracketPair>> originalStructure = buildBracketStructureMap(original, originalBrackets);
        Map<String, List<BracketPair>> resultStructure = buildBracketStructureMap(result, resultBrackets);
        
        // 检查是否存在括号结构的差异
        boolean hasDifferentStructure = false;
        
        // 首先检查原始字符串是否有更多的括号对
        if (originalBrackets.size() > resultBrackets.size()) {
            System.out.println("提示: 存在冗余括号。原始表达式中的一些括号是不必要的，可以被移除。");
            highlightRedundantBrackets(original, result, originalBrackets, resultBrackets);
        }
        // 检查括号数量相等但位置不同的情况
        else if (originalBrackets.size() == resultBrackets.size() &&
                 hasDifferentBracketPositions(original, result, originalBrackets, resultBrackets)) {
            System.out.println("提示: 相同度量需要使用括号包起来。原始表达式中的括号位置不正确，可能导致优先级错误。");
            highlightMissingBrackets(original, result, originalBrackets, resultBrackets);
            hasDifferentStructure = true;
        }
        // 检查是否缺少括号
        else if (originalBrackets.size() < resultBrackets.size()) {
            System.out.println("提示: 相同度量需要使用括号包起来。原始表达式中缺少必要的括号，可能导致优先级错误。");
            highlightMissingBrackets(original, result, originalBrackets, resultBrackets);
            hasDifferentStructure = true;
        }
        else {
            System.out.println("括号结构相同，无需调整。");
        }
    }
    
    /**
     * 构建括号结构映射，将括号内容作为键，括号对作为值
     */
    private static Map<String, List<BracketPair>> buildBracketStructureMap(String input, List<BracketPair> brackets) {
        Map<String, List<BracketPair>> structureMap = new HashMap<>();
        
        for (BracketPair pair : brackets) {
            String content = input.substring(pair.openPos + 1, pair.closePos).trim();
            content = normalizeContent(content);
            
            if (!structureMap.containsKey(content)) {
                structureMap.put(content, new ArrayList<>());
            }
            structureMap.get(content).add(pair);
        }
        
        return structureMap;
    }
    
    /**
     * 检查是否存在冗余括号（简化判断：如果原始字符串括号数量多于结果字符串，则认为存在冗余括号）
     */
    private static boolean hasRedundantBrackets(String original, String result,
            List<BracketPair> originalBrackets, List<BracketPair> resultBrackets) {
        // 简单判断：如果原始表达式的括号对数量多于结果表达式，则认为存在冗余括号
        return originalBrackets.size() > resultBrackets.size();
    }
    
    /**
     * 统计表达式出现的次数
     */
    private static Map<String, Integer> countExpressions(String input, List<BracketPair> brackets) {
        Map<String, Integer> exprCount = new HashMap<>();
        
        for (BracketPair pair : brackets) {
            String content = input.substring(pair.openPos + 1, pair.closePos).trim();
            content = normalizeContent(content);
            
            exprCount.put(content, exprCount.getOrDefault(content, 0) + 1);
        }
        
        return exprCount;
    }
    
    /**
     * 检查是否存在括号位置不同的情况
     */
    private static boolean hasDifferentBracketPositions(String original, String result,
            List<BracketPair> originalBrackets, List<BracketPair> resultBrackets) {
        // 从两个字符串中提取所有括号内的表达式
        Set<String> originalExpressions = extractExpressions(original, originalBrackets);
        Set<String> resultExpressions = extractExpressions(result, resultBrackets);
        
        // 检查结果字符串中是否有原始字符串中不存在的表达式
        for (String expr : resultExpressions) {
            if (!originalExpressions.contains(expr)) {
                return true;
            }
        }
        
        // 检查括号的相对位置是否不同
        return areBracketPositionsDifferent(original, result, originalBrackets, resultBrackets);
    }
    
    /**
     * 提取所有括号内的表达式
     */
    private static Set<String> extractExpressions(String input, List<BracketPair> brackets) {
        Set<String> expressions = new HashSet<>();
        
        for (BracketPair pair : brackets) {
            String content = input.substring(pair.openPos + 1, pair.closePos).trim();
            expressions.add(normalizeContent(content));
        }
        
        return expressions;
    }
    
    /**
     * 检查括号的相对位置是否不同
     */
    private static boolean areBracketPositionsDifferent(String original, String result,
            List<BracketPair> originalBrackets, List<BracketPair> resultBrackets) {
        // 比较每个内容相同的括号对，检查它们在原始内容中的相对位置是否不同
        
        // 构建括号内容映射
        Map<String, List<BracketPair>> originalMap = buildBracketStructureMap(original, originalBrackets);
        Map<String, List<BracketPair>> resultMap = buildBracketStructureMap(result, resultBrackets);
        
        // 检查每一个在两边都存在的表达式
        for (String expr : originalMap.keySet()) {
            if (resultMap.containsKey(expr)) {
                // 如果数量不同，说明位置发生了变化
                if (originalMap.get(expr).size() != resultMap.get(expr).size()) {
                    return true;
                }
                
                // 即使数量相同，检查内容是否包含相同的子表达式
                String normalizedOrigExpr = normalizeContent(expr);
                for (BracketPair origPair : originalMap.get(expr)) {
                    String origContent = original.substring(origPair.openPos + 1, origPair.closePos);
                    String normalizedOrigContent = normalizeContent(origContent);
                    
                    boolean hasMatch = false;
                    for (BracketPair resPair : resultMap.get(expr)) {
                        String resContent = result.substring(resPair.openPos + 1, resPair.closePos);
                        String normalizedResContent = normalizeContent(resContent);
                        
                        if (normalizedOrigContent.equals(normalizedResContent)) {
                            hasMatch = true;
                            break;
                        }
                    }
                    
                    if (!hasMatch) {
                        return true;
                    }
                }
            }
        }
        
        // 检查原始字符串中是否缺少结果字符串中的某些表达式
        for (String expr : resultMap.keySet()) {
            if (!originalMap.containsKey(expr)) {
                return true;
            }
        }
        
        // 比较原始字符串和结果字符串中括号的位置关系
        // 检查是否有括号内容相同但位置不同的情况
        if (originalBrackets.size() == resultBrackets.size() && originalBrackets.size() > 0) {
            // 提取每个括号的内容并排序
            List<String> originalContents = new ArrayList<>();
            List<String> resultContents = new ArrayList<>();
            
            for (BracketPair pair : originalBrackets) {
                originalContents.add(normalizeContent(original.substring(pair.openPos + 1, pair.closePos)));
            }
            
            for (BracketPair pair : resultBrackets) {
                resultContents.add(normalizeContent(result.substring(pair.openPos + 1, pair.closePos)));
            }
            
            // 比较内容列表是否相同
            Collections.sort(originalContents);
            Collections.sort(resultContents);
            
            if (!originalContents.equals(resultContents)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 高亮显示冗余括号
     */
    private static void highlightRedundantBrackets(String original, String result, List<BracketPair> originalBrackets, List<BracketPair> resultBrackets) {
        StringBuilder highlighted = new StringBuilder(original);
        int offset = 0;
        
        // 直接查找不在结果表达式中的括号对
        // 使用简化的方法：假设最外层的第一对括号是冗余的
        if (originalBrackets.size() > 0) {
            // 标记最外层的括号
            BracketPair outermost = null;
            
            // 寻找最外层的括号对
            for (BracketPair pair : originalBrackets) {
                if (outermost == null ||
                   (pair.openPos < outermost.openPos && pair.closePos > outermost.closePos)) {
                    outermost = pair;
                }
            }
            
            // 检查这个括号对是否在结果中
            boolean foundInResult = false;
            for (BracketPair resultPair : resultBrackets) {
                // 尝试通过比较位置和内容来确定是否是相同的括号对
                if (Math.abs(resultPair.openPos - outermost.openPos) <= 3 &&
                    Math.abs(resultPair.closePos - outermost.closePos) <= 3) {
                    foundInResult = true;
                    break;
                }
            }
            
            if (!foundInResult) {
                // 标记为冗余括号
                highlighted.insert(outermost.openPos + offset, "***");
                offset += 3;
                highlighted.insert(outermost.closePos + offset + 1, "***");
                offset += 3;
            }
        }
        
        System.out.println("冗余括号标记: " + highlighted.toString());
    }
    
    /**
     * 高亮显示缺少的括号
     */
    private static void highlightMissingBrackets(String original, String result, List<BracketPair> originalBrackets, List<BracketPair> resultBrackets) {
        System.out.println("原始表达式应该调整为: " + result);
        
        // 创建括号内容到括号对的映射
        Map<String, List<BracketPair>> originalMap = buildBracketStructureMap(original, originalBrackets);
        Map<String, List<BracketPair>> resultMap = buildBracketStructureMap(result, resultBrackets);
        
        // 找出结果中有但原始字符串中没有的表达式或括号对数量不同的表达式
        for (String content : resultMap.keySet()) {
            if (!originalMap.containsKey(content) || originalMap.get(content).size() < resultMap.get(content).size()) {
                System.out.println("建议在以下表达式周围添加括号: " + content);
            }
        }
    }
    
    /**
     * 提取两个表达式中对应的术语对
     */
    private static List<String> extractTermPairs(String original, String result) {
        List<String> termPairs = new ArrayList<>();
        
        // 简单实现：提取所有可能的术语（非空白字符序列）
        String[] originalTerms = original.split("\\s+");
        String[] resultTerms = result.split("\\s+");
        
        // 尝试匹配术语
        int minLength = Math.min(originalTerms.length, resultTerms.length);
        for (int i = 0; i < minLength; i++) {
            if (!originalTerms[i].isEmpty() && !resultTerms[i].isEmpty() &&
                !originalTerms[i].equals("(") && !originalTerms[i].equals(")") &&
                !resultTerms[i].equals("(") && !resultTerms[i].equals(")")) {
                termPairs.add(originalTerms[i] + "|" + resultTerms[i]);
            }
        }
        
        return termPairs;
    }
    
    /**
     * 判断两个括号对是否等效（包含相似的内容）
     */
    private static boolean isEquivalentBracketPair(BracketPair pair1, BracketPair pair2, String str1, String str2) {
        String content1 = str1.substring(pair1.openPos + 1, pair1.closePos).trim();
        String content2 = str2.substring(pair2.openPos + 1, pair2.closePos).trim();
        
        // 简化比较：移除多余空格后比较内容
        content1 = normalizeContent(content1);
        content2 = normalizeContent(content2);
        
        return content1.equals(content2);
    }
    
    /**
     * 标准化内容以便比较（移除多余空格）
     */
    private static String normalizeContent(String content) {
        return content.replaceAll("\\s+", " ").trim();
    }
    
    /**
     * 从字符串中提取所有括号对
     */
    private static List<BracketPair> extractBracketPairs(String input) {
        List<BracketPair> pairs = new ArrayList<>();
        Stack<Integer> stack = new Stack<>();
        char[] chars = input.toCharArray();
        
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '(') {
                stack.push(i);
            } else if (chars[i] == ')' && !stack.isEmpty()) {
                int openPos = stack.pop();
                pairs.add(new BracketPair(openPos, i, input));
            }
        }
        
        return pairs;
    }
    
    /**
     * 表示一对括号的数据结构
     */
    static class BracketPair {
        int openPos;  // 左括号位置
        int closePos; // 右括号位置
        String original; // 原始字符串
        
        public BracketPair(int openPos, int closePos, String original) {
            this.openPos = openPos;
            this.closePos = closePos;
            this.original = original;
        }
        
        @Override
        public String toString() {
            return "(" + openPos + "," + closePos + ")";
        }
    }
}