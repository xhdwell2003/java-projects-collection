import lombok.Getter;
import lombok.experimental.SuperBuilder;
import java.io.Serializable;
import java.util.regex.Pattern;
import java.util.List;
import java.util.Arrays;
import java.util.Date;

@Getter
@SuperBuilder
public class MetricDataItem implements Serializable {
    public static final Pattern PATTERN = Pattern.compile("(.*?)(HIGHER|LOWER)(.*)");
    private static final List<String> SAME_METRIC_ITEMS = Arrays.asList("持仓数量占养老金季度规模(数量)", "'持仓数量占总发行量", "持仓数量占存量规模",
           "持仓数量占基金总发行量", "持仓数量占总股本", "持仓数量占流通股本", "持仓数量占发行数量", "持仓数量占发行规模", "持仓市值占发行人公司上一年度净资产");
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
     * 转译后的名称
     */
    String commonName;
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
}