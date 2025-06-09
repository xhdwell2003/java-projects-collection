import re
import csv
import os
from collections import defaultdict

def extract_sql_params(file_path):
    """
    从CSV文件中提取与EVLGZ表相关的SQL参数
    """
    if not os.path.exists(file_path):
        print(f"文件 {file_path} 不存在")
        return
    
    # 用于存储结果的数据结构
    evlgz_queries = []
    table_columns = set()
    join_tables = set()
    where_conditions = defaultdict(int)
    
    with open(file_path, 'r', encoding='utf-8') as file:
        csv_reader = csv.reader(file, delimiter='|')
        
        for row in csv_reader:
            if len(row) < 4:  # 确保行有足够的数据
                continue
                
            # 获取SQL内容，它应该在第4个元素（索引3）
            sql_content = row[3] if len(row) > 3 else ""
            
            # 检查是否包含EVLGZ表
            if "TGCORE.EVLGZ" in sql_content.upper():
                # 添加到查询列表
                source_file = row[0] if len(row) > 0 else "未知文件"
                method_name = row[1] if len(row) > 1 else "未知方法"
                line_number = row[2] if len(row) > 2 else "未知行号"
                
                evlgz_queries.append({
                    "source_file": source_file,
                    "method_name": method_name,
                    "line_number": line_number,
                    "sql_content": sql_content
                })
                
                # 提取列名
                # 使用正则表达式查找EVLGZ后面的列引用
                column_pattern = r'EVLGZ\s*\.\s*(\w+)|EVLGZ\s*[^\.]*\.([A-Za-z0-9_]+)|(?:FROM|JOIN)\s+TGCORE\.EVLGZ\s+(?:AS\s+)?\w+\s+.*?SELECT\s+(.*?)\s+FROM|EVLGZ\s*\([^)]*\)\s*\.\s*(\w+)|[^\.a-zA-Z0-9_]([A-Z]{2,}(?:CPDM|DATE|ZHDM|KMDM|ZHMC|ZQDM|GZSL|DWCB|CBZE|CBBL|GZJG|SZZE|SZBL|GZZZ|ZZBL|GZFF|ZZKM|QYXX|BZXX))[^\.a-zA-Z0-9_]'
                columns = re.findall(column_pattern, sql_content, re.IGNORECASE)
                for col_match in columns:
                    for col in col_match:
                        if col and col.strip():
                            table_columns.add(col.strip())
                
                # 提取JOIN的表
                join_pattern = r'JOIN\s+([A-Za-z0-9_\.]+)'
                joins = re.findall(join_pattern, sql_content, re.IGNORECASE)
                for join in joins:
                    if join != "TGCORE.EVLGZ":
                        join_tables.add(join.strip())
                
                # 提取WHERE条件
                where_pattern = r'WHERE\s+(.*?)(?:GROUP BY|ORDER BY|HAVING|UNION|$)'
                where_matches = re.findall(where_pattern, sql_content, re.IGNORECASE | re.DOTALL)
                
                for where_clause in where_matches:
                    # 分割不同的条件（通过AND或OR）
                    conditions = re.split(r'\s+AND\s+|\s+OR\s+', where_clause, flags=re.IGNORECASE)
                    for condition in conditions:
                        condition = condition.strip()
                        if condition and "GZCPDM" in condition or "GZDATE" in condition:
                            where_conditions[condition] += 1
    
    # 输出结果
    print(f"找到 {len(evlgz_queries)} 个包含EVLGZ表的SQL查询")
    print("\n提取的列名:")
    for col in sorted(table_columns):
        print(f"- {col}")
    
    print("\nJOIN的表:")
    for table in sorted(join_tables):
        print(f"- {table}")
    
    print("\n常见WHERE条件:")
    for condition, count in sorted(where_conditions.items(), key=lambda x: x[1], reverse=True)[:10]:
        print(f"- {condition} (出现 {count} 次)")
    
    print("\n详细SQL查询:")
    for i, query in enumerate(evlgz_queries[:5], 1):  # 只显示前5个作为示例
        print(f"\n{i}. 来源: {query['source_file']} - {query['method_name']} (行 {query['line_number']})")
        print(f"   SQL片段: {query['sql_content'][:200]}..." if len(query['sql_content']) > 200 else f"   SQL片段: {query['sql_content']}")
    
    if len(evlgz_queries) > 5:
        print(f"\n... 共有 {len(evlgz_queries)} 个查询，只显示了前5个")
    
    return {
        "queries_count": len(evlgz_queries),
        "columns": sorted(table_columns),
        "join_tables": sorted(join_tables),
        "where_conditions": dict(where_conditions),
        "queries": evlgz_queries
    }

def generate_csv_report(results, output_file):
    """
    生成CSV报告
    """
    with open(output_file, 'w', newline='', encoding='utf-8') as file:
        writer = csv.writer(file)
        writer.writerow(['源文件', '方法', '行号', '表', '列使用', '连接表', '条件'])
        
        for query in results["queries"]:
            source_file = query["source_file"]
            method_name = query["method_name"]
            line_number = query["line_number"]
            sql = query["sql_content"]
            
            # 分析列使用
            columns_used = []
            for col in results["columns"]:
                if col in sql:
                    columns_used.append(col)
            
            # 分析JOIN表
            joins_used = []
            for table in results["join_tables"]:
                if table in sql:
                    joins_used.append(table)
            
            # 分析条件
            conditions_used = []
            for condition in results["where_conditions"]:
                if condition in sql:
                    conditions_used.append(condition)
            
            writer.writerow([
                source_file,
                method_name,
                line_number,
                "TGCORE.EVLGZ",
                ", ".join(columns_used),
                ", ".join(joins_used),
                ", ".join(conditions_used[:3])  # 只显示前三个条件
            ])
    
    print(f"报告已生成到 {output_file}")

if __name__ == "__main__":
    input_file = "evlgz/evlgz_sql_extraction_20250523_191709.csv"
    output_file = "evlgz/evlgz_analysis_report.csv"
    
    results = extract_sql_params(input_file)
    if results:
        generate_csv_report(results, output_file)
