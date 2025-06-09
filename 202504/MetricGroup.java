package cmb.custody.configuration.domain.indexcalculate.model;

import cmb.custody.monitor.domain.metric.entity.MetricFilterCondition;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @author ho174929
 * @description
 * @createDate 2025/4/15 11:04
 */
@SuperBuilder
@Data
public class MetricGroup extends Metric {

    //虚拟id
    private String subId;
    //步骤编号
    private String step;
    //子度量间关联关系
    private String relation;
    //左子度量
    private Metric leftChildMetric;
    //右子度量
    private Metric rightChildMetric;
    //度量过滤条件表达式
    private String filterExpression;
    //度量过滤条件
    private List<MetricFilterCondition> filterCondition;

}