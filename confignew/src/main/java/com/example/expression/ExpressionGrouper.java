package com.example.expression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 表达式分组工具
 * 根据sourceId对表达式中的filter进行分组并添加括号
 */
public class ExpressionGrouper {

    /**
     * 将表达式中同sourceId的连续filter添加括号
     * 
     * @param expression 原始表达式
     * @param filterIdToSourceIdMap filterId到sourceId的映射关系
     * @return 处理后的表达式
     */
    public static String groupExpressionBySourceId(String expression, Map<String, String> filterIdToSourceIdMap) {
        // 将表达式分解为标记
        List<String> tokens = tokenize(expression);
        
        // 创建一个新的标记列表，可能会添加新的括号
        List<String> newTokens = new ArrayList<>();
        
        // 将标记按sourceId分组
        List<List<Integer>> sourceGroups = groupTokensBySourceId(tokens, filterIdToSourceIdMap);
        
        // 为每个组添加括号
        for (int i = 0; i < tokens.size(); i++) {
            // 检查是否是组的开始
            boolean isGroupStart = false;
            for (List<Integer> group : sourceGroups) {
                if (group.size() > 1 && group.get(0) == i) {
                    isGroupStart = true;
                    newTokens.add("(");
                    break;
                }
            }
            
            // 添加当前标记
            newTokens.add(tokens.get(i));
            
            // 检查是否是组的结束
            boolean isGroupEnd = false;
            for (List<Integer> group : sourceGroups) {
                if (group.size() > 1 && group.get(group.size() - 1) == i) {
                    isGroupEnd = true;
                    newTokens.add(")");
                    break;
                }
            }
        }
        
        // 将新标记列表转换为字符串
        StringBuilder result = new StringBuilder();
        for (String token : newTokens) {
            result.append(token).append(" ");
        }
        
        // 处理表达式，移除重复括号
        String processedExpression = result.toString().trim();
        return removeRedundantParentheses(processedExpression);
    }
    
    /**
     * 移除表达式中的重复括号
     * 
     * @param expression 原始表达式
     * @return 处理后的表达式
     */
    private static String removeRedundantParentheses(String expression) {
        // 将表达式简化为标准形式，去除多余的空格
        String normalized = expression.replaceAll("\\s+", " ").trim();
        
        // 使用正则表达式查找和替换模式： (( ... ))
        String pattern = "\\(\\s*\\(([^()]+)\\)\\s*\\)";
        Pattern regex = Pattern.compile(pattern);
        
        String result = normalized;
        boolean hasChanged;
        
        // 循环处理，直到没有更多变化
        do {
            hasChanged = false;
            Matcher matcher = regex.matcher(result);
            if (matcher.find()) {
                // 简化括号： (( ... )) -> ( ... )
                result = matcher.replaceAll("(" + "$1" + ")");
                hasChanged = true;
            }
        } while (hasChanged);
        
        // 移除同一sourceId组的重复括号
        // 如果遇到类似 "( ( filterA and filterB ) )" 这样的形式，简化为 "( filterA and filterB )"
        pattern = "\\(\\s*\\(\\s*(filter\\w+)(\\s+and\\s+|\\s+or\\s+)(filter\\w+)\\s*\\)\\s*\\)";
        regex = Pattern.compile(pattern);
        
        do {
            hasChanged = false;
            Matcher matcher = regex.matcher(result);
            if (matcher.find()) {
                result = matcher.replaceAll("( " + "$1" + "$2" + "$3" + " )");
                hasChanged = true;
            }
        } while (hasChanged);
        
        return result;
    }
    
    /**
     * 根据sourceId对标记进行分组
     */
    private static List<List<Integer>> groupTokensBySourceId(List<String> tokens, Map<String, String> filterIdToSourceIdMap) {
        List<List<Integer>> groups = new ArrayList<>();
        
        // 记录每个sourceId对应的filter索引
        Map<String, List<Integer>> sourceIdToIndices = new HashMap<>();
        
        // 首先找出所有filter的索引和对应的sourceId
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (token.startsWith("filter")) {
                String sourceId = filterIdToSourceIdMap.get(token);
                if (sourceId != null) {
                    sourceIdToIndices.computeIfAbsent(sourceId, k -> new ArrayList<>()).add(i);
                }
            }
        }
        
        // 然后遍历每个sourceId，找出连续的filter
        for (Map.Entry<String, List<Integer>> entry : sourceIdToIndices.entrySet()) {
            List<Integer> indices = entry.getValue();
            if (indices.size() > 1) {
                // 遍历索引，找出连续的部分
                List<Integer> currentGroup = new ArrayList<>();
                currentGroup.add(indices.get(0));
                
                for (int i = 1; i < indices.size(); i++) {
                    int prevIndex = indices.get(i - 1);
                    int currIndex = indices.get(i);
                    
                    // 检查中间是否只有一个操作符
                    boolean isConnected = false;
                    if (currIndex - prevIndex >= 2) {
                        String middleToken = tokens.get(prevIndex + 1);
                        if ("and".equalsIgnoreCase(middleToken) || "or".equalsIgnoreCase(middleToken)) {
                            isConnected = true;
                        }
                    }
                    
                    if (isConnected) {
                        // 添加到当前组
                        currentGroup.add(currIndex);
                    } else {
                        // 结束当前组，开始新组
                        if (currentGroup.size() > 1) {
                            groups.add(new ArrayList<>(currentGroup));
                        }
                        currentGroup.clear();
                        currentGroup.add(currIndex);
                    }
                }
                
                // 添加最后一个组
                if (currentGroup.size() > 1) {
                    groups.add(currentGroup);
                }
            }
        }
        
        return groups;
    }
    
    /**
     * 将表达式拆分为词元
     * 
     * @param expression 表达式
     * @return 词元列表
     */
    private static List<String> tokenize(String expression) {
        List<String> tokens = new ArrayList<>();
        
        // 匹配filter开头的ID、括号和操作符
        Pattern pattern = Pattern.compile("filter\\w+|\\(|\\)|and|or", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(expression);
        
        while (matcher.find()) {
            tokens.add(matcher.group());
        }
        
        return tokens;
    }
    
    /**
     * 使用Stream API从列表中构建filterId到sourceId的映射
     * 
     * @param filterList 过滤条件列表，每个条件是一个Map
     * @return filterId到sourceId的映射
     */
    public static Map<String, String> buildFilterIdToSourceIdMap(List<Map<String, String>> filterList) {
        return filterList.stream()
                .filter(filter -> filter.get("filterId") != null && filter.get("sourceId") != null)
                .collect(Collectors.toMap(
                        filter -> filter.get("filterId"),
                        filter -> filter.get("sourceId"),
                        (oldValue, newValue) -> newValue  // 如果有重复的key，保留新值
                ));
    }
    
    /**
     * 原始的分组实现
     */
    public static String groupExpressionBySourceIdOriginal(String expression, Map<String, String> filterIdToSourceIdMap) {
        // 用于存储处理后的表达式
        StringBuilder result = new StringBuilder();
        
        // 分词器，用于将表达式拆分为词元
        List<String> tokens = tokenize(expression);
        
        // 当前处理的sourceId
        String currentSourceId = null;
        // 记录是否已经开始一个新的sourceId组
        boolean groupStarted = false;
        // 记录括号的层级
        int bracketLevel = 0;
        // 上一个token是否为操作符
        boolean lastIsOperator = true;
        
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            
            if ("(".equals(token)) {
                // 处理左括号
                result.append(token).append(" ");
                bracketLevel++;
                lastIsOperator = true;
            } else if (")".equals(token)) {
                // 处理右括号
                // 如果当前有一个sourceId组未关闭，则先关闭它
                if (groupStarted) {
                    result.append(") ");
                    groupStarted = false;
                }
                result.append(token).append(" ");
                bracketLevel--;
                lastIsOperator = false;
            } else if ("and".equals(token.toLowerCase()) || "or".equals(token.toLowerCase())) {
                // 处理操作符
                result.append(token).append(" ");
                lastIsOperator = true;
            } else if (token.startsWith("filter")) {
                // 处理filterId
                String sourceId = filterIdToSourceIdMap.get(token);
                
                // 如果sourceId为null，则跳过处理
                if (sourceId == null) {
                    result.append(token).append(" ");
                    lastIsOperator = false;
                    continue;
                }
                
                // 如果是一个新的sourceId或者上一个不是操作符
                if (!sourceId.equals(currentSourceId) || !lastIsOperator) {
                    // 关闭前一个sourceId组
                    if (groupStarted) {
                        result.append(") ");
                        groupStarted = false;
                    }
                    
                    // 检查下一个token是否是操作符，以及下下一个token是否是同sourceId的filter
                    boolean shouldStartGroup = false;
                    if (i + 2 < tokens.size()) {
                        String nextToken = tokens.get(i + 1);
                        String nextNextToken = tokens.get(i + 2);
                        
                        if (("and".equals(nextToken.toLowerCase()) || "or".equals(nextToken.toLowerCase())) 
                                && nextNextToken.startsWith("filter") 
                                && sourceId.equals(filterIdToSourceIdMap.get(nextNextToken))) {
                            shouldStartGroup = true;
                        }
                    }
                    
                    if (shouldStartGroup) {
                        result.append("( ");
                        groupStarted = true;
                    }
                    
                    currentSourceId = sourceId;
                }
                
                result.append(token).append(" ");
                lastIsOperator = false;
            } else {
                // 其他token直接追加
                result.append(token).append(" ");
                lastIsOperator = false;
            }
        }
        
        // 如果结束时还有未关闭的sourceId组，则关闭它
        if (groupStarted) {
            result.append(") ");
        }
        
        return result.toString().trim();
    }
    
    /**
     * 示例main方法，尝试使用两种实现方法
     */
    public static void main(String[] args) {
        // 原始表达式
        String expression = "(  (  filterMWMAz  and  filterFLJta  or  filterdLBHI  and  filterlqEqW  )   or filterdLGFI and filtershFGI and filtershdRJ )";
        System.out.println("原始表达式: " + expression);
        
        // 构建filterId和sourceId的映射关系
        Map<String, String> filterMap = new HashMap<>();
        filterMap.put("filterMWMAz", "metric00000001");
        filterMap.put("filterFLJta", "metric00000001");
        filterMap.put("filterdLBHI", "metric00000002");
        filterMap.put("filterlqEqW", "metric00000002");
        filterMap.put("filterdLGFI", "metric00000003");
        filterMap.put("filtershFGI", "metric00000003");
        filterMap.put("filtershdRJ", "metric00000003");
        
        // 处理表达式 - 新实现
        String processedExpression = groupExpressionBySourceId(expression, filterMap);
        System.out.println("处理后表达式(新实现): " + processedExpression);
        
        // 处理表达式 - 原实现
        String processedExpressionOriginal = groupExpressionBySourceIdOriginal(expression, filterMap);
        System.out.println("处理后表达式(原实现): " + processedExpressionOriginal);
        
        // 预期表达式
        String expectedExpression = "(  ( ( filterMWMAz  and  filterFLJta ) or ( filterdLBHI  and  filterlqEqW ) )   or ( filterdLGFI and filtershFGI and filtershdRJ ) )";
        System.out.println("预期表达式: " + expectedExpression);
        
        // 检查结果
        boolean isMatchNew = processedExpression.replaceAll("\\s+", "")
                .equals(expectedExpression.replaceAll("\\s+", ""));
        System.out.println("新实现结果是否符合预期: " + isMatchNew);
        
        boolean isMatchOriginal = processedExpressionOriginal.replaceAll("\\s+", "")
                .equals(expectedExpression.replaceAll("\\s+", ""));
        System.out.println("原实现结果是否符合预期: " + isMatchOriginal);
        
        // 测试已有括号的情况
        String expressionWithParens = "( ( filterMWMAz and filterFLJta ) or ( filterdLBHI and filterlqEqW ) or ( filterdLGFI and filtershFGI and filtershdRJ ) )";
        System.out.println("\n已有括号的表达式: " + expressionWithParens);
        String processed = groupExpressionBySourceId(expressionWithParens, filterMap);
        System.out.println("处理后表达式: " + processed);
    }
} 