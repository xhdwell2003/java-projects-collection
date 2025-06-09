package com.example.dao;

import com.example.entity.MonChildrenExpr;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 多层树表达式数据访问对象
 */
@Mapper
public interface MonChildrenExprDao {

    /**
     * 保存多层树表达式
     *
     * @param monChildrenExpr 多层树表达式
     * @return 影响行数
     */
    @Insert("INSERT INTO MON_CHILDREN_EXPR(SYSTEM_ID, ORIGIN_TYPE, ORIGIN_ID, EXPR, CREATE_USER, UPDATE_USER, CREATE_TIME, UPDATE_TIME) " +
            "VALUES(#{systemId}, #{originType}, #{originId}, #{expr}, #{createUser}, #{updateUser}, SYSDATE, SYSDATE)")
    int insert(MonChildrenExpr monChildrenExpr);

    /**
     * 批量保存多层树表达式
     *
     * @param monChildrenExprs 多层树表达式列表
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<MonChildrenExpr> monChildrenExprs);

    /**
     * 根据源ID查询多层树表达式
     *
     * @param originId 源ID
     * @return 多层树表达式
     */
    @Select("SELECT SYSTEM_ID as systemId, ORIGIN_TYPE as originType, ORIGIN_ID as originId, EXPR as expr, " +
            "CREATE_USER as createUser, UPDATE_USER as updateUser, CREATE_TIME as createTime, UPDATE_TIME as updateTime " +
            "FROM MON_CHILDREN_EXPR WHERE ORIGIN_ID = #{originId}")
    MonChildrenExpr findByOriginId(@Param("originId") String originId);

    /**
     * 根据源类型和源ID查询多层树表达式
     *
     * @param originType 源类型
     * @param originId 源ID
     * @return 多层树表达式
     */
    @Select("SELECT SYSTEM_ID as systemId, ORIGIN_TYPE as originType, ORIGIN_ID as originId, EXPR as expr, " +
            "CREATE_USER as createUser, UPDATE_USER as updateUser, CREATE_TIME as createTime, UPDATE_TIME as updateTime " +
            "FROM MON_CHILDREN_EXPR WHERE ORIGIN_TYPE = #{originType} AND ORIGIN_ID = #{originId}")
    MonChildrenExpr findByOriginTypeAndOriginId(@Param("originType") String originType, @Param("originId") String originId);
} 