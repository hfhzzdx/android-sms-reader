#!/bin/bash

# 简单构建脚本 - 专注于生成APK

set -e

echo "=== 开始构建Android短信读取应用 ==="
echo "时间: $(date)"
echo "项目目录: $(pwd)"

# 设置环境变量
export ANDROID_HOME=~/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools

# 检查环境
echo -e "\n[1/4] 检查构建环境..."
if [ ! -d "$ANDROID_HOME" ]; then
    echo "错误: Android SDK未找到在 $ANDROID_HOME"
    echo "请先运行: ./setup-android-sdk.sh"
    exit 1
fi

if [ ! -f "./gradlew" ]; then
    echo "错误: gradlew未找到"
    exit 1
fi

chmod +x ./gradlew
echo "✓ 环境检查通过"

# 清理项目
echo -e "\n[2/4] 清理项目..."
./gradlew clean --no-daemon 2>&1 | grep -i "success\|error\|fail" || true
echo "✓ 项目清理完成"

# 构建APK（带超时）
echo -e "\n[3/4] 构建APK（可能需要5-10分钟）..."
echo "开始时间: $(date)"
echo "这可能需要一些时间，请耐心等待..."

# 在后台运行构建，同时显示进度
./gradlew assembleDebug --no-daemon --console=plain 2>&1 | tee build.log &
BUILD_PID=$!

# 等待构建完成或超时
TIMEOUT=600  # 10分钟
START_TIME=$(date +%s)
BUILD_SUCCESS=false

while true; do
    # 检查进程是否还在运行
    if ! kill -0 $BUILD_PID 2>/dev/null; then
        wait $BUILD_PID
        BUILD_EXIT=$?
        if [ $BUILD_EXIT -eq 0 ]; then
            BUILD_SUCCESS=true
        fi
        break
    fi
    
    # 检查是否超时
    CURRENT_TIME=$(date +%s)
    ELAPSED=$((CURRENT_TIME - START_TIME))
    
    if [ $ELAPSED -gt $TIMEOUT ]; then
        echo "构建超时（超过10分钟），终止进程..."
        kill $BUILD_PID 2>/dev/null
        break
    fi
    
    # 显示进度
    if [ $((ELAPSED % 30)) -eq 0 ]; then
        echo "构建中... 已等待 ${ELAPSED}秒"
        # 检查是否有APK生成
        if find . -name "*.apk" -type f 2>/dev/null | grep -q .; then
            echo "发现APK文件！"
        fi
    fi
    
    sleep 5
done

# 检查构建结果
if $BUILD_SUCCESS; then
    echo -e "\n✓ APK构建成功！"
    echo "完成时间: $(date)"
else
    echo -e "\n✗ APK构建失败"
    echo "最后10行日志:"
    tail -10 build.log
    exit 1
fi

# 查找APK文件
echo -e "\n[4/4] 查找APK文件..."
APK_FILES=$(find . -name "*.apk" -type f 2>/dev/null)

if [ -z "$APK_FILES" ]; then
    echo "错误: 未找到APK文件"
    echo "构建日志: build.log"
    exit 1
fi

echo "找到APK文件:"
for apk in $APK_FILES; do
    SIZE=$(du -h "$apk" | cut -f1)
    echo "  - $apk ($SIZE)"
done

# 复制主要APK到当前目录
MAIN_APK=$(echo "$APK_FILES" | head -1)
if [ -n "$MAIN_APK" ]; then
    cp "$MAIN_APK" ./sms-reader-app.apk
    echo -e "\n✓ APK已复制到: ./sms-reader-app.apk"
    
    # 显示文件信息
    echo -e "\n=== APK文件信息 ==="
    echo "文件名: $(basename ./sms-reader-app.apk)"
    echo "文件大小: $(du -h ./sms-reader-app.apk | cut -f1)"
    echo "完整路径: $(readlink -f ./sms-reader-app.apk)"
    echo "MD5: $(md5sum ./sms-reader-app.apk | cut -d' ' -f1)"
    
    # 预期安装命令
    echo -e "\n=== 安装和使用 ==="
    echo "安装到设备: adb install ./sms-reader-app.apk"
    echo "重新安装: adb install -r ./sms-reader-app.apk"
    echo "查看日志: adb logcat | grep -i sms"
    
    # 测试说明
    echo -e "\n=== 测试说明 ==="
    echo "1. 安装应用到Android设备"
    echo "2. 授予应用短信读取权限"
    echo "3. 发送测试短信（12123开头发件人）"
    echo "4. 应用会自动解析并提醒"
    
    # 示例短信
    echo -e "\n=== 测试短信示例 ==="
    echo "发件人: 12123"
    echo "内容: 【河南省交警】您的小型新能源汽车豫ADS2567于2025年11月18日9时32分在河南省郑州市建设路（京广路至大学路）路南未按规定停放已被记录，将依法予以处罚。并请立即驶离，未驶离的，将依法拖移，谢谢配合!"
fi

echo -e "\n=== 构建完成: $(date) ==="