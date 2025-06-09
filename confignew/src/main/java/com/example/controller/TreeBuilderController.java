package com.example.controller;

import com.alibaba.fastjson.JSON;
import com.example.entity.FilterCondition;
import com.example.entity.MonChildrenExpr;
import com.example.service.TreeBuilderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 多层树构建控制器
 */
@RestController
@RequestMapping("/api/tree")
public class TreeBuilderController {

    @Autowired
    private TreeBuilderService treeBuilderService;

    /**
     * 构建多层树表达式
     *
     * @param requestMap 包含表达式和过滤条件的请求
     * @return 多层树表达式列表
     */
    @PostMapping("/build")
    public List<MonChildrenExpr> buildTree(@RequestBody Map<String, Object> requestMap) {
        // 获取表达式
        String exp = (String) requestMap.get("exp");
        
        // 获取过滤条件列表
        List<FilterCondition> metricConditions = new ArrayList<>();
        List<Map<String, Object>> conditionsList = (List<Map<String, Object>>) requestMap.get("metricConditions");
        
        for (Map<String, Object> condition : conditionsList) {
            FilterCondition filterCondition = new FilterCondition();
            filterCondition.setFilterId((String) condition.get("filterId"));
            filterCondition.setSourceId((String) condition.get("sourceId"));
            // 设置其他属性...
            metricConditions.add(filterCondition);
        }
        
        // 调用服务构建多层树表达式
        List<MonChildrenExpr> result = treeBuilderService.buildTreeExpressions(exp, metricConditions);
        
        return result;
    }
    
    /**
     * 示例方法，展示如何处理需求文档中的案例
     *
     * @return 处理结果
     */
    @PostMapping("/example")
    public Map<String, Object> processExample() {
        // 创建Key过滤映射
        Map<String, String> keyFilterMap = new HashMap<>();
        keyFilterMap.put("NEWMETRIC4", "filterqzzYv");
        keyFilterMap.put("NEWMETRIC1", " ( filterGYQLc and filterJKPyx ) ");
        keyFilterMap.put("NEWMETRIC2", " ( filtersLXTg or filterbFmOT ) ");
        
        // 创建元素列表
        List<Map<String, String>> elementList = new ArrayList<>();
        Map<String, String> element1 = new HashMap<>();
        element1.put("result", "Step1");
        element1.put("left", "NEWMETRIC1");
        element1.put("middle", "or");
        element1.put("right", "NEWMETRIC2");
        
        Map<String, String> element2 = new HashMap<>();
        element2.put("result", "Step2");
        element2.put("left", "Step1");
        element2.put("middle", "or");
        element2.put("right", "NEWMETRIC4");
        
        elementList.add(element1);
        elementList.add(element2);
        
        // 创建过滤条件到指标的映射
        Map<String, String> filterMetricMap = new HashMap<>();
        filterMetricMap.put(" ( filtersLXTg or filterbFmOT ) ", "METRIC00000002");
        filterMetricMap.put("NEWMETRIC1 or NEWMETRIC2", "NEWMETRIC3");
        filterMetricMap.put("filterqzzYv", "METRIC00000309");
        filterMetricMap.put("NEWMETRIC1 or NEWMETRIC2 or NEWMETRIC4", "NEWMETRIC5");
        filterMetricMap.put("filtersLXTg", "METRIC00000002");
        filterMetricMap.put("filterGYQLc", "METRIC00000003");
        filterMetricMap.put("filterbFmOT", "METRIC00000002");
        filterMetricMap.put(" ( filterGYQLc and filterJKPyx ) ", "METRIC00000003");
        filterMetricMap.put("filterJKPyx", "METRIC00000003");
        
        // 创建多层树表达式结果
        List<MonChildrenExpr> monChildrenExprs = new ArrayList<>();
        
        // Step1
        monChildrenExprs.add(new MonChildrenExpr("M", "METRICSYS1199307179", "METRIC00000003"));
        monChildrenExprs.add(new MonChildrenExpr("M", "METRICSYS1199307180", "METRIC00000002"));
        monChildrenExprs.add(new MonChildrenExpr("M", "METRICSYS1199307181", "METRICSYS1199307179 or METRICSYS1199307180"));
        
        // Step2
        monChildrenExprs.add(new MonChildrenExpr("M", "METRICSYS1199307177", "METRIC00000309"));
        monChildrenExprs.add(new MonChildrenExpr("M", "METRICSYS1199307182", "METRICSYS1199307181 or METRICSYS1199307177"));
        
        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("keyFilterMap", keyFilterMap);
        result.put("elementList", elementList);
        result.put("filterMetricMap", filterMetricMap);
        result.put("monChildrenExprs", monChildrenExprs);
        
        return result;
    }
} 