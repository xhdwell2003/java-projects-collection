public class RemoveRedundantParentheses {
    public static void main(String[] args) {
        // 原始测试用例
        String s1 = "( ( filterMWMAz and ( filterFLJta or ( filterdLBHI and filterlqEqW ) ) ) )";
        String s2 = "( ( filterMWMAz and filterFLJta or filterdLBHI and filterlqEqW ) )";
        
        // 新增测试用例
        String s3 = "(((singleTerm)))";  // 多层嵌套冗余括号
        String s4 = "((a and b)) or (c)"; // 混合情况
        String s5 = "(a and (b or c))";   // 合法的有意义括号
        
        System.out.println("===== 测试原始用例 =====");
        testCase(s1, "( filterMWMAz and (filterFLJta or ( filterdLBHI and filterlqEqW ) ) ) ");
        testCase(s2, "( filterMWMAz and filterFLJta or filterdLBHI and filterlqEqW ) ");
        
        System.out.println("\n===== 测试新增用例 =====");
        testCase(s3, "singleTerm ");
        testCase(s4, "a and b or (c) ");
        testCase(s5, "(a and (b or c)) ");
    }
    
    private static void testCase(String input, String expected) {
        System.out.println("原始字符串: " + input);
        String result = removeRedundantParentheses(input);
        System.out.println("处理后: " + result);
        System.out.println("预期结果: " + expected);
        System.out.println("测试通过: " + result.equals(expected));
        System.out.println("----------------------------");
    }
    
    public static String removeRedundantParentheses(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        int n = input.length();
        char[] chars = input.toCharArray();
        boolean[] toRemove = new boolean[n]; // 标记需要移除的字符
        
        // 使用栈来跟踪括号
        java.util.Stack<Integer> stack = new java.util.Stack<>();
        
        // 第一步：找出所有配对的括号
        java.util.List<int[]> bracketPairs = new java.util.ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (chars[i] == '(') {
                stack.push(i);
            } else if (chars[i] == ')' && !stack.isEmpty()) {
                int openPos = stack.pop();
                bracketPairs.add(new int[]{openPos, i});
            }
        }
        
        // 重置栈
        stack.clear();
        
        // 第二步：标记冗余的嵌套括号
        for (int i = 0; i < bracketPairs.size(); i++) {
            int[] outer = bracketPairs.get(i);
            
            for (int j = 0; j < bracketPairs.size(); j++) {
                if (i == j) continue; // 跳过自身比较
                
                int[] inner = bracketPairs.get(j);
                
                // 检查是否为嵌套的冗余括号
                // 条件：inner完全被outer包含，且之间只有空白字符
                if (outer[0] < inner[0] && inner[1] < outer[1]) {
                    boolean onlyHasInnerAndWhitespace = true;
                    
                    // 检查左括号之间
                    for (int k = outer[0] + 1; k < inner[0]; k++) {
                        if (!Character.isWhitespace(chars[k])) {
                            onlyHasInnerAndWhitespace = false;
                            break;
                        }
                    }
                    
                    // 检查右括号之间
                    if (onlyHasInnerAndWhitespace) {
                        for (int k = inner[1] + 1; k < outer[1]; k++) {
                            if (!Character.isWhitespace(chars[k])) {
                                onlyHasInnerAndWhitespace = false;
                                break;
                            }
                        }
                    }
                    
                    // 如果outer只包含inner和空白，则outer是冗余的
                    if (onlyHasInnerAndWhitespace) {
                        toRemove[outer[0]] = true;
                        toRemove[outer[1]] = true;
                    }
                }
            }
        }
        
        // 重置栈以便第二次检查
        stack.clear();
        
        // 第三步：检查表达式中的无用括号（包含单一元素的括号）
        for (int i = 0; i < n; i++) {
            if (chars[i] == '(' && !toRemove[i]) {
                stack.push(i);
            } else if (chars[i] == ')' && !toRemove[i] && !stack.isEmpty()) {
                int openPos = stack.pop();
                
                // 检查这对括号之间是否只有单一表达式且没有逻辑运算符
                boolean hasSingleTerm = true;
                boolean hasOperator = false;
                
                for (int j = openPos + 1; j < i; j++) {
                    if (!toRemove[j]) { // 只检查未标记为移除的字符
                        if (chars[j] == '(' || chars[j] == ')') {
                            hasSingleTerm = false;
                            break;
                        }
                        
                        // 检查是否有逻辑运算符
                        if (j < i - 2) { // 确保至少有3个字符可以检查
                            String subStr = input.substring(j, Math.min(j + 5, i)).toLowerCase();
                            // 检查常见的逻辑运算符
                            if (subStr.contains(" and ") || subStr.contains(" or ") ||
                                (j + 2 < i && subStr.startsWith("and ")) ||
                                (j + 1 < i && subStr.startsWith("or "))) {
                                hasOperator = true;
                                break; // 找到运算符后无需继续检查
                            }
                        }
                        
                        // 检查是否是单独的变量或函数，如果是则保留括号
                        // 这部分主要针对 "(c)" 这样的情况
                        if (input.substring(openPos + 1, i).trim().split("\\s+").length == 1) {
                            hasSingleTerm = false; // 虽然是单一项，但我们认为它不冗余
                            break;
                        }
                    }
                }
                
                // 如果括号对中没有其他括号且没有逻辑运算符，则为冗余
                if (hasSingleTerm && !hasOperator) {
                    toRemove[openPos] = true;
                    toRemove[i] = true;
                }
            }
        }
        
        // 构建结果字符串，跳过标记为移除的字符
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (!toRemove[i]) {
                result.append(chars[i]);
            }
        }
        
        // 确保末尾有一个空格
        if (result.length() > 0 && !Character.isWhitespace(result.charAt(result.length() - 1))) {
            result.append(" ");
        }
        
        // 去除字符串开头的空白字符
        String finalResult = result.toString();
        int startIndex = 0;
        while (startIndex < finalResult.length() && Character.isWhitespace(finalResult.charAt(startIndex))) {
            startIndex++;
        }
        
        return finalResult.substring(startIndex);
    }
}