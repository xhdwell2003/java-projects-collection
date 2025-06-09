@echo off
REM EVLGZ SQL提取工具启动脚本 (Windows版)

REM 检查Python是否安装
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到python，请安装Python 3.x
    exit /b 1
)

REM 检查依赖包
python -c "import pandas" >nul 2>&1
if %errorlevel% neq 0 (
    echo 正在安装必要的依赖包...
    pip install pandas openpyxl
    if %errorlevel% neq 0 (
        echo 安装依赖包失败，请手动安装pandas和openpyxl
        exit /b 1
    )
)

REM 运行脚本
python main.py %*

echo 完成。
pause