import pandas as pd
import re
import os

def extract_param_info(sql_text, param_name):
    """
    从SQL文本中提取特定参数的信息，包括参数名、关联符号和值
    例如：GZCPDM = ? 或 GZCPDM = #{cpcode} 或 GZCPDM in ('1111','2222')
    """
    if not sql_text or pd.isna(sql_text):
        return None
    
    # 正则表达式匹配参数
    # 匹配格式：参数名 操作符 值
    pattern = rf'{param_name}\s*(=|!=|<>|>|<|>=|<=|LIKE|IN|NOT\s+IN|BETWEEN)\s*([^,;\)\s]+|\'[^\']*\'|\([^\)]*\)|#\{{[^}}]*\}}|\?)'
    
    match = re.search(pattern, sql_text, re.IGNORECASE)
    if match:
        operator = match.group(1).strip()
        value = match.group(2).strip()
        return f"{param_name} {operator} {value}"
    
    return None

def process_csv(file_path):
    # 检查文件是否存在
    if not os.path.exists(file_path):
        print(f"文件不存在: {file_path}")
        return
    
    # 读取CSV文件
    try:
        df = pd.read_csv(file_path)
        print(f"成功读取文件，共有 {len(df)} 行数据")
    except Exception as e:
        print(f"读取文件时出错: {e}")
        return
    
    # 找到可能包含SQL的列
    sql_column = None
    for col in df.columns:
        if 'sql' in col.lower() or 'query' in col.lower() or 'statement' in col.lower():
            sql_column = col
            break
    
    # 如果没找到，使用第一列
    if not sql_column and len(df.columns) > 0:
        sql_column = df.columns[0]
    
    print(f"使用列 '{sql_column}' 作为SQL内容来源")
    
    # 提取参数信息并添加到DataFrame
    df["GZCPDM参数"] = df[sql_column].apply(lambda x: extract_param_info(x, "GZCPDM"))
    df["GZDATE参数"] = df[sql_column].apply(lambda x: extract_param_info(x, "GZDATE"))
    
    # 保存结果
    output_file = file_path.replace(".csv", "_processed.csv")
    df.to_csv(output_file, index=False)
    
    print(f"处理完成，结果保存到: {output_file}")
    
    return df

if __name__ == "__main__":
    file_path = "evlgz_sql_extraction_20250523_191709.csv"
    
    # 检查文件路径是否为绝对路径
    if not os.path.isabs(file_path):
        # 假设文件与脚本在同一目录
        current_dir = os.path.dirname(os.path.abspath(__file__))
        file_path = os.path.join(current_dir, file_path)
    
    process_csv(file_path)