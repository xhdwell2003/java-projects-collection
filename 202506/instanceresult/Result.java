import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 结果计算类
 */
public class Result {
    
    /**
     * 计算指标比率
     * @param numeratorRs 分子的输出项
     * @param denominatorRs 分母输出项  
     * @param metricDataItems 数据项
     * @return 计算后的结果
     */
    public static MetricResult calculateIndexRatio(MetricResult numeratorRs, MetricResult denominatorRs, List<MetricDataItem> metricDataItems) {
        // 1. 从metricDataItems中分离出分子和分母的数据项
        List<MetricDataItem> numeratorItems = metricDataItems.stream()
            .filter(item -> "df1".equals(item.getDataSourceType()))
            .collect(Collectors.toList());
            
        List<MetricDataItem> denominatorItems = metricDataItems.stream()
            .filter(item -> "df2".equals(item.getDataSourceType()))
            .collect(Collectors.toList());
        
        // 2. 构建新的列定义（分子数据项 + 分母数据项 + 分子输出项/分母输出项）
        List<DataCol> resultColList = new ArrayList<>();
        
        // 添加分子的所有数据项列
        for (MetricDataItem item : numeratorItems) {
            resultColList.add(new DataCol(item.getDataItemId(), item.getDataItemField(), 
                item.getDataType(), item.getDataSourceDescription()));
        }
        
        // 添加分母的所有数据项列
        for (MetricDataItem item : denominatorItems) {
            resultColList.add(new DataCol(item.getDataItemId(), item.getDataItemField(), 
                item.getDataType(), item.getDataSourceDescription()));
        }
        
        // 添加分子输出项的列
        if (numeratorRs != null && numeratorRs.getColList() != null) {
            resultColList.addAll(numeratorRs.getColList());
        }
        
        // 添加分母输出项的列
        if (denominatorRs != null && denominatorRs.getColList() != null) {
            resultColList.addAll(denominatorRs.getColList());
        }
        
        // 添加比率列（分子输出项/分母输出项）
        resultColList.add(new DataCol("RATIO_RESULT", "ratio", "DECIMAL", "分子输出项/分母输出项"));
        
        // 3. 进行笛卡尔积计算，生成数据行（使用Stream优化）
        // 获取分子和分母的数据行
        List<RowData> numeratorDataList = numeratorRs != null ? numeratorRs.getDataList() : new ArrayList<>();
        List<RowData> denominatorDataList = denominatorRs != null ? denominatorRs.getDataList() : new ArrayList<>();
        
        // 如果其中一个为空，创建一个空行用于笛卡尔积
        if (numeratorDataList.isEmpty()) {
            numeratorDataList.add(new RowData(new ArrayList<>(), 0));
        }
        if (denominatorDataList.isEmpty()) {
            denominatorDataList.add(new RowData(new ArrayList<>(), 0));
        }
        
        // 使用AtomicInteger来处理resultId的自增
        AtomicInteger resultIdCounter = new AtomicInteger(0);
        
        // 使用Stream执行笛卡尔积
        List<RowData> resultDataList = numeratorDataList.stream()
            .flatMap(numeratorRow ->
                denominatorDataList.stream()
                    .map(denominatorRow -> createCombinedRowData(
                        numeratorRow,
                        denominatorRow,
                        numeratorItems,
                        denominatorItems,
                        resultIdCounter.getAndIncrement()
                    ))
            )
            .collect(Collectors.toList());
        
        // 4. 合并groupKey
        Map<String, String> mergedGroupKey = new HashMap<>();
        if (numeratorRs != null && numeratorRs.getGroupKey() != null) {
            mergedGroupKey.putAll(numeratorRs.getGroupKey());
        }
        if (denominatorRs != null && denominatorRs.getGroupKey() != null) {
            mergedGroupKey.putAll(denominatorRs.getGroupKey());
        }
        
        // 5. 构建结果
        String metricId = "RATIO_METRIC";
        if (numeratorRs != null && numeratorRs.getMetricId() != null) {
            metricId = numeratorRs.getMetricId() + "_RATIO";
        }
        
        return new MetricResult(resultColList, resultDataList, metricId, mergedGroupKey);
    }
    
    /**
     * 创建合并的行数据（笛卡尔积的一行结果）
     * @param numeratorRow 分子行数据
     * @param denominatorRow 分母行数据
     * @param numeratorItems 分子数据项列表
     * @param denominatorItems 分母数据项列表
     * @param resultId 结果ID
     * @return 合并后的行数据
     */
    private static RowData createCombinedRowData(RowData numeratorRow, RowData denominatorRow,
                                               List<MetricDataItem> numeratorItems,
                                               List<MetricDataItem> denominatorItems,
                                               int resultId) {
        List<DataCell> combinedCells = new ArrayList<>();
        String resultIdStr = String.valueOf(resultId);
        
        // 添加分子数据项的值
        numeratorItems.stream()
            .map(item -> new DataCell(resultIdStr, item.getDataItemField(),
                findCellValue(numeratorRow, item.getDataItemField())))
            .forEach(combinedCells::add);
        
        // 添加分母数据项的值
        denominatorItems.stream()
            .map(item -> new DataCell(resultIdStr, item.getDataItemField(),
                findCellValue(denominatorRow, item.getDataItemField())))
            .forEach(combinedCells::add);
        
        // 添加分子输出项的值
        Optional.ofNullable(numeratorRow.getCellList())
            .orElse(Collections.emptyList())
            .stream()
            .map(cell -> new DataCell(resultIdStr, cell.getColName(), cell.getValue()))
            .forEach(combinedCells::add);
        
        // 添加分母输出项的值
        Optional.ofNullable(denominatorRow.getCellList())
            .orElse(Collections.emptyList())
            .stream()
            .map(cell -> new DataCell(resultIdStr, cell.getColName(), cell.getValue()))
            .forEach(combinedCells::add);
        
        // 计算比率（分子输出项/分母输出项）
        String ratioValue = calculateRatio(numeratorRow, denominatorRow);
        combinedCells.add(new DataCell(resultIdStr, "ratio", ratioValue));
        
        return new RowData(combinedCells, resultId);
    }
    
    /**
     * 从行数据中查找指定列名的值
     */
    private static String findCellValue(RowData rowData, String colName) {
        if (rowData == null || rowData.getCellList() == null || colName == null) {
            return "";
        }
        
        return rowData.getCellList().stream()
            .filter(cell -> colName.equals(cell.getColName()))
            .map(DataCell::getValue)
            .findFirst()
            .orElse("");
    }
    
    /**
     * 计算比率值（分子/分母）
     */
    private static String calculateRatio(RowData numeratorRow, RowData denominatorRow) {
        try {
            // 获取分子值（假设分子输出项的第一个数值列）
            String numeratorValue = getFirstNumericValue(numeratorRow);
            // 获取分母值（假设分母输出项的第一个数值列）
            String denominatorValue = getFirstNumericValue(denominatorRow);
            
            if (numeratorValue.isEmpty() || denominatorValue.isEmpty()) {
                return "";
            }
            
            double num = Double.parseDouble(numeratorValue);
            double den = Double.parseDouble(denominatorValue);
            
            if (den == 0) {
                return ""; // 避免除零错误
            }
            
            double ratio = num / den;
            return String.valueOf(ratio);
            
        } catch (NumberFormatException e) {
            return ""; // 如果转换失败，返回空值
        }
    }
    
    /**
     * 获取行数据中第一个数值类型的值
     */
    private static String getFirstNumericValue(RowData rowData) {
        if (rowData == null || rowData.getCellList() == null) {
            return "";
        }
        
        return rowData.getCellList().stream()
            .map(DataCell::getValue)
            .filter(value -> value != null && !value.trim().isEmpty())
            .filter(value -> {
                try {
                    Double.parseDouble(value);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            })
            .findFirst()
            .orElse("");
    }
}
