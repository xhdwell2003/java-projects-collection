package com.custody.flowtask.application.task.service;

import com.custody.flowtask.application.task.dto.ManagerViewReqDTO;
import com.custody.flowtask.application.task.dto.ManagerViewRspDTO;
import com.custody.flowtask.application.task.dto.TaskInstanceRspDTO;
import com.custody.flowtask.application.task.support.TaskInstanceSupport;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author ho174929
 * @description
 * @createDate 2025/10/23 14:24
 */
@Service
@Slf4j
public class ManagerViewService {

    @Autowired
    private TaskInstanceSupport taskInstanceSupport;

    public List<ManagerViewRspDTO> queryAll(final ManagerViewReqDTO req, final String userId) {
        //查询日期内的所有数据
        List<TaskInstanceRspDTO> taskInstanceRspDTOS = taskInstanceSupport.queryTaskInstanceByManagerView(req, userId);
        
        // 使用stream.forEach为每个taskInstance设置isFinish字段
        Optional.ofNullable(taskInstanceRspDTOS).ifPresent(list ->
            list.forEach(taskInstance -> {
                // 如果taskState为U，则isFinish为N
                if ("U".equals(taskInstance.getTaskState())) {
                    taskInstance.setIsFinish("N");
                }
                // 如果taskState为S，需要根据时间判断
                else if ("S".equals(taskInstance.getTaskState())) {
                    // 检查时间是否为null
                    if (taskInstance.getTaskCreateTime() != null && taskInstance.getTaskUpdateTime() != null) {
                        // 计算taskCreateTime当天的18点时间
                        LocalDateTime taskCreateTime = taskInstance.getTaskCreateTime();
                        LocalDateTime eighteenOClock = taskCreateTime.withHour(18).withMinute(0).withSecond(0).withNano(0);
                        
                        // 如果taskUpdateTime小于当天18点，则isFinish为Y，否则为N
                        if (taskInstance.getTaskUpdateTime().isBefore(eighteenOClock)) {
                            taskInstance.setIsFinish("Y");
                        } else {
                            taskInstance.setIsFinish("N");
                        }
                    }
                    // 时间为null时保持原值不变
                }
                // 其他状态保持原值不变
            })
        );

        // 按机构维度统计数据
        return Optional.ofNullable(taskInstanceRspDTOS)
                .orElseGet(List::of)
                .stream()
                .collect(Collectors.groupingBy(
                    taskInstance -> taskInstance.getStartOrgcode() != null ? taskInstance.getStartOrgcode() : "DEFAULT",
                    Collectors.toList()
                ))
                .entrySet()
                .stream()
                .map(entry -> {
                    String orgid = entry.getKey();
                    List<TaskInstanceRspDTO> taskInstances = entry.getValue();
                    
                    // 创建ManagerViewRspDTO对象
                    ManagerViewRspDTO managerViewRspDTO = new ManagerViewRspDTO();
                    managerViewRspDTO.setOrgid(orgid);
                    // orgName暂时设置为空，后续再处理
                    managerViewRspDTO.setOrgName("");
                    
                    // 统计完成和未完成数量
                    long finishCount = taskInstances.stream()
                            .filter(taskInstance -> "Y".equals(taskInstance.getIsFinish()))
                            .count();
                    long notFinishCount = taskInstances.stream()
                            .filter(taskInstance -> "N".equals(taskInstance.getIsFinish()))
                            .count();
                    long totalCount = taskInstances.size();
                    
                    managerViewRspDTO.setFinishCount((int) finishCount);
                    managerViewRspDTO.setNotFinishCount((int) notFinishCount);
                    managerViewRspDTO.setCount((int) totalCount);
                    
                    // 计算finishPercent
                    String finishPercent = totalCount > 0 ?
                            String.format("%.0f", (double) finishCount / totalCount * 100) + "%" : "0%";
                    managerViewRspDTO.setFinishPercent(finishPercent);
                    
                    // 计算datauploadFinishType
                    boolean allDataUploadFinished = taskInstances.stream()
                            .filter(taskInstance -> "DATAUPLOAD".equals(taskInstance.getFlowType()))
                            .allMatch(taskInstance -> "Y".equals(taskInstance.getIsFinish()));
                    
                    // 如果没有DATAUPLOAD类型的数据，则默认为N
                    boolean hasDataUpload = taskInstances.stream()
                            .anyMatch(taskInstance -> "DATAUPLOAD".equals(taskInstance.getFlowType()));
                    
                    managerViewRspDTO.setDatauploadFinishType(
                            hasDataUpload ? (allDataUploadFinished ? "Y" : "N") : "N"
                    );
                    
                    // 其他finishType暂时设置为空，后续根据需要扩展
                    managerViewRspDTO.setFinishType("");
                    managerViewRspDTO.setPrdmonFinishType("");
                    managerViewRspDTO.setExpprdsetFinishType("");
                    managerViewRspDTO.setNewprdsetFinishType("");
                    managerViewRspDTO.setParamsetFinishType("");
                    managerViewRspDTO.setOtherFinishType("");
                    
                    return managerViewRspDTO;
                })
                .collect(Collectors.toList());
    }
}
