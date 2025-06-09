package com.example.filter;

import java.util.HashMap;
import java.util.Map;

/**
 * 过滤条件表达式处理演示
 */
public class FilterExpressionDemo {

    public static void main(String[] args) {
        // 原始表达式
        String expression = "(  (  filterMWMAz  and  filterFLJta  or  filterdLBHI  and  filterlqEqW  )   or filterdLGFI and filtershFGI and filtershdRJ )";
        System.out.println("原始表达式: " + expression);
        
        // 构建filterId和sourceId的映射关系
        Map<String, String> filterMap = buildFilterMap();
        
        // 处理表达式
        String processedExpression = FilterExpressionProcessor.processExpressionWithMap(expression, filterMap);
        System.out.println("处理后表达式: " + processedExpression);
        
        // 验证处理结果是否符合预期
        String expectedExpression = "(  ( ( filterMWMAz  and  filterFLJta ) or ( filterdLBHI  and  filterlqEqW ) )   or ( filterdLGFI and filtershFGI and filtershdRJ ) )";
        System.out.println("预期表达式: " + expectedExpression);
        
        // 检查处理后的表达式是否与预期一致（忽略空格的影响）
        boolean isMatch = processedExpression.replaceAll("\\s+", "")
                .equals(expectedExpression.replaceAll("\\s+", ""));
        System.out.println("处理结果是否符合预期: " + isMatch);
    }
    
    /**
     * 构建示例的filterId和sourceId映射关系
     */
    private static Map<String, String> buildFilterMap() {
        Map<String, String> filterMap = new HashMap<>();
        
        // 添加映射关系
        filterMap.put("filterMWMAz", "metric00000001");
        filterMap.put("filterFLJta", "metric00000001");
        
        filterMap.put("filterdLBHI", "metric00000002");
        filterMap.put("filterlqEqW", "metric00000002");
        
        filterMap.put("filterdLGFI", "metric00000003");
        filterMap.put("filtershFGI", "metric00000003");
        filterMap.put("filtershdRJ", "metric00000003");
        
        return filterMap;
    }
} 