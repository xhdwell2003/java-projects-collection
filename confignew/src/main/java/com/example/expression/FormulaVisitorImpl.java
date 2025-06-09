package com.example.expression;

import com.example.entity.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 公式访问器实现类
 */
public class FormulaVisitorImpl {
    
    private List<Element> elementList = new ArrayList<>();
    private Map<String, String> keyFilterMap = new HashMap<>();
    private Map<String, String> filterMetricMap = new HashMap<>();
    
    /**
     * 添加元素
     * 
     * @param element 元素
     */
    public void addElement(Element element) {
        elementList.add(element);
    }
    
    /**
     * 获取元素列表
     * 
     * @return 元素列表
     */
    public List<Element> getElementList() {
        return elementList;
    }
    
    /**
     * 设置键过滤映射
     * 
     * @param keyFilterMap 键过滤映射
     */
    public void setKeyFilterMap(Map<String, String> keyFilterMap) {
        this.keyFilterMap = keyFilterMap;
    }
    
    /**
     * 获取键过滤映射
     * 
     * @return 键过滤映射
     */
    public Map<String, String> getKeyFilterMap() {
        return keyFilterMap;
    }
    
    /**
     * 设置过滤条件到指标的映射
     * 
     * @param filterMetricMap 过滤条件到指标的映射
     */
    public void setFilterMetricMap(Map<String, String> filterMetricMap) {
        this.filterMetricMap = filterMetricMap;
    }
    
    /**
     * 获取过滤条件到指标的映射
     * 
     * @return 过滤条件到指标的映射
     */
    public Map<String, String> getFilterMetricMap() {
        return filterMetricMap;
    }
} 