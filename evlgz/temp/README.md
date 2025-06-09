# EVLGZ SQL提取工具

这个工具用于从Java和XML文件中提取包含EVLGZ表的SQL语句，并将结果导出为Excel文件。

## 功能特点

- 自动遍历指定目录下的所有Java和XML文件
- 提取包含EVLGZ表的SQL语句及其关联的方法/ID
- 分析SQL语句中的GZDATE和GZCPDM条件
- 将结果导出为Excel表格，包含以下列：
  - 文件全路径
  - 方法名
  - SQL语句
  - GZDATE条件
  - GZCPDM条件

## 使用方法

### 基本使用

```bash
python main.py
```

这会使用默认配置：
- 遍历上一级目录（即 `..` 目录）
- 将结果导出到当前目录下的带时间戳的文件，格式为 `evlgz_sql_extraction_YYYYMMDD_HHMMSS.xlsx`

### 高级选项

```bash
python main.py -d /path/to/directory -o output.xlsx
```

参数说明：
- `-d`, `--directory`: 指定要遍历的根目录路径（默认为上一级目录）
- `-o`, `--output`: 指定输出的Excel文件名（默认为带时间戳的文件名，如`evlgz_sql_extraction_20250523_163300.xlsx`）

## 示例

1. 遍历当前目录并导出结果：
   ```bash
   python main.py -d .
   ```

2. 指定输出文件：
   ```bash
   python main.py -o my_results.xlsx
   ```

3. 指定特定目录和输出文件：
   ```bash
   python main.py -d /path/to/java/project -o results.xlsx
   ```

## 注意事项

- 文件较大或SQL语句复杂时，解析可能需要较长时间
- 对于复杂拼接的SQL语句，提取可能不完全准确
- 默认使用UTF-8编码读取文件，如有其他编码需求请修改源代码

## 依赖库

- pandas：用于数据处理和Excel导出
- openpyxl：用于Excel格式化输出