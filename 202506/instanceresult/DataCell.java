import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class DataCell {
    private String rowId;
    private String colName;
    private String value;
}