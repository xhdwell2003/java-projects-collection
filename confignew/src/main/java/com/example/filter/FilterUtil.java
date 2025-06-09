package com.example.filter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 过滤条件工具类
 */
public class FilterUtil {

    /**
     * 根据filterId获取sourceId
     * 
     * @param expression 表达式字符串
     * @param filterMap filterId到sourceId的映射
     * @return 处理后的表达式
     */
    public static String processExpression(String expression, Map<String, String> filterMap) {
        return FilterExpressionProcessor.processExpressionWithMap(expression, filterMap);
    }
    
    /**
     * 将metricCondition列表转换为filterMetricMap
     * key为filterId，value为sourceId
     * 
     * @param metricConditions 条件列表
     * @return 映射关系
     */
    public static Map<String, String> convertToFilterMetricMap(List<Map<String, String>> metricConditions) {
        Map<String, String> filterMetricMap = new HashMap<>();
        
        for (Map<String, String> condition : metricConditions) {
            String filterId = condition.get("filterId");
            String sourceId = condition.get("sourceId");
            
            if (filterId != null && sourceId != null) {
                filterMetricMap.put(filterId, sourceId);
            }
        }
        
        return filterMetricMap;
    }
    
    /**
     * 使用Stream API将metricCondition列表转换为filterMetricMap
     * key为filterId，value为sourceId
     * 
     * @param metricConditions 条件列表
     * @return 映射关系
     */
    public static Map<String, String> convertToFilterMetricMapWithStream(List<Map<String, String>> metricConditions) {
        return metricConditions.stream()
                .filter(condition -> condition.get("filterId") != null && condition.get("sourceId") != null)
                .collect(Collectors.toMap(
                        condition -> condition.get("filterId"),
                        condition -> condition.get("sourceId"),
                        (oldValue, newValue) -> newValue // 如果有重复的key，保留新值
                ));
    }
    
    /**
     * 构建测试用的filterId到sourceId的映射
     */
    public static Map<String, String> buildTestFilterMap() {
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