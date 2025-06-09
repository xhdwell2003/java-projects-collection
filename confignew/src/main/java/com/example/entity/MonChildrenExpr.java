package com.example.entity;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 多层树表达式实体类
 */
@Data
public class MonChildrenExpr {
    private String systemId;
    private String originType;
    private String originId;
    private String expr;
    private String createUser;
    private String updateUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public MonChildrenExpr(String originId, String expr) {
        systemId = "CORE";
        originType = "M";
        this.originId = originId;
        this.expr = expr;
    }
    
    public MonChildrenExpr(String originType, String originId, String expr) {
        systemId = "CORE";
        this.originType = originType;
        this.originId = originId;
        this.expr = expr;
    }
} 