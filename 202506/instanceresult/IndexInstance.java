@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class IndexInstance {
    /**
     * 指标实例代码
     */
    private String instanceId;
    /**
     * 指标代码
     */
    private String indexId;
    /**
     * 系统号
     */
    private String systemId;
    /**
     * 监控对象
     */
    private String monObject;
    /**
     * 监控对象类型
     */
    private String monObjectType;
    /**
     * 监控级别
     */
    private String monLevel;
    /**
     * 生效日期
     */
    private LocalDate startDate;
    /**
     * 失效日期
     */
    private LocalDate endDate;
    /**
     * 限制条件
     */
    private LimitCondition limitCondition;

    private Threshold threshold;
    /**
     * 启用状态ENABLE
     */
    private String useState;
    /**
     * 成功次数
     */
    private Integer successNum;

    private Integer preSuccessNum;

    /**
     * 阈值参数
     */
    private String parameter;
    /**
     * 输出列
     */
    private String outputColumn;

    /**
     * 是否启用穿透
     */
    private String penetrate;

    /**
     * 是否支持事前
     */
    private String preMon;

    /**
     * 经办用户名
     */
    private String optUser;

    /**
     * 复核用户名
     */
    private String chkUser;

    /**
     * 产品集合
     */
    private Integer prdUnion;

    /**
     * 多产品联合绑定组批次号
     * 时间戳转成32进制字符串
     */
    private String groupId;


    /**
     * 组合指标实例id
     */
    private String unionInstanceId;

    /**
     * 组合实例类别
     */
    private String unionType;
    /**
     * 事中复核用户名
     */
    private String preChkUser;

    private String state;

    /**
     * 根据核心指标ID，更新非一级系统指标ID
     */
    public void updateFyjIndexId() {
        this.indexId = ExpressionGrouper.convertToFyjIndexId(indexId);
    }
    public void insertInstanceId(String id) {
        instanceId = id;
    }

    public void defaultState() {
        state = CommonEnum.STATE_DEFAULT.getFlag();
    }
    public void enableUseState() {
        useState = MonConstants.USE_STATE_ENABLE;
    }
    public void defaultSystemId() {
        systemId = MonConstants.SYSTEM_CORE;
    }

    public void isPreMon() {
        preMon = "Y";
    }

    public void noPreMon() {
        preMon = "N";
    }

    public void updateIndexId(final String indexId) {
        this.indexId = indexId;
    }
}
