package com.example.filter;

import com.example.entity.FilterCondition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 过滤条件表达式处理器
 * 
 * 用于处理过滤条件表达式，为同sourceId的连续filter添加括号
 */
public class FilterExpressionProcessor {
    
    /**
     * 处理表达式，使用FilterCondition列表
     * 
     * @param expression 原始表达式
     * @param conditions FilterCondition列表
     * @return 处理后的表达式
     */
    public static String processExpression(String expression, List<FilterCondition> conditions) {
        // 构建filterId到sourceId的映射
        Map<String, String> filterMap = new HashMap<>();
        for (FilterCondition condition : conditions) {
            if (condition.getFilterId() != null && condition.getSourceId() != null) {
                filterMap.put(condition.getFilterId(), condition.getSourceId());
            }
        }
        
        return processExpressionWithMap(expression, filterMap);
    }
    
    /**
     * 使用指定的映射处理表达式
     * 
     * @param expression 原始表达式
     * @param filterMap filterId到sourceId的映射
     * @return 处理后的表达式
     */
    public static String processExpressionWithMap(String expression, Map<String, String> filterMap) {
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
                String sourceId = filterMap.get(token);
                
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
                                && sourceId.equals(filterMap.get(nextNextToken))) {
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
} 