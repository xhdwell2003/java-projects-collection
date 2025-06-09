package com.example.service;

import com.alibaba.fastjson.JSON;
import com.example.entity.Element;
import com.example.entity.FilterCondition;
import com.example.entity.MonChildrenExpr;
import com.example.expression.FormulaParserImpl;
import com.example.expression.FormulaVisitorImpl;
import com.example.util.SpringContextUtil;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 多层树构建服务接口
 */
public interface TreeBuilderService {
    
    /**
     * 构建多层树表达式
     *
     * @param exp 表达式
     * @param metricConditions 指标条件列表
     * @return 多层树表达式列表
     */
    List<MonChildrenExpr> buildTreeExpressions(String exp, List<FilterCondition> metricConditions);
}

/**
 * 多层树构建服务
 */
@Service
public class TreeBuilderServiceImpl implements TreeBuilderService {

    /**
     * 构建多层树表达式
     *
     * @param exp 表达式
     * @param metricConditions 指标条件列表
     * @return 多层树表达式列表
     */
    @Override
    public List<MonChildrenExpr> buildTreeExpressions(String exp, List<FilterCondition> metricConditions) {
        // 初始化结果列表
        List<MonChildrenExpr> monChildrenExprs = new ArrayList<>();
        
        // 创建过滤条件映射
        Map<String, String> filterMetricMap = metricConditions.stream()
                .collect(Collectors.toMap(FilterCondition::getFilterId, FilterCondition::getSourceId, (o, n) -> n));
        
        // 获取表达式解析器
        FormulaParserImpl formulaParser = SpringContextUtil.getBean(FormulaParserImpl.class);
        formulaParser.setFilterMetricMap(filterMetricMap);
        
        // 解析表达式
        FormulaVisitorImpl formulaVisitor = formulaParser.obtainFormulaResult(exp);
        List<Element> elementList = formulaVisitor.getElementList();
        Map<String, String> keyFilterMap = formulaVisitor.getKeyFilterMap();
        
        // 创建度量ID映射（保存度量值到生成的度量ID的映射）
        Map<String, String> sourceIdToMetricIdMap = new HashMap<>();
        
        // 处理基础度量
        processBaseMetrics(filterMetricMap, sourceIdToMetricIdMap, monChildrenExprs);
        
        // 处理组合度量
        processExprElements(elementList, keyFilterMap, filterMetricMap, sourceIdToMetricIdMap, monChildrenExprs);
        
        return monChildrenExprs;
    }
    
    /**
     * 处理基础度量，创建度量ID
     * 
     * @param filterMetricMap 过滤条件到源ID的映射
     * @param sourceIdToMetricIdMap 源ID到度量ID的映射
     * @param monChildrenExprs 多层树表达式列表
     */
    private void processBaseMetrics(Map<String, String> filterMetricMap, 
                                   Map<String, String> sourceIdToMetricIdMap, 
                                   List<MonChildrenExpr> monChildrenExprs) {
        // 获取所有唯一的源ID
        Set<String> uniqueSourceIds = new HashSet<>(filterMetricMap.values());
        
        // 为每个唯一的源ID创建度量表达式
        for (String sourceId : uniqueSourceIds) {
            String metricId = generateMetricId();
            sourceIdToMetricIdMap.put(sourceId, metricId);
            
            // 创建基础度量表达式
            MonChildrenExpr baseExpr = new MonChildrenExpr("M", metricId, sourceId);
            monChildrenExprs.add(baseExpr);
        }
    }
    
    /**
     * 处理表达式元素，创建组合度量
     * 
     * @param elementList 元素列表
     * @param keyFilterMap 键过滤映射
     * @param filterMetricMap 过滤条件到源ID的映射
     * @param sourceIdToMetricIdMap 源ID到度量ID的映射
     * @param monChildrenExprs 多层树表达式列表
     */
    private void processExprElements(List<Element> elementList, 
                                    Map<String, String> keyFilterMap, 
                                    Map<String, String> filterMetricMap, 
                                    Map<String, String> sourceIdToMetricIdMap, 
                                    List<MonChildrenExpr> monChildrenExprs) {
        // 临时存储中间结果标识符与其对应的度量ID
        Map<String, String> resultToMetricIdMap = new HashMap<>();
        
        for (Element element : elementList) {
            String leftId;
            String rightId;
            
            // 处理左侧
            if (resultToMetricIdMap.containsKey(element.getLeft())) {
                // 左侧是之前处理过的中间结果
                leftId = resultToMetricIdMap.get(element.getLeft());
            } else if (keyFilterMap.containsKey(element.getLeft())) {
                // 左侧是过滤表达式
                String filterExpr = keyFilterMap.get(element.getLeft());
                String sourceId = filterMetricMap.get(filterExpr);
                leftId = sourceIdToMetricIdMap.get(sourceId);
            } else {
                // 左侧是直接度量ID
                String sourceId = filterMetricMap.get(element.getLeft());
                leftId = sourceIdToMetricIdMap.get(sourceId);
            }
            
            // 处理右侧
            if (resultToMetricIdMap.containsKey(element.getRight())) {
                // 右侧是之前处理过的中间结果
                rightId = resultToMetricIdMap.get(element.getRight());
            } else if (keyFilterMap.containsKey(element.getRight())) {
                // 右侧是过滤表达式
                String filterExpr = keyFilterMap.get(element.getRight());
                String sourceId = filterMetricMap.get(filterExpr);
                rightId = sourceIdToMetricIdMap.get(sourceId);
            } else {
                // 右侧是直接度量ID
                String sourceId = filterMetricMap.get(element.getRight());
                rightId = sourceIdToMetricIdMap.get(sourceId);
            }
            
            // 创建当前元素的度量ID
            String resultMetricId = generateMetricId();
            resultToMetricIdMap.put(element.getResult(), resultMetricId);
            
            // 创建组合表达式
            String expr = leftId + " " + element.getMiddle() + " " + rightId;
            MonChildrenExpr resultExpr = new MonChildrenExpr("M", resultMetricId, expr);
            monChildrenExprs.add(resultExpr);
        }
    }
    
    /**
     * 生成度量ID
     *
     * @return 度量ID
     */
    private String generateMetricId() {
        return "METRICSYS" + Math.abs(new Random().nextInt(Integer.MAX_VALUE));
    }
} 