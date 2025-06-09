package com.example.service.impl;

import com.example.dao.MonChildrenExprDao;
import com.example.entity.Element;
import com.example.entity.FilterCondition;
import com.example.entity.MonChildrenExpr;
import com.example.expression.FormulaParserImpl;
import com.example.expression.FormulaVisitorImpl;
import com.example.service.TreeBuilderService;
import com.example.util.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 多层树构建服务实现类
 */
@Service
public class TreeBuilderServiceImpl implements TreeBuilderService {

    @Autowired
    private MonChildrenExprDao monChildrenExprDao;

    /**
     * 构建多层树表达式
     *
     * @param exp 表达式
     * @param metricConditions 指标条件列表
     * @return 多层树表达式列表
     */
    @Override
    @Transactional
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
        
        // 创建指标ID映射（将指标名称映射到随机生成的唯一ID）
        Map<String, String> metricIdMap = new HashMap<>();
        
        // 第一步：处理所有基础指标
        for (Map.Entry<String, String> entry : filterMetricMap.entrySet()) {
            if (!metricIdMap.containsKey(entry.getValue())) {
                String metricId = generateMetricId();
                metricIdMap.put(entry.getValue(), metricId);
            }
        }
        
        // 第二步：处理组合指标
        for (Element element : elementList) {
            processElement(element, metricIdMap, keyFilterMap, filterMetricMap, monChildrenExprs);
        }
        
        // 保存到数据库
        if (!monChildrenExprs.isEmpty()) {
            // 设置创建和更新信息
            String currentUser = "SYSTEM"; // 可以从上下文中获取当前用户
            Date now = new Date();
            
            for (MonChildrenExpr expr : monChildrenExprs) {
                expr.setCreateUser(currentUser);
                expr.setUpdateUser(currentUser);
            }
            
            // 批量保存
            monChildrenExprDao.batchInsert(monChildrenExprs);
        }
        
        return monChildrenExprs;
    }
    
    /**
     * 处理表达式元素，创建多层树表达式
     *
     * @param element 元素
     * @param metricIdMap 指标ID映射
     * @param keyFilterMap 键过滤映射
     * @param filterMetricMap 过滤条件到指标的映射
     * @param monChildrenExprs 多层树表达式列表
     */
    private void processElement(Element element, Map<String, String> metricIdMap, 
                              Map<String, String> keyFilterMap, Map<String, String> filterMetricMap,
                              List<MonChildrenExpr> monChildrenExprs) {
        String leftMetricId;
        String rightMetricId;
        
        // 处理左侧指标
        if (keyFilterMap.containsKey(element.getLeft())) {
            // 如果左侧是一个中间结果
            String leftExpr = keyFilterMap.get(element.getLeft());
            String leftSourceId = getSourceIdFromKeyFilter(leftExpr, filterMetricMap);
            
            if (!metricIdMap.containsKey(leftSourceId)) {
                leftMetricId = generateMetricId();
                metricIdMap.put(leftSourceId, leftMetricId);
                
                // 如果左侧表达式还没有处理过，先创建左侧表达式的MonChildrenExpr
                MonChildrenExpr leftExprItem = new MonChildrenExpr("M", leftMetricId, 
                        replaceFilterIdsWithMetricIds(leftExpr, filterMetricMap, metricIdMap));
                monChildrenExprs.add(leftExprItem);
            } else {
                leftMetricId = metricIdMap.get(leftSourceId);
            }
        } else {
            // 如果左侧是一个基础指标
            String leftSourceId = getSourceIdFromFilter(element.getLeft(), filterMetricMap);
            leftMetricId = metricIdMap.get(leftSourceId);
        }
        
        // 处理右侧指标
        if (keyFilterMap.containsKey(element.getRight())) {
            // 如果右侧是一个中间结果
            String rightExpr = keyFilterMap.get(element.getRight());
            String rightSourceId = getSourceIdFromKeyFilter(rightExpr, filterMetricMap);
            
            if (!metricIdMap.containsKey(rightSourceId)) {
                rightMetricId = generateMetricId();
                metricIdMap.put(rightSourceId, rightMetricId);
                
                // 如果右侧表达式还没有处理过，先创建右侧表达式的MonChildrenExpr
                MonChildrenExpr rightExprItem = new MonChildrenExpr("M", rightMetricId, 
                        replaceFilterIdsWithMetricIds(rightExpr, filterMetricMap, metricIdMap));
                monChildrenExprs.add(rightExprItem);
            } else {
                rightMetricId = metricIdMap.get(rightSourceId);
            }
        } else {
            // 如果右侧是一个基础指标
            String rightSourceId = getSourceIdFromFilter(element.getRight(), filterMetricMap);
            rightMetricId = metricIdMap.get(rightSourceId);
        }
        
        // 为当前元素创建ID并添加到映射中
        String resultMetricId = generateMetricId();
        metricIdMap.put(element.getResult(), resultMetricId);
        
        // 创建当前元素的表达式
        String expr = leftMetricId + " " + element.getMiddle() + " " + rightMetricId;
        MonChildrenExpr resultExpr = new MonChildrenExpr("M", resultMetricId, expr);
        monChildrenExprs.add(resultExpr);
    }
    
    /**
     * 从过滤键获取源ID
     *
     * @param keyFilter 过滤键
     * @param filterMetricMap 过滤条件到指标的映射
     * @return 源ID
     */
    private String getSourceIdFromKeyFilter(String keyFilter, Map<String, String> filterMetricMap) {
        for (Map.Entry<String, String> entry : filterMetricMap.entrySet()) {
            if (keyFilter.equals(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }
    
    /**
     * 从过滤条件获取源ID
     *
     * @param filter 过滤条件
     * @param filterMetricMap 过滤条件到指标的映射
     * @return 源ID
     */
    private String getSourceIdFromFilter(String filter, Map<String, String> filterMetricMap) {
        return filterMetricMap.get(filter);
    }
    
    /**
     * 将表达式中的过滤条件ID替换为指标ID
     *
     * @param expr 表达式
     * @param filterMetricMap 过滤条件到指标的映射
     * @param metricIdMap 指标ID映射
     * @return 替换后的表达式
     */
    private String replaceFilterIdsWithMetricIds(String expr, Map<String, String> filterMetricMap, 
                                              Map<String, String> metricIdMap) {
        for (Map.Entry<String, String> entry : filterMetricMap.entrySet()) {
            if (expr.contains(entry.getKey())) {
                String metricId = metricIdMap.get(entry.getValue());
                expr = expr.replace(entry.getKey(), metricId);
            }
        }
        return expr;
    }
    
    /**
     * 生成指标ID
     *
     * @return 指标ID
     */
    private String generateMetricId() {
        return "METRICSYS" + Math.abs(new Random().nextInt(Integer.MAX_VALUE));
    }
} 