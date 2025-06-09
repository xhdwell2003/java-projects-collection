#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import argparse
from extract_evlgz_sql import SqlExtractor

def main():
    """主函数"""
    from datetime import datetime
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    default_filename = f"evlgz_sql_extraction_{timestamp}.xlsx"
    
    parser = argparse.ArgumentParser(description='提取Java和XML文件中关于EVLGZ表的SQL语句')
    parser.add_argument('-d', '--directory', type=str, default='..',
                        help='要遍历的根目录路径，默认为上一级目录')
    parser.add_argument('-o', '--output', type=str, default=default_filename,
                        help=f'输出的Excel文件名，默认为带时间戳的文件名 (如:{default_filename})')
    
    args = parser.parse_args()
    
    # 确保目录路径存在
    if not os.path.exists(args.directory):
        print(f"错误: 目录 '{args.directory}' 不存在")
        return
    
    print(f"开始从 '{args.directory}' 中提取EVLGZ表的SQL语句...")
    
    # 创建提取器并执行提取
    extractor = SqlExtractor(args.directory)
    results = extractor.extract()
    
    if results:
        print(f"找到 {len(results)} 条包含EVLGZ表的SQL语句")
        # 导出到Excel文件
        if extractor.export_to_excel(args.output):
            print(f"数据已成功导出到 '{args.output}'")
    else:
        print("未找到包含EVLGZ表的SQL语句")

if __name__ == "__main__":
    main()