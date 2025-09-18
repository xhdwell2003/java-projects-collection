<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.your.package.MonChildrenExprBackupMapper">

    <update id="backupOrUpdateFromSource">
        MERGE INTO TGCORE.MON_CHILDREN_EXPR_BACKUP target
                USING (
            SELECT *
            FROM TGCORE.MON_CHILDREN_EXPR
            <where>
                <if test="originId != null and originId != ''">
                    AND ORIGIN_ID = #{originId}
                </if>
            </where>
        ) source
        ON (
            target.SYSTEM_ID = source.SYSTEM_ID AND
            target.ORIGIN_ID = source.ORIGIN_ID AND
            target.ORIGIN_TYPE = source.ORIGIN_TYPE
        )
        WHEN MATCHED THEN
            UPDATE SET
                target.EXPR = source.EXPR,
                target.CREATE_USER = source.CREATE_USER,
                target.UPDATE_USER = source.UPDATE_USER,
                target.CREATE_TIME = source.CREATE_TIME,
                target.UPDATE_TIME = source.UPDATE_TIME
        WHEN NOT MATCHED THEN
            INSERT (
                SYSTEM_ID,
                ORIGIN_TYPE,
                ORIGIN_ID,
                EXPR,
                CREATE_USER,
                UPDATE_USER,
                CREATE_TIME,
                UPDATE_TIME
            ) VALUES (
                source.SYSTEM_ID,
                source.ORIGIN_TYPE,
                source.ORIGIN_ID,
                source.EXPR,
                source.CREATE_USER,
                source.UPDATE_USER,
                source.CREATE_TIME,
                source.UPDATE_TIME
            )
    </update>

</mapper>
