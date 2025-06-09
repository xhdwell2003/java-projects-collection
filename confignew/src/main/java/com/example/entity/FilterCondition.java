package com.example.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * 过滤条件实体类
 */
@Data
public class FilterCondition {
    
    /**
     * 过滤条件ID
     */
    private String filterId;
    
    /**
     * 数据源ID
     */
    private String sourceId;
    
    /**
     * 数据源名称
     */
    private String sourceName;
    
    /**
     * 属性ID
     */
    private String attrId;
    
    /**
     * 属性名称
     */
    private String attrName;
    
    /**
     * 操作符ID
     */
    private String flagId;
    
    /**
     * 值ID
     */
    private String valueId;
    
    /**
     * 单位ID
     */
    private String unitId;
    
    /**
     * 指标类型
     */
    private String metricType;
    
    /**
     * 是否插入表达式
     */
    private Boolean isExprInsert;
    
    /**
     * 附加参数名称
     */
    private String addParamName;
    
    /**
     * 附加参数值
     */
    private String addParamValue;
    
    /**
     * 类型
     */
    private String type;
    
    /**
     * 操作符名称
     */
    private String flagName;
    
    /**
     * 值名称
     */
    private String valueName;
    
    /**
     * 表达式ID
     */
    private String exprId;
    
    /**
     * 表达式名称
     */
    private String exprName;
} 