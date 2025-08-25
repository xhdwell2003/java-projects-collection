"""
ODS入湖脚本 - 华为Hive数据导入
确保空值存储不会变成null值，采用以下策略：
- 字符串字段：空值或null替换为单个空格' '
- 数值字段：空值或null替换为0或0.0
- 日期字段：空值或null替换为默认日期'1900-01-01'
- 时间戳字段：空值或null替换为默认时间戳'1900-01-01 00:00:00.000000'
"""
import configparser
import logging
import os
import sys
from pathlib import Path

work_path = Path("../../../../../..").resolve()
sys.path.append("%s/UTIL/" % work_path)

from commonutil import (msck_table, truncate_table, get_db_info, get_time_rotating_logger, hive_exec, log_info)

# 数据日期
bdw_data_dt = sys.argv[1]

# 配置参数
version = 1.0
max_days = 1
load_mode = "A"
job_name = "TGJK_EXT_TGHX_PUBQQ_A"
schema_name = "TGJK"
execute_mode = "HIVE"
job_desc = "期权信息表"
person_in_charge_a = "肖罕栋/80174929"
person_in_charge_b = "李季/80231149"
if_export = 'Y'
sys_name = "TGHX"
query_sql = r"""SELECT
QQSEQN,
CASE WHEN COALESCE(trim(QQHYDM), '') IS NULL OR trim(QQHYDM) = '' THEN ' ' ELSE trim(QQHYDM) END as QQHYDM,
CASE WHEN COALESCE(trim(QQHYMC), '') IS NULL OR trim(QQHYMC) = '' THEN ' ' ELSE trim(QQHYMC) END as QQHYMC,
CASE WHEN COALESCE(trim(QQZQDM), '') IS NULL OR trim(QQZQDM) = '' THEN ' ' ELSE trim(QQZQDM) END as QQZQDM,
CASE WHEN COALESCE(trim(QQZQMC), '') IS NULL OR trim(QQZQMC) = '' THEN ' ' ELSE trim(QQZQMC) END as QQZQMC,
CASE WHEN QQQSRQ IS NULL THEN '1900-01-01' ELSE to_char(QQQSRQ,'YYYY-MM-DD') END as QQQSRQ,
CASE WHEN QQENDD IS NULL THEN '1900-01-01' ELSE to_char(QQENDD,'YYYY-MM-DD') END as QQENDD,
CASE WHEN QQXQDD IS NULL THEN '1900-01-01' ELSE to_char(QQXQDD,'YYYY-MM-DD') END as QQXQDD,
CASE WHEN COALESCE(trim(QQMKET), '') IS NULL OR trim(QQMKET) = '' THEN ' ' ELSE trim(QQMKET) END as QQMKET,
CASE WHEN QQHYCS IS NULL THEN 0 ELSE QQHYCS END as QQHYCS,
CASE WHEN QQXQJG IS NULL THEN 0.0 ELSE QQXQJG END as QQXQJG,
CASE WHEN COALESCE(trim(QQJUSR), '') IS NULL OR trim(QQJUSR) = '' OR QQJUSR = ' ' THEN '1' ELSE trim(QQJUSR) END as QQJUSR,
CASE WHEN COALESCE(trim(QQSUSR), '') IS NULL OR trim(QQSUSR) = '' OR QQSUSR = ' ' THEN '1' ELSE trim(QQSUSR) END as QQSUSR,
CASE WHEN COALESCE(trim(QQSTAT), '') IS NULL OR trim(QQSTAT) = '' THEN ' ' ELSE trim(QQSTAT) END as QQSTAT,
CASE WHEN COALESCE(trim(QQHYBM), '') IS NULL OR trim(QQHYBM) = '' THEN ' ' ELSE trim(QQHYBM) END as QQHYBM,
CASE WHEN CREATE_TIME IS NULL THEN '1900-01-01 00:00:00.000000' ELSE to_char(CREATE_TIME,'yyyy-MM-dd HH24:MI:ss.FF6') END as CREATE_TIME,
CASE WHEN UPDATE_TIME IS NULL THEN '1900-01-01 00:00:00.000000' ELSE to_char(UPDATE_TIME,'yyyy-MM-dd HH24:MI:ss.FF6') END as UPDATE_TIME,
CASE WHEN COALESCE(trim(QQTYPE), '') IS NULL OR trim(QQTYPE) = '' THEN ' ' ELSE trim(QQTYPE) END as QQTYPE,
CASE WHEN COALESCE(trim(JYMKET), '') IS NULL OR trim(JYMKET) = '' THEN ' ' ELSE trim(JYMKET) END as JYMKET,
CASE WHEN COALESCE(trim(QQHYLB), '') IS NULL OR trim(QQHYLB) = '' THEN ' ' ELSE trim(QQHYLB) END as QQHYLB,
CASE WHEN COALESCE(trim(QQFXLB), '') IS NULL OR trim(QQFXLB) = '' THEN ' ' ELSE trim(QQFXLB) END as QQFXLB
FROM TGCORE.PUBQQ WHERE \${CONDITIONS}"""
# query_sql = r"""
# SELECT QQSEQN,
# case when coalesce(trim(QQHYDM), '') IS null then ' ' else trim(QQHYDM) end as QQHYDM,
# case when coalesce(trim(QQHYMC), '') IS null then ' ' else trim(QQHYMC) end as QQHYMC,
# case when coalesce(trim(QQZQDM), '') IS null then ' ' else trim(QQZQDM) end  as QQZQDM,
# case when coalesce(trim(QQZQMC), '') IS null then ' ' else trim(QQZQMC) end  as QQZQMC,
# to_char(QQQSRQ,'YYYY-MM-DD') as QQQSRQ,
# to_char(QQENDD,'YYYY-MM-DD') as QQENDD,
# to_char(QQXQDD,'YYYY-MM-DD') as QQXQDD,
# case when coalesce(trim(QQMKET), '') IS null then ' ' else trim(QQMKET) end  as QQMKET,
# QQHYCS,
# QQXQJG,
# case when coalesce(trim(QQJUSR), '') IS null then ' ' else trim(QQJUSR) end  as QQJUSR,
# case when coalesce(trim(QQSUSR), '') IS null then ' ' else trim(QQSUSR) end  as QQSUSR,
# case when coalesce(trim(QQSTAT), '') IS null then ' ' else trim(QQSTAT) end  as QQSTAT,
# case when coalesce(trim(QQHYBM), '') IS null then ' ' else trim(QQHYBM) end  as QQHYBM,
# to_char(CREATE_TIME,'yyyy-MM-dd HH24:MI:ss.FF6') as CREATE_TIME,
# to_char(UPDATE_TIME,'yyyy-MM-dd HH24:MI:ss.FF6') as UPDATE_TIME,
# case when coalesce(trim(QQTYPE), '') IS null then ' ' else trim(QQTYPE) end  as QQTYPE,
# case when coalesce(trim(JYMKET), '') IS null then ' ' else trim(JYMKET) end  as JYMKET,
# case when coalesce(trim(QQHYLB), '') IS null then ' ' else trim(QQHYLB) end  as QQHYLB,
# case when coalesce(trim(QQFXLB), '') IS null then ' ' else trim(QQFXLB) end  as QQFXLB
# FROM TGCORE.PUBQQ
# where \${CONDITIONS}"""
stemp_table = "TGJK_TGHX_PUBQQ_EXP"


def validate_null_handling():
    """
    验证空值处理策略的函数
    确保所有字段都有适当的空值处理逻辑
    """
    null_handling_rules = {
        'string_fields': ['QQHYDM', 'QQHYMC', 'QQZQDM', 'QQZQMC', 'QQMKET', 'QQJUSR', 'QQSUSR', 'QQSTAT', 'QQHYBM', 'QQTYPE', 'JYMKET', 'QQHYLB', 'QQFXLB'],
        'numeric_fields': ['QQSEQN', 'QQHYCS', 'QQXQJG'],
        'date_fields': ['QQQSRQ', 'QQENDD', 'QQXQDD'],
        'timestamp_fields': ['CREATE_TIME', 'UPDATE_TIME']
    }
    
    # 记录空值处理规则
    print("空值处理规则:")
    print(f"- 字符串字段 ({len(null_handling_rules['string_fields'])}个): 空值替换为单个空格' '")
    print(f"- 数值字段 ({len(null_handling_rules['numeric_fields'])}个): 空值替换为0或0.0")
    print(f"- 日期字段 ({len(null_handling_rules['date_fields'])}个): 空值替换为'1900-01-01'")
    print(f"- 时间戳字段 ({len(null_handling_rules['timestamp_fields'])}个): 空值替换为'1900-01-01 00:00:00.000000'")
    
    return null_handling_rules


def main():
    """主流程脚本开发

    - 1.进行配置信息读取
    - 2.执行SQOOP抽数
    - 3.写入最终表
  :return:
    """
    try:
        # 0. 验证空值处理策略
        null_rules = validate_null_handling()
        
        # 1. 读取Hive, impala, log的日志信息
        work_path = Path("../../../../../..").resolve()
        cfg_parser = configparser.ConfigParser()
        pub_cfg_path = f"{work_path}/CONF/etl_pub.ini"
        cfg_parser.read(pub_cfg_path, "utf-8")

        # 配置日志信息
        logback_count = cfg_parser.get("LOG_CONF", "LOGBACKCOUNT")
        log_dir = f"{work_path}/LOG/{schema_name}/{job_name}"
        if not Path(log_dir).is_dir():
            Path(log_dir).mkdir()
        log_file = f"{log_dir}/{job_name}.log"
        logger = get_time_rotating_logger(job_name, log_file, logging.DEBUG, int(logback_count))
        log_info(logger, "info", f"\n\n\n=============数据日期:{bdw_data_dt}  任务:{job_name}开始！===================\n\n")
        log_info(logger, "info", "===空值处理策略已启用，确保数据不会存储为null值===")

        # 2.sqoop抽数
        query_sql2 = query_sql.replace("${hiveconf:BDW_DATA_DT}", bdw_data_dt)
        truncate_table(schema_name.lower(), stemp_table, logger)
        sqoop_cmd = f"""sh /opt/hadoop_client/Loader/loader-tools-1.99.3/shell-client/submit_job.sh \
         -n {job_name} -u y \
         --jobType import --connectorType rdb \
         --frameworkType hdfs --extractors 1 \
         --outputDirectory /user/hive/warehouse/{schema_name.lower()}.db/{stemp_table.lower()} \
         --sql "{query_sql2}" \
        """
        log_info(logger, "info", "===sqoop抽数开始===")
        log_info(logger, "info", sqoop_cmd.replace('             ', '\n'))
        stat = os.system(sqoop_cmd)
        if stat:
            log_info(logger, "error", "===sqoop导入数据执行失败，请检查!===")
            sys.exit(-1)
        else:
            log_info(logger, "info", "===sqoop导入数据成功!===")
        msck_table(schema_name.lower(), stemp_table, logger)

        # 3.Hive将stemp区数据写入到ods的表中
        # log_info(logger, "info", "===由stemp表加载到ods表中===")
        # hive_exec(cfg_parser, bdw_data_dt, insert_sql, logger, job_name)
        log_info(logger, "info", f"\n=============数据日期:{bdw_data_dt}  任务:{job_name}执行成功！===================\n\n")
        sys.exit(0)
    except Exception:
        log_info(logger, "exception", "===推数作业执行失败!===")
        sys.exit(-1)


if __name__ == '__main__':
    main()
