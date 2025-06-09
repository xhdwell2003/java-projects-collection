public class exp4 {
    public List<BondGlfEntity> getHoldBonds(Date busDate, Date chDate, String prdCode, String subjectPrdType, String prdType)
            throws MonitorBussException, MonitorSystException {
        StringBuilder sbSql = new StringBuilder();
        Object[] params;
        if (ZbConstants.CPLX_BX.equals(prdType) || ZbConstants.CPLX_QD.equals(prdType)) {
            sbSql.append("with gz as ( ");
            sbSql.append("select BIZDATE GZDATE, PRDCODE GZCPDM, KMMKET GZMKET, SECCODE GZZQDM, KMZQLX, SUM(STADCOST) GZCBZE, SUM(STADVAL) GZSZZE ");
            sbSql.append("from tgcore.evaluation,tgcore.monkm ");
            sbSql.append("where BIZDATE=? and PRDCODE=? and kmcplx=? and coacode=kmcode ");
            sbSql.append("AND KMZQLX in ('ZQ','ZCZQ') and kmtype in ('CB','YZ','JZZB01','JZZB02','JZZB03','LX','YJLX') ");
            sbSql.append("AND ISFINAL='Y' and KMZQLS <> 'FZ' and KMKMBZ = 'CNY' and KMBZXX <> 'OVERSEA' ");
            sbSql.append("GROUP BY BIZDATE, PRDCODE, KMMKET, SECCODE, KMZQLX), ");
            sbSql.append("ZqResult AS ( ");
            sbSql.append("select gz.*, NVL(ZQNAME,' ') AS ZQNAME, ");
            // 如果是资产债券则取原始权益人
            sbSql.append("NVL(case KMZQLX when 'ZCZQ' then ZQYSQY else ZQFXRN end, ' ') AS ZQFXRN ");
            sbSql.append("FROM gz ");
            sbSql.append("LEFT JOIN TGCORE.MONZQ ON zqdate = ? and zqmket = GZMKET and zqcode = GZZQDM  ");
            sbSql.append("and zqstat = 'A' and ZQSYFW='P' ");
            sbSql.append(") ");
            sbSql.append("SELECT * FROM ZqResult ");

        } else {
            sbSql.append("WITH cczq as( ");
            sbSql.append("select GZDATE, GZCPDM, KMMKET AS GZMKET, GZZQDM, KMZQLX, SUM(GZCBZE) AS GZCBZE, SUM(GZSZZE) AS GZSZZE ");
            sbSql.append("from TGCORE.EVLGZ,TGCORE.MONKM ");
            sbSql.append("where GZDATE=? and GZCPDM=? and KMCPLX=? and KMCODE=GZKMDM ");
            sbSql.append("and KMZQLX in ('ZQ','ZCZQ') and KMTYPE in ('CB','YZ','JZZB01','JZZB02','JZZB03','LX','YJLX') ");
            sbSql.append("and ISFINAL='Y' and KMZQLS <> 'FZ' ");
            sbSql.append("GROUP BY GZDATE, GZCPDM, KMMKET, GZZQDM, KMZQLX) ");
            sbSql.append("select cczq.*, NVL(ZQNAME,' ') ZQNAME, ");
            // 如果是资产债券则取原始权益人
            sbSql.append("NVL(case KMZQLX when 'ZCZQ' then ZQYSQY else ZQFXRN end,' ') ZQFXRN ");
            sbSql.append("FROM cczq ");
            sbSql.append("LEFT JOIN TGCORE.MONZQ ON zqdate = ? and zqmket = GZMKET and zqcode = GZZQDM ");
            sbSql.append("and zqstat = 'A' and ZQSYFW='P' ");
        }
        params = new Object[]{busDate, prdCode, subjectPrdType, chDate};

        JdbcTemplate jdbc = JdbcTemplate.getJdbcTemplate();
        try {
            return jdbc.queryForList(sbSql.toString(), params, new RowMapper<BondGlfEntity>() {
                @Override
                public BondGlfEntity mapRow(ResultSet rs, int rowNum) throws Exception {
               BondGlfEntity res = new BondGlfEntity();
                    // 该属性临时存放标的类型
                    res.setZqKey("债券");
                    res.setGzDate(rs.getDate("GZDATE"));
                    res.setGzCpdm(rs.getString("GZCPDM"));
                    res.setGzMket(rs.getString("GZMKET"));
                    res.setGzZqdm(rs.getString("GZZQDM"));
                    res.setGzCbze(rs.getBigDecimal("GZCBZE"));
                    res.setGzSzze(rs.getBigDecimal("GZSZZE"));
                    res.setZqName(rs.getString("ZQNAME"));
                    res.setZqFxrn(rs.getString("ZQFXRN"));
                    return res;
                }
            });
        } catch (Exception e) {
            throw new MonitorSystException("获取持仓债券失败", e);
        }
    }

    /**
     * 清算款sql-非保险
     * @param sql
     * @param prdCode
     * @param subjectType
     */
    private void liquidationSQL(final StringBuilder sql, final String prdCode, final String subjectType) {
        sql.append(" select GZSZZE as JE,TRUNC(TO_NUMBER(1)) as SYQX,'EVLGZ' as SJLY,KMZQLX as KMZQLX,GZZQDM as SECCODE from TGCORE.EVLGZ,TGCORE.MONKM ");
        sql.append(" where GZCPDM= '").append(prdCode).append("' and GZDATE=? and KMCODE=GZZHDM ");
        sql.append(" and KMCPLX='").append(subjectType).append("' ");
        sql.append(" and KMZCLEVL='2' and KMZQLX= 'QSK' and KMTYPE in (' ','CB') ");
    }

     public List<Map<String, String>> getSumGroupByManager(Date evlDate, String prdCode, String sumType) throws Exception {

        //先查询当前产品计划下的所有产品，再从EVLGZ中查询市值总额和成本总额，并根据管理人分组后就资产净值的和
        String sql =
                " WITH cp AS (SELECT CPCODE,CPMNAM  FROM TGCORE.MONPR m WHERE CPSCLS <> 'STH' AND CPJHDM IN (SELECT CPJHDM FROM TGCORE.MONPR  WHERE CPCODE  = ? )) , "
                        +
                        " gzcp AS (SELECT e.*,cp.CPMNAM FROM TGCORE.EVLGZ e INNER JOIN cp ON e.GZCPDM = cp.CPCODE  WHERE GZZHDM='99990004' AND e.GZDATE = ? )  "
                        +
                        " SELECT CPMNAM,sum(GZSZZE) AS GZSZZE ,sum(GZCBZE) AS GZCBZE FROM gzcp GROUP BY CPMNAM ";
        if ("SZZE".equals(sumType)) {
            sql = sql + "ORDER BY GZSZZE desc";
        }
        if ("CBZE".equals(sumType)) {
            sql = sql + "ORDER BY GZCBZE desc";
        }

        JdbcTemplate jdbcTemplate = JdbcTemplate.getJdbcTemplate();
        Object[] args = {prdCode, evlDate};

        return jdbcTemplate.queryForList(sql, args, (rs, rowIndex) -> {

            Map<String, String> map = new HashMap<>();
            map.put("CPMNAM", rs.getString("CPMNAM").trim());
            map.put("GZSZZE", rs.getString("GZSZZE").trim());
            map.put("GZCBZE", rs.getString("GZCBZE").trim());
            return map;
        });
    }
}
