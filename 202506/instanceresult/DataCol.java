import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class DataCol {
    //本列所在的数据项ID
    private String dataItemId;
    //字段名
    private String name;
    //类型
    private String type;
    //中文描述
    private String description;
}
