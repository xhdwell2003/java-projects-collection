public class Element {
    private String result;
    private String left;
    private String middle;
    private String right;

    public Element() {
        this.result = "";
        this.left = "";
        this.middle = "";
        this.right = "";
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getLeft() {
        return left;
    }

    public void setLeft(String left) {
        this.left = left;
    }

    public String getMiddle() {
        return middle;
    }

    public void setMiddle(String middle) {
        this.middle = middle;
    }

    public String getRight() {
        return right;
    }

    public void setRight(String right) {
        this.right = right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Element roiEntry = (Element) o;
        return Objects.equals(result, roiEntry.result) &&
                Objects.equals(left, roiEntry.left) &&
                Objects.equals(middle, roiEntry.middle) &&
                Objects.equals(right, roiEntry.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(result, left, middle, right);
    }
}

@Data
public class MonChildrenExpr {
    private String systemId;
    private String originType;
    private String originId;
    private String expr;
    private String createUser;
    private String updateUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public MonChildrenExpr(String originId, String expr) {
        systemId = "CORE";
        originType = "M";
        this.originId = originId;
        this.expr = expr;
    }
    public MonChildrenExpr(String originType, String originId, String expr) {
        systemId = "CORE";
        this.originType = originType;
        this.originId = originId;
        this.expr = expr;
    }

}

  //多个筛选条件
        FormulaParserImpl formulaParser = SpringContextUtil.getBean(FormulaParserImpl.class);
        Map<String, String> filterMetricMap = new HashMap<>();
        filterMetricMap = metricCondition.stream().collect(Collectors.toMap(FilterCondition::getFilterId, FilterCondition::getSourceId, (o, n) -> n));
        formulaParser.setFilterMetricMap(filterMetricMap);
        FormulaVisitorImpl formulaVisitor = formulaParser.obtainFormulaResult(req.getExp());
        List<Element> elementList = formulaVisitor.getElementList();
        Map<String, String> keyFilterMap = formulaVisitor.getKeyFilterMap();

        List <MonChildrenExpr> monChildrenExprs = new ArrayList<>();

        根据以上代码，参考以下的案例。将数据保存到monChildrenExprs中。key为originId，value为expr

        多步骤例子:
         keyFilterMap:
         "NEWMETRIC4" -> "filterqzzYv"
         "NEWMETRIC1" -> " ( filterGYQLc and filterJKPyx ) "
         "NEWMETRIC2" -> " ( filtersLXTg or filterbFmOT ) "

         elementList:
         0 = {Element@21471}
          result = "Step1"
          left = "NEWMETRIC1"
          middle = "or"
          right = "NEWMETRIC2"
         1 = {Element@21472}
          result = "Step2"
          left = "Step1"
          middle = "or"
          right = "NEWMETRIC4"

         filterMetricMap:
         " ( filtersLXTg or filterbFmOT ) " -> "METRIC00000002"
         "NEWMETRIC1 or NEWMETRIC2" -> "NEWMETRIC3"
         "filterqzzYv" -> "METRIC00000309"
         "NEWMETRIC1 or NEWMETRIC2 or NEWMETRIC4" -> "NEWMETRIC5"
         "filtersLXTg" -> "METRIC00000002"
         "filterGYQLc" -> "METRIC00000003"
         "filterbFmOT" -> "METRIC00000002"
         " ( filterGYQLc and filterJKPyx ) " -> "METRIC00000003"
         "filterJKPyx" -> "METRIC00000003"

         根据以上分析内容构建度量树MON_CHILDREN_EXPR
         Step1:
         1. METRICSYS1199307179  METRIC00000003
         2. METRICSYS1199307180  METRIC00000002
         3. METRICSYS1199307181  METRICSYS1199307179 or METRICSYS1199307180
         Step2:
         1. METRICSYS1199307177  METRIC00000309
         2. METRICSYS1199307182  METRICSYS1199307181 or METRICSYS1199307177
        
        最后MON_CHILDREN_EXPR应该有5条记录
        1. METRICSYS1199307179  METRIC00000003
         2. METRICSYS1199307180  METRIC00000002
         3. METRICSYS1199307181  METRICSYS1199307179 or METRICSYS1199307180
         4. METRICSYS1199307177  METRIC00000309
         5. METRICSYS1199307182  METRICSYS1199307181 or METRICSYS1199307177
    