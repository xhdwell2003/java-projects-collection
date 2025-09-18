import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicInteger;
import java.math.BigDecimal;
import java.math.RoundingMode;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class InstanceDataResult {
    private MetricResult numeratorRs;
    private MetricResult denominatorRs;
    private List<MetricDataItem> indexDataItems;
    private IndexInstance indexInstance;

    /**
     * 分子分母矩阵运算
     * 分子,分母计算
     * o单只
     * 输出项按照汇总列分组汇总后作为指标的输出项
     * o汇总
     * 输出项全部汇总后作为指标的输出项
     * o维度汇总
     * 输出项按照汇总列分组汇总后作为指标的输出项
     * 分子/分母
     * o分母为汇总
     * 列表增加分母的汇总项和输出列,输出列为分子/分母
     * 行数与分子项行数保持一致
     * o分母维度汇总
     * 判断分子分母必须为相同的维度
     * 根据分子和分母的汇总列进行外关联,输出列为分子/分母,没有的项用0补充
     * o分母为单只
     * 判断分子分母必须为相同的维度
     * 根据分子和分母的汇总列进行外关联,输出列为分子/分母,没有的项用0补充
     * @return
     */
    public IndexInstanceResult calculateIndexRatio() {
        //分子项处理，如果分子项为空，则直接返回null
        if (numeratorRs == null || numeratorRs.getDataList() == null || numeratorRs.getDataList().isEmpty()) {
            return null;
        }
        //如果分子的groupkey为空，则使用group方法进行汇总，否则使用singleGroup方法进行单只汇总
        MetricResult numeratorResult = numeratorRs.getGroupKey() == null || numeratorRs.getGroupKey().isEmpty()
                ? group(numeratorRs) : singleGroup(numeratorRs);

        //分母项处理，如果分母项为空，则直接输出分子项的结果
        MetricResult denominatorResult = null;
        if (denominatorRs != null && denominatorRs.getDataList() != null && !denominatorRs.getDataList().isEmpty()) {
            //如果分母的groupkey为空，则使用group方法进行汇总，否则使用singleGroup方法进行单只汇总
            denominatorResult = denominatorRs.getGroupKey() == null || denominatorRs.getGroupKey().isEmpty()
                    ? group(denominatorRs) : singleGroup(denominatorRs);
        }
        //如果分母为null,则直接返回分子值
        if (denominatorResult == null) {
            return new IndexInstanceResult(numeratorResult.getColList(), numeratorResult.getDataList(), indexInstance.getInstanceId(),
                    indexInstance.getIndexId(), numeratorResult.getGroupKey());
        } else {
            //取比例输出项
            String output = indexDataItems.stream().filter(data -> "F".equals(data.getDataSourceType())).map(MetricDataItem::getDataItemField)
                    .findFirst().orElse("");
            //output为空则直接返回null
            if (output == null || output.isEmpty()) {
                return null;
            }
            //执行分子分母的矩阵运算,如果分子分母都有汇总项，则根据汇总项做外关联计算，同时将添加output列作为最终的输出项为分子输出项/分母输出项
            //如果没有汇总项，则为汇总项，则直接做笛卡尔积计算
            List<RowData> resultDataList = performMatrixCalculation(numeratorResult, denominatorResult);

            //构造输出列列表，包含分子分母的所有列以及比率输出列
            List<DataCol> outputColList = new ArrayList<>();
            
            //添加分子的列
            outputColList.addAll(numeratorResult.getColList());
            
            //添加分母的列（避免重复）
            for (DataCol denominatorCol : denominatorResult.getColList()) {
                boolean exists = outputColList.stream()
                    .anyMatch(col -> col.getName().equals(denominatorCol.getName()));
                if (!exists) {
                    outputColList.add(denominatorCol);
                }
            }
            
            //添加比率输出列
            outputColList.add(new DataCol(output, "DECIMAL", output));
            
            //构造最终的分组键（优先使用分子的分组键）
            Map<String, String> finalGroupKey = numeratorResult.getGroupKey() != null ?
                numeratorResult.getGroupKey() : denominatorResult.getGroupKey();
            
            return new IndexInstanceResult(outputColList, resultDataList, indexInstance.getInstanceId(),
                    indexInstance.getIndexId(), finalGroupKey);
        }
    }

    /**
     * 单只汇总,按照汇总列进行分组汇总
     * @param metricResult
     * @return
     */
    private MetricResult singleGroup(MetricResult metricResult) {
        if (metricResult == null || metricResult.getDataList() == null || metricResult.getDataList().isEmpty()) {
            return metricResult;
        }

        Map<String, String> groupKey = metricResult.getGroupKey();
        if (groupKey == null || groupKey.isEmpty()) {
            return metricResult;
        }

        // 使用Stream按照groupKey进行分组
        Map<String, List<RowData>> groupedData = metricResult.getDataList().stream()
            .collect(Collectors.groupingBy(rowData ->
                groupKey.keySet().stream()
                    .map(groupColName ->
                        rowData.getCellList().stream()
                            .filter(cell -> groupColName.equals(cell.getColName()))
                            .findFirst()
                            .map(DataCell::getValue)
                            .orElse("")
                    )
                    .collect(Collectors.joining("|"))
            ));

        // 使用Stream和AtomicInteger生成汇总结果
        AtomicInteger resultIdCounter = new AtomicInteger(0);
        
        List<RowData> aggregatedDataList = groupedData.values().stream()
            .map(groupRows -> {
                int resultId = resultIdCounter.getAndIncrement();
                
                // 对每一列进行汇总
                List<DataCell> aggregatedCells = metricResult.getColList().stream()
                    .map(col -> aggregateColumn(col, groupRows, groupKey, resultId))
                    .collect(Collectors.toList());
                
                return new RowData(aggregatedCells, resultId);
            })
            .collect(Collectors.toList());

        return new MetricResult(metricResult.getColList(), aggregatedDataList,
                               metricResult.getMetricId(), metricResult.getGroupKey());
    }
    
    /**
     * 汇总单个列的数据
     */
    private DataCell aggregateColumn(DataCol col, List<RowData> groupRows, Map<String, String> groupKey, int resultId) {
        String colName = col.getName();
        String colType = col.getType();

        if (groupKey.containsKey(colName)) {
            // 分组字段，取第一个值
            String firstValue = groupRows.stream()
                    .flatMap(row -> row.getCellList().stream())
                    .filter(cell -> colName.equals(cell.getColName()))
                    .findFirst()
                    .map(DataCell::getValue)
                    .orElse("");

            return new DataCell(String.valueOf(resultId), colName, firstValue);
        } else if ("DECIMAL".equals(colType) || "INTEGER".equals(colType)) {
            // 数值类型，使用BigDecimal求和确保精度
            java.math.BigDecimal sum = groupRows.stream()
                    .flatMap(row -> row.getCellList().stream())
                    .filter(cell -> colName.equals(cell.getColName()))
                    .filter(cell -> cell.getValue() != null && !cell.getValue().trim().isEmpty())
                    .map(cell -> {
                        try {
                            return new java.math.BigDecimal(cell.getValue());
                        } catch (NumberFormatException e) {
                            return java.math.BigDecimal.ZERO;
                        }
                    })
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

            return new DataCell(String.valueOf(resultId), colName, sum.toPlainString());
        } else {
            // 非数值类型，取第一条记录
            String firstValue = groupRows.stream()
                    .flatMap(row -> row.getCellList().stream())
                    .filter(cell -> colName.equals(cell.getColName()))
                    .findFirst()
                    .map(cell -> cell.getValue() != null ? cell.getValue() : "")
                    .orElse("");

            return new DataCell(String.valueOf(resultId), colName, firstValue);
        }
    }
    
    /**
     * 全部汇总方法，将所有数据汇总为一行
     * @param metricResult
     * @return
     */
    private MetricResult group(MetricResult metricResult) {
        if (metricResult == null || metricResult.getDataList() == null || metricResult.getDataList().isEmpty()) {
            return metricResult;
        }
        
        // 对每一列进行汇总
        List<DataCell> aggregatedCells = metricResult.getColList().stream()
            .map(col -> aggregateAllColumn(col, metricResult.getDataList()))
            .collect(Collectors.toList());
        
        // 创建单行汇总结果
        List<RowData> aggregatedDataList = Arrays.asList(new RowData(aggregatedCells, 0));
        
        return new MetricResult(metricResult.getColList(), aggregatedDataList,
                               metricResult.getMetricId(), metricResult.getGroupKey());
    }
    
    /**
     * 汇总单个列的所有数据（全部汇总）
     */
    private DataCell aggregateAllColumn(DataCol col, List<RowData> allRows) {
        String colName = col.getName();
        String colType = col.getType();
        
        if ("DECIMAL".equals(colType) || "INTEGER".equals(colType)) {
            // 数值类型，使用BigDecimal求和确保精度
            BigDecimal sum = allRows.stream()
                .flatMap(row -> row.getCellList().stream())
                .filter(cell -> colName.equals(cell.getColName()))
                .filter(cell -> cell.getValue() != null && !cell.getValue().trim().isEmpty())
                .map(cell -> {
                    try {
                        return new BigDecimal(cell.getValue());
                    } catch (NumberFormatException e) {
                        return BigDecimal.ZERO;
                    }
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            return new DataCell("0", colName, sum.stripTrailingZeros().toPlainString());
        } else {
            // 非数值类型，取第一条记录
            String firstValue = allRows.stream()
                .flatMap(row -> row.getCellList().stream())
                .filter(cell -> colName.equals(cell.getColName()))
                .findFirst()
                .map(cell -> cell.getValue() != null ? cell.getValue() : "")
                .orElse("");
            
            return new DataCell("0", colName, firstValue);
        }
    }
    
    /**
     * 执行分子分母的矩阵运算
     * 根据分子分母的汇总方式进行不同的关联计算
     * @param numeratorResult 分子结果
     * @param denominatorResult 分母结果
     * @return 运算结果
     */
    private List<RowData> performMatrixCalculation(MetricResult numeratorResult, MetricResult denominatorResult) {
        List<RowData> resultList = new ArrayList<>();
        AtomicInteger resultIdCounter = new AtomicInteger(0);
        
        // 判断分子分母的汇总类型
        boolean numeratorIsGroup = numeratorRs.getGroupKey() == null || numeratorRs.getGroupKey().isEmpty();
        boolean denominatorIsGroup = denominatorRs.getGroupKey() == null || denominatorRs.getGroupKey().isEmpty();
        
        if (denominatorIsGroup) {
            // 分母为汇总：列表增加分母的汇总项和输出列，输出列为分子/分母，行数与分子项行数保持一致
            RowData denominatorRow = denominatorResult.getDataList().get(0); // 汇总结果只有一行
            
            for (RowData numeratorRow : numeratorResult.getDataList()) {
                List<DataCell> combinedCells = new ArrayList<>();
                int resultId = resultIdCounter.getAndIncrement();
                String resultIdStr = String.valueOf(resultId);
                
                // 添加分子项的值
                for (DataCell cell : numeratorRow.getCellList()) {
                    combinedCells.add(new DataCell(resultIdStr, cell.getColName(), cell.getValue()));
                }
                
                // 添加分母项的值
                for (DataCell cell : denominatorRow.getCellList()) {
                    // 避免重复添加相同列名的数据
                    boolean exists = combinedCells.stream()
                        .anyMatch(existingCell -> existingCell.getColName().equals(cell.getColName()));
                    if (!exists) {
                        combinedCells.add(new DataCell(resultIdStr, cell.getColName(), cell.getValue()));
                    }
                }
                
                // 计算比率
                String ratioValue = calculateRatioValue(numeratorRow, denominatorRow);
                String output = indexDataItems.stream().filter(data -> "F".equals(data.getDataSourceType())).map(MetricDataItem::getDataItemField)
                        .findFirst().orElse("ratio");
                combinedCells.add(new DataCell(resultIdStr, output, ratioValue));
                
                resultList.add(new RowData(combinedCells, resultId));
            }
        } else {
            // 分母维度汇总或单只：根据分子和分母的汇总列进行外关联
            resultList = performOuterJoin(numeratorResult, denominatorResult, resultIdCounter);
        }
        
        return resultList;
    }
    
    /**
     * 执行外关联操作
     * @param numeratorResult 分子结果
     * @param denominatorResult 分母结果
     * @param resultIdCounter 结果ID计数器
     * @return 关联结果
     */
    private List<RowData> performOuterJoin(MetricResult numeratorResult, MetricResult denominatorResult,
                                         AtomicInteger resultIdCounter) {
        List<RowData> resultList = new ArrayList<>();
        Map<String, String> groupKey = numeratorResult.getGroupKey() != null ?
            numeratorResult.getGroupKey() : denominatorResult.getGroupKey();
        
        if (groupKey == null || groupKey.isEmpty()) {
            // 如果没有汇总列，直接做笛卡尔积
            for (RowData numeratorRow : numeratorResult.getDataList()) {
                for (RowData denominatorRow : denominatorResult.getDataList()) {
                    resultList.add(createJoinedRow(numeratorRow, denominatorRow, resultIdCounter.getAndIncrement()));
                }
            }
        } else {
            // 按照汇总列进行外关联
            Map<String, RowData> numeratorMap = buildGroupKeyMap(numeratorResult.getDataList(), groupKey);
            Map<String, RowData> denominatorMap = buildGroupKeyMap(denominatorResult.getDataList(), groupKey);
            
            // 获取所有的组合键
            Set<String> allKeys = new HashSet<>();
            allKeys.addAll(numeratorMap.keySet());
            allKeys.addAll(denominatorMap.keySet());
            
            // 对每个组合键进行关联
            for (String key : allKeys) {
                RowData numeratorRow = numeratorMap.get(key);
                RowData denominatorRow = denominatorMap.get(key);
                
                // 如果某一方为空，用0补充
                if (numeratorRow == null) {
                    numeratorRow = createEmptyRow(numeratorResult.getColList(), key, groupKey);
                }
                if (denominatorRow == null) {
                    denominatorRow = createEmptyRow(denominatorResult.getColList(), key, groupKey);
                }
                
                resultList.add(createJoinedRow(numeratorRow, denominatorRow, resultIdCounter.getAndIncrement()));
            }
        }
        
        return resultList;
    }
    
    /**
     * 根据汇总列构建分组键映射
     */
    private Map<String, RowData> buildGroupKeyMap(List<RowData> dataList, Map<String, String> groupKey) {
        return dataList.stream()
            .collect(Collectors.toMap(
                rowData -> groupKey.keySet().stream()
                    .map(groupColName ->
                        rowData.getCellList().stream()
                            .filter(cell -> groupColName.equals(cell.getColName()))
                            .findFirst()
                            .map(DataCell::getValue)
                            .orElse("")
                    )
                    .collect(Collectors.joining("|")),
                rowData -> rowData,
                (existing, replacement) -> existing // 如果有重复key，保留第一个
            ));
    }
    
    /**
     * 创建空行数据用于补充
     */
    private RowData createEmptyRow(List<DataCol> colList, String groupKeyValue, Map<String, String> groupKey) {
        List<DataCell> cells = new ArrayList<>();
        String[] keyValues = groupKeyValue.split("\\|");
        AtomicInteger keyIndex = new AtomicInteger(0);
        
        for (DataCol col : colList) {
            String value = "";
            if (groupKey.containsKey(col.getName()) && keyIndex.get() < keyValues.length) {
                value = keyValues[keyIndex.getAndIncrement()];
            } else if ("DECIMAL".equals(col.getType()) || "INTEGER".equals(col.getType())) {
                value = "0";
            }
            cells.add(new DataCell("0", col.getName(), value));
        }
        
        return new RowData(cells, 0);
    }
    
    /**
     * 创建关联后的行数据
     */
    private RowData createJoinedRow(RowData numeratorRow, RowData denominatorRow, int resultId) {
        List<DataCell> combinedCells = new ArrayList<>();
        String resultIdStr = String.valueOf(resultId);
        
        // 添加分子项的值
        for (DataCell cell : numeratorRow.getCellList()) {
            combinedCells.add(new DataCell(resultIdStr, cell.getColName(), cell.getValue()));
        }
        
        // 添加分母项的值（避免重复）
        for (DataCell cell : denominatorRow.getCellList()) {
            boolean exists = combinedCells.stream()
                .anyMatch(existingCell -> existingCell.getColName().equals(cell.getColName()));
            if (!exists) {
                combinedCells.add(new DataCell(resultIdStr, cell.getColName(), cell.getValue()));
            }
        }
        
        // 计算比率
        String ratioValue = calculateRatioValue(numeratorRow, denominatorRow);
        String output = indexDataItems.stream().filter(data -> "F".equals(data.getDataSourceType())).map(MetricDataItem::getDataItemField)
                .findFirst().orElse("ratio");
        combinedCells.add(new DataCell(resultIdStr, output, ratioValue));
        
        return new RowData(combinedCells, resultId);
    }
    
    /**
     * 计算比率值（分子/分母）
     * 使用BigDecimal进行精确计算，避免浮点数精度问题
     */
    private String calculateRatioValue(RowData numeratorRow, RowData denominatorRow) {
        try {
            // 获取第一个数值类型的值
            String numeratorValue = getFirstNumericValue(numeratorRow);
            String denominatorValue = getFirstNumericValue(denominatorRow);
            
            if (numeratorValue.isEmpty() || denominatorValue.isEmpty()) {
                return "";
            }
            
            BigDecimal numerator = new BigDecimal(numeratorValue);
            BigDecimal denominator = new BigDecimal(denominatorValue);
            
            // 检查分母是否为零
            if (denominator.compareTo(BigDecimal.ZERO) == 0) {
                return ""; // 避免除零错误
            }
            
            // 使用BigDecimal进行除法运算，保留10位小数，使用四舍五入模式
            BigDecimal ratio = numerator.divide(denominator, 10, RoundingMode.HALF_UP);
            
            // 去除尾部不必要的零
            return ratio.stripTrailingZeros().toPlainString();
            
        } catch (NumberFormatException | ArithmeticException e) {
            return ""; // 如果转换失败或计算错误，返回空值
        }
    }
    
    /**
     * 获取行数据中第一个数值类型的值
     * 使用BigDecimal验证数值有效性
     */
    private String getFirstNumericValue(RowData rowData) {
        if (rowData == null || rowData.getCellList() == null) {
            return "";
        }
        
        return rowData.getCellList().stream()
            .map(DataCell::getValue)
            .filter(value -> value != null && !value.trim().isEmpty())
            .filter(value -> {
                try {
                    new BigDecimal(value);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            })
            .findFirst()
            .orElse("");
    }
}