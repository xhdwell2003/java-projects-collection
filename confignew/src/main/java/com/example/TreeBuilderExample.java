package com.example;

import com.alibaba.fastjson.JSON;
import com.example.entity.FilterCondition;
import com.example.entity.MonChildrenExpr;
import com.example.service.TreeBuilderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 多层树构建示例应用
 */
@SpringBootApplication
public class TreeBuilderExample {

    public static void main(String[] args) {
        SpringApplication.run(TreeBuilderExample.class, args);
    }

    /**
     * 示例运行器
     */
    @Component
    public static class ExampleRunner implements CommandLineRunner {

        @Autowired
        private TreeBuilderService treeBuilderService;

        @Override
        public void run(String... args) {
            System.out.println("开始执行多层树构建示例...");
            
            // 构建示例数据
            List<FilterCondition> metricConditions = createMetricConditions();
            String exp = "Step1 = NEWMETRIC1 or NEWMETRIC2; Step2 = Step1 or NEWMETRIC4";
            
            System.out.println("表达式: " + exp);
            System.out.println("指标条件: " + JSON.toJSONString(metricConditions));
            
            // 调用服务构建多层树
            List<MonChildrenExpr> result = treeBuilderService.buildTreeExpressions(exp, metricConditions);
            
            // 打印结果
            System.out.println("\n构建结果 (共 " + result.size() + " 条记录):");
            for (int i = 0; i < result.size(); i++) {
                MonChildrenExpr expr = result.get(i);
                System.out.println((i + 1) + ". " + expr.getOriginId() + " -> " + expr.getExpr());
            }
        }
        
        /**
         * 创建测试用的指标条件
         */
        private List<FilterCondition> createMetricConditions() {
            List<FilterCondition> conditions = new ArrayList<>();
            
            // 添加指标条件
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
    }
} 