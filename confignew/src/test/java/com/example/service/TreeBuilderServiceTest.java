package com.example.service;

import com.example.entity.Element;
import com.example.entity.FilterCondition;
import com.example.entity.MonChildrenExpr;
import com.example.expression.FormulaParserImpl;
import com.example.expression.FormulaVisitorImpl;
import com.example.util.SpringContextUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 多层树构建服务测试类
 */
public class TreeBuilderServiceTest {

    @InjectMocks
    private TreeBuilderServiceImpl treeBuilderService;

    @Mock
    private FormulaParserImpl formulaParser;

    @Mock
    private FormulaVisitorImpl formulaVisitor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        // 设置SpringContextUtil的模拟行为
        mockSpringContextUtil();
    }

    /**
     * 测试多层树构建功能
     */
    @Test
    public void testBuildTreeExpressions() {
        // 准备测试数据
        String exp = "Step1 = NEWMETRIC1 or NEWMETRIC2; Step2 = Step1 or NEWMETRIC4";
        List<FilterCondition> metricConditions = createMetricConditions();
        
        // 设置模拟行为
        setupMockBehavior();
        
        // 调用测试方法
        List<MonChildrenExpr> result = treeBuilderService.buildTreeExpressions(exp, metricConditions);
        
        // 验证结果
        assertEquals(5, result.size());
        
        // 验证基础指标
        assertContainsExpr(result, "METRIC00000003");
        assertContainsExpr(result, "METRIC00000002");
        assertContainsExpr(result, "METRIC00000309");
        
        // 验证组合指标格式（由于ID是随机生成的，我们只能验证格式）
        assertContainsExprFormat(result, "METRICSYS", " or ");
        assertEquals(2, countExprsWithFormat(result, " or "));
    }
    
    /**
     * 测试指标表达式中是否包含特定的表达式
     */
    private void assertContainsExpr(List<MonChildrenExpr> exprs, String expectedExpr) {
        boolean found = false;
        for (MonChildrenExpr expr : exprs) {
            if (expr.getExpr().equals(expectedExpr)) {
                found = true;
                break;
            }
        }
        assertEquals("应该包含表达式: " + expectedExpr, true, found);
    }
    
    /**
     * 测试指标表达式中是否包含特定格式的表达式
     */
    private void assertContainsExprFormat(List<MonChildrenExpr> exprs, String idPrefix, String operator) {
        boolean found = false;
        for (MonChildrenExpr expr : exprs) {
            if (expr.getExpr().contains(idPrefix) && expr.getExpr().contains(operator)) {
                found = true;
                break;
            }
        }
        assertEquals("应该包含格式为: [" + idPrefix + "...] " + operator + " [" + idPrefix + "...] 的表达式", true, found);
    }
    
    /**
     * 计算包含特定格式表达式的数量
     */
    private int countExprsWithFormat(List<MonChildrenExpr> exprs, String operator) {
        int count = 0;
        for (MonChildrenExpr expr : exprs) {
            if (expr.getExpr().contains(operator)) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * 创建测试用的指标条件
     */
    private List<FilterCondition> createMetricConditions() {
        List<FilterCondition> conditions = new ArrayList<>();
        
        // 添加测试条件
        addCondition(conditions, "filterGYQLc", "METRIC00000003");
        addCondition(conditions, "filterJKPyx", "METRIC00000003");
        addCondition(conditions, "filtersLXTg", "METRIC00000002");
        addCondition(conditions, "filterbFmOT", "METRIC00000002");
        addCondition(conditions, "filterqzzYv", "METRIC00000309");
        
        return conditions;
    }
    
    /**
     * 添加指标条件
     */
    private void addCondition(List<FilterCondition> conditions, String filterId, String sourceId) {
        FilterCondition condition = new FilterCondition();
        condition.setFilterId(filterId);
        condition.setSourceId(sourceId);
        conditions.add(condition);
    }
    
    /**
     * 设置模拟行为
     */
    private void setupMockBehavior() {
        // 创建元素列表
        List<Element> elementList = new ArrayList<>();
        
        Element element1 = new Element();
        element1.setResult("Step1");
        element1.setLeft("NEWMETRIC1");
        element1.setMiddle("or");
        element1.setRight("NEWMETRIC2");
        
        Element element2 = new Element();
        element2.setResult("Step2");
        element2.setLeft("Step1");
        element2.setMiddle("or");
        element2.setRight("NEWMETRIC4");
        
        elementList.add(element1);
        elementList.add(element2);
        
        // 创建键过滤映射
        Map<String, String> keyFilterMap = new HashMap<>();
        keyFilterMap.put("NEWMETRIC4", "filterqzzYv");
        keyFilterMap.put("NEWMETRIC1", " ( filterGYQLc and filterJKPyx ) ");
        keyFilterMap.put("NEWMETRIC2", " ( filtersLXTg or filterbFmOT ) ");
        
        // 创建过滤条件到指标的映射
        Map<String, String> filterMetricMap = new HashMap<>();
        filterMetricMap.put(" ( filtersLXTg or filterbFmOT ) ", "METRIC00000002");
        filterMetricMap.put(" ( filterGYQLc and filterJKPyx ) ", "METRIC00000003");
        filterMetricMap.put("filterqzzYv", "METRIC00000309");
        
        // 设置模拟对象的行为
        when(formulaVisitor.getElementList()).thenReturn(elementList);
        when(formulaVisitor.getKeyFilterMap()).thenReturn(keyFilterMap);
        when(formulaParser.obtainFormulaResult(any())).thenReturn(formulaVisitor);
    }
    
    /**
     * 模拟SpringContextUtil的行为
     */
    private void mockSpringContextUtil() {
        try {
            // 使用PowerMock模拟静态方法
            Mockito.mockStatic(SpringContextUtil.class);
            when(SpringContextUtil.getBean(FormulaParserImpl.class)).thenReturn(formulaParser);
        } catch (Exception e) {
            // 如果不能使用PowerMock，则简单地忽略错误
            // 实际测试需要适当配置PowerMockito或调整代码结构以便于测试
        }
    }
} 