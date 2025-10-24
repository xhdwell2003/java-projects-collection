package com.custody.flowtask.application.task.dto;

import lombok.Data;

/**
 * @author ho174929
 * @description
 * @createDate 2025/10/23 14:27
 */
@Data
public class ManagerViewRspDTO {
    private String orgid;
    private String orgName;
    private String finishType;
    private String finishPercent;
    private int notFinishCount;
    private int finishCount;
    private int count;
    /**
     * 数据上传
     */
    private String datauploadFinishType;
    /**
     * 产品监督
     */
    private String prdmonFinishType;
    /**
     * 过期产品设置
     */
    private String expprdsetFinishType;
    /**
     * 产品设置
     */
    private String newprdsetFinishType;
    /**
     * 参数设置
     */
    private String paramsetFinishType;
    /**
     * 其他
     */
    private String otherFinishType;
}
