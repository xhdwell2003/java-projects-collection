import lombok.Getter;
import lombok.AllArgsConstructor;
import java.util.List;

@Getter
@AllArgsConstructor
public class RowData {
    private List<DataCell> cellList;
    private int resultId;
}