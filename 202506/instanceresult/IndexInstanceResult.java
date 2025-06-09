import lombok.Getter;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class IndexInstanceResult {
    //组成本结果的所有列
    private List<DataCol> colList;
    //每一行的结果
    private List<RowData> dataList;

    private String instanceId;

    private String indexId;

    //汇总用的字段
    private Map<String, String> groupKey;
}
