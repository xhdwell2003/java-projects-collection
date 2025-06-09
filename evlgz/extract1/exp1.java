public class exp1 {


    public List<SecurityBaseEntity> getForbiddenSpvHolding(String prdCode, Date monDate, String subjectType, String forbiddenType)
            throws SQLException {
        StringBuilder sql = new StringBuilder(128);
        sql.append("with gz as (select GZZQDM, KMMKET from TGCORE.EVLGZ, TGCORE.MONKM where GZCPDM=? and GZDATE=? and KMCPLX=? and GZKMDM=KMCODE "
                + "and KMTYPE='CB' and ISFINAL='Y' and KMZQLX='SPV') ");
        sql.append("select distinct GZZQDM,KMMKET, nvl(ZQNAME,' ') as ZQNAME, nvl(ZQZQLB,' ') as ZQZQLB from gz "
                + "left join (select * from TGCORE.MONSPV "
                + "where (ZQCODE,ZQCPDM) in (select ZQCODE,max(ZQCPDM) ZQCPDM from TGCORE.MONSPV where ZQCPDM=? or ZQCPDM=' ' group by ZQCODE) )"
                + "on  ZQMKET =KMMKET and ZQCODE = GZZQDM where ZQZQLB in (" + forbiddenType + ") or trim(ZQZQLB) is null ");
        Object[] args = new Object[]{prdCode, monDate, subjectType, prdCode};

        JdbcTemplate jdbcTemplate = JdbcTemplate.getJdbcTemplate();
        return jdbcTemplate.queryForList(sql.toString(), args, new RowMapper<SecurityBaseEntity>() {
            @Override
            public SecurityBaseEntity mapRow(final ResultSet rs, final int rowNum) throws Exception {
                SecurityBaseEntity e = new SecurityBaseEntity();
                e.setSecCode(rs.getString("GZZQDM").trim());
                e.setSecType(rs.getString("ZQZQLB").trim());
                e.setSecMarket(rs.getString("KMMKET").trim());
                e.setSecName(rs.getString("ZQNAME").trim());
                return e;
            }
        });

    }

}