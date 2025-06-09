import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.io.Serializable;

/**
 * 数据项
 */
public class DataItem implements Serializable {
    /**
     * 数据项ID
     */
    String dataItemId;
    /**
     * 数据项名称
     */
    String dataItemName;
    /**
     * 系统号
     */
    String systemId;
    /**
     * 指标ID/度量ID
     */
    String originId;
    /**
     * 所属指标/所属度量
     */
    String originType;
    /**
     * 数据项对应字段
     */
    String dataItemField;
    /**
     * 主维度标记
     */
    String monObjectMark;
    /**
     * 从维度标记
     */
    String subDimensionMark;
    /**
     * 数据来源类型
     */
    String dataSourceType;
    /**
     * 数据来源
     */
    String dataSource;
    /**
     * 数据中文描述
     */
    String dataSourceDescription;
    /**
     * 是否作为输出列
     */
    String outColumMark;
    /**
     * 数据类型
     */
    String dataType;
    /**
     * 审核状态
     */
    String state;
    /**
     * 创建用户
     */
    String createUser;
    /**
     * 更新用户
     */
    String updateUser;
    /**
     * 经办用户
     */
    String optUser;
    /**
     * 审核用户
     */
    String chkUser;
    /**
     * 创建时间
     */
    Date createTime;
    /**
     * 更新时间
     */
    Date updateTime;

    // 为了方便复制对象，添加一个复制构造函数
    public DataItem copy() {
        DataItem copy = new DataItem();
        copy.dataItemId = this.dataItemId;
        copy.dataItemName = this.dataItemName;
        copy.systemId = this.systemId;
        copy.originId = this.originId;
        copy.originType = this.originType;
        copy.dataItemField = this.dataItemField;
        copy.monObjectMark = this.monObjectMark;
        copy.subDimensionMark = this.subDimensionMark;
        copy.dataSourceType = this.dataSourceType;
        copy.dataSource = this.dataSource;
        copy.dataSourceDescription = this.dataSourceDescription;
        copy.outColumMark = this.outColumMark;
        copy.dataType = this.dataType;
        copy.state = this.state;
        copy.createUser = this.createUser;
        copy.updateUser = this.updateUser;
        copy.optUser = this.optUser;
        copy.chkUser = this.chkUser;
        copy.createTime = this.createTime;
        copy.updateTime = this.updateTime;
        return copy;
    }
}

public class DataItemMerger {

    /**
     * 合并两个DataItem列表，如果dataItemField相同，第二个列表中的项会在字段后加1
     * @param rightDataItem 第一个数据项列表
     * @param leftDataItem 第二个数据项列表
     * @return 合并后的数据项列表
     */
    public static List<DataItem> mergeDataItems(List<DataItem> rightDataItem, List<DataItem> leftDataItem) {
        List<DataItem> resultDataItem = new ArrayList<>();
        Set<String> existingFields = new HashSet<>();
        
        // 先添加右侧数据项，并记录所有字段名
        for (DataItem item : rightDataItem) {
            resultDataItem.add(item);
            existingFields.add(item.dataItemField);
        }
        
        // 添加左侧数据项，处理字段名冲突
        for (DataItem item : leftDataItem) {
            DataItem newItem = item.copy();
            String originalField = newItem.dataItemField;
            
            // 如果字段名已存在，则在字段后加1
            if (existingFields.contains(originalField)) {
                newItem.dataItemField = originalField + "1";
            }
            
            resultDataItem.add(newItem);
            existingFields.add(newItem.dataItemField);
        }
        
        return resultDataItem;
    }
    
    public static void main(String[] args) {
        // 示例用法
        List<DataItem> rightDataItem = rightDataCombination.getDataItemList(); 
        List<DataItem> leftDataItem = leftDataCombination.getDataItemList();
        List<DataItem> resultDataItem = mergeDataItems(rightDataItem, leftDataItem);
        
        // 现在resultDataItem包含了rightDataItem和leftDataItem的并集
        // 如果有重复的dataItemField，leftDataItem中的条目会在字段名后加1
    }
}