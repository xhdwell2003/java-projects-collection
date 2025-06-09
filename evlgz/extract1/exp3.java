public class exp3 {
        public List<Map<String, Object>> getCurrentDepositAndPosition(final String prdCode,
                                                   final Date evlDate,
                                                   final String subjectType) throws SQLException {
        String sql = "select GZSZZE as AMOUNT, (case KMZQLX when 'BFJ' then '备付金' else '活期存款' end) as SECTYPE, GZZHDM as SECCODE, 0 as SYQX "
                + "from tgcore.EVLGZ, tgcore.MONKM where GZCPDM=? and GZDATE=?  and  GZZHDM=KMCODE and KMCPLX=? and "
                + "((KMZQLX='CK' and KMZQLS='HQ' and ISFINAL='Y') or (KMZQLX='BFJ' and KMLEVL='1'))";
        Object[] args = {prdCode, evlDate, subjectType};
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        return jdbcTemplate.queryForList(sql, args);
    }
}
