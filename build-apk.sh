#!/bin/bash

# Android短信读取应用构建脚本
# 作者：霍风浩的个人开发助手

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== Android短信读取应用构建脚本 ===${NC}"
echo "项目路径: $(pwd)"
echo "构建时间: $(date)"

# 检查Java
echo -e "\n${YELLOW}[1/6] 检查Java环境...${NC}"
if command -v java &> /dev/null; then
    java_version=$(java -version 2>&1 | head -1 | cut -d'"' -f2)
    echo -e "${GREEN}✓ Java版本: $java_version${NC}"
else
    echo -e "${RED}✗ Java未安装${NC}"
    echo "请安装Java JDK 11或更高版本"
    exit 1
fi

# 检查Gradle
echo -e "\n${YELLOW}[2/6] 检查Gradle...${NC}"
if [ -f "./gradlew" ]; then
    echo -e "${GREEN}✓ Gradle Wrapper可用${NC}"
    chmod +x ./gradlew
else
    echo -e "${RED}✗ Gradle Wrapper未找到${NC}"
    exit 1
fi

# 清理项目
echo -e "\n${YELLOW}[3/6] 清理项目...${NC}"
if ./gradlew clean > /tmp/gradle-clean.log 2>&1; then
    echo -e "${GREEN}✓ 项目清理成功${NC}"
else
    echo -e "${YELLOW}⚠ 清理过程中有警告，继续构建...${NC}"
    cat /tmp/gradle-clean.log | tail -20
fi

# 下载依赖
echo -e "\n${YELLOW}[4/6] 下载依赖...${NC}"
echo "这可能需要几分钟，请耐心等待..."
if ./gradlew dependencies > /tmp/gradle-deps.log 2>&1; then
    echo -e "${GREEN}✓ 依赖下载成功${NC}"
else
    echo -e "${YELLOW}⚠ 依赖下载可能有警告，继续构建...${NC}"
    cat /tmp/gradle-deps.log | grep -i "error\|warning" | head -10
fi

# 构建APK
echo -e "\n${YELLOW}[5/6] 构建APK...${NC}"
echo "正在编译应用..."
if ./gradlew assembleDebug > /tmp/gradle-build.log 2>&1; then
    echo -e "${GREEN}✓ APK构建成功！${NC}"
else
    echo -e "${RED}✗ APK构建失败${NC}"
    echo "错误信息："
    cat /tmp/gradle-build.log | grep -i "error\|failed\|exception" | head -20
    echo -e "\n完整日志：/tmp/gradle-build.log"
    exit 1
fi

# 查找APK文件
echo -e "\n${YELLOW}[6/6] 查找APK文件...${NC}"
APK_PATH=$(find . -name "app-debug.apk" -type f 2>/dev/null | head -1)

if [ -n "$APK_PATH" ]; then
    APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
    echo -e "${GREEN}✓ APK文件找到: $APK_PATH${NC}"
    echo -e "${GREEN}✓ 文件大小: $APK_SIZE${NC}"
    
    # 显示APK信息
    echo -e "\n${BLUE}=== APK信息 ===${NC}"
    echo "路径: $(readlink -f "$APK_PATH")"
    echo "目录: $(dirname "$(readlink -f "$APK_PATH")")"
    
    # 复制到当前目录以便访问
    cp "$APK_PATH" ./sms-reader-app-debug.apk
    echo -e "${GREEN}✓ 已复制到: ./sms-reader-app-debug.apk${NC}"
    
    # 显示构建成功信息
    echo -e "\n${GREEN}🎉 构建完成！${NC}"
    echo "APK文件已准备好："
    echo "1. $(readlink -f ./sms-reader-app-debug.apk)"
    echo "2. $APK_PATH"
    
    echo -e "\n${BLUE}=== 安装和使用 ===${NC}"
    echo "安装到设备: adb install ./sms-reader-app-debug.apk"
    echo "查看日志: adb logcat | grep SmsReader"
    echo "测试短信: 发送12123开头的违法停车通知短信"
    
else
    echo -e "${RED}✗ 未找到APK文件${NC}"
    echo "尝试手动查找："
    find . -name "*.apk" -type f 2>/dev/null
    exit 1
fi

# 清理临时文件
rm -f /tmp/gradle-*.log

echo -e "\n${BLUE}=== 构建完成于: $(date) ===${NC}"