package cmb.custody.configuration.domain.indexcalculate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @author ho174929
 * @description
 * @createDate 2025/4/18 9:45
 */
@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Metric {
    //度量ID
    private String id;
    //输出项
    private List<MetricDataItem> dataItemList;
    //关联字段,jion,union
    private List<MetricRelation> joinRelationList;
    private List<MetricRelation> unionRelationList;
    //度量语句
    private String sql;
}