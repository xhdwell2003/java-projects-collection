package com.custody.flowtask.application.task.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author ho174929
 * @description
 * @createDate 2025/9/2 10:48
 */
@Data
public class TaskInstanceRspDTO {

    private String taskType;
    private String taskName;
    private String busTaskType;
    private String busTaskName;
    private long taskInsId;
    private long nodeInsId;
    private String taskState;
    private String nodeState;
    private String nodeType;
    private String nodeName;
    private String descs;
    private LocalDateTime taskCreateTime;
    private LocalDateTime taskUpdateTime;
    private LocalDateTime nodeCreateTime;
    private LocalDateTime nodeUpdateTime;
    private String startHandlerId;
    private String startOrgcode;
    private String realHandlerId;
    private String remark;
    private String operSource;
    private String operType;
    private String startOperType;
    private String startOperTypeName;
    private String businessId;
    private String isMyTask;
    private String flowType;
    private String isFinish;

}
