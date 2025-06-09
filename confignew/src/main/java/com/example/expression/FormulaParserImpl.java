package com.example.expression;

import com.example.entity.Element;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 公式解析器实现类
 */
@Component
public class FormulaParserImpl {
    
    private Map<String, String> filterMetricMap = new HashMap<>();
    
    /**
     * 设置过滤条件到指标的映射
     * 
     * @param filterMetricMap 过滤条件到指标的映射
     */
    public void setFilterMetricMap(Map<String, String> filterMetricMap) {
        this.filterMetricMap = filterMetricMap;
    }
    
    /**
     * 获取公式解析结果
     * 
     * @param expression 表达式
     * @return 公式访问器实现类
     */
    public FormulaVisitorImpl obtainFormulaResult(String expression) {
        FormulaVisitorImpl visitor = new FormulaVisitorImpl();
        
        // 解析表达式中的操作元素
        parseExpression(expression, visitor);
        
        // 设置过滤条件到指标的映射
        visitor.setFilterMetricMap(filterMetricMap);
        
        return visitor;
    }
    
    /**
     * 解析表达式，提取元素列表
     * 
     * @param expression 表达式
     * @param visitor 公式访问器
     */
    private void parseExpression(String expression, FormulaVisitorImpl visitor) {
        // 解析像 "Step1 = NEWMETRIC1 or NEWMETRIC2" 这样的表达式
        Pattern pattern = Pattern.compile("(\\w+)\\s*=\\s*(\\w+)\\s*(and|or)\\s*(\\w+)");
        Matcher matcher = pattern.matcher(expression);
        
        Map<String, String> keyFilterMap = new HashMap<>();
        
        while (matcher.find()) {
            String result = matcher.group(1);
            String left = matcher.group(2);
            String middle = matcher.group(3);
            String right = matcher.group(4);
            
            Element element = new Element();
            element.setResult(result);
            element.setLeft(left);
            element.setMiddle(middle);
            element.setRight(right);
            
            visitor.addElement(element);
            
            // 根据解析出的元素构建键过滤映射
            if (!keyFilterMap.containsKey(result)) {
                keyFilterMap.put(result, left + " " + middle + " " + right);
            }
        }
        
        visitor.setKeyFilterMap(keyFilterMap);
    }
} 