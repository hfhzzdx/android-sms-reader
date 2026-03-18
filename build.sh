#!/bin/bash

# 12123短信读取应用构建脚本

echo "开始构建12123短信读取应用..."

# 检查是否在项目根目录
if [ ! -f "settings.gradle" ]; then
    echo "错误：请在项目根目录运行此脚本"
    exit 1
fi

# 检查gradle是否安装
if ! command -v gradle &> /dev/null; then
    echo "错误：Gradle未安装，请先安装Gradle"
    echo "安装命令：sudo apt install gradle"
    exit 1
fi

echo "使用系统Gradle版本："
gradle --version | grep "Gradle"

# 清理构建
echo "清理构建..."
gradle clean

# 构建APK
echo "构建APK..."
gradle assembleDebug

# 检查构建是否成功
if [ $? -eq 0 ]; then
    echo "构建成功！"
    echo "APK文件位置："
    find . -name "*.apk" -type f | grep debug
else
    echo "构建失败，请检查错误信息"
    exit 1
fi

# 安装到设备（可选）
if [ "$1" = "--install" ]; then
    echo "安装到设备..."
    gradle installDebug
fi

echo "完成！"