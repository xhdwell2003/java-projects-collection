#!/bin/bash

# EVLGZ SQL提取工具启动脚本

# 检查Python是否安装
if ! command -v python3 &> /dev/null; then
    echo "错误: 未找到python3，请安装Python 3.x"
    exit 1
fi

# 检查依赖包
python3 -c "import pandas" 2>/dev/null
if [ $? -ne 0 ]; then
    echo "正在安装必要的依赖包..."
    pip3 install pandas openpyxl
fi

# 确保脚本有执行权限
chmod +x main.py

# 运行脚本
python3 main.py "$@"

echo "完成。"