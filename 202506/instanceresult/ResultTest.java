import java.util.*;
import java.util.regex.Pattern;
import java.io.Serializable;
import java.util.Date;

/**
 * Result类的测试类，验证calculateIndexRatio方法
 */
public class ResultTest {
    
    public static void main(String[] args) {
        // 根据简化需求1.md中的数据样例进行测试
        testCalculateIndexRatio();
    }
    
    /**
     * 测试calculateIndexRatio方法
     */
    public static void testCalculateIndexRatio() {
        System.out.println("=== 测试 Result.calculateIndexRatio 方法 ===");
        
        // 1. 构建测试数据 - colList
        List<DataCol> numeratorColList = Arrays.asList(
            new DataCol("ITEMSYS1215071987", "KOObi", "DECIMAL", "分子：市值总额")
        );
        
        List<DataCol> denominatorColList = Arrays.asList(
            new DataCol("ITEMSYS1215071702", "ZQFXRNQY", "STRING", "发行人（考虑原始权益）")
        );
        
        // 2. 构建测试数据 - dataList
        List<DataCell> numeratorCells = Arrays.asList(
            new DataCell("0", "KOObi", "1000.0")
        );
        List<RowData> numeratorDataList = Arrays.asList(
            new RowData(numeratorCells, 0)
        );
        
        List<DataCell> denominatorCells = Arrays.asList(
            new DataCell("0", "ZQFXRNQY", "")
        );
        List<RowData> denominatorDataList = Arrays.asList(
            new RowData(denominatorCells, 0)
        );
        
        // 3. 构建测试数据 - groupKey
        Map<String, String> groupKey = new HashMap<>();
        groupKey.put("ZQFXRNQY", "发行人（考虑原始权益）");
        
        // 4. 构建分子和分母的MetricResult
        MetricResult numeratorRs = new MetricResult(numeratorColList, numeratorDataList, "numerator_metric", groupKey);
        MetricResult denominatorRs = new MetricResult(denominatorColList, denominatorDataList, "denominator_metric", groupKey);
        
        // 5. 构建metricDataItems
        List<MetricDataItem> metricDataItems = Arrays.asList(
            MetricDataItem.builder()
                .dataItemId("ITEMSYS1215071987")
                .dataItemField("KOObi")
                .dataType("DECIMAL")
                .dataSourceDescription("分子：市值总额")
                .dataSourceType("df1")
                .build(),
            MetricDataItem.builder()
                .dataItemId("ITEMSYS1215071702")
                .dataItemField("ZQFXRNQY")
                .dataType("STRING")
                .dataSourceDescription("发行人（考虑原始权益）")
                .dataSourceType("df2")
                .build()
        );
        
        // 6. 调用calculateIndexRatio方法
        MetricResult result = Result.calculateIndexRatio(numeratorRs, denominatorRs, metricDataItems);
        
        // 7. 验证结果
        System.out.println("计算结果：");
        System.out.println("MetricId: " + result.getMetricId());
        System.out.println("列定义数量: " + result.getColList().size());
        System.out.println("数据行数量: " + result.getDataList().size());
        
        System.out.println("\n列定义详情：");
        for (int i = 0; i < result.getColList().size(); i++) {
            DataCol col = result.getColList().get(i);
            System.out.println(String.format("第%d列: %s (%s) - %s", 
                i+1, col.getName(), col.getType(), col.getDescription()));
        }
        
        System.out.println("\n数据行详情：");
        for (int i = 0; i < result.getDataList().size(); i++) {
            RowData row = result.getDataList().get(i);
            System.out.println(String.format("第%d行 (resultId=%d):", i+1, row.getResultId()));
            for (DataCell cell : row.getCellList()) {
                System.out.println(String.format("  %s = %s", cell.getColName(), cell.getValue()));
            }
        }
        
        System.out.println("\nGroupKey: " + result.getGroupKey());
        
        // 8. 验证期望结果
        System.out.println("\n=== 验证结果 ===");
        
        // 验证列数量（分子数据项 + 分母数据项 + 分子输出项 + 分母输出项 + 比率列）
        int expectedColCount = 1 + 1 + 1 + 1 + 1; // KOObi + ZQFXRNQY + KOObi输出 + ZQFXRNQY输出 + ratio
        System.out.println("期望列数: " + expectedColCount + ", 实际列数: " + result.getColList().size());
        
        // 验证数据行数量（笛卡尔积：1 * 1 = 1）
        int expectedRowCount = 1;
        System.out.println("期望行数: " + expectedRowCount + ", 实际行数: " + result.getDataList().size());
        
        // 验证是否包含比率列
        boolean hasRatioCol = result.getColList().stream()
            .anyMatch(col -> "ratio".equals(col.getName()));
        System.out.println("包含比率列: " + hasRatioCol);
        
        System.out.println("\n测试完成！");
    }
}